/*     */ package okhttp3.internal.http;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.net.HttpRetryException;
/*     */ import java.net.ProtocolException;
/*     */ import java.net.Proxy;
/*     */ import javax.net.ssl.HostnameVerifier;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import okhttp3.Address;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.CertificatePinner;
/*     */ import okhttp3.EventListener;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.RequestBody;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.Route;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.RouteException;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class RetryAndFollowUpInterceptor
/*     */   implements Interceptor
/*     */ {
/*     */   private static final int MAX_FOLLOW_UPS = 20;
/*     */   private final OkHttpClient client;
/*     */   private final boolean forWebSocket;
/*     */   private volatile StreamAllocation streamAllocation;
/*     */   private Object callStackTrace;
/*     */   private volatile boolean canceled;
/*     */   
/*     */   public RetryAndFollowUpInterceptor(OkHttpClient client, boolean forWebSocket) {
/*  75 */     this.client = client;
/*  76 */     this.forWebSocket = forWebSocket;
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
/*     */   public void cancel() {
/*  89 */     this.canceled = true;
/*  90 */     StreamAllocation streamAllocation = this.streamAllocation;
/*  91 */     if (streamAllocation != null) streamAllocation.cancel(); 
/*     */   }
/*     */   
/*     */   public boolean isCanceled() {
/*  95 */     return this.canceled;
/*     */   }
/*     */   
/*     */   public void setCallStackTrace(Object callStackTrace) {
/*  99 */     this.callStackTrace = callStackTrace;
/*     */   }
/*     */   
/*     */   public StreamAllocation streamAllocation() {
/* 103 */     return this.streamAllocation;
/*     */   }
/*     */   
/*     */   public Response intercept(Interceptor.Chain chain) throws IOException {
/* 107 */     Request request = chain.request();
/* 108 */     RealInterceptorChain realChain = (RealInterceptorChain)chain;
/* 109 */     Call call = realChain.call();
/* 110 */     EventListener eventListener = realChain.eventListener();
/*     */ 
/*     */     
/* 113 */     StreamAllocation streamAllocation = new StreamAllocation(this.client.connectionPool(), createAddress(request.url()), call, eventListener, this.callStackTrace);
/* 114 */     this.streamAllocation = streamAllocation;
/*     */     
/* 116 */     int followUpCount = 0;
/* 117 */     Response priorResponse = null; while (true) {
/*     */       Response response; Request followUp;
/* 119 */       if (this.canceled) {
/* 120 */         streamAllocation.release();
/* 121 */         throw new IOException("Canceled");
/*     */       } 
/*     */ 
/*     */       
/* 125 */       boolean releaseConnection = true;
/*     */       try {
/* 127 */         response = realChain.proceed(request, streamAllocation, null, null);
/* 128 */         releaseConnection = false;
/* 129 */       } catch (RouteException e) {
/*     */         
/* 131 */         if (!recover(e.getLastConnectException(), streamAllocation, false, request)) {
/* 132 */           throw e.getFirstConnectException();
/*     */         }
/* 134 */         releaseConnection = false;
/*     */         continue;
/* 136 */       } catch (IOException e) {
/*     */         
/* 138 */         boolean requestSendStarted = !(e instanceof okhttp3.internal.http2.ConnectionShutdownException);
/* 139 */         if (!recover(e, streamAllocation, requestSendStarted, request)) throw e; 
/* 140 */         releaseConnection = false;
/*     */         
/*     */         continue;
/*     */       } finally {
/* 144 */         if (releaseConnection) {
/* 145 */           streamAllocation.streamFailed(null);
/* 146 */           streamAllocation.release();
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 151 */       if (priorResponse != null)
/*     */       {
/*     */ 
/*     */ 
/*     */         
/* 156 */         response = response.newBuilder().priorResponse(priorResponse.newBuilder().body(null).build()).build();
/*     */       }
/*     */ 
/*     */       
/*     */       try {
/* 161 */         followUp = followUpRequest(response, streamAllocation.route());
/* 162 */       } catch (IOException e) {
/* 163 */         streamAllocation.release();
/* 164 */         throw e;
/*     */       } 
/*     */       
/* 167 */       if (followUp == null) {
/* 168 */         streamAllocation.release();
/* 169 */         return response;
/*     */       } 
/*     */       
/* 172 */       Util.closeQuietly((Closeable)response.body());
/*     */       
/* 174 */       if (++followUpCount > 20) {
/* 175 */         streamAllocation.release();
/* 176 */         throw new ProtocolException("Too many follow-up requests: " + followUpCount);
/*     */       } 
/*     */       
/* 179 */       if (followUp.body() instanceof UnrepeatableRequestBody) {
/* 180 */         streamAllocation.release();
/* 181 */         throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
/*     */       } 
/*     */       
/* 184 */       if (!sameConnection(response, followUp.url())) {
/* 185 */         streamAllocation.release();
/*     */         
/* 187 */         streamAllocation = new StreamAllocation(this.client.connectionPool(), createAddress(followUp.url()), call, eventListener, this.callStackTrace);
/* 188 */         this.streamAllocation = streamAllocation;
/* 189 */       } else if (streamAllocation.codec() != null) {
/* 190 */         throw new IllegalStateException("Closing the body of " + response + " didn't close its backing stream. Bad interceptor?");
/*     */       } 
/*     */ 
/*     */       
/* 194 */       request = followUp;
/* 195 */       priorResponse = response;
/*     */     } 
/*     */   }
/*     */   
/*     */   private Address createAddress(HttpUrl url) {
/* 200 */     SSLSocketFactory sslSocketFactory = null;
/* 201 */     HostnameVerifier hostnameVerifier = null;
/* 202 */     CertificatePinner certificatePinner = null;
/* 203 */     if (url.isHttps()) {
/* 204 */       sslSocketFactory = this.client.sslSocketFactory();
/* 205 */       hostnameVerifier = this.client.hostnameVerifier();
/* 206 */       certificatePinner = this.client.certificatePinner();
/*     */     } 
/*     */     
/* 209 */     return new Address(url.host(), url.port(), this.client.dns(), this.client.socketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, this.client
/* 210 */         .proxyAuthenticator(), this.client
/* 211 */         .proxy(), this.client.protocols(), this.client.connectionSpecs(), this.client.proxySelector());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean recover(IOException e, StreamAllocation streamAllocation, boolean requestSendStarted, Request userRequest) {
/* 222 */     streamAllocation.streamFailed(e);
/*     */ 
/*     */     
/* 225 */     if (!this.client.retryOnConnectionFailure()) return false;
/*     */ 
/*     */     
/* 228 */     if (requestSendStarted && requestIsUnrepeatable(e, userRequest)) return false;
/*     */ 
/*     */     
/* 231 */     if (!isRecoverable(e, requestSendStarted)) return false;
/*     */ 
/*     */     
/* 234 */     if (!streamAllocation.hasMoreRoutes()) return false;
/*     */ 
/*     */     
/* 237 */     return true;
/*     */   }
/*     */   
/*     */   private boolean requestIsUnrepeatable(IOException e, Request userRequest) {
/* 241 */     return (userRequest.body() instanceof UnrepeatableRequestBody || e instanceof java.io.FileNotFoundException);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean isRecoverable(IOException e, boolean requestSendStarted) {
/* 247 */     if (e instanceof ProtocolException) {
/* 248 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 253 */     if (e instanceof java.io.InterruptedIOException) {
/* 254 */       return (e instanceof java.net.SocketTimeoutException && !requestSendStarted);
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 259 */     if (e instanceof javax.net.ssl.SSLHandshakeException)
/*     */     {
/*     */       
/* 262 */       if (e.getCause() instanceof java.security.cert.CertificateException) {
/* 263 */         return false;
/*     */       }
/*     */     }
/* 266 */     if (e instanceof javax.net.ssl.SSLPeerUnverifiedException)
/*     */     {
/* 268 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 274 */     return true;
/*     */   }
/*     */   
/*     */   private Request followUpRequest(Response userResponse, Route route) throws IOException {
/*     */     Proxy selectedProxy;
/*     */     String location;
/*     */     HttpUrl url;
/*     */     boolean sameScheme;
/*     */     Request.Builder requestBuilder;
/* 283 */     if (userResponse == null) throw new IllegalStateException(); 
/* 284 */     int responseCode = userResponse.code();
/*     */     
/* 286 */     String method = userResponse.request().method();
/* 287 */     switch (responseCode) {
/*     */       case 407:
/* 289 */         selectedProxy = route.proxy();
/* 290 */         if (selectedProxy.type() != Proxy.Type.HTTP) {
/* 291 */           throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
/*     */         }
/* 293 */         return this.client.proxyAuthenticator().authenticate(route, userResponse);
/*     */       
/*     */       case 401:
/* 296 */         return this.client.authenticator().authenticate(route, userResponse);
/*     */ 
/*     */ 
/*     */       
/*     */       case 307:
/*     */       case 308:
/* 302 */         if (!method.equals("GET") && !method.equals("HEAD")) {
/* 303 */           return null;
/*     */         }
/*     */ 
/*     */       
/*     */       case 300:
/*     */       case 301:
/*     */       case 302:
/*     */       case 303:
/* 311 */         if (!this.client.followRedirects()) return null;
/*     */         
/* 313 */         location = userResponse.header("Location");
/* 314 */         if (location == null) return null; 
/* 315 */         url = userResponse.request().url().resolve(location);
/*     */ 
/*     */         
/* 318 */         if (url == null) return null;
/*     */ 
/*     */         
/* 321 */         sameScheme = url.scheme().equals(userResponse.request().url().scheme());
/* 322 */         if (!sameScheme && !this.client.followSslRedirects()) return null;
/*     */ 
/*     */         
/* 325 */         requestBuilder = userResponse.request().newBuilder();
/* 326 */         if (HttpMethod.permitsRequestBody(method)) {
/* 327 */           boolean maintainBody = HttpMethod.redirectsWithBody(method);
/* 328 */           if (HttpMethod.redirectsToGet(method)) {
/* 329 */             requestBuilder.method("GET", null);
/*     */           } else {
/* 331 */             RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
/* 332 */             requestBuilder.method(method, requestBody);
/*     */           } 
/* 334 */           if (!maintainBody) {
/* 335 */             requestBuilder.removeHeader("Transfer-Encoding");
/* 336 */             requestBuilder.removeHeader("Content-Length");
/* 337 */             requestBuilder.removeHeader("Content-Type");
/*     */           } 
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 344 */         if (!sameConnection(userResponse, url)) {
/* 345 */           requestBuilder.removeHeader("Authorization");
/*     */         }
/*     */         
/* 348 */         return requestBuilder.url(url).build();
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       case 408:
/* 354 */         if (!this.client.retryOnConnectionFailure())
/*     */         {
/* 356 */           return null;
/*     */         }
/*     */         
/* 359 */         if (userResponse.request().body() instanceof UnrepeatableRequestBody) {
/* 360 */           return null;
/*     */         }
/*     */         
/* 363 */         if (userResponse.priorResponse() != null && userResponse
/* 364 */           .priorResponse().code() == 408)
/*     */         {
/* 366 */           return null;
/*     */         }
/*     */         
/* 369 */         if (retryAfter(userResponse, 0) > 0) {
/* 370 */           return null;
/*     */         }
/*     */         
/* 373 */         return userResponse.request();
/*     */       
/*     */       case 503:
/* 376 */         if (userResponse.priorResponse() != null && userResponse
/* 377 */           .priorResponse().code() == 503)
/*     */         {
/* 379 */           return null;
/*     */         }
/*     */         
/* 382 */         if (retryAfter(userResponse, 2147483647) == 0)
/*     */         {
/* 384 */           return userResponse.request();
/*     */         }
/*     */         
/* 387 */         return null;
/*     */     } 
/*     */     
/* 390 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private int retryAfter(Response userResponse, int defaultDelay) {
/* 395 */     String header = userResponse.header("Retry-After");
/*     */     
/* 397 */     if (header == null) {
/* 398 */       return defaultDelay;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 403 */     if (header.matches("\\d+")) {
/* 404 */       return Integer.valueOf(header).intValue();
/*     */     }
/*     */     
/* 407 */     return Integer.MAX_VALUE;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean sameConnection(Response response, HttpUrl followUp) {
/* 415 */     HttpUrl url = response.request().url();
/* 416 */     return (url.host().equals(followUp.host()) && url
/* 417 */       .port() == followUp.port() && url
/* 418 */       .scheme().equals(followUp.scheme()));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\RetryAndFollowUpInterceptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */