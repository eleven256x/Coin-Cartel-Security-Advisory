/*     */ package okhttp3;
/*     */ 
/*     */ import java.util.ArrayDeque;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Collections;
/*     */ import java.util.Deque;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.SynchronousQueue;
/*     */ import java.util.concurrent.ThreadPoolExecutor;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
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
/*     */ public final class Dispatcher
/*     */ {
/*  40 */   private int maxRequests = 64;
/*  41 */   private int maxRequestsPerHost = 5;
/*     */   
/*     */   @Nullable
/*     */   private Runnable idleCallback;
/*     */   
/*     */   @Nullable
/*     */   private ExecutorService executorService;
/*  48 */   private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
/*     */ 
/*     */   
/*  51 */   private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
/*     */ 
/*     */   
/*  54 */   private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();
/*     */   
/*     */   public Dispatcher(ExecutorService executorService) {
/*  57 */     this.executorService = executorService;
/*     */   }
/*     */ 
/*     */   
/*     */   public Dispatcher() {}
/*     */   
/*     */   public synchronized ExecutorService executorService() {
/*  64 */     if (this.executorService == null) {
/*  65 */       this
/*  66 */         .executorService = new ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
/*     */     }
/*  68 */     return this.executorService;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setMaxRequests(int maxRequests) {
/*  79 */     if (maxRequests < 1) {
/*  80 */       throw new IllegalArgumentException("max < 1: " + maxRequests);
/*     */     }
/*  82 */     synchronized (this) {
/*  83 */       this.maxRequests = maxRequests;
/*     */     } 
/*  85 */     promoteAndExecute();
/*     */   }
/*     */   
/*     */   public synchronized int getMaxRequests() {
/*  89 */     return this.maxRequests;
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
/*     */   public void setMaxRequestsPerHost(int maxRequestsPerHost) {
/* 104 */     if (maxRequestsPerHost < 1) {
/* 105 */       throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
/*     */     }
/* 107 */     synchronized (this) {
/* 108 */       this.maxRequestsPerHost = maxRequestsPerHost;
/*     */     } 
/* 110 */     promoteAndExecute();
/*     */   }
/*     */   
/*     */   public synchronized int getMaxRequestsPerHost() {
/* 114 */     return this.maxRequestsPerHost;
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
/*     */   public synchronized void setIdleCallback(@Nullable Runnable idleCallback) {
/* 130 */     this.idleCallback = idleCallback;
/*     */   }
/*     */   
/*     */   void enqueue(RealCall.AsyncCall call) {
/* 134 */     synchronized (this) {
/* 135 */       this.readyAsyncCalls.add(call);
/*     */     } 
/* 137 */     promoteAndExecute();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized void cancelAll() {
/* 145 */     for (RealCall.AsyncCall call : this.readyAsyncCalls) {
/* 146 */       call.get().cancel();
/*     */     }
/*     */     
/* 149 */     for (RealCall.AsyncCall call : this.runningAsyncCalls) {
/* 150 */       call.get().cancel();
/*     */     }
/*     */     
/* 153 */     for (RealCall call : this.runningSyncCalls) {
/* 154 */       call.cancel();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean promoteAndExecute() {
/*     */     boolean isRunning;
/* 166 */     assert !Thread.holdsLock(this);
/*     */     
/* 168 */     List<RealCall.AsyncCall> executableCalls = new ArrayList<>();
/*     */     
/* 170 */     synchronized (this) {
/* 171 */       for (Iterator<RealCall.AsyncCall> iterator = this.readyAsyncCalls.iterator(); iterator.hasNext(); ) {
/* 172 */         RealCall.AsyncCall asyncCall = iterator.next();
/*     */         
/* 174 */         if (this.runningAsyncCalls.size() >= this.maxRequests)
/* 175 */           break;  if (runningCallsForHost(asyncCall) >= this.maxRequestsPerHost)
/*     */           continue; 
/* 177 */         iterator.remove();
/* 178 */         executableCalls.add(asyncCall);
/* 179 */         this.runningAsyncCalls.add(asyncCall);
/*     */       } 
/* 181 */       isRunning = (runningCallsCount() > 0);
/*     */     } 
/*     */     
/* 184 */     for (int i = 0, size = executableCalls.size(); i < size; i++) {
/* 185 */       RealCall.AsyncCall asyncCall = executableCalls.get(i);
/* 186 */       asyncCall.executeOn(executorService());
/*     */     } 
/*     */     
/* 189 */     return isRunning;
/*     */   }
/*     */ 
/*     */   
/*     */   private int runningCallsForHost(RealCall.AsyncCall call) {
/* 194 */     int result = 0;
/* 195 */     for (RealCall.AsyncCall c : this.runningAsyncCalls) {
/* 196 */       if (!(c.get()).forWebSocket && 
/* 197 */         c.host().equals(call.host())) result++; 
/*     */     } 
/* 199 */     return result;
/*     */   }
/*     */ 
/*     */   
/*     */   synchronized void executed(RealCall call) {
/* 204 */     this.runningSyncCalls.add(call);
/*     */   }
/*     */ 
/*     */   
/*     */   void finished(RealCall.AsyncCall call) {
/* 209 */     finished(this.runningAsyncCalls, call);
/*     */   }
/*     */ 
/*     */   
/*     */   void finished(RealCall call) {
/* 214 */     finished(this.runningSyncCalls, call);
/*     */   }
/*     */   
/*     */   private <T> void finished(Deque<T> calls, T call) {
/*     */     Runnable idleCallback;
/* 219 */     synchronized (this) {
/* 220 */       if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!"); 
/* 221 */       idleCallback = this.idleCallback;
/*     */     } 
/*     */     
/* 224 */     boolean isRunning = promoteAndExecute();
/*     */     
/* 226 */     if (!isRunning && idleCallback != null) {
/* 227 */       idleCallback.run();
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized List<Call> queuedCalls() {
/* 233 */     List<Call> result = new ArrayList<>();
/* 234 */     for (RealCall.AsyncCall asyncCall : this.readyAsyncCalls) {
/* 235 */       result.add(asyncCall.get());
/*     */     }
/* 237 */     return Collections.unmodifiableList(result);
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized List<Call> runningCalls() {
/* 242 */     List<Call> result = new ArrayList<>();
/* 243 */     result.addAll((Collection)this.runningSyncCalls);
/* 244 */     for (RealCall.AsyncCall asyncCall : this.runningAsyncCalls) {
/* 245 */       result.add(asyncCall.get());
/*     */     }
/* 247 */     return Collections.unmodifiableList(result);
/*     */   }
/*     */   
/*     */   public synchronized int queuedCallsCount() {
/* 251 */     return this.readyAsyncCalls.size();
/*     */   }
/*     */   
/*     */   public synchronized int runningCallsCount() {
/* 255 */     return this.runningAsyncCalls.size() + this.runningSyncCalls.size();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Dispatcher.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */