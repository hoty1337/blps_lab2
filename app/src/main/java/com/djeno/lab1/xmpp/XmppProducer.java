package com.djeno.lab1.xmpp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class XmppProducer {
    private XMPPConnection connection;
    @Value("${xmpp.host}") String host;
    @Value("${xmpp.domain}") String domain;
    @Value("${xmpp.port}") int port;
    @Value("${xmpp.user}") String user;
    @Value("${xmpp.nickname}") String nickname;
    @Value("${xmpp.password}") String password;

    @PostConstruct
    public void init() throws Exception {
        ConnectionConfiguration config = new ConnectionConfiguration(host, port);
        config.setSASLAuthenticationEnabled(false);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setCompressionEnabled(false);

        connection = new XMPPConnection(config);
        connection.connect();
        connection.login(user, password);
    }

    public void sendToTopic(String topic, String text) throws Exception {
        String roomJid = topic + "@conference." + domain;
        MultiUserChat muc = new MultiUserChat(connection, roomJid);

        if(!muc.isJoined()) {
            muc.join(nickname);
        }
        muc.sendMessage(text);
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        } catch (Exception ignored) {}
    }

}