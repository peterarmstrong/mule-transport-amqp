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

import junit.framework.TestCase;

public class AmqpEndpointUtilTestCase extends TestCase
{
    public void testGetQueueName()
    {
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqp://exchange/amqp-queue.queue?connector=foo"));
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqp://exchange/amqp-queue.queue"));
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqp://amqp-queue.queue?connector=foo\""));
        assertEquals("queue", AmqpEndpointUtil.getQueueName("amqp://amqp-queue.queue"));
        assertEquals("", AmqpEndpointUtil.getQueueName("amqp://exchange?connector=foo\""));
        assertEquals("", AmqpEndpointUtil.getQueueName("amqp://exchange"));
    }

    public void testGetExchangeName()
    {
        assertEquals("exchange",
            AmqpEndpointUtil.getExchangeName("amqp://exchange/amqp-queue.queue?connector=foo"));
        assertEquals("exchange", AmqpEndpointUtil.getExchangeName("amqp://exchange/amqp-queue.queue"));
        assertEquals("", AmqpEndpointUtil.getExchangeName("amqp://amqp-queue.queue?connector=foo"));
        assertEquals("", AmqpEndpointUtil.getExchangeName("amqp://amqp-queue.queue"));
        assertEquals("exchange", AmqpEndpointUtil.getExchangeName("amqp://exchange?connector=foo"));
        assertEquals("exchange", AmqpEndpointUtil.getExchangeName("amqp://exchange"));
    }
}
