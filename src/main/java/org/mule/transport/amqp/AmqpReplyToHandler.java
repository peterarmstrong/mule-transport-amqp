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

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.Transformer;
import org.mule.transport.DefaultReplyToHandler;

public class AmqpReplyToHandler extends DefaultReplyToHandler
{
    public AmqpReplyToHandler(final AmqpConnector amqpConnector)
    {
        super(Collections.<Transformer> emptyList(), amqpConnector.getMuleContext());
    }

    @Override
    public void processReplyTo(final MuleEvent event, final MuleMessage returnMessage, final Object replyTo)
        throws MuleException
    {
        System.err.println("*** Must reply to: " + replyTo);
        System.err.println(event);
        System.err.println(returnMessage);

        // FIXME implement
        throw new UnsupportedOperationException("Implement me!");
    }

}
