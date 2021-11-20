/*    */ package okhttp3.internal.proxy;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.net.Proxy;
/*    */ import java.net.ProxySelector;
/*    */ import java.net.SocketAddress;
/*    */ import java.net.URI;
/*    */ import java.util.Collections;
/*    */ import java.util.List;
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
/*    */ public class NullProxySelector
/*    */   extends ProxySelector
/*    */ {
/*    */   public List<Proxy> select(URI uri) {
/* 31 */     if (uri == null) {
/* 32 */       throw new IllegalArgumentException("uri must not be null");
/*    */     }
/* 34 */     return Collections.singletonList(Proxy.NO_PROXY);
/*    */   }
/*    */   
/*    */   public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {}
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\proxy\NullProxySelector.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */