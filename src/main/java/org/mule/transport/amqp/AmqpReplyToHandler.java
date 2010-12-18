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
import java.util.Collections;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.DispatchException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.DefaultReplyToHandler;
import org.mule.transport.amqp.AmqpConnector.OutboundConnection;
import org.mule.transport.amqp.transformers.ObjectToAmqpMessage;

public class AmqpReplyToHandler extends DefaultReplyToHandler
{
    private final AmqpConnector amqpConnector;

    public AmqpReplyToHandler(final AmqpConnector amqpConnector)
    {
        super(Collections.<Transformer> emptyList(), amqpConnector.getMuleContext());
        this.amqpConnector = amqpConnector;
    }

    @Override
    public void processReplyTo(final MuleEvent event, final MuleMessage returnMessage, final Object replyTo)
        throws MuleException
    {
        final String replyToQueueName = (String) replyTo;

        // FIXME use a message dispatcher
        final OutboundEndpoint outboundEndpoint = getEndpoint(event, AmqpConnector.AMQP + "://"
                                                                     + AmqpEndpointUtil.QUEUE_PREFIX
                                                                     + replyToQueueName);
        final OutboundConnection outboundConnection = amqpConnector.connect(outboundEndpoint);

        final ObjectToAmqpMessage objectToAmqpMessage = new ObjectToAmqpMessage();
        objectToAmqpMessage.setMuleContext(muleContext);
        final AmqpMessage amqpMessage = (AmqpMessage) objectToAmqpMessage.transform(returnMessage);

        try
        {
            outboundConnection.channel.basicPublish(outboundConnection.exchange, replyToQueueName,
                amqpMessage.getProperties(), amqpMessage.getBody());
        }
        catch (final IOException ioe)
        {
            throw new DispatchException(CoreMessages.failedToDispatchToReplyto(outboundEndpoint), event,
                outboundEndpoint, ioe);
        }
    }

}
