/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transport.Connectable;
import org.mule.api.transport.MessageDispatcher;
import org.mule.api.transport.MessageReceiver;
import org.mule.api.transport.MessageRequester;
import org.mule.api.transport.ReplyToHandler;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.AbstractConnector;
import org.mule.transport.ConnectException;
import org.mule.transport.amqp.AmqpConstants.AckMode;
import org.mule.transport.amqp.AmqpConstants.DeliveryMode;
import org.mule.util.NumberUtils;
import org.mule.util.StringUtils;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Connects to a particular virtual host on a particular AMQP broker.
 */
public class AmqpConnector extends AbstractConnector
{
    public static final String AMQP = "amqp";

    private String host;
    private int port;
    private String[] fallbackAddresses;
    private String virtualHost;
    private String username;
    private String password;
    private DeliveryMode deliveryMode;
    private byte priority;
    private AckMode ackMode;
    private boolean activeDeclarationsOnly;
    private boolean mandatory;
    private boolean immediate;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private ConnectorConnection connectorConnection;

    private static abstract class AmqpConnection
    {
        private final AmqpConnector amqpConnector;
        private final AtomicReference<Channel> channelRef = new AtomicReference<Channel>();

        private AmqpConnection(final AmqpConnector amqpConnector)
        {
            this.amqpConnector = amqpConnector;
        }

        private Channel newChannel()
        {
            try
            {
                final Channel channel = amqpConnector.getConnection().createChannel();
                channel.addShutdownListener(new ShutdownListener()
                {
                    public void shutdownCompleted(final ShutdownSignalException sse)
                    {
                        if (!sse.isInitiatedByApplication())
                        {
                            // do not inform the connector of the issue as it can't decide what to do
                            // reset the channel so it would later be lazily reconnected
                            channelRef.set(null);
                        }
                    }
                });

                return channel;
            }
            catch (final IOException ioe)
            {
                amqpConnector.handleException(new ConnectException(
                    MessageFactory.createStaticMessage("Impossible to create new channels on connection: "
                                                       + amqpConnector.getConnection()), ioe, amqpConnector));
                return null;
            }
        }

        public Channel getChannel()
        {
            Channel channel = channelRef.get();

            if (channel != null)
            {
                return channel;
            }

            channel = newChannel();

            if (channelRef.compareAndSet(null, channel))
            {
                return channel;
            }

            // race condition: use the channel created by another thread
            return getChannel();
        }
    }

    private static class ConnectorConnection extends AmqpConnection
    {
        private ConnectorConnection(final AmqpConnector amqpConnector)
        {
            super(amqpConnector);
        }
    }

    public static class InboundConnection extends AmqpConnection
    {
        private final String queue;

        private InboundConnection(final AmqpConnector amqpConnector, final String queue)
        {
            super(amqpConnector);
            this.queue = queue;
        }

        public String getQueue()
        {
            return queue;
        }
    }

    public static class OutboundConnection extends AmqpConnection
    {
        private final String exchange;
        private final String routingKey;

        private OutboundConnection(final AmqpConnector amqpConnector,
                                   final String exchange,
                                   final String routingKey)
        {
            super(amqpConnector);
            this.exchange = exchange;
            this.routingKey = routingKey;
        }

        public String getExchange()
        {
            return exchange;
        }

        public String getRoutingKey()
        {
            return routingKey;
        }
    }

    public AmqpConnector(final MuleContext context)
    {
        super(context);
    }

    @Override
    public void doInitialise() throws InitialisationException
    {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
    }

    @Override
    public void doDispose()
    {
        connectorConnection = null;
        connection = null;
        connectionFactory = null;
    }

    @Override
    public void doConnect() throws Exception
    {
        final List<Address> brokerAddresses = new ArrayList<Address>();
        brokerAddresses.add(new Address(host, port));

        addFallbackAddresses(brokerAddresses);

        connection = connectionFactory.newConnection(brokerAddresses.toArray(new Address[0]));

        connectorConnection = new ConnectorConnection(this);
    }

    private void addFallbackAddresses(final List<Address> brokerAddresses)
    {
        if (fallbackAddresses == null) return;

        for (final String fallbackAddress : fallbackAddresses)
        {
            final String[] fallbackAddressElements = StringUtils.splitAndTrim(fallbackAddress, ":");

            if (fallbackAddressElements.length == 2)
            {
                brokerAddresses.add(new Address(fallbackAddressElements[0],
                    NumberUtils.toInt(fallbackAddressElements[1])));
            }
            else if (fallbackAddressElements.length == 1)
            {
                brokerAddresses.add(new Address(fallbackAddressElements[0]));
            }
            else
            {
                logger.warn("Ignoring unparseable fallback address: " + fallbackAddress);
            }
        }
    }

    @Override
    public void doDisconnect() throws Exception
    {
        connectorConnection.getChannel().close();
        connection.close();
    }

    @Override
    public void doStart() throws MuleException
    {
        // NOOP
    }

    @Override
    public void doStop() throws MuleException
    {
        // NOOP
    }

    public static Channel getChannelFromMessage(final MuleMessage message)
    {
        return getChannelFromMessage(message, null);
    }

    public static Channel getChannelFromMessage(final MuleMessage message, final Channel defaultValue)
    {
        return message.getInvocationProperty(AmqpConstants.CHANNEL, defaultValue);
    }

