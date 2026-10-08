package isop.np.mq.bridge;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import java.util.Enumeration;

import static java.lang.System.getProperty;

///
/// # IBM MQ connection properties
/// ## Message Receiver connection properties
/// - `rHost` - receiver hostname default: `localhost`
/// - `rPort` - receiver port default: `1415`
/// - `rMqm` - receiver Queue Manager name default: `QM2`
/// - `rCh` - receiver Server Channel name default: `DEV.APP.SVRCONN`
/// ## Message Sender connection properties
/// - `sHost` - sender hostname default: `localhost`
/// - `sPort` - sender port default: `1414`
/// - `sMqm` - sender Queue Manager name default: `QM1`
/// - `sCh` - sender Server Channel name default: `DEV.APP.SVRCONN`
/// - `queue` - bridged Queue name default: `DEV.QUEUE.1`
///
public class JMSSupport {

    public static final long RECONNECTION_PERIOD = 5000L;

    public static String queueName() {
        return getProperty("queue", "DEV.QUEUE.1");
    }

    public static QueueConnectionFactory createSenderConnectionFactory() throws JMSException {
        return createConnectionFactory(new MQConnectionProperties(
                getProperty("sHost", "localhost"),
                Integer.parseInt(getProperty("sPort", "1414")),
                getProperty("sMqm", "QM1"),
                getProperty("sCh", "DEV.APP.SVRCONN")));
    }

    public static QueueConnectionFactory createReceiverConnectionFactory() throws JMSException {
        return createConnectionFactory(new MQConnectionProperties(
                getProperty("rHost", "localhost"),
                Integer.parseInt(getProperty("rPort", "1415")),
                getProperty("rMqm", "QM2"),
                getProperty("rCh", "DEV.APP.SVRCONN")));
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
