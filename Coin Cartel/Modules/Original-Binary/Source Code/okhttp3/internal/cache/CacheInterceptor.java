/*     */ package okhttp3.internal.cache;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.ResponseBody;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.http.HttpHeaders;
/*     */ import okhttp3.internal.http.HttpMethod;
/*     */ import okhttp3.internal.http.RealResponseBody;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
/*     */ import okio.BufferedSource;
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
/*     */ public final class CacheInterceptor
/*     */   implements Interceptor
/*     */ {
/*     */   final InternalCache cache;
/*     */   
/*     */   public CacheInterceptor(InternalCache cache) {
/*  49 */     this.cache = cache;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Response intercept(Interceptor.Chain chain) throws IOException {
/*  55 */     Response cacheCandidate = (this.cache != null) ? this.cache.get(chain.request()) : null;
/*     */     
/*  57 */     long now = System.currentTimeMillis();
/*     */     
/*  59 */     CacheStrategy strategy = (new CacheStrategy.Factory(now, chain.request(), cacheCandidate)).get();
/*  60 */     Request networkRequest = strategy.networkRequest;
/*  61 */     Response cacheResponse = strategy.cacheResponse;
/*     */     
/*  63 */     if (this.cache != null) {
/*  64 */       this.cache.trackResponse(strategy);
/*     */     }
/*     */     
/*  67 */     if (cacheCandidate != null && cacheResponse == null) {
/*  68 */       Util.closeQuietly((Closeable)cacheCandidate.body());
/*     */     }
/*     */ 
/*     */     
/*  72 */     if (networkRequest == null && cacheResponse == null) {
/*  73 */       return (new Response.Builder())
/*  74 */         .request(chain.request())
/*  75 */         .protocol(Protocol.HTTP_1_1)
/*  76 */         .code(504)
/*  77 */         .message("Unsatisfiable Request (only-if-cached)")
/*  78 */         .body(Util.EMPTY_RESPONSE)
/*  79 */         .sentRequestAtMillis(-1L)
/*  80 */         .receivedResponseAtMillis(System.currentTimeMillis())
/*  81 */         .build();
/*     */     }
/*     */ 
/*     */     
/*  85 */     if (networkRequest == null) {
/*  86 */       return cacheResponse.newBuilder()
/*  87 */         .cacheResponse(stripBody(cacheResponse))
/*  88 */         .build();
/*     */     }
/*     */     
/*  91 */     Response networkResponse = null;
/*     */     try {
/*  93 */       networkResponse = chain.proceed(networkRequest);
/*     */     } finally {
/*     */       
/*  96 */       if (networkResponse == null && cacheCandidate != null) {
/*  97 */         Util.closeQuietly((Closeable)cacheCandidate.body());
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 102 */     if (cacheResponse != null) {
/* 103 */       if (networkResponse.code() == 304) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 110 */         Response response1 = cacheResponse.newBuilder().headers(combine(cacheResponse.headers(), networkResponse.headers())).sentRequestAtMillis(networkResponse.sentRequestAtMillis()).receivedResponseAtMillis(networkResponse.receivedResponseAtMillis()).cacheResponse(stripBody(cacheResponse)).networkResponse(stripBody(networkResponse)).build();
/* 111 */         networkResponse.body().close();
/*     */ 
/*     */ 
/*     */         
/* 115 */         this.cache.trackConditionalCacheHit();
/* 116 */         this.cache.update(cacheResponse, response1);
/* 117 */         return response1;
/*     */       } 
/* 119 */       Util.closeQuietly((Closeable)cacheResponse.body());
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 126 */     Response response = networkResponse.newBuilder().cacheResponse(stripBody(cacheResponse)).networkResponse(stripBody(networkResponse)).build();
/*     */     
/* 128 */     if (this.cache != null) {
/* 129 */       if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
/*     */         
/* 131 */         CacheRequest cacheRequest = this.cache.put(response);
/* 132 */         return cacheWritingResponse(cacheRequest, response);
/*     */       } 
/*     */       
/* 135 */       if (HttpMethod.invalidatesCache(networkRequest.method())) {
/*     */         try {
/* 137 */           this.cache.remove(networkRequest);
/* 138 */         } catch (IOException iOException) {}
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 144 */     return response;
/*     */   }
/*     */   
/*     */   private static Response stripBody(Response response) {
/* 148 */     return (response != null && response.body() != null) ? 
/* 149 */       response.newBuilder().body(null).build() : 
/* 150 */       response;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response) throws IOException {
/* 161 */     if (cacheRequest == null) return response; 
/* 162 */     Sink cacheBodyUnbuffered = cacheRequest.body();
/* 163 */     if (cacheBodyUnbuffered == null) return response;
/*     */     
/* 165 */     final BufferedSource source = response.body().source();
/* 166 */     final BufferedSink cacheBody = Okio.buffer(cacheBodyUnbuffered);
/*     */     
/* 168 */     Source cacheWritingSource = new Source() {
/*     */         boolean cacheRequestClosed;
/*     */         
/*     */         public long read(Buffer sink, long byteCount) throws IOException {
/*     */           long bytesRead;
/*     */           try {
/* 174 */             bytesRead = source.read(sink, byteCount);
/* 175 */           } catch (IOException e) {
/* 176 */             if (!this.cacheRequestClosed) {
/* 177 */               this.cacheRequestClosed = true;
/* 178 */               cacheRequest.abort();
/*     */             } 
/* 180 */             throw e;
/*     */           } 
/*     */           
/* 183 */           if (bytesRead == -1L) {
/* 184 */             if (!this.cacheRequestClosed) {
/* 185 */               this.cacheRequestClosed = true;
/* 186 */               cacheBody.close();
/*     */             } 
/* 188 */             return -1L;
/*     */           } 
/*     */           
/* 191 */           sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
/* 192 */           cacheBody.emitCompleteSegments();
/* 193 */           return bytesRead;
/*     */         }
/*     */         
/*     */         public Timeout timeout() {
/* 197 */           return source.timeout();
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/* 201 */           if (!this.cacheRequestClosed && 
/* 202 */             !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
/* 203 */             this.cacheRequestClosed = true;
/* 204 */             cacheRequest.abort();
/*     */           } 
/* 206 */           source.close();
/*     */         }
/*     */       };
/*     */     
/* 210 */     String contentType = response.header("Content-Type");
/* 211 */     long contentLength = response.body().contentLength();
/* 212 */     return response.newBuilder()
/* 213 */       .body((ResponseBody)new RealResponseBody(contentType, contentLength, Okio.buffer(cacheWritingSource)))
/* 214 */       .build();
/*     */   }
/*     */ 
/*     */   
/*     */   private static Headers combine(Headers cachedHeaders, Headers networkHeaders) {
/* 219 */     Headers.Builder result = new Headers.Builder();
/*     */     int i, size;
/* 221 */     for (i = 0, size = cachedHeaders.size(); i < size; i++) {
/* 222 */       String fieldName = cachedHeaders.name(i);
/* 223 */       String value = cachedHeaders.value(i);
/* 224 */       if (!"Warning".equalsIgnoreCase(fieldName) || !value.startsWith("1"))
/*     */       {
/*     */         
/* 227 */         if (isContentSpecificHeader(fieldName) || !isEndToEnd(fieldName) || networkHeaders
/* 228 */           .get(fieldName) == null) {
/* 229 */           Internal.instance.addLenient(result, fieldName, value);
/*     */         }
/*     */       }
/*     */     } 
/* 233 */     for (i = 0, size = networkHeaders.size(); i < size; i++) {
/* 234 */       String fieldName = networkHeaders.name(i);
/* 235 */       if (!isContentSpecificHeader(fieldName) && isEndToEnd(fieldName)) {
/* 236 */         Internal.instance.addLenient(result, fieldName, networkHeaders.value(i));
/*     */       }
/*     */     } 
/*     */     
/* 240 */     return result.build();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static boolean isEndToEnd(String fieldName) {
/* 248 */     return (!"Connection".equalsIgnoreCase(fieldName) && 
/* 249 */       !"Keep-Alive".equalsIgnoreCase(fieldName) && 
/* 250 */       !"Proxy-Authenticate".equalsIgnoreCase(fieldName) && 
/* 251 */       !"Proxy-Authorization".equalsIgnoreCase(fieldName) && 
/* 252 */       !"TE".equalsIgnoreCase(fieldName) && 
/* 253 */       !"Trailers".equalsIgnoreCase(fieldName) && 
/* 254 */       !"Transfer-Encoding".equalsIgnoreCase(fieldName) && 
/* 255 */       !"Upgrade".equalsIgnoreCase(fieldName));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static boolean isContentSpecificHeader(String fieldName) {
/* 263 */     return ("Content-Length".equalsIgnoreCase(fieldName) || "Content-Encoding"
/* 264 */       .equalsIgnoreCase(fieldName) || "Content-Type"
/* 265 */       .equalsIgnoreCase(fieldName));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\cache\CacheInterceptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */