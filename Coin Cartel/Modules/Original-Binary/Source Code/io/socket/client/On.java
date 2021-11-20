/*    */ package io.socket.client;
/*    */ 
/*    */ import io.socket.emitter.Emitter;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class On
/*    */ {
/*    */   public static Handle on(final Emitter obj, final String ev, final Emitter.Listener fn) {
/* 10 */     obj.on(ev, fn);
/* 11 */     return new Handle()
/*    */       {
/*    */         public void destroy() {
/* 14 */           obj.off(ev, fn);
/*    */         }
/*    */       };
/*    */   }
/*    */   
/*    */   public static interface Handle {
/*    */     void destroy();
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\client\On.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */