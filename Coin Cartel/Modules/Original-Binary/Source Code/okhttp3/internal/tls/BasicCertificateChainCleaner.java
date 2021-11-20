/*     */ package okhttp3.internal.tls;
/*     */ 
/*     */ import java.security.GeneralSecurityException;
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.X509Certificate;
/*     */ import java.util.ArrayDeque;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Deque;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import javax.net.ssl.SSLPeerUnverifiedException;
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
/*     */ public final class BasicCertificateChainCleaner
/*     */   extends CertificateChainCleaner
/*     */ {
/*     */   private static final int MAX_SIGNERS = 9;
/*     */   private final TrustRootIndex trustRootIndex;
/*     */   
/*     */   public BasicCertificateChainCleaner(TrustRootIndex trustRootIndex) {
/*  44 */     this.trustRootIndex = trustRootIndex;
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
/*     */   public List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException {
/*  56 */     Deque<Certificate> queue = new ArrayDeque<>(chain);
/*  57 */     List<Certificate> result = new ArrayList<>();
/*  58 */     result.add(queue.removeFirst());
/*  59 */     boolean foundTrustedCertificate = false;
/*     */     
/*     */     int c;
/*  62 */     label27: for (c = 0; c < 9; c++) {
/*  63 */       X509Certificate toVerify = (X509Certificate)result.get(result.size() - 1);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  68 */       X509Certificate trustedCert = this.trustRootIndex.findByIssuerAndSignature(toVerify);
/*  69 */       if (trustedCert != null) {
/*  70 */         if (result.size() > 1 || !toVerify.equals(trustedCert)) {
/*  71 */           result.add(trustedCert);
/*     */         }
/*  73 */         if (verifySignature(trustedCert, trustedCert)) {
/*  74 */           return result;
/*     */         }
/*  76 */         foundTrustedCertificate = true;
/*     */       
/*     */       }
/*     */       else {
/*     */ 
/*     */         
/*  82 */         for (Iterator<Certificate> i = queue.iterator(); i.hasNext(); ) {
/*  83 */           X509Certificate signingCert = (X509Certificate)i.next();
/*  84 */           if (verifySignature(toVerify, signingCert)) {
/*  85 */             i.remove();
/*  86 */             result.add(signingCert);
/*     */             
/*     */             continue label27;
/*     */           } 
/*     */         } 
/*     */         
/*  92 */         if (foundTrustedCertificate) {
/*  93 */           return result;
/*     */         }
/*     */ 
/*     */         
/*  97 */         throw new SSLPeerUnverifiedException("Failed to find a trusted cert that signed " + toVerify);
/*     */       } 
/*     */     } 
/*     */     
/* 101 */     throw new SSLPeerUnverifiedException("Certificate chain too long: " + result);
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean verifySignature(X509Certificate toVerify, X509Certificate signingCert) {
/* 106 */     if (!toVerify.getIssuerDN().equals(signingCert.getSubjectDN())) return false; 
/*     */     try {
/* 108 */       toVerify.verify(signingCert.getPublicKey());
/* 109 */       return true;
/* 110 */     } catch (GeneralSecurityException verifyFailed) {
/* 111 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 116 */     return this.trustRootIndex.hashCode();
/*     */   }
/*     */   
/*     */   public boolean equals(Object other) {
/* 120 */     if (other == this) return true; 
/* 121 */     return (other instanceof BasicCertificateChainCleaner && ((BasicCertificateChainCleaner)other).trustRootIndex
/* 122 */       .equals(this.trustRootIndex));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\tls\BasicCertificateChainCleaner.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */