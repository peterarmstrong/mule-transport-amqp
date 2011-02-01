Mule AMQP Transport
===================

Read the [complete user guide](http://github.com/mulesoft/mule-transport-amqp/blob/master/GUIDE.md).

Supported AMQP Versions
-----------------------

This transport is based on the RabbitMQ Java Client, which is compatible with brokers supporting AMQP version 0.9.1.


Features
--------

- Inbound message receiving via subscription to existing or declared exchanges and queues.
- Outbound message publication to existing or declared exchanges.
- Outbound request-response pattern supported via temporary reply queues.
- Synchronous Message requesting with time-out.
- Passive or active-only exchange and queue declarations.
- Support for connection fallback accross a list of AMQP hosts.
- Support of all AMQP's message properties, including custom headers.
- Support of reply to (publishing replies to the default exchange).
- Support of automatic, Mule-driven and manual message acknowledgment.
- Support of manual message rejection.
- Support of the default exchange semantics in outbound endpoints.
- Support of mandatory and immediate publish parameters and handling of returned (undelivered) messages.
- Support of prefetch size and count "quality of service" settings.
- Support of noLocal and exclusive consumers.


Integration Testing
-------------------

Run:

    mvn -Pit clean verify

The integration tests rely on a locally running RabbitMQ broker and an OS that can run shell scripts (for the setup of the testing vhost and user).

Maven Support
-----

Add the following repository:

    `<repository>
      <id>muleforge-repo</id>
      <name>MuleForge Repository</name>
      <url>http://repository.muleforge.org</url>
      <layout>default</layout>
    </repository>`

To add the Mule AMQP transport to a Maven project add the following dependency:

    `<dependency>
      <groupId>org.mule.transports</groupId>
      <artifactId>mule-transport-amqp</artifactId>
      <version>3.1.0</version>
    </dependency>`


Not (Yet) Supported
-------------------

- Local transactions
