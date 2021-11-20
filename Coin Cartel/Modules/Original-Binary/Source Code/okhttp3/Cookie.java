/*     */ package okhttp3;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.GregorianCalendar;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.http.HttpDate;
/*     */ import okhttp3.internal.publicsuffix.PublicSuffixDatabase;
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
/*     */ public final class Cookie
/*     */ {
/*  48 */   private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{2,4})[^\\d]*");
/*     */   
/*  50 */   private static final Pattern MONTH_PATTERN = Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*");
/*     */   
/*  52 */   private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d{1,2})[^\\d]*");
/*     */   
/*  54 */   private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})[^\\d]*");
/*     */   
/*     */   private final String name;
/*     */   
/*     */   private final String value;
/*     */   
/*     */   private final long expiresAt;
/*     */   private final String domain;
/*     */   private final String path;
/*     */   private final boolean secure;
/*     */   private final boolean httpOnly;
/*     */   private final boolean persistent;
/*     */   private final boolean hostOnly;
/*     */   
/*     */   private Cookie(String name, String value, long expiresAt, String domain, String path, boolean secure, boolean httpOnly, boolean hostOnly, boolean persistent) {
/*  69 */     this.name = name;
/*  70 */     this.value = value;
/*  71 */     this.expiresAt = expiresAt;
/*  72 */     this.domain = domain;
/*  73 */     this.path = path;
/*  74 */     this.secure = secure;
/*  75 */     this.httpOnly = httpOnly;
/*  76 */     this.hostOnly = hostOnly;
/*  77 */     this.persistent = persistent;
/*     */   }
/*     */   
/*     */   Cookie(Builder builder) {
/*  81 */     if (builder.name == null) throw new NullPointerException("builder.name == null"); 
/*  82 */     if (builder.value == null) throw new NullPointerException("builder.value == null"); 
/*  83 */     if (builder.domain == null) throw new NullPointerException("builder.domain == null");
/*     */     
/*  85 */     this.name = builder.name;
/*  86 */     this.value = builder.value;
/*  87 */     this.expiresAt = builder.expiresAt;
/*  88 */     this.domain = builder.domain;
/*  89 */     this.path = builder.path;
/*  90 */     this.secure = builder.secure;
/*  91 */     this.httpOnly = builder.httpOnly;
/*  92 */     this.persistent = builder.persistent;
/*  93 */     this.hostOnly = builder.hostOnly;
/*     */   }
/*     */ 
/*     */   
/*     */   public String name() {
/*  98 */     return this.name;
/*     */   }
/*     */ 
/*     */   
/*     */   public String value() {
/* 103 */     return this.value;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean persistent() {
/* 108 */     return this.persistent;
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
/*     */   public long expiresAt() {
/* 121 */     return this.expiresAt;
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
/*     */   public boolean hostOnly() {
/* 135 */     return this.hostOnly;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String domain() {
/* 143 */     return this.domain;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String path() {
/* 152 */     return this.path;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean httpOnly() {
/* 160 */     return this.httpOnly;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean secure() {
/* 165 */     return this.secure;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean matches(HttpUrl url) {
/* 175 */     boolean domainMatch = this.hostOnly ? url.host().equals(this.domain) : domainMatch(url.host(), this.domain);
/* 176 */     if (!domainMatch) return false;
/*     */     
/* 178 */     if (!pathMatch(url, this.path)) return false;
/*     */     
/* 180 */     if (this.secure && !url.isHttps()) return false;
/*     */     
/* 182 */     return true;
/*     */   }
/*     */   
/*     */   private static boolean domainMatch(String urlHost, String domain) {
/* 186 */     if (urlHost.equals(domain)) {
/* 187 */       return true;
/*     */     }
/*     */     
/* 190 */     if (urlHost.endsWith(domain) && urlHost
/* 191 */       .charAt(urlHost.length() - domain.length() - 1) == '.' && 
/* 192 */       !Util.verifyAsIpAddress(urlHost)) {
/* 193 */       return true;
/*     */     }
/*     */     
/* 196 */     return false;
/*     */   }
/*     */   
/*     */   private static boolean pathMatch(HttpUrl url, String path) {
/* 200 */     String urlPath = url.encodedPath();
/*     */     
/* 202 */     if (urlPath.equals(path)) {
/* 203 */       return true;
/*     */     }
/*     */     
/* 206 */     if (urlPath.startsWith(path)) {
/* 207 */       if (path.endsWith("/")) return true; 
/* 208 */       if (urlPath.charAt(path.length()) == '/') return true;
/*     */     
/*     */     } 
/* 211 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public static Cookie parse(HttpUrl url, String setCookie) {
/* 219 */     return parse(System.currentTimeMillis(), url, setCookie);
/*     */   }
/*     */   @Nullable
/*     */   static Cookie parse(long currentTimeMillis, HttpUrl url, String setCookie) {
/* 223 */     int pos = 0;
/* 224 */     int limit = setCookie.length();
/* 225 */     int cookiePairEnd = Util.delimiterOffset(setCookie, pos, limit, ';');
/*     */     
/* 227 */     int pairEqualsSign = Util.delimiterOffset(setCookie, pos, cookiePairEnd, '=');
/* 228 */     if (pairEqualsSign == cookiePairEnd) return null;
/*     */     
/* 230 */     String cookieName = Util.trimSubstring(setCookie, pos, pairEqualsSign);
/* 231 */     if (cookieName.isEmpty() || Util.indexOfControlOrNonAscii(cookieName) != -1) return null;
/*     */     
/* 233 */     String cookieValue = Util.trimSubstring(setCookie, pairEqualsSign + 1, cookiePairEnd);
/* 234 */     if (Util.indexOfControlOrNonAscii(cookieValue) != -1) return null;
/*     */     
/* 236 */     long expiresAt = 253402300799999L;
/* 237 */     long deltaSeconds = -1L;
/* 238 */     String domain = null;
/* 239 */     String path = null;
/* 240 */     boolean secureOnly = false;
/* 241 */     boolean httpOnly = false;
/* 242 */     boolean hostOnly = true;
/* 243 */     boolean persistent = false;
/*     */     
/* 245 */     pos = cookiePairEnd + 1;
/* 246 */     while (pos < limit) {
/* 247 */       int attributePairEnd = Util.delimiterOffset(setCookie, pos, limit, ';');
/*     */       
/* 249 */       int attributeEqualsSign = Util.delimiterOffset(setCookie, pos, attributePairEnd, '=');
/* 250 */       String attributeName = Util.trimSubstring(setCookie, pos, attributeEqualsSign);
/*     */ 
/*     */       
/* 253 */       String attributeValue = (attributeEqualsSign < attributePairEnd) ? Util.trimSubstring(setCookie, attributeEqualsSign + 1, attributePairEnd) : "";
/*     */       
/* 255 */       if (attributeName.equalsIgnoreCase("expires")) {
/*     */         try {
/* 257 */           expiresAt = parseExpires(attributeValue, 0, attributeValue.length());
/* 258 */           persistent = true;
/* 259 */         } catch (IllegalArgumentException illegalArgumentException) {}
/*     */       
/*     */       }
/* 262 */       else if (attributeName.equalsIgnoreCase("max-age")) {
/*     */         try {
/* 264 */           deltaSeconds = parseMaxAge(attributeValue);
/* 265 */           persistent = true;
/* 266 */         } catch (NumberFormatException numberFormatException) {}
/*     */       
/*     */       }
/* 269 */       else if (attributeName.equalsIgnoreCase("domain")) {
/*     */         try {
/* 271 */           domain = parseDomain(attributeValue);
/* 272 */           hostOnly = false;
/* 273 */         } catch (IllegalArgumentException illegalArgumentException) {}
/*     */       
/*     */       }
/* 276 */       else if (attributeName.equalsIgnoreCase("path")) {
/* 277 */         path = attributeValue;
/* 278 */       } else if (attributeName.equalsIgnoreCase("secure")) {
/* 279 */         secureOnly = true;
/* 280 */       } else if (attributeName.equalsIgnoreCase("httponly")) {
/* 281 */         httpOnly = true;
/*     */       } 
/*     */       
/* 284 */       pos = attributePairEnd + 1;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 289 */     if (deltaSeconds == Long.MIN_VALUE) {
/* 290 */       expiresAt = Long.MIN_VALUE;
/* 291 */     } else if (deltaSeconds != -1L) {
/*     */ 
/*     */       
/* 294 */       long deltaMilliseconds = (deltaSeconds <= 9223372036854775L) ? (deltaSeconds * 1000L) : Long.MAX_VALUE;
/* 295 */       expiresAt = currentTimeMillis + deltaMilliseconds;
/* 296 */       if (expiresAt < currentTimeMillis || expiresAt > 253402300799999L) {
/* 297 */         expiresAt = 253402300799999L;
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 302 */     String urlHost = url.host();
/* 303 */     if (domain == null) {
/* 304 */       domain = urlHost;
/* 305 */     } else if (!domainMatch(urlHost, domain)) {
/* 306 */       return null;
/*     */     } 
/*     */ 
/*     */     
/* 310 */     if (urlHost.length() != domain.length() && 
/* 311 */       PublicSuffixDatabase.get().getEffectiveTldPlusOne(domain) == null) {
/* 312 */       return null;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 317 */     if (path == null || !path.startsWith("/")) {
/* 318 */       String encodedPath = url.encodedPath();
/* 319 */       int lastSlash = encodedPath.lastIndexOf('/');
/* 320 */       path = (lastSlash != 0) ? encodedPath.substring(0, lastSlash) : "/";
/*     */     } 
/*     */     
/* 323 */     return new Cookie(cookieName, cookieValue, expiresAt, domain, path, secureOnly, httpOnly, hostOnly, persistent);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static long parseExpires(String s, int pos, int limit) {
/* 329 */     pos = dateCharacterOffset(s, pos, limit, false);
/*     */     
/* 331 */     int hour = -1;
/* 332 */     int minute = -1;
/* 333 */     int second = -1;
/* 334 */     int dayOfMonth = -1;
/* 335 */     int month = -1;
/* 336 */     int year = -1;
/* 337 */     Matcher matcher = TIME_PATTERN.matcher(s);
/*     */     
/* 339 */     while (pos < limit) {
/* 340 */       int end = dateCharacterOffset(s, pos + 1, limit, true);
/* 341 */       matcher.region(pos, end);
/*     */       
/* 343 */       if (hour == -1 && matcher.usePattern(TIME_PATTERN).matches()) {
/* 344 */         hour = Integer.parseInt(matcher.group(1));
/* 345 */         minute = Integer.parseInt(matcher.group(2));
/* 346 */         second = Integer.parseInt(matcher.group(3));
/* 347 */       } else if (dayOfMonth == -1 && matcher.usePattern(DAY_OF_MONTH_PATTERN).matches()) {
/* 348 */         dayOfMonth = Integer.parseInt(matcher.group(1));
/* 349 */       } else if (month == -1 && matcher.usePattern(MONTH_PATTERN).matches()) {
/* 350 */         String monthString = matcher.group(1).toLowerCase(Locale.US);
/* 351 */         month = MONTH_PATTERN.pattern().indexOf(monthString) / 4;
/* 352 */       } else if (year == -1 && matcher.usePattern(YEAR_PATTERN).matches()) {
/* 353 */         year = Integer.parseInt(matcher.group(1));
/*     */       } 
/*     */       
/* 356 */       pos = dateCharacterOffset(s, end + 1, limit, false);
/*     */     } 
/*     */ 
/*     */     
/* 360 */     if (year >= 70 && year <= 99) year += 1900; 
/* 361 */     if (year >= 0 && year <= 69) year += 2000;
/*     */ 
/*     */ 
/*     */     
/* 365 */     if (year < 1601) throw new IllegalArgumentException(); 
/* 366 */     if (month == -1) throw new IllegalArgumentException(); 
/* 367 */     if (dayOfMonth < 1 || dayOfMonth > 31) throw new IllegalArgumentException(); 
/* 368 */     if (hour < 0 || hour > 23) throw new IllegalArgumentException(); 
/* 369 */     if (minute < 0 || minute > 59) throw new IllegalArgumentException(); 
/* 370 */     if (second < 0 || second > 59) throw new IllegalArgumentException();
/*     */     
/* 372 */     Calendar calendar = new GregorianCalendar(Util.UTC);
/* 373 */     calendar.setLenient(false);
/* 374 */     calendar.set(1, year);
/* 375 */     calendar.set(2, month - 1);
/* 376 */     calendar.set(5, dayOfMonth);
/* 377 */     calendar.set(11, hour);
/* 378 */     calendar.set(12, minute);
/* 379 */     calendar.set(13, second);
/* 380 */     calendar.set(14, 0);
/* 381 */     return calendar.getTimeInMillis();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static int dateCharacterOffset(String input, int pos, int limit, boolean invert) {
/* 389 */     for (int i = pos; i < limit; i++) {
/* 390 */       int c = input.charAt(i);
/* 391 */       boolean dateCharacter = ((c < 32 && c != 9) || c >= 127 || (c >= 48 && c <= 57) || (c >= 97 && c <= 122) || (c >= 65 && c <= 90) || c == 58);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 396 */       if (dateCharacter == (!invert)) return i; 
/*     */     } 
/* 398 */     return limit;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static long parseMaxAge(String s) {
/*     */     try {
/* 410 */       long parsed = Long.parseLong(s);
/* 411 */       return (parsed <= 0L) ? Long.MIN_VALUE : parsed;
/* 412 */     } catch (NumberFormatException e) {
/*     */       
/* 414 */       if (s.matches("-?\\d+")) {
/* 415 */         return s.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
/*     */       }
/* 417 */       throw e;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static String parseDomain(String s) {
/* 426 */     if (s.endsWith(".")) {
/* 427 */       throw new IllegalArgumentException();
/*     */     }
/* 429 */     if (s.startsWith(".")) {
/* 430 */       s = s.substring(1);
/*     */     }
/* 432 */     String canonicalDomain = Util.canonicalizeHost(s);
/* 433 */     if (canonicalDomain == null) {
/* 434 */       throw new IllegalArgumentException();
/*     */     }
/* 436 */     return canonicalDomain;
/*     */   }
/*     */ 
/*     */   
/*     */   public static List<Cookie> parseAll(HttpUrl url, Headers headers) {
/* 441 */     List<String> cookieStrings = headers.values("Set-Cookie");
/* 442 */     List<Cookie> cookies = null;
/*     */     
/* 444 */     for (int i = 0, size = cookieStrings.size(); i < size; i++) {
/* 445 */       Cookie cookie = parse(url, cookieStrings.get(i));
/* 446 */       if (cookie != null) {
/* 447 */         if (cookies == null) cookies = new ArrayList<>(); 
/* 448 */         cookies.add(cookie);
/*     */       } 
/*     */     } 
/* 451 */     return (cookies != null) ? 
/* 452 */       Collections.<Cookie>unmodifiableList(cookies) : 
/* 453 */       Collections.<Cookie>emptyList();
/*     */   }
/*     */ 
/*     */   
/*     */   public static final class Builder
/*     */   {
/*     */     @Nullable
/*     */     String name;
/*     */     @Nullable
/*     */     String value;
/* 463 */     long expiresAt = 253402300799999L; @Nullable
/*     */     String domain;
/* 465 */     String path = "/";
/*     */     boolean secure;
/*     */     boolean httpOnly;
/*     */     boolean persistent;
/*     */     boolean hostOnly;
/*     */     
/*     */     public Builder name(String name) {
/* 472 */       if (name == null) throw new NullPointerException("name == null"); 
/* 473 */       if (!name.trim().equals(name)) throw new IllegalArgumentException("name is not trimmed"); 
/* 474 */       this.name = name;
/* 475 */       return this;
/*     */     }
/*     */     
/*     */     public Builder value(String value) {
/* 479 */       if (value == null) throw new NullPointerException("value == null"); 
/* 480 */       if (!value.trim().equals(value)) throw new IllegalArgumentException("value is not trimmed"); 
/* 481 */       this.value = value;
/* 482 */       return this;
/*     */     }
/*     */     
/*     */     public Builder expiresAt(long expiresAt) {
/* 486 */       if (expiresAt <= 0L) expiresAt = Long.MIN_VALUE; 
/* 487 */       if (expiresAt > 253402300799999L) expiresAt = 253402300799999L; 
/* 488 */       this.expiresAt = expiresAt;
/* 489 */       this.persistent = true;
/* 490 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder domain(String domain) {
/* 498 */       return domain(domain, false);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder hostOnlyDomain(String domain) {
/* 506 */       return domain(domain, true);
/*     */     }
/*     */     
/*     */     private Builder domain(String domain, boolean hostOnly) {
/* 510 */       if (domain == null) throw new NullPointerException("domain == null"); 
/* 511 */       String canonicalDomain = Util.canonicalizeHost(domain);
/* 512 */       if (canonicalDomain == null) {
/* 513 */         throw new IllegalArgumentException("unexpected domain: " + domain);
/*     */       }
/* 515 */       this.domain = canonicalDomain;
/* 516 */       this.hostOnly = hostOnly;
/* 517 */       return this;
/*     */     }
/*     */     
/*     */     public Builder path(String path) {
/* 521 */       if (!path.startsWith("/")) throw new IllegalArgumentException("path must start with '/'"); 
/* 522 */       this.path = path;
/* 523 */       return this;
/*     */     }
/*     */     
/*     */     public Builder secure() {
/* 527 */       this.secure = true;
/* 528 */       return this;
/*     */     }
/*     */     
/*     */     public Builder httpOnly() {
/* 532 */       this.httpOnly = true;
/* 533 */       return this;
/*     */     }
/*     */     
/*     */     public Cookie build() {
/* 537 */       return new Cookie(this);
/*     */     }
/*     */   }
/*     */   
/*     */   public String toString() {
/* 542 */     return toString(false);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   String toString(boolean forObsoleteRfc2965) {
/* 551 */     StringBuilder result = new StringBuilder();
/* 552 */     result.append(this.name);
/* 553 */     result.append('=');
/* 554 */     result.append(this.value);
/*     */     
/* 556 */     if (this.persistent) {
/* 557 */       if (this.expiresAt == Long.MIN_VALUE) {
/* 558 */         result.append("; max-age=0");
/*     */       } else {
/* 560 */         result.append("; expires=").append(HttpDate.format(new Date(this.expiresAt)));
/*     */       } 
/*     */     }
/*     */     
/* 564 */     if (!this.hostOnly) {
/* 565 */       result.append("; domain=");
/* 566 */       if (forObsoleteRfc2965) {
/* 567 */         result.append(".");
/*     */       }
/* 569 */       result.append(this.domain);
/*     */     } 
/*     */     
/* 572 */     result.append("; path=").append(this.path);
/*     */     
/* 574 */     if (this.secure) {
/* 575 */       result.append("; secure");
/*     */     }
/*     */     
/* 578 */     if (this.httpOnly) {
/* 579 */       result.append("; httponly");
/*     */     }
/*     */     
/* 582 */     return result.toString();
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/* 586 */     if (!(other instanceof Cookie)) return false; 
/* 587 */     Cookie that = (Cookie)other;
/* 588 */     return (that.name.equals(this.name) && that.value
/* 589 */       .equals(this.value) && that.domain
/* 590 */       .equals(this.domain) && that.path
/* 591 */       .equals(this.path) && that.expiresAt == this.expiresAt && that.secure == this.secure && that.httpOnly == this.httpOnly && that.persistent == this.persistent && that.hostOnly == this.hostOnly);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int hashCode() {
/* 600 */     int hash = 17;
/* 601 */     hash = 31 * hash + this.name.hashCode();
/* 602 */     hash = 31 * hash + this.value.hashCode();
/* 603 */     hash = 31 * hash + this.domain.hashCode();
/* 604 */     hash = 31 * hash + this.path.hashCode();
/* 605 */     hash = 31 * hash + (int)(this.expiresAt ^ this.expiresAt >>> 32L);
/* 606 */     hash = 31 * hash + (this.secure ? 0 : 1);
/* 607 */     hash = 31 * hash + (this.httpOnly ? 0 : 1);
/* 608 */     hash = 31 * hash + (this.persistent ? 0 : 1);
/* 609 */     hash = 31 * hash + (this.hostOnly ? 0 : 1);
/* 610 */     return hash;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Cookie.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */