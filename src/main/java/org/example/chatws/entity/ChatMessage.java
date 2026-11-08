package org.example.chatws.entity;

/**
 * Mensaje del chat.
 *
 * El campo destinatario solo se usa en los mensajes privados: indica a que usuario
 * hay que entregarselo. En los mensajes publicos viaja en null.
 */
public record ChatMessage(String tipo, String usuario, String contenido, String destinatario) {

}
