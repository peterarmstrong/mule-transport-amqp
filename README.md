Mule AMQP Transport
===================

Supported AMQP Versions
-----------------------

This transport is based on the RabbitMQ Java Client, which is compatible with brokers supporting AMQP version 0.9.1.


Integration Testing
-------------------

Simply run:

    mvn -Pit clean verify

The integration tests rely on a locally running RabbitMQ broker and an OS that can run shell scripts (for the setup of the testing vhost and user).


Not (Yet) Supported
-------------------

- Local transactions