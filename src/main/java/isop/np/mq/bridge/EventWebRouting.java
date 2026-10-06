package isop.np.mq.bridge;

import io.avaje.jex.Jex;

import javax.jms.TextMessage;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class EventWebRouting {

    private final MessageNotifier messageNotifier;

    public EventWebRouting(MessageNotifier messageNotifier) {
        this.messageNotifier = messageNotifier;
    }

    public void register(Jex app) {
        // 1. Главная HTML страница
        app.get("/", ctx -> ctx.html(renderIndexPage()));

        // 2. Server-Sent Events канал обновлений
        app.sse("/events-feed", client -> {
            // Используем AtomicReference, чтобы лямбда имела ссылку на саму себя
            final var sseBridgeRef = new AtomicReference<Consumer<TextMessage>>();

            // Создаем мост для трансляции
            Consumer<TextMessage> sseBridge = textMessage -> {
                try {
                    var webEvent = WebEvent.toWebEvent(textMessage);
                    IO.println("Created: %s".formatted(webEvent));
                    String htmlRow = renderTableRow(webEvent);
                    client.sendEvent("newEvent", htmlRow);
                } catch (Exception e) {
                    // Если отправить не удалось (клиент ушел), принудительно убираем его
                    System.err.printf("Unable to send SSE %s%n", e);
                }
            };
            // Чистим за собой, если клиент инициировал закрытие
//            client.onClose(() -> {
//                messageNotifier.removeListener(sseBridge);
//                IO.println("SSE Client successfully unregistered");
//            });
            // Регистрируем слушателя в сервисе
            messageNotifier.registerListener(sseBridge);
            if (client.terminated() || Thread.currentThread().isInterrupted()) {
                Optional.ofNullable(sseBridgeRef.get()).ifPresent(messageNotifier::removeListener);
            }
            // ВАЖНО ДЛЯ JEX: Блокируем виртуальный поток обработчика,
            // чтобы соединение оставалось открытым в ожидании событий!
            client.keepAlive();
//            try {
//                while (true) {
//                    IO.println("Ping!");
//                    client.sendComment("ping");
//                    Thread.sleep(15000);
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } catch (Exception e) {
//                // Сюда мы попадем, как только браузер разорвет соединение и sendComment упадет
//                IO.println("Browser connection lost: " + e.getMessage());
//            } finally {
//                // На всякий случай дублируем очистку, если onClose не сработал автоматически
//                messageNotifier.removeListener(sseBridge);
//            }
        });
    }

    private String renderIndexPage() {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <title>Event Stream</title>
                
                    <!-- 1. Базовый HTMX -->
                    <script src="https://unpkg.com/htmx.org@2.0.3/dist/htmx.min.js"></script>
                    <!-- 2. Официальное расширение SSE для HTMX 2.x -->
                    <script src="https://unpkg.com/htmx-ext-sse@2.2.2/sse.js"></script>
                
                    <style>
                        body { font-family: sans-serif; margin: 40px; background: #f8f9fa; }
                        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
                        table { width: 100%; border-collapse: collapse; }
                        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }
                        th { background-color: #f1f3f5; }
                        tr { animation: fadeIn 0.4s ease-in-out; }
                        @keyframes fadeIn { from { background: #fff9c4; } to { background: transparent; } }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Real-time Application Events</h2>
                        <!-- Активируем расширение SSE и подключаемся к потоку -->
                        <div hx-ext="sse" sse-connect="/events-feed">
                            <table>
                                <thead>
                                    <tr><th>JMSMessageID</th><th>JMSTimestamp</th><th>PayloadLength</th></tr>
                                </thead>
                                <!-- Слушаем событие newEvent и вставляем новые строки наверх -->
                                <tbody id="events-table-body" sse-swap="newEvent" hx-swap="afterbegin">
                                </tbody>
                            </table>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String renderTableRow(WebEvent event) {
        return """
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d</td>
                </tr>
                """.formatted(event.id(), event.timestamp(), event.payloadLength());
    }
}
