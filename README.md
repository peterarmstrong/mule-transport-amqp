Mule AMQP Transport
===================

Supported AMQP Versions
-----------------------

This transport is based on the RabbitMQ Java Client, which is compatible with brokers supporting AMQP version 0.9.1.


Features
--------

- Inbound message receiving via subscription to existing or redefined exchanges and queues.
- Outbound message publication to existing or redefined exchanges.
- Message requesting.
- Support of all AMQP's message properties, including custom headers.
- Support of reply to (publishing replies to the default exchange).
- Support of automatic, Mule-driven and manual message acknowledgment.

Integration Testing
-------------------

Run:

    mvn -Pit clean verify

The integration tests rely on a locally running RabbitMQ broker and an OS that can run shell scripts (for the setup of the testing vhost and user).


Not (Yet) Supported
-------------------

- Local transactions