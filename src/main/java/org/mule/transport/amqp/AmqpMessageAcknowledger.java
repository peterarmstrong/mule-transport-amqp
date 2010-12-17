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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.model.SessionException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.MessageFactory;

import com.rabbitmq.client.Channel;

/**
 * Used to manually perform a basic ack of the message in flow, allowing fine control of message throttling. It looks
 * for a delivery-tag inbound message property and an amqp.channel session property. If the former is missing, it logs a
 * warning. If the former is present but not the latter, it throws an exception.
 */
public class AmqpMessageAcknowledger implements MessageProcessor
{
    private final static Log LOG = LogFactory.getLog(AmqpMessageAcknowledger.class);

    protected boolean multiple = false;

    public MuleEvent process(final MuleEvent event) throws MuleException
    {
        final Long deliveryTag = event.getMessage().getInboundProperty(AmqpConstants.DELIVERY_TAG);

        if (deliveryTag == null)
        {
            LOG.warn("Missing " + AmqpConstants.DELIVERY_TAG + " inbound property, impossible to ack event: "
                     + event);
            return event;
        }

        final Channel channel = event.getMessage().getProperty(AmqpConstants.CHANNEL,
            PropertyScope.INVOCATION);

        if (channel == null)
        {
            throw new SessionException(
                MessageFactory.createStaticMessage("No " + AmqpConstants.CHANNEL
                                                   + " session property found, impossible to ack event: "
                                                   + event));
        }

        try
        {
            channel.basicAck(deliveryTag, multiple);
        }
        catch (final IOException ioe)
        {
            throw new SessionException(
                MessageFactory.createStaticMessage("Failed to ack message w/deliveryTag: " + deliveryTag
                                                   + " on channel: " + channel), ioe);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Mule acknowledged message w/deliveryTag: " + deliveryTag + " on channel: " + channel);
        }

        return event;
    }

    public void setMultiple(final boolean multiple)
    {
        this.multiple = multiple;
    }

}
