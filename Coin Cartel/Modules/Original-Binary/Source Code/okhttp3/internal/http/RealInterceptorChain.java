/*     */ package okhttp3.internal.http;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.Connection;
/*     */ import okhttp3.EventListener;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.RealConnection;
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
/*     */ public final class RealInterceptorChain
/*     */   implements Interceptor.Chain
/*     */ {
/*     */   private final List<Interceptor> interceptors;
/*     */   private final StreamAllocation streamAllocation;
/*     */   private final HttpCodec httpCodec;
/*     */   private final RealConnection connection;
/*     */   private final int index;
/*     */   private final Request request;
/*     */   private final Call call;
/*     */   private final EventListener eventListener;
/*     */   private final int connectTimeout;
/*     */   private final int readTimeout;
/*     */   private final int writeTimeout;
/*     */   private int calls;
/*     */   
/*     */   public RealInterceptorChain(List<Interceptor> interceptors, StreamAllocation streamAllocation, HttpCodec httpCodec, RealConnection connection, int index, Request request, Call call, EventListener eventListener, int connectTimeout, int readTimeout, int writeTimeout) {
/*  53 */     this.interceptors = interceptors;
/*  54 */     this.connection = connection;
/*  55 */     this.streamAllocation = streamAllocation;
/*  56 */     this.httpCodec = httpCodec;
/*  57 */     this.index = index;
/*  58 */     this.request = request;
/*  59 */     this.call = call;
/*  60 */     this.eventListener = eventListener;
/*  61 */     this.connectTimeout = connectTimeout;
/*  62 */     this.readTimeout = readTimeout;
/*  63 */     this.writeTimeout = writeTimeout;
/*     */   }
/*     */   
/*     */   public Connection connection() {
/*  67 */     return (Connection)this.connection;
/*     */   }
/*     */   
/*     */   public int connectTimeoutMillis() {
/*  71 */     return this.connectTimeout;
/*     */   }
/*     */   
/*     */   public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
/*  75 */     int millis = Util.checkDuration("timeout", timeout, unit);
/*  76 */     return new RealInterceptorChain(this.interceptors, this.streamAllocation, this.httpCodec, this.connection, this.index, this.request, this.call, this.eventListener, millis, this.readTimeout, this.writeTimeout);
/*     */   }
/*     */ 
/*     */   
/*     */   public int readTimeoutMillis() {
/*  81 */     return this.readTimeout;
/*     */   }
/*     */   
/*     */   public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
/*  85 */     int millis = Util.checkDuration("timeout", timeout, unit);
/*  86 */     return new RealInterceptorChain(this.interceptors, this.streamAllocation, this.httpCodec, this.connection, this.index, this.request, this.call, this.eventListener, this.connectTimeout, millis, this.writeTimeout);
/*     */   }
/*     */ 
/*     */   
/*     */   public int writeTimeoutMillis() {
/*  91 */     return this.writeTimeout;
/*     */   }
/*     */   
/*     */   public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
/*  95 */     int millis = Util.checkDuration("timeout", timeout, unit);
/*  96 */     return new RealInterceptorChain(this.interceptors, this.streamAllocation, this.httpCodec, this.connection, this.index, this.request, this.call, this.eventListener, this.connectTimeout, this.readTimeout, millis);
/*     */   }
/*     */ 
/*     */   
/*     */   public StreamAllocation streamAllocation() {
/* 101 */     return this.streamAllocation;
/*     */   }
/*     */   
/*     */   public HttpCodec httpStream() {
/* 105 */     return this.httpCodec;
/*     */   }
/*     */   
/*     */   public Call call() {
/* 109 */     return this.call;
/*     */   }
/*     */   
/*     */   public EventListener eventListener() {
/* 113 */     return this.eventListener;
/*     */   }
/*     */   
/*     */   public Request request() {
/* 117 */     return this.request;
/*     */   }
/*     */   
/*     */   public Response proceed(Request request) throws IOException {
/* 121 */     return proceed(request, this.streamAllocation, this.httpCodec, this.connection);
/*     */   }
/*     */ 
/*     */   
/*     */   public Response proceed(Request request, StreamAllocation streamAllocation, HttpCodec httpCodec, RealConnection connection) throws IOException {
/* 126 */     if (this.index >= this.interceptors.size()) throw new AssertionError();
/*     */     
/* 128 */     this.calls++;
/*     */ 
/*     */     
/* 131 */     if (this.httpCodec != null && !this.connection.supportsUrl(request.url())) {
/* 132 */       throw new IllegalStateException("network interceptor " + this.interceptors.get(this.index - 1) + " must retain the same host and port");
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 137 */     if (this.httpCodec != null && this.calls > 1) {
/* 138 */       throw new IllegalStateException("network interceptor " + this.interceptors.get(this.index - 1) + " must call proceed() exactly once");
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 143 */     RealInterceptorChain next = new RealInterceptorChain(this.interceptors, streamAllocation, httpCodec, connection, this.index + 1, request, this.call, this.eventListener, this.connectTimeout, this.readTimeout, this.writeTimeout);
/*     */ 
/*     */     
/* 146 */     Interceptor interceptor = this.interceptors.get(this.index);
/* 147 */     Response response = interceptor.intercept(next);
/*     */ 
/*     */     
/* 150 */     if (httpCodec != null && this.index + 1 < this.interceptors.size() && next.calls != 1) {
/* 151 */       throw new IllegalStateException("network interceptor " + interceptor + " must call proceed() exactly once");
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 156 */     if (response == null) {
/* 157 */       throw new NullPointerException("interceptor " + interceptor + " returned null");
/*     */     }
/*     */     
/* 160 */     if (response.body() == null) {
/* 161 */       throw new IllegalStateException("interceptor " + interceptor + " returned a response with no body");
/*     */     }
/*     */ 
/*     */     
/* 165 */     return response;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\RealInterceptorChain.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */