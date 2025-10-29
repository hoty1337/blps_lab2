FROM eclipse-temurin:8-jre

ENV ACTIVEMQ_VERSION=5.8.0 \
    ACTIVEMQ_HOME=/opt/activemq

RUN set -eux; \
    curl -fsSL "https://archive.apache.org/dist/activemq/apache-activemq/${ACTIVEMQ_VERSION}/apache-activemq-${ACTIVEMQ_VERSION}-bin.tar.gz" \
    | tar -xz -C /opt && \
    mv /opt/apache-activemq-${ACTIVEMQ_VERSION} "${ACTIVEMQ_HOME}"

WORKDIR ${ACTIVEMQ_HOME}

# Порты: OpenWire, Web Console, XMPP
EXPOSE 61616 8161 61222

# Каталог для нашего внешнего activemq.xml
RUN mkdir -p /opt/override

# ВОЛЮМЫ: данные и логи (conf — НЕ добавляем!)
VOLUME ["${ACTIVEMQ_HOME}/data", "${ACTIVEMQ_HOME}/logs"]

CMD ["bin/activemq", "console"]
