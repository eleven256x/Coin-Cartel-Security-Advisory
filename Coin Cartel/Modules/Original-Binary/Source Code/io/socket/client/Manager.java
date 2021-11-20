/*     */ package io.socket.client;
/*     */ import io.socket.emitter.Emitter;
/*     */ import io.socket.engineio.client.Socket;
/*     */ import io.socket.parser.Packet;
/*     */ import io.socket.parser.Parser;
/*     */ import java.util.Timer;
/*     */ 
/*     */ public class Manager extends Emitter {
/*     */   public static final String EVENT_OPEN = "open";
/*     */   public static final String EVENT_CLOSE = "close";
/*     */   public static final String EVENT_PACKET = "packet";
/*     */   public static final String EVENT_ERROR = "error";
/*     */   public static final String EVENT_RECONNECT = "reconnect";
/*     */   public static final String EVENT_RECONNECT_ERROR = "reconnect_error";
/*     */   public static final String EVENT_RECONNECT_FAILED = "reconnect_failed";
/*     */   public static final String EVENT_RECONNECT_ATTEMPT = "reconnect_attempt";
/*     */   public static final String EVENT_TRANSPORT = "transport";
/*     */   static WebSocket.Factory defaultWebSocketFactory;
/*     */   static Call.Factory defaultCallFactory;
/*     */   ReadyState readyState;
/*     */   private boolean _reconnection;
/*     */   private boolean skipReconnect;
/*     */   private boolean reconnecting;
/*  24 */   private static final Logger logger = Logger.getLogger(Manager.class.getName()); private boolean encoding; private int _reconnectionAttempts; private long _reconnectionDelay; private long _reconnectionDelayMax; private double _randomizationFactor; private Backoff backoff; private long _timeout; private URI uri; private List<Packet> packetBuffer; private Queue<On.Handle> subs; private Options opts; Socket engine; private Parser.Encoder encoder; private Parser.Decoder decoder;
/*     */   ConcurrentHashMap<String, Socket> nsps;
/*     */   
/*  27 */   enum ReadyState { CLOSED, OPENING, OPEN; }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Manager() {
/*  92 */     this(null, null);
/*     */   }
/*     */   
/*     */   public Manager(URI uri) {
/*  96 */     this(uri, null);
/*     */   }
/*     */   
/*     */   public Manager(Options opts) {
/* 100 */     this(null, opts);
/*     */   }
/*     */   
/*     */   public Manager(URI uri, Options opts) {
/* 104 */     if (opts == null) {
/* 105 */       opts = new Options();
/*     */     }
/* 107 */     if (opts.path == null) {
/* 108 */       opts.path = "/socket.io";
/*     */     }
/* 110 */     if (opts.webSocketFactory == null) {
/* 111 */       opts.webSocketFactory = defaultWebSocketFactory;
/*     */     }
/* 113 */     if (opts.callFactory == null) {
/* 114 */       opts.callFactory = defaultCallFactory;
/*     */     }
/* 116 */     this.opts = opts;
/* 117 */     this.nsps = new ConcurrentHashMap<>();
/* 118 */     this.subs = new LinkedList<>();
/* 119 */     reconnection(opts.reconnection);
/* 120 */     reconnectionAttempts((opts.reconnectionAttempts != 0) ? opts.reconnectionAttempts : Integer.MAX_VALUE);
/* 121 */     reconnectionDelay((opts.reconnectionDelay != 0L) ? opts.reconnectionDelay : 1000L);
/* 122 */     reconnectionDelayMax((opts.reconnectionDelayMax != 0L) ? opts.reconnectionDelayMax : 5000L);
/* 123 */     randomizationFactor((opts.randomizationFactor != 0.0D) ? opts.randomizationFactor : 0.5D);
/* 124 */     this
/*     */ 
/*     */       
/* 127 */       .backoff = (new Backoff()).setMin(reconnectionDelay()).setMax(reconnectionDelayMax()).setJitter(randomizationFactor());
/* 128 */     timeout(opts.timeout);
/* 129 */     this.readyState = ReadyState.CLOSED;
/* 130 */     this.uri = uri;
/* 131 */     this.encoding = false;
/* 132 */     this.packetBuffer = new ArrayList<>();
/* 133 */     this.encoder = (opts.encoder != null) ? opts.encoder : (Parser.Encoder)new IOParser.Encoder();
/* 134 */     this.decoder = (opts.decoder != null) ? opts.decoder : (Parser.Decoder)new IOParser.Decoder();
/*     */   }
/*     */   
/*     */   public boolean reconnection() {
/* 138 */     return this._reconnection;
/*     */   }
/*     */   
/*     */   public Manager reconnection(boolean v) {
/* 142 */     this._reconnection = v;
/* 143 */     return this;
/*     */   }
/*     */   
/*     */   public boolean isReconnecting() {
/* 147 */     return this.reconnecting;
/*     */   }
/*     */   
/*     */   public int reconnectionAttempts() {
/* 151 */     return this._reconnectionAttempts;
/*     */   }
/*     */   
/*     */   public Manager reconnectionAttempts(int v) {
/* 155 */     this._reconnectionAttempts = v;
/* 156 */     return this;
/*     */   }
/*     */   
/*     */   public final long reconnectionDelay() {
/* 160 */     return this._reconnectionDelay;
/*     */   }
/*     */   
/*     */   public Manager reconnectionDelay(long v) {
/* 164 */     this._reconnectionDelay = v;
/* 165 */     if (this.backoff != null) {
/* 166 */       this.backoff.setMin(v);
/*     */     }
/* 168 */     return this;
/*     */   }
/*     */   
/*     */   public final double randomizationFactor() {
/* 172 */     return this._randomizationFactor;
/*     */   }
/*     */   
/*     */   public Manager randomizationFactor(double v) {
/* 176 */     this._randomizationFactor = v;
/* 177 */     if (this.backoff != null) {
/* 178 */       this.backoff.setJitter(v);
/*     */     }
/* 180 */     return this;
/*     */   }
/*     */   
/*     */   public final long reconnectionDelayMax() {
/* 184 */     return this._reconnectionDelayMax;
/*     */   }
/*     */   
/*     */   public Manager reconnectionDelayMax(long v) {
/* 188 */     this._reconnectionDelayMax = v;
/* 189 */     if (this.backoff != null) {
/* 190 */       this.backoff.setMax(v);
/*     */     }
/* 192 */     return this;
/*     */   }
/*     */   
/*     */   public long timeout() {
/* 196 */     return this._timeout;
/*     */   }
/*     */   
/*     */   public Manager timeout(long v) {
/* 200 */     this._timeout = v;
/* 201 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   private void maybeReconnectOnOpen() {
/* 206 */     if (!this.reconnecting && this._reconnection && this.backoff.getAttempts() == 0) {
/* 207 */       reconnect();
/*     */     }
/*     */   }
/*     */   
/*     */   public Manager open() {
/* 212 */     return open(null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Manager open(final OpenCallback fn) {
/* 222 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 225 */             if (Manager.logger.isLoggable(Level.FINE)) {
/* 226 */               Manager.logger.fine(String.format("readyState %s", new Object[] { this.this$0.readyState }));
/*     */             }
/* 228 */             if (Manager.this.readyState == Manager.ReadyState.OPEN || Manager.this.readyState == Manager.ReadyState.OPENING)
/*     */               return; 
/* 230 */             if (Manager.logger.isLoggable(Level.FINE)) {
/* 231 */               Manager.logger.fine(String.format("opening %s", new Object[] { Manager.access$100(this.this$0) }));
/*     */             }
/* 233 */             Manager.this.engine = new Manager.Engine(Manager.this.uri, Manager.this.opts);
/* 234 */             final Socket socket = Manager.this.engine;
/* 235 */             final Manager self = Manager.this;
/* 236 */             Manager.this.readyState = Manager.ReadyState.OPENING;
/* 237 */             Manager.this.skipReconnect = false;
/*     */ 
/*     */             
/* 240 */             socket.on("transport", new Emitter.Listener()
/*     */                 {
/*     */                   public void call(Object... args) {
/* 243 */                     self.emit("transport", args);
/*     */                   }
/*     */                 });
/*     */             
/* 247 */             final On.Handle openSub = On.on((Emitter)socket, "open", new Emitter.Listener()
/*     */                 {
/*     */                   public void call(Object... objects) {
/* 250 */                     self.onopen();
/* 251 */                     if (fn != null) fn.call(null);
/*     */                   
/*     */                   }
/*     */                 });
/* 255 */             On.Handle errorSub = On.on((Emitter)socket, "error", new Emitter.Listener()
/*     */                 {
/*     */                   public void call(Object... objects) {
/* 258 */                     Object data = (objects.length > 0) ? objects[0] : null;
/* 259 */                     Manager.logger.fine("connect_error");
/* 260 */                     self.cleanup();
/* 261 */                     self.readyState = Manager.ReadyState.CLOSED;
/* 262 */                     self.emit("error", new Object[] { data });
/* 263 */                     if (fn != null) {
/* 264 */                       Exception err = new SocketIOException("Connection error", (data instanceof Exception) ? (Exception)data : null);
/*     */                       
/* 266 */                       fn.call(err);
/*     */                     } else {
/*     */                       
/* 269 */                       self.maybeReconnectOnOpen();
/*     */                     } 
/*     */                   }
/*     */                 });
/*     */             
/* 274 */             if (Manager.this._timeout >= 0L) {
/* 275 */               final long timeout = Manager.this._timeout;
/* 276 */               Manager.logger.fine(String.format("connection attempt will timeout after %d", new Object[] { Long.valueOf(timeout) }));
/*     */               
/* 278 */               final Timer timer = new Timer();
/* 279 */               timer.schedule(new TimerTask()
/*     */                   {
/*     */                     public void run() {
/* 282 */                       EventThread.exec(new Runnable()
/*     */                           {
/*     */                             public void run() {
/* 285 */                               Manager.logger.fine(String.format("connect attempt timed out after %d", new Object[] { Long.valueOf(this.this$2.val$timeout) }));
/* 286 */                               openSub.destroy();
/* 287 */                               socket.close();
/* 288 */                               socket.emit("error", new Object[] { new SocketIOException("timeout") });
/*     */                             }
/*     */                           });
/*     */                     }
/*     */                   }timeout);
/*     */               
/* 294 */               Manager.this.subs.add(new On.Handle()
/*     */                   {
/*     */                     public void destroy() {
/* 297 */                       timer.cancel();
/*     */                     }
/*     */                   });
/*     */             } 
/*     */             
/* 302 */             Manager.this.subs.add(openSub);
/* 303 */             Manager.this.subs.add(errorSub);
/*     */             
/* 305 */             Manager.this.engine.open();
/*     */           }
/*     */         });
/* 308 */     return this;
/*     */   }
/*     */   
/*     */   private void onopen() {
/* 312 */     logger.fine("open");
/*     */     
/* 314 */     cleanup();
/*     */     
/* 316 */     this.readyState = ReadyState.OPEN;
/* 317 */     emit("open", new Object[0]);
/*     */     
/* 319 */     Socket socket = this.engine;
/* 320 */     this.subs.add(On.on((Emitter)socket, "data", new Emitter.Listener()
/*     */           {
/*     */             public void call(Object... objects) {
/* 323 */               Object data = objects[0];
/* 324 */               if (data instanceof String) {
/* 325 */                 Manager.this.ondata((String)data);
/* 326 */               } else if (data instanceof byte[]) {
/* 327 */                 Manager.this.ondata((byte[])data);
/*     */               } 
/*     */             }
/*     */           }));
/* 331 */     this.subs.add(On.on((Emitter)socket, "error", new Emitter.Listener()
/*     */           {
/*     */             public void call(Object... objects) {
/* 334 */               Manager.this.onerror((Exception)objects[0]);
/*     */             }
/*     */           }));
/* 337 */     this.subs.add(On.on((Emitter)socket, "close", new Emitter.Listener()
/*     */           {
/*     */             public void call(Object... objects) {
/* 340 */               Manager.this.onclose((String)objects[0]);
/*     */             }
/*     */           }));
/* 343 */     this.decoder.onDecoded(new Parser.Decoder.Callback()
/*     */         {
/*     */           public void call(Packet packet) {
/* 346 */             Manager.this.ondecoded(packet);
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private void ondata(String data) {
/*     */     try {
/* 353 */       this.decoder.add(data);
/* 354 */     } catch (DecodingException e) {
/* 355 */       onerror((Exception)e);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void ondata(byte[] data) {
/*     */     try {
/* 361 */       this.decoder.add(data);
/* 362 */     } catch (DecodingException e) {
/* 363 */       onerror((Exception)e);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void ondecoded(Packet packet) {
/* 368 */     emit("packet", new Object[] { packet });
/*     */   }
/*     */   
/*     */   private void onerror(Exception err) {
/* 372 */     logger.log(Level.FINE, "error", err);
/* 373 */     emit("error", new Object[] { err });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket socket(String nsp, Options opts) {
/* 384 */     synchronized (this.nsps) {
/* 385 */       Socket socket = this.nsps.get(nsp);
/* 386 */       if (socket == null) {
/* 387 */         socket = new Socket(this, nsp, opts);
/* 388 */         this.nsps.put(nsp, socket);
/*     */       } 
/* 390 */       return socket;
/*     */     } 
/*     */   }
/*     */   
/*     */   public Socket socket(String nsp) {
/* 395 */     return socket(nsp, (Options)null);
/*     */   }
/*     */   
/*     */   void destroy() {
/* 399 */     synchronized (this.nsps) {
/* 400 */       for (Socket socket : this.nsps.values()) {
/* 401 */         if (socket.isActive()) {
/* 402 */           logger.fine("socket is still active, skipping close");
/*     */           
/*     */           return;
/*     */         } 
/*     */       } 
/* 407 */       close();
/*     */     } 
/*     */   }
/*     */   
/*     */   void packet(Packet packet) {
/* 412 */     if (logger.isLoggable(Level.FINE)) {
/* 413 */       logger.fine(String.format("writing packet %s", new Object[] { packet }));
/*     */     }
/* 415 */     final Manager self = this;
/*     */     
/* 417 */     if (!self.encoding) {
/* 418 */       self.encoding = true;
/* 419 */       this.encoder.encode(packet, new Parser.Encoder.Callback()
/*     */           {
/*     */             public void call(Object[] encodedPackets) {
/* 422 */               for (Object packet : encodedPackets) {
/* 423 */                 if (packet instanceof String) {
/* 424 */                   self.engine.write((String)packet);
/* 425 */                 } else if (packet instanceof byte[]) {
/* 426 */                   self.engine.write((byte[])packet);
/*     */                 } 
/*     */               } 
/* 429 */               self.encoding = false;
/* 430 */               self.processPacketQueue();
/*     */             }
/*     */           });
/*     */     } else {
/* 434 */       self.packetBuffer.add(packet);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void processPacketQueue() {
/* 439 */     if (!this.packetBuffer.isEmpty() && !this.encoding) {
/* 440 */       Packet pack = this.packetBuffer.remove(0);
/* 441 */       packet(pack);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void cleanup() {
/* 446 */     logger.fine("cleanup");
/*     */     
/*     */     On.Handle sub;
/* 449 */     for (; (sub = this.subs.poll()) != null; sub.destroy());
/* 450 */     this.decoder.onDecoded(null);
/*     */     
/* 452 */     this.packetBuffer.clear();
/* 453 */     this.encoding = false;
/*     */     
/* 455 */     this.decoder.destroy();
/*     */   }
/*     */   
/*     */   void close() {
/* 459 */     logger.fine("disconnect");
/* 460 */     this.skipReconnect = true;
/* 461 */     this.reconnecting = false;
/* 462 */     if (this.readyState != ReadyState.OPEN)
/*     */     {
/*     */       
/* 465 */       cleanup();
/*     */     }
/* 467 */     this.backoff.reset();
/* 468 */     this.readyState = ReadyState.CLOSED;
/* 469 */     if (this.engine != null) {
/* 470 */       this.engine.close();
/*     */     }
/*     */   }
/*     */   
/*     */   private void onclose(String reason) {
/* 475 */     logger.fine("onclose");
/* 476 */     cleanup();
/* 477 */     this.backoff.reset();
/* 478 */     this.readyState = ReadyState.CLOSED;
/* 479 */     emit("close", new Object[] { reason });
/*     */     
/* 481 */     if (this._reconnection && !this.skipReconnect) {
/* 482 */       reconnect();
/*     */     }
/*     */   }
/*     */   
/*     */   private void reconnect() {
/* 487 */     if (this.reconnecting || this.skipReconnect)
/*     */       return; 
/* 489 */     final Manager self = this;
/*     */     
/* 491 */     if (this.backoff.getAttempts() >= this._reconnectionAttempts) {
/* 492 */       logger.fine("reconnect failed");
/* 493 */       this.backoff.reset();
/* 494 */       emit("reconnect_failed", new Object[0]);
/* 495 */       this.reconnecting = false;
/*     */     } else {
/* 497 */       long delay = this.backoff.duration();
/* 498 */       logger.fine(String.format("will wait %dms before reconnect attempt", new Object[] { Long.valueOf(delay) }));
/*     */       
/* 500 */       this.reconnecting = true;
/* 501 */       final Timer timer = new Timer();
/* 502 */       timer.schedule(new TimerTask()
/*     */           {
/*     */             public void run() {
/* 505 */               EventThread.exec(new Runnable()
/*     */                   {
/*     */                     public void run() {
/* 508 */                       if (self.skipReconnect)
/*     */                         return; 
/* 510 */                       Manager.logger.fine("attempting reconnect");
/* 511 */                       int attempts = self.backoff.getAttempts();
/* 512 */                       self.emit("reconnect_attempt", new Object[] { Integer.valueOf(attempts) });
/*     */ 
/*     */                       
/* 515 */                       if (self.skipReconnect)
/*     */                         return; 
/* 517 */                       self.open(new Manager.OpenCallback()
/*     */                           {
/*     */                             public void call(Exception err) {
/* 520 */                               if (err != null) {
/* 521 */                                 Manager.logger.fine("reconnect attempt error");
/* 522 */                                 self.reconnecting = false;
/* 523 */                                 self.reconnect();
/* 524 */                                 self.emit("reconnect_error", new Object[] { err });
/*     */                               } else {
/* 526 */                                 Manager.logger.fine("reconnect success");
/* 527 */                                 self.onreconnect();
/*     */                               } 
/*     */                             }
/*     */                           });
/*     */                     }
/*     */                   });
/*     */             }
/*     */           }delay);
/*     */       
/* 536 */       this.subs.add(new On.Handle()
/*     */           {
/*     */             public void destroy() {
/* 539 */               timer.cancel();
/*     */             }
/*     */           });
/*     */     } 
/*     */   }
/*     */   
/*     */   private void onreconnect() {
/* 546 */     int attempts = this.backoff.getAttempts();
/* 547 */     this.reconnecting = false;
/* 548 */     this.backoff.reset();
/* 549 */     emit("reconnect", new Object[] { Integer.valueOf(attempts) });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static class Engine
/*     */     extends Socket
/*     */   {
/*     */     Engine(URI uri, Socket.Options opts) {
/* 562 */       super(uri, opts);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static class Options
/*     */     extends Socket.Options
/*     */   {
/*     */     public boolean reconnection = true;
/*     */     
/*     */     public int reconnectionAttempts;
/*     */     
/*     */     public long reconnectionDelay;
/*     */     public long reconnectionDelayMax;
/*     */     public double randomizationFactor;
/*     */     public Parser.Encoder encoder;
/*     */     public Parser.Decoder decoder;
/*     */     public Map<String, String> auth;
/* 580 */     public long timeout = 20000L;
/*     */   }
/*     */   
/*     */   public static interface OpenCallback {
/*     */     void call(Exception param1Exception);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\client\Manager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */