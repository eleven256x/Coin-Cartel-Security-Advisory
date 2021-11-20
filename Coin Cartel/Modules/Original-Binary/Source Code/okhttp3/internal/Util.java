/*     */ package okhttp3.internal;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.IDN;
/*     */ import java.net.InetAddress;
/*     */ import java.net.ServerSocket;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.nio.charset.Charset;
/*     */ import java.security.GeneralSecurityException;
/*     */ import java.security.KeyStore;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.Comparator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.TimeZone;
/*     */ import java.util.concurrent.ThreadFactory;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.regex.Pattern;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.TrustManager;
/*     */ import javax.net.ssl.TrustManagerFactory;
/*     */ import javax.net.ssl.X509TrustManager;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.RequestBody;
/*     */ import okhttp3.ResponseBody;
/*     */ import okhttp3.internal.http2.Header;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSource;
/*     */ import okio.ByteString;
/*     */ import okio.Source;
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
/*     */ public final class Util
/*     */ {
/*  59 */   public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
/*  60 */   public static final String[] EMPTY_STRING_ARRAY = new String[0];
/*     */   
/*  62 */   public static final ResponseBody EMPTY_RESPONSE = ResponseBody.create(null, EMPTY_BYTE_ARRAY);
/*  63 */   public static final RequestBody EMPTY_REQUEST = RequestBody.create(null, EMPTY_BYTE_ARRAY);
/*     */   
/*  65 */   private static final ByteString UTF_8_BOM = ByteString.decodeHex("efbbbf");
/*  66 */   private static final ByteString UTF_16_BE_BOM = ByteString.decodeHex("feff");
/*  67 */   private static final ByteString UTF_16_LE_BOM = ByteString.decodeHex("fffe");
/*  68 */   private static final ByteString UTF_32_BE_BOM = ByteString.decodeHex("0000ffff");
/*  69 */   private static final ByteString UTF_32_LE_BOM = ByteString.decodeHex("ffff0000");
/*     */   
/*  71 */   public static final Charset UTF_8 = Charset.forName("UTF-8");
/*  72 */   public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
/*  73 */   private static final Charset UTF_16_BE = Charset.forName("UTF-16BE");
/*  74 */   private static final Charset UTF_16_LE = Charset.forName("UTF-16LE");
/*  75 */   private static final Charset UTF_32_BE = Charset.forName("UTF-32BE");
/*  76 */   private static final Charset UTF_32_LE = Charset.forName("UTF-32LE");
/*     */ 
/*     */   
/*  79 */   public static final TimeZone UTC = TimeZone.getTimeZone("GMT");
/*     */   
/*  81 */   public static final Comparator<String> NATURAL_ORDER = new Comparator<String>() {
/*     */       public int compare(String a, String b) {
/*  83 */         return a.compareTo(b);
/*     */       }
/*     */     };
/*     */ 
/*     */   
/*     */   private static final Method addSuppressedExceptionMethod;
/*     */   
/*     */   static {
/*     */     try {
/*  92 */       m = Throwable.class.getDeclaredMethod("addSuppressed", new Class[] { Throwable.class });
/*  93 */     } catch (Exception e) {
/*  94 */       m = null;
/*     */     } 
/*  96 */     addSuppressedExceptionMethod = m;
/*     */   } static {
/*     */     Method m;
/*     */   } public static void addSuppressedIfPossible(Throwable e, Throwable suppressed) {
/* 100 */     if (addSuppressedExceptionMethod != null) {
/*     */       try {
/* 102 */         addSuppressedExceptionMethod.invoke(e, new Object[] { suppressed });
/* 103 */       } catch (InvocationTargetException|IllegalAccessException invocationTargetException) {}
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
/*     */ 
/*     */   
/* 118 */   private static final Pattern VERIFY_AS_IP_ADDRESS = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void checkOffsetAndCount(long arrayLength, long offset, long count) {
/* 125 */     if ((offset | count) < 0L || offset > arrayLength || arrayLength - offset < count) {
/* 126 */       throw new ArrayIndexOutOfBoundsException();
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean equal(Object a, Object b) {
/* 132 */     return (a == b || (a != null && a.equals(b)));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void closeQuietly(Closeable closeable) {
/* 140 */     if (closeable != null) {
/*     */       try {
/* 142 */         closeable.close();
/* 143 */       } catch (RuntimeException rethrown) {
/* 144 */         throw rethrown;
/* 145 */       } catch (Exception exception) {}
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void closeQuietly(Socket socket) {
/* 155 */     if (socket != null) {
/*     */       try {
/* 157 */         socket.close();
/* 158 */       } catch (AssertionError e) {
/* 159 */         if (!isAndroidGetsocknameError(e)) throw e; 
/* 160 */       } catch (RuntimeException rethrown) {
/* 161 */         throw rethrown;
/* 162 */       } catch (Exception exception) {}
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void closeQuietly(ServerSocket serverSocket) {
/* 172 */     if (serverSocket != null) {
/*     */       try {
/* 174 */         serverSocket.close();
/* 175 */       } catch (RuntimeException rethrown) {
/* 176 */         throw rethrown;
/* 177 */       } catch (Exception exception) {}
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean discard(Source source, int timeout, TimeUnit timeUnit) {
/*     */     try {
/* 189 */       return skipAll(source, timeout, timeUnit);
/* 190 */     } catch (IOException e) {
/* 191 */       return false;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean skipAll(Source source, int duration, TimeUnit timeUnit) throws IOException {
/* 200 */     long now = System.nanoTime();
/*     */ 
/*     */     
/* 203 */     long originalDuration = source.timeout().hasDeadline() ? (source.timeout().deadlineNanoTime() - now) : Long.MAX_VALUE;
/* 204 */     source.timeout().deadlineNanoTime(now + Math.min(originalDuration, timeUnit.toNanos(duration)));
/*     */     try {
/* 206 */       Buffer skipBuffer = new Buffer();
/* 207 */       while (source.read(skipBuffer, 8192L) != -1L) {
/* 208 */         skipBuffer.clear();
/*     */       }
/* 210 */       return true;
/* 211 */     } catch (InterruptedIOException e) {
/* 212 */       return false;
/*     */     } finally {
/* 214 */       if (originalDuration == Long.MAX_VALUE) {
/* 215 */         source.timeout().clearDeadline();
/*     */       } else {
/* 217 */         source.timeout().deadlineNanoTime(now + originalDuration);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static <T> List<T> immutableList(List<T> list) {
/* 224 */     return Collections.unmodifiableList(new ArrayList<>(list));
/*     */   }
/*     */ 
/*     */   
/*     */   public static <K, V> Map<K, V> immutableMap(Map<K, V> map) {
/* 229 */     return map.isEmpty() ? 
/* 230 */       Collections.<K, V>emptyMap() : 
/* 231 */       Collections.<K, V>unmodifiableMap(new LinkedHashMap<>(map));
/*     */   }
/*     */ 
/*     */   
/*     */   public static <T> List<T> immutableList(T... elements) {
/* 236 */     return Collections.unmodifiableList(Arrays.asList((T[])elements.clone()));
/*     */   }
/*     */   
/*     */   public static ThreadFactory threadFactory(final String name, final boolean daemon) {
/* 240 */     return new ThreadFactory() {
/*     */         public Thread newThread(Runnable runnable) {
/* 242 */           Thread result = new Thread(runnable, name);
/* 243 */           result.setDaemon(daemon);
/* 244 */           return result;
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String[] intersect(Comparator<? super String> comparator, String[] first, String[] second) {
/* 256 */     List<String> result = new ArrayList<>();
/* 257 */     for (String a : first) {
/* 258 */       for (String b : second) {
/* 259 */         if (comparator.compare(a, b) == 0) {
/* 260 */           result.add(a);
/*     */           break;
/*     */         } 
/*     */       } 
/*     */     } 
/* 265 */     return result.<String>toArray(new String[result.size()]);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean nonEmptyIntersection(Comparator<String> comparator, String[] first, String[] second) {
/* 276 */     if (first == null || second == null || first.length == 0 || second.length == 0) {
/* 277 */       return false;
/*     */     }
/* 279 */     for (String a : first) {
/* 280 */       for (String b : second) {
/* 281 */         if (comparator.compare(a, b) == 0) {
/* 282 */           return true;
/*     */         }
/*     */       } 
/*     */     } 
/* 286 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static String hostHeader(HttpUrl url, boolean includeDefaultPort) {
/* 292 */     String host = url.host().contains(":") ? ("[" + url.host() + "]") : url.host();
/* 293 */     return (includeDefaultPort || url.port() != HttpUrl.defaultPort(url.scheme())) ? (
/* 294 */       host + ":" + url.port()) : 
/* 295 */       host;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isAndroidGetsocknameError(AssertionError e) {
/* 303 */     return (e.getCause() != null && e.getMessage() != null && e
/* 304 */       .getMessage().contains("getsockname failed"));
/*     */   }
/*     */   
/*     */   public static int indexOf(Comparator<String> comparator, String[] array, String value) {
/* 308 */     for (int i = 0, size = array.length; i < size; i++) {
/* 309 */       if (comparator.compare(array[i], value) == 0) return i; 
/*     */     } 
/* 311 */     return -1;
/*     */   }
/*     */   
/*     */   public static String[] concat(String[] array, String value) {
/* 315 */     String[] result = new String[array.length + 1];
/* 316 */     System.arraycopy(array, 0, result, 0, array.length);
/* 317 */     result[result.length - 1] = value;
/* 318 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int skipLeadingAsciiWhitespace(String input, int pos, int limit) {
/* 326 */     for (int i = pos; i < limit; i++) {
/* 327 */       switch (input.charAt(i)) {
/*     */         case '\t':
/*     */         case '\n':
/*     */         case '\f':
/*     */         case '\r':
/*     */         case ' ':
/*     */           break;
/*     */         default:
/* 335 */           return i;
/*     */       } 
/*     */     } 
/* 338 */     return limit;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int skipTrailingAsciiWhitespace(String input, int pos, int limit) {
/* 346 */     for (int i = limit - 1; i >= pos; i--) {
/* 347 */       switch (input.charAt(i)) {
/*     */         case '\t':
/*     */         case '\n':
/*     */         case '\f':
/*     */         case '\r':
/*     */         case ' ':
/*     */           break;
/*     */         default:
/* 355 */           return i + 1;
/*     */       } 
/*     */     } 
/* 358 */     return pos;
/*     */   }
/*     */ 
/*     */   
/*     */   public static String trimSubstring(String string, int pos, int limit) {
/* 363 */     int start = skipLeadingAsciiWhitespace(string, pos, limit);
/* 364 */     int end = skipTrailingAsciiWhitespace(string, start, limit);
/* 365 */     return string.substring(start, end);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int delimiterOffset(String input, int pos, int limit, String delimiters) {
/* 373 */     for (int i = pos; i < limit; i++) {
/* 374 */       if (delimiters.indexOf(input.charAt(i)) != -1) return i; 
/*     */     } 
/* 376 */     return limit;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int delimiterOffset(String input, int pos, int limit, char delimiter) {
/* 384 */     for (int i = pos; i < limit; i++) {
/* 385 */       if (input.charAt(i) == delimiter) return i; 
/*     */     } 
/* 387 */     return limit;
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
/*     */   public static String canonicalizeHost(String host) {
/* 400 */     if (host.contains(":")) {
/*     */ 
/*     */ 
/*     */       
/* 404 */       InetAddress inetAddress = (host.startsWith("[") && host.endsWith("]")) ? decodeIpv6(host, 1, host.length() - 1) : decodeIpv6(host, 0, host.length());
/* 405 */       if (inetAddress == null) return null; 
/* 406 */       byte[] address = inetAddress.getAddress();
/* 407 */       if (address.length == 16) return inet6AddressToAscii(address); 
/* 408 */       throw new AssertionError("Invalid IPv6 address: '" + host + "'");
/*     */     } 
/*     */     
/*     */     try {
/* 412 */       String result = IDN.toASCII(host).toLowerCase(Locale.US);
/* 413 */       if (result.isEmpty()) return null;
/*     */ 
/*     */       
/* 416 */       if (containsInvalidHostnameAsciiCodes(result)) {
/* 417 */         return null;
/*     */       }
/*     */       
/* 420 */       return result;
/* 421 */     } catch (IllegalArgumentException e) {
/* 422 */       return null;
/*     */     } 
/*     */   }
/*     */   
/*     */   private static boolean containsInvalidHostnameAsciiCodes(String hostnameAscii) {
/* 427 */     for (int i = 0; i < hostnameAscii.length(); i++) {
/* 428 */       char c = hostnameAscii.charAt(i);
/*     */ 
/*     */ 
/*     */       
/* 432 */       if (c <= '\037' || c >= '') {
/* 433 */         return true;
/*     */       }
/*     */ 
/*     */ 
/*     */       
/* 438 */       if (" #%/:?@[\\]".indexOf(c) != -1) {
/* 439 */         return true;
/*     */       }
/*     */     } 
/* 442 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int indexOfControlOrNonAscii(String input) {
/* 451 */     for (int i = 0, length = input.length(); i < length; i++) {
/* 452 */       char c = input.charAt(i);
/* 453 */       if (c <= '\037' || c >= '') {
/* 454 */         return i;
/*     */       }
/*     */     } 
/* 457 */     return -1;
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean verifyAsIpAddress(String host) {
/* 462 */     return VERIFY_AS_IP_ADDRESS.matcher(host).matches();
/*     */   }
/*     */ 
/*     */   
/*     */   public static String format(String format, Object... args) {
/* 467 */     return String.format(Locale.US, format, args);
/*     */   }
/*     */   
/*     */   public static Charset bomAwareCharset(BufferedSource source, Charset charset) throws IOException {
/* 471 */     if (source.rangeEquals(0L, UTF_8_BOM)) {
/* 472 */       source.skip(UTF_8_BOM.size());
/* 473 */       return UTF_8;
/*     */     } 
/* 475 */     if (source.rangeEquals(0L, UTF_16_BE_BOM)) {
/* 476 */       source.skip(UTF_16_BE_BOM.size());
/* 477 */       return UTF_16_BE;
/*     */     } 
/* 479 */     if (source.rangeEquals(0L, UTF_16_LE_BOM)) {
/* 480 */       source.skip(UTF_16_LE_BOM.size());
/* 481 */       return UTF_16_LE;
/*     */     } 
/* 483 */     if (source.rangeEquals(0L, UTF_32_BE_BOM)) {
/* 484 */       source.skip(UTF_32_BE_BOM.size());
/* 485 */       return UTF_32_BE;
/*     */     } 
/* 487 */     if (source.rangeEquals(0L, UTF_32_LE_BOM)) {
/* 488 */       source.skip(UTF_32_LE_BOM.size());
/* 489 */       return UTF_32_LE;
/*     */     } 
/* 491 */     return charset;
/*     */   }
/*     */   
/*     */   public static int checkDuration(String name, long duration, TimeUnit unit) {
/* 495 */     if (duration < 0L) throw new IllegalArgumentException(name + " < 0"); 
/* 496 */     if (unit == null) throw new NullPointerException("unit == null"); 
/* 497 */     long millis = unit.toMillis(duration);
/* 498 */     if (millis > 2147483647L) throw new IllegalArgumentException(name + " too large."); 
/* 499 */     if (millis == 0L && duration > 0L) throw new IllegalArgumentException(name + " too small."); 
/* 500 */     return (int)millis;
/*     */   }
/*     */   
/*     */   public static AssertionError assertionError(String message, Exception e) {
/* 504 */     AssertionError assertionError = new AssertionError(message);
/*     */     try {
/* 506 */       assertionError.initCause(e);
/* 507 */     } catch (IllegalStateException illegalStateException) {}
/*     */ 
/*     */     
/* 510 */     return assertionError;
/*     */   }
/*     */   
/*     */   public static int decodeHexDigit(char c) {
/* 514 */     if (c >= '0' && c <= '9') return c - 48; 
/* 515 */     if (c >= 'a' && c <= 'f') return c - 97 + 10; 
/* 516 */     if (c >= 'A' && c <= 'F') return c - 65 + 10; 
/* 517 */     return -1;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   private static InetAddress decodeIpv6(String input, int pos, int limit) {
/* 522 */     byte[] address = new byte[16];
/* 523 */     int b = 0;
/* 524 */     int compress = -1;
/* 525 */     int groupOffset = -1;
/*     */     
/* 527 */     for (int i = pos; i < limit; ) {
/* 528 */       if (b == address.length) return null;
/*     */ 
/*     */       
/* 531 */       if (i + 2 <= limit && input.regionMatches(i, "::", 0, 2))
/*     */       
/* 533 */       { if (compress != -1) return null; 
/* 534 */         i += 2;
/* 535 */         b += 2;
/* 536 */         compress = b;
/* 537 */         if (i == limit)
/* 538 */           break;  } else if (b != 0)
/*     */       
/* 540 */       { if (input.regionMatches(i, ":", 0, 1))
/* 541 */         { i++; }
/* 542 */         else { if (input.regionMatches(i, ".", 0, 1)) {
/*     */             
/* 544 */             if (!decodeIpv4Suffix(input, groupOffset, limit, address, b - 2)) return null; 
/* 545 */             b += 2;
/*     */             break;
/*     */           } 
/* 548 */           return null; }
/*     */          }
/*     */ 
/*     */ 
/*     */       
/* 553 */       int value = 0;
/* 554 */       groupOffset = i;
/* 555 */       for (; i < limit; i++) {
/* 556 */         char c = input.charAt(i);
/* 557 */         int hexDigit = decodeHexDigit(c);
/* 558 */         if (hexDigit == -1)
/* 559 */           break;  value = (value << 4) + hexDigit;
/*     */       } 
/* 561 */       int groupLength = i - groupOffset;
/* 562 */       if (groupLength == 0 || groupLength > 4) return null;
/*     */ 
/*     */       
/* 565 */       address[b++] = (byte)(value >>> 8 & 0xFF);
/* 566 */       address[b++] = (byte)(value & 0xFF);
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
/* 578 */     if (b != address.length) {
/* 579 */       if (compress == -1) return null; 
/* 580 */       System.arraycopy(address, compress, address, address.length - b - compress, b - compress);
/* 581 */       Arrays.fill(address, compress, compress + address.length - b, (byte)0);
/*     */     } 
/*     */     
/*     */     try {
/* 585 */       return InetAddress.getByAddress(address);
/* 586 */     } catch (UnknownHostException e) {
/* 587 */       throw new AssertionError();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static boolean decodeIpv4Suffix(String input, int pos, int limit, byte[] address, int addressOffset) {
/* 594 */     int b = addressOffset;
/*     */     
/* 596 */     for (int i = pos; i < limit; ) {
/* 597 */       if (b == address.length) return false;
/*     */ 
/*     */       
/* 600 */       if (b != addressOffset) {
/* 601 */         if (input.charAt(i) != '.') return false; 
/* 602 */         i++;
/*     */       } 
/*     */ 
/*     */       
/* 606 */       int value = 0;
/* 607 */       int groupOffset = i;
/* 608 */       for (; i < limit; i++) {
/* 609 */         char c = input.charAt(i);
/* 610 */         if (c < '0' || c > '9')
/* 611 */           break;  if (value == 0 && groupOffset != i) return false; 
/* 612 */         value = value * 10 + c - 48;
/* 613 */         if (value > 255) return false; 
/*     */       } 
/* 615 */       int groupLength = i - groupOffset;
/* 616 */       if (groupLength == 0) return false;
/*     */ 
/*     */       
/* 619 */       address[b++] = (byte)value;
/*     */     } 
/*     */     
/* 622 */     if (b != addressOffset + 4) return false; 
/* 623 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static String inet6AddressToAscii(byte[] address) {
/* 631 */     int longestRunOffset = -1;
/* 632 */     int longestRunLength = 0;
/* 633 */     for (int i = 0; i < address.length; i += 2) {
/* 634 */       int currentRunOffset = i;
/* 635 */       while (i < 16 && address[i] == 0 && address[i + 1] == 0) {
/* 636 */         i += 2;
/*     */       }
/* 638 */       int currentRunLength = i - currentRunOffset;
/* 639 */       if (currentRunLength > longestRunLength && currentRunLength >= 4) {
/* 640 */         longestRunOffset = currentRunOffset;
/* 641 */         longestRunLength = currentRunLength;
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 646 */     Buffer result = new Buffer();
/* 647 */     for (int j = 0; j < address.length; ) {
/* 648 */       if (j == longestRunOffset) {
/* 649 */         result.writeByte(58);
/* 650 */         j += longestRunLength;
/* 651 */         if (j == 16) result.writeByte(58);  continue;
/*     */       } 
/* 653 */       if (j > 0) result.writeByte(58); 
/* 654 */       int group = (address[j] & 0xFF) << 8 | address[j + 1] & 0xFF;
/* 655 */       result.writeHexadecimalUnsignedLong(group);
/* 656 */       j += 2;
/*     */     } 
/*     */     
/* 659 */     return result.readUtf8();
/*     */   }
/*     */   
/*     */   public static X509TrustManager platformTrustManager() {
/*     */     try {
/* 664 */       TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
/* 665 */           TrustManagerFactory.getDefaultAlgorithm());
/* 666 */       trustManagerFactory.init((KeyStore)null);
/* 667 */       TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
/* 668 */       if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
/* 669 */         throw new IllegalStateException("Unexpected default trust managers:" + 
/* 670 */             Arrays.toString(trustManagers));
/*     */       }
/* 672 */       return (X509TrustManager)trustManagers[0];
/* 673 */     } catch (GeneralSecurityException e) {
/* 674 */       throw assertionError("No System TLS", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static Headers toHeaders(List<Header> headerBlock) {
/* 679 */     Headers.Builder builder = new Headers.Builder();
/* 680 */     for (Header header : headerBlock) {
/* 681 */       Internal.instance.addLenient(builder, header.name.utf8(), header.value.utf8());
/*     */     }
/* 683 */     return builder.build();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\Util.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */