package org.example.chatws.controller;

import org.example.chatws.entity.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage enviarMessage(ChatMessage mensaje) {
        System.out.println("Mensaje Recibido en Servidor:" + mensaje);
        return mensaje;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage agregarUsuario(ChatMessage mensaje) {
        return mensaje;
    }

    /**
     * Mensaje privado: en vez de difundirlo a todos, lo entrega a un solo usuario.
     *
     * No se puede usar @SendTo porque el destino no es fijo: depende de a quien se
     * le escriba. Por eso se usa SimpMessagingTemplate, que permite armar el destino
     * en tiempo de ejecucion y publicar en /queue/{destinatario}. Solo el cliente
     * suscripto a esa cola lo recibe.
     */
    @MessageMapping("/chat.sendPrivate")
    public void enviarMensajePrivado(ChatMessage mensaje) {
        System.out.println("Mensaje privado recibido en servidor: " + mensaje);
        messagingTemplate.convertAndSend("/queue/" + mensaje.destinatario(), mensaje);
    }
}
