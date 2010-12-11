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

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.transport.AbstractConnector;
import org.mule.transport.amqp.AmqpConstants.AckMode;
import org.mule.transport.amqp.AmqpConstants.DeliveryMode;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Connects to a particular virtual host on a particular AMQP broker.
 */
public class AmqpConnector extends AbstractConnector
{
    public static final String AMQP = "amqp";

    private String host;
    private int port;
    private String virtualHost;
    private String username;
    private String password;
    private DeliveryMode deliveryMode;
    private byte priority;
    private AckMode ackMode;

    private ConnectionFactory connectionFactory;
    private Connection connection;

    public AmqpConnector(final MuleContext context)
    {
        super(context);
    }

    @Override
    public void doInitialise() throws InitialisationException
    {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
    }

    @Override
    public void doDispose()
    {
        connection = null;
        connectionFactory = null;
    }

    @Override
    public void doConnect() throws Exception
    {
        connection = connectionFactory.newConnection();
    }

    @Override
    public void doDisconnect() throws Exception
    {
        connection.close();
    }

    @Override
    public void doStart() throws MuleException
    {
        // NOOP
    }

    @Override
    public void doStop() throws MuleException
    {
        // NOOP
    }

    public Channel newChannel() throws IOException
    {
        return connection.createChannel();
    }

    public String getProtocol()
    {
        return AMQP;
    }

    public byte getPriority()
    {
        return priority;
    }

    public void setPriority(final byte priority)
    {
        this.priority = priority;
    }

    public AckMode getAckMode()
    {
        return ackMode;
    }

    public void setAckMode(final AckMode ackMode)
    {
        this.ackMode = ackMode;
    }

    public DeliveryMode getDeliveryMode()
    {
        return deliveryMode;
    }

    public void setDeliveryMode(final DeliveryMode deliveryMode)
    {
        this.deliveryMode = deliveryMode;
    }

    public void setHost(final String host)
    {
        this.host = host;
    }

    public void setPort(final int port)
    {
        this.port = port;
    }

    public void setVirtualHost(final String virtualHost)
    {
        this.virtualHost = virtualHost;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }
}
