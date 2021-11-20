/*     */ package okhttp3.internal.ws;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.net.ProtocolException;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.util.ArrayDeque;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import java.util.Random;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.ScheduledThreadPoolExecutor;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.Callback;
/*     */ import okhttp3.EventListener;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.WebSocket;
/*     */ import okhttp3.WebSocketListener;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ import okio.BufferedSink;
/*     */ import okio.BufferedSource;
/*     */ import okio.ByteString;
/*     */ import okio.Okio;
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
/*     */ public final class RealWebSocket
/*     */   implements WebSocket, WebSocketReader.FrameCallback
/*     */ {
/*  57 */   private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);
/*     */ 
/*     */ 
/*     */   
/*     */   private static final long MAX_QUEUE_SIZE = 16777216L;
/*     */ 
/*     */ 
/*     */   
/*     */   private static final long CANCEL_AFTER_CLOSE_MILLIS = 60000L;
/*     */ 
/*     */ 
/*     */   
/*     */   private final Request originalRequest;
/*     */ 
/*     */ 
/*     */   
/*     */   final WebSocketListener listener;
/*     */ 
/*     */ 
/*     */   
/*     */   private final Random random;
/*     */ 
/*     */   
/*     */   private final long pingIntervalMillis;
/*     */ 
/*     */   
/*     */   private final String key;
/*     */ 
/*     */   
/*     */   private Call call;
/*     */ 
/*     */   
/*     */   private final Runnable writerRunnable;
/*     */ 
/*     */   
/*     */   private WebSocketReader reader;
/*     */ 
/*     */   
/*     */   private WebSocketWriter writer;
/*     */ 
/*     */   
/*     */   private ScheduledExecutorService executor;
/*     */ 
/*     */   
/*     */   private Streams streams;
/*     */ 
/*     */   
/* 104 */   private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
/*     */ 
/*     */   
/* 107 */   private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque();
/*     */ 
/*     */ 
/*     */   
/*     */   private long queueSize;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean enqueuedClose;
/*     */ 
/*     */ 
/*     */   
/*     */   private ScheduledFuture<?> cancelFuture;
/*     */ 
/*     */   
/* 122 */   private int receivedCloseCode = -1;
/*     */ 
/*     */   
/*     */   private String receivedCloseReason;
/*     */ 
/*     */   
/*     */   private boolean failed;
/*     */ 
/*     */   
/*     */   private int sentPingCount;
/*     */ 
/*     */   
/*     */   private int receivedPingCount;
/*     */ 
/*     */   
/*     */   private int receivedPongCount;
/*     */ 
/*     */   
/*     */   private boolean awaitingPong;
/*     */ 
/*     */   
/*     */   public RealWebSocket(Request request, WebSocketListener listener, Random random, long pingIntervalMillis) {
/* 144 */     if (!"GET".equals(request.method())) {
/* 145 */       throw new IllegalArgumentException("Request must be GET: " + request.method());
/*     */     }
/* 147 */     this.originalRequest = request;
/* 148 */     this.listener = listener;
/* 149 */     this.random = random;
/* 150 */     this.pingIntervalMillis = pingIntervalMillis;
/*     */     
/* 152 */     byte[] nonce = new byte[16];
/* 153 */     random.nextBytes(nonce);
/* 154 */     this.key = ByteString.of(nonce).base64();
/*     */     
/* 156 */     this.writerRunnable = new Runnable() {
/*     */         public void run() {
/*     */           try {
/* 159 */             while (RealWebSocket.this.writeOneFrame());
/*     */           }
/* 161 */           catch (IOException e) {
/* 162 */             RealWebSocket.this.failWebSocket(e, null);
/*     */           } 
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   public Request request() {
/* 169 */     return this.originalRequest;
/*     */   }
/*     */   
/*     */   public synchronized long queueSize() {
/* 173 */     return this.queueSize;
/*     */   }
/*     */   
/*     */   public void cancel() {
/* 177 */     this.call.cancel();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void connect(OkHttpClient client) {
/* 184 */     client = client.newBuilder().eventListener(EventListener.NONE).protocols(ONLY_HTTP1).build();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 190 */     final Request request = this.originalRequest.newBuilder().header("Upgrade", "websocket").header("Connection", "Upgrade").header("Sec-WebSocket-Key", this.key).header("Sec-WebSocket-Version", "13").build();
/* 191 */     this.call = Internal.instance.newWebSocketCall(client, request);
/* 192 */     this.call.timeout().clearTimeout();
/* 193 */     this.call.enqueue(new Callback() {
/*     */           public void onResponse(Call call, Response response) {
/*     */             try {
/* 196 */               RealWebSocket.this.checkResponse(response);
/* 197 */             } catch (ProtocolException e) {
/* 198 */               RealWebSocket.this.failWebSocket(e, response);
/* 199 */               Util.closeQuietly((Closeable)response);
/*     */               
/*     */               return;
/*     */             } 
/*     */             
/* 204 */             StreamAllocation streamAllocation = Internal.instance.streamAllocation(call);
/* 205 */             streamAllocation.noNewStreams();
/* 206 */             RealWebSocket.Streams streams = streamAllocation.connection().newWebSocketStreams(streamAllocation);
/*     */ 
/*     */             
/*     */             try {
/* 210 */               RealWebSocket.this.listener.onOpen(RealWebSocket.this, response);
/* 211 */               String name = "OkHttp WebSocket " + request.url().redact();
/* 212 */               RealWebSocket.this.initReaderAndWriter(name, streams);
/* 213 */               streamAllocation.connection().socket().setSoTimeout(0);
/* 214 */               RealWebSocket.this.loopReader();
/* 215 */             } catch (Exception e) {
/* 216 */               RealWebSocket.this.failWebSocket(e, null);
/*     */             } 
/*     */           }
/*     */           
/*     */           public void onFailure(Call call, IOException e) {
/* 221 */             RealWebSocket.this.failWebSocket(e, null);
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   void checkResponse(Response response) throws ProtocolException {
/* 227 */     if (response.code() != 101) {
/* 228 */       throw new ProtocolException("Expected HTTP 101 response but was '" + response
/* 229 */           .code() + " " + response.message() + "'");
/*     */     }
/*     */     
/* 232 */     String headerConnection = response.header("Connection");
/* 233 */     if (!"Upgrade".equalsIgnoreCase(headerConnection)) {
/* 234 */       throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '" + headerConnection + "'");
/*     */     }
/*     */ 
/*     */     
/* 238 */     String headerUpgrade = response.header("Upgrade");
/* 239 */     if (!"websocket".equalsIgnoreCase(headerUpgrade)) {
/* 240 */       throw new ProtocolException("Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + "'");
/*     */     }
/*     */ 
/*     */     
/* 244 */     String headerAccept = response.header("Sec-WebSocket-Accept");
/*     */     
/* 246 */     String acceptExpected = ByteString.encodeUtf8(this.key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").sha1().base64();
/* 247 */     if (!acceptExpected.equals(headerAccept)) {
/* 248 */       throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '" + acceptExpected + "' but was '" + headerAccept + "'");
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public void initReaderAndWriter(String name, Streams streams) throws IOException {
/* 254 */     synchronized (this) {
/* 255 */       this.streams = streams;
/* 256 */       this.writer = new WebSocketWriter(streams.client, streams.sink, this.random);
/* 257 */       this.executor = new ScheduledThreadPoolExecutor(1, Util.threadFactory(name, false));
/* 258 */       if (this.pingIntervalMillis != 0L) {
/* 259 */         this.executor.scheduleAtFixedRate(new PingRunnable(), this.pingIntervalMillis, this.pingIntervalMillis, TimeUnit.MILLISECONDS);
/*     */       }
/*     */       
/* 262 */       if (!this.messageAndCloseQueue.isEmpty()) {
/* 263 */         runWriter();
/*     */       }
/*     */     } 
/*     */     
/* 267 */     this.reader = new WebSocketReader(streams.client, streams.source, this);
/*     */   }
/*     */ 
/*     */   
/*     */   public void loopReader() throws IOException {
/* 272 */     while (this.receivedCloseCode == -1)
/*     */     {
/* 274 */       this.reader.processNextFrame();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   boolean processNextFrame() throws IOException {
/*     */     try {
/* 284 */       this.reader.processNextFrame();
/* 285 */       return (this.receivedCloseCode == -1);
/* 286 */     } catch (Exception e) {
/* 287 */       failWebSocket(e, null);
/* 288 */       return false;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
/* 296 */     this.executor.awaitTermination(timeout, timeUnit);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void tearDown() throws InterruptedException {
/* 303 */     if (this.cancelFuture != null) {
/* 304 */       this.cancelFuture.cancel(false);
/*     */     }
/* 306 */     this.executor.shutdown();
/* 307 */     this.executor.awaitTermination(10L, TimeUnit.SECONDS);
/*     */   }
/*     */   
/*     */   synchronized int sentPingCount() {
/* 311 */     return this.sentPingCount;
/*     */   }
/*     */   
/*     */   synchronized int receivedPingCount() {
/* 315 */     return this.receivedPingCount;
/*     */   }
/*     */   
/*     */   synchronized int receivedPongCount() {
/* 319 */     return this.receivedPongCount;
/*     */   }
/*     */   
/*     */   public void onReadMessage(String text) throws IOException {
/* 323 */     this.listener.onMessage(this, text);
/*     */   }
/*     */   
/*     */   public void onReadMessage(ByteString bytes) throws IOException {
/* 327 */     this.listener.onMessage(this, bytes);
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void onReadPing(ByteString payload) {
/* 332 */     if (this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty()))
/*     */       return; 
/* 334 */     this.pongQueue.add(payload);
/* 335 */     runWriter();
/* 336 */     this.receivedPingCount++;
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void onReadPong(ByteString buffer) {
/* 341 */     this.receivedPongCount++;
/* 342 */     this.awaitingPong = false;
/*     */   }
/*     */   
/*     */   public void onReadClose(int code, String reason) {
/* 346 */     if (code == -1) throw new IllegalArgumentException();
/*     */     
/* 348 */     Streams toClose = null;
/* 349 */     synchronized (this) {
/* 350 */       if (this.receivedCloseCode != -1) throw new IllegalStateException("already closed"); 
/* 351 */       this.receivedCloseCode = code;
/* 352 */       this.receivedCloseReason = reason;
/* 353 */       if (this.enqueuedClose && this.messageAndCloseQueue.isEmpty()) {
/* 354 */         toClose = this.streams;
/* 355 */         this.streams = null;
/* 356 */         if (this.cancelFuture != null) this.cancelFuture.cancel(false); 
/* 357 */         this.executor.shutdown();
/*     */       } 
/*     */     } 
/*     */     
/*     */     try {
/* 362 */       this.listener.onClosing(this, code, reason);
/*     */       
/* 364 */       if (toClose != null) {
/* 365 */         this.listener.onClosed(this, code, reason);
/*     */       }
/*     */     } finally {
/* 368 */       Util.closeQuietly(toClose);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean send(String text) {
/* 375 */     if (text == null) throw new NullPointerException("text == null"); 
/* 376 */     return send(ByteString.encodeUtf8(text), 1);
/*     */   }
/*     */   
/*     */   public boolean send(ByteString bytes) {
/* 380 */     if (bytes == null) throw new NullPointerException("bytes == null"); 
/* 381 */     return send(bytes, 2);
/*     */   }
/*     */ 
/*     */   
/*     */   private synchronized boolean send(ByteString data, int formatOpcode) {
/* 386 */     if (this.failed || this.enqueuedClose) return false;
/*     */ 
/*     */     
/* 389 */     if (this.queueSize + data.size() > 16777216L) {
/* 390 */       close(1001, null);
/* 391 */       return false;
/*     */     } 
/*     */ 
/*     */     
/* 395 */     this.queueSize += data.size();
/* 396 */     this.messageAndCloseQueue.add(new Message(formatOpcode, data));
/* 397 */     runWriter();
/* 398 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   synchronized boolean pong(ByteString payload) {
/* 403 */     if (this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty())) return false;
/*     */     
/* 405 */     this.pongQueue.add(payload);
/* 406 */     runWriter();
/* 407 */     return true;
/*     */   }
/*     */   
/*     */   public boolean close(int code, String reason) {
/* 411 */     return close(code, reason, 60000L);
/*     */   }
/*     */   
/*     */   synchronized boolean close(int code, String reason, long cancelAfterCloseMillis) {
/* 415 */     WebSocketProtocol.validateCloseCode(code);
/*     */     
/* 417 */     ByteString reasonBytes = null;
/* 418 */     if (reason != null) {
/* 419 */       reasonBytes = ByteString.encodeUtf8(reason);
/* 420 */       if (reasonBytes.size() > 123L) {
/* 421 */         throw new IllegalArgumentException("reason.size() > 123: " + reason);
/*     */       }
/*     */     } 
/*     */     
/* 425 */     if (this.failed || this.enqueuedClose) return false;
/*     */ 
/*     */     
/* 428 */     this.enqueuedClose = true;
/*     */ 
/*     */     
/* 431 */     this.messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
/* 432 */     runWriter();
/* 433 */     return true;
/*     */   }
/*     */   
/*     */   private void runWriter() {
/* 437 */     assert Thread.holdsLock(this);
/*     */     
/* 439 */     if (this.executor != null) {
/* 440 */       this.executor.execute(this.writerRunnable);
/*     */     }
/*     */   }
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
/*     */   boolean writeOneFrame() throws IOException {
/*     */     WebSocketWriter writer;
/*     */     ByteString pong;
/* 460 */     Object messageOrClose = null;
/* 461 */     int receivedCloseCode = -1;
/* 462 */     String receivedCloseReason = null;
/* 463 */     Streams streamsToClose = null;
/*     */     
/* 465 */     synchronized (this) {
/* 466 */       if (this.failed) {
/* 467 */         return false;
/*     */       }
/*     */       
/* 470 */       writer = this.writer;
/* 471 */       pong = this.pongQueue.poll();
/* 472 */       if (pong == null) {
/* 473 */         messageOrClose = this.messageAndCloseQueue.poll();
/* 474 */         if (messageOrClose instanceof Close) {
/* 475 */           receivedCloseCode = this.receivedCloseCode;
/* 476 */           receivedCloseReason = this.receivedCloseReason;
/* 477 */           if (receivedCloseCode != -1) {
/* 478 */             streamsToClose = this.streams;
/* 479 */             this.streams = null;
/* 480 */             this.executor.shutdown();
/*     */           } else {
/*     */             
/* 483 */             this.cancelFuture = this.executor.schedule(new CancelRunnable(), ((Close)messageOrClose).cancelAfterCloseMillis, TimeUnit.MILLISECONDS);
/*     */           }
/*     */         
/* 486 */         } else if (messageOrClose == null) {
/* 487 */           return false;
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/*     */     try {
/* 493 */       if (pong != null) {
/* 494 */         writer.writePong(pong);
/*     */       }
/* 496 */       else if (messageOrClose instanceof Message) {
/* 497 */         ByteString data = ((Message)messageOrClose).data;
/* 498 */         BufferedSink sink = Okio.buffer(writer.newMessageSink(((Message)messageOrClose).formatOpcode, data
/* 499 */               .size()));
/* 500 */         sink.write(data);
/* 501 */         sink.close();
/* 502 */         synchronized (this) {
/* 503 */           this.queueSize -= data.size();
/*     */         }
/*     */       
/* 506 */       } else if (messageOrClose instanceof Close) {
/* 507 */         Close close = (Close)messageOrClose;
/* 508 */         writer.writeClose(close.code, close.reason);
/*     */ 
/*     */         
/* 511 */         if (streamsToClose != null) {
/* 512 */           this.listener.onClosed(this, receivedCloseCode, receivedCloseReason);
/*     */         }
/*     */       } else {
/*     */         
/* 516 */         throw new AssertionError();
/*     */       } 
/*     */       
/* 519 */       return true;
/*     */     } finally {
/* 521 */       Util.closeQuietly(streamsToClose);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private final class PingRunnable
/*     */     implements Runnable
/*     */   {
/*     */     public void run() {
/* 530 */       RealWebSocket.this.writePingFrame();
/*     */     }
/*     */   }
/*     */   
/*     */   void writePingFrame() {
/*     */     WebSocketWriter writer;
/*     */     int failedPing;
/* 537 */     synchronized (this) {
/* 538 */       if (this.failed)
/* 539 */         return;  writer = this.writer;
/* 540 */       failedPing = this.awaitingPong ? this.sentPingCount : -1;
/* 541 */       this.sentPingCount++;
/* 542 */       this.awaitingPong = true;
/*     */     } 
/*     */     
/* 545 */     if (failedPing != -1) {
/* 546 */       failWebSocket(new SocketTimeoutException("sent ping but didn't receive pong within " + this.pingIntervalMillis + "ms (after " + (failedPing - 1) + " successful ping/pongs)"), null);
/*     */ 
/*     */       
/*     */       return;
/*     */     } 
/*     */     
/*     */     try {
/* 553 */       writer.writePing(ByteString.EMPTY);
/* 554 */     } catch (IOException e) {
/* 555 */       failWebSocket(e, null);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void failWebSocket(Exception e, @Nullable Response response) {
/*     */     Streams streamsToClose;
/* 561 */     synchronized (this) {
/* 562 */       if (this.failed)
/* 563 */         return;  this.failed = true;
/* 564 */       streamsToClose = this.streams;
/* 565 */       this.streams = null;
/* 566 */       if (this.cancelFuture != null) this.cancelFuture.cancel(false); 
/* 567 */       if (this.executor != null) this.executor.shutdown();
/*     */     
/*     */     } 
/*     */     try {
/* 571 */       this.listener.onFailure(this, e, response);
/*     */     } finally {
/* 573 */       Util.closeQuietly(streamsToClose);
/*     */     } 
/*     */   }
/*     */   
/*     */   static final class Message {
/*     */     final int formatOpcode;
/*     */     final ByteString data;
/*     */     
/*     */     Message(int formatOpcode, ByteString data) {
/* 582 */       this.formatOpcode = formatOpcode;
/* 583 */       this.data = data;
/*     */     }
/*     */   }
/*     */   
/*     */   static final class Close {
/*     */     final int code;
/*     */     final ByteString reason;
/*     */     final long cancelAfterCloseMillis;
/*     */     
/*     */     Close(int code, ByteString reason, long cancelAfterCloseMillis) {
/* 593 */       this.code = code;
/* 594 */       this.reason = reason;
/* 595 */       this.cancelAfterCloseMillis = cancelAfterCloseMillis;
/*     */     }
/*     */   }
/*     */   
/*     */   public static abstract class Streams implements Closeable {
/*     */     public final boolean client;
/*     */     public final BufferedSource source;
/*     */     public final BufferedSink sink;
/*     */     
/*     */     public Streams(boolean client, BufferedSource source, BufferedSink sink) {
/* 605 */       this.client = client;
/* 606 */       this.source = source;
/* 607 */       this.sink = sink;
/*     */     }
/*     */   }
/*     */   
/*     */   final class CancelRunnable implements Runnable {
/*     */     public void run() {
/* 613 */       RealWebSocket.this.cancel();
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\ws\RealWebSocket.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */