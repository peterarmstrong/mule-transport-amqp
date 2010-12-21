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

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.DispatchException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.AbstractMessageDispatcher;
import org.mule.transport.amqp.AmqpConnector.OutboundConnection;
import org.mule.util.StringUtils;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

/**
 * The <code>AmqpMessageDispatcher</code> takes care of sending messages from Mule to an AMQP broker. It supports
 * synchronous sending by the means of private temporary reply queues.
 */
public class AmqpMessageDispatcher extends AbstractMessageDispatcher
{
    protected final AmqpConnector amqpConnector;
    protected OutboundConnection outboundConnection;

    protected enum OutboundAction
    {
        DISPATCH
        {
            @Override
            public AmqpMessage run(final AmqpConnector amqpConnector,
                                   final Channel channel,
                                   final String exchange,
                                   final String routingKey,
                                   final AmqpMessage amqpMessage,
                                   final long timeout) throws IOException
            {
                channel.basicPublish(exchange, routingKey, amqpMessage.getProperties(), amqpMessage.getBody());
                return null;
            }
        },
        SEND
        {
            @Override
            public AmqpMessage run(final AmqpConnector amqpConnector,
                                   final Channel channel,
                                   final String exchange,
                                   final String routingKey,
                                   final AmqpMessage amqpMessage,
                                   final long timeout) throws IOException, InterruptedException
            {
                final DeclareOk declareOk = channel.queueDeclare();
                final String temporaryReplyToQueue = declareOk.getQueue();
                amqpMessage.getProperties().setReplyTo(temporaryReplyToQueue);

                DISPATCH.run(amqpConnector, channel, exchange, routingKey, amqpMessage, timeout);
                return amqpConnector.consume(amqpConnector.getConnection().createChannel(),
                    temporaryReplyToQueue, true, timeout);
            }
        };

        public abstract AmqpMessage run(final AmqpConnector amqpConnector,
                                        Channel channel,
                                        String exchange,
                                        String routingKey,
                                        AmqpMessage amqpMessage,
                                        final long timeout) throws IOException, InterruptedException;
    };

    public AmqpMessageDispatcher(final OutboundEndpoint endpoint)
    {
        super(endpoint);
        amqpConnector = (AmqpConnector) endpoint.getConnector();
    }

    @Override
    public void doConnect() throws Exception
    {
        outboundConnection = amqpConnector.connect(getEndpoint());
    }

    @Override
    public void doDisconnect() throws Exception
    {
        amqpConnector.closeChannel(getChannel());
    }

    @Override
    public void doDispose()
    {
        outboundConnection = null;
    }

    @Override
    public void doDispatch(final MuleEvent event) throws Exception
    {
        doOutboundAction(event, OutboundAction.DISPATCH);
    }

    @Override
    public MuleMessage doSend(final MuleEvent event) throws Exception
    {
        return createMuleMessage(doOutboundAction(event, OutboundAction.SEND));
    }

    protected AmqpMessage doOutboundAction(final MuleEvent event, final OutboundAction outboundAction)
        throws Exception
    {
        final MuleMessage message = event.getMessage();

        if (!(message.getPayload() instanceof AmqpMessage))
        {
            throw new DispatchException(
                MessageFactory.createStaticMessage("Message payload is not an instance of: "
                                                   + AmqpMessage.class.getName()), event, getEndpoint());
        }

        final Channel eventChannel = AmqpConnector.getChannelFromMessage(message, getChannel());
        final String eventExchange = message.getOutboundProperty(AmqpConstants.EXCHANGE, getExchange());
        final String eventRoutingKey = message.getOutboundProperty(AmqpConstants.ROUTING_KEY, getRoutingKey());
        final AmqpMessage amqpMessage = (AmqpMessage) message.getPayload();

        return outboundAction.run(amqpConnector, eventChannel, eventExchange, eventRoutingKey, amqpMessage,
            event.getTimeout());
    }

    protected Channel getChannel()
    {
        return outboundConnection == null ? null : outboundConnection.getChannel();
    }

    protected String getExchange()
    {
        return outboundConnection == null ? StringUtils.EMPTY : outboundConnection.getExchange();
    }

    protected String getRoutingKey()
    {
        return outboundConnection == null ? StringUtils.EMPTY : outboundConnection.getRoutingKey();
    }
}
