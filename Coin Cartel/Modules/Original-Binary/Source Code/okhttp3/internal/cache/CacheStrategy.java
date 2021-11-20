/*     */ package okhttp3.internal.cache;
/*     */ 
/*     */ import java.util.Date;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.CacheControl;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.http.HttpDate;
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
/*     */ public final class CacheStrategy
/*     */ {
/*     */   @Nullable
/*     */   public final Request networkRequest;
/*     */   @Nullable
/*     */   public final Response cacheResponse;
/*     */   
/*     */   CacheStrategy(Request networkRequest, Response cacheResponse) {
/*  58 */     this.networkRequest = networkRequest;
/*  59 */     this.cacheResponse = cacheResponse;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isCacheable(Response response, Request request) {
/*  66 */     switch (response.code()) {
/*     */       case 200:
/*     */       case 203:
/*     */       case 204:
/*     */       case 300:
/*     */       case 301:
/*     */       case 308:
/*     */       case 404:
/*     */       case 405:
/*     */       case 410:
/*     */       case 414:
/*     */       case 501:
/*     */         break;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       case 302:
/*     */       case 307:
/*  86 */         if (response.header("Expires") != null || response
/*  87 */           .cacheControl().maxAgeSeconds() != -1 || response
/*  88 */           .cacheControl().isPublic() || response
/*  89 */           .cacheControl().isPrivate()) {
/*     */           break;
/*     */         }
/*     */ 
/*     */ 
/*     */       
/*     */       default:
/*  96 */         return false;
/*     */     } 
/*     */ 
/*     */     
/* 100 */     return (!response.cacheControl().noStore() && !request.cacheControl().noStore());
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static class Factory
/*     */   {
/*     */     final long nowMillis;
/*     */ 
/*     */     
/*     */     final Request request;
/*     */ 
/*     */     
/*     */     final Response cacheResponse;
/*     */ 
/*     */     
/*     */     private Date servedDate;
/*     */ 
/*     */     
/*     */     private String servedDateString;
/*     */ 
/*     */     
/*     */     private Date lastModified;
/*     */ 
/*     */     
/*     */     private String lastModifiedString;
/*     */ 
/*     */     
/*     */     private Date expires;
/*     */ 
/*     */     
/*     */     private long sentRequestMillis;
/*     */ 
/*     */     
/*     */     private long receivedResponseMillis;
/*     */     
/*     */     private String etag;
/*     */     
/* 138 */     private int ageSeconds = -1;
/*     */     
/*     */     public Factory(long nowMillis, Request request, Response cacheResponse) {
/* 141 */       this.nowMillis = nowMillis;
/* 142 */       this.request = request;
/* 143 */       this.cacheResponse = cacheResponse;
/*     */       
/* 145 */       if (cacheResponse != null) {
/* 146 */         this.sentRequestMillis = cacheResponse.sentRequestAtMillis();
/* 147 */         this.receivedResponseMillis = cacheResponse.receivedResponseAtMillis();
/* 148 */         Headers headers = cacheResponse.headers();
/* 149 */         for (int i = 0, size = headers.size(); i < size; i++) {
/* 150 */           String fieldName = headers.name(i);
/* 151 */           String value = headers.value(i);
/* 152 */           if ("Date".equalsIgnoreCase(fieldName)) {
/* 153 */             this.servedDate = HttpDate.parse(value);
/* 154 */             this.servedDateString = value;
/* 155 */           } else if ("Expires".equalsIgnoreCase(fieldName)) {
/* 156 */             this.expires = HttpDate.parse(value);
/* 157 */           } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
/* 158 */             this.lastModified = HttpDate.parse(value);
/* 159 */             this.lastModifiedString = value;
/* 160 */           } else if ("ETag".equalsIgnoreCase(fieldName)) {
/* 161 */             this.etag = value;
/* 162 */           } else if ("Age".equalsIgnoreCase(fieldName)) {
/* 163 */             this.ageSeconds = HttpHeaders.parseSeconds(value, -1);
/*     */           } 
/*     */         } 
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public CacheStrategy get() {
/* 173 */       CacheStrategy candidate = getCandidate();
/*     */       
/* 175 */       if (candidate.networkRequest != null && this.request.cacheControl().onlyIfCached())
/*     */       {
/* 177 */         return new CacheStrategy(null, null);
/*     */       }
/*     */       
/* 180 */       return candidate;
/*     */     }
/*     */ 
/*     */     
/*     */     private CacheStrategy getCandidate() {
/*     */       String conditionName, conditionValue;
/* 186 */       if (this.cacheResponse == null) {
/* 187 */         return new CacheStrategy(this.request, null);
/*     */       }
/*     */ 
/*     */       
/* 191 */       if (this.request.isHttps() && this.cacheResponse.handshake() == null) {
/* 192 */         return new CacheStrategy(this.request, null);
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 198 */       if (!CacheStrategy.isCacheable(this.cacheResponse, this.request)) {
/* 199 */         return new CacheStrategy(this.request, null);
/*     */       }
/*     */       
/* 202 */       CacheControl requestCaching = this.request.cacheControl();
/* 203 */       if (requestCaching.noCache() || hasConditions(this.request)) {
/* 204 */         return new CacheStrategy(this.request, null);
/*     */       }
/*     */       
/* 207 */       CacheControl responseCaching = this.cacheResponse.cacheControl();
/*     */       
/* 209 */       long ageMillis = cacheResponseAge();
/* 210 */       long freshMillis = computeFreshnessLifetime();
/*     */       
/* 212 */       if (requestCaching.maxAgeSeconds() != -1) {
/* 213 */         freshMillis = Math.min(freshMillis, TimeUnit.SECONDS.toMillis(requestCaching.maxAgeSeconds()));
/*     */       }
/*     */       
/* 216 */       long minFreshMillis = 0L;
/* 217 */       if (requestCaching.minFreshSeconds() != -1) {
/* 218 */         minFreshMillis = TimeUnit.SECONDS.toMillis(requestCaching.minFreshSeconds());
/*     */       }
/*     */       
/* 221 */       long maxStaleMillis = 0L;
/* 222 */       if (!responseCaching.mustRevalidate() && requestCaching.maxStaleSeconds() != -1) {
/* 223 */         maxStaleMillis = TimeUnit.SECONDS.toMillis(requestCaching.maxStaleSeconds());
/*     */       }
/*     */       
/* 226 */       if (!responseCaching.noCache() && ageMillis + minFreshMillis < freshMillis + maxStaleMillis) {
/* 227 */         Response.Builder builder = this.cacheResponse.newBuilder();
/* 228 */         if (ageMillis + minFreshMillis >= freshMillis) {
/* 229 */           builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
/*     */         }
/* 231 */         long oneDayMillis = 86400000L;
/* 232 */         if (ageMillis > oneDayMillis && isFreshnessLifetimeHeuristic()) {
/* 233 */           builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
/*     */         }
/* 235 */         return new CacheStrategy(null, builder.build());
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 242 */       if (this.etag != null) {
/* 243 */         conditionName = "If-None-Match";
/* 244 */         conditionValue = this.etag;
/* 245 */       } else if (this.lastModified != null) {
/* 246 */         conditionName = "If-Modified-Since";
/* 247 */         conditionValue = this.lastModifiedString;
/* 248 */       } else if (this.servedDate != null) {
/* 249 */         conditionName = "If-Modified-Since";
/* 250 */         conditionValue = this.servedDateString;
/*     */       } else {
/* 252 */         return new CacheStrategy(this.request, null);
/*     */       } 
/*     */       
/* 255 */       Headers.Builder conditionalRequestHeaders = this.request.headers().newBuilder();
/* 256 */       Internal.instance.addLenient(conditionalRequestHeaders, conditionName, conditionValue);
/*     */ 
/*     */ 
/*     */       
/* 260 */       Request conditionalRequest = this.request.newBuilder().headers(conditionalRequestHeaders.build()).build();
/* 261 */       return new CacheStrategy(conditionalRequest, this.cacheResponse);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private long computeFreshnessLifetime() {
/* 269 */       CacheControl responseCaching = this.cacheResponse.cacheControl();
/* 270 */       if (responseCaching.maxAgeSeconds() != -1)
/* 271 */         return TimeUnit.SECONDS.toMillis(responseCaching.maxAgeSeconds()); 
/* 272 */       if (this.expires != null) {
/*     */ 
/*     */         
/* 275 */         long servedMillis = (this.servedDate != null) ? this.servedDate.getTime() : this.receivedResponseMillis;
/* 276 */         long delta = this.expires.getTime() - servedMillis;
/* 277 */         return (delta > 0L) ? delta : 0L;
/* 278 */       }  if (this.lastModified != null && this.cacheResponse
/* 279 */         .request().url().query() == null) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 286 */         long servedMillis = (this.servedDate != null) ? this.servedDate.getTime() : this.sentRequestMillis;
/* 287 */         long delta = servedMillis - this.lastModified.getTime();
/* 288 */         return (delta > 0L) ? (delta / 10L) : 0L;
/*     */       } 
/* 290 */       return 0L;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private long cacheResponseAge() {
/* 300 */       long apparentReceivedAge = (this.servedDate != null) ? Math.max(0L, this.receivedResponseMillis - this.servedDate.getTime()) : 0L;
/*     */ 
/*     */       
/* 303 */       long receivedAge = (this.ageSeconds != -1) ? Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis(this.ageSeconds)) : apparentReceivedAge;
/* 304 */       long responseDuration = this.receivedResponseMillis - this.sentRequestMillis;
/* 305 */       long residentDuration = this.nowMillis - this.receivedResponseMillis;
/* 306 */       return receivedAge + responseDuration + residentDuration;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private boolean isFreshnessLifetimeHeuristic() {
/* 314 */       return (this.cacheResponse.cacheControl().maxAgeSeconds() == -1 && this.expires == null);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private static boolean hasConditions(Request request) {
/* 323 */       return (request.header("If-Modified-Since") != null || request.header("If-None-Match") != null);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\cache\CacheStrategy.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */