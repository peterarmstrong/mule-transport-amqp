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

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.amqp.AmqpConnector;
import org.mule.transport.amqp.AmqpConstants.AckMode;
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
        // TODO add more assertions
    }

    public void testFullGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpFullGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-exchange/target-queue", inboundEndpoint.getAddress());
        final EndpointURI inboundEndpointURI = inboundEndpoint.getEndpointURI();
        assertEquals("target-exchange", inboundEndpointURI.getHost());
        assertEquals("/target-queue", inboundEndpointURI.getPath());
        assertEquals("a.b.c", inboundEndpoint.getProperty("routingKey"));

        final OutboundEndpoint outboundEndpoint = endpointBuilder.buildOutboundEndpoint();
        assertEquals("amqp://target-exchange/target-queue", outboundEndpoint.getAddress());
        final EndpointURI outboundEndpointURI = outboundEndpoint.getEndpointURI();
        assertEquals("target-exchange", outboundEndpointURI.getHost());
        assertEquals("/target-queue", outboundEndpointURI.getPath());
        assertEquals("a.b.c", outboundEndpoint.getProperty("routingKey"));
        // TODO add more assertions
    }

    public void testExistingQueueGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpExistingQueueGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-queue", inboundEndpoint.getAddress());
        final EndpointURI inboundEndpointURI = inboundEndpoint.getEndpointURI();
        assertEquals("target-queue", inboundEndpointURI.getHost());
        assertEquals("", inboundEndpointURI.getPath());
    }

    public void testPrivateQueueGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpPrivateQueueGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-exchange", inboundEndpoint.getAddress());
        final EndpointURI inboundEndpointURI = inboundEndpoint.getEndpointURI();
        assertEquals("target-exchange", inboundEndpointURI.getHost());
        assertEquals("", inboundEndpointURI.getPath());
    }

    public void testExistingExchangeGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpExistingExchangeGlobalEndpoint");
        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        assertEquals("amqp://target-exchange", inboundEndpoint.getAddress());
        final EndpointURI inboundEndpointURI = inboundEndpoint.getEndpointURI();
        assertEquals("target-exchange", inboundEndpointURI.getHost());
        assertEquals("", inboundEndpointURI.getPath());
    }

    // TODO add more tests for other endpoints

    public void testGlobalTransformers() throws Exception
    {
        assertTrue(muleContext.getRegistry().lookupTransformer("a2o") instanceof AmqpMessageToObject);
        assertTrue(muleContext.getRegistry().lookupTransformer("o2a") instanceof ObjectToAmqpMessage);
    }
}
