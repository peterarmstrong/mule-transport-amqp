
package org.mule.transport.amqp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.util.UUID;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AmqpMessageReceiverITCase extends FunctionalTestCase
{
    private final Connection conn;
    private final Channel channel;

    public AmqpMessageReceiverITCase() throws IOException
    {
        super();
        setDisposeManagerPerSuite(true);

        final ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("mule");
        factory.setPassword("elum");
        factory.setVirtualHost("mule-test");
        conn = factory.newConnection();
        channel = conn.createChannel();

        // create the required pre-existing exchanges and queues
        setupExchangeAndQueueForFlow("amqpExistingQueueService");
        setupExchangeForFlow("amqpServerNamedQueueExistingExchangeService");
        setupExchangeForFlow("amqpNewQueueExistingExchangeService");
        setupExchangeForFlow("amqpNewQueueRedeclaredExistingExchangeService");
    }

    @Override
    protected boolean isGracefulShutdown()
    {
        return true;
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
        dispatchTestMessageAndAssertValidReceivedMessage("amqpExistingQueueService");
    }

    public void testServerNamedQueueExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpServerNamedQueueExistingExchangeService");
    }

    public void testNewQueueExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpNewQueueExistingExchangeService");
    }

    public void testNewQueueRedeclaredExistingExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpNewQueueRedeclaredExistingExchangeService");
    }

    public void testNewQueueNewExchange() throws Exception
    {
        dispatchTestMessageAndAssertValidReceivedMessage("amqpNewQueueNewExchangeService");
    }

    private void dispatchTestMessageAndAssertValidReceivedMessage(final String flowName)
        throws Exception, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        final Future<MuleMessage> futureReceivedMessage = setupFunctionTestComponentForFlow(flowName);

        final byte[] body = RandomStringUtils.randomAlphanumeric(20).getBytes();
        final String correlationId = dispatchTestMessageToFlow(body, flowName);

        final MuleMessage receivedMessage = futureReceivedMessage.get(DEFAULT_MULE_TEST_TIMEOUT_SECS,
            TimeUnit.SECONDS);

        assertValidReceivedMessage(correlationId, body, receivedMessage);
    }

    private String dispatchTestMessageToFlow(final byte[] body, final String flowName) throws IOException
    {
        final String correlationId = UUID.getUUID();
        final BasicProperties props = new BasicProperties();
        props.setContentType("text/plain");
        props.setCorrelationId(correlationId);
        props.setHeaders(Collections.<String, Object> singletonMap("customHeader", 123L));
        channel.basicPublish(getExchangeForFlow(flowName), "", props, body);
        return correlationId;
    }

    private void assertValidReceivedMessage(final String correlationId,
                                            final byte[] body,
                                            final MuleMessage receivedMessage) throws Exception
    {
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.getPayload() instanceof byte[]);
        assertTrue(Arrays.equals(body, receivedMessage.getPayloadAsBytes()));
        assertEquals(correlationId, receivedMessage.getCorrelationId());
        assertEquals(correlationId, receivedMessage.getInboundProperty(AmqpConstants.CORRELATION_ID));
        assertEquals(123L, receivedMessage.getInboundProperty("customHeader"));
    }

    private Future<MuleMessage> setupFunctionTestComponentForFlow(final String flowName) throws Exception
    {

        final FunctionalTestComponent functionalTestComponent = getFunctionalTestComponent(flowName);

        final CountDownLatch messageReceivedLatch = new CountDownLatch(1);
        final AtomicReference<MuleMessage> receivedMessageRef = new AtomicReference<MuleMessage>(null);

        functionalTestComponent.setEventCallback(new EventCallback()
        {
            @Override
            public void eventReceived(final MuleEventContext context, final Object component)
                throws Exception
            {
                receivedMessageRef.set(context.getMessage());
                messageReceivedLatch.countDown();
            }
        });

        final Future<MuleMessage> futureReceivedMessage = new Future<MuleMessage>()
        {
            public boolean cancel(final boolean mayInterruptIfRunning)
            {
                throw new UnsupportedOperationException();
            }

            public boolean isCancelled()
            {
                throw new UnsupportedOperationException();
            }

            public boolean isDone()
            {
                throw new UnsupportedOperationException();
            }

            public MuleMessage get() throws InterruptedException, ExecutionException
            {
                throw new UnsupportedOperationException();
            }

            public MuleMessage get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException
            {
                messageReceivedLatch.await(timeout, unit);
                return receivedMessageRef.get();
            }
        };

        return futureReceivedMessage;
    }

    private void setupExchangeAndQueueForFlow(final String flowName) throws IOException
    {
        final String exchange = setupExchangeForFlow(flowName);
        final String queue = flowName + "-queue";
        channel.queueDeclare(queue, false, false, true, Collections.<String, Object> emptyMap());
        channel.queueBind(queue, exchange, "");
    }

    private String setupExchangeForFlow(final String flowName) throws IOException
    {
        final String exchange = getExchangeForFlow(flowName);
        channel.exchangeDeclare(exchange, "fanout");
        return exchange;
    }

    private static String getExchangeForFlow(final String flowName)
    {
        return flowName + "-exchange";
    }
}
