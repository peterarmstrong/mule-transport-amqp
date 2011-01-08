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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.api.MuleMessage;

public class AmqpMessageReceiverITCase extends AbstractAmqpITCase
{
    public AmqpMessageReceiverITCase() throws IOException
    {
        super();
        // create the required pre-existing exchanges and queues
        setupExchangeAndQueue("amqpExistingQueueService");
        setupExchange("amqpServerNamedQueueExistingExchangeService");
        setupExchange("amqpNewQueueExistingExchangeService");
        setupExchange("amqpNewQueueRedeclaredExistingExchangeService");
        setupExchangeAndQueue("amqpMuleAckService");
        setupExchangeAndQueue("amqpManualAckService");
        setupExchangeAndQueue("amqpManualRejectService");
        setupExchangeAndQueue("amqpExclusiveConsumerService");
    }

    @Override
    protected String getConfigResources()
    {
        return "message-receiver-tests-config.xml";
    }

    public void testExistingQueue() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpExistingQueueService");
    }

    public void testServerNamedQueueExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpServerNamedQueueExistingExchangeService");
    }

    public void testNewQueueExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpNewQueueExistingExchangeService");
    }

    public void testNewQueueRedeclaredExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpNewQueueRedeclaredExistingExchangeService");
    }

    public void testNewQueueNewExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpNewQueueNewExchangeService");
    }

    public void testMuleAcknowledgment() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpMuleAckService");
    }

    public void testManualAcknowledgment() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpManualAckService");
    }

    public void testManualRejection() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpManualRejectService");
        // check the message has been successfully pushed back to the queue
        assertNotNull(consumeMessageWithAmqp(getQueueName("amqpManualRejectService"),
            DEFAULT_MULE_TEST_TIMEOUT_SECS));
    }

    public void testExclusiveConsumer() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpExclusiveConsumerService");
    }

    private void dispatchTestMessageAndAssertValidReceivedMessage(final String flowName)
        throws Exception, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        final Future<MuleMessage> futureReceivedMessage = setupFunctionTestComponentForFlow(flowName);

        final byte[] body = RandomStringUtils.randomAlphanumeric(20).getBytes();
        final String correlationId = publishMessageWithAmqp(body, flowName);

        final MuleMessage receivedMessage = futureReceivedMessage.get(DEFAULT_MULE_TEST_TIMEOUT_SECS,
            TimeUnit.SECONDS);

        assertValidReceivedMessage(correlationId, body, receivedMessage);
    }
}
