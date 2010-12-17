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
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleRuntimeException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.config.i18n.MessageFactory;
import org.mule.util.StringUtils;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

public abstract class AmqpEndpointUtil
{
    private static final String DEFAULT_EXCHANGE = "DEFAULT";

    private static final Map<String, Object> NO_ARGS = Collections.<String, Object> emptyMap();

    public static final String QUEUE_EXCLUSIVE = "queueExclusive";

    public static final String QUEUE_AUTO_DELETE = "queueAutoDelete";

    public static final String QUEUE_DURABLE = "queueDurable";

    private final static Log LOG = LogFactory.getLog(AmqpEndpointUtil.class);

    public static final String EXCHANGE_AUTO_DELETE = "exchangeAutoDelete";
    public static final String EXCHANGE_DURABLE = "exchangeDurable";
    public static final String QUEUE_PREFIX = "amqp-queue.";
    public static final String EXCHANGE_TYPE = "exchangeType";
    public static final String ROUTING_KEY = "routingKey";

    public static String getOrCreateQueueFor(final Channel channel, final InboundEndpoint inboundEndpoint)
        throws IOException
    {
        final String inboundEndpointAddress = inboundEndpoint.getAddress();
        final String exchangeName = getExchangeName(inboundEndpointAddress);
        final String queueName = getQueueName(inboundEndpointAddress);
        final String routingKey = StringUtils.defaultString((String) inboundEndpoint.getProperty(ROUTING_KEY));

        if (StringUtils.isBlank(exchangeName))
        {
            // no exchange name -> enforce routing key to be empty
            if (StringUtils.isNotBlank(routingKey))
            {
                throw new MuleRuntimeException(
                    MessageFactory.createStaticMessage("An exchange name must be provided if a routing key is provided in endpoint: "
                                                       + inboundEndpoint));
            }
        }
        else if (DEFAULT_EXCHANGE.equals(exchangeName))
        {
            // default exchange name -> enforce routing key to be empty
            if (StringUtils.isNotBlank(routingKey))
            {
                throw new MuleRuntimeException(
                    MessageFactory.createStaticMessage("No routing key should be provided when using the default exchange in endpoint: "
                                                       + inboundEndpoint));
            }
        }
        else
        {
            final String exchangeType = (String) inboundEndpoint.getProperty(EXCHANGE_TYPE);
            if (StringUtils.isNotBlank(exchangeType))
            {
                // an exchange type is provided -> the exchange must be declared
                final boolean exchangeDurable = BooleanUtils.toBoolean((String) inboundEndpoint.getProperty(EXCHANGE_DURABLE));
                final boolean exchangeAutoDelete = BooleanUtils.toBoolean((String) inboundEndpoint.getProperty(EXCHANGE_AUTO_DELETE));

                channel.exchangeDeclare(exchangeName, exchangeType, exchangeDurable, exchangeAutoDelete,
                    NO_ARGS);

                LOG.info("Declared exchange: " + exchangeName + " of type: " + exchangeType + ", durable: "
                         + exchangeDurable + ", autoDelete: " + exchangeAutoDelete);
            }
            else
            {
                // no exchange type -> ensure the exchange exists
                channel.exchangeDeclarePassive(exchangeName);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Validated presence of exchange: " + exchangeName);
                }
            }
        }

        String queueNameInUse = queueName;
        boolean queueNeedsBinding = false;

        if (StringUtils.isBlank(queueName))
        {
            // no queue name -> create a private one on the server
            final DeclareOk queueDeclareResult = channel.queueDeclare();

            queueNameInUse = queueDeclareResult.getQueue();
            queueNeedsBinding = true;
            LOG.info("Declared private queue: " + queueNameInUse);
        }
        else
        {
            // queue name -> either create or ensure the queue exists

            if (inboundEndpoint.getProperties().containsKey(QUEUE_DURABLE)
                || inboundEndpoint.getProperties().containsKey(QUEUE_AUTO_DELETE)
                || inboundEndpoint.getProperties().containsKey(QUEUE_EXCLUSIVE))
            {
                // any of the queue declaration parameter provided -> declare the queue
                final boolean queueDurable = BooleanUtils.toBoolean((String) inboundEndpoint.getProperty(QUEUE_DURABLE));
                final boolean queueExclusive = BooleanUtils.toBoolean((String) inboundEndpoint.getProperty(QUEUE_EXCLUSIVE));
                final boolean queueAutoDelete = BooleanUtils.toBoolean((String) inboundEndpoint.getProperty(QUEUE_AUTO_DELETE));

                channel.queueDeclare(queueName, queueDurable, queueExclusive, queueAutoDelete, NO_ARGS);
                queueNeedsBinding = true;

                LOG.info("Declared queue: " + queueName + ", durable: " + queueDurable + ", exclusive: "
                         + queueExclusive + ", autoDelete: " + queueAutoDelete);
            }
            else
            {
                // no declaration parameter -> ensure the queue exists
                channel.queueDeclarePassive(queueName);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Validated presence of queue: " + queueName);
                }
            }
        }

        if (queueNeedsBinding)
        {
            // bind queue to exchange (we have previously enforced exchange name to be non blank)
            channel.queueBind(queueNameInUse, exchangeName, routingKey);

            LOG.info("Bound queue: " + queueNameInUse + " to exchange: " + exchangeName
                     + " with routing key: " + routingKey);
        }

        return queueNameInUse;
    }

    static String getQueueName(final String inboundEndpointAddress)
    {
        return StringUtils.defaultString(StringUtils.substringAfter(inboundEndpointAddress, QUEUE_PREFIX));
    }

    static String getExchangeName(final String inboundEndpointAddress)
    {
        final String exchangeName = StringUtils.defaultString(
            StringUtils.substringBetween(inboundEndpointAddress, AmqpConnector.AMQP + "://", "/"
                                                                                             + QUEUE_PREFIX),
            StringUtils.substringAfter(inboundEndpointAddress, AmqpConnector.AMQP + "://"));

        if (exchangeName.startsWith(QUEUE_PREFIX))
        {
            return StringUtils.EMPTY;
        }

        return exchangeName;
    }
}
