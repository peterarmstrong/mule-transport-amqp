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
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.config.i18n.MessageFactory;
import org.mule.util.StringUtils;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

public abstract class AmqpEndpointUtil
{
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

    public static String getOrCreateQueue(final Channel channel,
                                          final InboundEndpoint inboundEndpoint,
                                          final boolean activeDeclarationsOnly) throws IOException
    {
        final String exchangeName = getOrCreateExchange(channel, inboundEndpoint, activeDeclarationsOnly);
        final String routingKey = getRoutingKey(inboundEndpoint);

        if ((StringUtils.isBlank(exchangeName)) && (StringUtils.isNotBlank(routingKey)))
        {
            // no exchange name -> enforce routing key to be empty
            throw new MuleRuntimeException(
                MessageFactory.createStaticMessage("An exchange name must be provided if a routing key is provided in endpoint: "
                                                   + inboundEndpoint));
        }

        final String queueName = getQueueName(inboundEndpoint.getAddress());

        if (StringUtils.isBlank(queueName))
        {
            // no queue name -> create a private one on the server
            final DeclareOk queueDeclareResult = channel.queueDeclare();

            final String privateQueueName = queueDeclareResult.getQueue();
            LOG.info("Declared private queue: " + privateQueueName);

            bindQueue(channel, inboundEndpoint, exchangeName, routingKey, privateQueueName);
            return privateQueueName;
        }

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
            LOG.info("Declared queue: " + queueName + ", durable: " + queueDurable + ", exclusive: "
                     + queueExclusive + ", autoDelete: " + queueAutoDelete);

            bindQueue(channel, inboundEndpoint, exchangeName, routingKey, queueName);
        }
        else if (!activeDeclarationsOnly)
        {
            // no declaration parameter -> ensure the queue exists
            channel.queueDeclarePassive(queueName);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Validated presence of queue: " + queueName);
            }
        }

        return queueName;
    }

    private static void bindQueue(final Channel channel,
                                  final InboundEndpoint inboundEndpoint,
                                  final String exchangeName,
                                  final String routingKey,
                                  final String queueName) throws IOException
    {
        if (StringUtils.isBlank(exchangeName))
        {
            // default exchange name -> can not bind a queue to it
            throw new MuleRuntimeException(
                MessageFactory.createStaticMessage("No queue can be programmatically bound to the default exchange: "
                                                   + inboundEndpoint));
        }

        // bind queue to exchange
        channel.queueBind(queueName, exchangeName, routingKey);

        LOG.info("Bound queue: " + queueName + " to exchange: " + exchangeName + " with routing key: "
                 + routingKey);
    }

    public static String getOrCreateExchange(final Channel channel,
                                             final ImmutableEndpoint endpoint,
                                             final boolean activeDeclarationsOnly) throws IOException
    {
        final String outboundEndpointAddress = endpoint.getAddress();
        final String exchangeName = getExchangeName(outboundEndpointAddress);

        if (StringUtils.isBlank(exchangeName))
        {
            LOG.info("Using default exchange for endpoint: " + endpoint);
            return exchangeName;
        }

        final String exchangeType = (String) endpoint.getProperty(EXCHANGE_TYPE);
        if (StringUtils.isNotBlank(exchangeType))
        {
            // an exchange type is provided -> the exchange must be declared
            final boolean exchangeDurable = BooleanUtils.toBoolean((String) endpoint.getProperty(EXCHANGE_DURABLE));
            final boolean exchangeAutoDelete = BooleanUtils.toBoolean((String) endpoint.getProperty(EXCHANGE_AUTO_DELETE));

            channel.exchangeDeclare(exchangeName, exchangeType, exchangeDurable, exchangeAutoDelete, NO_ARGS);

            LOG.info("Declared exchange: " + exchangeName + " of type: " + exchangeType + ", durable: "
                     + exchangeDurable + ", autoDelete: " + exchangeAutoDelete);
        }
        else if (!activeDeclarationsOnly)
        {
            // no exchange type -> ensure the exchange exists
            channel.exchangeDeclarePassive(exchangeName);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Validated presence of exchange: " + exchangeName);
            }
        }

        return exchangeName;
    }

    public static String getRoutingKey(final ImmutableEndpoint endpoint)
    {
        return StringUtils.defaultString((String) endpoint.getProperty(ROUTING_KEY));
    }

    public static String getQueueName(final String endpointAddress)
    {
        return StringUtils.defaultString(StringUtils.substringAfter(trimQuery(endpointAddress), QUEUE_PREFIX));
    }

    public static String getExchangeName(final String endpointAddress)
    {
        final String trimmedQuery = trimQuery(endpointAddress);
        final String exchangeName = StringUtils.defaultString(
            StringUtils.substringBetween(trimmedQuery, AmqpConnector.AMQP + "://", "/" + QUEUE_PREFIX),
            StringUtils.substringAfter(trimmedQuery, AmqpConnector.AMQP + "://"));

        if (exchangeName.startsWith(QUEUE_PREFIX))
        {
            return StringUtils.EMPTY;
        }

        return exchangeName;
    }

    private static String trimQuery(final String address)
    {
        return StringUtils.substringBefore(address, "?");
    }
}
