/*     */ package okhttp3;
/*     */ 
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLSocket;
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
/*     */ public final class ConnectionSpec
/*     */ {
/*  44 */   private static final CipherSuite[] RESTRICTED_CIPHER_SUITES = new CipherSuite[] { CipherSuite.TLS_AES_128_GCM_SHA256, CipherSuite.TLS_AES_256_GCM_SHA384, CipherSuite.TLS_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_AES_128_CCM_SHA256, CipherSuite.TLS_AES_256_CCM_8_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 };
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
/*  63 */   private static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[] { CipherSuite.TLS_AES_128_GCM_SHA256, CipherSuite.TLS_AES_256_GCM_SHA384, CipherSuite.TLS_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_AES_128_CCM_SHA256, CipherSuite.TLS_AES_256_CCM_8_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA };
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
/*  91 */   public static final ConnectionSpec RESTRICTED_TLS = (new Builder(true))
/*  92 */     .cipherSuites(RESTRICTED_CIPHER_SUITES)
/*  93 */     .tlsVersions(new TlsVersion[] { TlsVersion.TLS_1_3, TlsVersion.TLS_1_2
/*  94 */       }).supportsTlsExtensions(true)
/*  95 */     .build();
/*     */ 
/*     */   
/*  98 */   public static final ConnectionSpec MODERN_TLS = (new Builder(true))
/*  99 */     .cipherSuites(APPROVED_CIPHER_SUITES)
/* 100 */     .tlsVersions(new TlsVersion[] { TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0
/* 101 */       }).supportsTlsExtensions(true)
/* 102 */     .build();
/*     */ 
/*     */   
/* 105 */   public static final ConnectionSpec COMPATIBLE_TLS = (new Builder(true))
/* 106 */     .cipherSuites(APPROVED_CIPHER_SUITES)
/* 107 */     .tlsVersions(new TlsVersion[] { TlsVersion.TLS_1_0
/* 108 */       }).supportsTlsExtensions(true)
/* 109 */     .build();
/*     */ 
/*     */   
/* 112 */   public static final ConnectionSpec CLEARTEXT = (new Builder(false)).build(); final boolean tls;
/*     */   final boolean supportsTlsExtensions;
/*     */   @Nullable
/*     */   final String[] cipherSuites;
/*     */   @Nullable
/*     */   final String[] tlsVersions;
/*     */   
/*     */   ConnectionSpec(Builder builder) {
/* 120 */     this.tls = builder.tls;
/* 121 */     this.cipherSuites = builder.cipherSuites;
/* 122 */     this.tlsVersions = builder.tlsVersions;
/* 123 */     this.supportsTlsExtensions = builder.supportsTlsExtensions;
/*     */   }
/*     */   
/*     */   public boolean isTls() {
/* 127 */     return this.tls;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public List<CipherSuite> cipherSuites() {
/* 135 */     return (this.cipherSuites != null) ? CipherSuite.forJavaNames(this.cipherSuites) : null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public List<TlsVersion> tlsVersions() {
/* 143 */     return (this.tlsVersions != null) ? TlsVersion.forJavaNames(this.tlsVersions) : null;
/*     */   }
/*     */   
/*     */   public boolean supportsTlsExtensions() {
/* 147 */     return this.supportsTlsExtensions;
/*     */   }
/*     */ 
/*     */   
/*     */   void apply(SSLSocket sslSocket, boolean isFallback) {
/* 152 */     ConnectionSpec specToApply = supportedSpec(sslSocket, isFallback);
/*     */     
/* 154 */     if (specToApply.tlsVersions != null) {
/* 155 */       sslSocket.setEnabledProtocols(specToApply.tlsVersions);
/*     */     }
/* 157 */     if (specToApply.cipherSuites != null) {
/* 158 */       sslSocket.setEnabledCipherSuites(specToApply.cipherSuites);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private ConnectionSpec supportedSpec(SSLSocket sslSocket, boolean isFallback) {
/* 169 */     String[] cipherSuitesIntersection = (this.cipherSuites != null) ? Util.intersect(CipherSuite.ORDER_BY_NAME, sslSocket.getEnabledCipherSuites(), this.cipherSuites) : sslSocket.getEnabledCipherSuites();
/*     */ 
/*     */     
/* 172 */     String[] tlsVersionsIntersection = (this.tlsVersions != null) ? Util.intersect(Util.NATURAL_ORDER, sslSocket.getEnabledProtocols(), this.tlsVersions) : sslSocket.getEnabledProtocols();
/*     */ 
/*     */ 
/*     */     
/* 176 */     String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
/* 177 */     int indexOfFallbackScsv = Util.indexOf(CipherSuite.ORDER_BY_NAME, supportedCipherSuites, "TLS_FALLBACK_SCSV");
/*     */     
/* 179 */     if (isFallback && indexOfFallbackScsv != -1) {
/* 180 */       cipherSuitesIntersection = Util.concat(cipherSuitesIntersection, supportedCipherSuites[indexOfFallbackScsv]);
/*     */     }
/*     */ 
/*     */     
/* 184 */     return (new Builder(this))
/* 185 */       .cipherSuites(cipherSuitesIntersection)
/* 186 */       .tlsVersions(tlsVersionsIntersection)
/* 187 */       .build();
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
/*     */   public boolean isCompatible(SSLSocket socket) {
/* 202 */     if (!this.tls) {
/* 203 */       return false;
/*     */     }
/*     */     
/* 206 */     if (this.tlsVersions != null && !Util.nonEmptyIntersection(Util.NATURAL_ORDER, this.tlsVersions, socket
/* 207 */         .getEnabledProtocols())) {
/* 208 */       return false;
/*     */     }
/*     */     
/* 211 */     if (this.cipherSuites != null && !Util.nonEmptyIntersection(CipherSuite.ORDER_BY_NAME, this.cipherSuites, socket
/* 212 */         .getEnabledCipherSuites())) {
/* 213 */       return false;
/*     */     }
/*     */     
/* 216 */     return true;
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/* 220 */     if (!(other instanceof ConnectionSpec)) return false; 
/* 221 */     if (other == this) return true;
/*     */     
/* 223 */     ConnectionSpec that = (ConnectionSpec)other;
/* 224 */     if (this.tls != that.tls) return false;
/*     */     
/* 226 */     if (this.tls) {
/* 227 */       if (!Arrays.equals((Object[])this.cipherSuites, (Object[])that.cipherSuites)) return false; 
/* 228 */       if (!Arrays.equals((Object[])this.tlsVersions, (Object[])that.tlsVersions)) return false; 
/* 229 */       if (this.supportsTlsExtensions != that.supportsTlsExtensions) return false;
/*     */     
/*     */     } 
/* 232 */     return true;
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 236 */     int result = 17;
/* 237 */     if (this.tls) {
/* 238 */       result = 31 * result + Arrays.hashCode((Object[])this.cipherSuites);
/* 239 */       result = 31 * result + Arrays.hashCode((Object[])this.tlsVersions);
/* 240 */       result = 31 * result + (this.supportsTlsExtensions ? 0 : 1);
/*     */     } 
/* 242 */     return result;
/*     */   }
/*     */   
/*     */   public String toString() {
/* 246 */     if (!this.tls) {
/* 247 */       return "ConnectionSpec()";
/*     */     }
/*     */     
/* 250 */     String cipherSuitesString = (this.cipherSuites != null) ? cipherSuites().toString() : "[all enabled]";
/* 251 */     String tlsVersionsString = (this.tlsVersions != null) ? tlsVersions().toString() : "[all enabled]";
/* 252 */     return "ConnectionSpec(cipherSuites=" + cipherSuitesString + ", tlsVersions=" + tlsVersionsString + ", supportsTlsExtensions=" + this.supportsTlsExtensions + ")";
/*     */   }
/*     */ 
/*     */   
/*     */   public static final class Builder
/*     */   {
/*     */     boolean tls;
/*     */     @Nullable
/*     */     String[] cipherSuites;
/*     */     @Nullable
/*     */     String[] tlsVersions;
/*     */     boolean supportsTlsExtensions;
/*     */     
/*     */     Builder(boolean tls) {
/* 266 */       this.tls = tls;
/*     */     }
/*     */     
/*     */     public Builder(ConnectionSpec connectionSpec) {
/* 270 */       this.tls = connectionSpec.tls;
/* 271 */       this.cipherSuites = connectionSpec.cipherSuites;
/* 272 */       this.tlsVersions = connectionSpec.tlsVersions;
/* 273 */       this.supportsTlsExtensions = connectionSpec.supportsTlsExtensions;
/*     */     }
/*     */     
/*     */     public Builder allEnabledCipherSuites() {
/* 277 */       if (!this.tls) throw new IllegalStateException("no cipher suites for cleartext connections"); 
/* 278 */       this.cipherSuites = null;
/* 279 */       return this;
/*     */     }
/*     */     
/*     */     public Builder cipherSuites(CipherSuite... cipherSuites) {
/* 283 */       if (!this.tls) throw new IllegalStateException("no cipher suites for cleartext connections");
/*     */       
/* 285 */       String[] strings = new String[cipherSuites.length];
/* 286 */       for (int i = 0; i < cipherSuites.length; i++) {
/* 287 */         strings[i] = (cipherSuites[i]).javaName;
/*     */       }
/* 289 */       return cipherSuites(strings);
/*     */     }
/*     */     
/*     */     public Builder cipherSuites(String... cipherSuites) {
/* 293 */       if (!this.tls) throw new IllegalStateException("no cipher suites for cleartext connections");
/*     */       
/* 295 */       if (cipherSuites.length == 0) {
/* 296 */         throw new IllegalArgumentException("At least one cipher suite is required");
/*     */       }
/*     */       
/* 299 */       this.cipherSuites = (String[])cipherSuites.clone();
/* 300 */       return this;
/*     */     }
/*     */     
/*     */     public Builder allEnabledTlsVersions() {
/* 304 */       if (!this.tls) throw new IllegalStateException("no TLS versions for cleartext connections"); 
/* 305 */       this.tlsVersions = null;
/* 306 */       return this;
/*     */     }
/*     */     
/*     */     public Builder tlsVersions(TlsVersion... tlsVersions) {
/* 310 */       if (!this.tls) throw new IllegalStateException("no TLS versions for cleartext connections");
/*     */       
/* 312 */       String[] strings = new String[tlsVersions.length];
/* 313 */       for (int i = 0; i < tlsVersions.length; i++) {
/* 314 */         strings[i] = (tlsVersions[i]).javaName;
/*     */       }
/*     */       
/* 317 */       return tlsVersions(strings);
/*     */     }
/*     */     
/*     */     public Builder tlsVersions(String... tlsVersions) {
/* 321 */       if (!this.tls) throw new IllegalStateException("no TLS versions for cleartext connections");
/*     */       
/* 323 */       if (tlsVersions.length == 0) {
/* 324 */         throw new IllegalArgumentException("At least one TLS version is required");
/*     */       }
/*     */       
/* 327 */       this.tlsVersions = (String[])tlsVersions.clone();
/* 328 */       return this;
/*     */     }
/*     */     
/*     */     public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
/* 332 */       if (!this.tls) throw new IllegalStateException("no TLS extensions for cleartext connections"); 
/* 333 */       this.supportsTlsExtensions = supportsTlsExtensions;
/* 334 */       return this;
/*     */     }
/*     */     
/*     */     public ConnectionSpec build() {
/* 338 */       return new ConnectionSpec(this);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\ConnectionSpec.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */