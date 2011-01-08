Mule AMQP Transport - User Guide
================================

Welcome to AMQP
---------------

*TBD* Introduce: connection, channel, exchanges, queues and messages



> ** AMQP for the JMS savvy **
>
> If you're a Java developer, chances are you have been exposed to JMS and are wondering how AMQP differs from JMS.
>
> In a nutshell, the main differences are the following:
>
> - AMQP defines both an API and a wire-format, ensuring compatibility between implementations (JMS only defines an API),
>
> - In JMS you publish directly to destinations (queues or topic) while in AMQP you publish to exchanges to which queues are bound (or not), which decouples the producer from the final destination of its messages.
>
> - For some types of exchanges, the delivery to the final destination depends on a routing key, a simple string that provides the necessary meta-information for successfully routing the message (unlike in JMS where the *name* of the destination is all it's needed).  

Core Transport Principles
-------------------------

The Mule AMQP Transport is an abstraction built on top of the previously introduced AMQP constructs: connection, channel, exchanges, queues and messages.

The transport hides the low level concepts, like dealing with channels, but gives a great deal of control on all the constructs it encapsulates allowing you to experience the richness of AMQP without the need to code to its API.

Here is a review of the main configuration elements and concepts you'll deal with when using the transport:

- The *connector* element take care of establishing the connection to AMQP brokers, deals with channels and manages a set of common properties that will be shared by all consumers or publishers that will use this connector.
TBD...
- The *inbound endpoint* elements are in charge of defining...
- The *outbound endpoint* elements are in charge of defining...

TBD MuleMessage abstraction byte[]



Configuration Reference
-----------------------

All the configuration parameters supported by the connector and endpoint configuration elements are described in this section.

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

### Connection fallback

It is possible to define a list of host:port or host (implying default port) to try to connect to in case the main one fails to connect.

    <amqp:connector name="amqpConnectorWithFallback"
                    host="rabbit1"
                    port="9876"
                    fallbackAddresses="rabbit1:9875,rabbit2:5672,rabbit3"
                    virtualHost="mule-test"
                    username="my-user"
                    password="my-pwd" />

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

### Manual message acknowledgement and rejection

So far, all incoming messages were automatically acknowledged by the AMQP client.

The following example shows how to manually acknowledge or reject messages within a flow, based on criteria of your choice.
 
    <amqp:connector name="amqpManualAckLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    ackMode="MANUAL" />

    <flow name="amqpChoiceAckNackService">
      <amqp:inbound-endpoint queueName="my-queue"
                             connector-ref="amqpManualAckLocalhostConnector" />
      <choice>
        <when ...condition...>
          <amqp:acknowledge-message />
        </when>
        <otherwise>
          <amqp:reject-message requeue="true" />
        </otherwise>
      </choice>
    </flow>

### Flow control

Expanding on the previous example, it is possible to throttle the delivery of messages by configuring the connector accordingly.

The following demonstrates a connector that fetches messages one by one and a flow that uses manual acknowledgment to throttle the message delivery.

    <amqp:connector name="amqpThrottledConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    prefetchCount="1"
                    ackMode="MANUAL" />

    <flow name="amqpManualAckService">
      <amqp:inbound-endpoint queueName="my-queue"
                             connector-ref="amqpThrottledConnector" />
      <!--
      components, routers... go here
      -->
      <amqp:acknowledge-message />
    </flow>

### Publish messages to a redeclared exchange

This is a typical AMQP pattern where producers redeclare the exchanges they intend to publish to.

    <amqp:connector name="amqpLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    activeDeclarationsOnly="true" />

    <amqp:outbound-endpoint routingKey="my-key"
                            exchangeName="my-exchange"
                            exchangeType="fanout"
                            exchangeAutoDelete="false"
                            exchangeDurable="false"
                            connector-ref="amqpLocalhostConnector" />

### Publish messages to a pre-existing exchange

It is also possible to publish to a pre-existing exchange:

    <amqp:outbound-endpoint exchangeName="my-exchange"
                            connector-ref="amqpLocalhostConnector" />

It can be desirable to strictly enforce the existence of this exchange before publishing to it.

This is done by configuring the connector to perform passive declarations:

    <amqp:connector name="amqpStrictLocalhostConnector"
                    virtualHost="my-vhost"
                    username="my-user"
                    password="my-pwd"
                    activeDeclarationsOnly="false" />
                    
    <amqp:outbound-endpoint routingKey="my-key"
                            exchangeName="my-exchange"
                            connector-ref="amqpStrictLocalhostConnector" />

### Message level override of exchange and routing key

It is possible to override some outbound endpoint attributes with **outbound-scoped** message properties:

- *routing-key* overrides the routingKey attribute,
- *exchange* overrides the exchangeName attribute.

### Mandatory and immediate deliveries and returned message handling

The connector supports the mandatory and immediate publication flags, as show hereafter:

  <amqp:connector name="mandatoryAmqpConnector"
                  virtualHost="mule-test"
                  username="mule"
                  password="elum"
                  mandatory="true"
                  immediate="true" />

If a message sent with this connector can't be delivered, the AMQP broker will return it asynchronously.

The AMQP transport offers the possibility to dispatch these returned messages to user defined endpoints for custom processing.

You can define the endpoint in charge of handling returned messages at connector level. Here is an example that targets a VM endpoint:

  <vm:endpoint name="globalReturnedMessageChannel" path="global.returnedMessages" />

  <amqp:connector name="mandatoryAmqpConnector"
                  virtualHost="mule-test"
                  username="mule"
                  password="elum"
                  mandatory="true"
                  default-return-endpoint-ref="globalReturnedMessageChannel" />

It is also possible to define the returned message endpoint at flow level:

    <vm:endpoint name="flowReturnedMessageChannel" path="flow.returnedMessages" />

    <flow name="amqpMandatoryDeliveryFailureFlowHandler">
      <!--
      inbound endpoint, components, routers ...
      -->

      <amqp:return-handler>
        <vm:outbound-endpoint ref="flowReturnedMessageChannel" />
      </amqp:return-handler>

      <amqp:outbound-endpoint routingKey="my-key"
                              exchangeName="my-exchange"
                              connector-ref="mandatoryAmqpConnector" />
    </flow>

If both are configured, the handler defined in the flow will supersede the one defined in the connector. 

If none is configured, Mule will log a warning with the full details of the returned message.

### Request-response publication

It is possible to perform synchronous (request-response) outbound operations:

    <amqp:outbound-endpoint routingKey="my-key"
                            exchange-pattern="request-response"
                            exchangeName="my-exchange"
                            connector-ref="amqpLocalhostConnector" />

In that case, Mule will:

- create a temporary auto-delete private reply queue,
- set-it as the reply-to property of the current message,
- publish the message to the specified exchange,
- wait for a response to be sent to the reply-queue (via the default exchange).

### Programmatic message requesting

It is possible to programmatically get messages from an AMQP queue.

For this you need first to build a URI that identifies the AMQP queue that you want to consume from. Here is the syntax to use, with optional parameters in square brackets:

    amqp://[${exchangeName}/]amqp-queue.${queueName}[?connector=${connectorName}[&...other parameters...]]

For example, the following identifies a prexisting queue named "my-queue" and will consume it with a unique AMQP connector available in the Mule configuration:

    amqp://amqp-queue.my-queue

This example will create and bind a non-durable auto-delete non-exclusive queue named "new-queue" to a pre-existing exchange named "my-exchange" with the provided routing key on the specified connector:

    amqp://my-exchange/amqp-queue.new-queue?connector=amqpAutoAckLocalhostConnector&queueDurable=false&queueExclusive=false&queueAutoDelete=true

With such a URI defined, it is possible to retrieve a message from the queue using the Mule Client, as shown in the following code sample:

    MuleMessage message = new MuleClient(muleContext).request("amqp://amqp-queue.my-queue", 2500L);
 
The above will wait for 2.5 seconds for a message and will return null if none has shown up in the queue after this amount of time.
