/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.util.ArrayDeque;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Deque;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.AsyncTimeout;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSource;
/*     */ import okio.Sink;
/*     */ import okio.Source;
/*     */ import okio.Timeout;
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
/*     */ public final class Http2Stream
/*     */ {
/*  46 */   long unacknowledgedBytesRead = 0L;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   long bytesLeftInWriteWindow;
/*     */ 
/*     */ 
/*     */   
/*     */   final int id;
/*     */ 
/*     */ 
/*     */   
/*     */   final Http2Connection connection;
/*     */ 
/*     */ 
/*     */   
/*  63 */   private final Deque<Headers> headersQueue = new ArrayDeque<>();
/*     */   
/*     */   private Header.Listener headersListener;
/*     */   
/*     */   private boolean hasResponseHeaders;
/*     */   
/*     */   private final FramingSource source;
/*     */   final FramingSink sink;
/*  71 */   final StreamTimeout readTimeout = new StreamTimeout();
/*  72 */   final StreamTimeout writeTimeout = new StreamTimeout();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  79 */   ErrorCode errorCode = null;
/*     */ 
/*     */   
/*     */   Http2Stream(int id, Http2Connection connection, boolean outFinished, boolean inFinished, @Nullable Headers headers) {
/*  83 */     if (connection == null) throw new NullPointerException("connection == null");
/*     */     
/*  85 */     this.id = id;
/*  86 */     this.connection = connection;
/*  87 */     this
/*  88 */       .bytesLeftInWriteWindow = connection.peerSettings.getInitialWindowSize();
/*  89 */     this.source = new FramingSource(connection.okHttpSettings.getInitialWindowSize());
/*  90 */     this.sink = new FramingSink();
/*  91 */     this.source.finished = inFinished;
/*  92 */     this.sink.finished = outFinished;
/*  93 */     if (headers != null) {
/*  94 */       this.headersQueue.add(headers);
/*     */     }
/*     */     
/*  97 */     if (isLocallyInitiated() && headers != null)
/*  98 */       throw new IllegalStateException("locally-initiated streams shouldn't have headers yet"); 
/*  99 */     if (!isLocallyInitiated() && headers == null) {
/* 100 */       throw new IllegalStateException("remotely-initiated streams should have headers");
/*     */     }
/*     */   }
/*     */   
/*     */   public int getId() {
/* 105 */     return this.id;
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
/*     */   public synchronized boolean isOpen() {
/* 120 */     if (this.errorCode != null) {
/* 121 */       return false;
/*     */     }
/* 123 */     if ((this.source.finished || this.source.closed) && (this.sink.finished || this.sink.closed) && this.hasResponseHeaders)
/*     */     {
/*     */       
/* 126 */       return false;
/*     */     }
/* 128 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean isLocallyInitiated() {
/* 133 */     boolean streamIsClient = ((this.id & 0x1) == 1);
/* 134 */     return (this.connection.client == streamIsClient);
/*     */   }
/*     */   
/*     */   public Http2Connection getConnection() {
/* 138 */     return this.connection;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized Headers takeHeaders() throws IOException {
/* 147 */     this.readTimeout.enter();
/*     */     try {
/* 149 */       while (this.headersQueue.isEmpty() && this.errorCode == null) {
/* 150 */         waitForIo();
/*     */       }
/*     */     } finally {
/* 153 */       this.readTimeout.exitAndThrowIfTimedOut();
/*     */     } 
/* 155 */     if (!this.headersQueue.isEmpty()) {
/* 156 */       return this.headersQueue.removeFirst();
/*     */     }
/* 158 */     throw new StreamResetException(this.errorCode);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized ErrorCode getErrorCode() {
/* 166 */     return this.errorCode;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void writeHeaders(List<Header> responseHeaders, boolean out) throws IOException {
/* 176 */     assert !Thread.holdsLock(this);
/* 177 */     if (responseHeaders == null) {
/* 178 */       throw new NullPointerException("headers == null");
/*     */     }
/* 180 */     boolean outFinished = false;
/* 181 */     boolean flushHeaders = false;
/* 182 */     synchronized (this) {
/* 183 */       this.hasResponseHeaders = true;
/* 184 */       if (!out) {
/* 185 */         this.sink.finished = true;
/* 186 */         flushHeaders = true;
/* 187 */         outFinished = true;
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 193 */     if (!flushHeaders) {
/* 194 */       synchronized (this.connection) {
/* 195 */         flushHeaders = (this.connection.bytesLeftInWriteWindow == 0L);
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/* 200 */     this.connection.writeSynReply(this.id, outFinished, responseHeaders);
/*     */     
/* 202 */     if (flushHeaders) {
/* 203 */       this.connection.flush();
/*     */     }
/*     */   }
/*     */   
/*     */   public Timeout readTimeout() {
/* 208 */     return (Timeout)this.readTimeout;
/*     */   }
/*     */   
/*     */   public Timeout writeTimeout() {
/* 212 */     return (Timeout)this.writeTimeout;
/*     */   }
/*     */ 
/*     */   
/*     */   public Source getSource() {
/* 217 */     return this.source;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Sink getSink() {
/* 227 */     synchronized (this) {
/* 228 */       if (!this.hasResponseHeaders && !isLocallyInitiated()) {
/* 229 */         throw new IllegalStateException("reply before requesting the sink");
/*     */       }
/*     */     } 
/* 232 */     return this.sink;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void close(ErrorCode rstStatusCode) throws IOException {
/* 240 */     if (!closeInternal(rstStatusCode)) {
/*     */       return;
/*     */     }
/* 243 */     this.connection.writeSynReset(this.id, rstStatusCode);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void closeLater(ErrorCode errorCode) {
/* 251 */     if (!closeInternal(errorCode)) {
/*     */       return;
/*     */     }
/* 254 */     this.connection.writeSynResetLater(this.id, errorCode);
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean closeInternal(ErrorCode errorCode) {
/* 259 */     assert !Thread.holdsLock(this);
/* 260 */     synchronized (this) {
/* 261 */       if (this.errorCode != null) {
/* 262 */         return false;
/*     */       }
/* 264 */       if (this.source.finished && this.sink.finished) {
/* 265 */         return false;
/*     */       }
/* 267 */       this.errorCode = errorCode;
/* 268 */       notifyAll();
/*     */     } 
/* 270 */     this.connection.removeStream(this.id);
/* 271 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void receiveHeaders(List<Header> headers) {
/*     */     boolean open;
/* 279 */     assert !Thread.holdsLock(this);
/*     */     
/* 281 */     synchronized (this) {
/* 282 */       this.hasResponseHeaders = true;
/* 283 */       this.headersQueue.add(Util.toHeaders(headers));
/* 284 */       open = isOpen();
/* 285 */       notifyAll();
/*     */     } 
/* 287 */     if (!open) {
/* 288 */       this.connection.removeStream(this.id);
/*     */     }
/*     */   }
/*     */   
/*     */   void receiveData(BufferedSource in, int length) throws IOException {
/* 293 */     assert !Thread.holdsLock(this);
/* 294 */     this.source.receive(in, length);
/*     */   }
/*     */   void receiveFin() {
/*     */     boolean open;
/* 298 */     assert !Thread.holdsLock(this);
/*     */     
/* 300 */     synchronized (this) {
/* 301 */       this.source.finished = true;
/* 302 */       open = isOpen();
/* 303 */       notifyAll();
/*     */     } 
/* 305 */     if (!open) {
/* 306 */       this.connection.removeStream(this.id);
/*     */     }
/*     */   }
/*     */   
/*     */   synchronized void receiveRstStream(ErrorCode errorCode) {
/* 311 */     if (this.errorCode == null) {
/* 312 */       this.errorCode = errorCode;
/* 313 */       notifyAll();
/*     */     } 
/*     */   }
/*     */   
/*     */   public synchronized void setHeadersListener(Header.Listener headersListener) {
/* 318 */     this.headersListener = headersListener;
/* 319 */     if (!this.headersQueue.isEmpty() && headersListener != null) {
/* 320 */       notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final class FramingSource
/*     */     implements Source
/*     */   {
/* 331 */     private final Buffer receiveBuffer = new Buffer();
/*     */ 
/*     */     
/* 334 */     private final Buffer readBuffer = new Buffer();
/*     */ 
/*     */ 
/*     */     
/*     */     private final long maxByteCount;
/*     */ 
/*     */     
/*     */     boolean closed;
/*     */ 
/*     */     
/*     */     boolean finished;
/*     */ 
/*     */ 
/*     */     
/*     */     FramingSource(long maxByteCount) {
/* 349 */       this.maxByteCount = maxByteCount;
/*     */     } public long read(Buffer sink, long byteCount) throws IOException {
/*     */       long readBytesDelivered;
/*     */       ErrorCode errorCodeToDeliver;
/* 353 */       if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
/*     */       
/*     */       while (true) {
/* 356 */         Headers headersToDeliver = null;
/* 357 */         Header.Listener headersListenerToNotify = null;
/* 358 */         readBytesDelivered = -1L;
/* 359 */         errorCodeToDeliver = null;
/*     */ 
/*     */ 
/*     */         
/* 363 */         synchronized (Http2Stream.this) {
/* 364 */           Http2Stream.this.readTimeout.enter();
/*     */           
/* 366 */           try { if (Http2Stream.this.errorCode != null)
/*     */             {
/* 368 */               errorCodeToDeliver = Http2Stream.this.errorCode;
/*     */             }
/*     */             
/* 371 */             if (this.closed) {
/* 372 */               throw new IOException("stream closed");
/*     */             }
/* 374 */             if (!Http2Stream.this.headersQueue.isEmpty() && Http2Stream.this.headersListener != null)
/*     */             
/* 376 */             { headersToDeliver = Http2Stream.this.headersQueue.removeFirst();
/* 377 */               headersListenerToNotify = Http2Stream.this.headersListener; }
/*     */             
/* 379 */             else if (this.readBuffer.size() > 0L)
/*     */             
/* 381 */             { readBytesDelivered = this.readBuffer.read(sink, Math.min(byteCount, this.readBuffer.size()));
/* 382 */               Http2Stream.this.unacknowledgedBytesRead += readBytesDelivered;
/*     */               
/* 384 */               if (errorCodeToDeliver == null && Http2Stream.this.unacknowledgedBytesRead >= (Http2Stream.this.connection.okHttpSettings
/*     */                 
/* 386 */                 .getInitialWindowSize() / 2)) {
/*     */ 
/*     */                 
/* 389 */                 Http2Stream.this.connection.writeWindowUpdateLater(Http2Stream.this.id, Http2Stream.this.unacknowledgedBytesRead);
/* 390 */                 Http2Stream.this.unacknowledgedBytesRead = 0L;
/*     */               }  }
/* 392 */             else if (!this.finished && errorCodeToDeliver == null)
/*     */             
/* 394 */             { Http2Stream.this.waitForIo();
/*     */ 
/*     */ 
/*     */               
/* 398 */               Http2Stream.this.readTimeout.exitAndThrowIfTimedOut(); continue; }  } finally { Http2Stream.this.readTimeout.exitAndThrowIfTimedOut(); }
/*     */         
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 404 */         if (headersToDeliver != null && headersListenerToNotify != null) {
/* 405 */           headersListenerToNotify.onHeaders(headersToDeliver); continue;
/*     */         } 
/*     */         break;
/*     */       } 
/* 409 */       if (readBytesDelivered != -1L) {
/*     */         
/* 411 */         updateConnectionFlowControl(readBytesDelivered);
/* 412 */         return readBytesDelivered;
/*     */       } 
/*     */       
/* 415 */       if (errorCodeToDeliver != null)
/*     */       {
/*     */ 
/*     */ 
/*     */         
/* 420 */         throw new StreamResetException(errorCodeToDeliver);
/*     */       }
/*     */       
/* 423 */       return -1L;
/*     */     }
/*     */ 
/*     */     
/*     */     private void updateConnectionFlowControl(long read) {
/* 428 */       assert !Thread.holdsLock(Http2Stream.this);
/* 429 */       Http2Stream.this.connection.updateConnectionFlowControl(read);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     void receive(BufferedSource in, long byteCount) throws IOException {
/* 437 */       assert !Thread.holdsLock(Http2Stream.this);
/*     */       
/* 439 */       while (byteCount > 0L) {
/*     */         boolean finished, flowControlError;
/*     */         
/* 442 */         synchronized (Http2Stream.this) {
/* 443 */           finished = this.finished;
/* 444 */           flowControlError = (byteCount + this.readBuffer.size() > this.maxByteCount);
/*     */         } 
/*     */ 
/*     */         
/* 448 */         if (flowControlError) {
/* 449 */           in.skip(byteCount);
/* 450 */           Http2Stream.this.closeLater(ErrorCode.FLOW_CONTROL_ERROR);
/*     */           
/*     */           return;
/*     */         } 
/*     */         
/* 455 */         if (finished) {
/* 456 */           in.skip(byteCount);
/*     */           
/*     */           return;
/*     */         } 
/*     */         
/* 461 */         long read = in.read(this.receiveBuffer, byteCount);
/* 462 */         if (read == -1L) throw new EOFException(); 
/* 463 */         byteCount -= read;
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 468 */         long bytesDiscarded = 0L;
/* 469 */         synchronized (Http2Stream.this) {
/* 470 */           if (this.closed) {
/* 471 */             bytesDiscarded = this.receiveBuffer.size();
/* 472 */             this.receiveBuffer.clear();
/*     */           } else {
/* 474 */             boolean wasEmpty = (this.readBuffer.size() == 0L);
/* 475 */             this.readBuffer.writeAll((Source)this.receiveBuffer);
/* 476 */             if (wasEmpty) {
/* 477 */               Http2Stream.this.notifyAll();
/*     */             }
/*     */           } 
/*     */         } 
/* 481 */         if (bytesDiscarded > 0L) {
/* 482 */           updateConnectionFlowControl(bytesDiscarded);
/*     */         }
/*     */       } 
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 488 */       return (Timeout)Http2Stream.this.readTimeout;
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/*     */       long bytesDiscarded;
/* 493 */       List<Headers> headersToDeliver = null;
/* 494 */       Header.Listener headersListenerToNotify = null;
/* 495 */       synchronized (Http2Stream.this) {
/* 496 */         this.closed = true;
/* 497 */         bytesDiscarded = this.readBuffer.size();
/* 498 */         this.readBuffer.clear();
/* 499 */         if (!Http2Stream.this.headersQueue.isEmpty() && Http2Stream.this.headersListener != null) {
/* 500 */           headersToDeliver = new ArrayList<>(Http2Stream.this.headersQueue);
/* 501 */           Http2Stream.this.headersQueue.clear();
/* 502 */           headersListenerToNotify = Http2Stream.this.headersListener;
/*     */         } 
/* 504 */         Http2Stream.this.notifyAll();
/*     */       } 
/* 506 */       if (bytesDiscarded > 0L) {
/* 507 */         updateConnectionFlowControl(bytesDiscarded);
/*     */       }
/* 509 */       Http2Stream.this.cancelStreamIfNecessary();
/* 510 */       if (headersListenerToNotify != null)
/* 511 */         for (Headers headers : headersToDeliver)
/* 512 */           headersListenerToNotify.onHeaders(headers);  
/*     */     }
/*     */   }
/*     */   
/*     */   void cancelStreamIfNecessary() throws IOException {
/*     */     boolean cancel;
/*     */     boolean open;
/* 519 */     assert !Thread.holdsLock(this);
/*     */ 
/*     */     
/* 522 */     synchronized (this) {
/* 523 */       cancel = (!this.source.finished && this.source.closed && (this.sink.finished || this.sink.closed));
/* 524 */       open = isOpen();
/*     */     } 
/* 526 */     if (cancel) {
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 531 */       close(ErrorCode.CANCEL);
/* 532 */     } else if (!open) {
/* 533 */       this.connection.removeStream(this.id);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   final class FramingSink
/*     */     implements Sink
/*     */   {
/*     */     private static final long EMIT_BUFFER_SIZE = 16384L;
/*     */ 
/*     */     
/* 545 */     private final Buffer sendBuffer = new Buffer();
/*     */ 
/*     */     
/*     */     boolean closed;
/*     */ 
/*     */     
/*     */     boolean finished;
/*     */ 
/*     */     
/*     */     public void write(Buffer source, long byteCount) throws IOException {
/* 555 */       assert !Thread.holdsLock(Http2Stream.this);
/* 556 */       this.sendBuffer.write(source, byteCount);
/* 557 */       while (this.sendBuffer.size() >= 16384L) {
/* 558 */         emitFrame(false);
/*     */       }
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private void emitFrame(boolean outFinished) throws IOException {
/*     */       long toWrite;
/* 568 */       synchronized (Http2Stream.this) {
/* 569 */         Http2Stream.this.writeTimeout.enter();
/*     */         try {
/* 571 */           while (Http2Stream.this.bytesLeftInWriteWindow <= 0L && !this.finished && !this.closed && Http2Stream.this.errorCode == null) {
/* 572 */             Http2Stream.this.waitForIo();
/*     */           }
/*     */         } finally {
/* 575 */           Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
/*     */         } 
/*     */         
/* 578 */         Http2Stream.this.checkOutNotClosed();
/* 579 */         toWrite = Math.min(Http2Stream.this.bytesLeftInWriteWindow, this.sendBuffer.size());
/* 580 */         Http2Stream.this.bytesLeftInWriteWindow -= toWrite;
/*     */       } 
/*     */       
/* 583 */       Http2Stream.this.writeTimeout.enter();
/*     */       try {
/* 585 */         Http2Stream.this.connection.writeData(Http2Stream.this.id, (outFinished && toWrite == this.sendBuffer.size()), this.sendBuffer, toWrite);
/*     */       } finally {
/* 587 */         Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
/*     */       } 
/*     */     }
/*     */     
/*     */     public void flush() throws IOException {
/* 592 */       assert !Thread.holdsLock(Http2Stream.this);
/* 593 */       synchronized (Http2Stream.this) {
/* 594 */         Http2Stream.this.checkOutNotClosed();
/*     */       } 
/* 596 */       while (this.sendBuffer.size() > 0L) {
/* 597 */         emitFrame(false);
/* 598 */         Http2Stream.this.connection.flush();
/*     */       } 
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 603 */       return (Timeout)Http2Stream.this.writeTimeout;
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 607 */       assert !Thread.holdsLock(Http2Stream.this);
/* 608 */       synchronized (Http2Stream.this) {
/* 609 */         if (this.closed)
/*     */           return; 
/* 611 */       }  if (!Http2Stream.this.sink.finished)
/*     */       {
/* 613 */         if (this.sendBuffer.size() > 0L) {
/* 614 */           while (this.sendBuffer.size() > 0L) {
/* 615 */             emitFrame(true);
/*     */           }
/*     */         } else {
/*     */           
/* 619 */           Http2Stream.this.connection.writeData(Http2Stream.this.id, true, null, 0L);
/*     */         } 
/*     */       }
/* 622 */       synchronized (Http2Stream.this) {
/* 623 */         this.closed = true;
/*     */       } 
/* 625 */       Http2Stream.this.connection.flush();
/* 626 */       Http2Stream.this.cancelStreamIfNecessary();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void addBytesToWriteWindow(long delta) {
/* 634 */     this.bytesLeftInWriteWindow += delta;
/* 635 */     if (delta > 0L) notifyAll(); 
/*     */   }
/*     */   
/*     */   void checkOutNotClosed() throws IOException {
/* 639 */     if (this.sink.closed)
/* 640 */       throw new IOException("stream closed"); 
/* 641 */     if (this.sink.finished)
/* 642 */       throw new IOException("stream finished"); 
/* 643 */     if (this.errorCode != null) {
/* 644 */       throw new StreamResetException(this.errorCode);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void waitForIo() throws InterruptedIOException {
/*     */     try {
/* 654 */       wait();
/* 655 */     } catch (InterruptedException e) {
/* 656 */       Thread.currentThread().interrupt();
/* 657 */       throw new InterruptedIOException();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   class StreamTimeout
/*     */     extends AsyncTimeout
/*     */   {
/*     */     protected void timedOut() {
/* 667 */       Http2Stream.this.closeLater(ErrorCode.CANCEL);
/* 668 */       Http2Stream.this.connection.sendDegradedPingLater();
/*     */     }
/*     */     
/*     */     protected IOException newTimeoutException(IOException cause) {
/* 672 */       SocketTimeoutException socketTimeoutException = new SocketTimeoutException("timeout");
/* 673 */       if (cause != null) {
/* 674 */         socketTimeoutException.initCause(cause);
/*     */       }
/* 676 */       return socketTimeoutException;
/*     */     }
/*     */     
/*     */     public void exitAndThrowIfTimedOut() throws IOException {
/* 680 */       if (exit()) throw newTimeoutException(null); 
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Http2Stream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */