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

public class DefaultClusterNodeInfo implements ClusterNodeInfo {

	private final boolean coordinator;
	private final String ipAddress;
	private final long clusterVersion;
	private final int clusterPosition;
	private final int clusterSize;

	public DefaultClusterNodeInfo(boolean coordinator, String ipAddress, long clusterVersion, int clusterPosition, int clusterSize) {
		this.coordinator = coordinator;
		this.ipAddress = ipAddress;
		this.clusterVersion = clusterVersion;
		this.clusterPosition = clusterPosition;
		this.clusterSize = clusterSize;
	}
	
	@Override
	public boolean isCoordinator() {
		return coordinator;
	}

	@Override
	public long getClusterVersion() {
		return clusterVersion;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	@Override
	public int getClusterPosition() {
		return clusterPosition;
	}

	@Override
	public int getClusterSize() {
		return clusterSize;
	}
}
