/*     */ package okio;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.io.ObjectOutputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Serializable;
/*     */ import java.lang.reflect.Field;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.charset.Charset;
/*     */ import java.security.InvalidKeyException;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import java.util.Arrays;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.crypto.Mac;
/*     */ import javax.crypto.spec.SecretKeySpec;
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
/*     */ public class ByteString
/*     */   implements Serializable, Comparable<ByteString>
/*     */ {
/*  53 */   static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
/*     */ 
/*     */   
/*     */   private static final long serialVersionUID = 1L;
/*     */   
/*  58 */   public static final ByteString EMPTY = of(new byte[0]);
/*     */   
/*     */   final byte[] data;
/*     */   transient int hashCode;
/*     */   transient String utf8;
/*     */   
/*     */   ByteString(byte[] data) {
/*  65 */     this.data = data;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ByteString of(byte... data) {
/*  72 */     if (data == null) throw new IllegalArgumentException("data == null"); 
/*  73 */     return new ByteString((byte[])data.clone());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ByteString of(byte[] data, int offset, int byteCount) {
/*  81 */     if (data == null) throw new IllegalArgumentException("data == null"); 
/*  82 */     Util.checkOffsetAndCount(data.length, offset, byteCount);
/*     */     
/*  84 */     byte[] copy = new byte[byteCount];
/*  85 */     System.arraycopy(data, offset, copy, 0, byteCount);
/*  86 */     return new ByteString(copy);
/*     */   }
/*     */   
/*     */   public static ByteString of(ByteBuffer data) {
/*  90 */     if (data == null) throw new IllegalArgumentException("data == null");
/*     */     
/*  92 */     byte[] copy = new byte[data.remaining()];
/*  93 */     data.get(copy);
/*  94 */     return new ByteString(copy);
/*     */   }
/*     */ 
/*     */   
/*     */   public static ByteString encodeUtf8(String s) {
/*  99 */     if (s == null) throw new IllegalArgumentException("s == null"); 
/* 100 */     ByteString byteString = new ByteString(s.getBytes(Util.UTF_8));
/* 101 */     byteString.utf8 = s;
/* 102 */     return byteString;
/*     */   }
/*     */ 
/*     */   
/*     */   public static ByteString encodeString(String s, Charset charset) {
/* 107 */     if (s == null) throw new IllegalArgumentException("s == null"); 
/* 108 */     if (charset == null) throw new IllegalArgumentException("charset == null"); 
/* 109 */     return new ByteString(s.getBytes(charset));
/*     */   }
/*     */ 
/*     */   
/*     */   public String utf8() {
/* 114 */     String result = this.utf8;
/*     */     
/* 116 */     return (result != null) ? result : (this.utf8 = new String(this.data, Util.UTF_8));
/*     */   }
/*     */ 
/*     */   
/*     */   public String string(Charset charset) {
/* 121 */     if (charset == null) throw new IllegalArgumentException("charset == null"); 
/* 122 */     return new String(this.data, charset);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String base64() {
/* 131 */     return Base64.encode(this.data);
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString md5() {
/* 136 */     return digest("MD5");
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString sha1() {
/* 141 */     return digest("SHA-1");
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString sha256() {
/* 146 */     return digest("SHA-256");
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString sha512() {
/* 151 */     return digest("SHA-512");
/*     */   }
/*     */   
/*     */   private ByteString digest(String algorithm) {
/*     */     try {
/* 156 */       return of(MessageDigest.getInstance(algorithm).digest(this.data));
/* 157 */     } catch (NoSuchAlgorithmException e) {
/* 158 */       throw new AssertionError(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString hmacSha1(ByteString key) {
/* 164 */     return hmac("HmacSHA1", key);
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString hmacSha256(ByteString key) {
/* 169 */     return hmac("HmacSHA256", key);
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteString hmacSha512(ByteString key) {
/* 174 */     return hmac("HmacSHA512", key);
/*     */   }
/*     */   
/*     */   private ByteString hmac(String algorithm, ByteString key) {
/*     */     try {
/* 179 */       Mac mac = Mac.getInstance(algorithm);
/* 180 */       mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
/* 181 */       return of(mac.doFinal(this.data));
/* 182 */     } catch (NoSuchAlgorithmException e) {
/* 183 */       throw new AssertionError(e);
/* 184 */     } catch (InvalidKeyException e) {
/* 185 */       throw new IllegalArgumentException(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String base64Url() {
/* 194 */     return Base64.encodeUrl(this.data);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public static ByteString decodeBase64(String base64) {
/* 202 */     if (base64 == null) throw new IllegalArgumentException("base64 == null"); 
/* 203 */     byte[] decoded = Base64.decode(base64);
/* 204 */     return (decoded != null) ? new ByteString(decoded) : null;
/*     */   }
/*     */ 
/*     */   
/*     */   public String hex() {
/* 209 */     char[] result = new char[this.data.length * 2];
/* 210 */     int c = 0;
/* 211 */     for (byte b : this.data) {
/* 212 */       result[c++] = HEX_DIGITS[b >> 4 & 0xF];
/* 213 */       result[c++] = HEX_DIGITS[b & 0xF];
/*     */     } 
/* 215 */     return new String(result);
/*     */   }
/*     */ 
/*     */   
/*     */   public static ByteString decodeHex(String hex) {
/* 220 */     if (hex == null) throw new IllegalArgumentException("hex == null"); 
/* 221 */     if (hex.length() % 2 != 0) throw new IllegalArgumentException("Unexpected hex string: " + hex);
/*     */     
/* 223 */     byte[] result = new byte[hex.length() / 2];
/* 224 */     for (int i = 0; i < result.length; i++) {
/* 225 */       int d1 = decodeHexDigit(hex.charAt(i * 2)) << 4;
/* 226 */       int d2 = decodeHexDigit(hex.charAt(i * 2 + 1));
/* 227 */       result[i] = (byte)(d1 + d2);
/*     */     } 
/* 229 */     return of(result);
/*     */   }
/*     */   
/*     */   private static int decodeHexDigit(char c) {
/* 233 */     if (c >= '0' && c <= '9') return c - 48; 
/* 234 */     if (c >= 'a' && c <= 'f') return c - 97 + 10; 
/* 235 */     if (c >= 'A' && c <= 'F') return c - 65 + 10; 
/* 236 */     throw new IllegalArgumentException("Unexpected hex digit: " + c);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ByteString read(InputStream in, int byteCount) throws IOException {
/* 246 */     if (in == null) throw new IllegalArgumentException("in == null"); 
/* 247 */     if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
/*     */     
/* 249 */     byte[] result = new byte[byteCount];
/* 250 */     for (int offset = 0; offset < byteCount; offset += read) {
/* 251 */       int read = in.read(result, offset, byteCount - offset);
/* 252 */       if (read == -1) throw new EOFException(); 
/*     */     } 
/* 254 */     return new ByteString(result);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ByteString toAsciiLowercase() {
/* 264 */     for (int i = 0; i < this.data.length; ) {
/* 265 */       byte c = this.data[i];
/* 266 */       if (c < 65 || c > 90) {
/*     */         i++;
/*     */         continue;
/*     */       } 
/* 270 */       byte[] lowercase = (byte[])this.data.clone();
/* 271 */       lowercase[i++] = (byte)(c - -32);
/* 272 */       for (; i < lowercase.length; i++) {
/* 273 */         c = lowercase[i];
/* 274 */         if (c >= 65 && c <= 90)
/* 275 */           lowercase[i] = (byte)(c - -32); 
/*     */       } 
/* 277 */       return new ByteString(lowercase);
/*     */     } 
/* 279 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ByteString toAsciiUppercase() {
/* 289 */     for (int i = 0; i < this.data.length; ) {
/* 290 */       byte c = this.data[i];
/* 291 */       if (c < 97 || c > 122) {
/*     */         i++;
/*     */         continue;
/*     */       } 
/* 295 */       byte[] lowercase = (byte[])this.data.clone();
/* 296 */       lowercase[i++] = (byte)(c - 32);
/* 297 */       for (; i < lowercase.length; i++) {
/* 298 */         c = lowercase[i];
/* 299 */         if (c >= 97 && c <= 122)
/* 300 */           lowercase[i] = (byte)(c - 32); 
/*     */       } 
/* 302 */       return new ByteString(lowercase);
/*     */     } 
/* 304 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ByteString substring(int beginIndex) {
/* 312 */     return substring(beginIndex, this.data.length);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ByteString substring(int beginIndex, int endIndex) {
/* 321 */     if (beginIndex < 0) throw new IllegalArgumentException("beginIndex < 0"); 
/* 322 */     if (endIndex > this.data.length) {
/* 323 */       throw new IllegalArgumentException("endIndex > length(" + this.data.length + ")");
/*     */     }
/*     */     
/* 326 */     int subLen = endIndex - beginIndex;
/* 327 */     if (subLen < 0) throw new IllegalArgumentException("endIndex < beginIndex");
/*     */     
/* 329 */     if (beginIndex == 0 && endIndex == this.data.length) {
/* 330 */       return this;
/*     */     }
/*     */     
/* 333 */     byte[] copy = new byte[subLen];
/* 334 */     System.arraycopy(this.data, beginIndex, copy, 0, subLen);
/* 335 */     return new ByteString(copy);
/*     */   }
/*     */ 
/*     */   
/*     */   public byte getByte(int pos) {
/* 340 */     return this.data[pos];
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int size() {
/* 347 */     return this.data.length;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public byte[] toByteArray() {
/* 354 */     return (byte[])this.data.clone();
/*     */   }
/*     */ 
/*     */   
/*     */   byte[] internalArray() {
/* 359 */     return this.data;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ByteBuffer asByteBuffer() {
/* 366 */     return ByteBuffer.wrap(this.data).asReadOnlyBuffer();
/*     */   }
/*     */ 
/*     */   
/*     */   public void write(OutputStream out) throws IOException {
/* 371 */     if (out == null) throw new IllegalArgumentException("out == null"); 
/* 372 */     out.write(this.data);
/*     */   }
/*     */ 
/*     */   
/*     */   void write(Buffer buffer) {
/* 377 */     buffer.write(this.data, 0, this.data.length);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
/* 386 */     return other.rangeEquals(otherOffset, this.data, offset, byteCount);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
/* 395 */     return (offset >= 0 && offset <= this.data.length - byteCount && otherOffset >= 0 && otherOffset <= other.length - byteCount && 
/*     */       
/* 397 */       Util.arrayRangeEquals(this.data, offset, other, otherOffset, byteCount));
/*     */   }
/*     */   
/*     */   public final boolean startsWith(ByteString prefix) {
/* 401 */     return rangeEquals(0, prefix, 0, prefix.size());
/*     */   }
/*     */   
/*     */   public final boolean startsWith(byte[] prefix) {
/* 405 */     return rangeEquals(0, prefix, 0, prefix.length);
/*     */   }
/*     */   
/*     */   public final boolean endsWith(ByteString suffix) {
/* 409 */     return rangeEquals(size() - suffix.size(), suffix, 0, suffix.size());
/*     */   }
/*     */   
/*     */   public final boolean endsWith(byte[] suffix) {
/* 413 */     return rangeEquals(size() - suffix.length, suffix, 0, suffix.length);
/*     */   }
/*     */   
/*     */   public final int indexOf(ByteString other) {
/* 417 */     return indexOf(other.internalArray(), 0);
/*     */   }
/*     */   
/*     */   public final int indexOf(ByteString other, int fromIndex) {
/* 421 */     return indexOf(other.internalArray(), fromIndex);
/*     */   }
/*     */   
/*     */   public final int indexOf(byte[] other) {
/* 425 */     return indexOf(other, 0);
/*     */   }
/*     */   
/*     */   public int indexOf(byte[] other, int fromIndex) {
/* 429 */     fromIndex = Math.max(fromIndex, 0);
/* 430 */     for (int i = fromIndex, limit = this.data.length - other.length; i <= limit; i++) {
/* 431 */       if (Util.arrayRangeEquals(this.data, i, other, 0, other.length)) {
/* 432 */         return i;
/*     */       }
/*     */     } 
/* 435 */     return -1;
/*     */   }
/*     */   
/*     */   public final int lastIndexOf(ByteString other) {
/* 439 */     return lastIndexOf(other.internalArray(), size());
/*     */   }
/*     */   
/*     */   public final int lastIndexOf(ByteString other, int fromIndex) {
/* 443 */     return lastIndexOf(other.internalArray(), fromIndex);
/*     */   }
/*     */   
/*     */   public final int lastIndexOf(byte[] other) {
/* 447 */     return lastIndexOf(other, size());
/*     */   }
/*     */   
/*     */   public int lastIndexOf(byte[] other, int fromIndex) {
/* 451 */     fromIndex = Math.min(fromIndex, this.data.length - other.length);
/* 452 */     for (int i = fromIndex; i >= 0; i--) {
/* 453 */       if (Util.arrayRangeEquals(this.data, i, other, 0, other.length)) {
/* 454 */         return i;
/*     */       }
/*     */     } 
/* 457 */     return -1;
/*     */   }
/*     */   
/*     */   public boolean equals(Object o) {
/* 461 */     if (o == this) return true; 
/* 462 */     return (o instanceof ByteString && ((ByteString)o)
/* 463 */       .size() == this.data.length && ((ByteString)o)
/* 464 */       .rangeEquals(0, this.data, 0, this.data.length));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 468 */     int result = this.hashCode;
/* 469 */     return (result != 0) ? result : (this.hashCode = Arrays.hashCode(this.data));
/*     */   }
/*     */   
/*     */   public int compareTo(ByteString byteString) {
/* 473 */     int sizeA = size();
/* 474 */     int sizeB = byteString.size();
/* 475 */     for (int i = 0, size = Math.min(sizeA, sizeB); i < size; ) {
/* 476 */       int byteA = getByte(i) & 0xFF;
/* 477 */       int byteB = byteString.getByte(i) & 0xFF;
/* 478 */       if (byteA == byteB) { i++; continue; }
/* 479 */        return (byteA < byteB) ? -1 : 1;
/*     */     } 
/* 481 */     if (sizeA == sizeB) return 0; 
/* 482 */     return (sizeA < sizeB) ? -1 : 1;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 490 */     if (this.data.length == 0) {
/* 491 */       return "[size=0]";
/*     */     }
/*     */     
/* 494 */     String text = utf8();
/* 495 */     int i = codePointIndexToCharIndex(text, 64);
/*     */     
/* 497 */     if (i == -1) {
/* 498 */       return (this.data.length <= 64) ? (
/* 499 */         "[hex=" + hex() + "]") : (
/* 500 */         "[size=" + this.data.length + " hex=" + substring(0, 64).hex() + "…]");
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 506 */     String safeText = text.substring(0, i).replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
/* 507 */     return (i < text.length()) ? (
/* 508 */       "[size=" + this.data.length + " text=" + safeText + "…]") : (
/* 509 */       "[text=" + safeText + "]");
/*     */   }
/*     */   
/*     */   static int codePointIndexToCharIndex(String s, int codePointCount) {
/* 513 */     for (int i = 0, j = 0, length = s.length(); i < length; i += Character.charCount(c)) {
/* 514 */       if (j == codePointCount) {
/* 515 */         return i;
/*     */       }
/* 517 */       int c = s.codePointAt(i);
/* 518 */       if ((Character.isISOControl(c) && c != 10 && c != 13) || c == 65533)
/*     */       {
/* 520 */         return -1;
/*     */       }
/* 522 */       j++;
/*     */     } 
/* 524 */     return s.length();
/*     */   }
/*     */   
/*     */   private void readObject(ObjectInputStream in) throws IOException {
/* 528 */     int dataLength = in.readInt();
/* 529 */     ByteString byteString = read(in, dataLength);
/*     */     try {
/* 531 */       Field field = ByteString.class.getDeclaredField("data");
/* 532 */       field.setAccessible(true);
/* 533 */       field.set(this, byteString.data);
/* 534 */     } catch (NoSuchFieldException e) {
/* 535 */       throw new AssertionError();
/* 536 */     } catch (IllegalAccessException e) {
/* 537 */       throw new AssertionError();
/*     */     } 
/*     */   }
/*     */   
/*     */   private void writeObject(ObjectOutputStream out) throws IOException {
/* 542 */     out.writeInt(this.data.length);
/* 543 */     out.write(this.data);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\ByteString.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */