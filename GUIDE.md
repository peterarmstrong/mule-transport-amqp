Mule AMQP Transport - User Guide
================================

Configuration Reference
-----------------------

### Connector Attributes
<!--
	Generated with: http://svn.codehaus.org/mule/branches/mule-2.0.x/tools/schemadocs/src/main/resources/xslt/single-element.xsl
	Parameter     : elementName=connector
-->

The AMQP connector defines what broker to connect to, which credentials to use when doing so and all the common properties used by the inbound and outbound endpoints using this connector.

It is possible to create several connectors connected to the same broker for the purpose of having different sets of common properties that the endpoints will use. 

<table class="confluenceTable">
  <th style="width:10%" class="confluenceTh">Name</th><th style="width:10%" class="confluenceTh">Type</th><th style="width:10%" class="confluenceTh">Required</th><th style="width:10%" class="confluenceTh">Default</th><th class="confluenceTh">Description</th>
  <tr>
    <td rowspan="1" class="confluenceTd">host</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">localhost</td><td class="confluenceTd">
      <p>
          The main AMQP broker host to connect to.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">port</td><td style="text-align: center" class="confluenceTd">port number</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">5672</td><td class="confluenceTd">
      <p>
          The port to use to connect to the main
          AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">fallbackAddresses</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
          A comma-separated list of "host:port" or
          "host", defining fallback brokers to attempt connection
          to, should the connection to main broker fail.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">virtualHost</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">/</td><td class="confluenceTd">
      <p>
          The virtual host to connect to on the
          AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">username</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">guest</td><td class="confluenceTd">
      <p>
          The user name to use to connect to the
          AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">password</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">guest</td><td class="confluenceTd">
      <p>
          The password to use to connect to the
          AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">deliveryMode</td><td style="text-align: center" class="confluenceTd"><b>PERSISTENT</b> / <b>NON_PERSISTENT</b></td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">PERSISTENT</td><td class="confluenceTd">
      <p>
          The delivery mode to use when publishing
          to the AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">priority</td><td style="text-align: center" class="confluenceTd"></td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">0</td><td class="confluenceTd">
      <p>
          The priority to use when publishing to
          the AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">mandatory</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">false</td><td class="confluenceTd">
      <p>
          This flag tells the server how to react
          if the message cannot be
          routed to a queue. If this flag is
          set to true, the server will throw an exception for any
          unroutable message. If this flag is false, the server
          silently drops the message.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">immediate</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">false</td><td class="confluenceTd">
      <p>
          This flag tells the server how to react
          if the message cannot be
          routed to a queue consumer
          immediately. If this flag is set to true, the server
          will
          throw an exception for any undeliverable message. If
          this
          flag is false, the server will queue the message, but
          with
          no guarantee that it will ever be consumed.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">default-return-endpoint-ref</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
          Reference to an endpoint to which AMQP
          returned message should be
          dispatched to.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">ackMode</td><td style="text-align: center" class="confluenceTd"><b>AMQP_AUTO</b> / <b>MULE_AUTO</b> / <b>MANUAL</b></td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">AMQP_AUTO</td><td class="confluenceTd">
      <p>
          The acknowledgment mode to use when
          consuming from the AMQP broker.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">prefetchSize</td><td style="text-align: center" class="confluenceTd">integer</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">0</td><td class="confluenceTd">
      <p>
          The maximum amount of content (measured
          in octets) that the server will deliver, 0 if unlimited.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">prefetchCount</td><td style="text-align: center" class="confluenceTd">integer</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">0</td><td class="confluenceTd">
      <p>
          The maximum number of messages that the
          server will deliver, 0 if unlimited.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">noLocal</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">false</td><td class="confluenceTd">
      <p>
          If the no-local field is set the server
          will not send messages to the connection that published
          them.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">exclusiveConsumers</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">false</td><td class="confluenceTd">
      <p>
          Set to true if the connector should only
          create exclusive consumers.
        </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">activeDeclarationsOnly</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd">false</td><td class="confluenceTd">
      <p>
          Defines if the connector should only do
          active exchange and queue declarations or can also perform
          passive declarations to enforce their existence.
        </p>
    </td>
  </tr>
</table>

### Endpoint Attributes

Endpoint attributes are interpreted differently if they are used on inbound or outbound endpoints. For example, routingKey on an inbound endpoint is meant to be used for queue binding while it is used as basic publish parameter on outbound endpoints. 

<!--
	Generated with: http://svn.codehaus.org/mule/branches/mule-2.0.x/tools/schemadocs/src/main/resources/xslt/single-element.xsl
	Parameter     : elementName=endpoint
-->
<table class="confluenceTable">
  <th style="width:10%" class="confluenceTh">Name</th><th style="width:10%" class="confluenceTh">Type</th><th style="width:10%" class="confluenceTh">Required</th><th style="width:10%" class="confluenceTh">Default</th><th class="confluenceTh">Description</th>
  <tr>
    <td rowspan="1" class="confluenceTd">exchangeName</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      The exchange to publish to or bind queues to.
      Leave blank or omit for the default exchange.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">queueName</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      The queue name to consume from. Leave blank
      or omit for using a new private exclusive server-named queue.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">routingKey</td><td style="text-align: center" class="confluenceTd">string</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      The routing key to use when binding a queue or publishing a message.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">exchangeType</td><td style="text-align: center" class="confluenceTd"><b>fanout</b> / <b>direct</b> / <b>topic</b> / <b>headers</b></td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      The type of exchange to be declared.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">exchangeDurable</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      The durability of the declared exchange.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">exchangeAutoDelete</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      Specifies if the declared exchange should be
      autodeleted.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">queueDurable</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      Specifies if the declared queue is durable.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">queueAutoDelete</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      Specifies if the declared queue should be
      autodeleted.
    </p>
    </td>
  </tr>
  <tr>
    <td rowspan="1" class="confluenceTd">queueExclusive</td><td style="text-align: center" class="confluenceTd">boolean</td><td style="text-align: center" class="confluenceTd">no</td><td style="text-align: center" class="confluenceTd"></td><td class="confluenceTd">
      <p>
      Specifies if the declared queue is exclusive.
    </p>
    </td>
  </tr>
</table>

Examples
--------

There are many ways to use the AMQP connector and endpoints. The following examples will demonstrate the common use cases.

### Listen to messages with exchange re-declaration and queue creation

This is a typical AMQP pattern where consumers redeclare the exchanges they intend to bind queues to.

    <amqp:connector name="amqpAutoAckLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    activeDeclarationsOnly="true" />

    <amqp:inbound-endpoint exchangeName="my-exchange"
                           exchangeType="fanout"
                           exchangeAutoDelete="false"
                           exchangeDurable="true"
                           queueName="my-queue"
                           queueDurable="false"
                           queueExclusive="false"
                           queueAutoDelete="true"
                           connector-ref="amqpAutoAckLocalhostConnector" />

### Listen to messages with exchange re-declaration and private queue creation

In this variation of the previous example, Mule will create an exclusive server-named, auto-delete, non-durable queue and bind it to the re-declared exchange.

    <amqp:connector name="amqpAutoAckLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    activeDeclarationsOnly="true" />

    <amqp:inbound-endpoint exchangeName="my-exchange"
                           exchangeType="fanout"
                           exchangeAutoDelete="false"
                           exchangeDurable="true"
                           connector-ref="amqpAutoAckLocalhostConnector" />

### Listen to messages on a pre-existing exchange

In this mode, the inbound connection will fail if the exchange doesn't pre-exist.

This behavior is enforced by activeDeclarationsOnly=false, which means that Mule will strictly ensure the pre-existence of the exchange before trying to subscribe to it. 

    <amqp:connector name="amqpAutoAckStrictLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    activeDeclarationsOnly="false" />
                    
   <amqp:inbound-endpoint exchangeName="my-exchange"
                          queueName="my-queue"
                          queueDurable="false"
                          queueExclusive="false"
                          queueAutoDelete="true"
                          queueName="my-queue"
                          connector-ref="amqpAutoAckStrictLocalhostConnector" />

### Listen to messages on a pre-existing queue

Similarly to the previous example, the inbound connection will fail if the queue doesn't pre-exist.

    <amqp:connector name="amqpAutoAckStrictLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    activeDeclarationsOnly="false" />
                    
   <amqp:inbound-endpoint queueName="my-queue"
                          connector-ref="amqpAutoAckStrictLocalhostConnector" />
                    
