/*     */ package okhttp3;
/*     */ 
/*     */ import java.net.Proxy;
/*     */ import java.net.ProxySelector;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.SocketFactory;
/*     */ import javax.net.ssl.HostnameVerifier;
/*     */ import javax.net.ssl.SSLSocketFactory;
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
/*     */ public final class Address
/*     */ {
/*     */   final HttpUrl url;
/*     */   final Dns dns;
/*     */   final SocketFactory socketFactory;
/*     */   final Authenticator proxyAuthenticator;
/*     */   final List<Protocol> protocols;
/*     */   final List<ConnectionSpec> connectionSpecs;
/*     */   final ProxySelector proxySelector;
/*     */   @Nullable
/*     */   final Proxy proxy;
/*     */   @Nullable
/*     */   final SSLSocketFactory sslSocketFactory;
/*     */   @Nullable
/*     */   final HostnameVerifier hostnameVerifier;
/*     */   @Nullable
/*     */   final CertificatePinner certificatePinner;
/*     */   
/*     */   public Address(String uriHost, int uriPort, Dns dns, SocketFactory socketFactory, @Nullable SSLSocketFactory sslSocketFactory, @Nullable HostnameVerifier hostnameVerifier, @Nullable CertificatePinner certificatePinner, Authenticator proxyAuthenticator, @Nullable Proxy proxy, List<Protocol> protocols, List<ConnectionSpec> connectionSpecs, ProxySelector proxySelector) {
/*  56 */     this
/*     */ 
/*     */ 
/*     */       
/*  60 */       .url = (new HttpUrl.Builder()).scheme((sslSocketFactory != null) ? "https" : "http").host(uriHost).port(uriPort).build();
/*     */     
/*  62 */     if (dns == null) throw new NullPointerException("dns == null"); 
/*  63 */     this.dns = dns;
/*     */     
/*  65 */     if (socketFactory == null) throw new NullPointerException("socketFactory == null"); 
/*  66 */     this.socketFactory = socketFactory;
/*     */     
/*  68 */     if (proxyAuthenticator == null) {
/*  69 */       throw new NullPointerException("proxyAuthenticator == null");
/*     */     }
/*  71 */     this.proxyAuthenticator = proxyAuthenticator;
/*     */     
/*  73 */     if (protocols == null) throw new NullPointerException("protocols == null"); 
/*  74 */     this.protocols = Util.immutableList(protocols);
/*     */     
/*  76 */     if (connectionSpecs == null) throw new NullPointerException("connectionSpecs == null"); 
/*  77 */     this.connectionSpecs = Util.immutableList(connectionSpecs);
/*     */     
/*  79 */     if (proxySelector == null) throw new NullPointerException("proxySelector == null"); 
/*  80 */     this.proxySelector = proxySelector;
/*     */     
/*  82 */     this.proxy = proxy;
/*  83 */     this.sslSocketFactory = sslSocketFactory;
/*  84 */     this.hostnameVerifier = hostnameVerifier;
/*  85 */     this.certificatePinner = certificatePinner;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public HttpUrl url() {
/*  93 */     return this.url;
/*     */   }
/*     */ 
/*     */   
/*     */   public Dns dns() {
/*  98 */     return this.dns;
/*     */   }
/*     */ 
/*     */   
/*     */   public SocketFactory socketFactory() {
/* 103 */     return this.socketFactory;
/*     */   }
/*     */ 
/*     */   
/*     */   public Authenticator proxyAuthenticator() {
/* 108 */     return this.proxyAuthenticator;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<Protocol> protocols() {
/* 116 */     return this.protocols;
/*     */   }
/*     */   
/*     */   public List<ConnectionSpec> connectionSpecs() {
/* 120 */     return this.connectionSpecs;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ProxySelector proxySelector() {
/* 128 */     return this.proxySelector;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Proxy proxy() {
/* 136 */     return this.proxy;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public SSLSocketFactory sslSocketFactory() {
/* 141 */     return this.sslSocketFactory;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public HostnameVerifier hostnameVerifier() {
/* 146 */     return this.hostnameVerifier;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public CertificatePinner certificatePinner() {
/* 151 */     return this.certificatePinner;
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/* 155 */     return (other instanceof Address && this.url
/* 156 */       .equals(((Address)other).url) && 
/* 157 */       equalsNonHost((Address)other));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 161 */     int result = 17;
/* 162 */     result = 31 * result + this.url.hashCode();
/* 163 */     result = 31 * result + this.dns.hashCode();
/* 164 */     result = 31 * result + this.proxyAuthenticator.hashCode();
/* 165 */     result = 31 * result + this.protocols.hashCode();
/* 166 */     result = 31 * result + this.connectionSpecs.hashCode();
/* 167 */     result = 31 * result + this.proxySelector.hashCode();
/* 168 */     result = 31 * result + ((this.proxy != null) ? this.proxy.hashCode() : 0);
/* 169 */     result = 31 * result + ((this.sslSocketFactory != null) ? this.sslSocketFactory.hashCode() : 0);
/* 170 */     result = 31 * result + ((this.hostnameVerifier != null) ? this.hostnameVerifier.hashCode() : 0);
/* 171 */     result = 31 * result + ((this.certificatePinner != null) ? this.certificatePinner.hashCode() : 0);
/* 172 */     return result;
/*     */   }
/*     */   
/*     */   boolean equalsNonHost(Address that) {
/* 176 */     return (this.dns.equals(that.dns) && this.proxyAuthenticator
/* 177 */       .equals(that.proxyAuthenticator) && this.protocols
/* 178 */       .equals(that.protocols) && this.connectionSpecs
/* 179 */       .equals(that.connectionSpecs) && this.proxySelector
/* 180 */       .equals(that.proxySelector) && 
/* 181 */       Util.equal(this.proxy, that.proxy) && 
/* 182 */       Util.equal(this.sslSocketFactory, that.sslSocketFactory) && 
/* 183 */       Util.equal(this.hostnameVerifier, that.hostnameVerifier) && 
/* 184 */       Util.equal(this.certificatePinner, that.certificatePinner) && 
/* 185 */       url().port() == that.url().port());
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 191 */     StringBuilder result = (new StringBuilder()).append("Address{").append(this.url.host()).append(":").append(this.url.port());
/*     */     
/* 193 */     if (this.proxy != null) {
/* 194 */       result.append(", proxy=").append(this.proxy);
/*     */     } else {
/* 196 */       result.append(", proxySelector=").append(this.proxySelector);
/*     */     } 
/*     */     
/* 199 */     result.append("}");
/* 200 */     return result.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Address.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */