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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

import org.mule.DefaultMuleEvent;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transformer.Transformer;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.DefaultReplyToHandler;

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

        // target the default (ie. "") exchange with a routing key equals to the queue replied to
        final OutboundEndpoint outboundEndpoint = getEndpoint(event, AmqpConnector.AMQP + "://?routingKey="
                                                                     + urlEncode(event, replyToQueueName));

        final MessageProcessor dispatcher = amqpConnector.createDispatcherMessageProcessor(outboundEndpoint);
        final DefaultMuleEvent replyEvent = new DefaultMuleEvent(returnMessage, outboundEndpoint,
            event.getSession());
        dispatcher.process(replyEvent);

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("Successfully replied to %s: %s", replyToQueueName, replyEvent));
        }
    }

    protected String urlEncode(final MuleEvent event, final String stringToEncode)
        throws MessagingException

    {
        try
        {
            return URLEncoder.encode(stringToEncode, event.getEncoding());
        }
        catch (final UnsupportedEncodingException uee)
        {
            throw new MessagingException(MessageFactory.createStaticMessage(String.format(
                "Impossible to url encode: %s", stringToEncode)), event, uee);
        }
    }
}
