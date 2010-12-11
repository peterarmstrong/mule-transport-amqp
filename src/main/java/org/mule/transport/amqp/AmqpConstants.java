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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AmqpConstants
{
    public enum DeliveryMode
    {
        NON_PERSISTENT(1), PERSISTENT(2);

        private final int code;

        private DeliveryMode(final int code)
        {
            this.code = code;
        }

        public int getCode()
        {
            return code;
        }
    }

    public enum AckMode
    {
        AUTO(true), MANUAL(false);

        private final boolean autoAck;

        private AckMode(final boolean autoAck)
        {
            this.autoAck = autoAck;
        }

        public boolean isAutoAck()
        {
            return autoAck;
        }
    }

    // properties names are consistent with AMQP spec
    // (cluster-id is deprecated and not supported here)
    public static final String APP_ID = "app-id";
    public static final String CONSUMER_TAG = "consumer-tag";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String CONTENT_TYPE = "content-type";
    public static final String CORRELATION_ID = "correlation-id";
    public static final String DELIVERY_MODE = "delivery_mode";
    public static final String DELIVERY_TAG = "delivery-tag";
    public static final String EXCHANGE = "exchange";
    public static final String EXPIRATION = "expiration";
    public static final String MESSAGE_ID = "message-id";
    public static final String PRIORITY = "priority";
    public static final String REDELIVER = "redelivered";
    public static final String REPLY_TO = "reply-to";
    public static final String ROUTING_KEY = "routing-key";
    public static final String TIMESTAMP = "timestamp";
    public static final String TYPE = "type";
    public static final String USER_ID = "user-id";

    public static final Set<String> AMQP_PROPERTY_NAMES = Collections.unmodifiableSet(new HashSet<String>(
        Arrays.asList(new String[]{APP_ID, CONSUMER_TAG, CONTENT_ENCODING, CONTENT_TYPE, CORRELATION_ID,
            DELIVERY_MODE, DELIVERY_TAG, EXCHANGE, EXPIRATION, MESSAGE_ID, PRIORITY, REPLY_TO, REDELIVER,
            ROUTING_KEY, TIMESTAMP, TYPE, USER_ID})));
}
