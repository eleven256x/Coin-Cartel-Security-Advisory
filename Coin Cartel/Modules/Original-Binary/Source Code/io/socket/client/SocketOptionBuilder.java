/*     */ package io.socket.client;
/*     */ 
/*     */ import java.util.List;
/*     */ import java.util.Map;
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
/*     */ public class SocketOptionBuilder
/*     */ {
/*     */   public static SocketOptionBuilder builder() {
/*  22 */     return new SocketOptionBuilder();
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
/*     */   public static SocketOptionBuilder builder(IO.Options options) {
/*  37 */     return new SocketOptionBuilder(options);
/*     */   }
/*     */ 
/*     */   
/*  41 */   private final IO.Options options = new IO.Options();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected SocketOptionBuilder() {
/*  48 */     this(null);
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
/*     */   protected SocketOptionBuilder(IO.Options options) {
/*  62 */     if (options != null) {
/*  63 */       setForceNew(options.forceNew)
/*  64 */         .setMultiplex(options.multiplex)
/*  65 */         .setReconnection(options.reconnection)
/*  66 */         .setReconnectionAttempts(options.reconnectionAttempts)
/*  67 */         .setReconnectionDelay(options.reconnectionDelay)
/*  68 */         .setReconnectionDelayMax(options.reconnectionDelayMax)
/*  69 */         .setRandomizationFactor(options.randomizationFactor)
/*  70 */         .setTimeout(options.timeout)
/*  71 */         .setTransports(options.transports)
/*  72 */         .setUpgrade(options.upgrade)
/*  73 */         .setRememberUpgrade(options.rememberUpgrade)
/*  74 */         .setHost(options.host)
/*  75 */         .setHostname(options.hostname)
/*  76 */         .setPort(options.port)
/*  77 */         .setPolicyPort(options.policyPort)
/*  78 */         .setSecure(options.secure)
/*  79 */         .setPath(options.path)
/*  80 */         .setQuery(options.query)
/*  81 */         .setAuth(options.auth)
/*  82 */         .setExtraHeaders(options.extraHeaders);
/*     */     }
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setForceNew(boolean forceNew) {
/*  87 */     this.options.forceNew = forceNew;
/*  88 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setMultiplex(boolean multiplex) {
/*  92 */     this.options.multiplex = multiplex;
/*  93 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setReconnection(boolean reconnection) {
/*  97 */     this.options.reconnection = reconnection;
/*  98 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setReconnectionAttempts(int reconnectionAttempts) {
/* 102 */     this.options.reconnectionAttempts = reconnectionAttempts;
/* 103 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setReconnectionDelay(long reconnectionDelay) {
/* 107 */     this.options.reconnectionDelay = reconnectionDelay;
/* 108 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setReconnectionDelayMax(long reconnectionDelayMax) {
/* 112 */     this.options.reconnectionDelayMax = reconnectionDelayMax;
/* 113 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public SocketOptionBuilder setRandomizationFactor(double randomizationFactor) {
/* 118 */     this.options.randomizationFactor = randomizationFactor;
/* 119 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setTimeout(long timeout) {
/* 123 */     this.options.timeout = timeout;
/* 124 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setTransports(String[] transports) {
/* 128 */     this.options.transports = transports;
/* 129 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setUpgrade(boolean upgrade) {
/* 133 */     this.options.upgrade = upgrade;
/* 134 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setRememberUpgrade(boolean rememberUpgrade) {
/* 138 */     this.options.rememberUpgrade = rememberUpgrade;
/* 139 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setHost(String host) {
/* 143 */     this.options.host = host;
/* 144 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setHostname(String hostname) {
/* 148 */     this.options.hostname = hostname;
/* 149 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setPort(int port) {
/* 153 */     this.options.port = port;
/* 154 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setPolicyPort(int policyPort) {
/* 158 */     this.options.policyPort = policyPort;
/* 159 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setQuery(String query) {
/* 163 */     this.options.query = query;
/* 164 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setSecure(boolean secure) {
/* 168 */     this.options.secure = secure;
/* 169 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setPath(String path) {
/* 173 */     this.options.path = path;
/* 174 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setAuth(Map<String, String> auth) {
/* 178 */     this.options.auth = auth;
/* 179 */     return this;
/*     */   }
/*     */   
/*     */   public SocketOptionBuilder setExtraHeaders(Map<String, List<String>> extraHeaders) {
/* 183 */     this.options.extraHeaders = extraHeaders;
/* 184 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public IO.Options build() {
/* 194 */     return this.options;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\client\SocketOptionBuilder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */