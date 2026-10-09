# Chat WebSocket - Guía para Alumnos

Este proyecto implementa un sistema de chat en tiempo real utilizando **WebSocket** y el protocolo **STOMP** con Spring Boot. A continuación se explica el flujo completo de funcionamiento y los conceptos clave.

## 📝 Descripción del Proyecto

ChatWS es una aplicación de chat en tiempo real que permite a múltiples usuarios conectarse, enviar mensajes y recibir actualizaciones instantáneas sin necesidad de recargar la página. Utiliza una arquitectura basada en eventos donde los mensajes se propagan automáticamente a todos los clientes conectados.

**Tecnologías principales:**
- Spring Boot 4.1.1
- WebSocket para comunicación bidireccional
- STOMP como protocolo de mensajería
- SockJS para compatibilidad con navegadores antiguos
- Jackson para serialización JSON

---

## 🏗️ Arquitectura y Flujo

### Diagrama de Arquitectura

```
┌─────────────────┐         WebSocket          ┌──────────────────┐
│   Cliente Web   │ ◄────────────────────────► │   Servidor       │
│  (Navegador)    │     (ws://localhost:8080)  │   Spring Boot    │
└─────────────────┘                            └──────────────────┘
       │                                                 │
       │ 1. Conexión WebSocket                          │
       │ 2. Suscripción a /topic/public                 │
       │ 3. Envío a /app/chat.sendMessage               │
       │                                                 │
       │                                                 │
       │ 4. Recepción de mensajes                       │
       │    (broadcast desde /topic/public)             │
```

### Flujo Completo de un Mensaje

1. **Conexión:** El cliente web establece una conexión WebSocket con el servidor
2. **Suscripción:** El cliente se suscribe al canal `/topic/public` para recibir mensajes
3. **Envío:** El cliente envía un mensaje a `/app/chat.sendMessage`
4. **Procesamiento:** El `ChatController` recibe el mensaje
5. **Broadcast:** El mensaje se reenvía a todos los suscriptores de `/topic/public`
6. **Recepción:** Todos los clientes conectados reciben el mensaje simultáneamente

---

## 🐇 Migración a RabbitMQ (en progreso)

El proyecto ya tiene la **base preparada**, pero sigue arrancando con el broker en
memoria. El cambio se activa con una sola propiedad, sin tocar código.

### Los dos puertos (no confundirlos)

| Puerto | Protocolo | Lo usa | Propiedad |
|---|---|---|---|
| `5672` | AMQP | `RabbitTemplate`, `@RabbitListener` | `spring.rabbitmq.port` |
| `61613` | STOMP | `enableStompBrokerRelay()` | `app.broker.relay.port` |

El relay de Spring habla **STOMP**, no AMQP. Por eso hace falta el plugin
`rabbitmq_stomp` habilitado en el servidor; sin él, el puerto 61613 no existe.

### Activarlo

1. Levantar RabbitMQ con el plugin STOMP ya habilitado:

   ```bash
   docker compose up -d rabbitmq
   ```

   Consola web en http://localhost:15672 (`guest` / `guest`).

2. Cambiar la propiedad en `application.yaml`:

   ```yaml
   app:
     broker:
       relay:
         enabled: true
   ```

3. Arrancar la app. Si el relay conecta, el log muestra
   `"Started... StompBrokerRelay"` y deja de aparecer `SimpleBrokerMessageHandler`.

### Volver atrás

`app.broker.relay.enabled: false` y la app vuelve al broker en memoria.
No necesita que RabbitMQ esté corriendo.

### Lo que falta para la migración completa

- Declarar exchanges/colas duraderas en vez de confiar en `amq.topic`.
- Persistencia de historial de mensajes (hoy no se guarda nada).
- Credenciales fuera del YAML (variables de entorno o Spring Config).
- DLQ y TTL para mensajes no entregados.

