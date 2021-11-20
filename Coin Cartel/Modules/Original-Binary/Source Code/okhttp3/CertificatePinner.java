/*     */ package okhttp3;
/*     */ 
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.X509Certificate;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedHashSet;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLPeerUnverifiedException;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.tls.CertificateChainCleaner;
/*     */ import okio.ByteString;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class CertificatePinner
/*     */ {
/* 129 */   public static final CertificatePinner DEFAULT = (new Builder()).build();
/*     */   private final Set<Pin> pins;
/*     */   @Nullable
/*     */   private final CertificateChainCleaner certificateChainCleaner;
/*     */   
/*     */   CertificatePinner(Set<Pin> pins, @Nullable CertificateChainCleaner certificateChainCleaner) {
/* 135 */     this.pins = pins;
/* 136 */     this.certificateChainCleaner = certificateChainCleaner;
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/* 140 */     if (other == this) return true; 
/* 141 */     return (other instanceof CertificatePinner && 
/* 142 */       Util.equal(this.certificateChainCleaner, ((CertificatePinner)other).certificateChainCleaner) && this.pins
/* 143 */       .equals(((CertificatePinner)other).pins));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 147 */     int result = (this.certificateChainCleaner != null) ? this.certificateChainCleaner.hashCode() : 0;
/* 148 */     result = 31 * result + this.pins.hashCode();
/* 149 */     return result;
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
/*     */   public void check(String hostname, List<Certificate> peerCertificates) throws SSLPeerUnverifiedException {
/* 162 */     List<Pin> pins = findMatchingPins(hostname);
/* 163 */     if (pins.isEmpty())
/*     */       return; 
/* 165 */     if (this.certificateChainCleaner != null) {
/* 166 */       peerCertificates = this.certificateChainCleaner.clean(peerCertificates, hostname);
/*     */     }
/*     */     
/* 169 */     for (int c = 0, certsSize = peerCertificates.size(); c < certsSize; c++) {
/* 170 */       X509Certificate x509Certificate = (X509Certificate)peerCertificates.get(c);
/*     */ 
/*     */       
/* 173 */       ByteString sha1 = null;
/* 174 */       ByteString sha256 = null;
/*     */       
/* 176 */       for (int k = 0, m = pins.size(); k < m; k++) {
/* 177 */         Pin pin = pins.get(k);
/* 178 */         if (pin.hashAlgorithm.equals("sha256/"))
/* 179 */         { if (sha256 == null) sha256 = sha256(x509Certificate); 
/* 180 */           if (pin.hash.equals(sha256))
/* 181 */             return;  } else if (pin.hashAlgorithm.equals("sha1/"))
/* 182 */         { if (sha1 == null) sha1 = sha1(x509Certificate); 
/* 183 */           if (pin.hash.equals(sha1))
/*     */             return;  }
/* 185 */         else { throw new AssertionError("unsupported hashAlgorithm: " + pin.hashAlgorithm); }
/*     */       
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 193 */     StringBuilder message = (new StringBuilder()).append("Certificate pinning failure!").append("\n  Peer certificate chain:");
/* 194 */     for (int i = 0, j = peerCertificates.size(); i < j; i++) {
/* 195 */       X509Certificate x509Certificate = (X509Certificate)peerCertificates.get(i);
/* 196 */       message.append("\n    ").append(pin(x509Certificate))
/* 197 */         .append(": ").append(x509Certificate.getSubjectDN().getName());
/*     */     } 
/* 199 */     message.append("\n  Pinned certificates for ").append(hostname).append(":");
/* 200 */     for (int p = 0, pinsSize = pins.size(); p < pinsSize; p++) {
/* 201 */       Pin pin = pins.get(p);
/* 202 */       message.append("\n    ").append(pin);
/*     */     } 
/* 204 */     throw new SSLPeerUnverifiedException(message.toString());
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void check(String hostname, Certificate... peerCertificates) throws SSLPeerUnverifiedException {
/* 210 */     check(hostname, Arrays.asList(peerCertificates));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   List<Pin> findMatchingPins(String hostname) {
/* 218 */     List<Pin> result = Collections.emptyList();
/* 219 */     for (Pin pin : this.pins) {
/* 220 */       if (pin.matches(hostname)) {
/* 221 */         if (result.isEmpty()) result = new ArrayList<>(); 
/* 222 */         result.add(pin);
/*     */       } 
/*     */     } 
/* 225 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   CertificatePinner withCertificateChainCleaner(@Nullable CertificateChainCleaner certificateChainCleaner) {
/* 231 */     return Util.equal(this.certificateChainCleaner, certificateChainCleaner) ? 
/* 232 */       this : 
/* 233 */       new CertificatePinner(this.pins, certificateChainCleaner);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String pin(Certificate certificate) {
/* 243 */     if (!(certificate instanceof X509Certificate)) {
/* 244 */       throw new IllegalArgumentException("Certificate pinning requires X509 certificates");
/*     */     }
/* 246 */     return "sha256/" + sha256((X509Certificate)certificate).base64();
/*     */   }
/*     */   
/*     */   static ByteString sha1(X509Certificate x509Certificate) {
/* 250 */     return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha1();
/*     */   }
/*     */   
/*     */   static ByteString sha256(X509Certificate x509Certificate) {
/* 254 */     return ByteString.of(x509Certificate.getPublicKey().getEncoded()).sha256();
/*     */   }
/*     */ 
/*     */   
/*     */   static final class Pin
/*     */   {
/*     */     private static final String WILDCARD = "*.";
/*     */     
/*     */     final String pattern;
/*     */     
/*     */     final String canonicalHostname;
/*     */     final String hashAlgorithm;
/*     */     final ByteString hash;
/*     */     
/*     */     Pin(String pattern, String pin) {
/* 269 */       this.pattern = pattern;
/* 270 */       this
/*     */         
/* 272 */         .canonicalHostname = pattern.startsWith("*.") ? HttpUrl.get("http://" + pattern.substring("*.".length())).host() : HttpUrl.get("http://" + pattern).host();
/* 273 */       if (pin.startsWith("sha1/")) {
/* 274 */         this.hashAlgorithm = "sha1/";
/* 275 */         this.hash = ByteString.decodeBase64(pin.substring("sha1/".length()));
/* 276 */       } else if (pin.startsWith("sha256/")) {
/* 277 */         this.hashAlgorithm = "sha256/";
/* 278 */         this.hash = ByteString.decodeBase64(pin.substring("sha256/".length()));
/*     */       } else {
/* 280 */         throw new IllegalArgumentException("pins must start with 'sha256/' or 'sha1/': " + pin);
/*     */       } 
/*     */       
/* 283 */       if (this.hash == null) {
/* 284 */         throw new IllegalArgumentException("pins must be base64: " + pin);
/*     */       }
/*     */     }
/*     */     
/*     */     boolean matches(String hostname) {
/* 289 */       if (this.pattern.startsWith("*.")) {
/* 290 */         int firstDot = hostname.indexOf('.');
/* 291 */         return (hostname.length() - firstDot - 1 == this.canonicalHostname.length() && hostname
/* 292 */           .regionMatches(false, firstDot + 1, this.canonicalHostname, 0, this.canonicalHostname
/* 293 */             .length()));
/*     */       } 
/*     */       
/* 296 */       return hostname.equals(this.canonicalHostname);
/*     */     }
/*     */     
/*     */     public boolean equals(Object other) {
/* 300 */       return (other instanceof Pin && this.pattern
/* 301 */         .equals(((Pin)other).pattern) && this.hashAlgorithm
/* 302 */         .equals(((Pin)other).hashAlgorithm) && this.hash
/* 303 */         .equals(((Pin)other).hash));
/*     */     }
/*     */     
/*     */     public int hashCode() {
/* 307 */       int result = 17;
/* 308 */       result = 31 * result + this.pattern.hashCode();
/* 309 */       result = 31 * result + this.hashAlgorithm.hashCode();
/* 310 */       result = 31 * result + this.hash.hashCode();
/* 311 */       return result;
/*     */     }
/*     */     
/*     */     public String toString() {
/* 315 */       return this.hashAlgorithm + this.hash.base64();
/*     */     }
/*     */   }
/*     */   
/*     */   public static final class Builder
/*     */   {
/* 321 */     private final List<CertificatePinner.Pin> pins = new ArrayList<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder add(String pattern, String... pins) {
/* 331 */       if (pattern == null) throw new NullPointerException("pattern == null");
/*     */       
/* 333 */       for (String pin : pins) {
/* 334 */         this.pins.add(new CertificatePinner.Pin(pattern, pin));
/*     */       }
/*     */       
/* 337 */       return this;
/*     */     }
/*     */     
/*     */     public CertificatePinner build() {
/* 341 */       return new CertificatePinner(new LinkedHashSet<>(this.pins), null);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\CertificatePinner.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */