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

public abstract class AmqpConstants
{
    // properties names are consistent with AMQP spec
    public static final String USER_ID = "user-id";
    public static final String TYPE = "type";
    public static final String TIMESTAMP = "timestamp";
    public static final String REPLY_TO = "reply-to";
    public static final String PRIORITY = "priority";
    public static final String MESSAGE_ID = "message-id";
    public static final String EXPIRATION = "expiration";
    public static final String DELIVERY_MODE = "delivery_mode";
    public static final String CORRELATION_ID = "correlation-id";
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String APP_ID = "app-id";
    public static final String ROUTING_KEY = "routing-key";
    public static final String EXCHANGE = "exchange";
    public static final String DELIVERY_TAG = "delivery-tag";
    public static final String CONSUMER_TAG = "consumer-tag";
}
