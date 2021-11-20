/*     */ package okhttp3.internal.tls;
/*     */ 
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.CertificateParsingException;
/*     */ import java.security.cert.X509Certificate;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import javax.net.ssl.HostnameVerifier;
/*     */ import javax.net.ssl.SSLException;
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
/*     */ public final class OkHostnameVerifier
/*     */   implements HostnameVerifier
/*     */ {
/*  37 */   public static final OkHostnameVerifier INSTANCE = new OkHostnameVerifier();
/*     */ 
/*     */   
/*     */   private static final int ALT_DNS_NAME = 2;
/*     */ 
/*     */   
/*     */   private static final int ALT_IPA_NAME = 7;
/*     */ 
/*     */   
/*     */   public boolean verify(String host, SSLSession session) {
/*     */     try {
/*  48 */       Certificate[] certificates = session.getPeerCertificates();
/*  49 */       return verify(host, (X509Certificate)certificates[0]);
/*  50 */     } catch (SSLException e) {
/*  51 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public boolean verify(String host, X509Certificate certificate) {
/*  56 */     return Util.verifyAsIpAddress(host) ? 
/*  57 */       verifyIpAddress(host, certificate) : 
/*  58 */       verifyHostname(host, certificate);
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
/*  63 */     List<String> altNames = getSubjectAltNames(certificate, 7);
/*  64 */     for (int i = 0, size = altNames.size(); i < size; i++) {
/*  65 */       if (ipAddress.equalsIgnoreCase(altNames.get(i))) {
/*  66 */         return true;
/*     */       }
/*     */     } 
/*  69 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean verifyHostname(String hostname, X509Certificate certificate) {
/*  74 */     hostname = hostname.toLowerCase(Locale.US);
/*  75 */     List<String> altNames = getSubjectAltNames(certificate, 2);
/*  76 */     for (String altName : altNames) {
/*  77 */       if (verifyHostname(hostname, altName)) {
/*  78 */         return true;
/*     */       }
/*     */     } 
/*  81 */     return false;
/*     */   }
/*     */   
/*     */   public static List<String> allSubjectAltNames(X509Certificate certificate) {
/*  85 */     List<String> altIpaNames = getSubjectAltNames(certificate, 7);
/*  86 */     List<String> altDnsNames = getSubjectAltNames(certificate, 2);
/*  87 */     List<String> result = new ArrayList<>(altIpaNames.size() + altDnsNames.size());
/*  88 */     result.addAll(altIpaNames);
/*  89 */     result.addAll(altDnsNames);
/*  90 */     return result;
/*     */   }
/*     */   
/*     */   private static List<String> getSubjectAltNames(X509Certificate certificate, int type) {
/*  94 */     List<String> result = new ArrayList<>();
/*     */     try {
/*  96 */       Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
/*  97 */       if (subjectAltNames == null) {
/*  98 */         return Collections.emptyList();
/*     */       }
/* 100 */       for (Object subjectAltName : subjectAltNames) {
/* 101 */         List<?> entry = (List)subjectAltName;
/* 102 */         if (entry == null || entry.size() < 2) {
/*     */           continue;
/*     */         }
/* 105 */         Integer altNameType = (Integer)entry.get(0);
/* 106 */         if (altNameType == null) {
/*     */           continue;
/*     */         }
/* 109 */         if (altNameType.intValue() == type) {
/* 110 */           String altName = (String)entry.get(1);
/* 111 */           if (altName != null) {
/* 112 */             result.add(altName);
/*     */           }
/*     */         } 
/*     */       } 
/* 116 */       return result;
/* 117 */     } catch (CertificateParsingException e) {
/* 118 */       return Collections.emptyList();
/*     */     } 
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
/*     */   public boolean verifyHostname(String hostname, String pattern) {
/* 132 */     if (hostname == null || hostname.length() == 0 || hostname.startsWith(".") || hostname
/* 133 */       .endsWith(".."))
/*     */     {
/* 135 */       return false;
/*     */     }
/* 137 */     if (pattern == null || pattern.length() == 0 || pattern.startsWith(".") || pattern
/* 138 */       .endsWith(".."))
/*     */     {
/* 140 */       return false;
/*     */     }
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
/* 152 */     if (!hostname.endsWith(".")) {
/* 153 */       hostname = hostname + '.';
/*     */     }
/* 155 */     if (!pattern.endsWith(".")) {
/* 156 */       pattern = pattern + '.';
/*     */     }
/*     */ 
/*     */     
/* 160 */     pattern = pattern.toLowerCase(Locale.US);
/*     */ 
/*     */     
/* 163 */     if (!pattern.contains("*"))
/*     */     {
/* 165 */       return hostname.equals(pattern);
/*     */     }
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
/* 179 */     if (!pattern.startsWith("*.") || pattern.indexOf('*', 1) != -1)
/*     */     {
/*     */       
/* 182 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 188 */     if (hostname.length() < pattern.length())
/*     */     {
/* 190 */       return false;
/*     */     }
/*     */     
/* 193 */     if ("*.".equals(pattern))
/*     */     {
/* 195 */       return false;
/*     */     }
/*     */ 
/*     */     
/* 199 */     String suffix = pattern.substring(1);
/* 200 */     if (!hostname.endsWith(suffix))
/*     */     {
/* 202 */       return false;
/*     */     }
/*     */ 
/*     */     
/* 206 */     int suffixStartIndexInHostname = hostname.length() - suffix.length();
/* 207 */     if (suffixStartIndexInHostname > 0 && hostname
/* 208 */       .lastIndexOf('.', suffixStartIndexInHostname - 1) != -1)
/*     */     {
/* 210 */       return false;
/*     */     }
/*     */ 
/*     */     
/* 214 */     return true;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\tls\OkHostnameVerifier.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */