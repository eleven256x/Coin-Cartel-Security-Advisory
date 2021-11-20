/*    */ package okio;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.util.concurrent.TimeUnit;
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
/*    */ public class ForwardingTimeout
/*    */   extends Timeout
/*    */ {
/*    */   private Timeout delegate;
/*    */   
/*    */   public ForwardingTimeout(Timeout delegate) {
/* 26 */     if (delegate == null) throw new IllegalArgumentException("delegate == null"); 
/* 27 */     this.delegate = delegate;
/*    */   }
/*    */ 
/*    */   
/*    */   public final Timeout delegate() {
/* 32 */     return this.delegate;
/*    */   }
/*    */   
/*    */   public final ForwardingTimeout setDelegate(Timeout delegate) {
/* 36 */     if (delegate == null) throw new IllegalArgumentException("delegate == null"); 
/* 37 */     this.delegate = delegate;
/* 38 */     return this;
/*    */   }
/*    */   
/*    */   public Timeout timeout(long timeout, TimeUnit unit) {
/* 42 */     return this.delegate.timeout(timeout, unit);
/*    */   }
/*    */   
/*    */   public long timeoutNanos() {
/* 46 */     return this.delegate.timeoutNanos();
/*    */   }
/*    */   
/*    */   public boolean hasDeadline() {
/* 50 */     return this.delegate.hasDeadline();
/*    */   }
/*    */   
/*    */   public long deadlineNanoTime() {
/* 54 */     return this.delegate.deadlineNanoTime();
/*    */   }
/*    */   
/*    */   public Timeout deadlineNanoTime(long deadlineNanoTime) {
/* 58 */     return this.delegate.deadlineNanoTime(deadlineNanoTime);
/*    */   }
/*    */   
/*    */   public Timeout clearTimeout() {
/* 62 */     return this.delegate.clearTimeout();
/*    */   }
/*    */   
/*    */   public Timeout clearDeadline() {
/* 66 */     return this.delegate.clearDeadline();
/*    */   }
/*    */   
/*    */   public void throwIfReached() throws IOException {
/* 70 */     this.delegate.throwIfReached();
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\ForwardingTimeout.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */