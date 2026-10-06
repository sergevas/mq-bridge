package isop.np.mq.bridge;

import javax.jms.*;
import java.util.function.Consumer;

import static isop.np.mq.bridge.JMSClosables.closeQuietly;
import static isop.np.mq.bridge.JMSSupport.QUEUE_NAME;

public class MessageSender implements Consumer<TextMessage> {

    private final QueueConnection connection;
    private final QueueSession session;
    private final QueueSender sender;

    public MessageSender() {
        try {
            QueueConnectionFactory factory = JMSSupport.createSenderConnectionFactory();
            connection = factory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            sender = session.createSender(queue);
            connection.start();
        } catch (JMSException e) {
            throw new RuntimeException("Unable to create message receiver", e);
        }
    }

    public void routeInboundMessage(TextMessage inboundMessage) {
        try {
            TextMessage outboundMessage = session.createTextMessage();
            JMSSupport.copyProperties(inboundMessage, outboundMessage);
            outboundMessage.setText(inboundMessage.getText());
            IO.println("Send message: %s".formatted(outboundMessage));
            sender.send(outboundMessage);
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
