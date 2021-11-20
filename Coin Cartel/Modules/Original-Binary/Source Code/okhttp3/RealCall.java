/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.RejectedExecutionException;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.NamedRunnable;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.cache.CacheInterceptor;
/*     */ import okhttp3.internal.connection.ConnectInterceptor;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ import okhttp3.internal.http.BridgeInterceptor;
/*     */ import okhttp3.internal.http.CallServerInterceptor;
/*     */ import okhttp3.internal.http.RealInterceptorChain;
/*     */ import okhttp3.internal.http.RetryAndFollowUpInterceptor;
/*     */ import okhttp3.internal.platform.Platform;
/*     */ import okio.AsyncTimeout;
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
/*     */ final class RealCall
/*     */   implements Call
/*     */ {
/*     */   final OkHttpClient client;
/*     */   final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
/*     */   final AsyncTimeout timeout;
/*     */   @Nullable
/*     */   private EventListener eventListener;
/*     */   final Request originalRequest;
/*     */   final boolean forWebSocket;
/*     */   private boolean executed;
/*     */   
/*     */   private RealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
/*  60 */     this.client = client;
/*  61 */     this.originalRequest = originalRequest;
/*  62 */     this.forWebSocket = forWebSocket;
/*  63 */     this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client, forWebSocket);
/*  64 */     this.timeout = new AsyncTimeout() {
/*     */         protected void timedOut() {
/*  66 */           RealCall.this.cancel();
/*     */         }
/*     */       };
/*  69 */     this.timeout.timeout(client.callTimeoutMillis(), TimeUnit.MILLISECONDS);
/*     */   }
/*     */ 
/*     */   
/*     */   static RealCall newRealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
/*  74 */     RealCall call = new RealCall(client, originalRequest, forWebSocket);
/*  75 */     call.eventListener = client.eventListenerFactory().create(call);
/*  76 */     return call;
/*     */   }
/*     */   
/*     */   public Request request() {
/*  80 */     return this.originalRequest;
/*     */   }
/*     */   
/*     */   public Response execute() throws IOException {
/*  84 */     synchronized (this) {
/*  85 */       if (this.executed) throw new IllegalStateException("Already Executed"); 
/*  86 */       this.executed = true;
/*     */     } 
/*  88 */     captureCallStackTrace();
/*  89 */     this.timeout.enter();
/*  90 */     this.eventListener.callStart(this);
/*     */     try {
/*  92 */       this.client.dispatcher().executed(this);
/*  93 */       Response result = getResponseWithInterceptorChain();
/*  94 */       if (result == null) throw new IOException("Canceled"); 
/*  95 */       return result;
/*  96 */     } catch (IOException e) {
/*  97 */       e = timeoutExit(e);
/*  98 */       this.eventListener.callFailed(this, e);
/*  99 */       throw e;
/*     */     } finally {
/* 101 */       this.client.dispatcher().finished(this);
/*     */     } 
/*     */   }
/*     */   @Nullable
/*     */   IOException timeoutExit(@Nullable IOException cause) {
/* 106 */     if (!this.timeout.exit()) return cause;
/*     */     
/* 108 */     InterruptedIOException e = new InterruptedIOException("timeout");
/* 109 */     if (cause != null) {
/* 110 */       e.initCause(cause);
/*     */     }
/* 112 */     return e;
/*     */   }
/*     */   
/*     */   private void captureCallStackTrace() {
/* 116 */     Object callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
/* 117 */     this.retryAndFollowUpInterceptor.setCallStackTrace(callStackTrace);
/*     */   }
/*     */   
/*     */   public void enqueue(Callback responseCallback) {
/* 121 */     synchronized (this) {
/* 122 */       if (this.executed) throw new IllegalStateException("Already Executed"); 
/* 123 */       this.executed = true;
/*     */     } 
/* 125 */     captureCallStackTrace();
/* 126 */     this.eventListener.callStart(this);
/* 127 */     this.client.dispatcher().enqueue(new AsyncCall(responseCallback));
/*     */   }
/*     */   
/*     */   public void cancel() {
/* 131 */     this.retryAndFollowUpInterceptor.cancel();
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/* 135 */     return (Timeout)this.timeout;
/*     */   }
/*     */   
/*     */   public synchronized boolean isExecuted() {
/* 139 */     return this.executed;
/*     */   }
/*     */   
/*     */   public boolean isCanceled() {
/* 143 */     return this.retryAndFollowUpInterceptor.isCanceled();
/*     */   }
/*     */ 
/*     */   
/*     */   public RealCall clone() {
/* 148 */     return newRealCall(this.client, this.originalRequest, this.forWebSocket);
/*     */   }
/*     */   
/*     */   StreamAllocation streamAllocation() {
/* 152 */     return this.retryAndFollowUpInterceptor.streamAllocation();
/*     */   }
/*     */   
/*     */   final class AsyncCall extends NamedRunnable {
/*     */     private final Callback responseCallback;
/*     */     
/*     */     AsyncCall(Callback responseCallback) {
/* 159 */       super("OkHttp %s", new Object[] { this$0.redactedUrl() });
/* 160 */       this.responseCallback = responseCallback;
/*     */     }
/*     */     
/*     */     String host() {
/* 164 */       return RealCall.this.originalRequest.url().host();
/*     */     }
/*     */     
/*     */     Request request() {
/* 168 */       return RealCall.this.originalRequest;
/*     */     }
/*     */     
/*     */     RealCall get() {
/* 172 */       return RealCall.this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     void executeOn(ExecutorService executorService) {
/* 180 */       assert !Thread.holdsLock(RealCall.this.client.dispatcher());
/* 181 */       boolean success = false;
/*     */       try {
/* 183 */         executorService.execute((Runnable)this);
/* 184 */         success = true;
/* 185 */       } catch (RejectedExecutionException e) {
/* 186 */         InterruptedIOException ioException = new InterruptedIOException("executor rejected");
/* 187 */         ioException.initCause(e);
/* 188 */         RealCall.this.eventListener.callFailed(RealCall.this, ioException);
/* 189 */         this.responseCallback.onFailure(RealCall.this, ioException);
/*     */       } finally {
/* 191 */         if (!success) {
/* 192 */           RealCall.this.client.dispatcher().finished(this);
/*     */         }
/*     */       } 
/*     */     }
/*     */     
/*     */     protected void execute() {
/* 198 */       boolean signalledCallback = false;
/* 199 */       RealCall.this.timeout.enter();
/*     */       try {
/* 201 */         Response response = RealCall.this.getResponseWithInterceptorChain();
/* 202 */         signalledCallback = true;
/* 203 */         this.responseCallback.onResponse(RealCall.this, response);
/* 204 */       } catch (IOException e) {
/* 205 */         e = RealCall.this.timeoutExit(e);
/* 206 */         if (signalledCallback) {
/*     */           
/* 208 */           Platform.get().log(4, "Callback failure for " + RealCall.this.toLoggableString(), e);
/*     */         } else {
/* 210 */           RealCall.this.eventListener.callFailed(RealCall.this, e);
/* 211 */           this.responseCallback.onFailure(RealCall.this, e);
/*     */         } 
/* 213 */       } catch (Throwable t) {
/* 214 */         RealCall.this.cancel();
/* 215 */         if (!signalledCallback) {
/* 216 */           IOException canceledException = new IOException("canceled due to " + t);
/* 217 */           this.responseCallback.onFailure(RealCall.this, canceledException);
/*     */         } 
/* 219 */         throw t;
/*     */       } finally {
/* 221 */         RealCall.this.client.dispatcher().finished(this);
/*     */       } 
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   String toLoggableString() {
/* 231 */     return (isCanceled() ? "canceled " : "") + (
/* 232 */       this.forWebSocket ? "web socket" : "call") + " to " + 
/* 233 */       redactedUrl();
/*     */   }
/*     */   
/*     */   String redactedUrl() {
/* 237 */     return this.originalRequest.url().redact();
/*     */   }
/*     */ 
/*     */   
/*     */   Response getResponseWithInterceptorChain() throws IOException {
/* 242 */     List<Interceptor> interceptors = new ArrayList<>();
/* 243 */     interceptors.addAll(this.client.interceptors());
/* 244 */     interceptors.add(this.retryAndFollowUpInterceptor);
/* 245 */     interceptors.add(new BridgeInterceptor(this.client.cookieJar()));
/* 246 */     interceptors.add(new CacheInterceptor(this.client.internalCache()));
/* 247 */     interceptors.add(new ConnectInterceptor(this.client));
/* 248 */     if (!this.forWebSocket) {
/* 249 */       interceptors.addAll(this.client.networkInterceptors());
/*     */     }
/* 251 */     interceptors.add(new CallServerInterceptor(this.forWebSocket));
/*     */ 
/*     */ 
/*     */     
/* 255 */     RealInterceptorChain realInterceptorChain = new RealInterceptorChain(interceptors, null, null, null, 0, this.originalRequest, this, this.eventListener, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis());
/*     */     
/* 257 */     Response response = realInterceptorChain.proceed(this.originalRequest);
/* 258 */     if (this.retryAndFollowUpInterceptor.isCanceled()) {
/* 259 */       Util.closeQuietly(response);
/* 260 */       throw new IOException("Canceled");
/*     */     } 
/* 262 */     return response;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\RealCall.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */