package isop.np.mq.bridge;

import io.avaje.jex.Jex;

import static java.util.Optional.ofNullable;

public class MqBridgeApp {

    static void main(String[] args) {
        var defReceiverConnProps = new MQConnectionProperties("localhost", 1415, "QM2", "DEV.APP.SVRCONN");
        var defSenderConnProps = new MQConnectionProperties("localhost", 1414, "QM1", "DEV.APP.SVRCONN");
        MQConnectionProperties rConnProps;
        MQConnectionProperties sConnProps;
        if (args.length == 0) {
            rConnProps = defReceiverConnProps;
            sConnProps = defSenderConnProps;
        } else {
            rConnProps = new MQConnectionProperties(
                    ofNullable(args[0]).orElse(defReceiverConnProps.host()),
                    ofNullable(args[1]).map(Integer::valueOf).orElse(defReceiverConnProps.port()),
                    ofNullable(args[2]).orElse(defReceiverConnProps.mqm()),
                    ofNullable(args[3]).orElse(defReceiverConnProps.channel()));
            sConnProps = new MQConnectionProperties(
                    ofNullable(args[4]).orElse(defSenderConnProps.host()),
                    ofNullable(args[5]).map(Integer::valueOf).orElse(defSenderConnProps.port()),
                    ofNullable(args[6]).orElse(defSenderConnProps.mqm()),
                    ofNullable(args[7]).orElse(defSenderConnProps.channel()));
        }
        IO.println("Message Receiver connection properties: %s".formatted(rConnProps));
        IO.println("Message Sender connection properties: %s".formatted(rConnProps));
        final var messageReceiver = new MessageReceiver(rConnProps);
        final var messageSender = new MessageSender(sConnProps);
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
