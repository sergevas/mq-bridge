package isop.np.mq.bridge;

import javax.jms.*;

import static isop.np.mq.bridge.JMSClosables.close;
import static isop.np.mq.bridge.MqBridgeApp.QUEUE_NAME;

public class MessageSender {

    private final QueueConnection connection;
    private final QueueSession session;
    private final QueueSender sender;

    public MessageSender() throws JMSException {
        QueueConnectionFactory factory = new JMSSetup().createConnectionFactory();
        connection = factory.createQueueConnection();
        session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);
        sender = session.createSender(queue);
        connection.start();
    }

    public void sendMessage(String textMessage) {
        try {
            TextMessage message = session.createTextMessage();
            message.setText(textMessage);
            sender.send(message);
        } catch (JMSException e) {
            throw new RuntimeException("Unable to send message '%s'".formatted(textMessage), e);
        } finally {
            close(sender);
            close(session);
            close(connection);
        }
    }
}
