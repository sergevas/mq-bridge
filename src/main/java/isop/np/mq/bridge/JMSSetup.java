package isop.np.mq.bridge;

import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;

public class JMSSetup {
    public QueueConnectionFactory createConnectionFactory() throws JMSException {
        MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
        factory.setHostName("localhost");
        factory.setPort(1414);
        factory.setQueueManager("QM1");
        factory.setChannel("DEV.APP.SVRCONN");
        return factory;
    }
}
