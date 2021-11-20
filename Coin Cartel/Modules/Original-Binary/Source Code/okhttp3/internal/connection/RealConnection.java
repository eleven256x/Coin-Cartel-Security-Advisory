/*     */ package okhttp3.internal.connection;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.lang.ref.Reference;
/*     */ import java.net.ConnectException;
/*     */ import java.net.ProtocolException;
/*     */ import java.net.Proxy;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.net.UnknownServiceException;
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.X509Certificate;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLPeerUnverifiedException;
/*     */ import javax.net.ssl.SSLSession;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import okhttp3.Address;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.CertificatePinner;
/*     */ import okhttp3.Connection;
/*     */ import okhttp3.ConnectionPool;
/*     */ import okhttp3.ConnectionSpec;
/*     */ import okhttp3.EventListener;
/*     */ import okhttp3.Handshake;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.Route;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.Version;
/*     */ import okhttp3.internal.http.HttpCodec;
/*     */ import okhttp3.internal.http.HttpHeaders;
/*     */ import okhttp3.internal.http1.Http1Codec;
/*     */ import okhttp3.internal.http2.ErrorCode;
/*     */ import okhttp3.internal.http2.Http2Codec;
/*     */ import okhttp3.internal.http2.Http2Connection;
/*     */ import okhttp3.internal.http2.Http2Stream;
/*     */ import okhttp3.internal.platform.Platform;
/*     */ import okhttp3.internal.tls.OkHostnameVerifier;
/*     */ import okhttp3.internal.ws.RealWebSocket;
/*     */ import okio.BufferedSink;
/*     */ import okio.BufferedSource;
/*     */ import okio.Okio;
/*     */ import okio.Source;
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
/*     */ public final class RealConnection
/*     */   extends Http2Connection.Listener
/*     */   implements Connection
/*     */ {
/*     */   private static final String NPE_THROW_WITH_NULL = "throw with null exception";
/*     */   private static final int MAX_TUNNEL_ATTEMPTS = 21;
/*     */   private final ConnectionPool connectionPool;
/*     */   private final Route route;
/*     */   private Socket rawSocket;
/*     */   private Socket socket;
/*     */   private Handshake handshake;
/*     */   private Protocol protocol;
/*     */   private Http2Connection http2Connection;
/*     */   private BufferedSource source;
/*     */   private BufferedSink sink;
/*     */   public boolean noNewStreams;
/*     */   public int successCount;
/* 111 */   public int allocationLimit = 1;
/*     */ 
/*     */   
/* 114 */   public final List<Reference<StreamAllocation>> allocations = new ArrayList<>();
/*     */ 
/*     */   
/* 117 */   public long idleAtNanos = Long.MAX_VALUE;
/*     */   
/*     */   public RealConnection(ConnectionPool connectionPool, Route route) {
/* 120 */     this.connectionPool = connectionPool;
/* 121 */     this.route = route;
/*     */   }
/*     */ 
/*     */   
/*     */   public static RealConnection testConnection(ConnectionPool connectionPool, Route route, Socket socket, long idleAtNanos) {
/* 126 */     RealConnection result = new RealConnection(connectionPool, route);
/* 127 */     result.socket = socket;
/* 128 */     result.idleAtNanos = idleAtNanos;
/* 129 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void connect(int connectTimeout, int readTimeout, int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled, Call call, EventListener eventListener) {
/* 135 */     if (this.protocol != null) throw new IllegalStateException("already connected");
/*     */     
/* 137 */     RouteException routeException = null;
/* 138 */     List<ConnectionSpec> connectionSpecs = this.route.address().connectionSpecs();
/* 139 */     ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);
/*     */     
/* 141 */     if (this.route.address().sslSocketFactory() == null) {
/* 142 */       if (!connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
/* 143 */         throw new RouteException(new UnknownServiceException("CLEARTEXT communication not enabled for client"));
/*     */       }
/*     */       
/* 146 */       String host = this.route.address().url().host();
/* 147 */       if (!Platform.get().isCleartextTrafficPermitted(host)) {
/* 148 */         throw new RouteException(new UnknownServiceException("CLEARTEXT communication to " + host + " not permitted by network security policy"));
/*     */       
/*     */       }
/*     */     }
/* 152 */     else if (this.route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
/* 153 */       throw new RouteException(new UnknownServiceException("H2_PRIOR_KNOWLEDGE cannot be used with HTTPS"));
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/*     */     while (true) {
/*     */       try {
/* 160 */         if (this.route.requiresTunnel()) {
/* 161 */           connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);
/* 162 */           if (this.rawSocket == null) {
/*     */             break;
/*     */           }
/*     */         } else {
/*     */           
/* 167 */           connectSocket(connectTimeout, readTimeout, call, eventListener);
/*     */         } 
/* 169 */         establishProtocol(connectionSpecSelector, pingIntervalMillis, call, eventListener);
/* 170 */         eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), this.protocol);
/*     */         break;
/* 172 */       } catch (IOException e) {
/* 173 */         Util.closeQuietly(this.socket);
/* 174 */         Util.closeQuietly(this.rawSocket);
/* 175 */         this.socket = null;
/* 176 */         this.rawSocket = null;
/* 177 */         this.source = null;
/* 178 */         this.sink = null;
/* 179 */         this.handshake = null;
/* 180 */         this.protocol = null;
/* 181 */         this.http2Connection = null;
/*     */         
/* 183 */         eventListener.connectFailed(call, this.route.socketAddress(), this.route.proxy(), null, e);
/*     */         
/* 185 */         if (routeException == null) {
/* 186 */           routeException = new RouteException(e);
/*     */         } else {
/* 188 */           routeException.addConnectException(e);
/*     */         } 
/*     */         
/* 191 */         if (!connectionRetryEnabled || !connectionSpecSelector.connectionFailed(e)) {
/* 192 */           throw routeException;
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 197 */     if (this.route.requiresTunnel() && this.rawSocket == null) {
/* 198 */       ProtocolException exception = new ProtocolException("Too many tunnel connections attempted: 21");
/*     */       
/* 200 */       throw new RouteException(exception);
/*     */     } 
/*     */     
/* 203 */     if (this.http2Connection != null) {
/* 204 */       synchronized (this.connectionPool) {
/* 205 */         this.allocationLimit = this.http2Connection.maxConcurrentStreams();
/*     */       } 
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout, Call call, EventListener eventListener) throws IOException {
/* 216 */     Request tunnelRequest = createTunnelRequest();
/* 217 */     HttpUrl url = tunnelRequest.url();
/* 218 */     for (int i = 0; i < 21; i++) {
/* 219 */       connectSocket(connectTimeout, readTimeout, call, eventListener);
/* 220 */       tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);
/*     */       
/* 222 */       if (tunnelRequest == null) {
/*     */         break;
/*     */       }
/*     */       
/* 226 */       Util.closeQuietly(this.rawSocket);
/* 227 */       this.rawSocket = null;
/* 228 */       this.sink = null;
/* 229 */       this.source = null;
/* 230 */       eventListener.connectEnd(call, this.route.socketAddress(), this.route.proxy(), null);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void connectSocket(int connectTimeout, int readTimeout, Call call, EventListener eventListener) throws IOException {
/* 237 */     Proxy proxy = this.route.proxy();
/* 238 */     Address address = this.route.address();
/*     */     
/* 240 */     this
/*     */       
/* 242 */       .rawSocket = (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP) ? address.socketFactory().createSocket() : new Socket(proxy);
/*     */     
/* 244 */     eventListener.connectStart(call, this.route.socketAddress(), proxy);
/* 245 */     this.rawSocket.setSoTimeout(readTimeout);
/*     */     try {
/* 247 */       Platform.get().connectSocket(this.rawSocket, this.route.socketAddress(), connectTimeout);
/* 248 */     } catch (ConnectException e) {
/* 249 */       ConnectException ce = new ConnectException("Failed to connect to " + this.route.socketAddress());
/* 250 */       ce.initCause(e);
/* 251 */       throw ce;
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 259 */       this.source = Okio.buffer(Okio.source(this.rawSocket));
/* 260 */       this.sink = Okio.buffer(Okio.sink(this.rawSocket));
/* 261 */     } catch (NullPointerException npe) {
/* 262 */       if ("throw with null exception".equals(npe.getMessage())) {
/* 263 */         throw new IOException(npe);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void establishProtocol(ConnectionSpecSelector connectionSpecSelector, int pingIntervalMillis, Call call, EventListener eventListener) throws IOException {
/* 270 */     if (this.route.address().sslSocketFactory() == null) {
/* 271 */       if (this.route.address().protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)) {
/* 272 */         this.socket = this.rawSocket;
/* 273 */         this.protocol = Protocol.H2_PRIOR_KNOWLEDGE;
/* 274 */         startHttp2(pingIntervalMillis);
/*     */         
/*     */         return;
/*     */       } 
/* 278 */       this.socket = this.rawSocket;
/* 279 */       this.protocol = Protocol.HTTP_1_1;
/*     */       
/*     */       return;
/*     */     } 
/* 283 */     eventListener.secureConnectStart(call);
/* 284 */     connectTls(connectionSpecSelector);
/* 285 */     eventListener.secureConnectEnd(call, this.handshake);
/*     */     
/* 287 */     if (this.protocol == Protocol.HTTP_2) {
/* 288 */       startHttp2(pingIntervalMillis);
/*     */     }
/*     */   }
/*     */   
/*     */   private void startHttp2(int pingIntervalMillis) throws IOException {
/* 293 */     this.socket.setSoTimeout(0);
/* 294 */     this
/*     */ 
/*     */ 
/*     */       
/* 298 */       .http2Connection = (new Http2Connection.Builder(true)).socket(this.socket, this.route.address().url().host(), this.source, this.sink).listener(this).pingIntervalMillis(pingIntervalMillis).build();
/* 299 */     this.http2Connection.start();
/*     */   }
/*     */   
/*     */   private void connectTls(ConnectionSpecSelector connectionSpecSelector) throws IOException {
/* 303 */     Address address = this.route.address();
/* 304 */     SSLSocketFactory sslSocketFactory = address.sslSocketFactory();
/* 305 */     boolean success = false;
/* 306 */     SSLSocket sslSocket = null;
/*     */     
/*     */     try {
/* 309 */       sslSocket = (SSLSocket)sslSocketFactory.createSocket(this.rawSocket, address
/* 310 */           .url().host(), address.url().port(), true);
/*     */ 
/*     */       
/* 313 */       ConnectionSpec connectionSpec = connectionSpecSelector.configureSecureSocket(sslSocket);
/* 314 */       if (connectionSpec.supportsTlsExtensions()) {
/* 315 */         Platform.get().configureTlsExtensions(sslSocket, address
/* 316 */             .url().host(), address.protocols());
/*     */       }
/*     */ 
/*     */       
/* 320 */       sslSocket.startHandshake();
/*     */       
/* 322 */       SSLSession sslSocketSession = sslSocket.getSession();
/* 323 */       Handshake unverifiedHandshake = Handshake.get(sslSocketSession);
/*     */ 
/*     */       
/* 326 */       if (!address.hostnameVerifier().verify(address.url().host(), sslSocketSession)) {
/* 327 */         List<Certificate> peerCertificates = unverifiedHandshake.peerCertificates();
/* 328 */         if (!peerCertificates.isEmpty()) {
/* 329 */           X509Certificate cert = (X509Certificate)peerCertificates.get(0);
/* 330 */           throw new SSLPeerUnverifiedException("Hostname " + address
/* 331 */               .url().host() + " not verified:\n    certificate: " + 
/* 332 */               CertificatePinner.pin(cert) + "\n    DN: " + cert
/* 333 */               .getSubjectDN().getName() + "\n    subjectAltNames: " + 
/* 334 */               OkHostnameVerifier.allSubjectAltNames(cert));
/*     */         } 
/* 336 */         throw new SSLPeerUnverifiedException("Hostname " + address
/* 337 */             .url().host() + " not verified (no certificates)");
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 342 */       address.certificatePinner().check(address.url().host(), unverifiedHandshake
/* 343 */           .peerCertificates());
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 348 */       String maybeProtocol = connectionSpec.supportsTlsExtensions() ? Platform.get().getSelectedProtocol(sslSocket) : null;
/* 349 */       this.socket = sslSocket;
/* 350 */       this.source = Okio.buffer(Okio.source(this.socket));
/* 351 */       this.sink = Okio.buffer(Okio.sink(this.socket));
/* 352 */       this.handshake = unverifiedHandshake;
/* 353 */       this
/*     */         
/* 355 */         .protocol = (maybeProtocol != null) ? Protocol.get(maybeProtocol) : Protocol.HTTP_1_1;
/* 356 */       success = true;
/* 357 */     } catch (AssertionError e) {
/* 358 */       if (Util.isAndroidGetsocknameError(e)) throw new IOException(e); 
/* 359 */       throw e;
/*     */     } finally {
/* 361 */       if (sslSocket != null) {
/* 362 */         Platform.get().afterHandshake(sslSocket);
/*     */       }
/* 364 */       if (!success) {
/* 365 */         Util.closeQuietly(sslSocket);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest, HttpUrl url) throws IOException {
/*     */     Response response;
/* 377 */     String requestLine = "CONNECT " + Util.hostHeader(url, true) + " HTTP/1.1";
/*     */     while (true) {
/* 379 */       Http1Codec tunnelConnection = new Http1Codec(null, null, this.source, this.sink);
/* 380 */       this.source.timeout().timeout(readTimeout, TimeUnit.MILLISECONDS);
/* 381 */       this.sink.timeout().timeout(writeTimeout, TimeUnit.MILLISECONDS);
/* 382 */       tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
/* 383 */       tunnelConnection.finishRequest();
/*     */ 
/*     */       
/* 386 */       response = tunnelConnection.readResponseHeaders(false).request(tunnelRequest).build();
/*     */ 
/*     */       
/* 389 */       long contentLength = HttpHeaders.contentLength(response);
/* 390 */       if (contentLength == -1L) {
/* 391 */         contentLength = 0L;
/*     */       }
/* 393 */       Source body = tunnelConnection.newFixedLengthSource(contentLength);
/* 394 */       Util.skipAll(body, 2147483647, TimeUnit.MILLISECONDS);
/* 395 */       body.close();
/*     */       
/* 397 */       switch (response.code()) {
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         case 200:
/* 403 */           if (!this.source.buffer().exhausted() || !this.sink.buffer().exhausted()) {
/* 404 */             throw new IOException("TLS tunnel buffered too many bytes!");
/*     */           }
/* 406 */           return null;
/*     */         
/*     */         case 407:
/* 409 */           tunnelRequest = this.route.address().proxyAuthenticator().authenticate(this.route, response);
/* 410 */           if (tunnelRequest == null) throw new IOException("Failed to authenticate with proxy");
/*     */           
/* 412 */           if ("close".equalsIgnoreCase(response.header("Connection")))
/* 413 */             return tunnelRequest; 
/*     */           continue;
/*     */       } 
/*     */       break;
/*     */     } 
/* 418 */     throw new IOException("Unexpected response code for CONNECT: " + response
/* 419 */         .code());
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
/*     */   private Request createTunnelRequest() throws IOException {
/* 440 */     Request proxyConnectRequest = (new Request.Builder()).url(this.route.address().url()).method("CONNECT", null).header("Host", Util.hostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header("User-Agent", Version.userAgent()).build();
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
/* 451 */     Response fakeAuthChallengeResponse = (new Response.Builder()).request(proxyConnectRequest).protocol(Protocol.HTTP_1_1).code(407).message("Preemptive Authenticate").body(Util.EMPTY_RESPONSE).sentRequestAtMillis(-1L).receivedResponseAtMillis(-1L).header("Proxy-Authenticate", "OkHttp-Preemptive").build();
/*     */ 
/*     */     
/* 454 */     Request authenticatedRequest = this.route.address().proxyAuthenticator().authenticate(this.route, fakeAuthChallengeResponse);
/*     */     
/* 456 */     return (authenticatedRequest != null) ? 
/* 457 */       authenticatedRequest : 
/* 458 */       proxyConnectRequest;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isEligible(Address address, @Nullable Route route) {
/* 467 */     if (this.allocations.size() >= this.allocationLimit || this.noNewStreams) return false;
/*     */ 
/*     */     
/* 470 */     if (!Internal.instance.equalsNonHost(this.route.address(), address)) return false;
/*     */ 
/*     */     
/* 473 */     if (address.url().host().equals(route().address().url().host())) {
/* 474 */       return true;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 483 */     if (this.http2Connection == null) return false;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 488 */     if (route == null) return false; 
/* 489 */     if (route.proxy().type() != Proxy.Type.DIRECT) return false; 
/* 490 */     if (this.route.proxy().type() != Proxy.Type.DIRECT) return false; 
/* 491 */     if (!this.route.socketAddress().equals(route.socketAddress())) return false;
/*     */ 
/*     */     
/* 494 */     if (route.address().hostnameVerifier() != OkHostnameVerifier.INSTANCE) return false; 
/* 495 */     if (!supportsUrl(address.url())) return false;
/*     */ 
/*     */     
/*     */     try {
/* 499 */       address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
/* 500 */     } catch (SSLPeerUnverifiedException e) {
/* 501 */       return false;
/*     */     } 
/*     */     
/* 504 */     return true;
/*     */   }
/*     */   
/*     */   public boolean supportsUrl(HttpUrl url) {
/* 508 */     if (url.port() != this.route.address().url().port()) {
/* 509 */       return false;
/*     */     }
/*     */     
/* 512 */     if (!url.host().equals(this.route.address().url().host()))
/*     */     {
/* 514 */       return (this.handshake != null && OkHostnameVerifier.INSTANCE.verify(url
/* 515 */           .host(), this.handshake.peerCertificates().get(0)));
/*     */     }
/*     */     
/* 518 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   public HttpCodec newCodec(OkHttpClient client, Interceptor.Chain chain, StreamAllocation streamAllocation) throws SocketException {
/* 523 */     if (this.http2Connection != null) {
/* 524 */       return (HttpCodec)new Http2Codec(client, chain, streamAllocation, this.http2Connection);
/*     */     }
/* 526 */     this.socket.setSoTimeout(chain.readTimeoutMillis());
/* 527 */     this.source.timeout().timeout(chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
/* 528 */     this.sink.timeout().timeout(chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
/* 529 */     return (HttpCodec)new Http1Codec(client, streamAllocation, this.source, this.sink);
/*     */   }
/*     */ 
/*     */   
/*     */   public RealWebSocket.Streams newWebSocketStreams(final StreamAllocation streamAllocation) {
/* 534 */     return new RealWebSocket.Streams(true, this.source, this.sink) {
/*     */         public void close() throws IOException {
/* 536 */           streamAllocation.streamFinished(true, streamAllocation.codec(), -1L, null);
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   public Route route() {
/* 542 */     return this.route;
/*     */   }
/*     */ 
/*     */   
/*     */   public void cancel() {
/* 547 */     Util.closeQuietly(this.rawSocket);
/*     */   }
/*     */   
/*     */   public Socket socket() {
/* 551 */     return this.socket;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean isHealthy(boolean doExtensiveChecks) {
/* 556 */     if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
/* 557 */       return false;
/*     */     }
/*     */     
/* 560 */     if (this.http2Connection != null) {
/* 561 */       return this.http2Connection.isHealthy(System.nanoTime());
/*     */     }
/*     */     
/* 564 */     if (doExtensiveChecks) {
/*     */       try {
/* 566 */         int readTimeout = this.socket.getSoTimeout();
/*     */         try {
/* 568 */           this.socket.setSoTimeout(1);
/* 569 */           if (this.source.exhausted()) {
/* 570 */             return false;
/*     */           }
/* 572 */           return true;
/*     */         } finally {
/* 574 */           this.socket.setSoTimeout(readTimeout);
/*     */         } 
/* 576 */       } catch (SocketTimeoutException socketTimeoutException) {
/*     */       
/* 578 */       } catch (IOException e) {
/* 579 */         return false;
/*     */       } 
/*     */     }
/*     */     
/* 583 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   public void onStream(Http2Stream stream) throws IOException {
/* 588 */     stream.close(ErrorCode.REFUSED_STREAM);
/*     */   }
/*     */ 
/*     */   
/*     */   public void onSettings(Http2Connection connection) {
/* 593 */     synchronized (this.connectionPool) {
/* 594 */       this.allocationLimit = connection.maxConcurrentStreams();
/*     */     } 
/*     */   }
/*     */   
/*     */   public Handshake handshake() {
/* 599 */     return this.handshake;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isMultiplexed() {
/* 607 */     return (this.http2Connection != null);
/*     */   }
/*     */   
/*     */   public Protocol protocol() {
/* 611 */     return this.protocol;
/*     */   }
/*     */   
/*     */   public String toString() {
/* 615 */     return "Connection{" + this.route
/* 616 */       .address().url().host() + ":" + this.route.address().url().port() + ", proxy=" + this.route
/*     */       
/* 618 */       .proxy() + " hostAddress=" + this.route
/*     */       
/* 620 */       .socketAddress() + " cipherSuite=" + (
/*     */       
/* 622 */       (this.handshake != null) ? (String)this.handshake.cipherSuite() : "none") + " protocol=" + this.protocol + '}';
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\connection\RealConnection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */