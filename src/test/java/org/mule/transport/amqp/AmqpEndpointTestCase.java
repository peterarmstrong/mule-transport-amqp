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

import org.mule.api.endpoint.EndpointURI;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;

public class AmqpEndpointTestCase extends AbstractMuleTestCase
{
    public void testValidEndpointURI() throws Exception
    {
        final EndpointURI url = new MuleEndpointURI("amqp://resource.name", muleContext);
        assertEquals("amqp", url.getScheme());
        // using the host as resource name could be an issue because exchange and queue names accept characters that are
        // invalid in host names: ^[a-zA-Z0-9-_.:]*$
        assertEquals("resource.name", url.getHost());
    }
}
