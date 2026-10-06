package isop.np.mq.bridge;

public class JMSClosables {

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
