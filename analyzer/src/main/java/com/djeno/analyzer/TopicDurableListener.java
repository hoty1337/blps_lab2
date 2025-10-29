package com.djeno.analyzer;

import com.djeno.analyzer.dto.JarUploadedEvent;
import com.djeno.analyzer.jms.JmsQueueProducer;
import com.djeno.api.SpotBugsConnectionFactory;
import com.djeno.model.AnalysisReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.jms.*;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Startup
@Singleton
@Slf4j
public class TopicDurableListener {

    @Resource(lookup = "java:/SpotBugsCF")
    private SpotBugsConnectionFactory connectionFactory;
    @Inject
    MinioClient minio;
    @Inject
    MailService mailService;
    @Inject
    JmsQueueProducer jmsQueueProducer;

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private final Jsonb jsonb = JsonbBuilder.create();

    @PostConstruct
    public void init() {
        try {
            log.info("[JMS] Starting topic listener...");

            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = connectionFactory.createConnection("admin", "admin");
            connection.setClientID("ee-consumer-1");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("analysis.request");
            consumer = session.createDurableSubscriber(topic, "ee-consumer-sub");

            consumer.setMessageListener(this::messageListener);

            connection.start();

            log.info("[JMS] Listener started and waiting for topic messages...");

        } catch (Exception e) {
            log.error("[JMS] Error initializing listener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            log.info("[JMS] Shutting down listener...");
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void messageListener(Message msg) {
        try {
            if (msg instanceof TextMessage text) {
                String json = text.getText();
                log.info(">>> GOT TOPIC MESSAGE: " + text.getText());

                JarUploadedEvent evt = jsonb.fromJson(json, JarUploadedEvent.class);

                GetObjectArgs.Builder gb = GetObjectArgs.builder()
                        .bucket(evt.bucket)
                        .object(evt.appId);

                byte[] jarBytes;
                try (GetObjectResponse obj = minio.getObject(gb.build())) {
                    jarBytes = readAllBytes(obj);
                }

                AnalysisReport report = connectionFactory.getConnection().analyze(jarBytes);
                log.info("Analysis finished: {}", report.getMessage());
                String message = report.getMessage();
                if (report.getMessage().contains("error")) {
                    message = "Ваше приложение не соответствует требованиям и будет удалено из магазина приложений.";
                    jmsQueueProducer.sendText(new ObjectMapper().writeValueAsString(evt));
                }
                try {
                    mailService.sendReport(
                            evt.email,
                            "Отчёт проверки вашего приложения готов",
                            "Здравствуйте, уважаемый " + evt.name + "!\n\nРезультат анализа:\n\n" + message
                    );
                } catch (Exception e) {
                    log.error("Не удалось отправить письмо: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8 * 1024);
        in.transferTo(out);
        return out.toByteArray();
    }
}
