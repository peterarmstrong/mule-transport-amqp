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

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.transport.Connector;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.ConnectException;
import org.mule.transport.amqp.AmqpConnector.InboundConnection;
import org.mule.transport.amqp.AmqpConstants.AckMode;
import org.mule.util.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * The <code>AmqpMessageReceiver</code> subscribes to a queue and dispatches received messages to Mule.
 */
public class AmqpMessageReceiver extends AbstractMessageReceiver
{
    protected final AmqpConnector amqpConnector;
    protected InboundConnection inboundConnection;
    protected String consumerTag;

    public AmqpMessageReceiver(final Connector connector,
                               final FlowConstruct flowConstruct,
                               final InboundEndpoint endpoint) throws CreateException
    {
        super(connector, flowConstruct, endpoint);
        this.amqpConnector = (AmqpConnector) connector;
    }

    // FIXME remove when http://www.mulesoft.org/jira/browse/MULE-5288 is fixed
    @Override
    public String getReceiverKey()
    {
        return StringUtils.defaultIfEmpty(endpoint.getEndpointURI().getFilterAddress(),
            endpoint.getEndpointURI().getAddress());
    }

    @Override
    public void doConnect() throws ConnectException
    {
        inboundConnection = amqpConnector.connect(this);

        if (logger.isDebugEnabled())
        {
            logger.debug("Connected queue: " + getQueueName() + " on channel: " + getChannel());
        }
    }

    @Override
    public void doDisconnect() throws ConnectException
    {
        amqpConnector.closeChannel(getChannel());
    }

    @Override
    public void doDispose()
    {
        inboundConnection = null;
    }

    @Override
    public void doStart()
    {
        try
        {
            consumerTag = getChannel().basicConsume(getQueueName(), amqpConnector.getAckMode().isAutoAck(),
                getClientConsumerTag(), amqpConnector.isNoLocal(), amqpConnector.isExclusiveConsumers(),
                null, new DefaultConsumer(getChannel())
                {
                    @Override
                    public void handleDelivery(final String consumerTag,
                                               final Envelope envelope,
                                               final AMQP.BasicProperties properties,
                                               final byte[] body) throws IOException
                    {
                        final AmqpMessage amqpMessage = new AmqpMessage(consumerTag, envelope, properties,
                            body);

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Received: " + amqpMessage);
                        }

                        deliverAmqpMessage(amqpMessage);
                    }

                    @Override
                    public void handleShutdownSignal(final String consumerTag,
                                                     final ShutdownSignalException sse)
                    {
                        if (!sse.isInitiatedByApplication())
                        {
                            // inform the connector the subscription is dead
                            // so it will reconnect the receiver
                            amqpConnector.handleException(new ConnectException(
                                MessageFactory.createStaticMessage("Unexpected susbscription shutdown for: "
                                                                   + consumerTag), sse,
                                AmqpMessageReceiver.this));
                        }
                    }
                });

            logger.info("Started subscription: " + consumerTag + " on channel: " + getChannel());
        }
        catch (final IOException ioe)
        {
            throw new MuleRuntimeException(
                MessageFactory.createStaticMessage("Error when subscribing to queue: " + getQueueName()
                                                   + " on channel: " + getChannel()), ioe);
        }
    }

    @Override
    public void doStop()
    {
        // FIXME remove when http://www.mulesoft.org/jira/browse/MULE-5290 is
        // fixed
        if (!getChannel().isOpen())
        {
            logger.warn("Attempting to stop a subscription on a closed channel (probably due to http://www.mulesoft.org/jira/browse/MULE-5290)");
            return;
        }

        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Cancelling subscription of: " + consumerTag + " on channel: " + getChannel());
            }

            getChannel().basicCancel(consumerTag);
            logger.info("Cancelled subscription of: " + consumerTag + " on channel: " + getChannel());
        }
        catch (final IOException ioe)
        {
            throw new MuleRuntimeException(
                MessageFactory.createStaticMessage("Error when cancelling subscription: " + consumerTag
                                                   + " on channel: " + getChannel()), ioe);
        }
    }

    protected Channel getChannel()
    {
        return inboundConnection == null ? null : inboundConnection.getChannel();
    }

    protected String getQueueName()
    {
        return inboundConnection == null ? null : inboundConnection.getQueue();
    }

    protected String getClientConsumerTag()
    {
        return AmqpEndpointUtil.getConsumerTag(getEndpoint());
    }

    private void deliverAmqpMessage(final AmqpMessage amqpMessage)
    {
        // deliver message in a different thread to free the Amqp Connector's
        // thread
        try
        {
            getWorkManager().scheduleWork(new AmqpMessageRouterWork(getChannel(), amqpMessage));
        }
        catch (final WorkException we)
        {
            throw new MuleRuntimeException(MessageFactory.createStaticMessage("Work manager can't deliver: "
                                                                              + amqpMessage), we);
        }
    }

    private final class AmqpMessageRouterWork implements Work
    {
        private final Log logger = LogFactory.getLog(AmqpMessageRouterWork.class);
        private final Channel channel;
        private final AmqpMessage amqpMessage;

        private AmqpMessageRouterWork(final Channel channel, final AmqpMessage amqpMessage)
        {
            this.channel = channel;
            this.amqpMessage = amqpMessage;
        }

        public void run()
        {
            try
            {
                final MuleMessage muleMessage = createMuleMessage(amqpMessage);

                if ((getEndpoint().getExchangePattern() == MessageExchangePattern.REQUEST_RESPONSE)
                    && (muleMessage.getReplyTo() == null))
                {
                    logger.warn(String.format(
                        "Impossible to honor the request-response exchange pattern of %s for AMQP message without reply to: %s",
                        getEndpoint(), muleMessage));
                }

                if (amqpConnector.getAckMode() == AckMode.MANUAL)
                {
                    // in manual AckMode, the channel will be needed to ack the
                    // message
                    muleMessage.setProperty(AmqpConstants.CHANNEL, channel, PropertyScope.INVOCATION);
                }

                try
                {
                    routeMessage(muleMessage);
                }
                finally
                {
                    amqpConnector.ackMessageIfNecessary(channel, amqpMessage);
                }
            }
            catch (final Exception e)
            {
                logger.error("Impossible to route: " + amqpMessage, e);
            }

        }

        public void release()
        {
            // NOOP
        }
    }
}
