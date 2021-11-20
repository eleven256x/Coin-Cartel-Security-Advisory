/*     */ package okhttp3;
/*     */ 
/*     */ import java.net.URL;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.http.HttpMethod;
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
/*     */ public final class Request
/*     */ {
/*     */   final HttpUrl url;
/*     */   final String method;
/*     */   final Headers headers;
/*     */   @Nullable
/*     */   final RequestBody body;
/*     */   final Map<Class<?>, Object> tags;
/*     */   @Nullable
/*     */   private volatile CacheControl cacheControl;
/*     */   
/*     */   Request(Builder builder) {
/*  41 */     this.url = builder.url;
/*  42 */     this.method = builder.method;
/*  43 */     this.headers = builder.headers.build();
/*  44 */     this.body = builder.body;
/*  45 */     this.tags = Util.immutableMap(builder.tags);
/*     */   }
/*     */   
/*     */   public HttpUrl url() {
/*  49 */     return this.url;
/*     */   }
/*     */   
/*     */   public String method() {
/*  53 */     return this.method;
/*     */   }
/*     */   
/*     */   public Headers headers() {
/*  57 */     return this.headers;
/*     */   }
/*     */   @Nullable
/*     */   public String header(String name) {
/*  61 */     return this.headers.get(name);
/*     */   }
/*     */   
/*     */   public List<String> headers(String name) {
/*  65 */     return this.headers.values(name);
/*     */   }
/*     */   @Nullable
/*     */   public RequestBody body() {
/*  69 */     return this.body;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Object tag() {
/*  81 */     return tag(Object.class);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public <T> T tag(Class<? extends T> type) {
/*  89 */     return type.cast(this.tags.get(type));
/*     */   }
/*     */   
/*     */   public Builder newBuilder() {
/*  93 */     return new Builder(this);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public CacheControl cacheControl() {
/* 101 */     CacheControl result = this.cacheControl;
/* 102 */     return (result != null) ? result : (this.cacheControl = CacheControl.parse(this.headers));
/*     */   }
/*     */   
/*     */   public boolean isHttps() {
/* 106 */     return this.url.isHttps();
/*     */   }
/*     */   
/*     */   public String toString() {
/* 110 */     return "Request{method=" + this.method + ", url=" + this.url + ", tags=" + this.tags + '}';
/*     */   }
/*     */ 
/*     */   
/*     */   public static class Builder
/*     */   {
/*     */     @Nullable
/*     */     HttpUrl url;
/*     */     
/*     */     String method;
/*     */     
/*     */     Headers.Builder headers;
/*     */     
/*     */     @Nullable
/*     */     RequestBody body;
/*     */     
/* 126 */     Map<Class<?>, Object> tags = Collections.emptyMap();
/*     */     
/*     */     public Builder() {
/* 129 */       this.method = "GET";
/* 130 */       this.headers = new Headers.Builder();
/*     */     }
/*     */     
/*     */     Builder(Request request) {
/* 134 */       this.url = request.url;
/* 135 */       this.method = request.method;
/* 136 */       this.body = request.body;
/* 137 */       this
/*     */         
/* 139 */         .tags = request.tags.isEmpty() ? Collections.<Class<?>, Object>emptyMap() : new LinkedHashMap<>(request.tags);
/* 140 */       this.headers = request.headers.newBuilder();
/*     */     }
/*     */     
/*     */     public Builder url(HttpUrl url) {
/* 144 */       if (url == null) throw new NullPointerException("url == null"); 
/* 145 */       this.url = url;
/* 146 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder url(String url) {
/* 156 */       if (url == null) throw new NullPointerException("url == null");
/*     */ 
/*     */       
/* 159 */       if (url.regionMatches(true, 0, "ws:", 0, 3)) {
/* 160 */         url = "http:" + url.substring(3);
/* 161 */       } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
/* 162 */         url = "https:" + url.substring(4);
/*     */       } 
/*     */       
/* 165 */       return url(HttpUrl.get(url));
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder url(URL url) {
/* 175 */       if (url == null) throw new NullPointerException("url == null"); 
/* 176 */       return url(HttpUrl.get(url.toString()));
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder header(String name, String value) {
/* 184 */       this.headers.set(name, value);
/* 185 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder addHeader(String name, String value) {
/* 196 */       this.headers.add(name, value);
/* 197 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public Builder removeHeader(String name) {
/* 202 */       this.headers.removeAll(name);
/* 203 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public Builder headers(Headers headers) {
/* 208 */       this.headers = headers.newBuilder();
/* 209 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder cacheControl(CacheControl cacheControl) {
/* 218 */       String value = cacheControl.toString();
/* 219 */       if (value.isEmpty()) return removeHeader("Cache-Control"); 
/* 220 */       return header("Cache-Control", value);
/*     */     }
/*     */     
/*     */     public Builder get() {
/* 224 */       return method("GET", null);
/*     */     }
/*     */     
/*     */     public Builder head() {
/* 228 */       return method("HEAD", null);
/*     */     }
/*     */     
/*     */     public Builder post(RequestBody body) {
/* 232 */       return method("POST", body);
/*     */     }
/*     */     
/*     */     public Builder delete(@Nullable RequestBody body) {
/* 236 */       return method("DELETE", body);
/*     */     }
/*     */     
/*     */     public Builder delete() {
/* 240 */       return delete(Util.EMPTY_REQUEST);
/*     */     }
/*     */     
/*     */     public Builder put(RequestBody body) {
/* 244 */       return method("PUT", body);
/*     */     }
/*     */     
/*     */     public Builder patch(RequestBody body) {
/* 248 */       return method("PATCH", body);
/*     */     }
/*     */     
/*     */     public Builder method(String method, @Nullable RequestBody body) {
/* 252 */       if (method == null) throw new NullPointerException("method == null"); 
/* 253 */       if (method.length() == 0) throw new IllegalArgumentException("method.length() == 0"); 
/* 254 */       if (body != null && !HttpMethod.permitsRequestBody(method)) {
/* 255 */         throw new IllegalArgumentException("method " + method + " must not have a request body.");
/*     */       }
/* 257 */       if (body == null && HttpMethod.requiresRequestBody(method)) {
/* 258 */         throw new IllegalArgumentException("method " + method + " must have a request body.");
/*     */       }
/* 260 */       this.method = method;
/* 261 */       this.body = body;
/* 262 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public Builder tag(@Nullable Object tag) {
/* 267 */       return tag(Object.class, tag);
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
/*     */     public <T> Builder tag(Class<? super T> type, @Nullable T tag) {
/* 279 */       if (type == null) throw new NullPointerException("type == null");
/*     */       
/* 281 */       if (tag == null) {
/* 282 */         this.tags.remove(type);
/*     */       } else {
/* 284 */         if (this.tags.isEmpty()) this.tags = new LinkedHashMap<>(); 
/* 285 */         this.tags.put(type, type.cast(tag));
/*     */       } 
/*     */       
/* 288 */       return this;
/*     */     }
/*     */     
/*     */     public Request build() {
/* 292 */       if (this.url == null) throw new IllegalStateException("url == null"); 
/* 293 */       return new Request(this);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Request.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */