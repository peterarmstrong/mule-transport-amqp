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

import org.mule.api.MuleMessage;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.amqp.AmqpMessage;
import org.mule.transport.amqp.AmqpMuleMessageFactory;
import org.mule.transport.amqp.AmqpMuleMessageFactoryTestCase;

import edu.emory.mathcs.backport.java.util.Arrays;

public class AmqpMessageToObjectTest extends AbstractMuleTestCase
{
    public void testTransformMessage() throws Exception
    {
        final AmqpMessage amqpMessage = AmqpMuleMessageFactoryTestCase.getTestMessage();

        final AmqpMessageToObject amqpMessageToObject = new AmqpMessageToObject();
        final AmqpMuleMessageFactory amqpMuleMessageFactory = new AmqpMuleMessageFactory(muleContext);
        final MuleMessage muleMessage = amqpMuleMessageFactory.create(amqpMessage, "utf-8");

        final Object result = amqpMessageToObject.transformMessage(muleMessage, "iso-8859-1");

        assertTrue(result instanceof byte[]);
        assertTrue(Arrays.equals(amqpMessage.getBody(), (byte[]) result));

        AmqpMuleMessageFactoryTestCase.checkInboundProperties(amqpMessage, muleMessage);
    }
}
