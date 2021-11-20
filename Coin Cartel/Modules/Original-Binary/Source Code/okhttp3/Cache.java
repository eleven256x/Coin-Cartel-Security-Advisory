/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.Flushable;
/*     */ import java.io.IOException;
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.CertificateEncodingException;
/*     */ import java.security.cert.CertificateException;
/*     */ import java.security.cert.CertificateFactory;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.NoSuchElementException;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.cache.CacheRequest;
/*     */ import okhttp3.internal.cache.CacheStrategy;
/*     */ import okhttp3.internal.cache.DiskLruCache;
/*     */ import okhttp3.internal.cache.InternalCache;
/*     */ import okhttp3.internal.http.HttpHeaders;
/*     */ import okhttp3.internal.http.HttpMethod;
/*     */ import okhttp3.internal.http.StatusLine;
/*     */ import okhttp3.internal.io.FileSystem;
/*     */ import okhttp3.internal.platform.Platform;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
/*     */ import okio.BufferedSource;
/*     */ import okio.ByteString;
/*     */ import okio.ForwardingSink;
/*     */ import okio.ForwardingSource;
/*     */ import okio.Okio;
/*     */ import okio.Sink;
/*     */ import okio.Source;
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
/*     */ public final class Cache
/*     */   implements Closeable, Flushable
/*     */ {
/*     */   private static final int VERSION = 201105;
/*     */   private static final int ENTRY_METADATA = 0;
/*     */   private static final int ENTRY_BODY = 1;
/*     */   private static final int ENTRY_COUNT = 2;
/*     */   
/* 143 */   final InternalCache internalCache = new InternalCache() {
/*     */       public Response get(Request request) throws IOException {
/* 145 */         return Cache.this.get(request);
/*     */       }
/*     */       
/*     */       public CacheRequest put(Response response) throws IOException {
/* 149 */         return Cache.this.put(response);
/*     */       }
/*     */       
/*     */       public void remove(Request request) throws IOException {
/* 153 */         Cache.this.remove(request);
/*     */       }
/*     */       
/*     */       public void update(Response cached, Response network) {
/* 157 */         Cache.this.update(cached, network);
/*     */       }
/*     */       
/*     */       public void trackConditionalCacheHit() {
/* 161 */         Cache.this.trackConditionalCacheHit();
/*     */       }
/*     */       
/*     */       public void trackResponse(CacheStrategy cacheStrategy) {
/* 165 */         Cache.this.trackResponse(cacheStrategy);
/*     */       }
/*     */     };
/*     */ 
/*     */   
/*     */   final DiskLruCache cache;
/*     */   
/*     */   int writeSuccessCount;
/*     */   
/*     */   int writeAbortCount;
/*     */   
/*     */   private int networkCount;
/*     */   
/*     */   private int hitCount;
/*     */   private int requestCount;
/*     */   
/*     */   public Cache(File directory, long maxSize) {
/* 182 */     this(directory, maxSize, FileSystem.SYSTEM);
/*     */   }
/*     */   
/*     */   Cache(File directory, long maxSize, FileSystem fileSystem) {
/* 186 */     this.cache = DiskLruCache.create(fileSystem, directory, 201105, 2, maxSize);
/*     */   }
/*     */   
/*     */   public static String key(HttpUrl url) {
/* 190 */     return ByteString.encodeUtf8(url.toString()).md5().hex(); } @Nullable
/*     */   Response get(Request request) {
/*     */     DiskLruCache.Snapshot snapshot;
/*     */     Entry entry;
/* 194 */     String key = key(request.url());
/*     */ 
/*     */     
/*     */     try {
/* 198 */       snapshot = this.cache.get(key);
/* 199 */       if (snapshot == null) {
/* 200 */         return null;
/*     */       }
/* 202 */     } catch (IOException e) {
/*     */       
/* 204 */       return null;
/*     */     } 
/*     */     
/*     */     try {
/* 208 */       entry = new Entry(snapshot.getSource(0));
/* 209 */     } catch (IOException e) {
/* 210 */       Util.closeQuietly((Closeable)snapshot);
/* 211 */       return null;
/*     */     } 
/*     */     
/* 214 */     Response response = entry.response(snapshot);
/*     */     
/* 216 */     if (!entry.matches(request, response)) {
/* 217 */       Util.closeQuietly(response.body());
/* 218 */       return null;
/*     */     } 
/*     */     
/* 221 */     return response;
/*     */   }
/*     */   @Nullable
/*     */   CacheRequest put(Response response) {
/* 225 */     String requestMethod = response.request().method();
/*     */     
/* 227 */     if (HttpMethod.invalidatesCache(response.request().method())) {
/*     */       try {
/* 229 */         remove(response.request());
/* 230 */       } catch (IOException iOException) {}
/*     */ 
/*     */       
/* 233 */       return null;
/*     */     } 
/* 235 */     if (!requestMethod.equals("GET"))
/*     */     {
/*     */ 
/*     */       
/* 239 */       return null;
/*     */     }
/*     */     
/* 242 */     if (HttpHeaders.hasVaryAll(response)) {
/* 243 */       return null;
/*     */     }
/*     */     
/* 246 */     Entry entry = new Entry(response);
/* 247 */     DiskLruCache.Editor editor = null;
/*     */     try {
/* 249 */       editor = this.cache.edit(key(response.request().url()));
/* 250 */       if (editor == null) {
/* 251 */         return null;
/*     */       }
/* 253 */       entry.writeTo(editor);
/* 254 */       return new CacheRequestImpl(editor);
/* 255 */     } catch (IOException e) {
/* 256 */       abortQuietly(editor);
/* 257 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   void remove(Request request) throws IOException {
/* 262 */     this.cache.remove(key(request.url()));
/*     */   }
/*     */   
/*     */   void update(Response cached, Response network) {
/* 266 */     Entry entry = new Entry(network);
/* 267 */     DiskLruCache.Snapshot snapshot = ((CacheResponseBody)cached.body()).snapshot;
/* 268 */     DiskLruCache.Editor editor = null;
/*     */     try {
/* 270 */       editor = snapshot.edit();
/* 271 */       if (editor != null) {
/* 272 */         entry.writeTo(editor);
/* 273 */         editor.commit();
/*     */       } 
/* 275 */     } catch (IOException e) {
/* 276 */       abortQuietly(editor);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void abortQuietly(@Nullable DiskLruCache.Editor editor) {
/*     */     try {
/* 283 */       if (editor != null) {
/* 284 */         editor.abort();
/*     */       }
/* 286 */     } catch (IOException iOException) {}
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
/*     */   public void initialize() throws IOException {
/* 302 */     this.cache.initialize();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void delete() throws IOException {
/* 310 */     this.cache.delete();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void evictAll() throws IOException {
/* 318 */     this.cache.evictAll();
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
/*     */   public Iterator<String> urls() throws IOException {
/* 331 */     return new Iterator<String>() {
/* 332 */         final Iterator<DiskLruCache.Snapshot> delegate = Cache.this.cache.snapshots();
/*     */         @Nullable
/*     */         String nextUrl;
/*     */         boolean canRemove;
/*     */         
/*     */         public boolean hasNext() {
/* 338 */           if (this.nextUrl != null) return true;
/*     */           
/* 340 */           this.canRemove = false;
/* 341 */           while (this.delegate.hasNext()) {
/* 342 */             DiskLruCache.Snapshot snapshot = this.delegate.next();
/*     */             try {
/* 344 */               BufferedSource metadata = Okio.buffer(snapshot.getSource(0));
/* 345 */               this.nextUrl = metadata.readUtf8LineStrict();
/* 346 */               return true;
/* 347 */             } catch (IOException iOException) {
/*     */ 
/*     */             
/*     */             } finally {
/* 351 */               snapshot.close();
/*     */             } 
/*     */           } 
/*     */           
/* 355 */           return false;
/*     */         }
/*     */         
/*     */         public String next() {
/* 359 */           if (!hasNext()) throw new NoSuchElementException(); 
/* 360 */           String result = this.nextUrl;
/* 361 */           this.nextUrl = null;
/* 362 */           this.canRemove = true;
/* 363 */           return result;
/*     */         }
/*     */         
/*     */         public void remove() {
/* 367 */           if (!this.canRemove) throw new IllegalStateException("remove() before next()"); 
/* 368 */           this.delegate.remove();
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   public synchronized int writeAbortCount() {
/* 374 */     return this.writeAbortCount;
/*     */   }
/*     */   
/*     */   public synchronized int writeSuccessCount() {
/* 378 */     return this.writeSuccessCount;
/*     */   }
/*     */   
/*     */   public long size() throws IOException {
/* 382 */     return this.cache.size();
/*     */   }
/*     */ 
/*     */   
/*     */   public long maxSize() {
/* 387 */     return this.cache.getMaxSize();
/*     */   }
/*     */   
/*     */   public void flush() throws IOException {
/* 391 */     this.cache.flush();
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 395 */     this.cache.close();
/*     */   }
/*     */   
/*     */   public File directory() {
/* 399 */     return this.cache.getDirectory();
/*     */   }
/*     */   
/*     */   public boolean isClosed() {
/* 403 */     return this.cache.isClosed();
/*     */   }
/*     */   
/*     */   synchronized void trackResponse(CacheStrategy cacheStrategy) {
/* 407 */     this.requestCount++;
/*     */     
/* 409 */     if (cacheStrategy.networkRequest != null) {
/*     */       
/* 411 */       this.networkCount++;
/* 412 */     } else if (cacheStrategy.cacheResponse != null) {
/*     */       
/* 414 */       this.hitCount++;
/*     */     } 
/*     */   }
/*     */   
/*     */   synchronized void trackConditionalCacheHit() {
/* 419 */     this.hitCount++;
/*     */   }
/*     */   
/*     */   public synchronized int networkCount() {
/* 423 */     return this.networkCount;
/*     */   }
/*     */   
/*     */   public synchronized int hitCount() {
/* 427 */     return this.hitCount;
/*     */   }
/*     */   
/*     */   public synchronized int requestCount() {
/* 431 */     return this.requestCount;
/*     */   }
/*     */   
/*     */   private final class CacheRequestImpl implements CacheRequest {
/*     */     private final DiskLruCache.Editor editor;
/*     */     private Sink cacheOut;
/*     */     private Sink body;
/*     */     boolean done;
/*     */     
/*     */     CacheRequestImpl(final DiskLruCache.Editor editor) {
/* 441 */       this.editor = editor;
/* 442 */       this.cacheOut = editor.newSink(1);
/* 443 */       this.body = (Sink)new ForwardingSink(this.cacheOut) {
/*     */           public void close() throws IOException {
/* 445 */             synchronized (Cache.this) {
/* 446 */               if (Cache.CacheRequestImpl.this.done) {
/*     */                 return;
/*     */               }
/* 449 */               Cache.CacheRequestImpl.this.done = true;
/* 450 */               Cache.this.writeSuccessCount++;
/*     */             } 
/* 452 */             super.close();
/* 453 */             editor.commit();
/*     */           }
/*     */         };
/*     */     }
/*     */     
/*     */     public void abort() {
/* 459 */       synchronized (Cache.this) {
/* 460 */         if (this.done) {
/*     */           return;
/*     */         }
/* 463 */         this.done = true;
/* 464 */         Cache.this.writeAbortCount++;
/*     */       } 
/* 466 */       Util.closeQuietly((Closeable)this.cacheOut);
/*     */       try {
/* 468 */         this.editor.abort();
/* 469 */       } catch (IOException iOException) {}
/*     */     }
/*     */ 
/*     */     
/*     */     public Sink body() {
/* 474 */       return this.body;
/*     */     }
/*     */   }
/*     */   
/*     */   private static final class Entry
/*     */   {
/* 480 */     private static final String SENT_MILLIS = Platform.get().getPrefix() + "-Sent-Millis";
/*     */ 
/*     */     
/* 483 */     private static final String RECEIVED_MILLIS = Platform.get().getPrefix() + "-Received-Millis";
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final String url;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final Headers varyHeaders;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final String requestMethod;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final Protocol protocol;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final int code;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final String message;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final Headers responseHeaders;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     @Nullable
/*     */     private final Handshake handshake;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final long sentRequestMillis;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final long receivedResponseMillis;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     Entry(Source in) throws IOException {
/*     */       try {
/* 546 */         BufferedSource source = Okio.buffer(in);
/* 547 */         this.url = source.readUtf8LineStrict();
/* 548 */         this.requestMethod = source.readUtf8LineStrict();
/* 549 */         Headers.Builder varyHeadersBuilder = new Headers.Builder();
/* 550 */         int varyRequestHeaderLineCount = Cache.readInt(source);
/* 551 */         for (int i = 0; i < varyRequestHeaderLineCount; i++) {
/* 552 */           varyHeadersBuilder.addLenient(source.readUtf8LineStrict());
/*     */         }
/* 554 */         this.varyHeaders = varyHeadersBuilder.build();
/*     */         
/* 556 */         StatusLine statusLine = StatusLine.parse(source.readUtf8LineStrict());
/* 557 */         this.protocol = statusLine.protocol;
/* 558 */         this.code = statusLine.code;
/* 559 */         this.message = statusLine.message;
/* 560 */         Headers.Builder responseHeadersBuilder = new Headers.Builder();
/* 561 */         int responseHeaderLineCount = Cache.readInt(source);
/* 562 */         for (int j = 0; j < responseHeaderLineCount; j++) {
/* 563 */           responseHeadersBuilder.addLenient(source.readUtf8LineStrict());
/*     */         }
/* 565 */         String sendRequestMillisString = responseHeadersBuilder.get(SENT_MILLIS);
/* 566 */         String receivedResponseMillisString = responseHeadersBuilder.get(RECEIVED_MILLIS);
/* 567 */         responseHeadersBuilder.removeAll(SENT_MILLIS);
/* 568 */         responseHeadersBuilder.removeAll(RECEIVED_MILLIS);
/* 569 */         this
/*     */           
/* 571 */           .sentRequestMillis = (sendRequestMillisString != null) ? Long.parseLong(sendRequestMillisString) : 0L;
/* 572 */         this
/*     */           
/* 574 */           .receivedResponseMillis = (receivedResponseMillisString != null) ? Long.parseLong(receivedResponseMillisString) : 0L;
/* 575 */         this.responseHeaders = responseHeadersBuilder.build();
/*     */         
/* 577 */         if (isHttps()) {
/* 578 */           String blank = source.readUtf8LineStrict();
/* 579 */           if (blank.length() > 0) {
/* 580 */             throw new IOException("expected \"\" but was \"" + blank + "\"");
/*     */           }
/* 582 */           String cipherSuiteString = source.readUtf8LineStrict();
/* 583 */           CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
/* 584 */           List<Certificate> peerCertificates = readCertificateList(source);
/* 585 */           List<Certificate> localCertificates = readCertificateList(source);
/*     */ 
/*     */           
/* 588 */           TlsVersion tlsVersion = !source.exhausted() ? TlsVersion.forJavaName(source.readUtf8LineStrict()) : TlsVersion.SSL_3_0;
/* 589 */           this.handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates);
/*     */         } else {
/* 591 */           this.handshake = null;
/*     */         } 
/*     */       } finally {
/* 594 */         in.close();
/*     */       } 
/*     */     }
/*     */     
/*     */     Entry(Response response) {
/* 599 */       this.url = response.request().url().toString();
/* 600 */       this.varyHeaders = HttpHeaders.varyHeaders(response);
/* 601 */       this.requestMethod = response.request().method();
/* 602 */       this.protocol = response.protocol();
/* 603 */       this.code = response.code();
/* 604 */       this.message = response.message();
/* 605 */       this.responseHeaders = response.headers();
/* 606 */       this.handshake = response.handshake();
/* 607 */       this.sentRequestMillis = response.sentRequestAtMillis();
/* 608 */       this.receivedResponseMillis = response.receivedResponseAtMillis();
/*     */     }
/*     */     
/*     */     public void writeTo(DiskLruCache.Editor editor) throws IOException {
/* 612 */       BufferedSink sink = Okio.buffer(editor.newSink(0));
/*     */       
/* 614 */       sink.writeUtf8(this.url)
/* 615 */         .writeByte(10);
/* 616 */       sink.writeUtf8(this.requestMethod)
/* 617 */         .writeByte(10);
/* 618 */       sink.writeDecimalLong(this.varyHeaders.size())
/* 619 */         .writeByte(10); int i, size;
/* 620 */       for (i = 0, size = this.varyHeaders.size(); i < size; i++) {
/* 621 */         sink.writeUtf8(this.varyHeaders.name(i))
/* 622 */           .writeUtf8(": ")
/* 623 */           .writeUtf8(this.varyHeaders.value(i))
/* 624 */           .writeByte(10);
/*     */       }
/*     */       
/* 627 */       sink.writeUtf8((new StatusLine(this.protocol, this.code, this.message)).toString())
/* 628 */         .writeByte(10);
/* 629 */       sink.writeDecimalLong((this.responseHeaders.size() + 2))
/* 630 */         .writeByte(10);
/* 631 */       for (i = 0, size = this.responseHeaders.size(); i < size; i++) {
/* 632 */         sink.writeUtf8(this.responseHeaders.name(i))
/* 633 */           .writeUtf8(": ")
/* 634 */           .writeUtf8(this.responseHeaders.value(i))
/* 635 */           .writeByte(10);
/*     */       }
/* 637 */       sink.writeUtf8(SENT_MILLIS)
/* 638 */         .writeUtf8(": ")
/* 639 */         .writeDecimalLong(this.sentRequestMillis)
/* 640 */         .writeByte(10);
/* 641 */       sink.writeUtf8(RECEIVED_MILLIS)
/* 642 */         .writeUtf8(": ")
/* 643 */         .writeDecimalLong(this.receivedResponseMillis)
/* 644 */         .writeByte(10);
/*     */       
/* 646 */       if (isHttps()) {
/* 647 */         sink.writeByte(10);
/* 648 */         sink.writeUtf8(this.handshake.cipherSuite().javaName())
/* 649 */           .writeByte(10);
/* 650 */         writeCertList(sink, this.handshake.peerCertificates());
/* 651 */         writeCertList(sink, this.handshake.localCertificates());
/* 652 */         sink.writeUtf8(this.handshake.tlsVersion().javaName()).writeByte(10);
/*     */       } 
/* 654 */       sink.close();
/*     */     }
/*     */     
/*     */     private boolean isHttps() {
/* 658 */       return this.url.startsWith("https://");
/*     */     }
/*     */     
/*     */     private List<Certificate> readCertificateList(BufferedSource source) throws IOException {
/* 662 */       int length = Cache.readInt(source);
/* 663 */       if (length == -1) return Collections.emptyList();
/*     */       
/*     */       try {
/* 666 */         CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
/* 667 */         List<Certificate> result = new ArrayList<>(length);
/* 668 */         for (int i = 0; i < length; i++) {
/* 669 */           String line = source.readUtf8LineStrict();
/* 670 */           Buffer bytes = new Buffer();
/* 671 */           bytes.write(ByteString.decodeBase64(line));
/* 672 */           result.add(certificateFactory.generateCertificate(bytes.inputStream()));
/*     */         } 
/* 674 */         return result;
/* 675 */       } catch (CertificateException e) {
/* 676 */         throw new IOException(e.getMessage());
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     private void writeCertList(BufferedSink sink, List<Certificate> certificates) throws IOException {
/*     */       try {
/* 683 */         sink.writeDecimalLong(certificates.size())
/* 684 */           .writeByte(10);
/* 685 */         for (int i = 0, size = certificates.size(); i < size; i++) {
/* 686 */           byte[] bytes = ((Certificate)certificates.get(i)).getEncoded();
/* 687 */           String line = ByteString.of(bytes).base64();
/* 688 */           sink.writeUtf8(line)
/* 689 */             .writeByte(10);
/*     */         } 
/* 691 */       } catch (CertificateEncodingException e) {
/* 692 */         throw new IOException(e.getMessage());
/*     */       } 
/*     */     }
/*     */     
/*     */     public boolean matches(Request request, Response response) {
/* 697 */       return (this.url.equals(request.url().toString()) && this.requestMethod
/* 698 */         .equals(request.method()) && 
/* 699 */         HttpHeaders.varyMatches(response, this.varyHeaders, request));
/*     */     }
/*     */     
/*     */     public Response response(DiskLruCache.Snapshot snapshot) {
/* 703 */       String contentType = this.responseHeaders.get("Content-Type");
/* 704 */       String contentLength = this.responseHeaders.get("Content-Length");
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 709 */       Request cacheRequest = (new Request.Builder()).url(this.url).method(this.requestMethod, null).headers(this.varyHeaders).build();
/* 710 */       return (new Response.Builder())
/* 711 */         .request(cacheRequest)
/* 712 */         .protocol(this.protocol)
/* 713 */         .code(this.code)
/* 714 */         .message(this.message)
/* 715 */         .headers(this.responseHeaders)
/* 716 */         .body(new Cache.CacheResponseBody(snapshot, contentType, contentLength))
/* 717 */         .handshake(this.handshake)
/* 718 */         .sentRequestAtMillis(this.sentRequestMillis)
/* 719 */         .receivedResponseAtMillis(this.receivedResponseMillis)
/* 720 */         .build();
/*     */     }
/*     */   }
/*     */   
/*     */   static int readInt(BufferedSource source) throws IOException {
/*     */     try {
/* 726 */       long result = source.readDecimalLong();
/* 727 */       String line = source.readUtf8LineStrict();
/* 728 */       if (result < 0L || result > 2147483647L || !line.isEmpty()) {
/* 729 */         throw new IOException("expected an int but was \"" + result + line + "\"");
/*     */       }
/* 731 */       return (int)result;
/* 732 */     } catch (NumberFormatException e) {
/* 733 */       throw new IOException(e.getMessage());
/*     */     } 
/*     */   }
/*     */   
/*     */   private static class CacheResponseBody extends ResponseBody { final DiskLruCache.Snapshot snapshot;
/*     */     private final BufferedSource bodySource;
/*     */     @Nullable
/*     */     private final String contentType;
/*     */     @Nullable
/*     */     private final String contentLength;
/*     */     
/*     */     CacheResponseBody(final DiskLruCache.Snapshot snapshot, String contentType, String contentLength) {
/* 745 */       this.snapshot = snapshot;
/* 746 */       this.contentType = contentType;
/* 747 */       this.contentLength = contentLength;
/*     */       
/* 749 */       Source source = snapshot.getSource(1);
/* 750 */       this.bodySource = Okio.buffer((Source)new ForwardingSource(source) {
/*     */             public void close() throws IOException {
/* 752 */               snapshot.close();
/* 753 */               super.close();
/*     */             }
/*     */           });
/*     */     }
/*     */     
/*     */     public MediaType contentType() {
/* 759 */       return (this.contentType != null) ? MediaType.parse(this.contentType) : null;
/*     */     }
/*     */     
/*     */     public long contentLength() {
/*     */       try {
/* 764 */         return (this.contentLength != null) ? Long.parseLong(this.contentLength) : -1L;
/* 765 */       } catch (NumberFormatException e) {
/* 766 */         return -1L;
/*     */       } 
/*     */     }
/*     */     
/*     */     public BufferedSource source() {
/* 771 */       return this.bodySource;
/*     */     } }
/*     */ 
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Cache.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */