/*     */ package io.socket.engineio.client;
/*     */ import io.socket.emitter.Emitter;
/*     */ import io.socket.engineio.client.transports.Polling;
/*     */ import io.socket.engineio.client.transports.PollingXHR;
/*     */ import io.socket.engineio.client.transports.WebSocket;
/*     */ import io.socket.engineio.parser.Packet;
/*     */ import io.socket.thread.EventThread;
/*     */ import java.net.URI;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.WebSocket;
/*     */ import org.json.JSONException;
/*     */ 
/*     */ public class Socket extends Emitter {
/*     */   private static final String PROBE_ERROR = "probe error";
/*     */   public static final String EVENT_OPEN = "open";
/*     */   public static final String EVENT_CLOSE = "close";
/*     */   public static final String EVENT_MESSAGE = "message";
/*     */   public static final String EVENT_ERROR = "error";
/*     */   public static final String EVENT_UPGRADE_ERROR = "upgradeError";
/*     */   public static final String EVENT_FLUSH = "flush";
/*     */   public static final String EVENT_DRAIN = "drain";
/*     */   public static final String EVENT_HANDSHAKE = "handshake";
/*     */   public static final String EVENT_UPGRADING = "upgrading";
/*     */   public static final String EVENT_UPGRADE = "upgrade";
/*     */   public static final String EVENT_PACKET = "packet";
/*     */   public static final String EVENT_PACKET_CREATE = "packetCreate";
/*     */   public static final String EVENT_HEARTBEAT = "heartbeat";
/*     */   public static final String EVENT_DATA = "data";
/*  38 */   private static final Logger logger = Logger.getLogger(Socket.class.getName());
/*     */   public static final String EVENT_PING = "ping";
/*     */   public static final String EVENT_PONG = "pong";
/*     */   public static final String EVENT_TRANSPORT = "transport";
/*     */   public static final int PROTOCOL = 4;
/*     */   
/*  44 */   private enum ReadyState { OPENING, OPEN, CLOSING, CLOSED;
/*     */ 
/*     */     
/*     */     public String toString() {
/*  48 */       return super.toString().toLowerCase();
/*     */     } }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static boolean priorWebsocketSuccess = false;
/*     */ 
/*     */ 
/*     */   
/*     */   private static WebSocket.Factory defaultWebSocketFactory;
/*     */ 
/*     */ 
/*     */   
/*     */   private static Call.Factory defaultCallFactory;
/*     */ 
/*     */ 
/*     */   
/*     */   private static OkHttpClient defaultOkHttpClient;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean secure;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean upgrade;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean timestampRequests;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean upgrading;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean rememberUpgrade;
/*     */ 
/*     */   
/*     */   int port;
/*     */ 
/*     */   
/*     */   private int policyPort;
/*     */ 
/*     */   
/*     */   private int prevBufferLen;
/*     */ 
/*     */   
/*     */   private long pingInterval;
/*     */ 
/*     */   
/*     */   private long pingTimeout;
/*     */ 
/*     */   
/*     */   private String id;
/*     */ 
/*     */   
/*     */   String hostname;
/*     */ 
/*     */   
/*     */   private String path;
/*     */ 
/*     */   
/*     */   private String timestampParam;
/*     */ 
/*     */   
/*     */   private List<String> transports;
/*     */ 
/*     */   
/*     */   private Map<String, Transport.Options> transportOptions;
/*     */ 
/*     */   
/*     */   private List<String> upgrades;
/*     */ 
/*     */   
/*     */   private Map<String, String> query;
/*     */ 
/*     */   
/* 128 */   LinkedList<Packet> writeBuffer = new LinkedList<>();
/*     */   Transport transport;
/*     */   private Future pingTimeoutTimer;
/*     */   private WebSocket.Factory webSocketFactory;
/*     */   private Call.Factory callFactory;
/*     */   private final Map<String, List<String>> extraHeaders;
/*     */   private ReadyState readyState;
/*     */   private ScheduledExecutorService heartbeatScheduler;
/*     */   
/* 137 */   private final Emitter.Listener onHeartbeatAsListener = new Emitter.Listener()
/*     */     {
/*     */       public void call(Object... args) {
/* 140 */         Socket.this.onHeartbeat();
/*     */       }
/*     */     };
/*     */   
/*     */   public Socket() {
/* 145 */     this(new Options());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket(String uri) throws URISyntaxException {
/* 155 */     this(uri, (Options)null);
/*     */   }
/*     */   
/*     */   public Socket(URI uri) {
/* 159 */     this(uri, (Options)null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket(String uri, Options opts) throws URISyntaxException {
/* 170 */     this((uri == null) ? null : new URI(uri), opts);
/*     */   }
/*     */   
/*     */   public Socket(URI uri, Options opts) {
/* 174 */     this((uri == null) ? opts : Options.fromURI(uri, opts));
/*     */   }
/*     */   
/*     */   public Socket(Options opts) {
/* 178 */     if (opts.host != null) {
/* 179 */       String hostname = opts.host;
/* 180 */       boolean ipv6 = ((hostname.split(":")).length > 2);
/* 181 */       if (ipv6) {
/* 182 */         int start = hostname.indexOf('[');
/* 183 */         if (start != -1) hostname = hostname.substring(start + 1); 
/* 184 */         int end = hostname.lastIndexOf(']');
/* 185 */         if (end != -1) hostname = hostname.substring(0, end); 
/*     */       } 
/* 187 */       opts.hostname = hostname;
/*     */     } 
/*     */     
/* 190 */     this.secure = opts.secure;
/*     */     
/* 192 */     if (opts.port == -1)
/*     */     {
/* 194 */       opts.port = this.secure ? 443 : 80;
/*     */     }
/*     */     
/* 197 */     this.hostname = (opts.hostname != null) ? opts.hostname : "localhost";
/* 198 */     this.port = opts.port;
/* 199 */     this
/* 200 */       .query = (opts.query != null) ? ParseQS.decode(opts.query) : new HashMap<>();
/* 201 */     this.upgrade = opts.upgrade;
/* 202 */     this.path = ((opts.path != null) ? opts.path : "/engine.io").replaceAll("/$", "") + "/";
/* 203 */     this.timestampParam = (opts.timestampParam != null) ? opts.timestampParam : "t";
/* 204 */     this.timestampRequests = opts.timestampRequests;
/* 205 */     (new String[2])[0] = "polling"; (new String[2])[1] = "websocket"; this.transports = new ArrayList<>(Arrays.asList((opts.transports != null) ? opts.transports : new String[2]));
/*     */     
/* 207 */     this.transportOptions = (opts.transportOptions != null) ? opts.transportOptions : new HashMap<>();
/*     */     
/* 209 */     this.policyPort = (opts.policyPort != 0) ? opts.policyPort : 843;
/* 210 */     this.rememberUpgrade = opts.rememberUpgrade;
/* 211 */     this.callFactory = (opts.callFactory != null) ? opts.callFactory : defaultCallFactory;
/* 212 */     this.webSocketFactory = (opts.webSocketFactory != null) ? opts.webSocketFactory : defaultWebSocketFactory;
/* 213 */     if (this.callFactory == null) {
/* 214 */       if (defaultOkHttpClient == null) {
/* 215 */         defaultOkHttpClient = new OkHttpClient();
/*     */       }
/* 217 */       this.callFactory = (Call.Factory)defaultOkHttpClient;
/*     */     } 
/* 219 */     if (this.webSocketFactory == null) {
/* 220 */       if (defaultOkHttpClient == null) {
/* 221 */         defaultOkHttpClient = new OkHttpClient();
/*     */       }
/* 223 */       this.webSocketFactory = (WebSocket.Factory)defaultOkHttpClient;
/*     */     } 
/* 225 */     this.extraHeaders = opts.extraHeaders;
/*     */   }
/*     */   
/*     */   public static void setDefaultOkHttpWebSocketFactory(WebSocket.Factory factory) {
/* 229 */     defaultWebSocketFactory = factory;
/*     */   }
/*     */   
/*     */   public static void setDefaultOkHttpCallFactory(Call.Factory factory) {
/* 233 */     defaultCallFactory = factory;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket open() {
/* 242 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/*     */             String transportName;
/* 246 */             if (Socket.this.rememberUpgrade && Socket.priorWebsocketSuccess && Socket.this.transports.contains("websocket"))
/* 247 */             { transportName = "websocket"; }
/* 248 */             else { if (0 == Socket.this.transports.size()) {
/*     */                 
/* 250 */                 final Socket self = Socket.this;
/* 251 */                 EventThread.nextTick(new Runnable()
/*     */                     {
/*     */                       public void run() {
/* 254 */                         self.emit("error", new Object[] { new EngineIOException("No transports available") });
/*     */                       }
/*     */                     });
/*     */                 return;
/*     */               } 
/* 259 */               transportName = Socket.this.transports.get(0); }
/*     */             
/* 261 */             Socket.this.readyState = Socket.ReadyState.OPENING;
/* 262 */             Transport transport = Socket.this.createTransport(transportName);
/* 263 */             Socket.this.setTransport(transport);
/* 264 */             transport.open();
/*     */           }
/*     */         });
/* 267 */     return this;
/*     */   }
/*     */   private Transport createTransport(String name) {
/*     */     PollingXHR pollingXHR;
/* 271 */     if (logger.isLoggable(Level.FINE)) {
/* 272 */       logger.fine(String.format("creating transport '%s'", new Object[] { name }));
/*     */     }
/* 274 */     Map<String, String> query = new HashMap<>(this.query);
/*     */     
/* 276 */     query.put("EIO", String.valueOf(4));
/* 277 */     query.put("transport", name);
/* 278 */     if (this.id != null) {
/* 279 */       query.put("sid", this.id);
/*     */     }
/*     */ 
/*     */     
/* 283 */     Transport.Options options = this.transportOptions.get(name);
/*     */     
/* 285 */     Transport.Options opts = new Transport.Options();
/* 286 */     opts.query = query;
/* 287 */     opts.socket = this;
/*     */     
/* 289 */     opts.hostname = (options != null) ? options.hostname : this.hostname;
/* 290 */     opts.port = (options != null) ? options.port : this.port;
/* 291 */     opts.secure = (options != null) ? options.secure : this.secure;
/* 292 */     opts.path = (options != null) ? options.path : this.path;
/* 293 */     opts.timestampRequests = (options != null) ? options.timestampRequests : this.timestampRequests;
/* 294 */     opts.timestampParam = (options != null) ? options.timestampParam : this.timestampParam;
/* 295 */     opts.policyPort = (options != null) ? options.policyPort : this.policyPort;
/* 296 */     opts.callFactory = (options != null) ? options.callFactory : this.callFactory;
/* 297 */     opts.webSocketFactory = (options != null) ? options.webSocketFactory : this.webSocketFactory;
/* 298 */     opts.extraHeaders = this.extraHeaders;
/*     */ 
/*     */     
/* 301 */     if ("websocket".equals(name)) {
/* 302 */       WebSocket webSocket = new WebSocket(opts);
/* 303 */     } else if ("polling".equals(name)) {
/* 304 */       pollingXHR = new PollingXHR(opts);
/*     */     } else {
/* 306 */       throw new RuntimeException();
/*     */     } 
/*     */     
/* 309 */     emit("transport", new Object[] { pollingXHR });
/*     */     
/* 311 */     return (Transport)pollingXHR;
/*     */   }
/*     */   
/*     */   private void setTransport(Transport transport) {
/* 315 */     if (logger.isLoggable(Level.FINE)) {
/* 316 */       logger.fine(String.format("setting transport %s", new Object[] { transport.name }));
/*     */     }
/* 318 */     final Socket self = this;
/*     */     
/* 320 */     if (this.transport != null) {
/* 321 */       if (logger.isLoggable(Level.FINE)) {
/* 322 */         logger.fine(String.format("clearing existing transport %s", new Object[] { this.transport.name }));
/*     */       }
/* 324 */       this.transport.off();
/*     */     } 
/*     */     
/* 327 */     this.transport = transport;
/*     */     
/* 329 */     transport.on("drain", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/* 332 */             self.onDrain();
/*     */           }
/* 334 */         }).on("packet", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/* 337 */             self.onPacket((args.length > 0) ? (Packet)args[0] : null);
/*     */           }
/* 339 */         }).on("error", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/* 342 */             self.onError((args.length > 0) ? (Exception)args[0] : null);
/*     */           }
/* 344 */         }).on("close", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/* 347 */             self.onClose("transport close");
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private void probe(final String name) {
/* 353 */     if (logger.isLoggable(Level.FINE)) {
/* 354 */       logger.fine(String.format("probing transport '%s'", new Object[] { name }));
/*     */     }
/* 356 */     final Transport[] transport = { createTransport(name) };
/* 357 */     final boolean[] failed = { false };
/* 358 */     final Socket self = this;
/*     */     
/* 360 */     priorWebsocketSuccess = false;
/*     */     
/* 362 */     final Runnable[] cleanup = new Runnable[1];
/*     */     
/* 364 */     final Emitter.Listener onTransportOpen = new Emitter.Listener()
/*     */       {
/*     */         public void call(Object... args) {
/* 367 */           if (failed[0])
/*     */             return; 
/* 369 */           if (Socket.logger.isLoggable(Level.FINE)) {
/* 370 */             Socket.logger.fine(String.format("probe transport '%s' opened", new Object[] { this.val$name }));
/*     */           }
/* 372 */           Packet<String> packet = new Packet("ping", "probe");
/* 373 */           transport[0].send(new Packet[] { packet });
/* 374 */           transport[0].once("packet", new Emitter.Listener()
/*     */               {
/*     */                 public void call(Object... args) {
/* 377 */                   if (failed[0])
/*     */                     return; 
/* 379 */                   Packet msg = (Packet)args[0];
/* 380 */                   if ("pong".equals(msg.type) && "probe".equals(msg.data)) {
/* 381 */                     if (Socket.logger.isLoggable(Level.FINE)) {
/* 382 */                       Socket.logger.fine(String.format("probe transport '%s' pong", new Object[] { this.this$1.val$name }));
/*     */                     }
/* 384 */                     self.upgrading = true;
/* 385 */                     self.emit("upgrading", new Object[] { this.this$1.val$transport[0] });
/* 386 */                     if (null == transport[0])
/* 387 */                       return;  Socket.priorWebsocketSuccess = "websocket".equals((transport[0]).name);
/*     */                     
/* 389 */                     if (Socket.logger.isLoggable(Level.FINE)) {
/* 390 */                       Socket.logger.fine(String.format("pausing current transport '%s'", new Object[] { this.this$1.val$self.transport.name }));
/*     */                     }
/* 392 */                     ((Polling)self.transport).pause(new Runnable()
/*     */                         {
/*     */                           public void run() {
/* 395 */                             if (failed[0])
/* 396 */                               return;  if (Socket.ReadyState.CLOSED == self.readyState)
/*     */                               return; 
/* 398 */                             Socket.logger.fine("changing transport and sending upgrade packet");
/*     */                             
/* 400 */                             cleanup[0].run();
/*     */                             
/* 402 */                             self.setTransport(transport[0]);
/* 403 */                             Packet packet = new Packet("upgrade");
/* 404 */                             transport[0].send(new Packet[] { packet });
/* 405 */                             self.emit("upgrade", new Object[] { this.this$2.this$1.val$transport[0] });
/* 406 */                             transport[0] = null;
/* 407 */                             self.upgrading = false;
/* 408 */                             self.flush();
/*     */                           }
/*     */                         });
/*     */                   } else {
/* 412 */                     if (Socket.logger.isLoggable(Level.FINE)) {
/* 413 */                       Socket.logger.fine(String.format("probe transport '%s' failed", new Object[] { this.this$1.val$name }));
/*     */                     }
/* 415 */                     EngineIOException err = new EngineIOException("probe error");
/* 416 */                     err.transport = (transport[0]).name;
/* 417 */                     self.emit("upgradeError", new Object[] { err });
/*     */                   } 
/*     */                 }
/*     */               });
/*     */         }
/*     */       };
/*     */     
/* 424 */     final Emitter.Listener freezeTransport = new Emitter.Listener()
/*     */       {
/*     */         public void call(Object... args) {
/* 427 */           if (failed[0])
/*     */             return; 
/* 429 */           failed[0] = true;
/*     */           
/* 431 */           cleanup[0].run();
/*     */           
/* 433 */           transport[0].close();
/* 434 */           transport[0] = null;
/*     */         }
/*     */       };
/*     */ 
/*     */     
/* 439 */     final Emitter.Listener onerror = new Emitter.Listener() {
/*     */         public void call(Object... args) {
/*     */           EngineIOException error;
/* 442 */           Object err = args[0];
/*     */           
/* 444 */           if (err instanceof Exception) {
/* 445 */             error = new EngineIOException("probe error", (Exception)err);
/* 446 */           } else if (err instanceof String) {
/* 447 */             error = new EngineIOException("probe error: " + (String)err);
/*     */           } else {
/* 449 */             error = new EngineIOException("probe error");
/*     */           } 
/* 451 */           error.transport = (transport[0]).name;
/*     */           
/* 453 */           freezeTransport.call(new Object[0]);
/*     */           
/* 455 */           if (Socket.logger.isLoggable(Level.FINE)) {
/* 456 */             Socket.logger.fine(String.format("probe transport \"%s\" failed because of error: %s", new Object[] { this.val$name, err }));
/*     */           }
/*     */           
/* 459 */           self.emit("upgradeError", new Object[] { error });
/*     */         }
/*     */       };
/*     */     
/* 463 */     final Emitter.Listener onTransportClose = new Emitter.Listener()
/*     */       {
/*     */         public void call(Object... args) {
/* 466 */           onerror.call(new Object[] { "transport closed" });
/*     */         }
/*     */       };
/*     */ 
/*     */     
/* 471 */     final Emitter.Listener onclose = new Emitter.Listener()
/*     */       {
/*     */         public void call(Object... args) {
/* 474 */           onerror.call(new Object[] { "socket closed" });
/*     */         }
/*     */       };
/*     */ 
/*     */     
/* 479 */     final Emitter.Listener onupgrade = new Emitter.Listener()
/*     */       {
/*     */         public void call(Object... args) {
/* 482 */           Transport to = (Transport)args[0];
/* 483 */           if (transport[0] != null && !to.name.equals((transport[0]).name)) {
/* 484 */             if (Socket.logger.isLoggable(Level.FINE)) {
/* 485 */               Socket.logger.fine(String.format("'%s' works - aborting '%s'", new Object[] { to.name, (this.val$transport[0]).name }));
/*     */             }
/* 487 */             freezeTransport.call(new Object[0]);
/*     */           } 
/*     */         }
/*     */       };
/*     */     
/* 492 */     cleanup[0] = new Runnable()
/*     */       {
/*     */         public void run() {
/* 495 */           transport[0].off("open", onTransportOpen);
/* 496 */           transport[0].off("error", onerror);
/* 497 */           transport[0].off("close", onTransportClose);
/* 498 */           self.off("close", onclose);
/* 499 */           self.off("upgrading", onupgrade);
/*     */         }
/*     */       };
/*     */     
/* 503 */     transport[0].once("open", onTransportOpen);
/* 504 */     transport[0].once("error", onerror);
/* 505 */     transport[0].once("close", onTransportClose);
/*     */     
/* 507 */     once("close", onclose);
/* 508 */     once("upgrading", onupgrade);
/*     */     
/* 510 */     transport[0].open();
/*     */   }
/*     */   
/*     */   private void onOpen() {
/* 514 */     logger.fine("socket open");
/* 515 */     this.readyState = ReadyState.OPEN;
/* 516 */     priorWebsocketSuccess = "websocket".equals(this.transport.name);
/* 517 */     emit("open", new Object[0]);
/* 518 */     flush();
/*     */     
/* 520 */     if (this.readyState == ReadyState.OPEN && this.upgrade && this.transport instanceof Polling) {
/* 521 */       logger.fine("starting upgrade probes");
/* 522 */       for (String upgrade : this.upgrades) {
/* 523 */         probe(upgrade);
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   private void onPacket(Packet packet) {
/* 529 */     if (this.readyState == ReadyState.OPENING || this.readyState == ReadyState.OPEN || this.readyState == ReadyState.CLOSING) {
/*     */ 
/*     */       
/* 532 */       if (logger.isLoggable(Level.FINE)) {
/* 533 */         logger.fine(String.format("socket received: type '%s', data '%s'", new Object[] { packet.type, packet.data }));
/*     */       }
/*     */       
/* 536 */       emit("packet", new Object[] { packet });
/* 537 */       emit("heartbeat", new Object[0]);
/*     */       
/* 539 */       if ("open".equals(packet.type)) {
/*     */         try {
/* 541 */           onHandshake(new HandshakeData((String)packet.data));
/* 542 */         } catch (JSONException e) {
/* 543 */           emit("error", new Object[] { new EngineIOException((Throwable)e) });
/*     */         } 
/* 545 */       } else if ("ping".equals(packet.type)) {
/* 546 */         emit("ping", new Object[0]);
/* 547 */         EventThread.exec(new Runnable()
/*     */             {
/*     */               public void run() {
/* 550 */                 Socket.this.sendPacket("pong", (Runnable)null);
/*     */               }
/*     */             });
/* 553 */       } else if ("error".equals(packet.type)) {
/* 554 */         EngineIOException err = new EngineIOException("server error");
/* 555 */         err.code = packet.data;
/* 556 */         onError(err);
/* 557 */       } else if ("message".equals(packet.type)) {
/* 558 */         emit("data", new Object[] { packet.data });
/* 559 */         emit("message", new Object[] { packet.data });
/*     */       }
/*     */     
/* 562 */     } else if (logger.isLoggable(Level.FINE)) {
/* 563 */       logger.fine(String.format("packet received with socket readyState '%s'", new Object[] { this.readyState }));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void onHandshake(HandshakeData data) {
/* 569 */     emit("handshake", new Object[] { data });
/* 570 */     this.id = data.sid;
/* 571 */     this.transport.query.put("sid", data.sid);
/* 572 */     this.upgrades = filterUpgrades(Arrays.asList(data.upgrades));
/* 573 */     this.pingInterval = data.pingInterval;
/* 574 */     this.pingTimeout = data.pingTimeout;
/* 575 */     onOpen();
/*     */     
/* 577 */     if (ReadyState.CLOSED == this.readyState)
/* 578 */       return;  onHeartbeat();
/*     */     
/* 580 */     off("heartbeat", this.onHeartbeatAsListener);
/* 581 */     on("heartbeat", this.onHeartbeatAsListener);
/*     */   }
/*     */   
/*     */   private void onHeartbeat() {
/* 585 */     if (this.pingTimeoutTimer != null) {
/* 586 */       this.pingTimeoutTimer.cancel(false);
/*     */     }
/*     */     
/* 589 */     long timeout = this.pingInterval + this.pingTimeout;
/*     */     
/* 591 */     final Socket self = this;
/* 592 */     this.pingTimeoutTimer = getHeartbeatScheduler().schedule(new Runnable()
/*     */         {
/*     */           public void run() {
/* 595 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/* 598 */                     if (self.readyState == Socket.ReadyState.CLOSED)
/* 599 */                       return;  self.onClose("ping timeout");
/*     */                   }
/*     */                 },  );
/*     */           }
/*     */         },  timeout, TimeUnit.MILLISECONDS);
/*     */   }
/*     */   
/*     */   private void onDrain() {
/* 607 */     for (int i = 0; i < this.prevBufferLen; i++) {
/* 608 */       this.writeBuffer.poll();
/*     */     }
/*     */     
/* 611 */     this.prevBufferLen = 0;
/* 612 */     if (0 == this.writeBuffer.size()) {
/* 613 */       emit("drain", new Object[0]);
/*     */     } else {
/* 615 */       flush();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void flush() {
/* 620 */     if (this.readyState != ReadyState.CLOSED && this.transport.writable && !this.upgrading && this.writeBuffer
/* 621 */       .size() != 0) {
/* 622 */       if (logger.isLoggable(Level.FINE)) {
/* 623 */         logger.fine(String.format("flushing %d packets in socket", new Object[] { Integer.valueOf(this.writeBuffer.size()) }));
/*     */       }
/* 625 */       this.prevBufferLen = this.writeBuffer.size();
/* 626 */       this.transport.send(this.writeBuffer.<Packet>toArray(new Packet[this.writeBuffer.size()]));
/* 627 */       emit("flush", new Object[0]);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void write(String msg) {
/* 632 */     write(msg, (Runnable)null);
/*     */   }
/*     */   
/*     */   public void write(String msg, Runnable fn) {
/* 636 */     send(msg, fn);
/*     */   }
/*     */   
/*     */   public void write(byte[] msg) {
/* 640 */     write(msg, (Runnable)null);
/*     */   }
/*     */   
/*     */   public void write(byte[] msg, Runnable fn) {
/* 644 */     send(msg, fn);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void send(String msg) {
/* 653 */     send(msg, (Runnable)null);
/*     */   }
/*     */   
/*     */   public void send(byte[] msg) {
/* 657 */     send(msg, (Runnable)null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void send(final String msg, final Runnable fn) {
/* 667 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 670 */             Socket.this.sendPacket("message", msg, fn);
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   public void send(final byte[] msg, final Runnable fn) {
/* 676 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 679 */             Socket.this.sendPacket("message", msg, fn);
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private void sendPacket(String type, Runnable fn) {
/* 685 */     sendPacket(new Packet(type), fn);
/*     */   }
/*     */   
/*     */   private void sendPacket(String type, String data, Runnable fn) {
/* 689 */     Packet<String> packet = new Packet(type, data);
/* 690 */     sendPacket(packet, fn);
/*     */   }
/*     */   
/*     */   private void sendPacket(String type, byte[] data, Runnable fn) {
/* 694 */     Packet<byte[]> packet = new Packet(type, data);
/* 695 */     sendPacket(packet, fn);
/*     */   }
/*     */   
/*     */   private void sendPacket(Packet packet, final Runnable fn) {
/* 699 */     if (ReadyState.CLOSING == this.readyState || ReadyState.CLOSED == this.readyState) {
/*     */       return;
/*     */     }
/*     */     
/* 703 */     emit("packetCreate", new Object[] { packet });
/* 704 */     this.writeBuffer.offer(packet);
/* 705 */     if (null != fn) {
/* 706 */       once("flush", new Emitter.Listener()
/*     */           {
/*     */             public void call(Object... args) {
/* 709 */               fn.run();
/*     */             }
/*     */           });
/*     */     }
/* 713 */     flush();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket close() {
/* 722 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 725 */             if (Socket.this.readyState == Socket.ReadyState.OPENING || Socket.this.readyState == Socket.ReadyState.OPEN) {
/* 726 */               Socket.this.readyState = Socket.ReadyState.CLOSING;
/*     */               
/* 728 */               final Socket self = Socket.this;
/*     */               
/* 730 */               final Runnable close = new Runnable()
/*     */                 {
/*     */                   public void run() {
/* 733 */                     self.onClose("forced close");
/* 734 */                     Socket.logger.fine("socket closing - telling transport to close");
/* 735 */                     self.transport.close();
/*     */                   }
/*     */                 };
/*     */               
/* 739 */               final Emitter.Listener[] cleanupAndClose = new Emitter.Listener[1];
/* 740 */               cleanupAndClose[0] = new Emitter.Listener()
/*     */                 {
/*     */                   public void call(Object... args) {
/* 743 */                     self.off("upgrade", cleanupAndClose[0]);
/* 744 */                     self.off("upgradeError", cleanupAndClose[0]);
/* 745 */                     close.run();
/*     */                   }
/*     */                 };
/*     */               
/* 749 */               final Runnable waitForUpgrade = new Runnable()
/*     */                 {
/*     */                   public void run()
/*     */                   {
/* 753 */                     self.once("upgrade", cleanupAndClose[0]);
/* 754 */                     self.once("upgradeError", cleanupAndClose[0]);
/*     */                   }
/*     */                 };
/*     */               
/* 758 */               if (Socket.this.writeBuffer.size() > 0) {
/* 759 */                 Socket.this.once("drain", new Emitter.Listener()
/*     */                     {
/*     */                       public void call(Object... args) {
/* 762 */                         if (Socket.this.upgrading) {
/* 763 */                           waitForUpgrade.run();
/*     */                         } else {
/* 765 */                           close.run();
/*     */                         } 
/*     */                       }
/*     */                     });
/* 769 */               } else if (Socket.this.upgrading) {
/* 770 */                 waitForUpgrade.run();
/*     */               } else {
/* 772 */                 close.run();
/*     */               } 
/*     */             } 
/*     */           }
/*     */         });
/* 777 */     return this;
/*     */   }
/*     */   
/*     */   private void onError(Exception err) {
/* 781 */     if (logger.isLoggable(Level.FINE)) {
/* 782 */       logger.fine(String.format("socket error %s", new Object[] { err }));
/*     */     }
/* 784 */     priorWebsocketSuccess = false;
/* 785 */     emit("error", new Object[] { err });
/* 786 */     onClose("transport error", err);
/*     */   }
/*     */   
/*     */   private void onClose(String reason) {
/* 790 */     onClose(reason, (Exception)null);
/*     */   }
/*     */   
/*     */   private void onClose(String reason, Exception desc) {
/* 794 */     if (ReadyState.OPENING == this.readyState || ReadyState.OPEN == this.readyState || ReadyState.CLOSING == this.readyState) {
/* 795 */       if (logger.isLoggable(Level.FINE)) {
/* 796 */         logger.fine(String.format("socket close with reason: %s", new Object[] { reason }));
/*     */       }
/* 798 */       Socket self = this;
/*     */ 
/*     */       
/* 801 */       if (this.pingTimeoutTimer != null) {
/* 802 */         this.pingTimeoutTimer.cancel(false);
/*     */       }
/* 804 */       if (this.heartbeatScheduler != null) {
/* 805 */         this.heartbeatScheduler.shutdown();
/*     */       }
/*     */ 
/*     */       
/* 809 */       this.transport.off("close");
/*     */ 
/*     */       
/* 812 */       this.transport.close();
/*     */ 
/*     */       
/* 815 */       this.transport.off();
/*     */ 
/*     */       
/* 818 */       this.readyState = ReadyState.CLOSED;
/*     */ 
/*     */       
/* 821 */       this.id = null;
/*     */ 
/*     */       
/* 824 */       emit("close", new Object[] { reason, desc });
/*     */ 
/*     */ 
/*     */       
/* 828 */       self.writeBuffer.clear();
/* 829 */       self.prevBufferLen = 0;
/*     */     } 
/*     */   }
/*     */   
/*     */   List<String> filterUpgrades(List<String> upgrades) {
/* 834 */     List<String> filteredUpgrades = new ArrayList<>();
/* 835 */     for (String upgrade : upgrades) {
/* 836 */       if (this.transports.contains(upgrade)) {
/* 837 */         filteredUpgrades.add(upgrade);
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 842 */     return filteredUpgrades;
/*     */   }
/*     */   
/*     */   public String id() {
/* 846 */     return this.id;
/*     */   }
/*     */   
/*     */   private ScheduledExecutorService getHeartbeatScheduler() {
/* 850 */     if (this.heartbeatScheduler == null || this.heartbeatScheduler.isShutdown()) {
/* 851 */       this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
/*     */     }
/* 853 */     return this.heartbeatScheduler;
/*     */   }
/*     */ 
/*     */   
/*     */   public static class Options
/*     */     extends Transport.Options
/*     */   {
/*     */     public String[] transports;
/*     */     
/*     */     public boolean upgrade = true;
/*     */     
/*     */     public boolean rememberUpgrade;
/*     */     
/*     */     public String host;
/*     */     
/*     */     public String query;
/*     */     
/*     */     public Map<String, Transport.Options> transportOptions;
/*     */ 
/*     */     
/*     */     private static Options fromURI(URI uri, Options opts) {
/* 874 */       if (opts == null) {
/* 875 */         opts = new Options();
/*     */       }
/*     */       
/* 878 */       opts.host = uri.getHost();
/* 879 */       opts.secure = ("https".equals(uri.getScheme()) || "wss".equals(uri.getScheme()));
/* 880 */       opts.port = uri.getPort();
/*     */       
/* 882 */       String query = uri.getRawQuery();
/* 883 */       if (query != null) {
/* 884 */         opts.query = query;
/*     */       }
/*     */       
/* 887 */       return opts;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\Socket.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */