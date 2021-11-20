/*     */ package okhttp3.internal.http;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import okhttp3.Cookie;
/*     */ import okhttp3.CookieJar;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.MediaType;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.RequestBody;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.Version;
/*     */ import okio.GzipSource;
/*     */ import okio.Okio;
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
/*     */ public final class BridgeInterceptor
/*     */   implements Interceptor
/*     */ {
/*     */   private final CookieJar cookieJar;
/*     */   
/*     */   public BridgeInterceptor(CookieJar cookieJar) {
/*  44 */     this.cookieJar = cookieJar;
/*     */   }
/*     */   
/*     */   public Response intercept(Interceptor.Chain chain) throws IOException {
/*  48 */     Request userRequest = chain.request();
/*  49 */     Request.Builder requestBuilder = userRequest.newBuilder();
/*     */     
/*  51 */     RequestBody body = userRequest.body();
/*  52 */     if (body != null) {
/*  53 */       MediaType contentType = body.contentType();
/*  54 */       if (contentType != null) {
/*  55 */         requestBuilder.header("Content-Type", contentType.toString());
/*     */       }
/*     */       
/*  58 */       long contentLength = body.contentLength();
/*  59 */       if (contentLength != -1L) {
/*  60 */         requestBuilder.header("Content-Length", Long.toString(contentLength));
/*  61 */         requestBuilder.removeHeader("Transfer-Encoding");
/*     */       } else {
/*  63 */         requestBuilder.header("Transfer-Encoding", "chunked");
/*  64 */         requestBuilder.removeHeader("Content-Length");
/*     */       } 
/*     */     } 
/*     */     
/*  68 */     if (userRequest.header("Host") == null) {
/*  69 */       requestBuilder.header("Host", Util.hostHeader(userRequest.url(), false));
/*     */     }
/*     */     
/*  72 */     if (userRequest.header("Connection") == null) {
/*  73 */       requestBuilder.header("Connection", "Keep-Alive");
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*  78 */     boolean transparentGzip = false;
/*  79 */     if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
/*  80 */       transparentGzip = true;
/*  81 */       requestBuilder.header("Accept-Encoding", "gzip");
/*     */     } 
/*     */     
/*  84 */     List<Cookie> cookies = this.cookieJar.loadForRequest(userRequest.url());
/*  85 */     if (!cookies.isEmpty()) {
/*  86 */       requestBuilder.header("Cookie", cookieHeader(cookies));
/*     */     }
/*     */     
/*  89 */     if (userRequest.header("User-Agent") == null) {
/*  90 */       requestBuilder.header("User-Agent", Version.userAgent());
/*     */     }
/*     */     
/*  93 */     Response networkResponse = chain.proceed(requestBuilder.build());
/*     */     
/*  95 */     HttpHeaders.receiveHeaders(this.cookieJar, userRequest.url(), networkResponse.headers());
/*     */ 
/*     */     
/*  98 */     Response.Builder responseBuilder = networkResponse.newBuilder().request(userRequest);
/*     */     
/* 100 */     if (transparentGzip && "gzip"
/* 101 */       .equalsIgnoreCase(networkResponse.header("Content-Encoding")) && 
/* 102 */       HttpHeaders.hasBody(networkResponse)) {
/* 103 */       GzipSource responseBody = new GzipSource((Source)networkResponse.body().source());
/*     */ 
/*     */ 
/*     */       
/* 107 */       Headers strippedHeaders = networkResponse.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build();
/* 108 */       responseBuilder.headers(strippedHeaders);
/* 109 */       String contentType = networkResponse.header("Content-Type");
/* 110 */       responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer((Source)responseBody)));
/*     */     } 
/*     */     
/* 113 */     return responseBuilder.build();
/*     */   }
/*     */ 
/*     */   
/*     */   private String cookieHeader(List<Cookie> cookies) {
/* 118 */     StringBuilder cookieHeader = new StringBuilder();
/* 119 */     for (int i = 0, size = cookies.size(); i < size; i++) {
/* 120 */       if (i > 0) {
/* 121 */         cookieHeader.append("; ");
/*     */       }
/* 123 */       Cookie cookie = cookies.get(i);
/* 124 */       cookieHeader.append(cookie.name()).append('=').append(cookie.value());
/*     */     } 
/* 126 */     return cookieHeader.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\BridgeInterceptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */