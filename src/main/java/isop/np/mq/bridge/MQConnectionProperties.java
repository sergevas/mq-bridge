package isop.np.mq.bridge;

public record MQConnectionProperties(String host, int port, String mqm, String channel) {
}
