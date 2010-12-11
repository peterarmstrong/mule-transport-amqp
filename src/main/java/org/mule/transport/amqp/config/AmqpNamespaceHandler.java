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

import org.mule.config.spring.factories.InboundEndpointFactoryBean;
import org.mule.config.spring.factories.OutboundEndpointFactoryBean;
import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportGlobalEndpointDefinitionParser;
import org.mule.transport.amqp.AmqpConnector;

/**
 * Registers a Bean Definition Parser for handling <code><amqp:connector></code> elements and supporting endpoint
 * elements.
 */
public class AmqpNamespaceHandler extends AbstractMuleNamespaceHandler
{
    // TODO refactor to constants
    public static final String[][] AMQP_ENDPOINT_ATTRIBUTES = new String[][]{
        new String[]{"exchangeName", "routingKey"}, new String[]{"exchangeName", "queueName", "routingKey"},
        new String[]{"queueName"}};

    public void init()
    {
        registerAmqpTransportEndpoints();

        registerConnectorDefinitionParser(AmqpConnector.class);
    }

    protected void registerAmqpTransportEndpoints()
    {
        registerErlangEndpointDefinitionParser("endpoint", new TransportGlobalEndpointDefinitionParser(
            AmqpConnector.AMQP, TransportGlobalEndpointDefinitionParser.PROTOCOL,
            TransportGlobalEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES, AMQP_ENDPOINT_ATTRIBUTES,
            new String[][]{}));

        registerErlangEndpointDefinitionParser("inbound-endpoint", new TransportEndpointDefinitionParser(
            AmqpConnector.AMQP, TransportEndpointDefinitionParser.PROTOCOL, InboundEndpointFactoryBean.class,
            TransportEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES, AMQP_ENDPOINT_ATTRIBUTES,
            new String[][]{}));

        registerErlangEndpointDefinitionParser("outbound-endpoint", new TransportEndpointDefinitionParser(
            AmqpConnector.AMQP, TransportEndpointDefinitionParser.PROTOCOL,
            OutboundEndpointFactoryBean.class,
            TransportEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES, AMQP_ENDPOINT_ATTRIBUTES,
            new String[][]{}));
    }

    protected void registerErlangEndpointDefinitionParser(final String element,
                                                          final MuleDefinitionParser parser)
    {
        // parser.addAlias("", URIBuilder.PATH);
        // parser.addAlias("", URIBuilder.HOST);
        registerBeanDefinitionParser(element, parser);
    }
}
