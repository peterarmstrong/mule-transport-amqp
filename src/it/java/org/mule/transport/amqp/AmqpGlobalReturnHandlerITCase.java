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

import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.util.UUID;

import com.rabbitmq.client.QueueingConsumer.Delivery;

public class AmqpGlobalReturnHandlerITCase extends AbstractAmqpITCase
{
    public AmqpGlobalReturnHandlerITCase() throws Exception
    {
        super();
        // create/delete the required pre-existing exchanges and queues
        setupExchangeAndQueue("amqpMandatoryDeliveryWithGlobalHandlerSuccess");
        setupExchange("amqpMandatoryDeliveryFailureGlobalHandler");
        setupExchange("amqpMandatoryDeliveryFailureFlowHandler");
    }

    @Override
    protected String getConfigResources()
    {
        return "global-return-handler-tests-config.xml";
    }

    public void testMandatoryDeliverySuccess() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpMandatoryDeliveryWithGlobalHandlerSuccess");
    }

    public void testMandatoryDeliveryFailureGlobalHandler() throws Exception
    {
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        final Future<MuleMessage> futureReturnedMessage = setupFunctionTestComponentForFlow("globalReturnedMessageProcessor");
        new MuleClient(muleContext).dispatch("vm://amqpMandatoryDeliveryFailureGlobalHandler.in", payload,
            null);
        final MuleMessage returnedMessage = futureReturnedMessage.get(DEFAULT_MULE_TEST_TIMEOUT_SECS,
            TimeUnit.SECONDS);
        assertNotNull(returnedMessage);
        assertEquals(payload, returnedMessage.getPayloadAsString());
    }

    public void testMandatoryDeliveryFailureFlowHandler() throws Exception
    {
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        final Future<MuleMessage> futureReturnedMessage = setupFunctionTestComponentForFlow("flowReturnedMessageProcessor");
        new MuleClient(muleContext).dispatch("vm://amqpMandatoryDeliveryFailureFlowHandler.in", payload, null);
        final MuleMessage returnedMessage = futureReturnedMessage.get(DEFAULT_MULE_TEST_TIMEOUT_SECS,
            TimeUnit.SECONDS);
        assertNotNull(returnedMessage);
        assertEquals(payload, returnedMessage.getPayloadAsString());
    }

    private void dispatchTestMessageAndAssertValidReceivedMessage(final String flowName) throws Exception
    {
        final String customHeaderValue = UUID.getUUID();
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        new MuleClient(muleContext).dispatch("vm://" + flowName + ".in", payload,
            Collections.singletonMap("customHeader", customHeaderValue));

        final Delivery dispatchedMessage = consumeMessageWithAmqp(getQueueName(flowName),
            DEFAULT_MULE_TEST_TIMEOUT_SECS * 1000L);

        assertNotNull(dispatchedMessage);
        assertEquals(payload, new String(dispatchedMessage.getBody()));
        assertEquals(customHeaderValue, dispatchedMessage.getProperties()
            .getHeaders()
            .get("customHeader")
            .toString());
    }
}
