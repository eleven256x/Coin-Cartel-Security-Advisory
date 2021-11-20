/*     */ package okhttp3.internal.http1;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.net.ProtocolException;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.ResponseBody;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.RealConnection;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ import okhttp3.internal.http.HttpCodec;
/*     */ import okhttp3.internal.http.HttpHeaders;
/*     */ import okhttp3.internal.http.RealResponseBody;
/*     */ import okhttp3.internal.http.RequestLine;
/*     */ import okhttp3.internal.http.StatusLine;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
/*     */ import okio.BufferedSource;
/*     */ import okio.ForwardingTimeout;
/*     */ import okio.Okio;
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
/*     */ public final class Http1Codec
/*     */   implements HttpCodec
/*     */ {
/*     */   private static final int STATE_IDLE = 0;
/*     */   private static final int STATE_OPEN_REQUEST_BODY = 1;
/*     */   private static final int STATE_WRITING_REQUEST_BODY = 2;
/*     */   private static final int STATE_READ_RESPONSE_HEADERS = 3;
/*     */   private static final int STATE_OPEN_RESPONSE_BODY = 4;
/*     */   private static final int STATE_READING_RESPONSE_BODY = 5;
/*     */   private static final int STATE_CLOSED = 6;
/*     */   private static final int HEADER_LIMIT = 262144;
/*     */   final OkHttpClient client;
/*     */   final StreamAllocation streamAllocation;
/*     */   final BufferedSource source;
/*     */   final BufferedSink sink;
/*  86 */   int state = 0;
/*  87 */   private long headerLimit = 262144L;
/*     */ 
/*     */   
/*     */   public Http1Codec(OkHttpClient client, StreamAllocation streamAllocation, BufferedSource source, BufferedSink sink) {
/*  91 */     this.client = client;
/*  92 */     this.streamAllocation = streamAllocation;
/*  93 */     this.source = source;
/*  94 */     this.sink = sink;
/*     */   }
/*     */   
/*     */   public Sink createRequestBody(Request request, long contentLength) {
/*  98 */     if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding")))
/*     */     {
/* 100 */       return newChunkedSink();
/*     */     }
/*     */     
/* 103 */     if (contentLength != -1L)
/*     */     {
/* 105 */       return newFixedLengthSink(contentLength);
/*     */     }
/*     */     
/* 108 */     throw new IllegalStateException("Cannot stream a request body without chunked encoding or a known content length!");
/*     */   }
/*     */ 
/*     */   
/*     */   public void cancel() {
/* 113 */     RealConnection connection = this.streamAllocation.connection();
/* 114 */     if (connection != null) connection.cancel();
/*     */   
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
/*     */   public void writeRequestHeaders(Request request) throws IOException {
/* 128 */     String requestLine = RequestLine.get(request, this.streamAllocation
/* 129 */         .connection().route().proxy().type());
/* 130 */     writeRequest(request.headers(), requestLine);
/*     */   }
/*     */   
/*     */   public ResponseBody openResponseBody(Response response) throws IOException {
/* 134 */     this.streamAllocation.eventListener.responseBodyStart(this.streamAllocation.call);
/* 135 */     String contentType = response.header("Content-Type");
/*     */     
/* 137 */     if (!HttpHeaders.hasBody(response)) {
/* 138 */       Source source = newFixedLengthSource(0L);
/* 139 */       return (ResponseBody)new RealResponseBody(contentType, 0L, Okio.buffer(source));
/*     */     } 
/*     */     
/* 142 */     if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
/* 143 */       Source source = newChunkedSource(response.request().url());
/* 144 */       return (ResponseBody)new RealResponseBody(contentType, -1L, Okio.buffer(source));
/*     */     } 
/*     */     
/* 147 */     long contentLength = HttpHeaders.contentLength(response);
/* 148 */     if (contentLength != -1L) {
/* 149 */       Source source = newFixedLengthSource(contentLength);
/* 150 */       return (ResponseBody)new RealResponseBody(contentType, contentLength, Okio.buffer(source));
/*     */     } 
/*     */     
/* 153 */     return (ResponseBody)new RealResponseBody(contentType, -1L, Okio.buffer(newUnknownLengthSource()));
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean isClosed() {
/* 158 */     return (this.state == 6);
/*     */   }
/*     */   
/*     */   public void flushRequest() throws IOException {
/* 162 */     this.sink.flush();
/*     */   }
/*     */   
/*     */   public void finishRequest() throws IOException {
/* 166 */     this.sink.flush();
/*     */   }
/*     */ 
/*     */   
/*     */   public void writeRequest(Headers headers, String requestLine) throws IOException {
/* 171 */     if (this.state != 0) throw new IllegalStateException("state: " + this.state); 
/* 172 */     this.sink.writeUtf8(requestLine).writeUtf8("\r\n");
/* 173 */     for (int i = 0, size = headers.size(); i < size; i++) {
/* 174 */       this.sink.writeUtf8(headers.name(i))
/* 175 */         .writeUtf8(": ")
/* 176 */         .writeUtf8(headers.value(i))
/* 177 */         .writeUtf8("\r\n");
/*     */     }
/* 179 */     this.sink.writeUtf8("\r\n");
/* 180 */     this.state = 1;
/*     */   }
/*     */   
/*     */   public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
/* 184 */     if (this.state != 1 && this.state != 3) {
/* 185 */       throw new IllegalStateException("state: " + this.state);
/*     */     }
/*     */     
/*     */     try {
/* 189 */       StatusLine statusLine = StatusLine.parse(readHeaderLine());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 195 */       Response.Builder responseBuilder = (new Response.Builder()).protocol(statusLine.protocol).code(statusLine.code).message(statusLine.message).headers(readHeaders());
/*     */       
/* 197 */       if (expectContinue && statusLine.code == 100)
/* 198 */         return null; 
/* 199 */       if (statusLine.code == 100) {
/* 200 */         this.state = 3;
/* 201 */         return responseBuilder;
/*     */       } 
/*     */       
/* 204 */       this.state = 4;
/* 205 */       return responseBuilder;
/* 206 */     } catch (EOFException e) {
/*     */       
/* 208 */       IOException exception = new IOException("unexpected end of stream on " + this.streamAllocation);
/* 209 */       exception.initCause(e);
/* 210 */       throw exception;
/*     */     } 
/*     */   }
/*     */   
/*     */   private String readHeaderLine() throws IOException {
/* 215 */     String line = this.source.readUtf8LineStrict(this.headerLimit);
/* 216 */     this.headerLimit -= line.length();
/* 217 */     return line;
/*     */   }
/*     */ 
/*     */   
/*     */   public Headers readHeaders() throws IOException {
/* 222 */     Headers.Builder headers = new Headers.Builder();
/*     */     String line;
/* 224 */     while ((line = readHeaderLine()).length() != 0) {
/* 225 */       Internal.instance.addLenient(headers, line);
/*     */     }
/* 227 */     return headers.build();
/*     */   }
/*     */   
/*     */   public Sink newChunkedSink() {
/* 231 */     if (this.state != 1) throw new IllegalStateException("state: " + this.state); 
/* 232 */     this.state = 2;
/* 233 */     return new ChunkedSink();
/*     */   }
/*     */   
/*     */   public Sink newFixedLengthSink(long contentLength) {
/* 237 */     if (this.state != 1) throw new IllegalStateException("state: " + this.state); 
/* 238 */     this.state = 2;
/* 239 */     return new FixedLengthSink(contentLength);
/*     */   }
/*     */   
/*     */   public Source newFixedLengthSource(long length) throws IOException {
/* 243 */     if (this.state != 4) throw new IllegalStateException("state: " + this.state); 
/* 244 */     this.state = 5;
/* 245 */     return new FixedLengthSource(length);
/*     */   }
/*     */   
/*     */   public Source newChunkedSource(HttpUrl url) throws IOException {
/* 249 */     if (this.state != 4) throw new IllegalStateException("state: " + this.state); 
/* 250 */     this.state = 5;
/* 251 */     return new ChunkedSource(url);
/*     */   }
/*     */   
/*     */   public Source newUnknownLengthSource() throws IOException {
/* 255 */     if (this.state != 4) throw new IllegalStateException("state: " + this.state); 
/* 256 */     if (this.streamAllocation == null) throw new IllegalStateException("streamAllocation == null"); 
/* 257 */     this.state = 5;
/* 258 */     this.streamAllocation.noNewStreams();
/* 259 */     return new UnknownLengthSource();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void detachTimeout(ForwardingTimeout timeout) {
/* 268 */     Timeout oldDelegate = timeout.delegate();
/* 269 */     timeout.setDelegate(Timeout.NONE);
/* 270 */     oldDelegate.clearDeadline();
/* 271 */     oldDelegate.clearTimeout();
/*     */   }
/*     */   
/*     */   private final class FixedLengthSink
/*     */     implements Sink {
/* 276 */     private final ForwardingTimeout timeout = new ForwardingTimeout(Http1Codec.this.sink.timeout());
/*     */     private boolean closed;
/*     */     private long bytesRemaining;
/*     */     
/*     */     FixedLengthSink(long bytesRemaining) {
/* 281 */       this.bytesRemaining = bytesRemaining;
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 285 */       return (Timeout)this.timeout;
/*     */     }
/*     */     
/*     */     public void write(Buffer source, long byteCount) throws IOException {
/* 289 */       if (this.closed) throw new IllegalStateException("closed"); 
/* 290 */       Util.checkOffsetAndCount(source.size(), 0L, byteCount);
/* 291 */       if (byteCount > this.bytesRemaining) {
/* 292 */         throw new ProtocolException("expected " + this.bytesRemaining + " bytes but received " + byteCount);
/*     */       }
/*     */       
/* 295 */       Http1Codec.this.sink.write(source, byteCount);
/* 296 */       this.bytesRemaining -= byteCount;
/*     */     }
/*     */     
/*     */     public void flush() throws IOException {
/* 300 */       if (this.closed)
/* 301 */         return;  Http1Codec.this.sink.flush();
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 305 */       if (this.closed)
/* 306 */         return;  this.closed = true;
/* 307 */       if (this.bytesRemaining > 0L) throw new ProtocolException("unexpected end of stream"); 
/* 308 */       Http1Codec.this.detachTimeout(this.timeout);
/* 309 */       Http1Codec.this.state = 3;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private final class ChunkedSink
/*     */     implements Sink
/*     */   {
/* 318 */     private final ForwardingTimeout timeout = new ForwardingTimeout(Http1Codec.this.sink.timeout());
/*     */ 
/*     */     
/*     */     private boolean closed;
/*     */ 
/*     */     
/*     */     public Timeout timeout() {
/* 325 */       return (Timeout)this.timeout;
/*     */     }
/*     */     
/*     */     public void write(Buffer source, long byteCount) throws IOException {
/* 329 */       if (this.closed) throw new IllegalStateException("closed"); 
/* 330 */       if (byteCount == 0L)
/*     */         return; 
/* 332 */       Http1Codec.this.sink.writeHexadecimalUnsignedLong(byteCount);
/* 333 */       Http1Codec.this.sink.writeUtf8("\r\n");
/* 334 */       Http1Codec.this.sink.write(source, byteCount);
/* 335 */       Http1Codec.this.sink.writeUtf8("\r\n");
/*     */     }
/*     */     
/*     */     public synchronized void flush() throws IOException {
/* 339 */       if (this.closed)
/* 340 */         return;  Http1Codec.this.sink.flush();
/*     */     }
/*     */     
/*     */     public synchronized void close() throws IOException {
/* 344 */       if (this.closed)
/* 345 */         return;  this.closed = true;
/* 346 */       Http1Codec.this.sink.writeUtf8("0\r\n\r\n");
/* 347 */       Http1Codec.this.detachTimeout(this.timeout);
/* 348 */       Http1Codec.this.state = 3;
/*     */     }
/*     */   }
/*     */   
/*     */   private abstract class AbstractSource implements Source {
/* 353 */     protected final ForwardingTimeout timeout = new ForwardingTimeout(Http1Codec.this.source.timeout());
/*     */     protected boolean closed;
/* 355 */     protected long bytesRead = 0L;
/*     */     
/*     */     public Timeout timeout() {
/* 358 */       return (Timeout)this.timeout;
/*     */     }
/*     */     
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/*     */       try {
/* 363 */         long read = Http1Codec.this.source.read(sink, byteCount);
/* 364 */         if (read > 0L) {
/* 365 */           this.bytesRead += read;
/*     */         }
/* 367 */         return read;
/* 368 */       } catch (IOException e) {
/* 369 */         endOfInput(false, e);
/* 370 */         throw e;
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     protected final void endOfInput(boolean reuseConnection, IOException e) throws IOException {
/* 379 */       if (Http1Codec.this.state == 6)
/* 380 */         return;  if (Http1Codec.this.state != 5) throw new IllegalStateException("state: " + Http1Codec.this.state);
/*     */       
/* 382 */       Http1Codec.this.detachTimeout(this.timeout);
/*     */       
/* 384 */       Http1Codec.this.state = 6;
/* 385 */       if (Http1Codec.this.streamAllocation != null)
/* 386 */         Http1Codec.this.streamAllocation.streamFinished(!reuseConnection, Http1Codec.this, this.bytesRead, e); 
/*     */     }
/*     */     
/*     */     private AbstractSource() {}
/*     */   }
/*     */   
/*     */   private class FixedLengthSource extends AbstractSource {
/*     */     private long bytesRemaining;
/*     */     
/*     */     FixedLengthSource(long length) throws IOException {
/* 396 */       this.bytesRemaining = length;
/* 397 */       if (this.bytesRemaining == 0L) {
/* 398 */         endOfInput(true, null);
/*     */       }
/*     */     }
/*     */     
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/* 403 */       if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/* 404 */       if (this.closed) throw new IllegalStateException("closed"); 
/* 405 */       if (this.bytesRemaining == 0L) return -1L;
/*     */       
/* 407 */       long read = super.read(sink, Math.min(this.bytesRemaining, byteCount));
/* 408 */       if (read == -1L) {
/* 409 */         ProtocolException e = new ProtocolException("unexpected end of stream");
/* 410 */         endOfInput(false, e);
/* 411 */         throw e;
/*     */       } 
/*     */       
/* 414 */       this.bytesRemaining -= read;
/* 415 */       if (this.bytesRemaining == 0L) {
/* 416 */         endOfInput(true, null);
/*     */       }
/* 418 */       return read;
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 422 */       if (this.closed)
/*     */         return; 
/* 424 */       if (this.bytesRemaining != 0L && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
/* 425 */         endOfInput(false, null);
/*     */       }
/*     */       
/* 428 */       this.closed = true;
/*     */     }
/*     */   }
/*     */   
/*     */   private class ChunkedSource
/*     */     extends AbstractSource {
/*     */     private static final long NO_CHUNK_YET = -1L;
/*     */     private final HttpUrl url;
/* 436 */     private long bytesRemainingInChunk = -1L;
/*     */     private boolean hasMoreChunks = true;
/*     */     
/*     */     ChunkedSource(HttpUrl url) {
/* 440 */       this.url = url;
/*     */     }
/*     */     
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/* 444 */       if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/* 445 */       if (this.closed) throw new IllegalStateException("closed"); 
/* 446 */       if (!this.hasMoreChunks) return -1L;
/*     */       
/* 448 */       if (this.bytesRemainingInChunk == 0L || this.bytesRemainingInChunk == -1L) {
/* 449 */         readChunkSize();
/* 450 */         if (!this.hasMoreChunks) return -1L;
/*     */       
/*     */       } 
/* 453 */       long read = super.read(sink, Math.min(byteCount, this.bytesRemainingInChunk));
/* 454 */       if (read == -1L) {
/* 455 */         ProtocolException e = new ProtocolException("unexpected end of stream");
/* 456 */         endOfInput(false, e);
/* 457 */         throw e;
/*     */       } 
/* 459 */       this.bytesRemainingInChunk -= read;
/* 460 */       return read;
/*     */     }
/*     */ 
/*     */     
/*     */     private void readChunkSize() throws IOException {
/* 465 */       if (this.bytesRemainingInChunk != -1L) {
/* 466 */         Http1Codec.this.source.readUtf8LineStrict();
/*     */       }
/*     */       try {
/* 469 */         this.bytesRemainingInChunk = Http1Codec.this.source.readHexadecimalUnsignedLong();
/* 470 */         String extensions = Http1Codec.this.source.readUtf8LineStrict().trim();
/* 471 */         if (this.bytesRemainingInChunk < 0L || (!extensions.isEmpty() && !extensions.startsWith(";"))) {
/* 472 */           throw new ProtocolException("expected chunk size and optional extensions but was \"" + this.bytesRemainingInChunk + extensions + "\"");
/*     */         }
/*     */       }
/* 475 */       catch (NumberFormatException e) {
/* 476 */         throw new ProtocolException(e.getMessage());
/*     */       } 
/* 478 */       if (this.bytesRemainingInChunk == 0L) {
/* 479 */         this.hasMoreChunks = false;
/* 480 */         HttpHeaders.receiveHeaders(Http1Codec.this.client.cookieJar(), this.url, Http1Codec.this.readHeaders());
/* 481 */         endOfInput(true, null);
/*     */       } 
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 486 */       if (this.closed)
/* 487 */         return;  if (this.hasMoreChunks && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
/* 488 */         endOfInput(false, null);
/*     */       }
/* 490 */       this.closed = true;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private class UnknownLengthSource
/*     */     extends AbstractSource
/*     */   {
/*     */     private boolean inputExhausted;
/*     */ 
/*     */     
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/* 503 */       if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/* 504 */       if (this.closed) throw new IllegalStateException("closed"); 
/* 505 */       if (this.inputExhausted) return -1L;
/*     */       
/* 507 */       long read = super.read(sink, byteCount);
/* 508 */       if (read == -1L) {
/* 509 */         this.inputExhausted = true;
/* 510 */         endOfInput(true, null);
/* 511 */         return -1L;
/*     */       } 
/* 513 */       return read;
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 517 */       if (this.closed)
/* 518 */         return;  if (!this.inputExhausted) {
/* 519 */         endOfInput(false, null);
/*     */       }
/* 521 */       this.closed = true;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http1\Http1Codec.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */