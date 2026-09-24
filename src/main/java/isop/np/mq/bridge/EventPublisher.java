package isop.np.mq.bridge;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EventPublisher {

    public record SystemEvent(int id, String timestamp, String type, String details) {
    }

    private final Set<Consumer<SystemEvent>> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public EventPublisher() {
        startBackgroundJob();
    }

    public void registerListener(Consumer<SystemEvent> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Consumer<SystemEvent> listener) {
        this.listeners.remove(listener);
    }

    /**
     * Запуск фонового генератора в виртуальном потоке.
     * Отлично масштабируется в экосистеме Avaje Jex.
     */
    private void startBackgroundJob() {
        String[] eventTypes = {"INFO", "WARNING", "SUCCESS", "CRITICAL"};
        String[] descriptions = {"User logged in", "High CPU usage", "Backup completed", "Memory leak detected"};

        // Создаем и запускаем легковесный виртуальный поток
        Thread.startVirtualThread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int id = idGenerator.getAndIncrement();
                    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String type = eventTypes[id % eventTypes.length];
                    String details = descriptions[id % descriptions.length];

                    SystemEvent event = new SystemEvent(id, time, type, details);

                    // Оповещаем подписчиков
                    listeners.forEach(listener -> {
                        try {
                            listener.accept(event);
                        } catch (Exception e) {
                            listeners.remove(listener);
                        }
                    });

                    // Идеально для Virtual Threads: поток паркуется, не блокируя нативный carrier thread
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                IO.println("Фоновый генератор событий остановлен.");
            }
        });
    }
}
