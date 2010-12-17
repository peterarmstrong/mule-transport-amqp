
package org.mule.transport.amqp;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.api.MuleEventContext;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.util.concurrent.Latch;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

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

        // declare an fanout exchange and bind a queue to it
        channel.exchangeDeclare(PRE_EXISTING_EXCHANGE, "fanout");
        channel.queueDeclare(PRE_EXISTING_QUEUE, false, false, true, Collections.<String, Object> emptyMap());
        channel.queueBind(PRE_EXISTING_QUEUE, PRE_EXISTING_EXCHANGE, "");
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
        final Latch messageReceivedLatch = new Latch();
        final FunctionalTestComponent functionalTestComponent = getFunctionalTestComponent("amqpExistingQueueService");
        functionalTestComponent.setEventCallback(new EventCallback()
        {
            @Override
            public void eventReceived(final MuleEventContext context, final Object component)
                throws Exception
            {
                messageReceivedLatch.release();
            }
        });

        final byte[] body = RandomStringUtils.randomAlphanumeric(20).getBytes();
        final BasicProperties props = new BasicProperties();
        props.setContentType("text/plain");
        props.setHeaders(Collections.<String, Object> singletonMap("customHeader", 123L));
        channel.basicPublish(PRE_EXISTING_EXCHANGE, "ignored", props, body);

        messageReceivedLatch.await(30, TimeUnit.SECONDS);

        assertEquals(1, functionalTestComponent.getReceivedMessagesCount());
        final Object lastReceivedMessage = functionalTestComponent.getLastReceivedMessage();
        assertTrue(lastReceivedMessage instanceof byte[]);
        assertTrue(Arrays.equals(body, (byte[]) lastReceivedMessage));
    }
}
