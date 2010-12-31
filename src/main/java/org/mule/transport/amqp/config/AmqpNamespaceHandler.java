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
import org.mule.config.spring.parsers.AbstractMuleBeanDefinitionParser;
import org.mule.config.spring.parsers.MuleChildDefinitionParser;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.assembly.configuration.PrefixValueMap;
import org.mule.config.spring.parsers.delegate.AbstractSingleParentFamilyDefinitionParser;
import org.mule.config.spring.parsers.generic.AttributePropertiesDefinitionParser;
import org.mule.config.spring.parsers.processors.BlockAttribute;
import org.mule.config.spring.parsers.processors.CheckRequiredAttributes;
import org.mule.config.spring.parsers.specific.MessageProcessorDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportGlobalEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.support.ChildAddressDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.support.ChildEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.support.OrphanEndpointDefinitionParser;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.amqp.AmqpConnector;
import org.mule.transport.amqp.AmqpEndpointUtil;
import org.mule.transport.amqp.AmqpMessageAcknowledger;
import org.mule.transport.amqp.AmqpReturnHandler;
import org.mule.transport.amqp.transformers.AmqpMessageToObject;
import org.mule.transport.amqp.transformers.ObjectToAmqpMessage;

/**
 * Registers a Bean Definition Parser for handling <code><amqp:connector></code> elements and supporting endpoint
 * elements.
 */
public class AmqpNamespaceHandler extends AbstractMuleNamespaceHandler
{
    private static final String[][] AMQP_ENDPOINT_ATTRIBUTES = new String[][]{new String[]{"queueName"},
        new String[]{"exchangeName"}, new String[]{"exchangeName", "queueName"}};

    public void init()
    {
        registerAmqpTransportEndpoints();

        registerConnectorDefinitionParser(AmqpConnector.class);

        registerBeanDefinitionParser("amqpmessage-to-object-transformer",
            new MessageProcessorDefinitionParser(AmqpMessageToObject.class));
        registerBeanDefinitionParser("object-to-amqpmessage-transformer",
            new MessageProcessorDefinitionParser(ObjectToAmqpMessage.class));

        registerBeanDefinitionParser("acknowledge-message", new MessageProcessorDefinitionParser(
            AmqpMessageAcknowledger.class));

        registerBeanDefinitionParser("return-handler", new MessageProcessorDefinitionParser(
            AmqpReturnHandler.class));
    }

    protected void registerAmqpTransportEndpoints()
    {
        registerErlangEndpointDefinitionParser("endpoint", new NonExclusiveAddressedEndpointDefinitionParser(
            AmqpConnector.AMQP, TransportGlobalEndpointDefinitionParser.PROTOCOL,
            new OrphanEndpointDefinitionParser(EndpointURIEndpointBuilder.class),
            TransportGlobalEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES,
            URIBuilder.ALL_ATTRIBUTES, AMQP_ENDPOINT_ATTRIBUTES, new String[][]{}));

        registerErlangEndpointDefinitionParser("inbound-endpoint",
            new NonExclusiveAddressedEndpointDefinitionParser(AmqpConnector.AMQP,
                TransportEndpointDefinitionParser.PROTOCOL, new ChildEndpointDefinitionParser(
                    InboundEndpointFactoryBean.class),
                TransportEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES, URIBuilder.ALL_ATTRIBUTES,
                AMQP_ENDPOINT_ATTRIBUTES, new String[][]{}));

        registerErlangEndpointDefinitionParser("outbound-endpoint",
            new NonExclusiveAddressedEndpointDefinitionParser(AmqpConnector.AMQP,
                TransportEndpointDefinitionParser.PROTOCOL, new ChildEndpointDefinitionParser(
                    OutboundEndpointFactoryBean.class),
                TransportEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES, URIBuilder.ALL_ATTRIBUTES,
                AMQP_ENDPOINT_ATTRIBUTES, new String[][]{}));
    }

    protected void registerErlangEndpointDefinitionParser(final String element,
                                                          final MuleDefinitionParser parser)
    {
        parser.addAlias("exchangeName", URIBuilder.HOST);
        parser.addAlias("queueName", URIBuilder.PATH);
        parser.addMapping("queueName", new PrefixValueMap(AmqpEndpointUtil.QUEUE_PREFIX));
        registerBeanDefinitionParser(element, parser);
    }

    /**
     * The following specific parser exists because the default AddressedEndpointDefinitionParser.AddressParser enforces
     * exclusivity of address attributes which is not OK for AMQP endpoints.
     */
    private static class NonExclusiveAddressedEndpointDefinitionParser extends
        AbstractSingleParentFamilyDefinitionParser
    {
        public static final String PROPERTIES = "properties";

        private NonExclusiveAddressedEndpointDefinitionParser(final String metaOrProtocol,
                                                              final boolean isMeta,
                                                              final MuleDefinitionParser endpointParser,
                                                              final String[] endpointAttributes,
                                                              final String[] addressAttributes,
                                                              final String[][] requiredAddressAttributes,
                                                              final String[][] requiredProperties)
        {
            // the first delegate, the parent, is an endpoint; we block everything except the endpoint attributes
            enableAttributes(endpointParser, endpointAttributes);
            enableAttribute(endpointParser, AbstractMuleBeanDefinitionParser.ATTRIBUTE_NAME);
            addDelegate(endpointParser);

            // we handle the address and properties separately, setting the
            // properties directly on the endpoint (rather than as part of the address)
            final MuleChildDefinitionParser addressParser = new AddressParser(metaOrProtocol, isMeta,
                addressAttributes, requiredAddressAttributes);

            // this handles the exception thrown if a ref is found in the address parser
            addHandledException(BlockAttribute.BlockAttributeException.class);
            addChildDelegate(addressParser);

            final MuleChildDefinitionParser propertiesParser = new PropertiesParser(PROPERTIES,
                endpointAttributes, requiredAddressAttributes, requiredProperties);
            addChildDelegate(propertiesParser);
        }

        private static class AddressParser extends ChildAddressDefinitionParser
        {

            public AddressParser(final String metaOrProtocol,
                                 final boolean isMeta,
                                 final String[] addressAttributes,
                                 final String[][] requiredAddressAttributes)
            {
                super(metaOrProtocol, isMeta);

                // this handles the "ref problem" - we don't want this parsers to be used if a "ref"
                // defines the address so add a preprocessor to check for that and indicate that the
                // exception should be handled internally, rather than shown to the user.
                // we do this before the extra processors below so that this is called last,
                // allowing other processors to check for conflicts between ref and other attributes
                registerPreProcessor(new BlockAttribute(AbstractMuleBeanDefinitionParser.ATTRIBUTE_REF));

                // the address parser sees only the endpoint attributes
                enableAttributes(this, addressAttributes);

                // we require either a reference, an address, or the attributes specified
                // (properties can be used in parallel with "address")
                final String[][] addressAttributeSets = new String[(null != requiredAddressAttributes
                                                                                                     ? requiredAddressAttributes.length
                                                                                                     : 0) + 2][];
                addressAttributeSets[0] = new String[]{URIBuilder.ADDRESS};
                addressAttributeSets[1] = new String[]{AbstractMuleBeanDefinitionParser.ATTRIBUTE_REF};
                if (null != requiredAddressAttributes)
                {
                    enableAttributes(this, requiredAddressAttributes);
                    System.arraycopy(requiredAddressAttributes, 0, addressAttributeSets, 2,
                        requiredAddressAttributes.length);
                }
                registerPreProcessor(new CheckRequiredAttributes(addressAttributeSets));
            }

        }

        private static class PropertiesParser extends AttributePropertiesDefinitionParser
        {

            public PropertiesParser(final String setter,
                                    final String[] endpointAttributes,
                                    final String[][] requiredAddressAttributes,
                                    final String[][] requiredProperties)
            {
                super(setter);

                // the properties parser gets to see everything that the other parsers don't - if you
                // don't want something, don't enable it in the schema!
                disableAttributes(this, endpointAttributes);
                disableAttributes(this, URIBuilder.ALL_ATTRIBUTES);
                disableAttributes(this, requiredAddressAttributes);
                disableAttribute(this, AbstractMuleBeanDefinitionParser.ATTRIBUTE_NAME);
                disableAttribute(this, AbstractMuleBeanDefinitionParser.ATTRIBUTE_REF);
                if (null != requiredProperties && requiredProperties.length > 0
                    && null != requiredProperties[0] && requiredProperties[0].length > 0)
                {
                    // if "ref" is present then we don't complain if required properties are missing, since they
                    // must have been provided on the global endpoint
                    final String[][] requiredPropertiesSets = new String[requiredProperties.length + 1][];
                    requiredPropertiesSets[0] = new String[]{AbstractMuleBeanDefinitionParser.ATTRIBUTE_REF};
                    System.arraycopy(requiredProperties, 0, requiredPropertiesSets, 1,
                        requiredProperties.length);
                    registerPreProcessor(new CheckRequiredAttributes(requiredPropertiesSets));
                }
            }

        }

    }

}
