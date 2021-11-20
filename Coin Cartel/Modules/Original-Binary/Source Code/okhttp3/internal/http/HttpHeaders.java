/*     */ package okhttp3.internal.http;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TreeSet;
/*     */ import okhttp3.Challenge;
/*     */ import okhttp3.Cookie;
/*     */ import okhttp3.CookieJar;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
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
/*     */ public final class HttpHeaders
/*     */ {
/*  44 */   private static final ByteString QUOTED_STRING_DELIMITERS = ByteString.encodeUtf8("\"\\");
/*  45 */   private static final ByteString TOKEN_DELIMITERS = ByteString.encodeUtf8("\t ,=");
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static long contentLength(Response response) {
/*  51 */     return contentLength(response.headers());
/*     */   }
/*     */   
/*     */   public static long contentLength(Headers headers) {
/*  55 */     return stringToLong(headers.get("Content-Length"));
/*     */   }
/*     */   
/*     */   private static long stringToLong(String s) {
/*  59 */     if (s == null) return -1L; 
/*     */     try {
/*  61 */       return Long.parseLong(s);
/*  62 */     } catch (NumberFormatException e) {
/*  63 */       return -1L;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean varyMatches(Response cachedResponse, Headers cachedRequest, Request newRequest) {
/*  73 */     for (String field : varyFields(cachedResponse)) {
/*  74 */       if (!Util.equal(cachedRequest.values(field), newRequest.headers(field))) return false; 
/*     */     } 
/*  76 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean hasVaryAll(Response response) {
/*  83 */     return hasVaryAll(response.headers());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean hasVaryAll(Headers responseHeaders) {
/*  90 */     return varyFields(responseHeaders).contains("*");
/*     */   }
/*     */   
/*     */   private static Set<String> varyFields(Response response) {
/*  94 */     return varyFields(response.headers());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Set<String> varyFields(Headers responseHeaders) {
/* 101 */     Set<String> result = Collections.emptySet();
/* 102 */     for (int i = 0, size = responseHeaders.size(); i < size; i++) {
/* 103 */       if ("Vary".equalsIgnoreCase(responseHeaders.name(i))) {
/*     */         
/* 105 */         String value = responseHeaders.value(i);
/* 106 */         if (result.isEmpty()) {
/* 107 */           result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
/*     */         }
/* 109 */         for (String varyField : value.split(","))
/* 110 */           result.add(varyField.trim()); 
/*     */       } 
/*     */     } 
/* 113 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Headers varyHeaders(Response response) {
/* 124 */     Headers requestHeaders = response.networkResponse().request().headers();
/* 125 */     Headers responseHeaders = response.headers();
/* 126 */     return varyHeaders(requestHeaders, responseHeaders);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
/* 134 */     Set<String> varyFields = varyFields(responseHeaders);
/* 135 */     if (varyFields.isEmpty()) return (new Headers.Builder()).build();
/*     */     
/* 137 */     Headers.Builder result = new Headers.Builder();
/* 138 */     for (int i = 0, size = requestHeaders.size(); i < size; i++) {
/* 139 */       String fieldName = requestHeaders.name(i);
/* 140 */       if (varyFields.contains(fieldName)) {
/* 141 */         result.add(fieldName, requestHeaders.value(i));
/*     */       }
/*     */     } 
/* 144 */     return result.build();
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
/*     */   public static List<Challenge> parseChallenges(Headers responseHeaders, String headerName) {
/* 169 */     List<Challenge> result = new ArrayList<>();
/* 170 */     for (int h = 0; h < responseHeaders.size(); h++) {
/* 171 */       if (headerName.equalsIgnoreCase(responseHeaders.name(h))) {
/* 172 */         Buffer header = (new Buffer()).writeUtf8(responseHeaders.value(h));
/* 173 */         parseChallengeHeader(result, header);
/*     */       } 
/*     */     } 
/* 176 */     return result;
/*     */   }
/*     */   
/*     */   private static void parseChallengeHeader(List<Challenge> result, Buffer header) {
/* 180 */     String peek = null;
/*     */ 
/*     */     
/*     */     while (true) {
/* 184 */       if (peek == null) {
/* 185 */         skipWhitespaceAndCommas(header);
/* 186 */         peek = readToken(header);
/* 187 */         if (peek == null)
/*     */           return; 
/*     */       } 
/* 190 */       String schemeName = peek;
/*     */ 
/*     */       
/* 193 */       boolean commaPrefixed = skipWhitespaceAndCommas(header);
/* 194 */       peek = readToken(header);
/* 195 */       if (peek == null) {
/* 196 */         if (!header.exhausted())
/* 197 */           return;  result.add(new Challenge(schemeName, Collections.emptyMap()));
/*     */         
/*     */         return;
/*     */       } 
/* 201 */       int eqCount = skipAll(header, (byte)61);
/* 202 */       boolean commaSuffixed = skipWhitespaceAndCommas(header);
/*     */ 
/*     */       
/* 205 */       if (!commaPrefixed && (commaSuffixed || header.exhausted())) {
/* 206 */         result.add(new Challenge(schemeName, Collections.singletonMap((String)null, peek + 
/* 207 */                 repeat('=', eqCount))));
/* 208 */         peek = null;
/*     */         
/*     */         continue;
/*     */       } 
/*     */       
/* 213 */       Map<String, String> parameters = new LinkedHashMap<>();
/* 214 */       eqCount += skipAll(header, (byte)61);
/*     */       while (true) {
/* 216 */         if (peek == null) {
/* 217 */           peek = readToken(header);
/* 218 */           if (skipWhitespaceAndCommas(header))
/* 219 */             break;  eqCount = skipAll(header, (byte)61);
/*     */         } 
/* 221 */         if (eqCount == 0)
/* 222 */           break;  if (eqCount > 1)
/* 223 */           return;  if (skipWhitespaceAndCommas(header)) {
/*     */           return;
/*     */         }
/*     */         
/* 227 */         String parameterValue = (!header.exhausted() && header.getByte(0L) == 34) ? readQuotedString(header) : readToken(header);
/* 228 */         if (parameterValue == null)
/* 229 */           return;  String replaced = parameters.put(peek, parameterValue);
/* 230 */         peek = null;
/* 231 */         if (replaced != null)
/* 232 */           return;  if (!skipWhitespaceAndCommas(header) && !header.exhausted())
/*     */           return; 
/* 234 */       }  result.add(new Challenge(schemeName, parameters));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static boolean skipWhitespaceAndCommas(Buffer buffer) {
/* 240 */     boolean commaFound = false;
/* 241 */     while (!buffer.exhausted()) {
/* 242 */       byte b = buffer.getByte(0L);
/* 243 */       if (b == 44) {
/* 244 */         buffer.readByte();
/* 245 */         commaFound = true; continue;
/* 246 */       }  if (b == 32 || b == 9) {
/* 247 */         buffer.readByte();
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 252 */     return commaFound;
/*     */   }
/*     */   
/*     */   private static int skipAll(Buffer buffer, byte b) {
/* 256 */     int count = 0;
/* 257 */     while (!buffer.exhausted() && buffer.getByte(0L) == b) {
/* 258 */       count++;
/* 259 */       buffer.readByte();
/*     */     } 
/* 261 */     return count;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static String readQuotedString(Buffer buffer) {
/* 270 */     if (buffer.readByte() != 34) throw new IllegalArgumentException(); 
/* 271 */     Buffer result = new Buffer();
/*     */     while (true) {
/* 273 */       long i = buffer.indexOfElement(QUOTED_STRING_DELIMITERS);
/* 274 */       if (i == -1L) return null;
/*     */       
/* 276 */       if (buffer.getByte(i) == 34) {
/* 277 */         result.write(buffer, i);
/* 278 */         buffer.readByte();
/* 279 */         return result.readUtf8();
/*     */       } 
/*     */       
/* 282 */       if (buffer.size() == i + 1L) return null; 
/* 283 */       result.write(buffer, i);
/* 284 */       buffer.readByte();
/* 285 */       result.write(buffer, 1L);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static String readToken(Buffer buffer) {
/*     */     try {
/* 295 */       long tokenSize = buffer.indexOfElement(TOKEN_DELIMITERS);
/* 296 */       if (tokenSize == -1L) tokenSize = buffer.size();
/*     */       
/* 298 */       return (tokenSize != 0L) ? 
/* 299 */         buffer.readUtf8(tokenSize) : 
/* 300 */         null;
/* 301 */     } catch (EOFException e) {
/* 302 */       throw new AssertionError();
/*     */     } 
/*     */   }
/*     */   
/*     */   private static String repeat(char c, int count) {
/* 307 */     char[] array = new char[count];
/* 308 */     Arrays.fill(array, c);
/* 309 */     return new String(array);
/*     */   }
/*     */   
/*     */   public static void receiveHeaders(CookieJar cookieJar, HttpUrl url, Headers headers) {
/* 313 */     if (cookieJar == CookieJar.NO_COOKIES)
/*     */       return; 
/* 315 */     List<Cookie> cookies = Cookie.parseAll(url, headers);
/* 316 */     if (cookies.isEmpty())
/*     */       return; 
/* 318 */     cookieJar.saveFromResponse(url, cookies);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean hasBody(Response response) {
/* 324 */     if (response.request().method().equals("HEAD")) {
/* 325 */       return false;
/*     */     }
/*     */     
/* 328 */     int responseCode = response.code();
/* 329 */     if ((responseCode < 100 || responseCode >= 200) && responseCode != 204 && responseCode != 304)
/*     */     {
/*     */       
/* 332 */       return true;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 337 */     if (contentLength(response) != -1L || "chunked"
/* 338 */       .equalsIgnoreCase(response.header("Transfer-Encoding"))) {
/* 339 */       return true;
/*     */     }
/*     */     
/* 342 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int skipUntil(String input, int pos, String characters) {
/* 350 */     for (; pos < input.length() && 
/* 351 */       characters.indexOf(input.charAt(pos)) == -1; pos++);
/*     */ 
/*     */ 
/*     */     
/* 355 */     return pos;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int skipWhitespace(String input, int pos) {
/* 363 */     for (; pos < input.length(); pos++) {
/* 364 */       char c = input.charAt(pos);
/* 365 */       if (c != ' ' && c != '\t') {
/*     */         break;
/*     */       }
/*     */     } 
/* 369 */     return pos;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int parseSeconds(String value, int defaultValue) {
/*     */     try {
/* 378 */       long seconds = Long.parseLong(value);
/* 379 */       if (seconds > 2147483647L)
/* 380 */         return Integer.MAX_VALUE; 
/* 381 */       if (seconds < 0L) {
/* 382 */         return 0;
/*     */       }
/* 384 */       return (int)seconds;
/*     */     }
/* 386 */     catch (NumberFormatException e) {
/* 387 */       return defaultValue;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\HttpHeaders.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */