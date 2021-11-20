/*     */ package okhttp3;
/*     */ 
/*     */ import java.lang.ref.Reference;
/*     */ import java.net.Socket;
/*     */ import java.util.ArrayDeque;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Deque;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.Executor;
/*     */ import java.util.concurrent.SynchronousQueue;
/*     */ import java.util.concurrent.ThreadPoolExecutor;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.RealConnection;
/*     */ import okhttp3.internal.connection.RouteDatabase;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ import okhttp3.internal.platform.Platform;
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
/*     */ public final class ConnectionPool
/*     */ {
/*  50 */   private static final Executor executor = new ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), 
/*     */       
/*  52 */       Util.threadFactory("OkHttp ConnectionPool", true));
/*     */   
/*     */   private final int maxIdleConnections;
/*     */   private final long keepAliveDurationNs;
/*     */   
/*  57 */   private final Runnable cleanupRunnable = new Runnable() {
/*     */       public void run() {
/*     */         while (true) {
/*  60 */           long waitNanos = ConnectionPool.this.cleanup(System.nanoTime());
/*  61 */           if (waitNanos == -1L)
/*  62 */             return;  if (waitNanos > 0L) {
/*  63 */             long waitMillis = waitNanos / 1000000L;
/*  64 */             waitNanos -= waitMillis * 1000000L;
/*  65 */             synchronized (ConnectionPool.this) {
/*     */               try {
/*  67 */                 ConnectionPool.this.wait(waitMillis, (int)waitNanos);
/*  68 */               } catch (InterruptedException interruptedException) {}
/*     */             } 
/*     */           } 
/*     */         } 
/*     */       }
/*     */     };
/*     */ 
/*     */   
/*  76 */   private final Deque<RealConnection> connections = new ArrayDeque<>();
/*  77 */   final RouteDatabase routeDatabase = new RouteDatabase();
/*     */ 
/*     */ 
/*     */   
/*     */   boolean cleanupRunning;
/*     */ 
/*     */ 
/*     */   
/*     */   public ConnectionPool() {
/*  86 */     this(5, 5L, TimeUnit.MINUTES);
/*     */   }
/*     */   
/*     */   public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
/*  90 */     this.maxIdleConnections = maxIdleConnections;
/*  91 */     this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);
/*     */ 
/*     */     
/*  94 */     if (keepAliveDuration <= 0L) {
/*  95 */       throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized int idleConnectionCount() {
/* 101 */     int total = 0;
/* 102 */     for (RealConnection connection : this.connections) {
/* 103 */       if (connection.allocations.isEmpty()) total++; 
/*     */     } 
/* 105 */     return total;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized int connectionCount() {
/* 115 */     return this.connections.size();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   RealConnection get(Address address, StreamAllocation streamAllocation, Route route) {
/* 123 */     assert Thread.holdsLock(this);
/* 124 */     for (RealConnection connection : this.connections) {
/* 125 */       if (connection.isEligible(address, route)) {
/* 126 */         streamAllocation.acquire(connection, true);
/* 127 */         return connection;
/*     */       } 
/*     */     } 
/* 130 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   Socket deduplicate(Address address, StreamAllocation streamAllocation) {
/* 138 */     assert Thread.holdsLock(this);
/* 139 */     for (RealConnection connection : this.connections) {
/* 140 */       if (connection.isEligible(address, null) && connection
/* 141 */         .isMultiplexed() && connection != streamAllocation
/* 142 */         .connection()) {
/* 143 */         return streamAllocation.releaseAndAcquire(connection);
/*     */       }
/*     */     } 
/* 146 */     return null;
/*     */   }
/*     */   
/*     */   void put(RealConnection connection) {
/* 150 */     assert Thread.holdsLock(this);
/* 151 */     if (!this.cleanupRunning) {
/* 152 */       this.cleanupRunning = true;
/* 153 */       executor.execute(this.cleanupRunnable);
/*     */     } 
/* 155 */     this.connections.add(connection);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   boolean connectionBecameIdle(RealConnection connection) {
/* 163 */     assert Thread.holdsLock(this);
/* 164 */     if (connection.noNewStreams || this.maxIdleConnections == 0) {
/* 165 */       this.connections.remove(connection);
/* 166 */       return true;
/*     */     } 
/* 168 */     notifyAll();
/* 169 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void evictAll() {
/* 175 */     List<RealConnection> evictedConnections = new ArrayList<>();
/* 176 */     synchronized (this) {
/* 177 */       for (Iterator<RealConnection> i = this.connections.iterator(); i.hasNext(); ) {
/* 178 */         RealConnection connection = i.next();
/* 179 */         if (connection.allocations.isEmpty()) {
/* 180 */           connection.noNewStreams = true;
/* 181 */           evictedConnections.add(connection);
/* 182 */           i.remove();
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 187 */     for (RealConnection connection : evictedConnections) {
/* 188 */       Util.closeQuietly(connection.socket());
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
/*     */   long cleanup(long now) {
/* 200 */     int inUseConnectionCount = 0;
/* 201 */     int idleConnectionCount = 0;
/* 202 */     RealConnection longestIdleConnection = null;
/* 203 */     long longestIdleDurationNs = Long.MIN_VALUE;
/*     */ 
/*     */     
/* 206 */     synchronized (this) {
/* 207 */       for (Iterator<RealConnection> i = this.connections.iterator(); i.hasNext(); ) {
/* 208 */         RealConnection connection = i.next();
/*     */ 
/*     */         
/* 211 */         if (pruneAndGetAllocationCount(connection, now) > 0) {
/* 212 */           inUseConnectionCount++;
/*     */           
/*     */           continue;
/*     */         } 
/* 216 */         idleConnectionCount++;
/*     */ 
/*     */         
/* 219 */         long idleDurationNs = now - connection.idleAtNanos;
/* 220 */         if (idleDurationNs > longestIdleDurationNs) {
/* 221 */           longestIdleDurationNs = idleDurationNs;
/* 222 */           longestIdleConnection = connection;
/*     */         } 
/*     */       } 
/*     */       
/* 226 */       if (longestIdleDurationNs >= this.keepAliveDurationNs || idleConnectionCount > this.maxIdleConnections)
/*     */       
/*     */       { 
/*     */         
/* 230 */         this.connections.remove(longestIdleConnection); }
/* 231 */       else { if (idleConnectionCount > 0)
/*     */         {
/* 233 */           return this.keepAliveDurationNs - longestIdleDurationNs; } 
/* 234 */         if (inUseConnectionCount > 0)
/*     */         {
/* 236 */           return this.keepAliveDurationNs;
/*     */         }
/*     */         
/* 239 */         this.cleanupRunning = false;
/* 240 */         return -1L; }
/*     */     
/*     */     } 
/*     */     
/* 244 */     Util.closeQuietly(longestIdleConnection.socket());
/*     */ 
/*     */     
/* 247 */     return 0L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int pruneAndGetAllocationCount(RealConnection connection, long now) {
/* 257 */     List<Reference<StreamAllocation>> references = connection.allocations;
/* 258 */     for (int i = 0; i < references.size(); ) {
/* 259 */       Reference<StreamAllocation> reference = references.get(i);
/*     */       
/* 261 */       if (reference.get() != null) {
/* 262 */         i++;
/*     */         
/*     */         continue;
/*     */       } 
/*     */       
/* 267 */       StreamAllocation.StreamAllocationReference streamAllocRef = (StreamAllocation.StreamAllocationReference)reference;
/*     */       
/* 269 */       String message = "A connection to " + connection.route().address().url() + " was leaked. Did you forget to close a response body?";
/*     */       
/* 271 */       Platform.get().logCloseableLeak(message, streamAllocRef.callStackTrace);
/*     */       
/* 273 */       references.remove(i);
/* 274 */       connection.noNewStreams = true;
/*     */ 
/*     */       
/* 277 */       if (references.isEmpty()) {
/* 278 */         connection.idleAtNanos = now - this.keepAliveDurationNs;
/* 279 */         return 0;
/*     */       } 
/*     */     } 
/*     */     
/* 283 */     return references.size();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\ConnectionPool.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */