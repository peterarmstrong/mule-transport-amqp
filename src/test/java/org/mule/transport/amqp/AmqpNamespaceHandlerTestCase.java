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

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.tck.FunctionalTestCase;

/**
 * TODO
 */
public class AmqpNamespaceHandlerTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "amqp-namespace-config.xml";
    }

    public void testAmqpDefaultConnector() throws Exception
    {
        final AmqpConnector c = (AmqpConnector) muleContext.getRegistry().lookupConnector(
            "amqpDefaultConnector");
        assertNotNull(c);
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());

        // TODO Assert specific properties are configured correctly
    }

    public void testAmqpFullGlobalEndpoint() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getRegistry().lookupEndpointBuilder(
            "amqpFullGlobalEndpoint");
        assertNotNull(endpointBuilder);

        // TODO Assert specific properties are configured correctly
    }
}
