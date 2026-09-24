package isop.np;

import io.avaje.jex.Jex;

import java.util.function.Consumer;

public class EventRouting {
    private final EventPublisher eventPublisher;

    public EventRouting(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void register(Jex app) {
        // 1. Главная HTML страница
        app.get("/", ctx -> {
            ctx.html(renderIndexPage());
        });

        // 2. Server-Sent Events канал обновлений
        app.sse("/events-feed", client -> {
            // Создаем мост для трансляции
            Consumer<EventPublisher.SystemEvent> sseBridge = event -> {
                try {
                    String htmlRow = renderTableRow(event);
                    client.sendEvent("newEvent", htmlRow);
                } catch (Exception e) {
                    // Если отправить не удалось (клиент ушел), принудительно убираем его
                    System.out.println("Ошибка отправки, клиент будет удален.");
                }
            };

            // Регистрируем слушателя в сервисе
            eventPublisher.registerListener(sseBridge);

            // Чистим за собой, если клиент инициировал закрытие
            client.onClose(() -> eventPublisher.removeListener(sseBridge));

            // ВАЖНО ДЛЯ JEX: Блокируем виртуальный поток обработчика,
            // чтобы соединение оставалось открытым в ожидании событий!
            client.keepAlive();
        });
    }

    private String renderIndexPage() {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <title>Event Stream</title>
                
                    <!-- ОСТАВЛЯЕМ ТОЛЬКО ЭТИ ДВА КОРРЕКТНЫХ СКРИПТА -->
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
                                    <tr><th>ID</th><th>Time</th><th>Type</th><th>Details</th></tr>
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

    private String renderTableRow(EventPublisher.SystemEvent event) {
        return """
                <tr>
                    <td>%d</td>
                    <td>%s</td>
                    <td><strong>%s</strong></td>
                    <td>%s</td>
                </tr>
                """.formatted(event.id(), event.timestamp(), event.type(), event.details());
    }
}
