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

import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;

public class AmqpConnectorTestCase extends AbstractConnectorTestCase
{
    /* For general guidelines on writing transports see
       http://www.mulesoft.org/documentation/display/MULE3USER/Creating+Transports */

    @Override
    public Connector createConnector() throws Exception
    {
        /* IMPLEMENTATION NOTE: Create and initialise an instance of your
           connector here. Do not actually call the connect method. */

        AmqpConnector connector = new AmqpConnector(muleContext);
        connector.setName("Test");
        // TODO Set any additional properties on the connector here
        return connector;
    }

    @Override
    public String getTestEndpointURI()
    {
        // TODO Return a valid endpoint for you transport here
        throw new UnsupportedOperationException("getTestEndpointURI");
    }

    @Override
    public Object getValidMessage() throws Exception
    {
        // TODO Return an valid message for your transport
        throw new UnsupportedOperationException("getValidMessage");
    }


    public void testProperties() throws Exception
    {
        // TODO test setting and retrieving any custom properties on the
        // Connector as necessary
    }
}
