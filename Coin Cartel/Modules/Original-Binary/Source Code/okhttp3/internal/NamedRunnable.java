/*    */ package okhttp3.internal;
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
/*    */ public abstract class NamedRunnable
/*    */   implements Runnable
/*    */ {
/*    */   protected final String name;
/*    */   
/*    */   public NamedRunnable(String format, Object... args) {
/* 25 */     this.name = Util.format(format, args);
/*    */   }
/*    */   
/*    */   public final void run() {
/* 29 */     String oldName = Thread.currentThread().getName();
/* 30 */     Thread.currentThread().setName(this.name);
/*    */     try {
/* 32 */       execute();
/*    */     } finally {
/* 34 */       Thread.currentThread().setName(oldName);
/*    */     } 
/*    */   }
/*    */   
/*    */   protected abstract void execute();
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\NamedRunnable.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */