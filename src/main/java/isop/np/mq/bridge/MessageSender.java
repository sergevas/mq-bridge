package isop.np.mq.bridge;

import javax.jms.*;
import java.util.function.Consumer;

import static isop.np.mq.bridge.JMSClosables.closeQuietly;
import static isop.np.mq.bridge.JMSSupport.queueName;

public class MessageSender implements Consumer<TextMessage> {

    private final QueueConnection connection;
    private final QueueSession session;
    private final QueueSender sender;

    public MessageSender() {
        try {
            QueueConnectionFactory factory = JMSSupport.createSenderConnectionFactory();
            connection = factory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName());
            sender = session.createSender(queue);
            connection.start();
        } catch (JMSException e) {
            throw new RuntimeException("Unable to create message receiver", e);
        }
    }

    public void routeInboundMessage(TextMessage inboundMessage) {
        try {
            IO.println("Route inbound message");
            sender.send(inboundMessage);
        } catch (JMSException e) {
            System.err.printf("[MQ Error] Unable to route inbound message: %s".formatted(e));
            throw new RuntimeException("[MQ Error] Unable to route inbound message", e);
        }
    }

    public void cleanup() {
        IO.println("MessageSender cleanup...");
        closeQuietly(sender, session, connection);
    }

    @Override
    public void accept(TextMessage textMessage) {
        routeInboundMessage(textMessage);
    }
}
