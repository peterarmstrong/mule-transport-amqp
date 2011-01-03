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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.DefaultMuleEvent;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transformer.Transformer;
import org.mule.processor.AbstractInterceptingMessageProcessor;
import org.mule.session.DefaultMuleSession;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;

/**
 * Message processor that sets the return message processors (usually: one endpoint) for the flow, leaving it up to the
 * dispatcher to construct a DispatchingReturnListener with them and set it on the channel.<br/>
 * This class is also the holder of all the different return listeners of the transport.
 */
public class AmqpReturnHandler extends AbstractInterceptingMessageProcessor
{
    public static abstract class AbstractAmqpReturnHandlerListener implements ReturnListener
    {
        protected static Log LOGGER = LogFactory.getLog(AmqpReturnHandler.class);

        public void handleBasicReturn(final int replyCode,
                                      final String replyText,
                                      final String exchange,
                                      final String routingKey,
                                      final AMQP.BasicProperties properties,
                                      final byte[] body) throws IOException
        {
            final String errorMessage = String.format(
                "AMQP returned message with code: %d, reason: %s, exchange: %s, routing key: %s", replyCode,
                replyText, exchange, routingKey);

            final AmqpMessage returnedAmqpMessage = new AmqpMessage(null, null, properties, body);

            doHandleBasicReturn(errorMessage, returnedAmqpMessage);
        }

        protected abstract void doHandleBasicReturn(String errorMessage, AmqpMessage returnedAmqpMessage);
    }

    public static class LoggingReturnListener extends AbstractAmqpReturnHandlerListener
    {
        protected final AtomicInteger hitCount = new AtomicInteger(0);

        @Override
        protected void doHandleBasicReturn(final String errorMessage, final AmqpMessage returnedAmqpMessage)
        {
            hitCount.incrementAndGet();
            LOGGER.warn(String.format("%s: %s", errorMessage, returnedAmqpMessage));
        }

        public int getHitCount()
        {
            return hitCount.intValue();
        }
    }

    public static class DispatchingReturnListener extends AbstractAmqpReturnHandlerListener
    {
        protected final AmqpConnector amqpConnector;
        protected final List<MessageProcessor> returnMessageProcessors;
        protected final Transformer receiveTransformer;
        protected final MuleEvent event;

        public DispatchingReturnListener(final AmqpConnector amqpConnector,
                                         final List<MessageProcessor> returnMessageProcessors,
                                         final Transformer receiveTransformer,
                                         final MuleEvent event)
        {
            this.amqpConnector = amqpConnector;
            this.returnMessageProcessors = returnMessageProcessors;
            this.receiveTransformer = receiveTransformer;
            this.event = event;
        }

        @Override
        protected void doHandleBasicReturn(final String errorMessage, final AmqpMessage returnedAmqpMessage)
        {
            try
            {
                // thread safe copy of the message
                final MuleMessage returnedMuleMessage = amqpConnector.getMuleMessageFactory().create(
                    returnedAmqpMessage,
                    amqpConnector.getMuleContext().getConfiguration().getDefaultEncoding());

                for (final MessageProcessor returnMessageProcessor : returnMessageProcessors)
                {
                    final ImmutableEndpoint returnEndpoint = returnMessageProcessor instanceof ImmutableEndpoint
                                                                                                                ? (ImmutableEndpoint) returnMessageProcessor
                                                                                                                : event.getEndpoint();

                    final DefaultMuleEvent returnedMuleEvent = new DefaultMuleEvent(returnedMuleMessage,
                        returnEndpoint, new DefaultMuleSession(event.getFlowConstruct(),
                            amqpConnector.getMuleContext()));

                    returnedMuleMessage.applyTransformers(returnedMuleEvent, receiveTransformer);

                    returnMessageProcessor.process(returnedMuleEvent);
                }
            }
            catch (final Exception e)
            {
                LOGGER.error(String.format(
                    "%s, impossible to dispatch the following message to the configured endpoint(s): %s",
                    errorMessage, returnedAmqpMessage), e);
            }
        }
    }

    public static final ReturnListener DEFAULT_RETURN_LISTENER = new LoggingReturnListener();

    private List<MessageProcessor> processors;

    public void setMessageProcessors(final List<MessageProcessor> processors)
    {
        this.processors = processors;
    }

    public MuleEvent process(final MuleEvent event) throws MuleException
    {
        event.getMessage().setInvocationProperty(AmqpConstants.RETURN_MESSAGE_PROCESSORS, processors);
        return processNext(event);
    }
}
