/*    */ package io.socket.engineio.client;
/*    */ 
/*    */ 
/*    */ public class EngineIOException
/*    */   extends Exception
/*    */ {
/*    */   public String transport;
/*    */   public Object code;
/*    */   
/*    */   public EngineIOException() {}
/*    */   
/*    */   public EngineIOException(String message) {
/* 13 */     super(message);
/*    */   }
/*    */   
/*    */   public EngineIOException(String message, Throwable cause) {
/* 17 */     super(message, cause);
/*    */   }
/*    */   
/*    */   public EngineIOException(Throwable cause) {
/* 21 */     super(cause);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\EngineIOException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */