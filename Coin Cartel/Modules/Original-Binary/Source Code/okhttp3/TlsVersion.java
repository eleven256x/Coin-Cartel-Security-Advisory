/*    */ package okhttp3;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.Collections;
/*    */ import java.util.List;
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
/*    */ public enum TlsVersion
/*    */ {
/* 27 */   TLS_1_3("TLSv1.3"),
/* 28 */   TLS_1_2("TLSv1.2"),
/* 29 */   TLS_1_1("TLSv1.1"),
/* 30 */   TLS_1_0("TLSv1"),
/* 31 */   SSL_3_0("SSLv3");
/*    */   
/*    */   final String javaName;
/*    */ 
/*    */   
/*    */   TlsVersion(String javaName) {
/* 37 */     this.javaName = javaName;
/*    */   }
/*    */   
/*    */   public static TlsVersion forJavaName(String javaName) {
/* 41 */     switch (javaName) {
/*    */       case "TLSv1.3":
/* 43 */         return TLS_1_3;
/*    */       case "TLSv1.2":
/* 45 */         return TLS_1_2;
/*    */       case "TLSv1.1":
/* 47 */         return TLS_1_1;
/*    */       case "TLSv1":
/* 49 */         return TLS_1_0;
/*    */       case "SSLv3":
/* 51 */         return SSL_3_0;
/*    */     } 
/* 53 */     throw new IllegalArgumentException("Unexpected TLS version: " + javaName);
/*    */   }
/*    */   
/*    */   static List<TlsVersion> forJavaNames(String... tlsVersions) {
/* 57 */     List<TlsVersion> result = new ArrayList<>(tlsVersions.length);
/* 58 */     for (String tlsVersion : tlsVersions) {
/* 59 */       result.add(forJavaName(tlsVersion));
/*    */     }
/* 61 */     return Collections.unmodifiableList(result);
/*    */   }
/*    */   
/*    */   public String javaName() {
/* 65 */     return this.javaName;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\TlsVersion.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */