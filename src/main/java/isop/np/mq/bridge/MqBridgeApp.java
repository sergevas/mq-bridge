package isop.np.mq.bridge;

import io.avaje.jex.Jex;

public class MqBridgeApp {

    public static final String QUEUE_NAME = "DEV.QUEUE.1";

    static void main() {
        // Создаем сервис и веб-компонент
        var publisher = new EventPublisher();
        var routing = new EventRouting(publisher);
        // Создаем сервер Jex
        var app = Jex.create();
        // Передаем его для конфигурации путей
        routing.register(app);
        // Конфигурируем порт и запускаем
        app.port(8080).start();
        IO.println("Application started at http://localhost:8080");
    }
}