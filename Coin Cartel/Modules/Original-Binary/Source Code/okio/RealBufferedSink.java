/*     */ package okio;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.charset.Charset;
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
/*     */ final class RealBufferedSink
/*     */   implements BufferedSink
/*     */ {
/*  25 */   public final Buffer buffer = new Buffer();
/*     */   public final Sink sink;
/*     */   boolean closed;
/*     */   
/*     */   RealBufferedSink(Sink sink) {
/*  30 */     if (sink == null) throw new NullPointerException("sink == null"); 
/*  31 */     this.sink = sink;
/*     */   }
/*     */   
/*     */   public Buffer buffer() {
/*  35 */     return this.buffer;
/*     */   }
/*     */ 
/*     */   
/*     */   public void write(Buffer source, long byteCount) throws IOException {
/*  40 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  41 */     this.buffer.write(source, byteCount);
/*  42 */     emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink write(ByteString byteString) throws IOException {
/*  46 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  47 */     this.buffer.write(byteString);
/*  48 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeUtf8(String string) throws IOException {
/*  52 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  53 */     this.buffer.writeUtf8(string);
/*  54 */     return emitCompleteSegments();
/*     */   }
/*     */ 
/*     */   
/*     */   public BufferedSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
/*  59 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  60 */     this.buffer.writeUtf8(string, beginIndex, endIndex);
/*  61 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeUtf8CodePoint(int codePoint) throws IOException {
/*  65 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  66 */     this.buffer.writeUtf8CodePoint(codePoint);
/*  67 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeString(String string, Charset charset) throws IOException {
/*  71 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  72 */     this.buffer.writeString(string, charset);
/*  73 */     return emitCompleteSegments();
/*     */   }
/*     */ 
/*     */   
/*     */   public BufferedSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
/*  78 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  79 */     this.buffer.writeString(string, beginIndex, endIndex, charset);
/*  80 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink write(byte[] source) throws IOException {
/*  84 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  85 */     this.buffer.write(source);
/*  86 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink write(byte[] source, int offset, int byteCount) throws IOException {
/*  90 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  91 */     this.buffer.write(source, offset, byteCount);
/*  92 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public int write(ByteBuffer source) throws IOException {
/*  96 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  97 */     int result = this.buffer.write(source);
/*  98 */     emitCompleteSegments();
/*  99 */     return result;
/*     */   }
/*     */   
/*     */   public long writeAll(Source source) throws IOException {
/* 103 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/* 104 */     long totalBytesRead = 0L; long readCount;
/* 105 */     while ((readCount = source.read(this.buffer, 8192L)) != -1L) {
/* 106 */       totalBytesRead += readCount;
/* 107 */       emitCompleteSegments();
/*     */     } 
/* 109 */     return totalBytesRead;
/*     */   }
/*     */   
/*     */   public BufferedSink write(Source source, long byteCount) throws IOException {
/* 113 */     while (byteCount > 0L) {
/* 114 */       long read = source.read(this.buffer, byteCount);
/* 115 */       if (read == -1L) throw new EOFException(); 
/* 116 */       byteCount -= read;
/* 117 */       emitCompleteSegments();
/*     */     } 
/* 119 */     return this;
/*     */   }
/*     */   
/*     */   public BufferedSink writeByte(int b) throws IOException {
/* 123 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 124 */     this.buffer.writeByte(b);
/* 125 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeShort(int s) throws IOException {
/* 129 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 130 */     this.buffer.writeShort(s);
/* 131 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeShortLe(int s) throws IOException {
/* 135 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 136 */     this.buffer.writeShortLe(s);
/* 137 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeInt(int i) throws IOException {
/* 141 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 142 */     this.buffer.writeInt(i);
/* 143 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeIntLe(int i) throws IOException {
/* 147 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 148 */     this.buffer.writeIntLe(i);
/* 149 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeLong(long v) throws IOException {
/* 153 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 154 */     this.buffer.writeLong(v);
/* 155 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeLongLe(long v) throws IOException {
/* 159 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 160 */     this.buffer.writeLongLe(v);
/* 161 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeDecimalLong(long v) throws IOException {
/* 165 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 166 */     this.buffer.writeDecimalLong(v);
/* 167 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink writeHexadecimalUnsignedLong(long v) throws IOException {
/* 171 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 172 */     this.buffer.writeHexadecimalUnsignedLong(v);
/* 173 */     return emitCompleteSegments();
/*     */   }
/*     */   
/*     */   public BufferedSink emitCompleteSegments() throws IOException {
/* 177 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 178 */     long byteCount = this.buffer.completeSegmentByteCount();
/* 179 */     if (byteCount > 0L) this.sink.write(this.buffer, byteCount); 
/* 180 */     return this;
/*     */   }
/*     */   
/*     */   public BufferedSink emit() throws IOException {
/* 184 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 185 */     long byteCount = this.buffer.size();
/* 186 */     if (byteCount > 0L) this.sink.write(this.buffer, byteCount); 
/* 187 */     return this;
/*     */   }
/*     */   
/*     */   public OutputStream outputStream() {
/* 191 */     return new OutputStream() {
/*     */         public void write(int b) throws IOException {
/* 193 */           if (RealBufferedSink.this.closed) throw new IOException("closed"); 
/* 194 */           RealBufferedSink.this.buffer.writeByte((byte)b);
/* 195 */           RealBufferedSink.this.emitCompleteSegments();
/*     */         }
/*     */         
/*     */         public void write(byte[] data, int offset, int byteCount) throws IOException {
/* 199 */           if (RealBufferedSink.this.closed) throw new IOException("closed"); 
/* 200 */           RealBufferedSink.this.buffer.write(data, offset, byteCount);
/* 201 */           RealBufferedSink.this.emitCompleteSegments();
/*     */         }
/*     */ 
/*     */         
/*     */         public void flush() throws IOException {
/* 206 */           if (!RealBufferedSink.this.closed) {
/* 207 */             RealBufferedSink.this.flush();
/*     */           }
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/* 212 */           RealBufferedSink.this.close();
/*     */         }
/*     */         
/*     */         public String toString() {
/* 216 */           return RealBufferedSink.this + ".outputStream()";
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   public void flush() throws IOException {
/* 222 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 223 */     if (this.buffer.size > 0L) {
/* 224 */       this.sink.write(this.buffer, this.buffer.size);
/*     */     }
/* 226 */     this.sink.flush();
/*     */   }
/*     */   
/*     */   public boolean isOpen() {
/* 230 */     return !this.closed;
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 234 */     if (this.closed) {
/*     */       return;
/*     */     }
/*     */     
/* 238 */     Throwable thrown = null;
/*     */     try {
/* 240 */       if (this.buffer.size > 0L) {
/* 241 */         this.sink.write(this.buffer, this.buffer.size);
/*     */       }
/* 243 */     } catch (Throwable e) {
/* 244 */       thrown = e;
/*     */     } 
/*     */     
/*     */     try {
/* 248 */       this.sink.close();
/* 249 */     } catch (Throwable e) {
/* 250 */       if (thrown == null) thrown = e; 
/*     */     } 
/* 252 */     this.closed = true;
/*     */     
/* 254 */     if (thrown != null) Util.sneakyRethrow(thrown); 
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/* 258 */     return this.sink.timeout();
/*     */   }
/*     */   
/*     */   public String toString() {
/* 262 */     return "buffer(" + this.sink + ")";
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\RealBufferedSink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */