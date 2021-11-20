/*    */ package okhttp3.internal.connection;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import okhttp3.internal.Util;
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
/*    */ public final class RouteException
/*    */   extends RuntimeException
/*    */ {
/*    */   private IOException firstException;
/*    */   private IOException lastException;
/*    */   
/*    */   public RouteException(IOException cause) {
/* 31 */     super(cause);
/* 32 */     this.firstException = cause;
/* 33 */     this.lastException = cause;
/*    */   }
/*    */   
/*    */   public IOException getFirstConnectException() {
/* 37 */     return this.firstException;
/*    */   }
/*    */   
/*    */   public IOException getLastConnectException() {
/* 41 */     return this.lastException;
/*    */   }
/*    */   
/*    */   public void addConnectException(IOException e) {
/* 45 */     Util.addSuppressedIfPossible(this.firstException, e);
/* 46 */     this.lastException = e;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\connection\RouteException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */