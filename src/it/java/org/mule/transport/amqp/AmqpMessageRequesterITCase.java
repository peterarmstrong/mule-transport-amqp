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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;

public class AmqpMessageRequesterITCase extends AbstractAmqpITCase
{
    public AmqpMessageRequesterITCase() throws IOException
    {
        super();

        // create the required pre-existing exchanges and queues
        setupExchangeAndQueue("amqpAutoAckRequester");
        setupExchangeAndQueue("amqpMuleAckRequester");
        setupExchangeAndQueue("amqpManualAckRequester");
        setupExchangeAndQueue("amqpTimeOutRequester");
    }

    @Override
    protected String getConfigResources()
    {
        return "message-requester-tests-config.xml";
    }

    public void testAutoAcknowledgment() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpAutoAckRequester",
            "amqpAutoAckLocalhostConnector");
    }

    public void testMuleAcknowledgment() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpMuleAckRequester",
            "amqpMuleAckLocalhostConnector");
    }

    public void testManualAcknowledgment() throws Exception
    {
        final MuleMessage receivedMessage = dispatchTestMessageAndAssertValidReceivedMessage(
            "amqpManualAckRequester", "amqpManualAckLocalhostConnector");

        AmqpMessageAcknowledger.ack(receivedMessage, false);
    }

    public void testTimeOut() throws Exception
    {
        final long startTime = System.nanoTime();

        final MuleMessage receivedMessage = new MuleClient(muleContext).request(
            "amqp://amqp-queue." + getQueueName("amqpTimeOutRequester")
                            + "?connector=amqpAutoAckLocalhostConnector", 2500L);

        assertNull(receivedMessage);
        final long durationNano = System.nanoTime() - startTime;
        assertTrue(TimeUnit.MILLISECONDS.convert(durationNano, TimeUnit.NANOSECONDS) >= 2400L);
    }

    private MuleMessage dispatchTestMessageAndAssertValidReceivedMessage(final String flowName,
                                                                         final String connectorName)
        throws Exception, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        final byte[] body = RandomStringUtils.randomAlphanumeric(20).getBytes();
        final String correlationId = dispatchTestMessage(body, flowName);

        final MuleMessage receivedMessage = new MuleClient(muleContext).request("amqp://amqp-queue."
                                                                                + getQueueName(flowName)
                                                                                + "?connector="
                                                                                + connectorName,
            DEFAULT_MULE_TEST_TIMEOUT_SECS * 1000L);

        assertValidReceivedMessage(correlationId, body, receivedMessage);

        return receivedMessage;
    }
}
