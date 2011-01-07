Mule AMQP Transport - User Guide
================================

Configuration Reference
-----------------------

### Connector Attributes
<!--
	Generated with: http://svn.codehaus.org/mule/branches/mule-2.0.x/tools/schemadocs/src/main/resources/xslt/single-element.xsl
	Parameter     : elementName=connector
-->
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


Examples
--------
