/*    */ package okio;
/*    */ 
/*    */ import java.io.IOException;
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
/*    */ public abstract class ForwardingSink
/*    */   implements Sink
/*    */ {
/*    */   private final Sink delegate;
/*    */   
/*    */   public ForwardingSink(Sink delegate) {
/* 25 */     if (delegate == null) throw new IllegalArgumentException("delegate == null"); 
/* 26 */     this.delegate = delegate;
/*    */   }
/*    */ 
/*    */   
/*    */   public final Sink delegate() {
/* 31 */     return this.delegate;
/*    */   }
/*    */   
/*    */   public void write(Buffer source, long byteCount) throws IOException {
/* 35 */     this.delegate.write(source, byteCount);
/*    */   }
/*    */   
/*    */   public void flush() throws IOException {
/* 39 */     this.delegate.flush();
/*    */   }
/*    */   
/*    */   public Timeout timeout() {
/* 43 */     return this.delegate.timeout();
/*    */   }
/*    */   
/*    */   public void close() throws IOException {
/* 47 */     this.delegate.close();
/*    */   }
/*    */   
/*    */   public String toString() {
/* 51 */     return getClass().getSimpleName() + "(" + this.delegate.toString() + ")";
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\ForwardingSink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */