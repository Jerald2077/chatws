let stompClient = null;

function connect() {
    const serverIp = document.getElementById('serverIp').value;
    const username = document.getElementById('username').value;

    const socket = new SockJS("http://" + serverIp + ":8080/chat");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame){
    setConnected(true);
    console.log('Connected: ' + frame);

    // Canal publico: lo reciben todos los conectados.
    stompClient.subscribe('/topic/public', function (message) {
        showMessage(JSON.parse(message.body));
    });

    // Cola privada propia: solo llega lo que alguien envie a este usuario.
    stompClient.subscribe('/queue/' + username, function (message) {
        showMessage(JSON.parse(message.body), true);
    });
}, function (error) {
    console.error('Error connecting to WebSocket:', error);
    setConnected(false);
});

    //conexion y subcripcion
}

function disconnect() {
    // Desconectar
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}


function setConnected(connected) {
    const status = document.getElementById('status');
    if (connected) {
        status.className = 'connected';
        status.textContent = 'Conectado';
    } else {
        status.className = 'disconnected';
        status.textContent = 'Desconectado';
    }
    document.getElementById('message').disabled = !connected;
    document.getElementById('destinatario').disabled = !connected;
}

function sendMessage() {
    const username = document.getElementById('username').value;
    const content = document.getElementById('message').value;

    if (content.trim() === '') return;

    stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
        tipo: 'CHAT',
        usuario: username,
        contenido: content,
        destinatario: null
    }));

     // Conexion

    document.getElementById('message').value = '';
}

function sendPrivate() {
    const username = document.getElementById('username').value;
    const destinatario = document.getElementById('destinatario').value;
    const content = document.getElementById('message').value;

    if (content.trim() === '' || destinatario.trim() === '') return;

    // Va a /chat.sendPrivate, no a /chat.sendMessage: el servidor lo publica en
    // /queue/{destinatario} en vez de difundirlo por /topic/public.
    stompClient.send("/app/chat.sendPrivate", {}, JSON.stringify({
        tipo: 'PRIVADO',
        usuario: username,
        contenido: content,
        destinatario: destinatario
    }));

    // El que envia no esta suscripto a la cola del otro, asi que se muestra local.
    showMessage({ usuario: username + ' -> ' + destinatario, contenido: content }, true);

    document.getElementById('message').value = '';
}

function showMessage(message, privado) {
    const messages = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = privado ? 'message private' : 'message';
    const etiqueta = privado ? ' [privado]' : '';
    messageDiv.innerHTML = '<strong>' + message.usuario + etiqueta + ':</strong> ' + message.contenido;
    messages.appendChild(messageDiv);
    messages.scrollTop = messages.scrollHeight;
}

// Allow sending message with Enter key
document.getElementById('message').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});
