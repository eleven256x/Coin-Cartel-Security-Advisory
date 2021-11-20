/*     */ package okhttp3;
/*     */ 
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import javax.annotation.Nullable;
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
/*     */ public final class Challenge
/*     */ {
/*     */   private final String scheme;
/*     */   private final Map<String, String> authParams;
/*     */   
/*     */   public Challenge(String scheme, Map<String, String> authParams) {
/*  35 */     if (scheme == null) throw new NullPointerException("scheme == null"); 
/*  36 */     if (authParams == null) throw new NullPointerException("authParams == null"); 
/*  37 */     this.scheme = scheme;
/*  38 */     Map<String, String> newAuthParams = new LinkedHashMap<>();
/*  39 */     for (Map.Entry<String, String> authParam : authParams.entrySet()) {
/*  40 */       String key = (authParam.getKey() == null) ? null : ((String)authParam.getKey()).toLowerCase(Locale.US);
/*  41 */       newAuthParams.put(key, authParam.getValue());
/*     */     } 
/*  43 */     this.authParams = Collections.unmodifiableMap(newAuthParams);
/*     */   }
/*     */   
/*     */   public Challenge(String scheme, String realm) {
/*  47 */     if (scheme == null) throw new NullPointerException("scheme == null"); 
/*  48 */     if (realm == null) throw new NullPointerException("realm == null"); 
/*  49 */     this.scheme = scheme;
/*  50 */     this.authParams = Collections.singletonMap("realm", realm);
/*     */   }
/*     */ 
/*     */   
/*     */   public Challenge withCharset(Charset charset) {
/*  55 */     if (charset == null) throw new NullPointerException("charset == null"); 
/*  56 */     Map<String, String> authParams = new LinkedHashMap<>(this.authParams);
/*  57 */     authParams.put("charset", charset.name());
/*  58 */     return new Challenge(this.scheme, authParams);
/*     */   }
/*     */ 
/*     */   
/*     */   public String scheme() {
/*  63 */     return this.scheme;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Map<String, String> authParams() {
/*  71 */     return this.authParams;
/*     */   }
/*     */ 
/*     */   
/*     */   public String realm() {
/*  76 */     return this.authParams.get("realm");
/*     */   }
/*     */ 
/*     */   
/*     */   public Charset charset() {
/*  81 */     String charset = this.authParams.get("charset");
/*  82 */     if (charset != null) {
/*     */       try {
/*  84 */         return Charset.forName(charset);
/*  85 */       } catch (Exception exception) {}
/*     */     }
/*     */     
/*  88 */     return Util.ISO_8859_1;
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/*  92 */     return (other instanceof Challenge && ((Challenge)other).scheme
/*  93 */       .equals(this.scheme) && ((Challenge)other).authParams
/*  94 */       .equals(this.authParams));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/*  98 */     int result = 29;
/*  99 */     result = 31 * result + this.scheme.hashCode();
/* 100 */     result = 31 * result + this.authParams.hashCode();
/* 101 */     return result;
/*     */   }
/*     */   
/*     */   public String toString() {
/* 105 */     return this.scheme + " authParams=" + this.authParams;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Challenge.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */