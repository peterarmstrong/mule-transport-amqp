
package org.mule.transport.amqp;

import java.util.Collections;

import org.mule.tck.FunctionalTestCase;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AmqpMessageReceiverITCase extends FunctionalTestCase
{
    private static final String PRE_EXISTING_QUEUE = "pre-existing-queue";
    private static final String PRE_EXISTING_EXCHANGE = "pre-existing-exchange";
    private Connection conn;
    private Channel channel;

    @Override
    protected void suitePreSetUp() throws Exception
    {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("mule");
        factory.setPassword("elum");
        factory.setVirtualHost("mule-test");
        conn = factory.newConnection();
        channel = conn.createChannel();

        // declare an fanout exchange an bind a queue to it
        channel.exchangeDeclare(PRE_EXISTING_EXCHANGE, "fanout");
        channel.queueDeclare(PRE_EXISTING_QUEUE, false, false, true, Collections.<String, Object> emptyMap());

    }

    @Override
    protected void suitePostTearDown() throws Exception
    {
        channel.close();
        conn.close();
    }

    @Override
    protected String getConfigResources()
    {
        return "message-receiver-tests-config.xml";
    }

    public void testExistingQueue() throws Exception
    {
        // FIXME re-activate test when transport is able of clean shutdown

        // final FunctionalTestComponent functionalTestComponent =
        // getFunctionalTestComponent("amqpExistingQueueService");
        // final byte[] body = RandomStringUtils.randomAlphanumeric(20).getBytes();
        // final BasicProperties props = new BasicProperties();
        // props.setContentType("text/plain");
        // props.setHeaders(Collections.<String, Object> singletonMap("customHeader", 123L));
        // channel.basicPublish(PRE_EXISTING_EXCHANGE, "ignored", props, body);

        // temporary: wait 1s and stop
        Thread.sleep(1000L);
    }
}
