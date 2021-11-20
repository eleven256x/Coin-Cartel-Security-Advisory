/*     */ package okio;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.charset.Charset;
/*     */ import javax.annotation.Nullable;
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
/*     */ final class RealBufferedSource
/*     */   implements BufferedSource
/*     */ {
/*  28 */   public final Buffer buffer = new Buffer();
/*     */   public final Source source;
/*     */   boolean closed;
/*     */   
/*     */   RealBufferedSource(Source source) {
/*  33 */     if (source == null) throw new NullPointerException("source == null"); 
/*  34 */     this.source = source;
/*     */   }
/*     */   
/*     */   public Buffer buffer() {
/*  38 */     return this.buffer;
/*     */   }
/*     */   
/*     */   public long read(Buffer sink, long byteCount) throws IOException {
/*  42 */     if (sink == null) throw new IllegalArgumentException("sink == null"); 
/*  43 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/*  44 */     if (this.closed) throw new IllegalStateException("closed");
/*     */     
/*  46 */     if (this.buffer.size == 0L) {
/*  47 */       long read = this.source.read(this.buffer, 8192L);
/*  48 */       if (read == -1L) return -1L;
/*     */     
/*     */     } 
/*  51 */     long toRead = Math.min(byteCount, this.buffer.size);
/*  52 */     return this.buffer.read(sink, toRead);
/*     */   }
/*     */   
/*     */   public boolean exhausted() throws IOException {
/*  56 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  57 */     return (this.buffer.exhausted() && this.source.read(this.buffer, 8192L) == -1L);
/*     */   }
/*     */   
/*     */   public void require(long byteCount) throws IOException {
/*  61 */     if (!request(byteCount)) throw new EOFException(); 
/*     */   }
/*     */   
/*     */   public boolean request(long byteCount) throws IOException {
/*  65 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/*  66 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  67 */     while (this.buffer.size < byteCount) {
/*  68 */       if (this.source.read(this.buffer, 8192L) == -1L) return false; 
/*     */     } 
/*  70 */     return true;
/*     */   }
/*     */   
/*     */   public byte readByte() throws IOException {
/*  74 */     require(1L);
/*  75 */     return this.buffer.readByte();
/*     */   }
/*     */   
/*     */   public ByteString readByteString() throws IOException {
/*  79 */     this.buffer.writeAll(this.source);
/*  80 */     return this.buffer.readByteString();
/*     */   }
/*     */   
/*     */   public ByteString readByteString(long byteCount) throws IOException {
/*  84 */     require(byteCount);
/*  85 */     return this.buffer.readByteString(byteCount);
/*     */   }
/*     */   public int select(Options options) throws IOException {
/*     */     int index;
/*  89 */     if (this.closed) throw new IllegalStateException("closed");
/*     */     
/*     */     while (true) {
/*  92 */       index = this.buffer.selectPrefix(options, true);
/*  93 */       if (index == -1) return -1; 
/*  94 */       if (index == -2) {
/*     */         
/*  96 */         if (this.source.read(this.buffer, 8192L) == -1L) return -1;  continue;
/*     */       }  break;
/*     */     } 
/*  99 */     int selectedSize = options.byteStrings[index].size();
/* 100 */     this.buffer.skip(selectedSize);
/* 101 */     return index;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public byte[] readByteArray() throws IOException {
/* 107 */     this.buffer.writeAll(this.source);
/* 108 */     return this.buffer.readByteArray();
/*     */   }
/*     */   
/*     */   public byte[] readByteArray(long byteCount) throws IOException {
/* 112 */     require(byteCount);
/* 113 */     return this.buffer.readByteArray(byteCount);
/*     */   }
/*     */   
/*     */   public int read(byte[] sink) throws IOException {
/* 117 */     return read(sink, 0, sink.length);
/*     */   }
/*     */   
/*     */   public void readFully(byte[] sink) throws IOException {
/*     */     try {
/* 122 */       require(sink.length);
/* 123 */     } catch (EOFException e) {
/*     */       
/* 125 */       int offset = 0;
/* 126 */       while (this.buffer.size > 0L) {
/* 127 */         int read = this.buffer.read(sink, offset, (int)this.buffer.size);
/* 128 */         if (read == -1) throw new AssertionError(); 
/* 129 */         offset += read;
/*     */       } 
/* 131 */       throw e;
/*     */     } 
/* 133 */     this.buffer.readFully(sink);
/*     */   }
/*     */   
/*     */   public int read(byte[] sink, int offset, int byteCount) throws IOException {
/* 137 */     Util.checkOffsetAndCount(sink.length, offset, byteCount);
/*     */     
/* 139 */     if (this.buffer.size == 0L) {
/* 140 */       long read = this.source.read(this.buffer, 8192L);
/* 141 */       if (read == -1L) return -1;
/*     */     
/*     */     } 
/* 144 */     int toRead = (int)Math.min(byteCount, this.buffer.size);
/* 145 */     return this.buffer.read(sink, offset, toRead);
/*     */   }
/*     */   
/*     */   public int read(ByteBuffer sink) throws IOException {
/* 149 */     if (this.buffer.size == 0L) {
/* 150 */       long read = this.source.read(this.buffer, 8192L);
/* 151 */       if (read == -1L) return -1;
/*     */     
/*     */     } 
/* 154 */     return this.buffer.read(sink);
/*     */   }
/*     */   
/*     */   public void readFully(Buffer sink, long byteCount) throws IOException {
/*     */     try {
/* 159 */       require(byteCount);
/* 160 */     } catch (EOFException e) {
/*     */       
/* 162 */       sink.writeAll(this.buffer);
/* 163 */       throw e;
/*     */     } 
/* 165 */     this.buffer.readFully(sink, byteCount);
/*     */   }
/*     */   
/*     */   public long readAll(Sink sink) throws IOException {
/* 169 */     if (sink == null) throw new IllegalArgumentException("sink == null");
/*     */     
/* 171 */     long totalBytesWritten = 0L;
/* 172 */     while (this.source.read(this.buffer, 8192L) != -1L) {
/* 173 */       long emitByteCount = this.buffer.completeSegmentByteCount();
/* 174 */       if (emitByteCount > 0L) {
/* 175 */         totalBytesWritten += emitByteCount;
/* 176 */         sink.write(this.buffer, emitByteCount);
/*     */       } 
/*     */     } 
/* 179 */     if (this.buffer.size() > 0L) {
/* 180 */       totalBytesWritten += this.buffer.size();
/* 181 */       sink.write(this.buffer, this.buffer.size());
/*     */     } 
/* 183 */     return totalBytesWritten;
/*     */   }
/*     */   
/*     */   public String readUtf8() throws IOException {
/* 187 */     this.buffer.writeAll(this.source);
/* 188 */     return this.buffer.readUtf8();
/*     */   }
/*     */   
/*     */   public String readUtf8(long byteCount) throws IOException {
/* 192 */     require(byteCount);
/* 193 */     return this.buffer.readUtf8(byteCount);
/*     */   }
/*     */   
/*     */   public String readString(Charset charset) throws IOException {
/* 197 */     if (charset == null) throw new IllegalArgumentException("charset == null");
/*     */     
/* 199 */     this.buffer.writeAll(this.source);
/* 200 */     return this.buffer.readString(charset);
/*     */   }
/*     */   
/*     */   public String readString(long byteCount, Charset charset) throws IOException {
/* 204 */     require(byteCount);
/* 205 */     if (charset == null) throw new IllegalArgumentException("charset == null"); 
/* 206 */     return this.buffer.readString(byteCount, charset);
/*     */   }
/*     */   @Nullable
/*     */   public String readUtf8Line() throws IOException {
/* 210 */     long newline = indexOf((byte)10);
/*     */     
/* 212 */     if (newline == -1L) {
/* 213 */       return (this.buffer.size != 0L) ? readUtf8(this.buffer.size) : null;
/*     */     }
/*     */     
/* 216 */     return this.buffer.readUtf8Line(newline);
/*     */   }
/*     */   
/*     */   public String readUtf8LineStrict() throws IOException {
/* 220 */     return readUtf8LineStrict(Long.MAX_VALUE);
/*     */   }
/*     */   
/*     */   public String readUtf8LineStrict(long limit) throws IOException {
/* 224 */     if (limit < 0L) throw new IllegalArgumentException("limit < 0: " + limit); 
/* 225 */     long scanLength = (limit == Long.MAX_VALUE) ? Long.MAX_VALUE : (limit + 1L);
/* 226 */     long newline = indexOf((byte)10, 0L, scanLength);
/* 227 */     if (newline != -1L) return this.buffer.readUtf8Line(newline); 
/* 228 */     if (scanLength < Long.MAX_VALUE && 
/* 229 */       request(scanLength) && this.buffer.getByte(scanLength - 1L) == 13 && 
/* 230 */       request(scanLength + 1L) && this.buffer.getByte(scanLength) == 10) {
/* 231 */       return this.buffer.readUtf8Line(scanLength);
/*     */     }
/* 233 */     Buffer data = new Buffer();
/* 234 */     this.buffer.copyTo(data, 0L, Math.min(32L, this.buffer.size()));
/* 235 */     throw new EOFException("\\n not found: limit=" + Math.min(this.buffer.size(), limit) + " content=" + data
/* 236 */         .readByteString().hex() + '…');
/*     */   }
/*     */   
/*     */   public int readUtf8CodePoint() throws IOException {
/* 240 */     require(1L);
/*     */     
/* 242 */     byte b0 = this.buffer.getByte(0L);
/* 243 */     if ((b0 & 0xE0) == 192) {
/* 244 */       require(2L);
/* 245 */     } else if ((b0 & 0xF0) == 224) {
/* 246 */       require(3L);
/* 247 */     } else if ((b0 & 0xF8) == 240) {
/* 248 */       require(4L);
/*     */     } 
/*     */     
/* 251 */     return this.buffer.readUtf8CodePoint();
/*     */   }
/*     */   
/*     */   public short readShort() throws IOException {
/* 255 */     require(2L);
/* 256 */     return this.buffer.readShort();
/*     */   }
/*     */   
/*     */   public short readShortLe() throws IOException {
/* 260 */     require(2L);
/* 261 */     return this.buffer.readShortLe();
/*     */   }
/*     */   
/*     */   public int readInt() throws IOException {
/* 265 */     require(4L);
/* 266 */     return this.buffer.readInt();
/*     */   }
/*     */   
/*     */   public int readIntLe() throws IOException {
/* 270 */     require(4L);
/* 271 */     return this.buffer.readIntLe();
/*     */   }
/*     */   
/*     */   public long readLong() throws IOException {
/* 275 */     require(8L);
/* 276 */     return this.buffer.readLong();
/*     */   }
/*     */   
/*     */   public long readLongLe() throws IOException {
/* 280 */     require(8L);
/* 281 */     return this.buffer.readLongLe();
/*     */   }
/*     */   
/*     */   public long readDecimalLong() throws IOException {
/* 285 */     require(1L);
/*     */     
/* 287 */     for (int pos = 0; request((pos + 1)); pos++) {
/* 288 */       byte b = this.buffer.getByte(pos);
/* 289 */       if ((b < 48 || b > 57) && (pos != 0 || b != 45)) {
/*     */         
/* 291 */         if (pos == 0) {
/* 292 */           throw new NumberFormatException(String.format("Expected leading [0-9] or '-' character but was %#x", new Object[] {
/* 293 */                   Byte.valueOf(b)
/*     */                 }));
/*     */         }
/*     */         break;
/*     */       } 
/*     */     } 
/* 299 */     return this.buffer.readDecimalLong();
/*     */   }
/*     */   
/*     */   public long readHexadecimalUnsignedLong() throws IOException {
/* 303 */     require(1L);
/*     */     
/* 305 */     for (int pos = 0; request((pos + 1)); pos++) {
/* 306 */       byte b = this.buffer.getByte(pos);
/* 307 */       if ((b < 48 || b > 57) && (b < 97 || b > 102) && (b < 65 || b > 70)) {
/*     */         
/* 309 */         if (pos == 0) {
/* 310 */           throw new NumberFormatException(String.format("Expected leading [0-9a-fA-F] character but was %#x", new Object[] {
/* 311 */                   Byte.valueOf(b)
/*     */                 }));
/*     */         }
/*     */         break;
/*     */       } 
/*     */     } 
/* 317 */     return this.buffer.readHexadecimalUnsignedLong();
/*     */   }
/*     */   
/*     */   public void skip(long byteCount) throws IOException {
/* 321 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 322 */     while (byteCount > 0L) {
/* 323 */       if (this.buffer.size == 0L && this.source.read(this.buffer, 8192L) == -1L) {
/* 324 */         throw new EOFException();
/*     */       }
/* 326 */       long toSkip = Math.min(byteCount, this.buffer.size());
/* 327 */       this.buffer.skip(toSkip);
/* 328 */       byteCount -= toSkip;
/*     */     } 
/*     */   }
/*     */   
/*     */   public long indexOf(byte b) throws IOException {
/* 333 */     return indexOf(b, 0L, Long.MAX_VALUE);
/*     */   }
/*     */   
/*     */   public long indexOf(byte b, long fromIndex) throws IOException {
/* 337 */     return indexOf(b, fromIndex, Long.MAX_VALUE);
/*     */   }
/*     */   
/*     */   public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
/* 341 */     if (this.closed) throw new IllegalStateException("closed"); 
/* 342 */     if (fromIndex < 0L || toIndex < fromIndex) {
/* 343 */       throw new IllegalArgumentException(
/* 344 */           String.format("fromIndex=%s toIndex=%s", new Object[] { Long.valueOf(fromIndex), Long.valueOf(toIndex) }));
/*     */     }
/*     */     
/* 347 */     while (fromIndex < toIndex) {
/* 348 */       long result = this.buffer.indexOf(b, fromIndex, toIndex);
/* 349 */       if (result != -1L) return result;
/*     */ 
/*     */ 
/*     */       
/* 353 */       long lastBufferSize = this.buffer.size;
/* 354 */       if (lastBufferSize >= toIndex || this.source.read(this.buffer, 8192L) == -1L) return -1L;
/*     */ 
/*     */       
/* 357 */       fromIndex = Math.max(fromIndex, lastBufferSize);
/*     */     } 
/* 359 */     return -1L;
/*     */   }
/*     */   
/*     */   public long indexOf(ByteString bytes) throws IOException {
/* 363 */     return indexOf(bytes, 0L);
/*     */   }
/*     */   
/*     */   public long indexOf(ByteString bytes, long fromIndex) throws IOException {
/* 367 */     if (this.closed) throw new IllegalStateException("closed");
/*     */     
/*     */     while (true) {
/* 370 */       long result = this.buffer.indexOf(bytes, fromIndex);
/* 371 */       if (result != -1L) return result;
/*     */       
/* 373 */       long lastBufferSize = this.buffer.size;
/* 374 */       if (this.source.read(this.buffer, 8192L) == -1L) return -1L;
/*     */ 
/*     */       
/* 377 */       fromIndex = Math.max(fromIndex, lastBufferSize - bytes.size() + 1L);
/*     */     } 
/*     */   }
/*     */   
/*     */   public long indexOfElement(ByteString targetBytes) throws IOException {
/* 382 */     return indexOfElement(targetBytes, 0L);
/*     */   }
/*     */   
/*     */   public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
/* 386 */     if (this.closed) throw new IllegalStateException("closed");
/*     */     
/*     */     while (true) {
/* 389 */       long result = this.buffer.indexOfElement(targetBytes, fromIndex);
/* 390 */       if (result != -1L) return result;
/*     */       
/* 392 */       long lastBufferSize = this.buffer.size;
/* 393 */       if (this.source.read(this.buffer, 8192L) == -1L) return -1L;
/*     */ 
/*     */       
/* 396 */       fromIndex = Math.max(fromIndex, lastBufferSize);
/*     */     } 
/*     */   }
/*     */   
/*     */   public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
/* 401 */     return rangeEquals(offset, bytes, 0, bytes.size());
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
/* 407 */     if (this.closed) throw new IllegalStateException("closed");
/*     */     
/* 409 */     if (offset < 0L || bytesOffset < 0 || byteCount < 0 || bytes
/*     */ 
/*     */       
/* 412 */       .size() - bytesOffset < byteCount) {
/* 413 */       return false;
/*     */     }
/* 415 */     for (int i = 0; i < byteCount; i++) {
/* 416 */       long bufferOffset = offset + i;
/* 417 */       if (!request(bufferOffset + 1L)) return false; 
/* 418 */       if (this.buffer.getByte(bufferOffset) != bytes.getByte(bytesOffset + i)) return false; 
/*     */     } 
/* 420 */     return true;
/*     */   }
/*     */   
/*     */   public InputStream inputStream() {
/* 424 */     return new InputStream() {
/*     */         public int read() throws IOException {
/* 426 */           if (RealBufferedSource.this.closed) throw new IOException("closed"); 
/* 427 */           if (RealBufferedSource.this.buffer.size == 0L) {
/* 428 */             long count = RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192L);
/* 429 */             if (count == -1L) return -1; 
/*     */           } 
/* 431 */           return RealBufferedSource.this.buffer.readByte() & 0xFF;
/*     */         }
/*     */         
/*     */         public int read(byte[] data, int offset, int byteCount) throws IOException {
/* 435 */           if (RealBufferedSource.this.closed) throw new IOException("closed"); 
/* 436 */           Util.checkOffsetAndCount(data.length, offset, byteCount);
/*     */           
/* 438 */           if (RealBufferedSource.this.buffer.size == 0L) {
/* 439 */             long count = RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192L);
/* 440 */             if (count == -1L) return -1;
/*     */           
/*     */           } 
/* 443 */           return RealBufferedSource.this.buffer.read(data, offset, byteCount);
/*     */         }
/*     */         
/*     */         public int available() throws IOException {
/* 447 */           if (RealBufferedSource.this.closed) throw new IOException("closed"); 
/* 448 */           return (int)Math.min(RealBufferedSource.this.buffer.size, 2147483647L);
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/* 452 */           RealBufferedSource.this.close();
/*     */         }
/*     */         
/*     */         public String toString() {
/* 456 */           return RealBufferedSource.this + ".inputStream()";
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   public boolean isOpen() {
/* 462 */     return !this.closed;
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 466 */     if (this.closed)
/* 467 */       return;  this.closed = true;
/* 468 */     this.source.close();
/* 469 */     this.buffer.clear();
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/* 473 */     return this.source.timeout();
/*     */   }
/*     */   
/*     */   public String toString() {
/* 477 */     return "buffer(" + this.source + ")";
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\RealBufferedSource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */