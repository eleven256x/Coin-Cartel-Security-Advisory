/*      */ package okhttp3;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.net.Proxy;
/*      */ import java.net.ProxySelector;
/*      */ import java.net.Socket;
/*      */ import java.security.GeneralSecurityException;
/*      */ import java.time.Duration;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.List;
/*      */ import java.util.Random;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import javax.annotation.Nullable;
/*      */ import javax.net.SocketFactory;
/*      */ import javax.net.ssl.HostnameVerifier;
/*      */ import javax.net.ssl.SSLContext;
/*      */ import javax.net.ssl.SSLSocket;
/*      */ import javax.net.ssl.SSLSocketFactory;
/*      */ import javax.net.ssl.TrustManager;
/*      */ import javax.net.ssl.X509TrustManager;
/*      */ import okhttp3.internal.Internal;
/*      */ import okhttp3.internal.Util;
/*      */ import okhttp3.internal.cache.InternalCache;
/*      */ import okhttp3.internal.connection.RealConnection;
/*      */ import okhttp3.internal.connection.RouteDatabase;
/*      */ import okhttp3.internal.connection.StreamAllocation;
/*      */ import okhttp3.internal.platform.Platform;
/*      */ import okhttp3.internal.proxy.NullProxySelector;
/*      */ import okhttp3.internal.tls.CertificateChainCleaner;
/*      */ import okhttp3.internal.tls.OkHostnameVerifier;
/*      */ import okhttp3.internal.ws.RealWebSocket;
/*      */ import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class OkHttpClient
/*      */   implements Cloneable, Call.Factory, WebSocket.Factory
/*      */ {
/*  127 */   static final List<Protocol> DEFAULT_PROTOCOLS = Util.immutableList((Object[])new Protocol[] { Protocol.HTTP_2, Protocol.HTTP_1_1 });
/*      */ 
/*      */   
/*  130 */   static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList((Object[])new ConnectionSpec[] { ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT }); final Dispatcher dispatcher; @Nullable
/*      */   final Proxy proxy; final List<Protocol> protocols;
/*      */   
/*      */   static {
/*  134 */     Internal.instance = new Internal() {
/*      */         public void addLenient(Headers.Builder builder, String line) {
/*  136 */           builder.addLenient(line);
/*      */         }
/*      */         
/*      */         public void addLenient(Headers.Builder builder, String name, String value) {
/*  140 */           builder.addLenient(name, value);
/*      */         }
/*      */         
/*      */         public void setCache(OkHttpClient.Builder builder, InternalCache internalCache) {
/*  144 */           builder.setInternalCache(internalCache);
/*      */         }
/*      */ 
/*      */         
/*      */         public boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection) {
/*  149 */           return pool.connectionBecameIdle(connection);
/*      */         }
/*      */ 
/*      */         
/*      */         public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation, Route route) {
/*  154 */           return pool.get(address, streamAllocation, route);
/*      */         }
/*      */         
/*      */         public boolean equalsNonHost(Address a, Address b) {
/*  158 */           return a.equalsNonHost(b);
/*      */         }
/*      */ 
/*      */         
/*      */         public Socket deduplicate(ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
/*  163 */           return pool.deduplicate(address, streamAllocation);
/*      */         }
/*      */         
/*      */         public void put(ConnectionPool pool, RealConnection connection) {
/*  167 */           pool.put(connection);
/*      */         }
/*      */         
/*      */         public RouteDatabase routeDatabase(ConnectionPool connectionPool) {
/*  171 */           return connectionPool.routeDatabase;
/*      */         }
/*      */         
/*      */         public int code(Response.Builder responseBuilder) {
/*  175 */           return responseBuilder.code;
/*      */         }
/*      */ 
/*      */         
/*      */         public void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
/*  180 */           tlsConfiguration.apply(sslSocket, isFallback);
/*      */         }
/*      */         
/*      */         public boolean isInvalidHttpUrlHost(IllegalArgumentException e) {
/*  184 */           return e.getMessage().startsWith("Invalid URL host");
/*      */         }
/*      */         
/*      */         public StreamAllocation streamAllocation(Call call) {
/*  188 */           return ((RealCall)call).streamAllocation();
/*      */         }
/*      */         @Nullable
/*      */         public IOException timeoutExit(Call call, @Nullable IOException e) {
/*  192 */           return ((RealCall)call).timeoutExit(e);
/*      */         }
/*      */         
/*      */         public Call newWebSocketCall(OkHttpClient client, Request originalRequest) {
/*  196 */           return RealCall.newRealCall(client, originalRequest, true);
/*      */         }
/*      */       };
/*      */   }
/*      */ 
/*      */   
/*      */   final List<ConnectionSpec> connectionSpecs;
/*      */   final List<Interceptor> interceptors;
/*      */   final List<Interceptor> networkInterceptors;
/*      */   final EventListener.Factory eventListenerFactory;
/*      */   final ProxySelector proxySelector;
/*      */   final CookieJar cookieJar;
/*      */   @Nullable
/*      */   final Cache cache;
/*      */   @Nullable
/*      */   final InternalCache internalCache;
/*      */   final SocketFactory socketFactory;
/*      */   final SSLSocketFactory sslSocketFactory;
/*      */   final CertificateChainCleaner certificateChainCleaner;
/*      */   final HostnameVerifier hostnameVerifier;
/*      */   final CertificatePinner certificatePinner;
/*      */   final Authenticator proxyAuthenticator;
/*      */   final Authenticator authenticator;
/*      */   final ConnectionPool connectionPool;
/*      */   final Dns dns;
/*      */   final boolean followSslRedirects;
/*      */   final boolean followRedirects;
/*      */   final boolean retryOnConnectionFailure;
/*      */   final int callTimeout;
/*      */   final int connectTimeout;
/*      */   final int readTimeout;
/*      */   final int writeTimeout;
/*      */   final int pingInterval;
/*      */   
/*      */   public OkHttpClient() {
/*  231 */     this(new Builder());
/*      */   }
/*      */   
/*      */   OkHttpClient(Builder builder) {
/*  235 */     this.dispatcher = builder.dispatcher;
/*  236 */     this.proxy = builder.proxy;
/*  237 */     this.protocols = builder.protocols;
/*  238 */     this.connectionSpecs = builder.connectionSpecs;
/*  239 */     this.interceptors = Util.immutableList(builder.interceptors);
/*  240 */     this.networkInterceptors = Util.immutableList(builder.networkInterceptors);
/*  241 */     this.eventListenerFactory = builder.eventListenerFactory;
/*  242 */     this.proxySelector = builder.proxySelector;
/*  243 */     this.cookieJar = builder.cookieJar;
/*  244 */     this.cache = builder.cache;
/*  245 */     this.internalCache = builder.internalCache;
/*  246 */     this.socketFactory = builder.socketFactory;
/*      */     
/*  248 */     boolean isTLS = false;
/*  249 */     for (ConnectionSpec spec : this.connectionSpecs) {
/*  250 */       isTLS = (isTLS || spec.isTls());
/*      */     }
/*      */     
/*  253 */     if (builder.sslSocketFactory != null || !isTLS) {
/*  254 */       this.sslSocketFactory = builder.sslSocketFactory;
/*  255 */       this.certificateChainCleaner = builder.certificateChainCleaner;
/*      */     } else {
/*  257 */       X509TrustManager trustManager = Util.platformTrustManager();
/*  258 */       this.sslSocketFactory = newSslSocketFactory(trustManager);
/*  259 */       this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
/*      */     } 
/*      */     
/*  262 */     if (this.sslSocketFactory != null) {
/*  263 */       Platform.get().configureSslSocketFactory(this.sslSocketFactory);
/*      */     }
/*      */     
/*  266 */     this.hostnameVerifier = builder.hostnameVerifier;
/*  267 */     this.certificatePinner = builder.certificatePinner.withCertificateChainCleaner(this.certificateChainCleaner);
/*      */     
/*  269 */     this.proxyAuthenticator = builder.proxyAuthenticator;
/*  270 */     this.authenticator = builder.authenticator;
/*  271 */     this.connectionPool = builder.connectionPool;
/*  272 */     this.dns = builder.dns;
/*  273 */     this.followSslRedirects = builder.followSslRedirects;
/*  274 */     this.followRedirects = builder.followRedirects;
/*  275 */     this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
/*  276 */     this.callTimeout = builder.callTimeout;
/*  277 */     this.connectTimeout = builder.connectTimeout;
/*  278 */     this.readTimeout = builder.readTimeout;
/*  279 */     this.writeTimeout = builder.writeTimeout;
/*  280 */     this.pingInterval = builder.pingInterval;
/*      */     
/*  282 */     if (this.interceptors.contains(null)) {
/*  283 */       throw new IllegalStateException("Null interceptor: " + this.interceptors);
/*      */     }
/*  285 */     if (this.networkInterceptors.contains(null)) {
/*  286 */       throw new IllegalStateException("Null network interceptor: " + this.networkInterceptors);
/*      */     }
/*      */   }
/*      */   
/*      */   private static SSLSocketFactory newSslSocketFactory(X509TrustManager trustManager) {
/*      */     try {
/*  292 */       SSLContext sslContext = Platform.get().getSSLContext();
/*  293 */       sslContext.init(null, new TrustManager[] { trustManager }, null);
/*  294 */       return sslContext.getSocketFactory();
/*  295 */     } catch (GeneralSecurityException e) {
/*  296 */       throw Util.assertionError("No System TLS", e);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public int callTimeoutMillis() {
/*  302 */     return this.callTimeout;
/*      */   }
/*      */ 
/*      */   
/*      */   public int connectTimeoutMillis() {
/*  307 */     return this.connectTimeout;
/*      */   }
/*      */ 
/*      */   
/*      */   public int readTimeoutMillis() {
/*  312 */     return this.readTimeout;
/*      */   }
/*      */ 
/*      */   
/*      */   public int writeTimeoutMillis() {
/*  317 */     return this.writeTimeout;
/*      */   }
/*      */ 
/*      */   
/*      */   public int pingIntervalMillis() {
/*  322 */     return this.pingInterval;
/*      */   }
/*      */   @Nullable
/*      */   public Proxy proxy() {
/*  326 */     return this.proxy;
/*      */   }
/*      */   
/*      */   public ProxySelector proxySelector() {
/*  330 */     return this.proxySelector;
/*      */   }
/*      */   
/*      */   public CookieJar cookieJar() {
/*  334 */     return this.cookieJar;
/*      */   }
/*      */   @Nullable
/*      */   public Cache cache() {
/*  338 */     return this.cache;
/*      */   }
/*      */   
/*      */   InternalCache internalCache() {
/*  342 */     return (this.cache != null) ? this.cache.internalCache : this.internalCache;
/*      */   }
/*      */   
/*      */   public Dns dns() {
/*  346 */     return this.dns;
/*      */   }
/*      */   
/*      */   public SocketFactory socketFactory() {
/*  350 */     return this.socketFactory;
/*      */   }
/*      */   
/*      */   public SSLSocketFactory sslSocketFactory() {
/*  354 */     return this.sslSocketFactory;
/*      */   }
/*      */   
/*      */   public HostnameVerifier hostnameVerifier() {
/*  358 */     return this.hostnameVerifier;
/*      */   }
/*      */   
/*      */   public CertificatePinner certificatePinner() {
/*  362 */     return this.certificatePinner;
/*      */   }
/*      */   
/*      */   public Authenticator authenticator() {
/*  366 */     return this.authenticator;
/*      */   }
/*      */   
/*      */   public Authenticator proxyAuthenticator() {
/*  370 */     return this.proxyAuthenticator;
/*      */   }
/*      */   
/*      */   public ConnectionPool connectionPool() {
/*  374 */     return this.connectionPool;
/*      */   }
/*      */   
/*      */   public boolean followSslRedirects() {
/*  378 */     return this.followSslRedirects;
/*      */   }
/*      */   
/*      */   public boolean followRedirects() {
/*  382 */     return this.followRedirects;
/*      */   }
/*      */   
/*      */   public boolean retryOnConnectionFailure() {
/*  386 */     return this.retryOnConnectionFailure;
/*      */   }
/*      */   
/*      */   public Dispatcher dispatcher() {
/*  390 */     return this.dispatcher;
/*      */   }
/*      */   
/*      */   public List<Protocol> protocols() {
/*  394 */     return this.protocols;
/*      */   }
/*      */   
/*      */   public List<ConnectionSpec> connectionSpecs() {
/*  398 */     return this.connectionSpecs;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Interceptor> interceptors() {
/*  407 */     return this.interceptors;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Interceptor> networkInterceptors() {
/*  416 */     return this.networkInterceptors;
/*      */   }
/*      */   
/*      */   public EventListener.Factory eventListenerFactory() {
/*  420 */     return this.eventListenerFactory;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Call newCall(Request request) {
/*  427 */     return RealCall.newRealCall(this, request, false);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public WebSocket newWebSocket(Request request, WebSocketListener listener) {
/*  434 */     RealWebSocket webSocket = new RealWebSocket(request, listener, new Random(), this.pingInterval);
/*  435 */     webSocket.connect(this);
/*  436 */     return (WebSocket)webSocket;
/*      */   }
/*      */   
/*      */   public Builder newBuilder() {
/*  440 */     return new Builder(this);
/*      */   }
/*      */   
/*      */   public static final class Builder { Dispatcher dispatcher;
/*      */     @Nullable
/*      */     Proxy proxy;
/*      */     List<Protocol> protocols;
/*      */     List<ConnectionSpec> connectionSpecs;
/*  448 */     final List<Interceptor> interceptors = new ArrayList<>();
/*  449 */     final List<Interceptor> networkInterceptors = new ArrayList<>();
/*      */     
/*      */     EventListener.Factory eventListenerFactory;
/*      */     
/*      */     ProxySelector proxySelector;
/*      */     
/*      */     CookieJar cookieJar;
/*      */     
/*      */     @Nullable
/*      */     Cache cache;
/*      */     
/*      */     @Nullable
/*      */     InternalCache internalCache;
/*      */     
/*      */     SocketFactory socketFactory;
/*      */     
/*      */     @Nullable
/*      */     SSLSocketFactory sslSocketFactory;
/*      */     @Nullable
/*      */     CertificateChainCleaner certificateChainCleaner;
/*      */     HostnameVerifier hostnameVerifier;
/*      */     CertificatePinner certificatePinner;
/*      */     Authenticator proxyAuthenticator;
/*      */     
/*      */     public Builder() {
/*  474 */       this.dispatcher = new Dispatcher();
/*  475 */       this.protocols = OkHttpClient.DEFAULT_PROTOCOLS;
/*  476 */       this.connectionSpecs = OkHttpClient.DEFAULT_CONNECTION_SPECS;
/*  477 */       this.eventListenerFactory = EventListener.factory(EventListener.NONE);
/*  478 */       this.proxySelector = ProxySelector.getDefault();
/*  479 */       if (this.proxySelector == null) {
/*  480 */         this.proxySelector = (ProxySelector)new NullProxySelector();
/*      */       }
/*  482 */       this.cookieJar = CookieJar.NO_COOKIES;
/*  483 */       this.socketFactory = SocketFactory.getDefault();
/*  484 */       this.hostnameVerifier = (HostnameVerifier)OkHostnameVerifier.INSTANCE;
/*  485 */       this.certificatePinner = CertificatePinner.DEFAULT;
/*  486 */       this.proxyAuthenticator = Authenticator.NONE;
/*  487 */       this.authenticator = Authenticator.NONE;
/*  488 */       this.connectionPool = new ConnectionPool();
/*  489 */       this.dns = Dns.SYSTEM;
/*  490 */       this.followSslRedirects = true;
/*  491 */       this.followRedirects = true;
/*  492 */       this.retryOnConnectionFailure = true;
/*  493 */       this.callTimeout = 0;
/*  494 */       this.connectTimeout = 10000;
/*  495 */       this.readTimeout = 10000;
/*  496 */       this.writeTimeout = 10000;
/*  497 */       this.pingInterval = 0;
/*      */     }
/*      */     Authenticator authenticator; ConnectionPool connectionPool; Dns dns; boolean followSslRedirects; boolean followRedirects; boolean retryOnConnectionFailure; int callTimeout; int connectTimeout; int readTimeout; int writeTimeout; int pingInterval;
/*      */     Builder(OkHttpClient okHttpClient) {
/*  501 */       this.dispatcher = okHttpClient.dispatcher;
/*  502 */       this.proxy = okHttpClient.proxy;
/*  503 */       this.protocols = okHttpClient.protocols;
/*  504 */       this.connectionSpecs = okHttpClient.connectionSpecs;
/*  505 */       this.interceptors.addAll(okHttpClient.interceptors);
/*  506 */       this.networkInterceptors.addAll(okHttpClient.networkInterceptors);
/*  507 */       this.eventListenerFactory = okHttpClient.eventListenerFactory;
/*  508 */       this.proxySelector = okHttpClient.proxySelector;
/*  509 */       this.cookieJar = okHttpClient.cookieJar;
/*  510 */       this.internalCache = okHttpClient.internalCache;
/*  511 */       this.cache = okHttpClient.cache;
/*  512 */       this.socketFactory = okHttpClient.socketFactory;
/*  513 */       this.sslSocketFactory = okHttpClient.sslSocketFactory;
/*  514 */       this.certificateChainCleaner = okHttpClient.certificateChainCleaner;
/*  515 */       this.hostnameVerifier = okHttpClient.hostnameVerifier;
/*  516 */       this.certificatePinner = okHttpClient.certificatePinner;
/*  517 */       this.proxyAuthenticator = okHttpClient.proxyAuthenticator;
/*  518 */       this.authenticator = okHttpClient.authenticator;
/*  519 */       this.connectionPool = okHttpClient.connectionPool;
/*  520 */       this.dns = okHttpClient.dns;
/*  521 */       this.followSslRedirects = okHttpClient.followSslRedirects;
/*  522 */       this.followRedirects = okHttpClient.followRedirects;
/*  523 */       this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;
/*  524 */       this.callTimeout = okHttpClient.callTimeout;
/*  525 */       this.connectTimeout = okHttpClient.connectTimeout;
/*  526 */       this.readTimeout = okHttpClient.readTimeout;
/*  527 */       this.writeTimeout = okHttpClient.writeTimeout;
/*  528 */       this.pingInterval = okHttpClient.pingInterval;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder callTimeout(long timeout, TimeUnit unit) {
/*  540 */       this.callTimeout = Util.checkDuration("timeout", timeout, unit);
/*  541 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     @IgnoreJRERequirement
/*      */     public Builder callTimeout(Duration duration) {
/*  554 */       this.callTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
/*  555 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder connectTimeout(long timeout, TimeUnit unit) {
/*  567 */       this.connectTimeout = Util.checkDuration("timeout", timeout, unit);
/*  568 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     @IgnoreJRERequirement
/*      */     public Builder connectTimeout(Duration duration) {
/*  581 */       this.connectTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
/*  582 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder readTimeout(long timeout, TimeUnit unit) {
/*  596 */       this.readTimeout = Util.checkDuration("timeout", timeout, unit);
/*  597 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     @IgnoreJRERequirement
/*      */     public Builder readTimeout(Duration duration) {
/*  612 */       this.readTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
/*  613 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder writeTimeout(long timeout, TimeUnit unit) {
/*  626 */       this.writeTimeout = Util.checkDuration("timeout", timeout, unit);
/*  627 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     @IgnoreJRERequirement
/*      */     public Builder writeTimeout(Duration duration) {
/*  641 */       this.writeTimeout = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
/*  642 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder pingInterval(long interval, TimeUnit unit) {
/*  659 */       this.pingInterval = Util.checkDuration("interval", interval, unit);
/*  660 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     @IgnoreJRERequirement
/*      */     public Builder pingInterval(Duration duration) {
/*  678 */       this.pingInterval = Util.checkDuration("timeout", duration.toMillis(), TimeUnit.MILLISECONDS);
/*  679 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder proxy(@Nullable Proxy proxy) {
/*  688 */       this.proxy = proxy;
/*  689 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder proxySelector(ProxySelector proxySelector) {
/*  701 */       if (proxySelector == null) throw new NullPointerException("proxySelector == null"); 
/*  702 */       this.proxySelector = proxySelector;
/*  703 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder cookieJar(CookieJar cookieJar) {
/*  713 */       if (cookieJar == null) throw new NullPointerException("cookieJar == null"); 
/*  714 */       this.cookieJar = cookieJar;
/*  715 */       return this;
/*      */     }
/*      */ 
/*      */     
/*      */     void setInternalCache(@Nullable InternalCache internalCache) {
/*  720 */       this.internalCache = internalCache;
/*  721 */       this.cache = null;
/*      */     }
/*      */ 
/*      */     
/*      */     public Builder cache(@Nullable Cache cache) {
/*  726 */       this.cache = cache;
/*  727 */       this.internalCache = null;
/*  728 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder dns(Dns dns) {
/*  737 */       if (dns == null) throw new NullPointerException("dns == null"); 
/*  738 */       this.dns = dns;
/*  739 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder socketFactory(SocketFactory socketFactory) {
/*  751 */       if (socketFactory == null) throw new NullPointerException("socketFactory == null"); 
/*  752 */       this.socketFactory = socketFactory;
/*  753 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
/*  766 */       if (sslSocketFactory == null) throw new NullPointerException("sslSocketFactory == null"); 
/*  767 */       this.sslSocketFactory = sslSocketFactory;
/*  768 */       this.certificateChainCleaner = Platform.get().buildCertificateChainCleaner(sslSocketFactory);
/*  769 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
/*  804 */       if (sslSocketFactory == null) throw new NullPointerException("sslSocketFactory == null"); 
/*  805 */       if (trustManager == null) throw new NullPointerException("trustManager == null"); 
/*  806 */       this.sslSocketFactory = sslSocketFactory;
/*  807 */       this.certificateChainCleaner = CertificateChainCleaner.get(trustManager);
/*  808 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
/*  818 */       if (hostnameVerifier == null) throw new NullPointerException("hostnameVerifier == null"); 
/*  819 */       this.hostnameVerifier = hostnameVerifier;
/*  820 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder certificatePinner(CertificatePinner certificatePinner) {
/*  829 */       if (certificatePinner == null) throw new NullPointerException("certificatePinner == null"); 
/*  830 */       this.certificatePinner = certificatePinner;
/*  831 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder authenticator(Authenticator authenticator) {
/*  841 */       if (authenticator == null) throw new NullPointerException("authenticator == null"); 
/*  842 */       this.authenticator = authenticator;
/*  843 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder proxyAuthenticator(Authenticator proxyAuthenticator) {
/*  853 */       if (proxyAuthenticator == null) throw new NullPointerException("proxyAuthenticator == null"); 
/*  854 */       this.proxyAuthenticator = proxyAuthenticator;
/*  855 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder connectionPool(ConnectionPool connectionPool) {
/*  864 */       if (connectionPool == null) throw new NullPointerException("connectionPool == null"); 
/*  865 */       this.connectionPool = connectionPool;
/*  866 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder followSslRedirects(boolean followProtocolRedirects) {
/*  876 */       this.followSslRedirects = followProtocolRedirects;
/*  877 */       return this;
/*      */     }
/*      */ 
/*      */     
/*      */     public Builder followRedirects(boolean followRedirects) {
/*  882 */       this.followRedirects = followRedirects;
/*  883 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
/*  905 */       this.retryOnConnectionFailure = retryOnConnectionFailure;
/*  906 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder dispatcher(Dispatcher dispatcher) {
/*  913 */       if (dispatcher == null) throw new IllegalArgumentException("dispatcher == null"); 
/*  914 */       this.dispatcher = dispatcher;
/*  915 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder protocols(List<Protocol> protocols) {
/*  951 */       protocols = new ArrayList<>(protocols);
/*      */ 
/*      */       
/*  954 */       if (!protocols.contains(Protocol.H2_PRIOR_KNOWLEDGE) && 
/*  955 */         !protocols.contains(Protocol.HTTP_1_1)) {
/*  956 */         throw new IllegalArgumentException("protocols must contain h2_prior_knowledge or http/1.1: " + protocols);
/*      */       }
/*      */       
/*  959 */       if (protocols.contains(Protocol.H2_PRIOR_KNOWLEDGE) && protocols.size() > 1) {
/*  960 */         throw new IllegalArgumentException("protocols containing h2_prior_knowledge cannot use other protocols: " + protocols);
/*      */       }
/*      */       
/*  963 */       if (protocols.contains(Protocol.HTTP_1_0)) {
/*  964 */         throw new IllegalArgumentException("protocols must not contain http/1.0: " + protocols);
/*      */       }
/*  966 */       if (protocols.contains(null)) {
/*  967 */         throw new IllegalArgumentException("protocols must not contain null");
/*      */       }
/*      */ 
/*      */       
/*  971 */       protocols.remove(Protocol.SPDY_3);
/*      */ 
/*      */       
/*  974 */       this.protocols = Collections.unmodifiableList(protocols);
/*  975 */       return this;
/*      */     }
/*      */     
/*      */     public Builder connectionSpecs(List<ConnectionSpec> connectionSpecs) {
/*  979 */       this.connectionSpecs = Util.immutableList(connectionSpecs);
/*  980 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public List<Interceptor> interceptors() {
/*  989 */       return this.interceptors;
/*      */     }
/*      */     
/*      */     public Builder addInterceptor(Interceptor interceptor) {
/*  993 */       if (interceptor == null) throw new IllegalArgumentException("interceptor == null"); 
/*  994 */       this.interceptors.add(interceptor);
/*  995 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public List<Interceptor> networkInterceptors() {
/* 1004 */       return this.networkInterceptors;
/*      */     }
/*      */     
/*      */     public Builder addNetworkInterceptor(Interceptor interceptor) {
/* 1008 */       if (interceptor == null) throw new IllegalArgumentException("interceptor == null"); 
/* 1009 */       this.networkInterceptors.add(interceptor);
/* 1010 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder eventListener(EventListener eventListener) {
/* 1020 */       if (eventListener == null) throw new NullPointerException("eventListener == null"); 
/* 1021 */       this.eventListenerFactory = EventListener.factory(eventListener);
/* 1022 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder eventListenerFactory(EventListener.Factory eventListenerFactory) {
/* 1032 */       if (eventListenerFactory == null) {
/* 1033 */         throw new NullPointerException("eventListenerFactory == null");
/*      */       }
/* 1035 */       this.eventListenerFactory = eventListenerFactory;
/* 1036 */       return this;
/*      */     }
/*      */     
/*      */     public OkHttpClient build() {
/* 1040 */       return new OkHttpClient(this);
/*      */     } }
/*      */ 
/*      */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\OkHttpClient.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */