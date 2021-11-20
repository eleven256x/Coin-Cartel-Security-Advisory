/*    */ package okhttp3;
/*    */ 
/*    */ import java.net.InetAddress;
/*    */ import java.net.UnknownHostException;
/*    */ import java.util.Arrays;
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
/*    */ public interface Dns
/*    */ {
/* 36 */   public static final Dns SYSTEM = new Dns() {
/*    */       public List<InetAddress> lookup(String hostname) throws UnknownHostException {
/* 38 */         if (hostname == null) throw new UnknownHostException("hostname == null"); 
/*    */         try {
/* 40 */           return Arrays.asList(InetAddress.getAllByName(hostname));
/* 41 */         } catch (NullPointerException e) {
/* 42 */           UnknownHostException unknownHostException = new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
/*    */           
/* 44 */           unknownHostException.initCause(e);
/* 45 */           throw unknownHostException;
/*    */         } 
/*    */       }
/*    */     };
/*    */   
/*    */   List<InetAddress> lookup(String paramString) throws UnknownHostException;
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Dns.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */