/*     */ package okhttp3.internal.connection;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.lang.ref.Reference;
/*     */ import java.lang.ref.WeakReference;
/*     */ import java.net.Socket;
/*     */ import java.util.List;
/*     */ import okhttp3.Address;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.Connection;
/*     */ import okhttp3.ConnectionPool;
/*     */ import okhttp3.EventListener;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Route;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.http.HttpCodec;
/*     */ import okhttp3.internal.http2.ErrorCode;
/*     */ import okhttp3.internal.http2.StreamResetException;
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
/*     */ public final class StreamAllocation
/*     */ {
/*     */   public final Address address;
/*     */   private RouteSelector.Selection routeSelection;
/*     */   private Route route;
/*     */   private final ConnectionPool connectionPool;
/*     */   public final Call call;
/*     */   public final EventListener eventListener;
/*     */   private final Object callStackTrace;
/*     */   private final RouteSelector routeSelector;
/*     */   private int refusedStreamCount;
/*     */   private RealConnection connection;
/*     */   private boolean reportedAcquired;
/*     */   private boolean released;
/*     */   private boolean canceled;
/*     */   private HttpCodec codec;
/*     */   
/*     */   public StreamAllocation(ConnectionPool connectionPool, Address address, Call call, EventListener eventListener, Object callStackTrace) {
/*  97 */     this.connectionPool = connectionPool;
/*  98 */     this.address = address;
/*  99 */     this.call = call;
/* 100 */     this.eventListener = eventListener;
/* 101 */     this.routeSelector = new RouteSelector(address, routeDatabase(), call, eventListener);
/* 102 */     this.callStackTrace = callStackTrace;
/*     */   }
/*     */ 
/*     */   
/*     */   public HttpCodec newStream(OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
/* 107 */     int connectTimeout = chain.connectTimeoutMillis();
/* 108 */     int readTimeout = chain.readTimeoutMillis();
/* 109 */     int writeTimeout = chain.writeTimeoutMillis();
/* 110 */     int pingIntervalMillis = client.pingIntervalMillis();
/* 111 */     boolean connectionRetryEnabled = client.retryOnConnectionFailure();
/*     */     
/*     */     try {
/* 114 */       RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
/*     */       
/* 116 */       HttpCodec resultCodec = resultConnection.newCodec(client, chain, this);
/*     */       
/* 118 */       synchronized (this.connectionPool) {
/* 119 */         this.codec = resultCodec;
/* 120 */         return resultCodec;
/*     */       } 
/* 122 */     } catch (IOException e) {
/* 123 */       throw new RouteException(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private RealConnection findHealthyConnection(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws IOException {
/*     */     RealConnection candidate;
/*     */     while (true) {
/* 135 */       candidate = findConnection(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled);
/*     */ 
/*     */ 
/*     */       
/* 139 */       synchronized (this.connectionPool) {
/* 140 */         if (candidate.successCount == 0 && !candidate.isMultiplexed()) {
/* 141 */           return candidate;
/*     */         }
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 147 */       if (!candidate.isHealthy(doExtensiveHealthChecks)) {
/* 148 */         noNewStreams(); continue;
/*     */       } 
/*     */       break;
/*     */     } 
/* 152 */     return candidate;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
/*     */     Connection releasedConnection;
/*     */     Socket toClose;
/* 162 */     boolean foundPooledConnection = false;
/* 163 */     RealConnection result = null;
/* 164 */     Route selectedRoute = null;
/*     */ 
/*     */     
/* 167 */     synchronized (this.connectionPool) {
/* 168 */       if (this.released) throw new IllegalStateException("released"); 
/* 169 */       if (this.codec != null) throw new IllegalStateException("codec != null"); 
/* 170 */       if (this.canceled) throw new IOException("Canceled");
/*     */ 
/*     */ 
/*     */       
/* 174 */       releasedConnection = this.connection;
/* 175 */       toClose = releaseIfNoNewStreams();
/* 176 */       if (this.connection != null) {
/*     */         
/* 178 */         result = this.connection;
/* 179 */         releasedConnection = null;
/*     */       } 
/* 181 */       if (!this.reportedAcquired)
/*     */       {
/* 183 */         releasedConnection = null;
/*     */       }
/*     */       
/* 186 */       if (result == null) {
/*     */         
/* 188 */         Internal.instance.get(this.connectionPool, this.address, this, null);
/* 189 */         if (this.connection != null) {
/* 190 */           foundPooledConnection = true;
/* 191 */           result = this.connection;
/*     */         } else {
/* 193 */           selectedRoute = this.route;
/*     */         } 
/*     */       } 
/*     */     } 
/* 197 */     Util.closeQuietly(toClose);
/*     */     
/* 199 */     if (releasedConnection != null) {
/* 200 */       this.eventListener.connectionReleased(this.call, releasedConnection);
/*     */     }
/* 202 */     if (foundPooledConnection) {
/* 203 */       this.eventListener.connectionAcquired(this.call, result);
/*     */     }
/* 205 */     if (result != null) {
/*     */       
/* 207 */       this.route = this.connection.route();
/* 208 */       return result;
/*     */     } 
/*     */ 
/*     */     
/* 212 */     boolean newRouteSelection = false;
/* 213 */     if (selectedRoute == null && (this.routeSelection == null || !this.routeSelection.hasNext())) {
/* 214 */       newRouteSelection = true;
/* 215 */       this.routeSelection = this.routeSelector.next();
/*     */     } 
/*     */     
/* 218 */     synchronized (this.connectionPool) {
/* 219 */       if (this.canceled) throw new IOException("Canceled");
/*     */       
/* 221 */       if (newRouteSelection) {
/*     */ 
/*     */         
/* 224 */         List<Route> routes = this.routeSelection.getAll();
/* 225 */         for (int i = 0, size = routes.size(); i < size; i++) {
/* 226 */           Route route = routes.get(i);
/* 227 */           Internal.instance.get(this.connectionPool, this.address, this, route);
/* 228 */           if (this.connection != null) {
/* 229 */             foundPooledConnection = true;
/* 230 */             result = this.connection;
/* 231 */             this.route = route;
/*     */             
/*     */             break;
/*     */           } 
/*     */         } 
/*     */       } 
/* 237 */       if (!foundPooledConnection) {
/* 238 */         if (selectedRoute == null) {
/* 239 */           selectedRoute = this.routeSelection.next();
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 244 */         this.route = selectedRoute;
/* 245 */         this.refusedStreamCount = 0;
/* 246 */         result = new RealConnection(this.connectionPool, selectedRoute);
/* 247 */         acquire(result, false);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 252 */     if (foundPooledConnection) {
/* 253 */       this.eventListener.connectionAcquired(this.call, result);
/* 254 */       return result;
/*     */     } 
/*     */ 
/*     */     
/* 258 */     result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis, connectionRetryEnabled, this.call, this.eventListener);
/*     */     
/* 260 */     routeDatabase().connected(result.route());
/*     */     
/* 262 */     Socket socket = null;
/* 263 */     synchronized (this.connectionPool) {
/* 264 */       this.reportedAcquired = true;
/*     */ 
/*     */       
/* 267 */       Internal.instance.put(this.connectionPool, result);
/*     */ 
/*     */ 
/*     */       
/* 271 */       if (result.isMultiplexed()) {
/* 272 */         socket = Internal.instance.deduplicate(this.connectionPool, this.address, this);
/* 273 */         result = this.connection;
/*     */       } 
/*     */     } 
/* 276 */     Util.closeQuietly(socket);
/*     */     
/* 278 */     this.eventListener.connectionAcquired(this.call, result);
/* 279 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Socket releaseIfNoNewStreams() {
/* 289 */     assert Thread.holdsLock(this.connectionPool);
/* 290 */     RealConnection allocatedConnection = this.connection;
/* 291 */     if (allocatedConnection != null && allocatedConnection.noNewStreams) {
/* 292 */       return deallocate(false, false, true);
/*     */     }
/* 294 */     return null; } public void streamFinished(boolean noNewStreams, HttpCodec codec, long bytesRead, IOException e) {
/*     */     Connection releasedConnection;
/*     */     Socket socket;
/*     */     boolean callEnd;
/* 298 */     this.eventListener.responseBodyEnd(this.call, bytesRead);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 303 */     synchronized (this.connectionPool) {
/* 304 */       if (codec == null || codec != this.codec) {
/* 305 */         throw new IllegalStateException("expected " + this.codec + " but was " + codec);
/*     */       }
/* 307 */       if (!noNewStreams) {
/* 308 */         this.connection.successCount++;
/*     */       }
/* 310 */       releasedConnection = this.connection;
/* 311 */       socket = deallocate(noNewStreams, false, true);
/* 312 */       if (this.connection != null) releasedConnection = null; 
/* 313 */       callEnd = this.released;
/*     */     } 
/* 315 */     Util.closeQuietly(socket);
/* 316 */     if (releasedConnection != null) {
/* 317 */       this.eventListener.connectionReleased(this.call, releasedConnection);
/*     */     }
/*     */     
/* 320 */     if (e != null) {
/* 321 */       e = Internal.instance.timeoutExit(this.call, e);
/* 322 */       this.eventListener.callFailed(this.call, e);
/* 323 */     } else if (callEnd) {
/* 324 */       Internal.instance.timeoutExit(this.call, null);
/* 325 */       this.eventListener.callEnd(this.call);
/*     */     } 
/*     */   }
/*     */   
/*     */   public HttpCodec codec() {
/* 330 */     synchronized (this.connectionPool) {
/* 331 */       return this.codec;
/*     */     } 
/*     */   }
/*     */   
/*     */   private RouteDatabase routeDatabase() {
/* 336 */     return Internal.instance.routeDatabase(this.connectionPool);
/*     */   }
/*     */   
/*     */   public Route route() {
/* 340 */     return this.route;
/*     */   }
/*     */   
/*     */   public synchronized RealConnection connection() {
/* 344 */     return this.connection;
/*     */   }
/*     */   
/*     */   public void release() {
/*     */     Connection releasedConnection;
/*     */     Socket socket;
/* 350 */     synchronized (this.connectionPool) {
/* 351 */       releasedConnection = this.connection;
/* 352 */       socket = deallocate(false, true, false);
/* 353 */       if (this.connection != null) releasedConnection = null; 
/*     */     } 
/* 355 */     Util.closeQuietly(socket);
/* 356 */     if (releasedConnection != null) {
/* 357 */       Internal.instance.timeoutExit(this.call, null);
/* 358 */       this.eventListener.connectionReleased(this.call, releasedConnection);
/* 359 */       this.eventListener.callEnd(this.call);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void noNewStreams() {
/*     */     Connection releasedConnection;
/*     */     Socket socket;
/* 367 */     synchronized (this.connectionPool) {
/* 368 */       releasedConnection = this.connection;
/* 369 */       socket = deallocate(true, false, false);
/* 370 */       if (this.connection != null) releasedConnection = null; 
/*     */     } 
/* 372 */     Util.closeQuietly(socket);
/* 373 */     if (releasedConnection != null) {
/* 374 */       this.eventListener.connectionReleased(this.call, releasedConnection);
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
/*     */   private Socket deallocate(boolean noNewStreams, boolean released, boolean streamFinished) {
/* 386 */     assert Thread.holdsLock(this.connectionPool);
/*     */     
/* 388 */     if (streamFinished) {
/* 389 */       this.codec = null;
/*     */     }
/* 391 */     if (released) {
/* 392 */       this.released = true;
/*     */     }
/* 394 */     Socket socket = null;
/* 395 */     if (this.connection != null) {
/* 396 */       if (noNewStreams) {
/* 397 */         this.connection.noNewStreams = true;
/*     */       }
/* 399 */       if (this.codec == null && (this.released || this.connection.noNewStreams)) {
/* 400 */         release(this.connection);
/* 401 */         if (this.connection.allocations.isEmpty()) {
/* 402 */           this.connection.idleAtNanos = System.nanoTime();
/* 403 */           if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
/* 404 */             socket = this.connection.socket();
/*     */           }
/*     */         } 
/* 407 */         this.connection = null;
/*     */       } 
/*     */     } 
/* 410 */     return socket;
/*     */   }
/*     */   
/*     */   public void cancel() {
/*     */     HttpCodec codecToCancel;
/*     */     RealConnection connectionToCancel;
/* 416 */     synchronized (this.connectionPool) {
/* 417 */       this.canceled = true;
/* 418 */       codecToCancel = this.codec;
/* 419 */       connectionToCancel = this.connection;
/*     */     } 
/* 421 */     if (codecToCancel != null) {
/* 422 */       codecToCancel.cancel();
/* 423 */     } else if (connectionToCancel != null) {
/* 424 */       connectionToCancel.cancel();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void streamFailed(IOException e) {
/*     */     Connection releasedConnection;
/*     */     Socket socket;
/* 431 */     boolean noNewStreams = false;
/*     */     
/* 433 */     synchronized (this.connectionPool) {
/* 434 */       if (e instanceof StreamResetException) {
/* 435 */         ErrorCode errorCode = ((StreamResetException)e).errorCode;
/* 436 */         if (errorCode == ErrorCode.REFUSED_STREAM) {
/*     */           
/* 438 */           this.refusedStreamCount++;
/* 439 */           if (this.refusedStreamCount > 1) {
/* 440 */             noNewStreams = true;
/* 441 */             this.route = null;
/*     */           } 
/* 443 */         } else if (errorCode != ErrorCode.CANCEL) {
/*     */           
/* 445 */           noNewStreams = true;
/* 446 */           this.route = null;
/*     */         } 
/* 448 */       } else if (this.connection != null && (
/* 449 */         !this.connection.isMultiplexed() || e instanceof okhttp3.internal.http2.ConnectionShutdownException)) {
/* 450 */         noNewStreams = true;
/*     */ 
/*     */         
/* 453 */         if (this.connection.successCount == 0) {
/* 454 */           if (this.route != null && e != null) {
/* 455 */             this.routeSelector.connectFailed(this.route, e);
/*     */           }
/* 457 */           this.route = null;
/*     */         } 
/*     */       } 
/* 460 */       releasedConnection = this.connection;
/* 461 */       socket = deallocate(noNewStreams, false, true);
/* 462 */       if (this.connection != null || !this.reportedAcquired) releasedConnection = null;
/*     */     
/*     */     } 
/* 465 */     Util.closeQuietly(socket);
/* 466 */     if (releasedConnection != null) {
/* 467 */       this.eventListener.connectionReleased(this.call, releasedConnection);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void acquire(RealConnection connection, boolean reportedAcquired) {
/* 476 */     assert Thread.holdsLock(this.connectionPool);
/* 477 */     if (this.connection != null) throw new IllegalStateException();
/*     */     
/* 479 */     this.connection = connection;
/* 480 */     this.reportedAcquired = reportedAcquired;
/* 481 */     connection.allocations.add(new StreamAllocationReference(this, this.callStackTrace));
/*     */   }
/*     */ 
/*     */   
/*     */   private void release(RealConnection connection) {
/* 486 */     for (int i = 0, size = connection.allocations.size(); i < size; i++) {
/* 487 */       Reference<StreamAllocation> reference = connection.allocations.get(i);
/* 488 */       if (reference.get() == this) {
/* 489 */         connection.allocations.remove(i);
/*     */         return;
/*     */       } 
/*     */     } 
/* 493 */     throw new IllegalStateException();
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
/*     */   public Socket releaseAndAcquire(RealConnection newConnection) {
/* 505 */     assert Thread.holdsLock(this.connectionPool);
/* 506 */     if (this.codec != null || this.connection.allocations.size() != 1) throw new IllegalStateException();
/*     */ 
/*     */     
/* 509 */     Reference<StreamAllocation> onlyAllocation = this.connection.allocations.get(0);
/* 510 */     Socket socket = deallocate(true, false, false);
/*     */ 
/*     */     
/* 513 */     this.connection = newConnection;
/* 514 */     newConnection.allocations.add(onlyAllocation);
/*     */     
/* 516 */     return socket;
/*     */   }
/*     */   
/*     */   public boolean hasMoreRoutes() {
/* 520 */     return (this.route != null || (this.routeSelection != null && this.routeSelection
/* 521 */       .hasNext()) || this.routeSelector
/* 522 */       .hasNext());
/*     */   }
/*     */   
/*     */   public String toString() {
/* 526 */     RealConnection connection = connection();
/* 527 */     return (connection != null) ? connection.toString() : this.address.toString();
/*     */   }
/*     */ 
/*     */   
/*     */   public static final class StreamAllocationReference
/*     */     extends WeakReference<StreamAllocation>
/*     */   {
/*     */     public final Object callStackTrace;
/*     */ 
/*     */     
/*     */     StreamAllocationReference(StreamAllocation referent, Object callStackTrace) {
/* 538 */       super(referent);
/* 539 */       this.callStackTrace = callStackTrace;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\connection\StreamAllocation.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */