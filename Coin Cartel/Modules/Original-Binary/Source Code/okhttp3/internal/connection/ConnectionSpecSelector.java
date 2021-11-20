/*     */ package okhttp3.internal.connection;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.UnknownServiceException;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import okhttp3.ConnectionSpec;
/*     */ import okhttp3.internal.Internal;
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
/*     */ public final class ConnectionSpecSelector
/*     */ {
/*     */   private final List<ConnectionSpec> connectionSpecs;
/*     */   private int nextModeIndex;
/*     */   private boolean isFallbackPossible;
/*     */   private boolean isFallback;
/*     */   
/*     */   public ConnectionSpecSelector(List<ConnectionSpec> connectionSpecs) {
/*  46 */     this.nextModeIndex = 0;
/*  47 */     this.connectionSpecs = connectionSpecs;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ConnectionSpec configureSecureSocket(SSLSocket sslSocket) throws IOException {
/*  57 */     ConnectionSpec tlsConfiguration = null;
/*  58 */     for (int i = this.nextModeIndex, size = this.connectionSpecs.size(); i < size; i++) {
/*  59 */       ConnectionSpec connectionSpec = this.connectionSpecs.get(i);
/*  60 */       if (connectionSpec.isCompatible(sslSocket)) {
/*  61 */         tlsConfiguration = connectionSpec;
/*  62 */         this.nextModeIndex = i + 1;
/*     */         
/*     */         break;
/*     */       } 
/*     */     } 
/*  67 */     if (tlsConfiguration == null)
/*     */     {
/*     */ 
/*     */       
/*  71 */       throw new UnknownServiceException("Unable to find acceptable protocols. isFallback=" + this.isFallback + ", modes=" + this.connectionSpecs + ", supported protocols=" + 
/*     */ 
/*     */           
/*  74 */           Arrays.toString(sslSocket.getEnabledProtocols()));
/*     */     }
/*     */     
/*  77 */     this.isFallbackPossible = isFallbackPossible(sslSocket);
/*     */     
/*  79 */     Internal.instance.apply(tlsConfiguration, sslSocket, this.isFallback);
/*     */     
/*  81 */     return tlsConfiguration;
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
/*     */   public boolean connectionFailed(IOException e) {
/*  93 */     this.isFallback = true;
/*     */     
/*  95 */     if (!this.isFallbackPossible) {
/*  96 */       return false;
/*     */     }
/*     */ 
/*     */     
/* 100 */     if (e instanceof java.net.ProtocolException) {
/* 101 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 107 */     if (e instanceof java.io.InterruptedIOException) {
/* 108 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 113 */     if (e instanceof javax.net.ssl.SSLHandshakeException)
/*     */     {
/*     */       
/* 116 */       if (e.getCause() instanceof java.security.cert.CertificateException) {
/* 117 */         return false;
/*     */       }
/*     */     }
/* 120 */     if (e instanceof javax.net.ssl.SSLPeerUnverifiedException)
/*     */     {
/* 122 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 127 */     return (e instanceof javax.net.ssl.SSLHandshakeException || e instanceof javax.net.ssl.SSLProtocolException || e instanceof javax.net.ssl.SSLException);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean isFallbackPossible(SSLSocket socket) {
/* 138 */     for (int i = this.nextModeIndex; i < this.connectionSpecs.size(); i++) {
/* 139 */       if (((ConnectionSpec)this.connectionSpecs.get(i)).isCompatible(socket)) {
/* 140 */         return true;
/*     */       }
/*     */     } 
/* 143 */     return false;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\connection\ConnectionSpecSelector.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */