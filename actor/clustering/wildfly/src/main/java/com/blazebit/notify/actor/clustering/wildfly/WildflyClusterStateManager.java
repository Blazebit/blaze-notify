/*
 * Copyright 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.notify.actor.clustering.wildfly;

import com.blazebit.notify.actor.spi.ClusterNodeInfo;
import com.blazebit.notify.actor.spi.ClusterStateListener;
import com.blazebit.notify.actor.spi.ClusterStateManager;
import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.dispatcher.CommandResponse;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WildflyClusterStateManager implements ClusterStateManager {

    private static final Logger LOG = Logger.getLogger(WildflyClusterStateManager.class.getName());
    private static final Node[] EMPTY = new Node[0];

//    @Resource(lookup = "java:jboss/clustering/group/ee")
    private Group channelGroup;

//    @Resource(lookup = "java:jboss/clustering/dispatcher/ee")
    private CommandDispatcherFactory factory;

    private final List<ClusterStateListener> clusterStateListeners = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, List<java.util.function.Consumer<Serializable>>> listeners = new ConcurrentHashMap<>();
    private final Node[] localNode;
    private final AtomicReference<ClusterNodeInfo> currentNodeInfo = new AtomicReference<>(new DefaultClusterNodeInfo(true, "127.0.0.1", 0L, 0, 1));
    private volatile CommandDispatcher<WildflyClusterStateManager> fireEventDispatcher;

    public WildflyClusterStateManager(Group channelGroup, CommandDispatcherFactory factory) {
        this.channelGroup = channelGroup;
        this.factory = factory;
        this.localNode = new Node[]{ channelGroup.getLocalNode() };
    }

    public void start() {
        final Node localNode = channelGroup.getLocalNode();
        updateCurrentPosition(localNode, channelGroup.getNodes());

//	    for (Node node : nodes) {
//	        String text = "I execute on server " + node.getName() + " and address " + node.getSocketAddress();
//
//	        try (CommandDispatcher<String> dispatcher = this.factory.createCommandDispatcher("id1", text)) {
//	            dispatcher.executeOnNode(new PrintWriterCommand(), node);
//	        } catch (Exception ex) {
//	        	System.err.println("Could not broadcast!");
//	        	ex.printStackTrace(System.err);
//	        }
//	    }

        fireEventDispatcher = this.factory.createCommandDispatcher("fireEventDispatcher", this);

        channelGroup.addListener(new Group.Listener() {
            @Override
            public void membershipChanged(List<Node> previousMembers, List<Node> members, boolean merged) {
                updateCurrentPosition(localNode, members);
            }
        });
    }

    public void close() {
        fireEventDispatcher.close();
    }

    @Override
    public ClusterNodeInfo getCurrentNodeInfo() {
        return currentNodeInfo.get();
    }

    @Override
    public void registerListener(ClusterStateListener listener) {
        listener.onClusterStateChanged(currentNodeInfo.get());
        clusterStateListeners.add(listener);
    }

    @Override
    public <T extends Serializable> void registerListener(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add((java.util.function.Consumer<Serializable>) listener);
    }

    public void fireEventLocally(Serializable event) {
        java.util.function.Consumer<Class<?>> consumer = eventClass -> {
            List<java.util.function.Consumer<Serializable>> consumers = listeners.get(eventClass);
            if (consumers != null) {
                consumers.forEach(c -> c.accept(event));
            }
        };
        Class<?> clazz = event.getClass();
        Set<Class<?>> visitedClasses = new HashSet<>();
        do {
            consumer.accept(clazz);
            visitInterfaces(consumer, clazz, visitedClasses);
            clazz = clazz.getSuperclass();
        } while (clazz != null);
    }

    private void visitInterfaces(java.util.function.Consumer<Class<?>> consumer, Class<?> clazz, Set<Class<?>> visitedClasses) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> interfaceClass = interfaces[i];
            if (visitedClasses.add(interfaceClass)) {
                consumer.accept(interfaceClass);
                visitInterfaces(consumer, interfaceClass, visitedClasses);
            }
        }
    }

    @Override
    public void fireEvent(Serializable event) {
        fireEvent(event, EMPTY);
    }

    @Override
    public void fireEventExcludeSelf(Serializable event) {
        fireEvent(event, localNode);
    }

    public void fireEvent(Serializable event, Node[] excludedNodes) {
        try {
            Map<Node, CommandResponse<Object>> results = fireEventDispatcher.executeOnCluster(new FireEventCommand(event), excludedNodes);
            for (final Map.Entry<Node, CommandResponse<Object>> result : results.entrySet()) {
                LOG.fine(() -> {
                    try {
                        return "Command result: Node [" + result.getKey().getName() + "@" + result.getKey().getSocketAddress().toString() + "]: "
                                + (result.getValue() == null ? "null" : (result.getValue().get() == null ? "null" : result.getValue().get().toString()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Could not broadcast!", ex);
        }
    }

    public static class FireEventCommand implements Command<Object, WildflyClusterStateManager> {
        private static final long serialVersionUID = 1L;
        private Serializable event;

        public FireEventCommand(Serializable event) {
            this.event = event;
        }

        @Override
        public Object execute(WildflyClusterStateManager context) throws Exception {
            LOG.fine("Fire event");
            context.fireEventLocally(event);
            return "";
        }

    }

    private void updateCurrentPosition(Node localNode, List<Node> nodes) {
        List<Node> nodeList = new ArrayList<>(nodes.size() + 1);

        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (!localNode.equals(n)) {
                nodeList.add(n);
            }
        }

        nodeList.add(localNode);
        Collections.sort(nodeList, NODE_COMPARATOR);
        int newPosition = nodeList.indexOf(localNode);

        boolean isCoordinator = channelGroup.isCoordinator();

        ClusterNodeInfo old = currentNodeInfo.get();
        ClusterNodeInfo nodeInfo = new DefaultClusterNodeInfo(isCoordinator, localNode.getSocketAddress().getAddress().getHostAddress(), old.getClusterVersion() + 1L, newPosition, nodeList.size());
        currentNodeInfo.compareAndSet(old, nodeInfo);
        LOG.info("Updated cluster position to: " + newPosition + " of " + members(nodeList));
        LOG.info("ChannelGroup members: " + members(channelGroup.getNodes()));
        clusterStateListeners.forEach(l -> l.onClusterStateChanged(nodeInfo));
    }

    private static String members(List<Node> nodeList) {
        return nodeList.stream().map(node -> node.getSocketAddress().getAddress().getHostAddress()).collect(Collectors.joining(", ", "(", ")"));
    }

//	public static class PrintWriterCommand implements Command<Object, String> {
//
//		private static final long serialVersionUID = 1L;
//
//		@Override
//	    public Object execute(String str) throws Exception {
//	        System.out.println(str);
//	        return null;
//	    }
//
//	}

    private static final Comparator<Node> NODE_COMPARATOR = new Comparator<Node>() {

        @Override
        public int compare(Node o1, Node o2) {
            byte[] b1 = o1.getSocketAddress().getAddress().getAddress();
            byte[] b2 = o2.getSocketAddress().getAddress().getAddress();
            for (int i = 0; i < b1.length; i++) {
                int cmp = b1[i] - b2[i];
                if (cmp != 0) {
                    return cmp;
                }
            }

            return Integer.compare(o1.getSocketAddress().getPort(), o2.getSocketAddress().getPort());
        }

    };
}
