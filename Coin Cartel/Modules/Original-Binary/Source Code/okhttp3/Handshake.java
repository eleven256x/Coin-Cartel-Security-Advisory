/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.security.Principal;
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.X509Certificate;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLPeerUnverifiedException;
/*     */ import javax.net.ssl.SSLSession;
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
/*     */ public final class Handshake
/*     */ {
/*     */   private final TlsVersion tlsVersion;
/*     */   private final CipherSuite cipherSuite;
/*     */   private final List<Certificate> peerCertificates;
/*     */   private final List<Certificate> localCertificates;
/*     */   
/*     */   private Handshake(TlsVersion tlsVersion, CipherSuite cipherSuite, List<Certificate> peerCertificates, List<Certificate> localCertificates) {
/*  44 */     this.tlsVersion = tlsVersion;
/*  45 */     this.cipherSuite = cipherSuite;
/*  46 */     this.peerCertificates = peerCertificates;
/*  47 */     this.localCertificates = localCertificates;
/*     */   }
/*     */   public static Handshake get(SSLSession session) throws IOException {
/*     */     Certificate[] peerCertificates;
/*  51 */     String cipherSuiteString = session.getCipherSuite();
/*  52 */     if (cipherSuiteString == null) throw new IllegalStateException("cipherSuite == null"); 
/*  53 */     if ("SSL_NULL_WITH_NULL_NULL".equals(cipherSuiteString)) {
/*  54 */       throw new IOException("cipherSuite == SSL_NULL_WITH_NULL_NULL");
/*     */     }
/*  56 */     CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
/*     */     
/*  58 */     String tlsVersionString = session.getProtocol();
/*  59 */     if (tlsVersionString == null) throw new IllegalStateException("tlsVersion == null"); 
/*  60 */     if ("NONE".equals(tlsVersionString)) throw new IOException("tlsVersion == NONE"); 
/*  61 */     TlsVersion tlsVersion = TlsVersion.forJavaName(tlsVersionString);
/*     */ 
/*     */     
/*     */     try {
/*  65 */       peerCertificates = session.getPeerCertificates();
/*  66 */     } catch (SSLPeerUnverifiedException ignored) {
/*  67 */       peerCertificates = null;
/*     */     } 
/*     */ 
/*     */     
/*  71 */     List<Certificate> peerCertificatesList = (peerCertificates != null) ? Util.immutableList((Object[])peerCertificates) : Collections.<Certificate>emptyList();
/*     */     
/*  73 */     Certificate[] localCertificates = session.getLocalCertificates();
/*     */ 
/*     */     
/*  76 */     List<Certificate> localCertificatesList = (localCertificates != null) ? Util.immutableList((Object[])localCertificates) : Collections.<Certificate>emptyList();
/*     */     
/*  78 */     return new Handshake(tlsVersion, cipherSuite, peerCertificatesList, localCertificatesList);
/*     */   }
/*     */ 
/*     */   
/*     */   public static Handshake get(TlsVersion tlsVersion, CipherSuite cipherSuite, List<Certificate> peerCertificates, List<Certificate> localCertificates) {
/*  83 */     if (tlsVersion == null) throw new NullPointerException("tlsVersion == null"); 
/*  84 */     if (cipherSuite == null) throw new NullPointerException("cipherSuite == null"); 
/*  85 */     return new Handshake(tlsVersion, cipherSuite, Util.immutableList(peerCertificates), 
/*  86 */         Util.immutableList(localCertificates));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public TlsVersion tlsVersion() {
/*  94 */     return this.tlsVersion;
/*     */   }
/*     */ 
/*     */   
/*     */   public CipherSuite cipherSuite() {
/*  99 */     return this.cipherSuite;
/*     */   }
/*     */ 
/*     */   
/*     */   public List<Certificate> peerCertificates() {
/* 104 */     return this.peerCertificates;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public Principal peerPrincipal() {
/* 109 */     return !this.peerCertificates.isEmpty() ? (
/* 110 */       (X509Certificate)this.peerCertificates.get(0)).getSubjectX500Principal() : 
/* 111 */       null;
/*     */   }
/*     */ 
/*     */   
/*     */   public List<Certificate> localCertificates() {
/* 116 */     return this.localCertificates;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public Principal localPrincipal() {
/* 121 */     return !this.localCertificates.isEmpty() ? (
/* 122 */       (X509Certificate)this.localCertificates.get(0)).getSubjectX500Principal() : 
/* 123 */       null;
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/* 127 */     if (!(other instanceof Handshake)) return false; 
/* 128 */     Handshake that = (Handshake)other;
/* 129 */     return (this.tlsVersion.equals(that.tlsVersion) && this.cipherSuite
/* 130 */       .equals(that.cipherSuite) && this.peerCertificates
/* 131 */       .equals(that.peerCertificates) && this.localCertificates
/* 132 */       .equals(that.localCertificates));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 136 */     int result = 17;
/* 137 */     result = 31 * result + this.tlsVersion.hashCode();
/* 138 */     result = 31 * result + this.cipherSuite.hashCode();
/* 139 */     result = 31 * result + this.peerCertificates.hashCode();
/* 140 */     result = 31 * result + this.localCertificates.hashCode();
/* 141 */     return result;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Handshake.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */