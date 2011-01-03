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
import com.rabbitmq.client.ReturnListener;

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
                channel.basicPublish(exchange, routingKey, amqpConnector.isMandatory(),
                    amqpConnector.isImmediate(), amqpMessage.getProperties(), amqpMessage.getBody());
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
                return amqpConnector.consume(channel, temporaryReplyToQueue, true, timeout);
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
        outboundConnection = amqpConnector.connect(this);
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
        final MuleMessage resultMessage = createMuleMessage(doOutboundAction(event, OutboundAction.SEND));
        resultMessage.applyTransformers(event, amqpConnector.getReceiveTransformer());
        return resultMessage;
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

        // override publication properties if they are not set
        if ((amqpMessage.getProperties().getDeliveryMode() == null)
            && (amqpConnector.getDeliveryMode() != null))
        {
            amqpMessage.getProperties().setDeliveryMode(amqpConnector.getDeliveryMode().getCode());
        }
        if ((amqpMessage.getProperties().getPriority() == null) && (amqpConnector.getPriority() != null))
        {
            amqpMessage.getProperties().setPriority(amqpConnector.getPriority().intValue());
        }

        setReturnListenerIfNeeded(event, eventChannel);

        final AmqpMessage result = outboundAction.run(amqpConnector, eventChannel, eventExchange,
            eventRoutingKey, amqpMessage, event.getTimeout());

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format(
                "Successfully performed %s(channel: %s, exchange: %s, routing key: %s) for: %s and received: %s",
                outboundAction, eventChannel, eventExchange, eventRoutingKey, event, result));
        }

        return result;
    }

    /**
     * Try to associate a return listener to the channel in order to allow flow-level exception strategy to handle
     * return messages.
     */
    protected void setReturnListenerIfNeeded(final MuleEvent event, final Channel channel)
    {
        final ReturnListener returnListener = event.getMessage().getInvocationProperty(
            AmqpConstants.RETURN_LISTENER);

        if (returnListener == null)
        {
            // no return listener defined in the flow that encompasses the event
            return;
        }

        if (returnListener instanceof AmqpReturnHandler.DispatchingReturnListener)
        {
            ((AmqpReturnHandler.DispatchingReturnListener) returnListener).setAmqpConnector(amqpConnector);
        }

        channel.setReturnListener(returnListener);

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Set return listener: %s on channel: %s", returnListener, channel));
        }
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
