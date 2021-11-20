/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.nio.charset.Charset;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSource;
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
/*     */ public abstract class ResponseBody
/*     */   implements Closeable
/*     */ {
/*     */   @Nullable
/*     */   private Reader reader;
/*     */   
/*     */   @Nullable
/*     */   public abstract MediaType contentType();
/*     */   
/*     */   public abstract long contentLength();
/*     */   
/*     */   public final InputStream byteStream() {
/* 116 */     return source().inputStream();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public abstract BufferedSource source();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final byte[] bytes() throws IOException {
/*     */     byte[] bytes;
/* 129 */     long contentLength = contentLength();
/* 130 */     if (contentLength > 2147483647L) {
/* 131 */       throw new IOException("Cannot buffer entire body for content length: " + contentLength);
/*     */     }
/*     */     
/* 134 */     BufferedSource source = source();
/*     */     
/*     */     try {
/* 137 */       bytes = source.readByteArray();
/*     */     } finally {
/* 139 */       Util.closeQuietly((Closeable)source);
/*     */     } 
/* 141 */     if (contentLength != -1L && contentLength != bytes.length) {
/* 142 */       throw new IOException("Content-Length (" + contentLength + ") and stream length (" + bytes.length + ") disagree");
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 148 */     return bytes;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final Reader charStream() {
/* 158 */     Reader r = this.reader;
/* 159 */     return (r != null) ? r : (this.reader = new BomAwareReader(source(), charset()));
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
/*     */   public final String string() throws IOException {
/* 173 */     BufferedSource source = source();
/*     */     try {
/* 175 */       Charset charset = Util.bomAwareCharset(source, charset());
/* 176 */       return source.readString(charset);
/*     */     } finally {
/* 178 */       Util.closeQuietly((Closeable)source);
/*     */     } 
/*     */   }
/*     */   
/*     */   private Charset charset() {
/* 183 */     MediaType contentType = contentType();
/* 184 */     return (contentType != null) ? contentType.charset(Util.UTF_8) : Util.UTF_8;
/*     */   }
/*     */   
/*     */   public void close() {
/* 188 */     Util.closeQuietly((Closeable)source());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ResponseBody create(@Nullable MediaType contentType, String content) {
/* 196 */     Charset charset = Util.UTF_8;
/* 197 */     if (contentType != null) {
/* 198 */       charset = contentType.charset();
/* 199 */       if (charset == null) {
/* 200 */         charset = Util.UTF_8;
/* 201 */         contentType = MediaType.parse(contentType + "; charset=utf-8");
/*     */       } 
/*     */     } 
/* 204 */     Buffer buffer = (new Buffer()).writeString(content, charset);
/* 205 */     return create(contentType, buffer.size(), (BufferedSource)buffer);
/*     */   }
/*     */ 
/*     */   
/*     */   public static ResponseBody create(@Nullable MediaType contentType, byte[] content) {
/* 210 */     Buffer buffer = (new Buffer()).write(content);
/* 211 */     return create(contentType, content.length, (BufferedSource)buffer);
/*     */   }
/*     */ 
/*     */   
/*     */   public static ResponseBody create(@Nullable MediaType contentType, ByteString content) {
/* 216 */     Buffer buffer = (new Buffer()).write(content);
/* 217 */     return create(contentType, content.size(), (BufferedSource)buffer);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static ResponseBody create(@Nullable final MediaType contentType, final long contentLength, final BufferedSource content) {
/* 223 */     if (content == null) throw new NullPointerException("source == null"); 
/* 224 */     return new ResponseBody() { @Nullable
/*     */         public MediaType contentType() {
/* 226 */           return contentType;
/*     */         }
/*     */         
/*     */         public long contentLength() {
/* 230 */           return contentLength;
/*     */         }
/*     */         
/*     */         public BufferedSource source() {
/* 234 */           return content;
/*     */         } }
/*     */       ;
/*     */   }
/*     */   
/*     */   static final class BomAwareReader extends Reader {
/*     */     private final BufferedSource source;
/*     */     private final Charset charset;
/*     */     private boolean closed;
/*     */     @Nullable
/*     */     private Reader delegate;
/*     */     
/*     */     BomAwareReader(BufferedSource source, Charset charset) {
/* 247 */       this.source = source;
/* 248 */       this.charset = charset;
/*     */     }
/*     */     
/*     */     public int read(char[] cbuf, int off, int len) throws IOException {
/* 252 */       if (this.closed) throw new IOException("Stream closed");
/*     */       
/* 254 */       Reader delegate = this.delegate;
/* 255 */       if (delegate == null) {
/* 256 */         Charset charset = Util.bomAwareCharset(this.source, this.charset);
/* 257 */         delegate = this.delegate = new InputStreamReader(this.source.inputStream(), charset);
/*     */       } 
/* 259 */       return delegate.read(cbuf, off, len);
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 263 */       this.closed = true;
/* 264 */       if (this.delegate != null) {
/* 265 */         this.delegate.close();
/*     */       } else {
/* 267 */         this.source.close();
/*     */       } 
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\ResponseBody.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */