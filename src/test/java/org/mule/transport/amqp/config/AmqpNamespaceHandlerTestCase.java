/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.amqp.config;

import java.util.List;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.SimpleFlowConstruct;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.amqp.AmqpConnector;
import org.mule.transport.amqp.AmqpConstants.AckMode;
import org.mule.transport.amqp.AmqpEndpointUtil;
import org.mule.transport.amqp.AmqpMessageAcknowledger;
import org.mule.transport.amqp.transformers.AmqpMessageToObject;
import org.mule.transport.amqp.transformers.ObjectToAmqpMessage;

public class AmqpNamespaceHandlerTestCase extends FunctionalTestCase
{
    public AmqpNamespaceHandlerTestCase()
    {
        super();
        setStartContext(false);
        setDisposeManagerPerSuite(true);
    }

    @Override
    protected String getConfigResources()
    {
        return "amqp-namespace-config.xml";
    }

    public void testDefaultConnector() throws Exception
    {
        final AmqpConnector c = (AmqpConnector) muleContext.getRegistry().lookupConnector(
            "amqpDefaultConnector");
        assertNotNull(c);

        assertEquals(AckMode.AMQP_AUTO, c.getAckMode());
    }

    public void testFullGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpFullGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-exchange/amqp-queue.target-queue", inboundEndpoint.getAddress());
        assertEquals("amqp://target-exchange/amqp-queue.target-queue", inboundEndpoint.getEndpointURI()
            .getAddress());
        assertEquals("a.b.c", inboundEndpoint.getProperty(AmqpEndpointUtil.ROUTING_KEY));
        assertEquals("true", inboundEndpoint.getProperty(AmqpEndpointUtil.EXCHANGE_DURABLE));

        final OutboundEndpoint outboundEndpoint = endpointBuilder.buildOutboundEndpoint();
        assertEquals("amqp://target-exchange/amqp-queue.target-queue", outboundEndpoint.getAddress());
        assertEquals("amqp://target-exchange/amqp-queue.target-queue", outboundEndpoint.getEndpointURI()
            .getAddress());
        assertEquals("a.b.c", outboundEndpoint.getProperty(AmqpEndpointUtil.ROUTING_KEY));
        assertEquals("true", outboundEndpoint.getProperty(AmqpEndpointUtil.EXCHANGE_DURABLE));
    }

    public void testExistingQueueGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpExistingQueueGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://amqp-queue.target-queue", inboundEndpoint.getAddress());
        assertEquals("amqp://amqp-queue.target-queue", inboundEndpoint.getEndpointURI().getAddress());
    }

    public void testPrivateQueueGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpPrivateQueueGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-exchange", inboundEndpoint.getAddress());
        assertEquals("amqp://target-exchange", inboundEndpoint.getEndpointURI().getAddress());
    }

    public void testExistingExchangeGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpExistingExchangeGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-exchange", inboundEndpoint.getAddress());
        assertEquals("amqp://target-exchange", inboundEndpoint.getEndpointURI().getAddress());
    }

    public void testGlobalTransformers() throws Exception
    {
        assertTrue(muleContext.getRegistry().lookupTransformer("a2o") instanceof AmqpMessageToObject);
        assertTrue(muleContext.getRegistry().lookupTransformer("o2a") instanceof ObjectToAmqpMessage);
    }

    public void testAcknowledger() throws Exception
    {
        final List<MessageProcessor> messageProcessors = ((SimpleFlowConstruct) muleContext.getRegistry()
            .lookupFlowConstruct("ackerFlow")).getMessageProcessors();
        assertEquals(1, messageProcessors.size());
        System.out.println(messageProcessors.get(0));
        assertTrue(messageProcessors.get(0) instanceof AmqpMessageAcknowledger);
    }
}
