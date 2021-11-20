/*     */ package okhttp3;
/*     */ 
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.http.HttpHeaders;
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
/*     */ public final class CacheControl
/*     */ {
/*  18 */   public static final CacheControl FORCE_NETWORK = (new Builder()).noCache().build();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  25 */   public static final CacheControl FORCE_CACHE = (new Builder())
/*  26 */     .onlyIfCached()
/*  27 */     .maxStale(2147483647, TimeUnit.SECONDS)
/*  28 */     .build();
/*     */   
/*     */   private final boolean noCache;
/*     */   
/*     */   private final boolean noStore;
/*     */   
/*     */   private final int maxAgeSeconds;
/*     */   
/*     */   private final int sMaxAgeSeconds;
/*     */   private final boolean isPrivate;
/*     */   private final boolean isPublic;
/*     */   private final boolean mustRevalidate;
/*     */   private final int maxStaleSeconds;
/*     */   private final int minFreshSeconds;
/*     */   private final boolean onlyIfCached;
/*     */   private final boolean noTransform;
/*     */   private final boolean immutable;
/*     */   @Nullable
/*     */   String headerValue;
/*     */   
/*     */   private CacheControl(boolean noCache, boolean noStore, int maxAgeSeconds, int sMaxAgeSeconds, boolean isPrivate, boolean isPublic, boolean mustRevalidate, int maxStaleSeconds, int minFreshSeconds, boolean onlyIfCached, boolean noTransform, boolean immutable, @Nullable String headerValue) {
/*  49 */     this.noCache = noCache;
/*  50 */     this.noStore = noStore;
/*  51 */     this.maxAgeSeconds = maxAgeSeconds;
/*  52 */     this.sMaxAgeSeconds = sMaxAgeSeconds;
/*  53 */     this.isPrivate = isPrivate;
/*  54 */     this.isPublic = isPublic;
/*  55 */     this.mustRevalidate = mustRevalidate;
/*  56 */     this.maxStaleSeconds = maxStaleSeconds;
/*  57 */     this.minFreshSeconds = minFreshSeconds;
/*  58 */     this.onlyIfCached = onlyIfCached;
/*  59 */     this.noTransform = noTransform;
/*  60 */     this.immutable = immutable;
/*  61 */     this.headerValue = headerValue;
/*     */   }
/*     */   
/*     */   CacheControl(Builder builder) {
/*  65 */     this.noCache = builder.noCache;
/*  66 */     this.noStore = builder.noStore;
/*  67 */     this.maxAgeSeconds = builder.maxAgeSeconds;
/*  68 */     this.sMaxAgeSeconds = -1;
/*  69 */     this.isPrivate = false;
/*  70 */     this.isPublic = false;
/*  71 */     this.mustRevalidate = false;
/*  72 */     this.maxStaleSeconds = builder.maxStaleSeconds;
/*  73 */     this.minFreshSeconds = builder.minFreshSeconds;
/*  74 */     this.onlyIfCached = builder.onlyIfCached;
/*  75 */     this.noTransform = builder.noTransform;
/*  76 */     this.immutable = builder.immutable;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean noCache() {
/*  87 */     return this.noCache;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean noStore() {
/*  92 */     return this.noStore;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int maxAgeSeconds() {
/*  99 */     return this.maxAgeSeconds;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int sMaxAgeSeconds() {
/* 107 */     return this.sMaxAgeSeconds;
/*     */   }
/*     */   
/*     */   public boolean isPrivate() {
/* 111 */     return this.isPrivate;
/*     */   }
/*     */   
/*     */   public boolean isPublic() {
/* 115 */     return this.isPublic;
/*     */   }
/*     */   
/*     */   public boolean mustRevalidate() {
/* 119 */     return this.mustRevalidate;
/*     */   }
/*     */   
/*     */   public int maxStaleSeconds() {
/* 123 */     return this.maxStaleSeconds;
/*     */   }
/*     */   
/*     */   public int minFreshSeconds() {
/* 127 */     return this.minFreshSeconds;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean onlyIfCached() {
/* 137 */     return this.onlyIfCached;
/*     */   }
/*     */   
/*     */   public boolean noTransform() {
/* 141 */     return this.noTransform;
/*     */   }
/*     */   
/*     */   public boolean immutable() {
/* 145 */     return this.immutable;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static CacheControl parse(Headers headers) {
/* 153 */     boolean noCache = false;
/* 154 */     boolean noStore = false;
/* 155 */     int maxAgeSeconds = -1;
/* 156 */     int sMaxAgeSeconds = -1;
/* 157 */     boolean isPrivate = false;
/* 158 */     boolean isPublic = false;
/* 159 */     boolean mustRevalidate = false;
/* 160 */     int maxStaleSeconds = -1;
/* 161 */     int minFreshSeconds = -1;
/* 162 */     boolean onlyIfCached = false;
/* 163 */     boolean noTransform = false;
/* 164 */     boolean immutable = false;
/*     */     
/* 166 */     boolean canUseHeaderValue = true;
/* 167 */     String headerValue = null;
/*     */     
/* 169 */     for (int i = 0, size = headers.size(); i < size; i++) {
/* 170 */       String name = headers.name(i);
/* 171 */       String value = headers.value(i);
/*     */       
/* 173 */       if (name.equalsIgnoreCase("Cache-Control")) {
/* 174 */         if (headerValue != null) {
/*     */           
/* 176 */           canUseHeaderValue = false;
/*     */         } else {
/* 178 */           headerValue = value;
/*     */         } 
/* 180 */       } else if (name.equalsIgnoreCase("Pragma")) {
/*     */         
/* 182 */         canUseHeaderValue = false;
/*     */       } else {
/*     */         continue;
/*     */       } 
/*     */       
/* 187 */       int pos = 0;
/* 188 */       while (pos < value.length()) {
/* 189 */         String parameter; int tokenStart = pos;
/* 190 */         pos = HttpHeaders.skipUntil(value, pos, "=,;");
/* 191 */         String directive = value.substring(tokenStart, pos).trim();
/*     */ 
/*     */         
/* 194 */         if (pos == value.length() || value.charAt(pos) == ',' || value.charAt(pos) == ';') {
/* 195 */           pos++;
/* 196 */           parameter = null;
/*     */         } else {
/* 198 */           pos++;
/* 199 */           pos = HttpHeaders.skipWhitespace(value, pos);
/*     */ 
/*     */           
/* 202 */           if (pos < value.length() && value.charAt(pos) == '"') {
/*     */             
/* 204 */             int parameterStart = ++pos;
/* 205 */             pos = HttpHeaders.skipUntil(value, pos, "\"");
/* 206 */             parameter = value.substring(parameterStart, pos);
/* 207 */             pos++;
/*     */           }
/*     */           else {
/*     */             
/* 211 */             int parameterStart = pos;
/* 212 */             pos = HttpHeaders.skipUntil(value, pos, ",;");
/* 213 */             parameter = value.substring(parameterStart, pos).trim();
/*     */           } 
/*     */         } 
/*     */         
/* 217 */         if ("no-cache".equalsIgnoreCase(directive)) {
/* 218 */           noCache = true; continue;
/* 219 */         }  if ("no-store".equalsIgnoreCase(directive)) {
/* 220 */           noStore = true; continue;
/* 221 */         }  if ("max-age".equalsIgnoreCase(directive)) {
/* 222 */           maxAgeSeconds = HttpHeaders.parseSeconds(parameter, -1); continue;
/* 223 */         }  if ("s-maxage".equalsIgnoreCase(directive)) {
/* 224 */           sMaxAgeSeconds = HttpHeaders.parseSeconds(parameter, -1); continue;
/* 225 */         }  if ("private".equalsIgnoreCase(directive)) {
/* 226 */           isPrivate = true; continue;
/* 227 */         }  if ("public".equalsIgnoreCase(directive)) {
/* 228 */           isPublic = true; continue;
/* 229 */         }  if ("must-revalidate".equalsIgnoreCase(directive)) {
/* 230 */           mustRevalidate = true; continue;
/* 231 */         }  if ("max-stale".equalsIgnoreCase(directive)) {
/* 232 */           maxStaleSeconds = HttpHeaders.parseSeconds(parameter, 2147483647); continue;
/* 233 */         }  if ("min-fresh".equalsIgnoreCase(directive)) {
/* 234 */           minFreshSeconds = HttpHeaders.parseSeconds(parameter, -1); continue;
/* 235 */         }  if ("only-if-cached".equalsIgnoreCase(directive)) {
/* 236 */           onlyIfCached = true; continue;
/* 237 */         }  if ("no-transform".equalsIgnoreCase(directive)) {
/* 238 */           noTransform = true; continue;
/* 239 */         }  if ("immutable".equalsIgnoreCase(directive)) {
/* 240 */           immutable = true;
/*     */         }
/*     */       } 
/*     */       continue;
/*     */     } 
/* 245 */     if (!canUseHeaderValue) {
/* 246 */       headerValue = null;
/*     */     }
/* 248 */     return new CacheControl(noCache, noStore, maxAgeSeconds, sMaxAgeSeconds, isPrivate, isPublic, mustRevalidate, maxStaleSeconds, minFreshSeconds, onlyIfCached, noTransform, immutable, headerValue);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 254 */     String result = this.headerValue;
/* 255 */     return (result != null) ? result : (this.headerValue = headerValue());
/*     */   }
/*     */   
/*     */   private String headerValue() {
/* 259 */     StringBuilder result = new StringBuilder();
/* 260 */     if (this.noCache) result.append("no-cache, "); 
/* 261 */     if (this.noStore) result.append("no-store, "); 
/* 262 */     if (this.maxAgeSeconds != -1) result.append("max-age=").append(this.maxAgeSeconds).append(", "); 
/* 263 */     if (this.sMaxAgeSeconds != -1) result.append("s-maxage=").append(this.sMaxAgeSeconds).append(", "); 
/* 264 */     if (this.isPrivate) result.append("private, "); 
/* 265 */     if (this.isPublic) result.append("public, "); 
/* 266 */     if (this.mustRevalidate) result.append("must-revalidate, "); 
/* 267 */     if (this.maxStaleSeconds != -1) result.append("max-stale=").append(this.maxStaleSeconds).append(", "); 
/* 268 */     if (this.minFreshSeconds != -1) result.append("min-fresh=").append(this.minFreshSeconds).append(", "); 
/* 269 */     if (this.onlyIfCached) result.append("only-if-cached, "); 
/* 270 */     if (this.noTransform) result.append("no-transform, "); 
/* 271 */     if (this.immutable) result.append("immutable, "); 
/* 272 */     if (result.length() == 0) return ""; 
/* 273 */     result.delete(result.length() - 2, result.length());
/* 274 */     return result.toString();
/*     */   }
/*     */   
/*     */   public static final class Builder
/*     */   {
/*     */     boolean noCache;
/*     */     boolean noStore;
/* 281 */     int maxAgeSeconds = -1;
/* 282 */     int maxStaleSeconds = -1;
/* 283 */     int minFreshSeconds = -1;
/*     */     
/*     */     boolean onlyIfCached;
/*     */     boolean noTransform;
/*     */     boolean immutable;
/*     */     
/*     */     public Builder noCache() {
/* 290 */       this.noCache = true;
/* 291 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public Builder noStore() {
/* 296 */       this.noStore = true;
/* 297 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder maxAge(int maxAge, TimeUnit timeUnit) {
/* 308 */       if (maxAge < 0) throw new IllegalArgumentException("maxAge < 0: " + maxAge); 
/* 309 */       long maxAgeSecondsLong = timeUnit.toSeconds(maxAge);
/* 310 */       this
/*     */         
/* 312 */         .maxAgeSeconds = (maxAgeSecondsLong > 2147483647L) ? Integer.MAX_VALUE : (int)maxAgeSecondsLong;
/* 313 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder maxStale(int maxStale, TimeUnit timeUnit) {
/* 324 */       if (maxStale < 0) throw new IllegalArgumentException("maxStale < 0: " + maxStale); 
/* 325 */       long maxStaleSecondsLong = timeUnit.toSeconds(maxStale);
/* 326 */       this
/*     */         
/* 328 */         .maxStaleSeconds = (maxStaleSecondsLong > 2147483647L) ? Integer.MAX_VALUE : (int)maxStaleSecondsLong;
/* 329 */       return this;
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
/*     */     public Builder minFresh(int minFresh, TimeUnit timeUnit) {
/* 341 */       if (minFresh < 0) throw new IllegalArgumentException("minFresh < 0: " + minFresh); 
/* 342 */       long minFreshSecondsLong = timeUnit.toSeconds(minFresh);
/* 343 */       this
/*     */         
/* 345 */         .minFreshSeconds = (minFreshSecondsLong > 2147483647L) ? Integer.MAX_VALUE : (int)minFreshSecondsLong;
/* 346 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder onlyIfCached() {
/* 354 */       this.onlyIfCached = true;
/* 355 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public Builder noTransform() {
/* 360 */       this.noTransform = true;
/* 361 */       return this;
/*     */     }
/*     */     
/*     */     public Builder immutable() {
/* 365 */       this.immutable = true;
/* 366 */       return this;
/*     */     }
/*     */     
/*     */     public CacheControl build() {
/* 370 */       return new CacheControl(this);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\CacheControl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */