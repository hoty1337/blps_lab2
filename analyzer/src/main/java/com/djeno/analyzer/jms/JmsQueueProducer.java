package com.djeno.analyzer.jms;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

@Startup
@Singleton
@Slf4j
public class JmsQueueProducer {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Queue queue;

    @PostConstruct
    public void init() {
        try {
            ActiveMQConnectionFactory cf =
                    new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = cf.createConnection("admin", "admin");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            queue = session.createQueue("analysis.queue");

            producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            connection.start();

            log.info("[JMS] Producer initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendText(String text) throws JMSException {
        TextMessage msg = session.createTextMessage(text);
        producer.send(msg);
        log.info("[JMS] SENT: " + text);
    }

    @PreDestroy
    public void shutdown() {
        try { if (producer != null) producer.close(); } catch (Exception ignore) {}
        try { if (session != null) session.close(); } catch (Exception ignore) {}
        try { if (connection != null) connection.close(); } catch (Exception ignore) {}
    }
}
