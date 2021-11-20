/*     */ package okhttp3.internal.http;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.ProtocolException;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.RealConnection;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
/*     */ import okio.ForwardingSink;
/*     */ import okio.Okio;
/*     */ import okio.Sink;
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
/*     */ public final class CallServerInterceptor
/*     */   implements Interceptor
/*     */ {
/*     */   private final boolean forWebSocket;
/*     */   
/*     */   public CallServerInterceptor(boolean forWebSocket) {
/*  37 */     this.forWebSocket = forWebSocket;
/*     */   }
/*     */   
/*     */   public Response intercept(Interceptor.Chain chain) throws IOException {
/*  41 */     RealInterceptorChain realChain = (RealInterceptorChain)chain;
/*  42 */     HttpCodec httpCodec = realChain.httpStream();
/*  43 */     StreamAllocation streamAllocation = realChain.streamAllocation();
/*  44 */     RealConnection connection = (RealConnection)realChain.connection();
/*  45 */     Request request = realChain.request();
/*     */     
/*  47 */     long sentRequestMillis = System.currentTimeMillis();
/*     */     
/*  49 */     realChain.eventListener().requestHeadersStart(realChain.call());
/*  50 */     httpCodec.writeRequestHeaders(request);
/*  51 */     realChain.eventListener().requestHeadersEnd(realChain.call(), request);
/*     */     
/*  53 */     Response.Builder responseBuilder = null;
/*  54 */     if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
/*     */ 
/*     */ 
/*     */       
/*  58 */       if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
/*  59 */         httpCodec.flushRequest();
/*  60 */         realChain.eventListener().responseHeadersStart(realChain.call());
/*  61 */         responseBuilder = httpCodec.readResponseHeaders(true);
/*     */       } 
/*     */       
/*  64 */       if (responseBuilder == null) {
/*     */         
/*  66 */         realChain.eventListener().requestBodyStart(realChain.call());
/*  67 */         long contentLength = request.body().contentLength();
/*     */         
/*  69 */         CountingSink requestBodyOut = new CountingSink(httpCodec.createRequestBody(request, contentLength));
/*  70 */         BufferedSink bufferedRequestBody = Okio.buffer((Sink)requestBodyOut);
/*     */         
/*  72 */         request.body().writeTo(bufferedRequestBody);
/*  73 */         bufferedRequestBody.close();
/*  74 */         realChain.eventListener()
/*  75 */           .requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);
/*  76 */       } else if (!connection.isMultiplexed()) {
/*     */ 
/*     */ 
/*     */         
/*  80 */         streamAllocation.noNewStreams();
/*     */       } 
/*     */     } 
/*     */     
/*  84 */     httpCodec.finishRequest();
/*     */     
/*  86 */     if (responseBuilder == null) {
/*  87 */       realChain.eventListener().responseHeadersStart(realChain.call());
/*  88 */       responseBuilder = httpCodec.readResponseHeaders(false);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  96 */     Response response = responseBuilder.request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
/*     */     
/*  98 */     int code = response.code();
/*  99 */     if (code == 100) {
/*     */ 
/*     */       
/* 102 */       responseBuilder = httpCodec.readResponseHeaders(false);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 109 */       response = responseBuilder.request(request).handshake(streamAllocation.connection().handshake()).sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();
/*     */       
/* 111 */       code = response.code();
/*     */     } 
/*     */     
/* 114 */     realChain.eventListener()
/* 115 */       .responseHeadersEnd(realChain.call(), response);
/*     */     
/* 117 */     if (this.forWebSocket && code == 101) {
/*     */ 
/*     */ 
/*     */       
/* 121 */       response = response.newBuilder().body(Util.EMPTY_RESPONSE).build();
/*     */     }
/*     */     else {
/*     */       
/* 125 */       response = response.newBuilder().body(httpCodec.openResponseBody(response)).build();
/*     */     } 
/*     */     
/* 128 */     if ("close".equalsIgnoreCase(response.request().header("Connection")) || "close"
/* 129 */       .equalsIgnoreCase(response.header("Connection"))) {
/* 130 */       streamAllocation.noNewStreams();
/*     */     }
/*     */     
/* 133 */     if ((code == 204 || code == 205) && response.body().contentLength() > 0L) {
/* 134 */       throw new ProtocolException("HTTP " + code + " had non-zero Content-Length: " + response
/* 135 */           .body().contentLength());
/*     */     }
/*     */     
/* 138 */     return response;
/*     */   }
/*     */   
/*     */   static final class CountingSink extends ForwardingSink {
/*     */     long successfulCount;
/*     */     
/*     */     CountingSink(Sink delegate) {
/* 145 */       super(delegate);
/*     */     }
/*     */     
/*     */     public void write(Buffer source, long byteCount) throws IOException {
/* 149 */       super.write(source, byteCount);
/* 150 */       this.successfulCount += byteCount;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\CallServerInterceptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */