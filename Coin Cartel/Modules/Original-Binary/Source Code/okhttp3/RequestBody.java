/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.nio.charset.Charset;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.BufferedSink;
/*     */ import okio.ByteString;
/*     */ import okio.Okio;
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
/*     */ 
/*     */ 
/*     */ public abstract class RequestBody
/*     */ {
/*     */   @Nullable
/*     */   public abstract MediaType contentType();
/*     */   
/*     */   public long contentLength() throws IOException {
/*  37 */     return -1L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public abstract void writeTo(BufferedSink paramBufferedSink) throws IOException;
/*     */ 
/*     */ 
/*     */   
/*     */   public static RequestBody create(@Nullable MediaType contentType, String content) {
/*  48 */     Charset charset = Util.UTF_8;
/*  49 */     if (contentType != null) {
/*  50 */       charset = contentType.charset();
/*  51 */       if (charset == null) {
/*  52 */         charset = Util.UTF_8;
/*  53 */         contentType = MediaType.parse(contentType + "; charset=utf-8");
/*     */       } 
/*     */     } 
/*  56 */     byte[] bytes = content.getBytes(charset);
/*  57 */     return create(contentType, bytes);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static RequestBody create(@Nullable final MediaType contentType, final ByteString content) {
/*  63 */     return new RequestBody() { @Nullable
/*     */         public MediaType contentType() {
/*  65 */           return contentType;
/*     */         }
/*     */         
/*     */         public long contentLength() throws IOException {
/*  69 */           return content.size();
/*     */         }
/*     */         
/*     */         public void writeTo(BufferedSink sink) throws IOException {
/*  73 */           sink.write(content);
/*     */         } }
/*     */       ;
/*     */   }
/*     */ 
/*     */   
/*     */   public static RequestBody create(@Nullable MediaType contentType, byte[] content) {
/*  80 */     return create(contentType, content, 0, content.length);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static RequestBody create(@Nullable final MediaType contentType, final byte[] content, final int offset, final int byteCount) {
/*  86 */     if (content == null) throw new NullPointerException("content == null"); 
/*  87 */     Util.checkOffsetAndCount(content.length, offset, byteCount);
/*  88 */     return new RequestBody() { @Nullable
/*     */         public MediaType contentType() {
/*  90 */           return contentType;
/*     */         }
/*     */         
/*     */         public long contentLength() {
/*  94 */           return byteCount;
/*     */         }
/*     */         
/*     */         public void writeTo(BufferedSink sink) throws IOException {
/*  98 */           sink.write(content, offset, byteCount);
/*     */         } }
/*     */       ;
/*     */   }
/*     */ 
/*     */   
/*     */   public static RequestBody create(@Nullable final MediaType contentType, final File file) {
/* 105 */     if (file == null) throw new NullPointerException("file == null");
/*     */     
/* 107 */     return new RequestBody() { @Nullable
/*     */         public MediaType contentType() {
/* 109 */           return contentType;
/*     */         }
/*     */         
/*     */         public long contentLength() {
/* 113 */           return file.length();
/*     */         }
/*     */         
/*     */         public void writeTo(BufferedSink sink) throws IOException {
/* 117 */           Source source = null;
/*     */           try {
/* 119 */             source = Okio.source(file);
/* 120 */             sink.writeAll(source);
/*     */           } finally {
/* 122 */             Util.closeQuietly((Closeable)source);
/*     */           } 
/*     */         } }
/*     */       ;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\RequestBody.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */