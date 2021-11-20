/*    */ package okhttp3.internal.connection;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import okhttp3.Interceptor;
/*    */ import okhttp3.OkHttpClient;
/*    */ import okhttp3.Request;
/*    */ import okhttp3.Response;
/*    */ import okhttp3.internal.http.HttpCodec;
/*    */ import okhttp3.internal.http.RealInterceptorChain;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class ConnectInterceptor
/*    */   implements Interceptor
/*    */ {
/*    */   public final OkHttpClient client;
/*    */   
/*    */   public ConnectInterceptor(OkHttpClient client) {
/* 32 */     this.client = client;
/*    */   }
/*    */   
/*    */   public Response intercept(Interceptor.Chain chain) throws IOException {
/* 36 */     RealInterceptorChain realChain = (RealInterceptorChain)chain;
/* 37 */     Request request = realChain.request();
/* 38 */     StreamAllocation streamAllocation = realChain.streamAllocation();
/*    */ 
/*    */     
/* 41 */     boolean doExtensiveHealthChecks = !request.method().equals("GET");
/* 42 */     HttpCodec httpCodec = streamAllocation.newStream(this.client, chain, doExtensiveHealthChecks);
/* 43 */     RealConnection connection = streamAllocation.connection();
/*    */     
/* 45 */     return realChain.proceed(request, streamAllocation, httpCodec, connection);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\connection\ConnectInterceptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */