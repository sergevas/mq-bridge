package isop.np.mq.bridge;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.time.Instant;
import java.util.Optional;

public record WebEvent(String id, Instant timestamp, int payloadLength) {

    public static WebEvent toWebEvent(TextMessage textMessage) {
        try {
            return new WebEvent(textMessage.getJMSMessageID(),
                    Instant.ofEpochMilli(textMessage.getJMSTimestamp()),
                    Optional.ofNullable(textMessage.getText())
                            .map(String::length)
                            .orElse(-1));
        } catch (JMSException e) {
            throw new RuntimeException("Unable to create WebEvent", e);
        }
    }
}