    public InboundConnection connect(final MessageReceiver messageReceiver) throws ConnectException
    {
        return connect(messageReceiver, messageReceiver.getEndpoint());
    }

    public InboundConnection connect(final MessageRequester messageRequester) throws ConnectException
    {
        return connect(messageRequester, messageRequester.getEndpoint());
    }

    private InboundConnection connect(final Connectable connectable, final InboundEndpoint inboundEndpoint)
        throws ConnectException
    {
        try
        {
            final String queueName = AmqpEndpointUtil.getOrCreateQueue(connectorConnection.getChannel(),
                inboundEndpoint, activeDeclarationsOnly);
            return new InboundConnection(this, queueName);
        }
        catch (final IOException ioe)
        {
            throw new ConnectException(
                MessageFactory.createStaticMessage("Error when connecting inbound endpoint: "
                                                   + inboundEndpoint), ioe, connectable);
        }
    }

    public OutboundConnection connect(final MessageDispatcher messageDispatcher) throws ConnectException
    {

        final OutboundEndpoint outboundEndpoint = messageDispatcher.getEndpoint();

        try
        {
            String routingKey = AmqpEndpointUtil.getRoutingKey(outboundEndpoint);
            final String exchange = AmqpEndpointUtil.getOrCreateExchange(connectorConnection.getChannel(),
                outboundEndpoint, activeDeclarationsOnly);

            // handle dispatching to default exchange
            if ((StringUtils.isBlank(exchange)) && (StringUtils.isBlank(routingKey)))
            {
                final String queueName = AmqpEndpointUtil.getQueueName(outboundEndpoint.getAddress());
                if (StringUtils.isNotBlank(queueName))
                {
                    routingKey = queueName;
                }
            }

            return new OutboundConnection(this, exchange, routingKey);
        }
        catch (final IOException ioe)
        {
            throw new ConnectException(
                MessageFactory.createStaticMessage("Error when connecting outbound endpoint: "
                                                   + outboundEndpoint), ioe, messageDispatcher);
        }
    }

    public AmqpMessage consume(final Channel channel,
                               final String queue,
                               final boolean autoAck,
                               final long timeout) throws IOException, InterruptedException
    {
        final QueueingConsumer consumer = new QueueingConsumer(channel);
        final String consumerTag = channel.basicConsume(queue, autoAck, consumer);
        final Delivery delivery = consumer.nextDelivery(timeout);
        channel.basicCancel(consumerTag);

        if (delivery == null) return null;

        return new AmqpMessage(consumerTag, delivery.getEnvelope(), delivery.getProperties(),
            delivery.getBody());
    }

    public void ackMessageIfNecessary(final Channel channel, final AmqpMessage amqpMessage)
        throws IOException
    {
        if (getAckMode() == AckMode.MULE_AUTO)
        {
            channel.basicAck(amqpMessage.getEnvelope().getDeliveryTag(), false);
            if (logger.isDebugEnabled())
            {
                logger.debug("Mule acknowledged message: " + amqpMessage + " on channel: " + channel);
            }
        }
    }

    public void closeChannel(final Channel channel) throws ConnectException
    {
        // FIXME remove when http://www.mulesoft.org/jira/browse/MULE-5290 is fixed
        if (!channel.isOpen())
        {
            logger.warn("Attempting to close an already closed channel (probably due to http://www.mulesoft.org/jira/browse/MULE-5290)");
            return;
        }

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Closing channel: " + channel);
            }

            channel.close();

            if (logger.isDebugEnabled())
            {
                logger.debug("Closed channel: " + channel);
            }
        }
        catch (final IOException ioe)
        {
            throw new ConnectException(MessageFactory.createStaticMessage("Error when closing channel: "
                                                                          + channel), ioe, this);
        }
    }

    @Override
    public ReplyToHandler getReplyToHandler(final ImmutableEndpoint endpoint)
    {
        return new AmqpReplyToHandler(this);
    }

    public Connection getConnection()
    {
        return connection;
    }

    public String getProtocol()
    {
        return AMQP;
    }

    public Byte getPriority()
    {
        return priority;
    }

    public void setPriority(final Byte priority)
    {
        this.priority = priority;
    }

    public AckMode getAckMode()
    {
        return ackMode;
    }

    public void setAckMode(final AckMode ackMode)
    {
        this.ackMode = ackMode;
    }

    public void setActiveDeclarationsOnly(final boolean activeDeclarationsOnly)
    {
        this.activeDeclarationsOnly = activeDeclarationsOnly;
    }

    public DeliveryMode getDeliveryMode()
    {
        return deliveryMode;
    }

    public void setDeliveryMode(final DeliveryMode deliveryMode)
    {
        this.deliveryMode = deliveryMode;
    }

    public void setHost(final String host)
    {
        this.host = host;
    }

    public void setPort(final int port)
    {
        this.port = port;
    }

    public void setFallbackAddresses(final String[] fallbackAddresses)
    {
        this.fallbackAddresses = fallbackAddresses;
    }

    public void setVirtualHost(final String virtualHost)
    {
        this.virtualHost = virtualHost;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public boolean isImmediate()
    {
        return immediate;
    }

    public void setImmediate(final boolean immediate)
    {
        this.immediate = immediate;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }
}
