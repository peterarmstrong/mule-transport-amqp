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

import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.PropertyScope;
import org.mule.transport.AbstractMessageRequester;
import org.mule.transport.ConnectException;
import org.mule.transport.amqp.AmqpConnector.InboundConnection;
import org.mule.transport.amqp.AmqpConstants.AckMode;
import org.mule.transport.amqp.transformers.AmqpMessageToObject;
import org.mule.util.StringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * The <code>AmqpMessageRequester</code> is used to consume individual messages from an AMQP broker.
 */
public class AmqpMessageRequester extends AbstractMessageRequester
{
    protected final AmqpConnector amqpConnector;
    protected InboundConnection inboundConnection;
    protected final Transformer receiveTransformer;

    public AmqpMessageRequester(final InboundEndpoint endpoint)
    {
        super(endpoint);
        amqpConnector = (AmqpConnector) endpoint.getConnector();
        receiveTransformer = new AmqpMessageToObject();
        receiveTransformer.setMuleContext(connector.getMuleContext());
    }

    @Override
    public void doConnect() throws ConnectException
    {
        inboundConnection = amqpConnector.connect(getEndpoint());
    }

    @Override
    public void doDisconnect() throws Exception
    {
        amqpConnector.closeChannel(getChannel());
    }

    @Override
    public void doDispose()
    {
        inboundConnection = null;
    }

    @Override
    protected MuleMessage doRequest(final long timeout) throws Exception
    {

        final QueueingConsumer consumer = new QueueingConsumer(getChannel());
        getChannel().basicConsume(getQueueName(), amqpConnector.getAckMode().isAutoAck(), consumer);
        final Delivery delivery = consumer.nextDelivery(timeout);

        if (delivery == null) return null;

        final AmqpMessage amqpMessage = new AmqpMessage(StringUtils.EMPTY, delivery.getEnvelope(),
            delivery.getProperties(), delivery.getBody());

        final MuleMessage muleMessage = createMuleMessage(amqpMessage);

        if (amqpConnector.getAckMode() == AckMode.MANUAL)
        {
            // in manual AckMode, the channel will be needed to ack the message
            muleMessage.setProperty(AmqpConstants.CHANNEL, getChannel(), PropertyScope.INVOCATION);
        }
        else
        {
            // otherwise, ack if it's mule's responsibility
            amqpConnector.ackMessageIfNecessary(getChannel(), amqpMessage);
        }

        return muleMessage;
    }

    protected Channel getChannel()
    {
        return inboundConnection == null ? null : inboundConnection.channel;
    }

    protected String getQueueName()
    {
        return inboundConnection == null ? null : inboundConnection.queue;
    }
}
