package isop.np.mq.bridge;

import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

public class JMSClosables {

    public static void close(QueueConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.printf("Unable to close QueueConnection: %s%n", e);
            }
        }
    }

    public static void close(QueueSession session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                System.err.printf("Unable to close QueueSession: %s%n", e);
            }
        }
    }

    public static void close(QueueSender sender) {
        if (sender != null) {
            try {
                sender.close();
            } catch (Exception e) {
                System.err.printf("Unable to close QueueSender: %s%n", e);
            }
        }
    }

    public static void close(QueueReceiver receiver) {
        if (receiver != null) {
            try {
                receiver.close();
            } catch (Exception e) {
                System.err.printf("Unable to close QueueReceiver: %s%n", e);
            }
        }
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    System.err.printf("Unable to close %s: %s%n", c, e);

                }
            }
        }
    }
}
