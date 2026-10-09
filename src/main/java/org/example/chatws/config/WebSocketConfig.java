package org.example.chatws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Prefijos que maneja el broker. /queue se agrega pensando en RabbitMQ. */
    private static final String[] DESTINOS = {"/topic", "/queue"};

    @Value("${app.broker.relay.enabled:false}")
    private boolean relayHabilitado;

    @Value("${app.broker.relay.host:localhost}")
    private String relayHost;

    @Value("${app.broker.relay.port:61613}")
    private int relayPort;

    @Value("${spring.rabbitmq.username:guest}")
    private String relayUsuario;

    @Value("${spring.rabbitmq.password:guest}")
    private String relayPassword;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if (relayHabilitado) {
            // Delega el broker a RabbitMQ via STOMP. Necesita el plugin
            // rabbitmq_stomp escuchando en relayPort.
            config.enableStompBrokerRelay(DESTINOS)
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort)
                    .setClientLogin(relayUsuario)
                    .setClientPasscode(relayPassword)
                    .setSystemLogin(relayUsuario)
                    .setSystemPasscode(relayPassword);
        } else {
            // Broker en memoria: suficiente para desarrollo single-instance.
            config.enableSimpleBroker(DESTINOS);
        }
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat").withSockJS();
    }
}
