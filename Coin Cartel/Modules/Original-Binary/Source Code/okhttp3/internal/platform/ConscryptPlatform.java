/*     */ package okhttp3.internal.platform;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import java.security.Provider;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLContext;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import javax.net.ssl.X509TrustManager;
/*     */ import okhttp3.Protocol;
/*     */ import org.conscrypt.Conscrypt;
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
/*     */ public class ConscryptPlatform
/*     */   extends Platform
/*     */ {
/*     */   private Provider getProvider() {
/*  40 */     return Conscrypt.newProviderBuilder().provideTrustManager().build();
/*     */   }
/*     */   @Nullable
/*     */   public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
/*  44 */     if (!Conscrypt.isConscrypt(sslSocketFactory)) {
/*  45 */       return super.trustManager(sslSocketFactory);
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/*  51 */       Object sp = readFieldOrNull(sslSocketFactory, Object.class, "sslParameters");
/*     */       
/*  53 */       if (sp != null) {
/*  54 */         return readFieldOrNull(sp, X509TrustManager.class, "x509TrustManager");
/*     */       }
/*     */       
/*  57 */       return null;
/*  58 */     } catch (Exception e) {
/*  59 */       throw new UnsupportedOperationException("clientBuilder.sslSocketFactory(SSLSocketFactory) not supported on Conscrypt", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) throws IOException {
/*  66 */     if (Conscrypt.isConscrypt(sslSocket)) {
/*     */       
/*  68 */       if (hostname != null) {
/*  69 */         Conscrypt.setUseSessionTickets(sslSocket, true);
/*  70 */         Conscrypt.setHostname(sslSocket, hostname);
/*     */       } 
/*     */ 
/*     */       
/*  74 */       List<String> names = Platform.alpnProtocolNames(protocols);
/*  75 */       Conscrypt.setApplicationProtocols(sslSocket, names.<String>toArray(new String[0]));
/*     */     } else {
/*  77 */       super.configureTlsExtensions(sslSocket, hostname, protocols);
/*     */     } 
/*     */   }
/*     */   @Nullable
/*     */   public String getSelectedProtocol(SSLSocket sslSocket) {
/*  82 */     if (Conscrypt.isConscrypt(sslSocket)) {
/*  83 */       return Conscrypt.getApplicationProtocol(sslSocket);
/*     */     }
/*  85 */     return super.getSelectedProtocol(sslSocket);
/*     */   }
/*     */ 
/*     */   
/*     */   public SSLContext getSSLContext() {
/*     */     try {
/*  91 */       return SSLContext.getInstance("TLSv1.3", getProvider());
/*  92 */     } catch (NoSuchAlgorithmException e) {
/*     */       
/*     */       try {
/*  95 */         return SSLContext.getInstance("TLS", getProvider());
/*  96 */       } catch (NoSuchAlgorithmException e2) {
/*  97 */         throw new IllegalStateException("No TLS provider", e);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static ConscryptPlatform buildIfSupported() {
/*     */     try {
/* 105 */       Class.forName("org.conscrypt.Conscrypt");
/*     */       
/* 107 */       if (!Conscrypt.isAvailable()) {
/* 108 */         return null;
/*     */       }
/*     */       
/* 111 */       return new ConscryptPlatform();
/* 112 */     } catch (ClassNotFoundException e) {
/* 113 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void configureSslSocketFactory(SSLSocketFactory socketFactory) {
/* 119 */     if (Conscrypt.isConscrypt(socketFactory))
/* 120 */       Conscrypt.setUseEngineSocket(socketFactory, true); 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\ConscryptPlatform.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */