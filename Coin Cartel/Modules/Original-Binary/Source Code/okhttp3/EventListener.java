/*    */ package okhttp3;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.net.InetAddress;
/*    */ import java.net.InetSocketAddress;
/*    */ import java.net.Proxy;
/*    */ import java.util.List;
/*    */ import javax.annotation.Nullable;
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
/*    */ public abstract class EventListener
/*    */ {
/* 53 */   public static final EventListener NONE = new EventListener() {  }
/*    */   ;
/*    */   
/*    */   static Factory factory(final EventListener listener) {
/* 57 */     return new Factory() {
/*    */         public EventListener create(Call call) {
/* 59 */           return listener;
/*    */         }
/*    */       };
/*    */   }
/*    */   
/*    */   public void callStart(Call call) {}
/*    */   
/*    */   public void dnsStart(Call call, String domainName) {}
/*    */   
/*    */   public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {}
/*    */   
/*    */   public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {}
/*    */   
/*    */   public void secureConnectStart(Call call) {}
/*    */   
/*    */   public void secureConnectEnd(Call call, @Nullable Handshake handshake) {}
/*    */   
/*    */   public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol) {}
/*    */   
/*    */   public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, @Nullable Protocol protocol, IOException ioe) {}
/*    */   
/*    */   public void connectionAcquired(Call call, Connection connection) {}
/*    */   
/*    */   public void connectionReleased(Call call, Connection connection) {}
/*    */   
/*    */   public void requestHeadersStart(Call call) {}
/*    */   
/*    */   public void requestHeadersEnd(Call call, Request request) {}
/*    */   
/*    */   public void requestBodyStart(Call call) {}
/*    */   
/*    */   public void requestBodyEnd(Call call, long byteCount) {}
/*    */   
/*    */   public void responseHeadersStart(Call call) {}
/*    */   
/*    */   public void responseHeadersEnd(Call call, Response response) {}
/*    */   
/*    */   public void responseBodyStart(Call call) {}
/*    */   
/*    */   public void responseBodyEnd(Call call, long byteCount) {}
/*    */   
/*    */   public void callEnd(Call call) {}
/*    */   
/*    */   public void callFailed(Call call, IOException ioe) {}
/*    */   
/*    */   public static interface Factory {
/*    */     EventListener create(Call param1Call);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\EventListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */