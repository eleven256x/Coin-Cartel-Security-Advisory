/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
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
/*     */ public class AsyncTimeout
/*     */   extends Timeout
/*     */ {
/*     */   private static final int TIMEOUT_WRITE_SIZE = 65536;
/*  50 */   private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60L);
/*  51 */   private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   static AsyncTimeout head;
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean inQueue;
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   private AsyncTimeout next;
/*     */ 
/*     */   
/*     */   private long timeoutAt;
/*     */ 
/*     */ 
/*     */   
/*     */   public final void enter() {
/*  73 */     if (this.inQueue) throw new IllegalStateException("Unbalanced enter/exit"); 
/*  74 */     long timeoutNanos = timeoutNanos();
/*  75 */     boolean hasDeadline = hasDeadline();
/*  76 */     if (timeoutNanos == 0L && !hasDeadline) {
/*     */       return;
/*     */     }
/*  79 */     this.inQueue = true;
/*  80 */     scheduleTimeout(this, timeoutNanos, hasDeadline);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
/*  86 */     if (head == null) {
/*  87 */       head = new AsyncTimeout();
/*  88 */       (new Watchdog()).start();
/*     */     } 
/*     */     
/*  91 */     long now = System.nanoTime();
/*  92 */     if (timeoutNanos != 0L && hasDeadline) {
/*     */ 
/*     */       
/*  95 */       node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
/*  96 */     } else if (timeoutNanos != 0L) {
/*  97 */       node.timeoutAt = now + timeoutNanos;
/*  98 */     } else if (hasDeadline) {
/*  99 */       node.timeoutAt = node.deadlineNanoTime();
/*     */     } else {
/* 101 */       throw new AssertionError();
/*     */     } 
/*     */ 
/*     */     
/* 105 */     long remainingNanos = node.remainingNanos(now);
/* 106 */     for (AsyncTimeout prev = head;; prev = prev.next) {
/* 107 */       if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
/* 108 */         node.next = prev.next;
/* 109 */         prev.next = node;
/* 110 */         if (prev == head) {
/* 111 */           AsyncTimeout.class.notify();
/*     */         }
/*     */         break;
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public final boolean exit() {
/* 120 */     if (!this.inQueue) return false; 
/* 121 */     this.inQueue = false;
/* 122 */     return cancelScheduledTimeout(this);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
/* 128 */     for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
/* 129 */       if (prev.next == node) {
/* 130 */         prev.next = node.next;
/* 131 */         node.next = null;
/* 132 */         return false;
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 137 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private long remainingNanos(long now) {
/* 145 */     return this.timeoutAt - now;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected void timedOut() {}
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final Sink sink(final Sink sink) {
/* 160 */     return new Sink() {
/*     */         public void write(Buffer source, long byteCount) throws IOException {
/* 162 */           Util.checkOffsetAndCount(source.size, 0L, byteCount);
/*     */           
/* 164 */           while (byteCount > 0L) {
/*     */             
/* 166 */             long toWrite = 0L;
/* 167 */             for (Segment s = source.head; toWrite < 65536L; s = s.next) {
/* 168 */               int segmentSize = s.limit - s.pos;
/* 169 */               toWrite += segmentSize;
/* 170 */               if (toWrite >= byteCount) {
/* 171 */                 toWrite = byteCount;
/*     */                 
/*     */                 break;
/*     */               } 
/*     */             } 
/*     */             
/* 177 */             boolean throwOnTimeout = false;
/* 178 */             AsyncTimeout.this.enter();
/*     */             try {
/* 180 */               sink.write(source, toWrite);
/* 181 */               byteCount -= toWrite;
/* 182 */               throwOnTimeout = true;
/* 183 */             } catch (IOException e) {
/* 184 */               throw AsyncTimeout.this.exit(e);
/*     */             } finally {
/* 186 */               AsyncTimeout.this.exit(throwOnTimeout);
/*     */             } 
/*     */           } 
/*     */         }
/*     */         
/*     */         public void flush() throws IOException {
/* 192 */           boolean throwOnTimeout = false;
/* 193 */           AsyncTimeout.this.enter();
/*     */           try {
/* 195 */             sink.flush();
/* 196 */             throwOnTimeout = true;
/* 197 */           } catch (IOException e) {
/* 198 */             throw AsyncTimeout.this.exit(e);
/*     */           } finally {
/* 200 */             AsyncTimeout.this.exit(throwOnTimeout);
/*     */           } 
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/* 205 */           boolean throwOnTimeout = false;
/* 206 */           AsyncTimeout.this.enter();
/*     */           try {
/* 208 */             sink.close();
/* 209 */             throwOnTimeout = true;
/* 210 */           } catch (IOException e) {
/* 211 */             throw AsyncTimeout.this.exit(e);
/*     */           } finally {
/* 213 */             AsyncTimeout.this.exit(throwOnTimeout);
/*     */           } 
/*     */         }
/*     */         
/*     */         public Timeout timeout() {
/* 218 */           return AsyncTimeout.this;
/*     */         }
/*     */         
/*     */         public String toString() {
/* 222 */           return "AsyncTimeout.sink(" + sink + ")";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final Source source(final Source source) {
/* 232 */     return new Source() {
/*     */         public long read(Buffer sink, long byteCount) throws IOException {
/* 234 */           boolean throwOnTimeout = false;
/* 235 */           AsyncTimeout.this.enter();
/*     */           try {
/* 237 */             long result = source.read(sink, byteCount);
/* 238 */             throwOnTimeout = true;
/* 239 */             return result;
/* 240 */           } catch (IOException e) {
/* 241 */             throw AsyncTimeout.this.exit(e);
/*     */           } finally {
/* 243 */             AsyncTimeout.this.exit(throwOnTimeout);
/*     */           } 
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/* 248 */           boolean throwOnTimeout = false;
/*     */           try {
/* 250 */             source.close();
/* 251 */             throwOnTimeout = true;
/* 252 */           } catch (IOException e) {
/* 253 */             throw AsyncTimeout.this.exit(e);
/*     */           } finally {
/* 255 */             AsyncTimeout.this.exit(throwOnTimeout);
/*     */           } 
/*     */         }
/*     */         
/*     */         public Timeout timeout() {
/* 260 */           return AsyncTimeout.this;
/*     */         }
/*     */         
/*     */         public String toString() {
/* 264 */           return "AsyncTimeout.source(" + source + ")";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   final void exit(boolean throwOnTimeout) throws IOException {
/* 274 */     boolean timedOut = exit();
/* 275 */     if (timedOut && throwOnTimeout) throw newTimeoutException(null);
/*     */   
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   final IOException exit(IOException cause) throws IOException {
/* 284 */     if (!exit()) return cause; 
/* 285 */     return newTimeoutException(cause);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected IOException newTimeoutException(@Nullable IOException cause) {
/* 294 */     InterruptedIOException e = new InterruptedIOException("timeout");
/* 295 */     if (cause != null) {
/* 296 */       e.initCause(cause);
/*     */     }
/* 298 */     return e;
/*     */   }
/*     */   
/*     */   private static final class Watchdog extends Thread {
/*     */     Watchdog() {
/* 303 */       super("Okio Watchdog");
/* 304 */       setDaemon(true);
/*     */     }
/*     */     
/*     */     public void run() {
/*     */       while (true) {
/*     */         try {
/*     */           AsyncTimeout timedOut;
/* 311 */           synchronized (AsyncTimeout.class) {
/* 312 */             timedOut = AsyncTimeout.awaitTimeout();
/*     */ 
/*     */             
/* 315 */             if (timedOut == null) {
/*     */               continue;
/*     */             }
/*     */             
/* 319 */             if (timedOut == AsyncTimeout.head) {
/* 320 */               AsyncTimeout.head = null;
/*     */               
/*     */               return;
/*     */             } 
/*     */           } 
/*     */           
/* 326 */           timedOut.timedOut();
/* 327 */         } catch (InterruptedException timedOut) {}
/*     */       } 
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
/*     */   @Nullable
/*     */   static AsyncTimeout awaitTimeout() throws InterruptedException {
/* 342 */     AsyncTimeout node = head.next;
/*     */ 
/*     */     
/* 345 */     if (node == null) {
/* 346 */       long startNanos = System.nanoTime();
/* 347 */       AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
/* 348 */       return (head.next == null && System.nanoTime() - startNanos >= IDLE_TIMEOUT_NANOS) ? 
/* 349 */         head : 
/* 350 */         null;
/*     */     } 
/*     */     
/* 353 */     long waitNanos = node.remainingNanos(System.nanoTime());
/*     */ 
/*     */     
/* 356 */     if (waitNanos > 0L) {
/*     */ 
/*     */       
/* 359 */       long waitMillis = waitNanos / 1000000L;
/* 360 */       waitNanos -= waitMillis * 1000000L;
/* 361 */       AsyncTimeout.class.wait(waitMillis, (int)waitNanos);
/* 362 */       return null;
/*     */     } 
/*     */ 
/*     */     
/* 366 */     head.next = node.next;
/* 367 */     node.next = null;
/* 368 */     return node;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\AsyncTimeout.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */