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
import org.mule.transport.amqp.AmqpConstants.AckMode;
import org.mule.util.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * <code>AmqpMessageReceiver</code> TODO document
 */
public class AmqpMessageReceiver extends AbstractMessageReceiver
{
    private final AmqpConnector amqpConnector;
    private String queueName;

    private Channel channel;
    private String consumerTag;

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
        try
        {
            channel = amqpConnector.newChannel();
            queueName = AmqpEndpointUtil.getOrCreateQueueFor(channel, getEndpoint());

            if (logger.isDebugEnabled())
            {
                logger.debug("Using queue: " + queueName + " on channel: " + channel);
            }
        }
        catch (final IOException ioe)
        {
            throw new ConnectException(MessageFactory.createStaticMessage("Error when opening new channel"),
                ioe, this);
        }
    }

    @Override
    public void doDisconnect() throws ConnectException
    {
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
    public void doDispose()
    {
        channel = null;
    }

    @Override
    public void doStart()
    {
        try
        {
            consumerTag = channel.basicConsume(queueName, amqpConnector.getAckMode().isAutoAck(),
                new DefaultConsumer(channel)
                {
                    @Override
                    public void handleDelivery(final String consumerTag,
                                               final Envelope envelope,
                                               final AMQP.BasicProperties properties,
                                               final byte[] body) throws IOException
                    {
                        final AmqpMessage amqpMessage = new AmqpMessage(consumerTag, envelope, properties,
                            body);
                        deliverAmqpMessage(amqpMessage);
                    }
                });

            logger.info("Started subscription: " + consumerTag + " on channel: " + channel);
        }
        catch (final IOException ioe)
        {
            throw new MuleRuntimeException(
                MessageFactory.createStaticMessage("Error when subscribing to queue: " + queueName
                                                   + " on channel: " + channel), ioe);
        }
    }

    @Override
    public void doStop()
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Cancelling subscription of: " + consumerTag + " on channel: " + channel);
            }

            channel.basicCancel(consumerTag);
            logger.info("Cancelled subscription of: " + consumerTag + " on channel: " + channel);
        }
        catch (final IOException ioe)
        {
            throw new MuleRuntimeException(
                MessageFactory.createStaticMessage("Error when cancelling subscription: " + consumerTag
                                                   + " on channel: " + channel), ioe);
        }
    }

    private void deliverAmqpMessage(final AmqpMessage amqpMessage)
    {
        // deliver message in a different thread to free the connector's thread
        try
        {
            getWorkManager().scheduleWork(new AmqpMessageRouterWork(amqpMessage));
        }
        catch (final WorkException we)
        {
            throw new MuleRuntimeException(MessageFactory.createStaticMessage("Work manager can't deliver: "
                                                                              + amqpMessage), we);
        }
    }

    private final class AmqpMessageRouterWork implements Work
    {
        private final AmqpMessage amqpMessage;

        private AmqpMessageRouterWork(final AmqpMessage amqpMessage)
        {
            this.amqpMessage = amqpMessage;
        }

        public void run()
        {
            try
            {
                final MuleMessage muleMessage = amqpConnector.getMuleMessageFactory().create(amqpMessage,
                    amqpConnector.getMuleContext().getConfiguration().getDefaultEncoding());

                if (amqpConnector.getAckMode() == AckMode.MANUAL)
                {
                    // in manual AckMode, the channel will be needed to ack the message
                    muleMessage.setProperty(AmqpConstants.CHANNEL, channel, PropertyScope.INVOCATION);
                }

                try
                {
                    routeMessage(muleMessage);
                }
                finally
                {
                    if (amqpConnector.getAckMode() == AckMode.MULE_AUTO)
                    {
                        channel.basicAck(amqpMessage.getEnvelope().getDeliveryTag(), false);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Mule acknowledged message: " + amqpMessage + " on channel: "
                                         + channel);
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                throw new MuleRuntimeException(MessageFactory.createStaticMessage("Impossible to route: "
                                                                                  + amqpMessage), e);
            }

        }

        public void release()
        {
            // NOOP
        }
    }
}
