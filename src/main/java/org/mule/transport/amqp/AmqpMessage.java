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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class AmqpMessage
{
    private final String consumerTag;
    private final Envelope envelope;
    private final AMQP.BasicProperties properties;
    private final byte[] body;

    public AmqpMessage(final String consumerTag,
                       final Envelope envelope,
                       final BasicProperties properties,
                       final byte[] body)
    {
        this.consumerTag = consumerTag;
        this.envelope = envelope;
        this.properties = properties;
        this.body = body;
    }

    public String getConsumerTag()
    {
        return consumerTag;
    }

    public Envelope getEnvelope()
    {
        return envelope;
    }

    public AMQP.BasicProperties getProperties()
    {
        return properties;
    }

    public byte[] getBody()
    {
        return body;
    }
}
