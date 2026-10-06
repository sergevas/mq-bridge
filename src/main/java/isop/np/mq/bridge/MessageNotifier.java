package isop.np.mq.bridge;

import javax.jms.TextMessage;
import java.util.function.Consumer;

public interface MessageNotifier {

    void registerListener(Consumer<TextMessage> listener);

    void removeListener(Consumer<TextMessage> listener);

    void broadcast(TextMessage textMessage);
}
