/*      */ package okhttp3.internal.http2;
/*      */ 
/*      */ import java.io.Closeable;
/*      */ import java.io.IOException;
/*      */ import java.io.InterruptedIOException;
/*      */ import java.net.InetSocketAddress;
/*      */ import java.net.Socket;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.LinkedHashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.concurrent.ExecutorService;
/*      */ import java.util.concurrent.LinkedBlockingQueue;
/*      */ import java.util.concurrent.RejectedExecutionException;
/*      */ import java.util.concurrent.ScheduledExecutorService;
/*      */ import java.util.concurrent.ScheduledThreadPoolExecutor;
/*      */ import java.util.concurrent.SynchronousQueue;
/*      */ import java.util.concurrent.ThreadPoolExecutor;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import okhttp3.Headers;
/*      */ import okhttp3.Protocol;
/*      */ import okhttp3.internal.NamedRunnable;
/*      */ import okhttp3.internal.Util;
/*      */ import okhttp3.internal.platform.Platform;
/*      */ import okio.Buffer;
/*      */ import okio.BufferedSink;
/*      */ import okio.BufferedSource;
/*      */ import okio.ByteString;
/*      */ import okio.Okio;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public final class Http2Connection
/*      */   implements Closeable
/*      */ {
/*      */   static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
/*      */   static final int INTERVAL_PING = 1;
/*      */   static final int DEGRADED_PING = 2;
/*      */   static final int AWAIT_PING = 3;
/*      */   static final long DEGRADED_PONG_TIMEOUT_NS = 1000000000L;
/*   86 */   private static final ExecutorService listenerExecutor = new ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), 
/*      */       
/*   88 */       Util.threadFactory("OkHttp Http2Connection", true));
/*      */ 
/*      */ 
/*      */   
/*      */   final boolean client;
/*      */ 
/*      */   
/*      */   final Listener listener;
/*      */ 
/*      */   
/*   98 */   final Map<Integer, Http2Stream> streams = new LinkedHashMap<>();
/*      */   
/*      */   final String hostname;
/*      */   
/*      */   int lastGoodStreamId;
/*      */   
/*      */   int nextStreamId;
/*      */   
/*      */   private boolean shutdown;
/*      */   
/*      */   private final ScheduledExecutorService writerExecutor;
/*      */   
/*      */   private final ExecutorService pushExecutor;
/*      */   
/*      */   final PushObserver pushObserver;
/*      */   
/*  114 */   private long intervalPingsSent = 0L;
/*  115 */   private long intervalPongsReceived = 0L;
/*  116 */   private long degradedPingsSent = 0L;
/*  117 */   private long degradedPongsReceived = 0L;
/*  118 */   private long awaitPingsSent = 0L;
/*  119 */   private long awaitPongsReceived = 0L;
/*      */ 
/*      */   
/*  122 */   private long degradedPongDeadlineNs = 0L;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  129 */   long unacknowledgedBytesRead = 0L;
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   long bytesLeftInWriteWindow;
/*      */ 
/*      */ 
/*      */   
/*  138 */   Settings okHttpSettings = new Settings();
/*      */ 
/*      */ 
/*      */   
/*  142 */   final Settings peerSettings = new Settings();
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   final Socket socket;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   final Http2Writer writer;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   final ReaderRunnable readerRunnable;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   final Set<Integer> currentPushRequests;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Protocol getProtocol() {
/*  192 */     return Protocol.HTTP_2;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized int openStreamCount() {
/*  199 */     return this.streams.size();
/*      */   }
/*      */   
/*      */   synchronized Http2Stream getStream(int id) {
/*  203 */     return this.streams.get(Integer.valueOf(id));
/*      */   }
/*      */   
/*      */   synchronized Http2Stream removeStream(int streamId) {
/*  207 */     Http2Stream stream = this.streams.remove(Integer.valueOf(streamId));
/*  208 */     notifyAll();
/*  209 */     return stream;
/*      */   }
/*      */   
/*      */   public synchronized int maxConcurrentStreams() {
/*  213 */     return this.peerSettings.getMaxConcurrentStreams(2147483647);
/*      */   }
/*      */   
/*      */   synchronized void updateConnectionFlowControl(long read) {
/*  217 */     this.unacknowledgedBytesRead += read;
/*  218 */     if (this.unacknowledgedBytesRead >= (this.okHttpSettings.getInitialWindowSize() / 2)) {
/*  219 */       writeWindowUpdateLater(0, this.unacknowledgedBytesRead);
/*  220 */       this.unacknowledgedBytesRead = 0L;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Http2Stream pushStream(int associatedStreamId, List<Header> requestHeaders, boolean out) throws IOException {
/*  233 */     if (this.client) throw new IllegalStateException("Client cannot push requests."); 
/*  234 */     return newStream(associatedStreamId, requestHeaders, out);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Http2Stream newStream(List<Header> requestHeaders, boolean out) throws IOException {
/*  243 */     return newStream(0, requestHeaders, out);
/*      */   }
/*      */   
/*      */   private Http2Stream newStream(int associatedStreamId, List<Header> requestHeaders, boolean out) throws IOException {
/*      */     Http2Stream stream;
/*  248 */     boolean flushHeaders, outFinished = !out;
/*  249 */     boolean inFinished = false;
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  254 */     synchronized (this.writer) {
/*  255 */       int streamId; synchronized (this) {
/*  256 */         if (this.nextStreamId > 1073741823) {
/*  257 */           shutdown(ErrorCode.REFUSED_STREAM);
/*      */         }
/*  259 */         if (this.shutdown) {
/*  260 */           throw new ConnectionShutdownException();
/*      */         }
/*  262 */         streamId = this.nextStreamId;
/*  263 */         this.nextStreamId += 2;
/*  264 */         stream = new Http2Stream(streamId, this, outFinished, inFinished, null);
/*  265 */         flushHeaders = (!out || this.bytesLeftInWriteWindow == 0L || stream.bytesLeftInWriteWindow == 0L);
/*  266 */         if (stream.isOpen()) {
/*  267 */           this.streams.put(Integer.valueOf(streamId), stream);
/*      */         }
/*      */       } 
/*  270 */       if (associatedStreamId == 0)
/*  271 */       { this.writer.synStream(outFinished, streamId, associatedStreamId, requestHeaders); }
/*  272 */       else { if (this.client) {
/*  273 */           throw new IllegalArgumentException("client streams shouldn't have associated stream IDs");
/*      */         }
/*  275 */         this.writer.pushPromise(associatedStreamId, streamId, requestHeaders); }
/*      */     
/*      */     } 
/*      */     
/*  279 */     if (flushHeaders) {
/*  280 */       this.writer.flush();
/*      */     }
/*      */     
/*  283 */     return stream;
/*      */   }
/*      */ 
/*      */   
/*      */   void writeSynReply(int streamId, boolean outFinished, List<Header> alternating) throws IOException {
/*  288 */     this.writer.synReply(outFinished, streamId, alternating);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void writeData(int streamId, boolean outFinished, Buffer buffer, long byteCount) throws IOException {
/*  305 */     if (byteCount == 0L) {
/*  306 */       this.writer.data(outFinished, streamId, buffer, 0);
/*      */       
/*      */       return;
/*      */     } 
/*  310 */     while (byteCount > 0L) {
/*      */       int toWrite;
/*  312 */       synchronized (this) { while (true) {
/*      */           try {
/*  314 */             if (this.bytesLeftInWriteWindow <= 0L) {
/*      */ 
/*      */               
/*  317 */               if (!this.streams.containsKey(Integer.valueOf(streamId))) {
/*  318 */                 throw new IOException("stream closed");
/*      */               }
/*  320 */               wait(); continue;
/*      */             } 
/*  322 */           } catch (InterruptedException e) {
/*  323 */             Thread.currentThread().interrupt();
/*  324 */             throw new InterruptedIOException();
/*      */           }  break;
/*      */         } 
/*  327 */         toWrite = (int)Math.min(byteCount, this.bytesLeftInWriteWindow);
/*  328 */         toWrite = Math.min(toWrite, this.writer.maxDataLength());
/*  329 */         this.bytesLeftInWriteWindow -= toWrite; }
/*      */ 
/*      */       
/*  332 */       byteCount -= toWrite;
/*  333 */       this.writer.data((outFinished && byteCount == 0L), streamId, buffer, toWrite);
/*      */     } 
/*      */   }
/*      */   
/*      */   void writeSynResetLater(final int streamId, final ErrorCode errorCode) {
/*      */     try {
/*  339 */       this.writerExecutor.execute((Runnable)new NamedRunnable("OkHttp %s stream %d", new Object[] { this.hostname, Integer.valueOf(streamId) }) {
/*      */             public void execute() {
/*      */               try {
/*  342 */                 Http2Connection.this.writeSynReset(streamId, errorCode);
/*  343 */               } catch (IOException e) {
/*  344 */                 Http2Connection.this.failConnection();
/*      */               } 
/*      */             }
/*      */           });
/*  348 */     } catch (RejectedExecutionException rejectedExecutionException) {}
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
/*  354 */     this.writer.rstStream(streamId, statusCode);
/*      */   }
/*      */   
/*      */   void writeWindowUpdateLater(final int streamId, final long unacknowledgedBytesRead) {
/*      */     try {
/*  359 */       this.writerExecutor.execute((Runnable)new NamedRunnable("OkHttp Window Update %s stream %d", new Object[] { this.hostname, 
/*  360 */               Integer.valueOf(streamId) }) {
/*      */             public void execute() {
/*      */               try {
/*  363 */                 Http2Connection.this.writer.windowUpdate(streamId, unacknowledgedBytesRead);
/*  364 */               } catch (IOException e) {
/*  365 */                 Http2Connection.this.failConnection();
/*      */               } 
/*      */             }
/*      */           });
/*  369 */     } catch (RejectedExecutionException rejectedExecutionException) {}
/*      */   }
/*      */   
/*      */   final class PingRunnable
/*      */     extends NamedRunnable
/*      */   {
/*      */     final boolean reply;
/*      */     final int payload1;
/*      */     final int payload2;
/*      */     
/*      */     PingRunnable(boolean reply, int payload1, int payload2) {
/*  380 */       super("OkHttp %s ping %08x%08x", new Object[] { this$0.hostname, Integer.valueOf(payload1), Integer.valueOf(payload2) });
/*  381 */       this.reply = reply;
/*  382 */       this.payload1 = payload1;
/*  383 */       this.payload2 = payload2;
/*      */     }
/*      */     
/*      */     public void execute() {
/*  387 */       Http2Connection.this.writePing(this.reply, this.payload1, this.payload2);
/*      */     }
/*      */   }
/*      */   
/*      */   final class IntervalPingRunnable extends NamedRunnable {
/*      */     IntervalPingRunnable() {
/*  393 */       super("OkHttp %s ping", new Object[] { this$0.hostname });
/*      */     }
/*      */     
/*      */     public void execute() {
/*      */       boolean failDueToMissingPong;
/*  398 */       synchronized (Http2Connection.this) {
/*  399 */         if (Http2Connection.this.intervalPongsReceived < Http2Connection.this.intervalPingsSent) {
/*  400 */           failDueToMissingPong = true;
/*      */         } else {
/*  402 */           Http2Connection.this.intervalPingsSent++;
/*  403 */           failDueToMissingPong = false;
/*      */         } 
/*      */       } 
/*  406 */       if (failDueToMissingPong) {
/*  407 */         Http2Connection.this.failConnection();
/*      */       } else {
/*  409 */         Http2Connection.this.writePing(false, 1, 0);
/*      */       } 
/*      */     }
/*      */   }
/*      */   
/*      */   void writePing(boolean reply, int payload1, int payload2) {
/*      */     try {
/*  416 */       this.writer.ping(reply, payload1, payload2);
/*  417 */     } catch (IOException e) {
/*  418 */       failConnection();
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   void writePingAndAwaitPong() throws InterruptedException {
/*  424 */     writePing();
/*  425 */     awaitPong();
/*      */   }
/*      */ 
/*      */   
/*      */   void writePing() {
/*  430 */     synchronized (this) {
/*  431 */       this.awaitPingsSent++;
/*      */     } 
/*  433 */     writePing(false, 3, 1330343787);
/*      */   }
/*      */ 
/*      */   
/*      */   synchronized void awaitPong() throws InterruptedException {
/*  438 */     while (this.awaitPongsReceived < this.awaitPingsSent) {
/*  439 */       wait();
/*      */     }
/*      */   }
/*      */   
/*      */   public void flush() throws IOException {
/*  444 */     this.writer.flush();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void shutdown(ErrorCode statusCode) throws IOException {
/*  453 */     synchronized (this.writer) {
/*      */       int lastGoodStreamId;
/*  455 */       synchronized (this) {
/*  456 */         if (this.shutdown) {
/*      */           return;
/*      */         }
/*  459 */         this.shutdown = true;
/*  460 */         lastGoodStreamId = this.lastGoodStreamId;
/*      */       } 
/*      */ 
/*      */       
/*  464 */       this.writer.goAway(lastGoodStreamId, statusCode, Util.EMPTY_BYTE_ARRAY);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void close() throws IOException {
/*  473 */     close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
/*      */   }
/*      */   
/*      */   void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
/*  477 */     assert !Thread.holdsLock(this);
/*  478 */     IOException thrown = null;
/*      */     try {
/*  480 */       shutdown(connectionCode);
/*  481 */     } catch (IOException e) {
/*  482 */       thrown = e;
/*      */     } 
/*      */     
/*  485 */     Http2Stream[] streamsToClose = null;
/*  486 */     synchronized (this) {
/*  487 */       if (!this.streams.isEmpty()) {
/*  488 */         streamsToClose = (Http2Stream[])this.streams.values().toArray((Object[])new Http2Stream[this.streams.size()]);
/*  489 */         this.streams.clear();
/*      */       } 
/*      */     } 
/*      */     
/*  493 */     if (streamsToClose != null) {
/*  494 */       for (Http2Stream stream : streamsToClose) {
/*      */         try {
/*  496 */           stream.close(streamCode);
/*  497 */         } catch (IOException e) {
/*  498 */           if (thrown != null) thrown = e;
/*      */         
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     try {
/*  505 */       this.writer.close();
/*  506 */     } catch (IOException e) {
/*  507 */       if (thrown == null) thrown = e;
/*      */     
/*      */     } 
/*      */     
/*      */     try {
/*  512 */       this.socket.close();
/*  513 */     } catch (IOException e) {
/*  514 */       thrown = e;
/*      */     } 
/*      */ 
/*      */     
/*  518 */     this.writerExecutor.shutdown();
/*  519 */     this.pushExecutor.shutdown();
/*      */     
/*  521 */     if (thrown != null) throw thrown; 
/*      */   }
/*      */   
/*      */   private void failConnection() {
/*      */     try {
/*  526 */       close(ErrorCode.PROTOCOL_ERROR, ErrorCode.PROTOCOL_ERROR);
/*  527 */     } catch (IOException iOException) {}
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void start() throws IOException {
/*  536 */     start(true);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   void start(boolean sendConnectionPreface) throws IOException {
/*  544 */     if (sendConnectionPreface) {
/*  545 */       this.writer.connectionPreface();
/*  546 */       this.writer.settings(this.okHttpSettings);
/*  547 */       int windowSize = this.okHttpSettings.getInitialWindowSize();
/*  548 */       if (windowSize != 65535) {
/*  549 */         this.writer.windowUpdate(0, (windowSize - 65535));
/*      */       }
/*      */     } 
/*  552 */     (new Thread((Runnable)this.readerRunnable)).start();
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSettings(Settings settings) throws IOException {
/*  557 */     synchronized (this.writer) {
/*  558 */       synchronized (this) {
/*  559 */         if (this.shutdown) {
/*  560 */           throw new ConnectionShutdownException();
/*      */         }
/*  562 */         this.okHttpSettings.merge(settings);
/*      */       } 
/*  564 */       this.writer.settings(settings);
/*      */     } 
/*      */   }
/*      */   
/*      */   public synchronized boolean isHealthy(long nowNs) {
/*  569 */     if (this.shutdown) return false;
/*      */ 
/*      */     
/*  572 */     if (this.degradedPongsReceived < this.degradedPingsSent && nowNs >= this.degradedPongDeadlineNs) return false;
/*      */     
/*  574 */     return true;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   void sendDegradedPingLater() {
/*  593 */     synchronized (this) {
/*  594 */       if (this.degradedPongsReceived < this.degradedPingsSent)
/*  595 */         return;  this.degradedPingsSent++;
/*  596 */       this.degradedPongDeadlineNs = System.nanoTime() + 1000000000L;
/*      */     } 
/*      */     try {
/*  599 */       this.writerExecutor.execute((Runnable)new NamedRunnable("OkHttp %s ping", new Object[] { this.hostname }) {
/*      */             public void execute() {
/*  601 */               Http2Connection.this.writePing(false, 2, 0);
/*      */             }
/*      */           });
/*  604 */     } catch (RejectedExecutionException rejectedExecutionException) {}
/*      */   }
/*      */ 
/*      */   
/*      */   public static class Builder
/*      */   {
/*      */     Socket socket;
/*      */     String hostname;
/*      */     BufferedSource source;
/*      */     BufferedSink sink;
/*  614 */     Http2Connection.Listener listener = Http2Connection.Listener.REFUSE_INCOMING_STREAMS;
/*  615 */     PushObserver pushObserver = PushObserver.CANCEL;
/*      */ 
/*      */     
/*      */     boolean client;
/*      */     
/*      */     int pingIntervalMillis;
/*      */ 
/*      */     
/*      */     public Builder(boolean client) {
/*  624 */       this.client = client;
/*      */     }
/*      */     
/*      */     public Builder socket(Socket socket) throws IOException {
/*  628 */       return socket(socket, ((InetSocketAddress)socket.getRemoteSocketAddress()).getHostName(), 
/*  629 */           Okio.buffer(Okio.source(socket)), Okio.buffer(Okio.sink(socket)));
/*      */     }
/*      */ 
/*      */     
/*      */     public Builder socket(Socket socket, String hostname, BufferedSource source, BufferedSink sink) {
/*  634 */       this.socket = socket;
/*  635 */       this.hostname = hostname;
/*  636 */       this.source = source;
/*  637 */       this.sink = sink;
/*  638 */       return this;
/*      */     }
/*      */     
/*      */     public Builder listener(Http2Connection.Listener listener) {
/*  642 */       this.listener = listener;
/*  643 */       return this;
/*      */     }
/*      */     
/*      */     public Builder pushObserver(PushObserver pushObserver) {
/*  647 */       this.pushObserver = pushObserver;
/*  648 */       return this;
/*      */     }
/*      */     
/*      */     public Builder pingIntervalMillis(int pingIntervalMillis) {
/*  652 */       this.pingIntervalMillis = pingIntervalMillis;
/*  653 */       return this;
/*      */     }
/*      */     
/*      */     public Http2Connection build() {
/*  657 */       return new Http2Connection(this);
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   class ReaderRunnable
/*      */     extends NamedRunnable
/*      */     implements Http2Reader.Handler
/*      */   {
/*      */     final Http2Reader reader;
/*      */     
/*      */     ReaderRunnable(Http2Reader reader) {
/*  669 */       super("OkHttp %s", new Object[] { this$0.hostname });
/*  670 */       this.reader = reader;
/*      */     }
/*      */     
/*      */     protected void execute() {
/*  674 */       ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
/*  675 */       ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
/*      */       try {
/*  677 */         this.reader.readConnectionPreface(this);
/*  678 */         while (this.reader.nextFrame(false, this));
/*      */         
/*  680 */         connectionErrorCode = ErrorCode.NO_ERROR;
/*  681 */         streamErrorCode = ErrorCode.CANCEL;
/*  682 */       } catch (IOException e) {
/*  683 */         connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
/*  684 */         streamErrorCode = ErrorCode.PROTOCOL_ERROR;
/*      */       } finally {
/*      */         try {
/*  687 */           Http2Connection.this.close(connectionErrorCode, streamErrorCode);
/*  688 */         } catch (IOException iOException) {}
/*      */         
/*  690 */         Util.closeQuietly(this.reader);
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     public void data(boolean inFinished, int streamId, BufferedSource source, int length) throws IOException {
/*  696 */       if (Http2Connection.this.pushedStream(streamId)) {
/*  697 */         Http2Connection.this.pushDataLater(streamId, source, length, inFinished);
/*      */         return;
/*      */       } 
/*  700 */       Http2Stream dataStream = Http2Connection.this.getStream(streamId);
/*  701 */       if (dataStream == null) {
/*  702 */         Http2Connection.this.writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
/*  703 */         Http2Connection.this.updateConnectionFlowControl(length);
/*  704 */         source.skip(length);
/*      */         return;
/*      */       } 
/*  707 */       dataStream.receiveData(source, length);
/*  708 */       if (inFinished) {
/*  709 */         dataStream.receiveFin();
/*      */       }
/*      */     }
/*      */     
/*      */     public void headers(boolean inFinished, int streamId, int associatedStreamId, List<Header> headerBlock) {
/*      */       Http2Stream stream;
/*  715 */       if (Http2Connection.this.pushedStream(streamId)) {
/*  716 */         Http2Connection.this.pushHeadersLater(streamId, headerBlock, inFinished);
/*      */         
/*      */         return;
/*      */       } 
/*  720 */       synchronized (Http2Connection.this) {
/*  721 */         stream = Http2Connection.this.getStream(streamId);
/*      */         
/*  723 */         if (stream == null) {
/*      */           
/*  725 */           if (Http2Connection.this.shutdown) {
/*      */             return;
/*      */           }
/*  728 */           if (streamId <= Http2Connection.this.lastGoodStreamId) {
/*      */             return;
/*      */           }
/*  731 */           if (streamId % 2 == Http2Connection.this.nextStreamId % 2) {
/*      */             return;
/*      */           }
/*  734 */           Headers headers = Util.toHeaders(headerBlock);
/*  735 */           final Http2Stream newStream = new Http2Stream(streamId, Http2Connection.this, false, inFinished, headers);
/*      */           
/*  737 */           Http2Connection.this.lastGoodStreamId = streamId;
/*  738 */           Http2Connection.this.streams.put(Integer.valueOf(streamId), newStream);
/*  739 */           Http2Connection.listenerExecutor.execute((Runnable)new NamedRunnable("OkHttp %s stream %d", new Object[] { this.this$0.hostname, Integer.valueOf(streamId) }) {
/*      */                 public void execute() {
/*      */                   try {
/*  742 */                     Http2Connection.this.listener.onStream(newStream);
/*  743 */                   } catch (IOException e) {
/*  744 */                     Platform.get().log(4, "Http2Connection.Listener failure for " + Http2Connection.this.hostname, e);
/*      */                     try {
/*  746 */                       newStream.close(ErrorCode.PROTOCOL_ERROR);
/*  747 */                     } catch (IOException iOException) {}
/*      */                   } 
/*      */                 }
/*      */               });
/*      */ 
/*      */           
/*      */           return;
/*      */         } 
/*      */       } 
/*      */       
/*  757 */       stream.receiveHeaders(headerBlock);
/*  758 */       if (inFinished) stream.receiveFin(); 
/*      */     }
/*      */     
/*      */     public void rstStream(int streamId, ErrorCode errorCode) {
/*  762 */       if (Http2Connection.this.pushedStream(streamId)) {
/*  763 */         Http2Connection.this.pushResetLater(streamId, errorCode);
/*      */         return;
/*      */       } 
/*  766 */       Http2Stream rstStream = Http2Connection.this.removeStream(streamId);
/*  767 */       if (rstStream != null) {
/*  768 */         rstStream.receiveRstStream(errorCode);
/*      */       }
/*      */     }
/*      */     
/*      */     public void settings(final boolean clearPrevious, final Settings settings) {
/*      */       try {
/*  774 */         Http2Connection.this.writerExecutor.execute((Runnable)new NamedRunnable("OkHttp %s ACK Settings", new Object[] { this.this$0.hostname }) {
/*      */               public void execute() {
/*  776 */                 Http2Connection.ReaderRunnable.this.applyAndAckSettings(clearPrevious, settings);
/*      */               }
/*      */             });
/*  779 */       } catch (RejectedExecutionException rejectedExecutionException) {}
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     void applyAndAckSettings(boolean clearPrevious, Settings settings) {
/*  785 */       long delta = 0L;
/*  786 */       Http2Stream[] streamsToNotify = null;
/*  787 */       synchronized (Http2Connection.this.writer) {
/*  788 */         synchronized (Http2Connection.this) {
/*  789 */           int priorWriteWindowSize = Http2Connection.this.peerSettings.getInitialWindowSize();
/*  790 */           if (clearPrevious) Http2Connection.this.peerSettings.clear(); 
/*  791 */           Http2Connection.this.peerSettings.merge(settings);
/*  792 */           int peerInitialWindowSize = Http2Connection.this.peerSettings.getInitialWindowSize();
/*  793 */           if (peerInitialWindowSize != -1 && peerInitialWindowSize != priorWriteWindowSize) {
/*  794 */             delta = (peerInitialWindowSize - priorWriteWindowSize);
/*      */ 
/*      */             
/*  797 */             streamsToNotify = !Http2Connection.this.streams.isEmpty() ? (Http2Stream[])Http2Connection.this.streams.values().toArray((Object[])new Http2Stream[Http2Connection.this.streams.size()]) : null;
/*      */           } 
/*      */         } 
/*      */         try {
/*  801 */           Http2Connection.this.writer.applyAndAckSettings(Http2Connection.this.peerSettings);
/*  802 */         } catch (IOException e) {
/*  803 */           Http2Connection.this.failConnection();
/*      */         } 
/*      */       } 
/*  806 */       if (streamsToNotify != null) {
/*  807 */         for (Http2Stream stream : streamsToNotify) {
/*  808 */           synchronized (stream) {
/*  809 */             stream.addBytesToWriteWindow(delta);
/*      */           } 
/*      */         } 
/*      */       }
/*  813 */       Http2Connection.listenerExecutor.execute((Runnable)new NamedRunnable("OkHttp %s settings", new Object[] { this.this$0.hostname }) {
/*      */             public void execute() {
/*  815 */               Http2Connection.this.listener.onSettings(Http2Connection.this);
/*      */             }
/*      */           });
/*      */     }
/*      */ 
/*      */     
/*      */     public void ackSettings() {}
/*      */ 
/*      */     
/*      */     public void ping(boolean reply, int payload1, int payload2) {
/*  825 */       if (reply) {
/*  826 */         synchronized (Http2Connection.this) {
/*  827 */           if (payload1 == 1) {
/*  828 */             Http2Connection.this.intervalPongsReceived++;
/*  829 */           } else if (payload1 == 2) {
/*  830 */             Http2Connection.this.degradedPongsReceived++;
/*  831 */           } else if (payload1 == 3) {
/*  832 */             Http2Connection.this.awaitPongsReceived++;
/*  833 */             Http2Connection.this.notifyAll();
/*      */           } 
/*      */         } 
/*      */       } else {
/*      */         
/*      */         try {
/*  839 */           Http2Connection.this.writerExecutor.execute((Runnable)new Http2Connection.PingRunnable(true, payload1, payload2));
/*  840 */         } catch (RejectedExecutionException rejectedExecutionException) {}
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     public void goAway(int lastGoodStreamId, ErrorCode errorCode, ByteString debugData) {
/*      */       Http2Stream[] streamsCopy;
/*  847 */       if (debugData.size() > 0);
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  852 */       synchronized (Http2Connection.this) {
/*  853 */         streamsCopy = (Http2Stream[])Http2Connection.this.streams.values().toArray((Object[])new Http2Stream[Http2Connection.this.streams.size()]);
/*  854 */         Http2Connection.this.shutdown = true;
/*      */       } 
/*      */ 
/*      */       
/*  858 */       for (Http2Stream http2Stream : streamsCopy) {
/*  859 */         if (http2Stream.getId() > lastGoodStreamId && http2Stream.isLocallyInitiated()) {
/*  860 */           http2Stream.receiveRstStream(ErrorCode.REFUSED_STREAM);
/*  861 */           Http2Connection.this.removeStream(http2Stream.getId());
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     public void windowUpdate(int streamId, long windowSizeIncrement) {
/*  867 */       if (streamId == 0) {
/*  868 */         synchronized (Http2Connection.this) {
/*  869 */           Http2Connection.this.bytesLeftInWriteWindow += windowSizeIncrement;
/*  870 */           Http2Connection.this.notifyAll();
/*      */         } 
/*      */       } else {
/*  873 */         Http2Stream stream = Http2Connection.this.getStream(streamId);
/*  874 */         if (stream != null) {
/*  875 */           synchronized (stream) {
/*  876 */             stream.addBytesToWriteWindow(windowSizeIncrement);
/*      */           } 
/*      */         }
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     public void priority(int streamId, int streamDependency, int weight, boolean exclusive) {}
/*      */ 
/*      */ 
/*      */     
/*      */     public void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders) {
/*  889 */       Http2Connection.this.pushRequestLater(promisedStreamId, requestHeaders);
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     public void alternateService(int streamId, String origin, ByteString protocol, String host, int port, long maxAge) {}
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   boolean pushedStream(int streamId) {
/*  900 */     return (streamId != 0 && (streamId & 0x1) == 0);
/*      */   }
/*      */   
/*      */   Http2Connection(Builder builder) {
/*  904 */     this.currentPushRequests = new LinkedHashSet<>(); this.pushObserver = builder.pushObserver; this.client = builder.client; this.listener = builder.listener; this.nextStreamId = builder.client ? 1 : 2; if (builder.client)
/*      */       this.nextStreamId += 2;  if (builder.client)
/*      */       this.okHttpSettings.set(7, 16777216);  this.hostname = builder.hostname; this.writerExecutor = new ScheduledThreadPoolExecutor(1, Util.threadFactory(Util.format("OkHttp %s Writer", new Object[] { this.hostname }), false)); if (builder.pingIntervalMillis != 0)
/*  907 */       this.writerExecutor.scheduleAtFixedRate((Runnable)new IntervalPingRunnable(), builder.pingIntervalMillis, builder.pingIntervalMillis, TimeUnit.MILLISECONDS);  this.pushExecutor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Util.threadFactory(Util.format("OkHttp %s Push Observer", new Object[] { this.hostname }), true)); this.peerSettings.set(7, 65535); this.peerSettings.set(5, 16384); this.bytesLeftInWriteWindow = this.peerSettings.getInitialWindowSize(); this.socket = builder.socket; this.writer = new Http2Writer(builder.sink, this.client); this.readerRunnable = new ReaderRunnable(new Http2Reader(builder.source, this.client)); } void pushRequestLater(final int streamId, final List<Header> requestHeaders) { synchronized (this) {
/*  908 */       if (this.currentPushRequests.contains(Integer.valueOf(streamId))) {
/*  909 */         writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
/*      */         return;
/*      */       } 
/*  912 */       this.currentPushRequests.add(Integer.valueOf(streamId));
/*      */     } 
/*      */     try {
/*  915 */       pushExecutorExecute(new NamedRunnable("OkHttp %s Push Request[%s]", new Object[] { this.hostname, Integer.valueOf(streamId) }) {
/*      */             public void execute() {
/*  917 */               boolean cancel = Http2Connection.this.pushObserver.onRequest(streamId, requestHeaders);
/*      */               try {
/*  919 */                 if (cancel) {
/*  920 */                   Http2Connection.this.writer.rstStream(streamId, ErrorCode.CANCEL);
/*  921 */                   synchronized (Http2Connection.this) {
/*  922 */                     Http2Connection.this.currentPushRequests.remove(Integer.valueOf(streamId));
/*      */                   } 
/*      */                 } 
/*  925 */               } catch (IOException iOException) {}
/*      */             }
/*      */           });
/*      */     }
/*  929 */     catch (RejectedExecutionException rejectedExecutionException) {} }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   void pushHeadersLater(final int streamId, final List<Header> requestHeaders, final boolean inFinished) {
/*      */     try {
/*  937 */       pushExecutorExecute(new NamedRunnable("OkHttp %s Push Headers[%s]", new Object[] { this.hostname, Integer.valueOf(streamId) }) {
/*      */             public void execute() {
/*  939 */               boolean cancel = Http2Connection.this.pushObserver.onHeaders(streamId, requestHeaders, inFinished);
/*      */               try {
/*  941 */                 if (cancel) Http2Connection.this.writer.rstStream(streamId, ErrorCode.CANCEL); 
/*  942 */                 if (cancel || inFinished) {
/*  943 */                   synchronized (Http2Connection.this) {
/*  944 */                     Http2Connection.this.currentPushRequests.remove(Integer.valueOf(streamId));
/*      */                   } 
/*      */                 }
/*  947 */               } catch (IOException iOException) {}
/*      */             }
/*      */           });
/*      */     }
/*  951 */     catch (RejectedExecutionException rejectedExecutionException) {}
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   void pushDataLater(final int streamId, BufferedSource source, final int byteCount, final boolean inFinished) throws IOException {
/*  962 */     final Buffer buffer = new Buffer();
/*  963 */     source.require(byteCount);
/*  964 */     source.read(buffer, byteCount);
/*  965 */     if (buffer.size() != byteCount) throw new IOException(buffer.size() + " != " + byteCount); 
/*  966 */     pushExecutorExecute(new NamedRunnable("OkHttp %s Push Data[%s]", new Object[] { this.hostname, Integer.valueOf(streamId) }) {
/*      */           public void execute() {
/*      */             try {
/*  969 */               boolean cancel = Http2Connection.this.pushObserver.onData(streamId, (BufferedSource)buffer, byteCount, inFinished);
/*  970 */               if (cancel) Http2Connection.this.writer.rstStream(streamId, ErrorCode.CANCEL); 
/*  971 */               if (cancel || inFinished) {
/*  972 */                 synchronized (Http2Connection.this) {
/*  973 */                   Http2Connection.this.currentPushRequests.remove(Integer.valueOf(streamId));
/*      */                 } 
/*      */               }
/*  976 */             } catch (IOException iOException) {}
/*      */           }
/*      */         });
/*      */   }
/*      */ 
/*      */   
/*      */   void pushResetLater(final int streamId, final ErrorCode errorCode) {
/*  983 */     pushExecutorExecute(new NamedRunnable("OkHttp %s Push Reset[%s]", new Object[] { this.hostname, Integer.valueOf(streamId) }) {
/*      */           public void execute() {
/*  985 */             Http2Connection.this.pushObserver.onReset(streamId, errorCode);
/*  986 */             synchronized (Http2Connection.this) {
/*  987 */               Http2Connection.this.currentPushRequests.remove(Integer.valueOf(streamId));
/*      */             } 
/*      */           }
/*      */         });
/*      */   }
/*      */   
/*      */   private synchronized void pushExecutorExecute(NamedRunnable namedRunnable) {
/*  994 */     if (!this.shutdown) {
/*  995 */       this.pushExecutor.execute((Runnable)namedRunnable);
/*      */     }
/*      */   }
/*      */   
/*      */   public static abstract class Listener
/*      */   {
/* 1001 */     public static final Listener REFUSE_INCOMING_STREAMS = new Listener() {
/*      */         public void onStream(Http2Stream stream) throws IOException {
/* 1003 */           stream.close(ErrorCode.REFUSED_STREAM);
/*      */         }
/*      */       };
/*      */     
/*      */     public abstract void onStream(Http2Stream param1Http2Stream) throws IOException;
/*      */     
/*      */     public void onSettings(Http2Connection connection) {}
/*      */   }
/*      */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Http2Connection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */