const { Server } = require('socket.io');
const io = new Server({});

io.on('connection', (socket) => {
    console.log(`[---------------------------------------------------------------------------------------------------]`);
    console.log(`[+] Recieved connection from game client at ${socket.handshake.time} from ${socket.handshake.address}`);
    console.log(`[+] Socket Connection ID: ${socket.client.conn.id}`);
    console.log(`[+] Authorization Token: ${socket.handshake.headers.authorization}`);
    console.log(`[+] URL: ${socket.handshake.url}`);
    console.log(`[+] Host: ${socket.handshake.headers.host}`);
});

io.listen(3000);
console.log(`[+] Port 3000 is now ready! Waiting for game-client requests`);