[![Build Status](https://travis-ci.org/Blazebit/blaze-notify.svg?branch=master)](https://travis-ci.org/Blazebit/blaze-notify)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-notify-notification-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.blazebit/blaze-notify-notification-impl)
[![Slack Status](https://blazebit.herokuapp.com/badge.svg)](https://blazebit.herokuapp.com)

[![Javadoc - Notification](https://www.javadoc.io/badge/com.blazebit/blaze-notify-notification-api.svg?label=javadoc%20-%20notification-api)](http://www.javadoc.io/doc/com.blazebit/blaze-notify-notification-api)

Blaze-Notify
==========
Blaze-Notify is a toolkit that can be used to implement notification processing for an application.

What is it?
===========

Blaze-Notify provides an abstraction for notification processing as well as a memory and a JPA base implementation
that can be integrated into existing applications.

It allows to create templated notification campaigns that can be scheduled and sent to various channels.
The Blaze-Expression integration makes it possible to efficiently implement recipient selection.

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
    <blaze-notify.version>1.0.0-Alpha3</blaze-notify.version>
    <blaze-expression.version>1.0.0-Alpha5</blaze-expression.version>
    <blaze-job.version>1.0.0-Alpha5</blaze-job.version>
</properties>
```

Alternatively you can also use our BOM in the `dependencyManagement` section.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.blazebit</groupId>
            <artifactId>blaze-notify-bom</artifactId>
            <version>${blaze-notify.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>    
    </dependencies>
</dependencyManagement>
```

## Dependencies setup

For compiling you will only need API artifacts and for the runtime you need impl and integration artifacts.

Blaze-Notify Core module dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-core-api</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-core-impl</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Notify JPA module dependencies for production workload

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-jpa-model-base</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-jpa-storage</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Notify JPA module Blaze-Expression integration dependencies

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-jpa-model-expression</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-recipient-resolver-expression</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>blaze-expression-persistence</artifactId>
    <version>${blaze-expression.version}</version>
</dependency>
```

Blaze-Notify Memory module dependencies for in-memory tests

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-memory-model</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-memory-storage</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Actor scheduler implementation for Blaze-Notify. Use either of the two, the Spring module if you are on Spring

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-actor-scheduler-executor</artifactId>
    <version>${blaze-actor.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-actor-scheduler-spring</artifactId>
    <version>${blaze-actor.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Job Schedule support for Blaze-Notify. Use either of the two, the Spring module if you are on Spring

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-job-schedule-cron</artifactId>
    <version>${blaze-job.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-job-schedule-spring</artifactId>
    <version>${blaze-job.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Job Transaction support for Blaze-Notify. Use either of the three, depending on the transaction API of the target environment

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-job-transaction-jpa</artifactId>
    <version>${blaze-job.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-job-transaction-jta</artifactId>
    <version>${blaze-job.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-job-transaction-spring</artifactId>
    <version>${blaze-job.version}</version>
    <scope>runtime</scope>
</dependency>
```

Blaze-Notify Template support

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-template-api</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-template-freemarker</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Notify Processor base implementations

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-processor-hibernate-insert-select</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-processor-memory</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
```

Channels
=========

Blaze-Notify has a SPI for custom channels but offers quite a few channels out of the box

Blaze-Notify SMTP channel

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-channel-smtp</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Notify Slack channel

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-channel-slack</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
```

Blaze-Notify Memory channel

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-channel-memory</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
```

E-Mail base model
===========

Blaze-Notify provides a JPA model that can be used as a basis for implementing E-Mail notifications with bounce detection.

```xml
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-email-model</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
<!-- Can be used to handle E-Mail bounces published to AWS SQS via AWS SNS -->
<dependency>
    <groupId>com.blazebit</groupId>
    <artifactId>blaze-notify-email-sns-sqs-feedback</artifactId>
    <version>${blaze-notify.version}</version>
    <scope>compile</scope>
</dependency>
```

Documentation
=========

Currently there is no documentation other than the Javadoc.
 
Quick-start
=================

Working with Blaze-Notify is as simple as persisting jobs that are processed, but first one needs to start a notification job context.

```java
NotificationJobContext jobContext = NotificationJobContext.builder()
    .withService(EntityManager.class, entityManager)
    .withService(TemplateContext.class, TemplateContext.builder().createContext())
    .withProperty(ExecutorServiceScheduler.EXECUTOR_SERVICE_PROPERTY, Executors.newScheduledThreadPool(2))
    // No need for trigger based jobs in this example
    .withJobProcessorFactory(NotificationJobProcessorFactory.of((context, jobTrigger) -> null))
    .withJobInstanceProcessorFactory(NotificationJobInstanceProcessorFactory.of((context, jobInstance) -> EmailNotificationJobInstanceProcessor.INSTANCE))
    // Only necessary for notification producing jobs
    .withRecipientResolver(NotificationRecipientResolver.of())
    .withProperty(SmtpChannel.SMTP_HOST_PROPERTY, "192.168.99.100")
    .withProperty(SmtpChannel.SMTP_PORT_PROPERTY, 25)
    .withProperty(SmtpChannel.SMTP_USER_PROPERTY, "test")
    .withProperty(SmtpChannel.SMTP_PASSWORD_PROPERTY, "test")
    .createContext();
```

With that in place, we can create a notification job.

```java
FromEmail from = entityManager.createQuery("SELECT e FROM FromEmail e WHERE e.email = 'no-reply@blazebit.com'", FromEmail.class)
    .setMaxResults(1)
    .getSingleResult();
EmailNotification emailNotification = new EmailNotification();
emailNotification.setTo("test@blazebit.com");
emailNotification.setChannelType("smtp");
emailNotification.setFrom(from);
emailNotification.setSubject("Hello");
emailNotification.setBodyText("Hey my friend!");
emailNotification.setScheduleTime(Instant.now());
jobContext.getJobManager().addJobInstance(emailNotification);
```

The job is scheduled, executed and then marked as done.

Licensing
=========

This distribution, as a whole, is licensed under the terms of the Apache
License, Version 2.0 (see LICENSE.txt).

References
==========

Project Site:              https://notify.blazebit.com (coming at some point)
