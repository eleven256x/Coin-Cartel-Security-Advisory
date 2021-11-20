/*    */ package okhttp3.internal.tls;
/*    */ 
/*    */ import java.security.PublicKey;
/*    */ import java.security.cert.X509Certificate;
/*    */ import java.util.LinkedHashMap;
/*    */ import java.util.LinkedHashSet;
/*    */ import java.util.Map;
/*    */ import java.util.Set;
/*    */ import javax.security.auth.x500.X500Principal;
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
/*    */ public final class BasicTrustRootIndex
/*    */   implements TrustRootIndex
/*    */ {
/* 31 */   private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts = new LinkedHashMap<>(); public BasicTrustRootIndex(X509Certificate... caCerts) {
/* 32 */     for (X509Certificate caCert : caCerts) {
/* 33 */       X500Principal subject = caCert.getSubjectX500Principal();
/* 34 */       Set<X509Certificate> subjectCaCerts = this.subjectToCaCerts.get(subject);
/* 35 */       if (subjectCaCerts == null) {
/* 36 */         subjectCaCerts = new LinkedHashSet<>(1);
/* 37 */         this.subjectToCaCerts.put(subject, subjectCaCerts);
/*    */       } 
/* 39 */       subjectCaCerts.add(caCert);
/*    */     } 
/*    */   }
/*    */   
/*    */   public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
/* 44 */     X500Principal issuer = cert.getIssuerX500Principal();
/* 45 */     Set<X509Certificate> subjectCaCerts = this.subjectToCaCerts.get(issuer);
/* 46 */     if (subjectCaCerts == null) return null;
/*    */     
/* 48 */     for (X509Certificate caCert : subjectCaCerts) {
/* 49 */       PublicKey publicKey = caCert.getPublicKey();
/*    */       try {
/* 51 */         cert.verify(publicKey);
/* 52 */         return caCert;
/* 53 */       } catch (Exception exception) {}
/*    */     } 
/*    */ 
/*    */     
/* 57 */     return null;
/*    */   }
/*    */   
/*    */   public boolean equals(Object other) {
/* 61 */     if (other == this) return true; 
/* 62 */     return (other instanceof BasicTrustRootIndex && ((BasicTrustRootIndex)other).subjectToCaCerts
/* 63 */       .equals(this.subjectToCaCerts));
/*    */   }
/*    */ 
/*    */   
/*    */   public int hashCode() {
/* 68 */     return this.subjectToCaCerts.hashCode();
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\tls\BasicTrustRootIndex.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */