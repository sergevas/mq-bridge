package isop.np.mq.bridge;

import javax.jms.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static isop.np.mq.bridge.JMSClosables.closeQuietly;
import static isop.np.mq.bridge.JMSSupport.*;

public class MessageReceiver implements MessageNotifier {

    private final QueueConnection connection;
    private final QueueSession session;
    private final MessageConsumer messageConsumer;

    private final Set<Consumer<TextMessage>> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public MessageReceiver(MQConnectionProperties properties) {
        try {
            var factory = createConnectionFactory(properties);
            connection = factory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            messageConsumer = session.createConsumer(queue);
            messageConsumer.setMessageListener(this::receiveMessage);
        } catch (JMSException e) {
            throw new RuntimeException("Unable to create message receiver", e);
        }
    }

    public void registerListener(Consumer<TextMessage> listener) {
        IO.println("Add listener %s".formatted(listener));
        this.listeners.add(listener);
    }

    public void removeListener(Consumer<TextMessage> listener) {
        IO.println("Remove listener %s".formatted(listener));
        this.listeners.remove(listener);
    }

    public void receiveMessage(Message message) {
        if (message instanceof TextMessage textMessage) {
            IO.println("Inbound message received: [%s]".formatted(textMessage));
            broadcast(textMessage);
        } else {
            IO.println(("Skip processing inbound message of type [%s]").formatted(message.getClass()));
        }
    }

    public void startMQListener() {
        try {
            IO.println("[MQ] Establish connection %s".formatted(connection));
            connection.start();
            IO.println("[MQ] Connected successfully. Waiting for inbound message...");
        } catch (JMSException e) {
            IO.println("[MQ Error]: %s%n Reconnecting in %d sec...".formatted(e.getMessage(), RECONNECTION_PERIOD));
            cleanup();
            try {
                // Пауза перед реконнектом, если Docker/MQ упал
                Thread.sleep(RECONNECTION_PERIOD);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            cleanup();
        }
    }

    public void broadcast(TextMessage textMessage) {
        listeners.forEach(listener -> {
            try {
                IO.println("Notify listener: %s".formatted(listener));
                listener.accept(textMessage);
            } catch (Exception e) {
                System.err.printf("Error while broadcasting inbound message: %s", e);
                listeners.remove(listener);
            }
        });
    }

    public void cleanup() {
        IO.println("MessageReceiver cleanup...");
        closeQuietly(messageConsumer, session, connection);
    }
}
