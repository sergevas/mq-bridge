package isop.np.mq.bridge;

import io.avaje.jex.Jex;

public class MqBridgeApp {

    static void main() {
        final var messageReceiver = new MessageReceiver();
        final var messageSender = new MessageSender();
        messageReceiver.registerListener(messageSender);
        messageReceiver.startMQListener();
        // Создаем сервис и веб-компонент
        var routing = new EventWebRouting(messageReceiver);
        // Создаем сервер Jex
        var app = Jex.create();
        // Передаем его для конфигурации путей
        routing.register(app);
        // Конфигурируем порт и запускаем
        app.port(8080).start().onShutdown(() -> {
            messageReceiver.cleanup();
            messageSender.cleanup();
        });
        IO.println("Application started at http://localhost:8080");
    }
}
