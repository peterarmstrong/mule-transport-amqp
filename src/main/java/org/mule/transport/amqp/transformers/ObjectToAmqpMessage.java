/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp.transformers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transport.amqp.AmqpConstants;
import org.mule.transport.amqp.AmqpMessage;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class ObjectToAmqpMessage extends AbstractAmqpMessageToObject
{
    @Override
    protected void declareInputOutputClasses()
    {
        registerSourceType(DataTypeFactory.BYTE_ARRAY);
        registerSourceType(DataTypeFactory.STRING);
        registerSourceType(DataTypeFactory.INPUT_STREAM);
        setReturnDataType(AMQP_MESSAGE_DATA_TYPE);
    }

    @Override
    public Object transformMessage(final MuleMessage message, final String outputEncoding)
        throws TransformerException
    {
        byte[] body;
        try
        {
            body = message.getPayloadAsBytes();
        }
        catch (final Exception e)
        {
            throw new TransformerException(
                MessageFactory.createStaticMessage("Impossible to extract bytes out of: " + message), e);
        }

        final String consumerTag = getProperty(message, AmqpConstants.CONSUMER_TAG);

        final long deliveryTag = getProperty(message, AmqpConstants.DELIVERY_TAG, 0L);
        final boolean redelivered = getProperty(message, AmqpConstants.REDELIVER, false);
        final String exchange = getProperty(message, AmqpConstants.EXCHANGE);
        final String routingKey = getProperty(message, AmqpConstants.ROUTING_KEY);
        final Envelope envelope = new Envelope(deliveryTag, redelivered, exchange, routingKey);

        final BasicProperties amqpProperties = new BasicProperties();
        amqpProperties.setAppId(this.<String> getProperty(message, AmqpConstants.APP_ID));
        amqpProperties.setContentEncoding(this.<String> getProperty(message, AmqpConstants.CONTENT_ENCODING,
            outputEncoding));
        amqpProperties.setContentType(this.<String> getProperty(message, AmqpConstants.CONTENT_TYPE));
        amqpProperties.setCorrelationId(this.<String> getProperty(message, AmqpConstants.CORRELATION_ID,
            message.getCorrelationId()));
        amqpProperties.setDeliveryMode(this.<Integer> getProperty(message, AmqpConstants.DELIVERY_MODE));
        amqpProperties.setExpiration(this.<String> getProperty(message, AmqpConstants.EXPIRATION));
        amqpProperties.setMessageId(this.<String> getProperty(message, AmqpConstants.MESSAGE_ID,
            message.getUniqueId()));
        amqpProperties.setPriority(this.<Integer> getProperty(message, AmqpConstants.PRIORITY));
        amqpProperties.setReplyTo(this.<String> getProperty(message, AmqpConstants.REPLY_TO,
            (String) message.getReplyTo()));
        amqpProperties.setTimestamp(this.<Date> getProperty(message, AmqpConstants.TIMESTAMP, new Date()));
        amqpProperties.setType(this.<String> getProperty(message, AmqpConstants.TYPE));
        amqpProperties.setUserId(this.<String> getProperty(message, AmqpConstants.USER_ID));

        amqpProperties.setHeaders(getHeaders(message));

        return new AmqpMessage(consumerTag, envelope, amqpProperties, body);
    }

    private Map<String, Object> getHeaders(final MuleMessage message)
    {
        final Map<String, Object> headers = new HashMap<String, Object>();
        for (final String propertyName : message.getPropertyNames(PropertyScope.OUTBOUND))
        {
            if (!AmqpConstants.AMQP_PROPERTY_NAMES.contains(propertyName))
            {
                headers.put(propertyName, message.getProperty(propertyName, PropertyScope.OUTBOUND));
            }
        }
        return headers;
    }

    private <T> T getProperty(final MuleMessage message, final String key)
    {
        return this.<T> getProperty(message, key, null);
    }

    private <T> T getProperty(final MuleMessage message, final String key, final T defaultValue)
    {
        return message.getProperty(key, PropertyScope.OUTBOUND, defaultValue);
    }
}
