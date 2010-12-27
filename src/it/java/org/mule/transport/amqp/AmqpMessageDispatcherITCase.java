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

import org.apache.commons.lang.RandomStringUtils;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.util.UUID;

import com.rabbitmq.client.QueueingConsumer.Delivery;

public class AmqpMessageDispatcherITCase extends AbstractAmqpITCase
{
    public AmqpMessageDispatcherITCase() throws Exception
    {
        super();
        // create/delete the required pre-existing exchanges and queues
        setupExchangeAndQueue("amqpExistingExchangeService");
        setupExchangeAndQueue("amqpRedeclaredExistingExchangeService");
        deleteExchange("amqpNewExchangeService");
        setupQueue("amqpDefaultExchangeService");
        setupExchangeAndQueue("amqpMessageLevelOverrideService");
    }

    @Override
    protected String getConfigResources()
    {
        return "message-dispatcher-tests-config.xml";
    }

    public void testDispatchToExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpExistingExchangeService");
    }

    public void testDispatchToRedeclaredExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpRedeclaredExistingExchangeService");
    }

    public void testDispatchToDefaultExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpDefaultExchangeService");
    }

    public void testMessageLevelOverrideService() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpMessageLevelOverrideService");
    }

    public void testDispatchToNewExchange() throws Exception
    {
        final String bridgeName = "amqpNewExchangeService";
        new MuleClient(muleContext).dispatch("vm://" + bridgeName + ".in", "ignored_payload", null);

        // there is no queue bound to this new exchange, so we can only test its presence
        int attempts = 0;
        while (attempts < DEFAULT_MULE_TEST_TIMEOUT_SECS * 2)
        {
            try
            {
                getChannel().exchangeDeclarePassive(getExchangeName(bridgeName));
                return;
            }
            catch (final IOException ioe)
            {
                attempts++;
                Thread.sleep(500L);
            }
        }
        fail("Exchange not created by outbound endpoint");
    }

    public void testRequestResponse() throws Exception
    {
        final String customHeaderValue = UUID.getUUID();
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        final MuleMessage response = new MuleClient(muleContext).send("vm://amqpRequestResponseService.in",
            payload, Collections.singletonMap("customHeader", customHeaderValue),
            DEFAULT_MULE_TEST_TIMEOUT_SECS * 1000);

        assertEquals(payload + "-response", response.getPayloadAsString());
        assertEquals(customHeaderValue, response.getInboundProperty("customHeader").toString());
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
