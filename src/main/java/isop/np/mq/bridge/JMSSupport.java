package isop.np.mq.bridge;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import java.util.Enumeration;

public class JMSSupport {

    public static final long POLLING_PERIOD = 1000L;
    public static final long RECONNECTION_PERIOD = 5000L;
    public static final String QUEUE_NAME = "DEV.QUEUE.1";

    public static QueueConnectionFactory createSenderConnectionFactory() throws JMSException {
        return createConnectionFactory(new MQConnectionProperties("localhost", 1414, "QM1", "DEV.APP.SVRCONN"));
    }

    public static QueueConnectionFactory createReceiverConnectionFactory() throws JMSException {
        return createConnectionFactory(new MQConnectionProperties("localhost", 1415, "QM2", "DEV.APP.SVRCONN"));
    }

    public static QueueConnectionFactory createConnectionFactory(MQConnectionProperties properties) throws JMSException {
        MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        factory.setHostName(properties.host());
        factory.setPort(properties.port());
        factory.setQueueManager(properties.mqm());
        factory.setChannel(properties.channel());
        return factory;
    }

    public static void copyProperties(Message source, Message target) throws JMSException {
        if (source == null || target == null) {
            throw new IllegalArgumentException("[JMS Error] Source and target messages must not be null");
        }
        // Enumerate all property names from the source message
        Enumeration<?> propertyNames = source.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            Object value = source.getObjectProperty(name);
            // Copy property to target message
            target.setObjectProperty(name, value);
        }
    }
}
