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
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.util.UUID;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public abstract class AbstractAmqpITCase extends FunctionalTestCase
{
    protected final ConnectionFactory factory;
    protected final Connection connection;
    private final AtomicReference<Channel> channelRef = new AtomicReference<Channel>();

    public AbstractAmqpITCase() throws IOException
    {
        super();
        setDisposeManagerPerSuite(true);

        factory = new ConnectionFactory();
        factory.setUsername("mule");
        factory.setPassword("elum");
        factory.setVirtualHost("mule-test");
        connection = factory.newConnection();
    }

    private Channel newChannel() throws IOException
    {
        final Channel channel = connection.createChannel();
        channel.addShutdownListener(new ShutdownListener()
        {
            public void shutdownCompleted(final ShutdownSignalException sse)
            {
                if (!sse.isInitiatedByApplication())
                {
                    channelRef.set(null);
                }
            }
        });
        return channel;
    }

    protected Channel getChannel() throws IOException
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

        return getChannel();
    }

    @Override
    protected boolean isGracefulShutdown()
    {
        return true;
    }

    protected Future<MuleMessage> setupFunctionTestComponentForFlow(final String flowName) throws Exception
    {

        final FunctionalTestComponent functionalTestComponent = getFunctionalTestComponent(flowName);

        final CountDownLatch messageReceivedLatch = new CountDownLatch(1);
        final AtomicReference<MuleMessage> receivedMessageRef = new AtomicReference<MuleMessage>(null);

        functionalTestComponent.setEventCallback(new EventCallback()
        {
            public void eventReceived(final MuleEventContext context, final Object component)
                throws Exception
            {
                receivedMessageRef.set(context.getMessage());
                messageReceivedLatch.countDown();
            }
        });

        final Future<MuleMessage> futureReceivedMessage = new Future<MuleMessage>()
        {
            public boolean cancel(final boolean mayInterruptIfRunning)
            {
                throw new UnsupportedOperationException();
            }

            public boolean isCancelled()
            {
                throw new UnsupportedOperationException();
            }

            public boolean isDone()
            {
                throw new UnsupportedOperationException();
            }

            public MuleMessage get() throws InterruptedException, ExecutionException
            {
                throw new UnsupportedOperationException();
            }

            public MuleMessage get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException
            {
                messageReceivedLatch.await(timeout, unit);
                return receivedMessageRef.get();
            }
        };

        return futureReceivedMessage;
    }

    protected void setupExchangeAndQueue(final String flowName) throws IOException
    {
        final String exchange = setupExchange(flowName);
        final String queue = setupQueue(flowName);
        getChannel().queueBind(queue, exchange, "");
        getChannel().queuePurge(queue);
    }

    protected String setupQueue(final String flowName) throws IOException
    {
        final String queue = getQueueName(flowName);

        getChannel().queueDeclare(queue, false, false, true, Collections.<String, Object> emptyMap());
        return queue;
    }

    protected String setupExchange(final String flowName) throws IOException
    {
        final String exchange = getExchangeName(flowName);
        getChannel().exchangeDeclare(exchange, "fanout");
        return exchange;
    }

    protected void deleteExchange(final String flowName) throws InterruptedException
    {
        final String exchange = getExchangeName(flowName);
        try
        {
            getChannel().exchangeDelete(exchange);
        }
        catch (final IOException ioe)
        {
            // ignored
            Thread.sleep(1000L);
        }
    }

    protected String getQueueName(final String flowName)
    {
        return flowName + "-queue";
    }

    protected static String getExchangeName(final String flowName)
    {
        return flowName + "-exchange";
    }

    protected String publishMessageWithAmqp(final byte[] body, final String flowName) throws IOException
    {
        return publishMessageWithAmqp(body, flowName, null);
    }

    protected String publishMessageWithAmqp(final byte[] body, final String flowName, final String replyTo)
        throws IOException
    {
        final String correlationId = UUID.getUUID();
        final BasicProperties props = new BasicProperties();
        props.setContentType("text/plain");
        props.setCorrelationId(correlationId);
        props.setReplyTo(replyTo);
        props.setHeaders(Collections.<String, Object> singletonMap("customHeader", 123L));
        getChannel().basicPublish(getExchangeName(flowName), "", props, body);
        return correlationId;
    }

    protected Delivery consumeMessageWithAmqp(final String queue, final long timeout)
        throws IOException, InterruptedException
    {
        final QueueingConsumer consumer = new QueueingConsumer(getChannel());
        final String consumerTag = getChannel().basicConsume(queue, true, consumer);
        final Delivery delivery = consumer.nextDelivery(timeout);
        getChannel().basicCancel(consumerTag);
        return delivery;
    }

    protected void assertValidReceivedMessage(final String correlationId,
                                              final byte[] body,
                                              final MuleMessage receivedMessage) throws Exception
    {
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.getPayload() instanceof byte[]);
        assertTrue(Arrays.equals(body, receivedMessage.getPayloadAsBytes()));
        assertEquals(correlationId, receivedMessage.getCorrelationId());
        assertEquals(correlationId, receivedMessage.getInboundProperty(AmqpConstants.CORRELATION_ID));
        assertEquals(123L, receivedMessage.getInboundProperty("customHeader"));
    }
}
