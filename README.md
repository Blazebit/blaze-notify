[![Build Status](https://travis-ci.org/Blazebit/blaze-notify.svg?branch=master)](https://travis-ci.org/Blazebit/blaze-notify)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-notify-notification-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-notify-notification-impl)
[![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com)

[![Javadoc - Notification](https://www.javadoc.io/badge/com.blazebit/blaze-notify-notification-api.svg?label=javadoc%20-%20notification-api)](http://www.javadoc.io/doc/com.blazebit/blaze-notify-notification-api)

Blaze-Notify
==========
Blaze-Notify is a toolkit that can be used to implement notification processing for an application.

What is it?
===========

Blaze-Notify provides an abstraction for notification processing as well as different implementations
that can be integrated into existing applications.

It allows to create templated notification campaigns that can be scheduled and sent to various channels.
The Blaze-Persistence integration makes it possible to efficiently implement recipient selection.

Features
==============

Blaze-Notify has support for

* Channels: SMTP, AWS SES, AWS SNS, Webhook
* Templating: Freemarker
* Bounce detection and handling
* Selector based campaigns to be able to target specific users
* In-memory model for high-performance and JPA model for persistence
* Cluster support
* Scheduled and recurring notification campaigns

How to use it?
==============

WARNING: Blaze-Notify is still under heavy initial development and is not yet intended to be used!

Blaze-Notify is split up into different modules. We recommend that you define a version property in your parent pom that you can use for all artifacts. Modules are all released in one batch so you can safely increment just that property. 

```xml
<properties>
    <blaze-persistence.version>1.0.0-SNAPSHOT</blaze-persistence.version>
</properties>
```

Alternatively you can also use our BOM in the `dependencyManagement` section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.blazebit</groupId>
            <artifactId>blaze-notify-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>    
    </dependencies>
</dependencyManagement>
```