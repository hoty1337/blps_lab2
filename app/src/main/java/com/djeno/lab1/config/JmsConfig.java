package com.djeno.lab1.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

@EnableJms
@Configuration
public class JmsConfig {

    @Bean
    public ActiveMQConnectionFactory activeMqConnectionFactory(
            @Value("${mq.url:tcp://activemq:61616}") String url,
            @Value("${mq.user:admin}") String user,
            @Value("${mq.pass:admin}") String pass) {
        return new ActiveMQConnectionFactory(user, pass, url);
    }

    @Primary
    @Bean(name = "jmsConnectionFactory")
    public ConnectionFactory connectionFactory(ActiveMQConnectionFactory delegate) {
        CachingConnectionFactory c = new CachingConnectionFactory(delegate);
        c.setSessionCacheSize(10);
        return c;
    }

    @Bean(name = "queueFactory")
    public DefaultJmsListenerContainerFactory queueFactory(@Qualifier("jmsConnectionFactory") ConnectionFactory cf) {
        DefaultJmsListenerContainerFactory f = new DefaultJmsListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setPubSubDomain(false);
        f.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return f;
    }
}
