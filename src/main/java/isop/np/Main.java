package isop.np;

import io.avaje.jex.Jex;

public class Main {

    static void main() {
        // Создаем сервис и веб-компонент
        EventPublisher publisher = new EventPublisher();
        EventRouting routing = new EventRouting(publisher);

        // Создаем сервер Jex
        Jex app = Jex.create();

        // Передаем его для конфигурации путей
        routing.register(app);

        // Конфигурируем порт и запускаем
        app.port(8080).start();

        IO.println("Application started at http://localhost:8080");
    }
}