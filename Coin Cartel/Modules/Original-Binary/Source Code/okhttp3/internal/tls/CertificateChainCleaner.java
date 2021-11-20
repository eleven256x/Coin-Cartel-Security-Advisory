/*    */ package okhttp3.internal.tls;
/*    */ 
/*    */ import java.security.cert.Certificate;
/*    */ import java.security.cert.X509Certificate;
/*    */ import java.util.List;
/*    */ import javax.net.ssl.SSLPeerUnverifiedException;
/*    */ import javax.net.ssl.X509TrustManager;
/*    */ import okhttp3.internal.platform.Platform;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class CertificateChainCleaner
/*    */ {
/*    */   public abstract List<Certificate> clean(List<Certificate> paramList, String paramString) throws SSLPeerUnverifiedException;
/*    */   
/*    */   public static CertificateChainCleaner get(X509TrustManager trustManager) {
/* 41 */     return Platform.get().buildCertificateChainCleaner(trustManager);
/*    */   }
/*    */   
/*    */   public static CertificateChainCleaner get(X509Certificate... caCerts) {
/* 45 */     return new BasicCertificateChainCleaner(new BasicTrustRootIndex(caCerts));
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\tls\CertificateChainCleaner.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */