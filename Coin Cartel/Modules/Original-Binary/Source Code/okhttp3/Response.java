/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.http.HttpHeaders;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSource;
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
/*     */ public final class Response
/*     */   implements Closeable
/*     */ {
/*     */   final Request request;
/*     */   final Protocol protocol;
/*     */   final int code;
/*     */   final String message;
/*     */   @Nullable
/*     */   final Handshake handshake;
/*     */   final Headers headers;
/*     */   @Nullable
/*     */   final ResponseBody body;
/*     */   @Nullable
/*     */   final Response networkResponse;
/*     */   @Nullable
/*     */   final Response cacheResponse;
/*     */   @Nullable
/*     */   final Response priorResponse;
/*     */   final long sentRequestAtMillis;
/*     */   final long receivedResponseAtMillis;
/*     */   @Nullable
/*     */   private volatile CacheControl cacheControl;
/*     */   
/*     */   Response(Builder builder) {
/*  60 */     this.request = builder.request;
/*  61 */     this.protocol = builder.protocol;
/*  62 */     this.code = builder.code;
/*  63 */     this.message = builder.message;
/*  64 */     this.handshake = builder.handshake;
/*  65 */     this.headers = builder.headers.build();
/*  66 */     this.body = builder.body;
/*  67 */     this.networkResponse = builder.networkResponse;
/*  68 */     this.cacheResponse = builder.cacheResponse;
/*  69 */     this.priorResponse = builder.priorResponse;
/*  70 */     this.sentRequestAtMillis = builder.sentRequestAtMillis;
/*  71 */     this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
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
/*     */   public Request request() {
/*  86 */     return this.request;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Protocol protocol() {
/*  93 */     return this.protocol;
/*     */   }
/*     */ 
/*     */   
/*     */   public int code() {
/*  98 */     return this.code;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isSuccessful() {
/* 106 */     return (this.code >= 200 && this.code < 300);
/*     */   }
/*     */ 
/*     */   
/*     */   public String message() {
/* 111 */     return this.message;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Handshake handshake() {
/* 119 */     return this.handshake;
/*     */   }
/*     */   
/*     */   public List<String> headers(String name) {
/* 123 */     return this.headers.values(name);
/*     */   }
/*     */   @Nullable
/*     */   public String header(String name) {
/* 127 */     return header(name, null);
/*     */   }
/*     */   @Nullable
/*     */   public String header(String name, @Nullable String defaultValue) {
/* 131 */     String result = this.headers.get(name);
/* 132 */     return (result != null) ? result : defaultValue;
/*     */   }
/*     */   
/*     */   public Headers headers() {
/* 136 */     return this.headers;
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
/*     */   public ResponseBody peekBody(long byteCount) throws IOException {
/*     */     Buffer result;
/* 151 */     BufferedSource source = this.body.source();
/* 152 */     source.request(byteCount);
/* 153 */     Buffer copy = source.buffer().clone();
/*     */ 
/*     */ 
/*     */     
/* 157 */     if (copy.size() > byteCount) {
/* 158 */       result = new Buffer();
/* 159 */       result.write(copy, byteCount);
/* 160 */       copy.clear();
/*     */     } else {
/* 162 */       result = copy;
/*     */     } 
/*     */     
/* 165 */     return ResponseBody.create(this.body.contentType(), result.size(), (BufferedSource)result);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public ResponseBody body() {
/* 177 */     return this.body;
/*     */   }
/*     */   
/*     */   public Builder newBuilder() {
/* 181 */     return new Builder(this);
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean isRedirect() {
/* 186 */     switch (this.code) {
/*     */       case 300:
/*     */       case 301:
/*     */       case 302:
/*     */       case 303:
/*     */       case 307:
/*     */       case 308:
/* 193 */         return true;
/*     */     } 
/* 195 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Response networkResponse() {
/* 205 */     return this.networkResponse;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Response cacheResponse() {
/* 214 */     return this.cacheResponse;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Response priorResponse() {
/* 224 */     return this.priorResponse;
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
/*     */   public List<Challenge> challenges() {
/*     */     String responseField;
/* 240 */     if (this.code == 401) {
/* 241 */       responseField = "WWW-Authenticate";
/* 242 */     } else if (this.code == 407) {
/* 243 */       responseField = "Proxy-Authenticate";
/*     */     } else {
/* 245 */       return Collections.emptyList();
/*     */     } 
/* 247 */     return HttpHeaders.parseChallenges(headers(), responseField);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public CacheControl cacheControl() {
/* 255 */     CacheControl result = this.cacheControl;
/* 256 */     return (result != null) ? result : (this.cacheControl = CacheControl.parse(this.headers));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public long sentRequestAtMillis() {
/* 265 */     return this.sentRequestAtMillis;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public long receivedResponseAtMillis() {
/* 274 */     return this.receivedResponseAtMillis;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void close() {
/* 285 */     if (this.body == null) {
/* 286 */       throw new IllegalStateException("response is not eligible for a body and must not be closed");
/*     */     }
/* 288 */     this.body.close();
/*     */   }
/*     */   
/*     */   public String toString() {
/* 292 */     return "Response{protocol=" + this.protocol + ", code=" + this.code + ", message=" + this.message + ", url=" + this.request
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 299 */       .url() + '}';
/*     */   }
/*     */   
/*     */   public static class Builder { @Nullable
/*     */     Request request;
/*     */     @Nullable
/*     */     Protocol protocol;
/* 306 */     int code = -1;
/*     */     
/*     */     String message;
/*     */     
/*     */     @Nullable
/*     */     Handshake handshake;
/*     */     
/*     */     Headers.Builder headers;
/*     */     @Nullable
/*     */     ResponseBody body;
/*     */     
/*     */     public Builder() {
/* 318 */       this.headers = new Headers.Builder(); } @Nullable
/*     */     Response networkResponse; @Nullable
/*     */     Response cacheResponse; @Nullable
/*     */     Response priorResponse; long sentRequestAtMillis; long receivedResponseAtMillis; Builder(Response response) {
/* 322 */       this.request = response.request;
/* 323 */       this.protocol = response.protocol;
/* 324 */       this.code = response.code;
/* 325 */       this.message = response.message;
/* 326 */       this.handshake = response.handshake;
/* 327 */       this.headers = response.headers.newBuilder();
/* 328 */       this.body = response.body;
/* 329 */       this.networkResponse = response.networkResponse;
/* 330 */       this.cacheResponse = response.cacheResponse;
/* 331 */       this.priorResponse = response.priorResponse;
/* 332 */       this.sentRequestAtMillis = response.sentRequestAtMillis;
/* 333 */       this.receivedResponseAtMillis = response.receivedResponseAtMillis;
/*     */     }
/*     */     
/*     */     public Builder request(Request request) {
/* 337 */       this.request = request;
/* 338 */       return this;
/*     */     }
/*     */     
/*     */     public Builder protocol(Protocol protocol) {
/* 342 */       this.protocol = protocol;
/* 343 */       return this;
/*     */     }
/*     */     
/*     */     public Builder code(int code) {
/* 347 */       this.code = code;
/* 348 */       return this;
/*     */     }
/*     */     
/*     */     public Builder message(String message) {
/* 352 */       this.message = message;
/* 353 */       return this;
/*     */     }
/*     */     
/*     */     public Builder handshake(@Nullable Handshake handshake) {
/* 357 */       this.handshake = handshake;
/* 358 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder header(String name, String value) {
/* 366 */       this.headers.set(name, value);
/* 367 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder addHeader(String name, String value) {
/* 375 */       this.headers.add(name, value);
/* 376 */       return this;
/*     */     }
/*     */     
/*     */     public Builder removeHeader(String name) {
/* 380 */       this.headers.removeAll(name);
/* 381 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public Builder headers(Headers headers) {
/* 386 */       this.headers = headers.newBuilder();
/* 387 */       return this;
/*     */     }
/*     */     
/*     */     public Builder body(@Nullable ResponseBody body) {
/* 391 */       this.body = body;
/* 392 */       return this;
/*     */     }
/*     */     
/*     */     public Builder networkResponse(@Nullable Response networkResponse) {
/* 396 */       if (networkResponse != null) checkSupportResponse("networkResponse", networkResponse); 
/* 397 */       this.networkResponse = networkResponse;
/* 398 */       return this;
/*     */     }
/*     */     
/*     */     public Builder cacheResponse(@Nullable Response cacheResponse) {
/* 402 */       if (cacheResponse != null) checkSupportResponse("cacheResponse", cacheResponse); 
/* 403 */       this.cacheResponse = cacheResponse;
/* 404 */       return this;
/*     */     }
/*     */     
/*     */     private void checkSupportResponse(String name, Response response) {
/* 408 */       if (response.body != null)
/* 409 */         throw new IllegalArgumentException(name + ".body != null"); 
/* 410 */       if (response.networkResponse != null)
/* 411 */         throw new IllegalArgumentException(name + ".networkResponse != null"); 
/* 412 */       if (response.cacheResponse != null)
/* 413 */         throw new IllegalArgumentException(name + ".cacheResponse != null"); 
/* 414 */       if (response.priorResponse != null) {
/* 415 */         throw new IllegalArgumentException(name + ".priorResponse != null");
/*     */       }
/*     */     }
/*     */     
/*     */     public Builder priorResponse(@Nullable Response priorResponse) {
/* 420 */       if (priorResponse != null) checkPriorResponse(priorResponse); 
/* 421 */       this.priorResponse = priorResponse;
/* 422 */       return this;
/*     */     }
/*     */     
/*     */     private void checkPriorResponse(Response response) {
/* 426 */       if (response.body != null) {
/* 427 */         throw new IllegalArgumentException("priorResponse.body != null");
/*     */       }
/*     */     }
/*     */     
/*     */     public Builder sentRequestAtMillis(long sentRequestAtMillis) {
/* 432 */       this.sentRequestAtMillis = sentRequestAtMillis;
/* 433 */       return this;
/*     */     }
/*     */     
/*     */     public Builder receivedResponseAtMillis(long receivedResponseAtMillis) {
/* 437 */       this.receivedResponseAtMillis = receivedResponseAtMillis;
/* 438 */       return this;
/*     */     }
/*     */     
/*     */     public Response build() {
/* 442 */       if (this.request == null) throw new IllegalStateException("request == null"); 
/* 443 */       if (this.protocol == null) throw new IllegalStateException("protocol == null"); 
/* 444 */       if (this.code < 0) throw new IllegalStateException("code < 0: " + this.code); 
/* 445 */       if (this.message == null) throw new IllegalStateException("message == null"); 
/* 446 */       return new Response(this);
/*     */     } }
/*     */ 
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Response.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */