/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.security.InvalidKeyException;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.NoSuchAlgorithmException;
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
/*     */ public final class HashingSource
/*     */   extends ForwardingSource
/*     */ {
/*     */   private final MessageDigest messageDigest;
/*     */   private final Mac mac;
/*     */   
/*     */   public static HashingSource md5(Source source) {
/*  47 */     return new HashingSource(source, "MD5");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSource sha1(Source source) {
/*  52 */     return new HashingSource(source, "SHA-1");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSource sha256(Source source) {
/*  57 */     return new HashingSource(source, "SHA-256");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSource hmacSha1(Source source, ByteString key) {
/*  62 */     return new HashingSource(source, key, "HmacSHA1");
/*     */   }
/*     */ 
/*     */   
/*     */   public static HashingSource hmacSha256(Source source, ByteString key) {
/*  67 */     return new HashingSource(source, key, "HmacSHA256");
/*     */   }
/*     */   
/*     */   private HashingSource(Source source, String algorithm) {
/*  71 */     super(source);
/*     */     try {
/*  73 */       this.messageDigest = MessageDigest.getInstance(algorithm);
/*  74 */       this.mac = null;
/*  75 */     } catch (NoSuchAlgorithmException e) {
/*  76 */       throw new AssertionError();
/*     */     } 
/*     */   }
/*     */   
/*     */   private HashingSource(Source source, ByteString key, String algorithm) {
/*  81 */     super(source);
/*     */     try {
/*  83 */       this.mac = Mac.getInstance(algorithm);
/*  84 */       this.mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
/*  85 */       this.messageDigest = null;
/*  86 */     } catch (NoSuchAlgorithmException e) {
/*  87 */       throw new AssertionError();
/*  88 */     } catch (InvalidKeyException e) {
/*  89 */       throw new IllegalArgumentException(e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public long read(Buffer sink, long byteCount) throws IOException {
/*  94 */     long result = super.read(sink, byteCount);
/*     */     
/*  96 */     if (result != -1L) {
/*  97 */       long start = sink.size - result;
/*     */ 
/*     */       
/* 100 */       long offset = sink.size;
/* 101 */       Segment s = sink.head;
/* 102 */       while (offset > start) {
/* 103 */         s = s.prev;
/* 104 */         offset -= (s.limit - s.pos);
/*     */       } 
/*     */ 
/*     */       
/* 108 */       while (offset < sink.size) {
/* 109 */         int pos = (int)(s.pos + start - offset);
/* 110 */         if (this.messageDigest != null) {
/* 111 */           this.messageDigest.update(s.data, pos, s.limit - pos);
/*     */         } else {
/* 113 */           this.mac.update(s.data, pos, s.limit - pos);
/*     */         } 
/* 115 */         offset += (s.limit - s.pos);
/* 116 */         start = offset;
/* 117 */         s = s.next;
/*     */       } 
/*     */     } 
/*     */     
/* 121 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final ByteString hash() {
/* 131 */     byte[] result = (this.messageDigest != null) ? this.messageDigest.digest() : this.mac.doFinal();
/* 132 */     return ByteString.of(result);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\HashingSource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */