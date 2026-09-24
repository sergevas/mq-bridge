package isop.np.mq.bridge;

import javax.jms.*;

import static isop.np.mq.bridge.MqBridgeApp.QUEUE_NAME;

public class MessageReceiver {

    private final QueueConnection connection;
    private final QueueSession session;
    private final QueueReceiver receiver;

    public MessageReceiver() throws JMSException {
        QueueConnectionFactory factory = new JMSSetup().createConnectionFactory();
        connection = factory.createQueueConnection();
        session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);
        receiver = session.createReceiver(queue);
        connection.start();
    }

    public void receiveMessage() {
        try {
            var message = receiver.receive(1000);
            if (message instanceof TextMessage textMessage) {
                IO.println("Inbound message received: [%s]".formatted(textMessage));
            } else {
                IO.println(("Skip processing inbound message of type [%s]").formatted(message.getClass()));
            }
        } catch (JMSException e) {
            throw new RuntimeException("Unable to receive and process message", e);
        } finally {
            JMSClosables.close(receiver);
            JMSClosables.close(session);
            JMSClosables.close(connection);
        }
    }
}
