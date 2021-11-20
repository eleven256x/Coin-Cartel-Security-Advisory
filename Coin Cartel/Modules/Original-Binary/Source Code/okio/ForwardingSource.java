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
/*    */ public abstract class ForwardingSource
/*    */   implements Source
/*    */ {
/*    */   private final Source delegate;
/*    */   
/*    */   public ForwardingSource(Source delegate) {
/* 25 */     if (delegate == null) throw new IllegalArgumentException("delegate == null"); 
/* 26 */     this.delegate = delegate;
/*    */   }
/*    */ 
/*    */   
/*    */   public final Source delegate() {
/* 31 */     return this.delegate;
/*    */   }
/*    */   
/*    */   public long read(Buffer sink, long byteCount) throws IOException {
/* 35 */     return this.delegate.read(sink, byteCount);
/*    */   }
/*    */   
/*    */   public Timeout timeout() {
/* 39 */     return this.delegate.timeout();
/*    */   }
/*    */   
/*    */   public void close() throws IOException {
/* 43 */     this.delegate.close();
/*    */   }
/*    */   
/*    */   public String toString() {
/* 47 */     return getClass().getSimpleName() + "(" + this.delegate.toString() + ")";
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\ForwardingSource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */