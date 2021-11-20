/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.security.InvalidKeyException;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.NoSuchAlgorithmException;
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
/*     */ 
/*     */ public final class HashingSink
/*     */   extends ForwardingSink
/*     */ {
/*     */   @Nullable
/*     */   private final MessageDigest messageDigest;
/*     */   @Nullable
/*     */   private final Mac mac;
/*     */   
/*     */   public static HashingSink md5(Sink sink) {
/*  50 */     return new HashingSink(sink, "MD5");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSink sha1(Sink sink) {
/*  55 */     return new HashingSink(sink, "SHA-1");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSink sha256(Sink sink) {
/*  60 */     return new HashingSink(sink, "SHA-256");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSink sha512(Sink sink) {
/*  65 */     return new HashingSink(sink, "SHA-512");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSink hmacSha1(Sink sink, ByteString key) {
/*  70 */     return new HashingSink(sink, key, "HmacSHA1");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSink hmacSha256(Sink sink, ByteString key) {
/*  75 */     return new HashingSink(sink, key, "HmacSHA256");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSink hmacSha512(Sink sink, ByteString key) {
/*  80 */     return new HashingSink(sink, key, "HmacSHA512");
/*     */   }
/*     */   
/*     */   private HashingSink(Sink sink, String algorithm) {
/*  84 */     super(sink);
/*     */     try {
/*  86 */       this.messageDigest = MessageDigest.getInstance(algorithm);
/*  87 */       this.mac = null;
/*  88 */     } catch (NoSuchAlgorithmException e) {
/*  89 */       throw new AssertionError();
/*     */     } 
/*     */   }
/*     */   
/*     */   private HashingSink(Sink sink, ByteString key, String algorithm) {
/*  94 */     super(sink);
/*     */     try {
/*  96 */       this.mac = Mac.getInstance(algorithm);
/*  97 */       this.mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
/*  98 */       this.messageDigest = null;
/*  99 */     } catch (NoSuchAlgorithmException e) {
/* 100 */       throw new AssertionError();
/* 101 */     } catch (InvalidKeyException e) {
/* 102 */       throw new IllegalArgumentException(e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void write(Buffer source, long byteCount) throws IOException {
/* 107 */     Util.checkOffsetAndCount(source.size, 0L, byteCount);
/*     */ 
/*     */     
/* 110 */     long hashedCount = 0L;
/* 111 */     for (Segment s = source.head; hashedCount < byteCount; s = s.next) {
/* 112 */       int toHash = (int)Math.min(byteCount - hashedCount, (s.limit - s.pos));
/* 113 */       if (this.messageDigest != null) {
/* 114 */         this.messageDigest.update(s.data, s.pos, toHash);
/*     */       } else {
/* 116 */         this.mac.update(s.data, s.pos, toHash);
/*     */       } 
/* 118 */       hashedCount += toHash;
/*     */     } 
/*     */ 
/*     */     
/* 122 */     super.write(source, byteCount);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final ByteString hash() {
/* 132 */     byte[] result = (this.messageDigest != null) ? this.messageDigest.digest() : this.mac.doFinal();
/* 133 */     return ByteString.of(result);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\HashingSink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */