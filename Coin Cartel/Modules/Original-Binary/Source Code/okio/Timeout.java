/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Timeout
/*     */ {
/*  48 */   public static final Timeout NONE = new Timeout() {
/*     */       public Timeout timeout(long timeout, TimeUnit unit) {
/*  50 */         return this;
/*     */       }
/*     */       
/*     */       public Timeout deadlineNanoTime(long deadlineNanoTime) {
/*  54 */         return this;
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       public void throwIfReached() throws IOException {}
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean hasDeadline;
/*     */ 
/*     */ 
/*     */   
/*     */   private long deadlineNanoTime;
/*     */ 
/*     */ 
/*     */   
/*     */   private long timeoutNanos;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Timeout timeout(long timeout, TimeUnit unit) {
/*  81 */     if (timeout < 0L) throw new IllegalArgumentException("timeout < 0: " + timeout); 
/*  82 */     if (unit == null) throw new IllegalArgumentException("unit == null"); 
/*  83 */     this.timeoutNanos = unit.toNanos(timeout);
/*  84 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public long timeoutNanos() {
/*  89 */     return this.timeoutNanos;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean hasDeadline() {
/*  94 */     return this.hasDeadline;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public long deadlineNanoTime() {
/* 104 */     if (!this.hasDeadline) throw new IllegalStateException("No deadline"); 
/* 105 */     return this.deadlineNanoTime;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Timeout deadlineNanoTime(long deadlineNanoTime) {
/* 114 */     this.hasDeadline = true;
/* 115 */     this.deadlineNanoTime = deadlineNanoTime;
/* 116 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public final Timeout deadline(long duration, TimeUnit unit) {
/* 121 */     if (duration <= 0L) throw new IllegalArgumentException("duration <= 0: " + duration); 
/* 122 */     if (unit == null) throw new IllegalArgumentException("unit == null"); 
/* 123 */     return deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
/*     */   }
/*     */ 
/*     */   
/*     */   public Timeout clearTimeout() {
/* 128 */     this.timeoutNanos = 0L;
/* 129 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public Timeout clearDeadline() {
/* 134 */     this.hasDeadline = false;
/* 135 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void throwIfReached() throws IOException {
/* 144 */     if (Thread.interrupted()) {
/* 145 */       Thread.currentThread().interrupt();
/* 146 */       throw new InterruptedIOException("interrupted");
/*     */     } 
/*     */     
/* 149 */     if (this.hasDeadline && this.deadlineNanoTime - System.nanoTime() <= 0L) {
/* 150 */       throw new InterruptedIOException("deadline reached");
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
/*     */     try {
/*     */       long waitNanos;
/* 192 */       boolean hasDeadline = hasDeadline();
/* 193 */       long timeoutNanos = timeoutNanos();
/*     */       
/* 195 */       if (!hasDeadline && timeoutNanos == 0L) {
/* 196 */         monitor.wait();
/*     */ 
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 202 */       long start = System.nanoTime();
/* 203 */       if (hasDeadline && timeoutNanos != 0L) {
/* 204 */         long deadlineNanos = deadlineNanoTime() - start;
/* 205 */         waitNanos = Math.min(timeoutNanos, deadlineNanos);
/* 206 */       } else if (hasDeadline) {
/* 207 */         waitNanos = deadlineNanoTime() - start;
/*     */       } else {
/* 209 */         waitNanos = timeoutNanos;
/*     */       } 
/*     */ 
/*     */       
/* 213 */       long elapsedNanos = 0L;
/* 214 */       if (waitNanos > 0L) {
/* 215 */         long waitMillis = waitNanos / 1000000L;
/* 216 */         monitor.wait(waitMillis, (int)(waitNanos - waitMillis * 1000000L));
/* 217 */         elapsedNanos = System.nanoTime() - start;
/*     */       } 
/*     */ 
/*     */       
/* 221 */       if (elapsedNanos >= waitNanos) {
/* 222 */         throw new InterruptedIOException("timeout");
/*     */       }
/* 224 */     } catch (InterruptedException e) {
/* 225 */       Thread.currentThread().interrupt();
/* 226 */       throw new InterruptedIOException("interrupted");
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Timeout.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */