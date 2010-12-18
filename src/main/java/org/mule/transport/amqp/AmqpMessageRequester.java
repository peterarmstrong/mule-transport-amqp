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

import org.mule.DefaultMuleMessage;
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
import com.rabbitmq.client.GetResponse;

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
    protected MuleMessage doRequest(final long ignoredTimeout) throws Exception
    {
        final GetResponse response = getChannel().basicGet(getQueueName(),
            amqpConnector.getAckMode().isAutoAck());

        if (response == null)
        {
            return new DefaultMuleMessage(null, amqpConnector.getMuleContext());
        }

        final AmqpMessage amqpMessage = new AmqpMessage(StringUtils.EMPTY, response.getEnvelope(),
            response.getProps(), response.getBody());

        final MuleMessage muleMessage = amqpConnector.getMuleMessageFactory().create(amqpMessage,
            amqpConnector.getMuleContext().getConfiguration().getDefaultEncoding());

        // add message count to the message properties in case downstream message processors care for it
        muleMessage.setProperty(AmqpConstants.MESSAGE_COUNT, response.getMessageCount(),
            PropertyScope.INBOUND);

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
