/*      */ package okio;
/*      */ 
/*      */ import java.io.Closeable;
/*      */ import java.io.EOFException;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.nio.ByteBuffer;
/*      */ import java.nio.channels.ByteChannel;
/*      */ import java.nio.charset.Charset;
/*      */ import java.security.InvalidKeyException;
/*      */ import java.security.MessageDigest;
/*      */ import java.security.NoSuchAlgorithmException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.List;
/*      */ import javax.annotation.Nullable;
/*      */ import javax.crypto.Mac;
/*      */ import javax.crypto.spec.SecretKeySpec;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public final class Buffer
/*      */   implements BufferedSource, BufferedSink, Cloneable, ByteChannel
/*      */ {
/*   55 */   private static final byte[] DIGITS = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
/*      */ 
/*      */   
/*      */   static final int REPLACEMENT_CHARACTER = 65533;
/*      */   
/*      */   @Nullable
/*      */   Segment head;
/*      */   
/*      */   long size;
/*      */ 
/*      */   
/*      */   public final long size() {
/*   67 */     return this.size;
/*      */   }
/*      */   
/*      */   public Buffer buffer() {
/*   71 */     return this;
/*      */   }
/*      */   
/*      */   public OutputStream outputStream() {
/*   75 */     return new OutputStream() {
/*      */         public void write(int b) {
/*   77 */           Buffer.this.writeByte((byte)b);
/*      */         }
/*      */         
/*      */         public void write(byte[] data, int offset, int byteCount) {
/*   81 */           Buffer.this.write(data, offset, byteCount);
/*      */         }
/*      */ 
/*      */         
/*      */         public void flush() {}
/*      */ 
/*      */         
/*      */         public void close() {}
/*      */         
/*      */         public String toString() {
/*   91 */           return Buffer.this + ".outputStream()";
/*      */         }
/*      */       };
/*      */   }
/*      */   
/*      */   public Buffer emitCompleteSegments() {
/*   97 */     return this;
/*      */   }
/*      */   
/*      */   public BufferedSink emit() {
/*  101 */     return this;
/*      */   }
/*      */   
/*      */   public boolean exhausted() {
/*  105 */     return (this.size == 0L);
/*      */   }
/*      */   
/*      */   public void require(long byteCount) throws EOFException {
/*  109 */     if (this.size < byteCount) throw new EOFException(); 
/*      */   }
/*      */   
/*      */   public boolean request(long byteCount) {
/*  113 */     return (this.size >= byteCount);
/*      */   }
/*      */   
/*      */   public InputStream inputStream() {
/*  117 */     return new InputStream() {
/*      */         public int read() {
/*  119 */           if (Buffer.this.size > 0L) return Buffer.this.readByte() & 0xFF; 
/*  120 */           return -1;
/*      */         }
/*      */         
/*      */         public int read(byte[] sink, int offset, int byteCount) {
/*  124 */           return Buffer.this.read(sink, offset, byteCount);
/*      */         }
/*      */         
/*      */         public int available() {
/*  128 */           return (int)Math.min(Buffer.this.size, 2147483647L);
/*      */         }
/*      */ 
/*      */         
/*      */         public void close() {}
/*      */         
/*      */         public String toString() {
/*  135 */           return Buffer.this + ".inputStream()";
/*      */         }
/*      */       };
/*      */   }
/*      */ 
/*      */   
/*      */   public final Buffer copyTo(OutputStream out) throws IOException {
/*  142 */     return copyTo(out, 0L, this.size);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public final Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
/*  150 */     if (out == null) throw new IllegalArgumentException("out == null"); 
/*  151 */     Util.checkOffsetAndCount(this.size, offset, byteCount);
/*  152 */     if (byteCount == 0L) return this;
/*      */ 
/*      */     
/*  155 */     Segment s = this.head;
/*  156 */     for (; offset >= (s.limit - s.pos); s = s.next) {
/*  157 */       offset -= (s.limit - s.pos);
/*      */     }
/*      */ 
/*      */     
/*  161 */     for (; byteCount > 0L; s = s.next) {
/*  162 */       int pos = (int)(s.pos + offset);
/*  163 */       int toCopy = (int)Math.min((s.limit - pos), byteCount);
/*  164 */       out.write(s.data, pos, toCopy);
/*  165 */       byteCount -= toCopy;
/*  166 */       offset = 0L;
/*      */     } 
/*      */     
/*  169 */     return this;
/*      */   }
/*      */ 
/*      */   
/*      */   public final Buffer copyTo(Buffer out, long offset, long byteCount) {
/*  174 */     if (out == null) throw new IllegalArgumentException("out == null"); 
/*  175 */     Util.checkOffsetAndCount(this.size, offset, byteCount);
/*  176 */     if (byteCount == 0L) return this;
/*      */     
/*  178 */     out.size += byteCount;
/*      */ 
/*      */     
/*  181 */     Segment s = this.head;
/*  182 */     for (; offset >= (s.limit - s.pos); s = s.next) {
/*  183 */       offset -= (s.limit - s.pos);
/*      */     }
/*      */ 
/*      */     
/*  187 */     for (; byteCount > 0L; s = s.next) {
/*  188 */       Segment copy = s.sharedCopy();
/*  189 */       copy.pos = (int)(copy.pos + offset);
/*  190 */       copy.limit = Math.min(copy.pos + (int)byteCount, copy.limit);
/*  191 */       if (out.head == null) {
/*  192 */         out.head = copy.next = copy.prev = copy;
/*      */       } else {
/*  194 */         out.head.prev.push(copy);
/*      */       } 
/*  196 */       byteCount -= (copy.limit - copy.pos);
/*  197 */       offset = 0L;
/*      */     } 
/*      */     
/*  200 */     return this;
/*      */   }
/*      */ 
/*      */   
/*      */   public final Buffer writeTo(OutputStream out) throws IOException {
/*  205 */     return writeTo(out, this.size);
/*      */   }
/*      */ 
/*      */   
/*      */   public final Buffer writeTo(OutputStream out, long byteCount) throws IOException {
/*  210 */     if (out == null) throw new IllegalArgumentException("out == null"); 
/*  211 */     Util.checkOffsetAndCount(this.size, 0L, byteCount);
/*      */     
/*  213 */     Segment s = this.head;
/*  214 */     while (byteCount > 0L) {
/*  215 */       int toCopy = (int)Math.min(byteCount, (s.limit - s.pos));
/*  216 */       out.write(s.data, s.pos, toCopy);
/*      */       
/*  218 */       s.pos += toCopy;
/*  219 */       this.size -= toCopy;
/*  220 */       byteCount -= toCopy;
/*      */       
/*  222 */       if (s.pos == s.limit) {
/*  223 */         Segment toRecycle = s;
/*  224 */         this.head = s = toRecycle.pop();
/*  225 */         SegmentPool.recycle(toRecycle);
/*      */       } 
/*      */     } 
/*      */     
/*  229 */     return this;
/*      */   }
/*      */ 
/*      */   
/*      */   public final Buffer readFrom(InputStream in) throws IOException {
/*  234 */     readFrom(in, Long.MAX_VALUE, true);
/*  235 */     return this;
/*      */   }
/*      */ 
/*      */   
/*      */   public final Buffer readFrom(InputStream in, long byteCount) throws IOException {
/*  240 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/*  241 */     readFrom(in, byteCount, false);
/*  242 */     return this;
/*      */   }
/*      */   
/*      */   private void readFrom(InputStream in, long byteCount, boolean forever) throws IOException {
/*  246 */     if (in == null) throw new IllegalArgumentException("in == null"); 
/*  247 */     while (byteCount > 0L || forever) {
/*  248 */       Segment tail = writableSegment(1);
/*  249 */       int maxToCopy = (int)Math.min(byteCount, (8192 - tail.limit));
/*  250 */       int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
/*  251 */       if (bytesRead == -1) {
/*  252 */         if (forever)
/*  253 */           return;  throw new EOFException();
/*      */       } 
/*  255 */       tail.limit += bytesRead;
/*  256 */       this.size += bytesRead;
/*  257 */       byteCount -= bytesRead;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public final long completeSegmentByteCount() {
/*  267 */     long result = this.size;
/*  268 */     if (result == 0L) return 0L;
/*      */ 
/*      */     
/*  271 */     Segment tail = this.head.prev;
/*  272 */     if (tail.limit < 8192 && tail.owner) {
/*  273 */       result -= (tail.limit - tail.pos);
/*      */     }
/*      */     
/*  276 */     return result;
/*      */   }
/*      */   
/*      */   public byte readByte() {
/*  280 */     if (this.size == 0L) throw new IllegalStateException("size == 0");
/*      */     
/*  282 */     Segment segment = this.head;
/*  283 */     int pos = segment.pos;
/*  284 */     int limit = segment.limit;
/*      */     
/*  286 */     byte[] data = segment.data;
/*  287 */     byte b = data[pos++];
/*  288 */     this.size--;
/*      */     
/*  290 */     if (pos == limit) {
/*  291 */       this.head = segment.pop();
/*  292 */       SegmentPool.recycle(segment);
/*      */     } else {
/*  294 */       segment.pos = pos;
/*      */     } 
/*      */     
/*  297 */     return b;
/*      */   }
/*      */ 
/*      */   
/*      */   public final byte getByte(long pos) {
/*  302 */     Util.checkOffsetAndCount(this.size, pos, 1L);
/*  303 */     if (this.size - pos > pos) {
/*  304 */       for (Segment segment = this.head;; segment = segment.next) {
/*  305 */         int segmentByteCount = segment.limit - segment.pos;
/*  306 */         if (pos < segmentByteCount) return segment.data[segment.pos + (int)pos]; 
/*  307 */         pos -= segmentByteCount;
/*      */       } 
/*      */     }
/*  310 */     pos -= this.size;
/*  311 */     for (Segment s = this.head.prev;; s = s.prev) {
/*  312 */       pos += (s.limit - s.pos);
/*  313 */       if (pos >= 0L) return s.data[s.pos + (int)pos];
/*      */     
/*      */     } 
/*      */   }
/*      */   
/*      */   public short readShort() {
/*  319 */     if (this.size < 2L) throw new IllegalStateException("size < 2: " + this.size);
/*      */     
/*  321 */     Segment segment = this.head;
/*  322 */     int pos = segment.pos;
/*  323 */     int limit = segment.limit;
/*      */ 
/*      */     
/*  326 */     if (limit - pos < 2) {
/*      */       
/*  328 */       int i = (readByte() & 0xFF) << 8 | readByte() & 0xFF;
/*  329 */       return (short)i;
/*      */     } 
/*      */     
/*  332 */     byte[] data = segment.data;
/*  333 */     int s = (data[pos++] & 0xFF) << 8 | data[pos++] & 0xFF;
/*      */     
/*  335 */     this.size -= 2L;
/*      */     
/*  337 */     if (pos == limit) {
/*  338 */       this.head = segment.pop();
/*  339 */       SegmentPool.recycle(segment);
/*      */     } else {
/*  341 */       segment.pos = pos;
/*      */     } 
/*      */     
/*  344 */     return (short)s;
/*      */   }
/*      */   
/*      */   public int readInt() {
/*  348 */     if (this.size < 4L) throw new IllegalStateException("size < 4: " + this.size);
/*      */     
/*  350 */     Segment segment = this.head;
/*  351 */     int pos = segment.pos;
/*  352 */     int limit = segment.limit;
/*      */ 
/*      */     
/*  355 */     if (limit - pos < 4) {
/*  356 */       return (readByte() & 0xFF) << 24 | (
/*  357 */         readByte() & 0xFF) << 16 | (
/*  358 */         readByte() & 0xFF) << 8 | 
/*  359 */         readByte() & 0xFF;
/*      */     }
/*      */     
/*  362 */     byte[] data = segment.data;
/*  363 */     int i = (data[pos++] & 0xFF) << 24 | (data[pos++] & 0xFF) << 16 | (data[pos++] & 0xFF) << 8 | data[pos++] & 0xFF;
/*      */ 
/*      */ 
/*      */     
/*  367 */     this.size -= 4L;
/*      */     
/*  369 */     if (pos == limit) {
/*  370 */       this.head = segment.pop();
/*  371 */       SegmentPool.recycle(segment);
/*      */     } else {
/*  373 */       segment.pos = pos;
/*      */     } 
/*      */     
/*  376 */     return i;
/*      */   }
/*      */   
/*      */   public long readLong() {
/*  380 */     if (this.size < 8L) throw new IllegalStateException("size < 8: " + this.size);
/*      */     
/*  382 */     Segment segment = this.head;
/*  383 */     int pos = segment.pos;
/*  384 */     int limit = segment.limit;
/*      */ 
/*      */     
/*  387 */     if (limit - pos < 8) {
/*  388 */       return (readInt() & 0xFFFFFFFFL) << 32L | 
/*  389 */         readInt() & 0xFFFFFFFFL;
/*      */     }
/*      */     
/*  392 */     byte[] data = segment.data;
/*  393 */     long v = (data[pos++] & 0xFFL) << 56L | (data[pos++] & 0xFFL) << 48L | (data[pos++] & 0xFFL) << 40L | (data[pos++] & 0xFFL) << 32L | (data[pos++] & 0xFFL) << 24L | (data[pos++] & 0xFFL) << 16L | (data[pos++] & 0xFFL) << 8L | data[pos++] & 0xFFL;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  401 */     this.size -= 8L;
/*      */     
/*  403 */     if (pos == limit) {
/*  404 */       this.head = segment.pop();
/*  405 */       SegmentPool.recycle(segment);
/*      */     } else {
/*  407 */       segment.pos = pos;
/*      */     } 
/*      */     
/*  410 */     return v;
/*      */   }
/*      */   
/*      */   public short readShortLe() {
/*  414 */     return Util.reverseBytesShort(readShort());
/*      */   }
/*      */   
/*      */   public int readIntLe() {
/*  418 */     return Util.reverseBytesInt(readInt());
/*      */   }
/*      */   
/*      */   public long readLongLe() {
/*  422 */     return Util.reverseBytesLong(readLong());
/*      */   }
/*      */   
/*      */   public long readDecimalLong() {
/*  426 */     if (this.size == 0L) throw new IllegalStateException("size == 0");
/*      */ 
/*      */     
/*  429 */     long value = 0L;
/*  430 */     int seen = 0;
/*  431 */     boolean negative = false;
/*  432 */     boolean done = false;
/*      */     
/*  434 */     long overflowZone = -922337203685477580L;
/*  435 */     long overflowDigit = -7L;
/*      */     
/*      */     do {
/*  438 */       Segment segment = this.head;
/*      */       
/*  440 */       byte[] data = segment.data;
/*  441 */       int pos = segment.pos;
/*  442 */       int limit = segment.limit;
/*      */       
/*  444 */       for (; pos < limit; pos++, seen++) {
/*  445 */         byte b = data[pos];
/*  446 */         if (b >= 48 && b <= 57) {
/*  447 */           int digit = 48 - b;
/*      */ 
/*      */           
/*  450 */           if (value < overflowZone || (value == overflowZone && digit < overflowDigit)) {
/*  451 */             Buffer buffer = (new Buffer()).writeDecimalLong(value).writeByte(b);
/*  452 */             if (!negative) buffer.readByte(); 
/*  453 */             throw new NumberFormatException("Number too large: " + buffer.readUtf8());
/*      */           } 
/*  455 */           value *= 10L;
/*  456 */           value += digit;
/*  457 */         } else if (b == 45 && seen == 0) {
/*  458 */           negative = true;
/*  459 */           overflowDigit--;
/*      */         } else {
/*  461 */           if (seen == 0) {
/*  462 */             throw new NumberFormatException("Expected leading [0-9] or '-' character but was 0x" + 
/*  463 */                 Integer.toHexString(b));
/*      */           }
/*      */           
/*  466 */           done = true;
/*      */           
/*      */           break;
/*      */         } 
/*      */       } 
/*  471 */       if (pos == limit) {
/*  472 */         this.head = segment.pop();
/*  473 */         SegmentPool.recycle(segment);
/*      */       } else {
/*  475 */         segment.pos = pos;
/*      */       } 
/*  477 */     } while (!done && this.head != null);
/*      */     
/*  479 */     this.size -= seen;
/*  480 */     return negative ? value : -value;
/*      */   }
/*      */   
/*      */   public long readHexadecimalUnsignedLong() {
/*  484 */     if (this.size == 0L) throw new IllegalStateException("size == 0");
/*      */     
/*  486 */     long value = 0L;
/*  487 */     int seen = 0;
/*  488 */     boolean done = false;
/*      */     
/*      */     do {
/*  491 */       Segment segment = this.head;
/*      */       
/*  493 */       byte[] data = segment.data;
/*  494 */       int pos = segment.pos;
/*  495 */       int limit = segment.limit;
/*      */       
/*  497 */       for (; pos < limit; pos++, seen++) {
/*      */         int digit;
/*      */         
/*  500 */         byte b = data[pos];
/*  501 */         if (b >= 48 && b <= 57) {
/*  502 */           digit = b - 48;
/*  503 */         } else if (b >= 97 && b <= 102) {
/*  504 */           digit = b - 97 + 10;
/*  505 */         } else if (b >= 65 && b <= 70) {
/*  506 */           digit = b - 65 + 10;
/*      */         } else {
/*  508 */           if (seen == 0) {
/*  509 */             throw new NumberFormatException("Expected leading [0-9a-fA-F] character but was 0x" + 
/*  510 */                 Integer.toHexString(b));
/*      */           }
/*      */           
/*  513 */           done = true;
/*      */           
/*      */           break;
/*      */         } 
/*      */         
/*  518 */         if ((value & 0xF000000000000000L) != 0L) {
/*  519 */           Buffer buffer = (new Buffer()).writeHexadecimalUnsignedLong(value).writeByte(b);
/*  520 */           throw new NumberFormatException("Number too large: " + buffer.readUtf8());
/*      */         } 
/*      */         
/*  523 */         value <<= 4L;
/*  524 */         value |= digit;
/*      */       } 
/*      */       
/*  527 */       if (pos == limit) {
/*  528 */         this.head = segment.pop();
/*  529 */         SegmentPool.recycle(segment);
/*      */       } else {
/*  531 */         segment.pos = pos;
/*      */       } 
/*  533 */     } while (!done && this.head != null);
/*      */     
/*  535 */     this.size -= seen;
/*  536 */     return value;
/*      */   }
/*      */   
/*      */   public ByteString readByteString() {
/*  540 */     return new ByteString(readByteArray());
/*      */   }
/*      */   
/*      */   public ByteString readByteString(long byteCount) throws EOFException {
/*  544 */     return new ByteString(readByteArray(byteCount));
/*      */   }
/*      */   
/*      */   public int select(Options options) {
/*  548 */     int index = selectPrefix(options, false);
/*  549 */     if (index == -1) return -1;
/*      */ 
/*      */     
/*  552 */     int selectedSize = options.byteStrings[index].size();
/*      */     try {
/*  554 */       skip(selectedSize);
/*  555 */     } catch (EOFException e) {
/*  556 */       throw new AssertionError();
/*      */     } 
/*  558 */     return index;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   int selectPrefix(Options options, boolean selectTruncated) {
/*  574 */     Segment head = this.head;
/*  575 */     if (head == null) {
/*  576 */       if (selectTruncated) return -2; 
/*  577 */       return options.indexOf(ByteString.EMPTY);
/*      */     } 
/*      */     
/*  580 */     Segment s = head;
/*  581 */     byte[] data = head.data;
/*  582 */     int pos = head.pos;
/*  583 */     int limit = head.limit;
/*      */     
/*  585 */     int[] trie = options.trie;
/*  586 */     int triePos = 0;
/*      */     
/*  588 */     int prefixIndex = -1;
/*      */ 
/*      */     
/*      */     while (true) {
/*  592 */       int scanOrSelect = trie[triePos++];
/*      */       
/*  594 */       int possiblePrefixIndex = trie[triePos++];
/*  595 */       if (possiblePrefixIndex != -1) {
/*  596 */         prefixIndex = possiblePrefixIndex;
/*      */       }
/*      */ 
/*      */ 
/*      */       
/*  601 */       if (s == null)
/*      */         break; 
/*  603 */       if (scanOrSelect < 0)
/*      */       
/*  605 */       { int scanByteCount = -1 * scanOrSelect;
/*  606 */         int trieLimit = triePos + scanByteCount;
/*      */         while (true)
/*  608 */         { int i = data[pos++] & 0xFF;
/*  609 */           if (i != trie[triePos++]) return prefixIndex; 
/*  610 */           boolean scanComplete = (triePos == trieLimit);
/*      */ 
/*      */           
/*  613 */           if (pos == limit) {
/*  614 */             s = s.next;
/*  615 */             pos = s.pos;
/*  616 */             data = s.data;
/*  617 */             limit = s.limit;
/*  618 */             if (s == head) {
/*  619 */               if (!scanComplete)
/*  620 */                 break;  s = null;
/*      */             } 
/*      */           } 
/*      */           
/*  624 */           if (scanComplete)
/*  625 */           { int nextStep = trie[triePos];
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*  657 */             if (nextStep >= 0) return nextStep; 
/*  658 */             triePos = -nextStep; }  }  break; }  int selectChoiceCount = scanOrSelect; int b = data[pos++] & 0xFF; int selectLimit = triePos + selectChoiceCount; while (true) { if (triePos == selectLimit)
/*      */           return prefixIndex;  if (b == trie[triePos]) { int nextStep = trie[triePos + selectChoiceCount]; break; }  triePos++; }  if (pos == limit) { s = s.next; pos = s.pos; data = s.data; limit = s.limit; if (s == head) { s = null; continue; }
/*      */          continue; }
/*      */        continue;
/*  662 */     }  if (selectTruncated) return -2; 
/*  663 */     return prefixIndex;
/*      */   }
/*      */   
/*      */   public void readFully(Buffer sink, long byteCount) throws EOFException {
/*  667 */     if (this.size < byteCount) {
/*  668 */       sink.write(this, this.size);
/*  669 */       throw new EOFException();
/*      */     } 
/*  671 */     sink.write(this, byteCount);
/*      */   }
/*      */   
/*      */   public long readAll(Sink sink) throws IOException {
/*  675 */     long byteCount = this.size;
/*  676 */     if (byteCount > 0L) {
/*  677 */       sink.write(this, byteCount);
/*      */     }
/*  679 */     return byteCount;
/*      */   }
/*      */   
/*      */   public String readUtf8() {
/*      */     try {
/*  684 */       return readString(this.size, Util.UTF_8);
/*  685 */     } catch (EOFException e) {
/*  686 */       throw new AssertionError(e);
/*      */     } 
/*      */   }
/*      */   
/*      */   public String readUtf8(long byteCount) throws EOFException {
/*  691 */     return readString(byteCount, Util.UTF_8);
/*      */   }
/*      */   
/*      */   public String readString(Charset charset) {
/*      */     try {
/*  696 */       return readString(this.size, charset);
/*  697 */     } catch (EOFException e) {
/*  698 */       throw new AssertionError(e);
/*      */     } 
/*      */   }
/*      */   
/*      */   public String readString(long byteCount, Charset charset) throws EOFException {
/*  703 */     Util.checkOffsetAndCount(this.size, 0L, byteCount);
/*  704 */     if (charset == null) throw new IllegalArgumentException("charset == null"); 
/*  705 */     if (byteCount > 2147483647L) {
/*  706 */       throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
/*      */     }
/*  708 */     if (byteCount == 0L) return "";
/*      */     
/*  710 */     Segment s = this.head;
/*  711 */     if (s.pos + byteCount > s.limit)
/*      */     {
/*  713 */       return new String(readByteArray(byteCount), charset);
/*      */     }
/*      */     
/*  716 */     String result = new String(s.data, s.pos, (int)byteCount, charset);
/*  717 */     s.pos = (int)(s.pos + byteCount);
/*  718 */     this.size -= byteCount;
/*      */     
/*  720 */     if (s.pos == s.limit) {
/*  721 */       this.head = s.pop();
/*  722 */       SegmentPool.recycle(s);
/*      */     } 
/*      */     
/*  725 */     return result;
/*      */   }
/*      */   @Nullable
/*      */   public String readUtf8Line() throws EOFException {
/*  729 */     long newline = indexOf((byte)10);
/*      */     
/*  731 */     if (newline == -1L) {
/*  732 */       return (this.size != 0L) ? readUtf8(this.size) : null;
/*      */     }
/*      */     
/*  735 */     return readUtf8Line(newline);
/*      */   }
/*      */   
/*      */   public String readUtf8LineStrict() throws EOFException {
/*  739 */     return readUtf8LineStrict(Long.MAX_VALUE);
/*      */   }
/*      */   
/*      */   public String readUtf8LineStrict(long limit) throws EOFException {
/*  743 */     if (limit < 0L) throw new IllegalArgumentException("limit < 0: " + limit); 
/*  744 */     long scanLength = (limit == Long.MAX_VALUE) ? Long.MAX_VALUE : (limit + 1L);
/*  745 */     long newline = indexOf((byte)10, 0L, scanLength);
/*  746 */     if (newline != -1L) return readUtf8Line(newline); 
/*  747 */     if (scanLength < size() && 
/*  748 */       getByte(scanLength - 1L) == 13 && getByte(scanLength) == 10) {
/*  749 */       return readUtf8Line(scanLength);
/*      */     }
/*  751 */     Buffer data = new Buffer();
/*  752 */     copyTo(data, 0L, Math.min(32L, size()));
/*  753 */     throw new EOFException("\\n not found: limit=" + Math.min(size(), limit) + " content=" + data
/*  754 */         .readByteString().hex() + '…');
/*      */   }
/*      */   
/*      */   String readUtf8Line(long newline) throws EOFException {
/*  758 */     if (newline > 0L && getByte(newline - 1L) == 13) {
/*      */       
/*  760 */       String str = readUtf8(newline - 1L);
/*  761 */       skip(2L);
/*  762 */       return str;
/*      */     } 
/*      */ 
/*      */     
/*  766 */     String result = readUtf8(newline);
/*  767 */     skip(1L);
/*  768 */     return result;
/*      */   }
/*      */   
/*      */   public int readUtf8CodePoint() throws EOFException {
/*      */     int codePoint, byteCount, min;
/*  773 */     if (this.size == 0L) throw new EOFException();
/*      */     
/*  775 */     byte b0 = getByte(0L);
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  780 */     if ((b0 & 0x80) == 0) {
/*      */       
/*  782 */       codePoint = b0 & Byte.MAX_VALUE;
/*  783 */       byteCount = 1;
/*  784 */       min = 0;
/*      */     }
/*  786 */     else if ((b0 & 0xE0) == 192) {
/*      */       
/*  788 */       codePoint = b0 & 0x1F;
/*  789 */       byteCount = 2;
/*  790 */       min = 128;
/*      */     }
/*  792 */     else if ((b0 & 0xF0) == 224) {
/*      */       
/*  794 */       codePoint = b0 & 0xF;
/*  795 */       byteCount = 3;
/*  796 */       min = 2048;
/*      */     }
/*  798 */     else if ((b0 & 0xF8) == 240) {
/*      */       
/*  800 */       codePoint = b0 & 0x7;
/*  801 */       byteCount = 4;
/*  802 */       min = 65536;
/*      */     }
/*      */     else {
/*      */       
/*  806 */       skip(1L);
/*  807 */       return 65533;
/*      */     } 
/*      */     
/*  810 */     if (this.size < byteCount) {
/*  811 */       throw new EOFException("size < " + byteCount + ": " + this.size + " (to read code point prefixed 0x" + 
/*  812 */           Integer.toHexString(b0) + ")");
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  818 */     for (int i = 1; i < byteCount; i++) {
/*  819 */       byte b = getByte(i);
/*  820 */       if ((b & 0xC0) == 128) {
/*      */         
/*  822 */         codePoint <<= 6;
/*  823 */         codePoint |= b & 0x3F;
/*      */       } else {
/*  825 */         skip(i);
/*  826 */         return 65533;
/*      */       } 
/*      */     } 
/*      */     
/*  830 */     skip(byteCount);
/*      */     
/*  832 */     if (codePoint > 1114111) {
/*  833 */       return 65533;
/*      */     }
/*      */     
/*  836 */     if (codePoint >= 55296 && codePoint <= 57343) {
/*  837 */       return 65533;
/*      */     }
/*      */     
/*  840 */     if (codePoint < min) {
/*  841 */       return 65533;
/*      */     }
/*      */     
/*  844 */     return codePoint;
/*      */   }
/*      */   
/*      */   public byte[] readByteArray() {
/*      */     try {
/*  849 */       return readByteArray(this.size);
/*  850 */     } catch (EOFException e) {
/*  851 */       throw new AssertionError(e);
/*      */     } 
/*      */   }
/*      */   
/*      */   public byte[] readByteArray(long byteCount) throws EOFException {
/*  856 */     Util.checkOffsetAndCount(this.size, 0L, byteCount);
/*  857 */     if (byteCount > 2147483647L) {
/*  858 */       throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
/*      */     }
/*      */     
/*  861 */     byte[] result = new byte[(int)byteCount];
/*  862 */     readFully(result);
/*  863 */     return result;
/*      */   }
/*      */   
/*      */   public int read(byte[] sink) {
/*  867 */     return read(sink, 0, sink.length);
/*      */   }
/*      */   
/*      */   public void readFully(byte[] sink) throws EOFException {
/*  871 */     int offset = 0;
/*  872 */     while (offset < sink.length) {
/*  873 */       int read = read(sink, offset, sink.length - offset);
/*  874 */       if (read == -1) throw new EOFException(); 
/*  875 */       offset += read;
/*      */     } 
/*      */   }
/*      */   
/*      */   public int read(byte[] sink, int offset, int byteCount) {
/*  880 */     Util.checkOffsetAndCount(sink.length, offset, byteCount);
/*      */     
/*  882 */     Segment s = this.head;
/*  883 */     if (s == null) return -1; 
/*  884 */     int toCopy = Math.min(byteCount, s.limit - s.pos);
/*  885 */     System.arraycopy(s.data, s.pos, sink, offset, toCopy);
/*      */     
/*  887 */     s.pos += toCopy;
/*  888 */     this.size -= toCopy;
/*      */     
/*  890 */     if (s.pos == s.limit) {
/*  891 */       this.head = s.pop();
/*  892 */       SegmentPool.recycle(s);
/*      */     } 
/*      */     
/*  895 */     return toCopy;
/*      */   }
/*      */   
/*      */   public int read(ByteBuffer sink) throws IOException {
/*  899 */     Segment s = this.head;
/*  900 */     if (s == null) return -1;
/*      */     
/*  902 */     int toCopy = Math.min(sink.remaining(), s.limit - s.pos);
/*  903 */     sink.put(s.data, s.pos, toCopy);
/*      */     
/*  905 */     s.pos += toCopy;
/*  906 */     this.size -= toCopy;
/*      */     
/*  908 */     if (s.pos == s.limit) {
/*  909 */       this.head = s.pop();
/*  910 */       SegmentPool.recycle(s);
/*      */     } 
/*      */     
/*  913 */     return toCopy;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public final void clear() {
/*      */     try {
/*  922 */       skip(this.size);
/*  923 */     } catch (EOFException e) {
/*  924 */       throw new AssertionError(e);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public void skip(long byteCount) throws EOFException {
/*  930 */     while (byteCount > 0L) {
/*  931 */       if (this.head == null) throw new EOFException();
/*      */       
/*  933 */       int toSkip = (int)Math.min(byteCount, (this.head.limit - this.head.pos));
/*  934 */       this.size -= toSkip;
/*  935 */       byteCount -= toSkip;
/*  936 */       this.head.pos += toSkip;
/*      */       
/*  938 */       if (this.head.pos == this.head.limit) {
/*  939 */         Segment toRecycle = this.head;
/*  940 */         this.head = toRecycle.pop();
/*  941 */         SegmentPool.recycle(toRecycle);
/*      */       } 
/*      */     } 
/*      */   }
/*      */   
/*      */   public Buffer write(ByteString byteString) {
/*  947 */     if (byteString == null) throw new IllegalArgumentException("byteString == null"); 
/*  948 */     byteString.write(this);
/*  949 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeUtf8(String string) {
/*  953 */     return writeUtf8(string, 0, string.length());
/*      */   }
/*      */   
/*      */   public Buffer writeUtf8(String string, int beginIndex, int endIndex) {
/*  957 */     if (string == null) throw new IllegalArgumentException("string == null"); 
/*  958 */     if (beginIndex < 0) throw new IllegalArgumentException("beginIndex < 0: " + beginIndex); 
/*  959 */     if (endIndex < beginIndex) {
/*  960 */       throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
/*      */     }
/*  962 */     if (endIndex > string.length()) {
/*  963 */       throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string
/*  964 */           .length());
/*      */     }
/*      */ 
/*      */     
/*  968 */     for (int i = beginIndex; i < endIndex; ) {
/*  969 */       int c = string.charAt(i);
/*      */       
/*  971 */       if (c < 128) {
/*  972 */         Segment tail = writableSegment(1);
/*  973 */         byte[] data = tail.data;
/*  974 */         int segmentOffset = tail.limit - i;
/*  975 */         int runLimit = Math.min(endIndex, 8192 - segmentOffset);
/*      */ 
/*      */         
/*  978 */         data[segmentOffset + i++] = (byte)c;
/*      */ 
/*      */ 
/*      */         
/*  982 */         while (i < runLimit) {
/*  983 */           c = string.charAt(i);
/*  984 */           if (c >= 128)
/*  985 */             break;  data[segmentOffset + i++] = (byte)c;
/*      */         } 
/*      */         
/*  988 */         int runSize = i + segmentOffset - tail.limit;
/*  989 */         tail.limit += runSize;
/*  990 */         this.size += runSize; continue;
/*      */       } 
/*  992 */       if (c < 2048) {
/*      */         
/*  994 */         writeByte(c >> 6 | 0xC0);
/*  995 */         writeByte(c & 0x3F | 0x80);
/*  996 */         i++; continue;
/*      */       } 
/*  998 */       if (c < 55296 || c > 57343) {
/*      */         
/* 1000 */         writeByte(c >> 12 | 0xE0);
/* 1001 */         writeByte(c >> 6 & 0x3F | 0x80);
/* 1002 */         writeByte(c & 0x3F | 0x80);
/* 1003 */         i++;
/*      */         
/*      */         continue;
/*      */       } 
/*      */       
/* 1008 */       int low = (i + 1 < endIndex) ? string.charAt(i + 1) : 0;
/* 1009 */       if (c > 56319 || low < 56320 || low > 57343) {
/* 1010 */         writeByte(63);
/* 1011 */         i++;
/*      */ 
/*      */         
/*      */         continue;
/*      */       } 
/*      */ 
/*      */       
/* 1018 */       int codePoint = 65536 + ((c & 0xFFFF27FF) << 10 | low & 0xFFFF23FF);
/*      */ 
/*      */       
/* 1021 */       writeByte(codePoint >> 18 | 0xF0);
/* 1022 */       writeByte(codePoint >> 12 & 0x3F | 0x80);
/* 1023 */       writeByte(codePoint >> 6 & 0x3F | 0x80);
/* 1024 */       writeByte(codePoint & 0x3F | 0x80);
/* 1025 */       i += 2;
/*      */     } 
/*      */ 
/*      */     
/* 1029 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeUtf8CodePoint(int codePoint) {
/* 1033 */     if (codePoint < 128) {
/*      */       
/* 1035 */       writeByte(codePoint);
/*      */     }
/* 1037 */     else if (codePoint < 2048) {
/*      */       
/* 1039 */       writeByte(codePoint >> 6 | 0xC0);
/* 1040 */       writeByte(codePoint & 0x3F | 0x80);
/*      */     }
/* 1042 */     else if (codePoint < 65536) {
/* 1043 */       if (codePoint >= 55296 && codePoint <= 57343) {
/*      */         
/* 1045 */         writeByte(63);
/*      */       } else {
/*      */         
/* 1048 */         writeByte(codePoint >> 12 | 0xE0);
/* 1049 */         writeByte(codePoint >> 6 & 0x3F | 0x80);
/* 1050 */         writeByte(codePoint & 0x3F | 0x80);
/*      */       }
/*      */     
/* 1053 */     } else if (codePoint <= 1114111) {
/*      */       
/* 1055 */       writeByte(codePoint >> 18 | 0xF0);
/* 1056 */       writeByte(codePoint >> 12 & 0x3F | 0x80);
/* 1057 */       writeByte(codePoint >> 6 & 0x3F | 0x80);
/* 1058 */       writeByte(codePoint & 0x3F | 0x80);
/*      */     } else {
/*      */       
/* 1061 */       throw new IllegalArgumentException("Unexpected code point: " + 
/* 1062 */           Integer.toHexString(codePoint));
/*      */     } 
/*      */     
/* 1065 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeString(String string, Charset charset) {
/* 1069 */     return writeString(string, 0, string.length(), charset);
/*      */   }
/*      */ 
/*      */   
/*      */   public Buffer writeString(String string, int beginIndex, int endIndex, Charset charset) {
/* 1074 */     if (string == null) throw new IllegalArgumentException("string == null"); 
/* 1075 */     if (beginIndex < 0) throw new IllegalAccessError("beginIndex < 0: " + beginIndex); 
/* 1076 */     if (endIndex < beginIndex) {
/* 1077 */       throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
/*      */     }
/* 1079 */     if (endIndex > string.length()) {
/* 1080 */       throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string
/* 1081 */           .length());
/*      */     }
/* 1083 */     if (charset == null) throw new IllegalArgumentException("charset == null"); 
/* 1084 */     if (charset.equals(Util.UTF_8)) return writeUtf8(string, beginIndex, endIndex); 
/* 1085 */     byte[] data = string.substring(beginIndex, endIndex).getBytes(charset);
/* 1086 */     return write(data, 0, data.length);
/*      */   }
/*      */   
/*      */   public Buffer write(byte[] source) {
/* 1090 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/* 1091 */     return write(source, 0, source.length);
/*      */   }
/*      */   
/*      */   public Buffer write(byte[] source, int offset, int byteCount) {
/* 1095 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/* 1096 */     Util.checkOffsetAndCount(source.length, offset, byteCount);
/*      */     
/* 1098 */     int limit = offset + byteCount;
/* 1099 */     while (offset < limit) {
/* 1100 */       Segment tail = writableSegment(1);
/*      */       
/* 1102 */       int toCopy = Math.min(limit - offset, 8192 - tail.limit);
/* 1103 */       System.arraycopy(source, offset, tail.data, tail.limit, toCopy);
/*      */       
/* 1105 */       offset += toCopy;
/* 1106 */       tail.limit += toCopy;
/*      */     } 
/*      */     
/* 1109 */     this.size += byteCount;
/* 1110 */     return this;
/*      */   }
/*      */   
/*      */   public int write(ByteBuffer source) throws IOException {
/* 1114 */     if (source == null) throw new IllegalArgumentException("source == null");
/*      */     
/* 1116 */     int byteCount = source.remaining();
/* 1117 */     int remaining = byteCount;
/* 1118 */     while (remaining > 0) {
/* 1119 */       Segment tail = writableSegment(1);
/*      */       
/* 1121 */       int toCopy = Math.min(remaining, 8192 - tail.limit);
/* 1122 */       source.get(tail.data, tail.limit, toCopy);
/*      */       
/* 1124 */       remaining -= toCopy;
/* 1125 */       tail.limit += toCopy;
/*      */     } 
/*      */     
/* 1128 */     this.size += byteCount;
/* 1129 */     return byteCount;
/*      */   }
/*      */   
/*      */   public long writeAll(Source source) throws IOException {
/* 1133 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/* 1134 */     long totalBytesRead = 0L; long readCount;
/* 1135 */     while ((readCount = source.read(this, 8192L)) != -1L) {
/* 1136 */       totalBytesRead += readCount;
/*      */     }
/* 1138 */     return totalBytesRead;
/*      */   }
/*      */   
/*      */   public BufferedSink write(Source source, long byteCount) throws IOException {
/* 1142 */     while (byteCount > 0L) {
/* 1143 */       long read = source.read(this, byteCount);
/* 1144 */       if (read == -1L) throw new EOFException(); 
/* 1145 */       byteCount -= read;
/*      */     } 
/* 1147 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeByte(int b) {
/* 1151 */     Segment tail = writableSegment(1);
/* 1152 */     tail.data[tail.limit++] = (byte)b;
/* 1153 */     this.size++;
/* 1154 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeShort(int s) {
/* 1158 */     Segment tail = writableSegment(2);
/* 1159 */     byte[] data = tail.data;
/* 1160 */     int limit = tail.limit;
/* 1161 */     data[limit++] = (byte)(s >>> 8 & 0xFF);
/* 1162 */     data[limit++] = (byte)(s & 0xFF);
/* 1163 */     tail.limit = limit;
/* 1164 */     this.size += 2L;
/* 1165 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeShortLe(int s) {
/* 1169 */     return writeShort(Util.reverseBytesShort((short)s));
/*      */   }
/*      */   
/*      */   public Buffer writeInt(int i) {
/* 1173 */     Segment tail = writableSegment(4);
/* 1174 */     byte[] data = tail.data;
/* 1175 */     int limit = tail.limit;
/* 1176 */     data[limit++] = (byte)(i >>> 24 & 0xFF);
/* 1177 */     data[limit++] = (byte)(i >>> 16 & 0xFF);
/* 1178 */     data[limit++] = (byte)(i >>> 8 & 0xFF);
/* 1179 */     data[limit++] = (byte)(i & 0xFF);
/* 1180 */     tail.limit = limit;
/* 1181 */     this.size += 4L;
/* 1182 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeIntLe(int i) {
/* 1186 */     return writeInt(Util.reverseBytesInt(i));
/*      */   }
/*      */   
/*      */   public Buffer writeLong(long v) {
/* 1190 */     Segment tail = writableSegment(8);
/* 1191 */     byte[] data = tail.data;
/* 1192 */     int limit = tail.limit;
/* 1193 */     data[limit++] = (byte)(int)(v >>> 56L & 0xFFL);
/* 1194 */     data[limit++] = (byte)(int)(v >>> 48L & 0xFFL);
/* 1195 */     data[limit++] = (byte)(int)(v >>> 40L & 0xFFL);
/* 1196 */     data[limit++] = (byte)(int)(v >>> 32L & 0xFFL);
/* 1197 */     data[limit++] = (byte)(int)(v >>> 24L & 0xFFL);
/* 1198 */     data[limit++] = (byte)(int)(v >>> 16L & 0xFFL);
/* 1199 */     data[limit++] = (byte)(int)(v >>> 8L & 0xFFL);
/* 1200 */     data[limit++] = (byte)(int)(v & 0xFFL);
/* 1201 */     tail.limit = limit;
/* 1202 */     this.size += 8L;
/* 1203 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeLongLe(long v) {
/* 1207 */     return writeLong(Util.reverseBytesLong(v));
/*      */   }
/*      */   
/*      */   public Buffer writeDecimalLong(long v) {
/* 1211 */     if (v == 0L)
/*      */     {
/* 1213 */       return writeByte(48);
/*      */     }
/*      */     
/* 1216 */     boolean negative = false;
/* 1217 */     if (v < 0L) {
/* 1218 */       v = -v;
/* 1219 */       if (v < 0L) {
/* 1220 */         return writeUtf8("-9223372036854775808");
/*      */       }
/* 1222 */       negative = true;
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1244 */     int width = (v < 100000000L) ? ((v < 10000L) ? ((v < 100L) ? ((v < 10L) ? 1 : 2) : ((v < 1000L) ? 3 : 4)) : ((v < 1000000L) ? ((v < 100000L) ? 5 : 6) : ((v < 10000000L) ? 7 : 8))) : ((v < 1000000000000L) ? ((v < 10000000000L) ? ((v < 1000000000L) ? 9 : 10) : ((v < 100000000000L) ? 11 : 12)) : ((v < 1000000000000000L) ? ((v < 10000000000000L) ? 13 : ((v < 100000000000000L) ? 14 : 15)) : ((v < 100000000000000000L) ? ((v < 10000000000000000L) ? 16 : 17) : ((v < 1000000000000000000L) ? 18 : 19))));
/* 1245 */     if (negative) {
/* 1246 */       width++;
/*      */     }
/*      */     
/* 1249 */     Segment tail = writableSegment(width);
/* 1250 */     byte[] data = tail.data;
/* 1251 */     int pos = tail.limit + width;
/* 1252 */     while (v != 0L) {
/* 1253 */       int digit = (int)(v % 10L);
/* 1254 */       data[--pos] = DIGITS[digit];
/* 1255 */       v /= 10L;
/*      */     } 
/* 1257 */     if (negative) {
/* 1258 */       data[--pos] = 45;
/*      */     }
/*      */     
/* 1261 */     tail.limit += width;
/* 1262 */     this.size += width;
/* 1263 */     return this;
/*      */   }
/*      */   
/*      */   public Buffer writeHexadecimalUnsignedLong(long v) {
/* 1267 */     if (v == 0L)
/*      */     {
/* 1269 */       return writeByte(48);
/*      */     }
/*      */     
/* 1272 */     int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;
/*      */     
/* 1274 */     Segment tail = writableSegment(width);
/* 1275 */     byte[] data = tail.data;
/* 1276 */     for (int pos = tail.limit + width - 1, start = tail.limit; pos >= start; pos--) {
/* 1277 */       data[pos] = DIGITS[(int)(v & 0xFL)];
/* 1278 */       v >>>= 4L;
/*      */     } 
/* 1280 */     tail.limit += width;
/* 1281 */     this.size += width;
/* 1282 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   Segment writableSegment(int minimumCapacity) {
/* 1290 */     if (minimumCapacity < 1 || minimumCapacity > 8192) throw new IllegalArgumentException();
/*      */     
/* 1292 */     if (this.head == null) {
/* 1293 */       this.head = SegmentPool.take();
/* 1294 */       return this.head.next = this.head.prev = this.head;
/*      */     } 
/*      */     
/* 1297 */     Segment tail = this.head.prev;
/* 1298 */     if (tail.limit + minimumCapacity > 8192 || !tail.owner) {
/* 1299 */       tail = tail.push(SegmentPool.take());
/*      */     }
/* 1301 */     return tail;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void write(Buffer source, long byteCount) {
/* 1355 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/* 1356 */     if (source == this) throw new IllegalArgumentException("source == this"); 
/* 1357 */     Util.checkOffsetAndCount(source.size, 0L, byteCount);
/*      */     
/* 1359 */     while (byteCount > 0L) {
/*      */       
/* 1361 */       if (byteCount < (source.head.limit - source.head.pos)) {
/* 1362 */         Segment tail = (this.head != null) ? this.head.prev : null;
/* 1363 */         if (tail != null && tail.owner && byteCount + tail.limit - (tail.shared ? 
/* 1364 */           0L : tail.pos) <= 8192L) {
/*      */           
/* 1366 */           source.head.writeTo(tail, (int)byteCount);
/* 1367 */           source.size -= byteCount;
/* 1368 */           this.size += byteCount;
/*      */           
/*      */           return;
/*      */         } 
/*      */         
/* 1373 */         source.head = source.head.split((int)byteCount);
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 1378 */       Segment segmentToMove = source.head;
/* 1379 */       long movedByteCount = (segmentToMove.limit - segmentToMove.pos);
/* 1380 */       source.head = segmentToMove.pop();
/* 1381 */       if (this.head == null) {
/* 1382 */         this.head = segmentToMove;
/* 1383 */         this.head.next = this.head.prev = this.head;
/*      */       } else {
/* 1385 */         Segment tail = this.head.prev;
/* 1386 */         tail = tail.push(segmentToMove);
/* 1387 */         tail.compact();
/*      */       } 
/* 1389 */       source.size -= movedByteCount;
/* 1390 */       this.size += movedByteCount;
/* 1391 */       byteCount -= movedByteCount;
/*      */     } 
/*      */   }
/*      */   
/*      */   public long read(Buffer sink, long byteCount) {
/* 1396 */     if (sink == null) throw new IllegalArgumentException("sink == null"); 
/* 1397 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/* 1398 */     if (this.size == 0L) return -1L; 
/* 1399 */     if (byteCount > this.size) byteCount = this.size; 
/* 1400 */     sink.write(this, byteCount);
/* 1401 */     return byteCount;
/*      */   }
/*      */   
/*      */   public long indexOf(byte b) {
/* 1405 */     return indexOf(b, 0L, Long.MAX_VALUE);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public long indexOf(byte b, long fromIndex) {
/* 1413 */     return indexOf(b, fromIndex, Long.MAX_VALUE);
/*      */   }
/*      */   public long indexOf(byte b, long fromIndex, long toIndex) {
/*      */     long offset;
/* 1417 */     if (fromIndex < 0L || toIndex < fromIndex) {
/* 1418 */       throw new IllegalArgumentException(
/* 1419 */           String.format("size=%s fromIndex=%s toIndex=%s", new Object[] { Long.valueOf(this.size), Long.valueOf(fromIndex), Long.valueOf(toIndex) }));
/*      */     }
/*      */     
/* 1422 */     if (toIndex > this.size) toIndex = this.size; 
/* 1423 */     if (fromIndex == toIndex) return -1L;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1431 */     Segment s = this.head;
/* 1432 */     if (s == null)
/*      */     {
/* 1434 */       return -1L; } 
/* 1435 */     if (this.size - fromIndex < fromIndex) {
/*      */       
/* 1437 */       offset = this.size;
/* 1438 */       while (offset > fromIndex) {
/* 1439 */         s = s.prev;
/* 1440 */         offset -= (s.limit - s.pos);
/*      */       } 
/*      */     } else {
/*      */       
/* 1444 */       offset = 0L; long nextOffset;
/* 1445 */       while ((nextOffset = offset + (s.limit - s.pos)) < fromIndex) {
/* 1446 */         s = s.next;
/* 1447 */         offset = nextOffset;
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 1453 */     while (offset < toIndex) {
/* 1454 */       byte[] data = s.data;
/* 1455 */       int limit = (int)Math.min(s.limit, s.pos + toIndex - offset);
/* 1456 */       int pos = (int)(s.pos + fromIndex - offset);
/* 1457 */       for (; pos < limit; pos++) {
/* 1458 */         if (data[pos] == b) {
/* 1459 */           return (pos - s.pos) + offset;
/*      */         }
/*      */       } 
/*      */ 
/*      */       
/* 1464 */       offset += (s.limit - s.pos);
/* 1465 */       fromIndex = offset;
/* 1466 */       s = s.next;
/*      */     } 
/*      */     
/* 1469 */     return -1L;
/*      */   }
/*      */   
/*      */   public long indexOf(ByteString bytes) throws IOException {
/* 1473 */     return indexOf(bytes, 0L);
/*      */   }
/*      */   public long indexOf(ByteString bytes, long fromIndex) throws IOException {
/*      */     long offset;
/* 1477 */     if (bytes.size() == 0) throw new IllegalArgumentException("bytes is empty"); 
/* 1478 */     if (fromIndex < 0L) throw new IllegalArgumentException("fromIndex < 0");
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1486 */     Segment s = this.head;
/* 1487 */     if (s == null)
/*      */     {
/* 1489 */       return -1L; } 
/* 1490 */     if (this.size - fromIndex < fromIndex) {
/*      */       
/* 1492 */       offset = this.size;
/* 1493 */       while (offset > fromIndex) {
/* 1494 */         s = s.prev;
/* 1495 */         offset -= (s.limit - s.pos);
/*      */       } 
/*      */     } else {
/*      */       
/* 1499 */       offset = 0L; long nextOffset;
/* 1500 */       while ((nextOffset = offset + (s.limit - s.pos)) < fromIndex) {
/* 1501 */         s = s.next;
/* 1502 */         offset = nextOffset;
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1509 */     byte b0 = bytes.getByte(0);
/* 1510 */     int bytesSize = bytes.size();
/* 1511 */     long resultLimit = this.size - bytesSize + 1L;
/* 1512 */     while (offset < resultLimit) {
/*      */       
/* 1514 */       byte[] data = s.data;
/* 1515 */       int segmentLimit = (int)Math.min(s.limit, s.pos + resultLimit - offset);
/* 1516 */       for (int pos = (int)(s.pos + fromIndex - offset); pos < segmentLimit; pos++) {
/* 1517 */         if (data[pos] == b0 && rangeEquals(s, pos + 1, bytes, 1, bytesSize)) {
/* 1518 */           return (pos - s.pos) + offset;
/*      */         }
/*      */       } 
/*      */ 
/*      */       
/* 1523 */       offset += (s.limit - s.pos);
/* 1524 */       fromIndex = offset;
/* 1525 */       s = s.next;
/*      */     } 
/*      */     
/* 1528 */     return -1L;
/*      */   }
/*      */   
/*      */   public long indexOfElement(ByteString targetBytes) {
/* 1532 */     return indexOfElement(targetBytes, 0L);
/*      */   }
/*      */   public long indexOfElement(ByteString targetBytes, long fromIndex) {
/*      */     long offset;
/* 1536 */     if (fromIndex < 0L) throw new IllegalArgumentException("fromIndex < 0");
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1544 */     Segment s = this.head;
/* 1545 */     if (s == null)
/*      */     {
/* 1547 */       return -1L; } 
/* 1548 */     if (this.size - fromIndex < fromIndex) {
/*      */       
/* 1550 */       offset = this.size;
/* 1551 */       while (offset > fromIndex) {
/* 1552 */         s = s.prev;
/* 1553 */         offset -= (s.limit - s.pos);
/*      */       } 
/*      */     } else {
/*      */       
/* 1557 */       offset = 0L; long nextOffset;
/* 1558 */       while ((nextOffset = offset + (s.limit - s.pos)) < fromIndex) {
/* 1559 */         s = s.next;
/* 1560 */         offset = nextOffset;
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1568 */     if (targetBytes.size() == 2) {
/*      */       
/* 1570 */       byte b0 = targetBytes.getByte(0);
/* 1571 */       byte b1 = targetBytes.getByte(1);
/* 1572 */       while (offset < this.size) {
/* 1573 */         byte[] data = s.data;
/* 1574 */         for (int pos = (int)(s.pos + fromIndex - offset), limit = s.limit; pos < limit; pos++) {
/* 1575 */           int b = data[pos];
/* 1576 */           if (b == b0 || b == b1) {
/* 1577 */             return (pos - s.pos) + offset;
/*      */           }
/*      */         } 
/*      */ 
/*      */         
/* 1582 */         offset += (s.limit - s.pos);
/* 1583 */         fromIndex = offset;
/* 1584 */         s = s.next;
/*      */       } 
/*      */     } else {
/*      */       
/* 1588 */       byte[] targetByteArray = targetBytes.internalArray();
/* 1589 */       while (offset < this.size) {
/* 1590 */         byte[] data = s.data;
/* 1591 */         for (int pos = (int)(s.pos + fromIndex - offset), limit = s.limit; pos < limit; pos++) {
/* 1592 */           int b = data[pos];
/* 1593 */           for (byte t : targetByteArray) {
/* 1594 */             if (b == t) return (pos - s.pos) + offset;
/*      */           
/*      */           } 
/*      */         } 
/*      */         
/* 1599 */         offset += (s.limit - s.pos);
/* 1600 */         fromIndex = offset;
/* 1601 */         s = s.next;
/*      */       } 
/*      */     } 
/*      */     
/* 1605 */     return -1L;
/*      */   }
/*      */   
/*      */   public boolean rangeEquals(long offset, ByteString bytes) {
/* 1609 */     return rangeEquals(offset, bytes, 0, bytes.size());
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) {
/* 1614 */     if (offset < 0L || bytesOffset < 0 || byteCount < 0 || this.size - offset < byteCount || bytes
/*      */ 
/*      */ 
/*      */       
/* 1618 */       .size() - bytesOffset < byteCount) {
/* 1619 */       return false;
/*      */     }
/* 1621 */     for (int i = 0; i < byteCount; i++) {
/* 1622 */       if (getByte(offset + i) != bytes.getByte(bytesOffset + i)) {
/* 1623 */         return false;
/*      */       }
/*      */     } 
/* 1626 */     return true;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private boolean rangeEquals(Segment segment, int segmentPos, ByteString bytes, int bytesOffset, int bytesLimit) {
/* 1635 */     int segmentLimit = segment.limit;
/* 1636 */     byte[] data = segment.data;
/*      */     
/* 1638 */     for (int i = bytesOffset; i < bytesLimit; ) {
/* 1639 */       if (segmentPos == segmentLimit) {
/* 1640 */         segment = segment.next;
/* 1641 */         data = segment.data;
/* 1642 */         segmentPos = segment.pos;
/* 1643 */         segmentLimit = segment.limit;
/*      */       } 
/*      */       
/* 1646 */       if (data[segmentPos] != bytes.getByte(i)) {
/* 1647 */         return false;
/*      */       }
/*      */       
/* 1650 */       segmentPos++;
/* 1651 */       i++;
/*      */     } 
/*      */     
/* 1654 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   public void flush() {}
/*      */   
/*      */   public boolean isOpen() {
/* 1661 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   public void close() {}
/*      */   
/*      */   public Timeout timeout() {
/* 1668 */     return Timeout.NONE;
/*      */   }
/*      */ 
/*      */   
/*      */   List<Integer> segmentSizes() {
/* 1673 */     if (this.head == null) return Collections.emptyList(); 
/* 1674 */     List<Integer> result = new ArrayList<>();
/* 1675 */     result.add(Integer.valueOf(this.head.limit - this.head.pos));
/* 1676 */     for (Segment s = this.head.next; s != this.head; s = s.next) {
/* 1677 */       result.add(Integer.valueOf(s.limit - s.pos));
/*      */     }
/* 1679 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString md5() {
/* 1684 */     return digest("MD5");
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString sha1() {
/* 1689 */     return digest("SHA-1");
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString sha256() {
/* 1694 */     return digest("SHA-256");
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString sha512() {
/* 1699 */     return digest("SHA-512");
/*      */   }
/*      */   
/*      */   private ByteString digest(String algorithm) {
/*      */     try {
/* 1704 */       MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
/* 1705 */       if (this.head != null) {
/* 1706 */         messageDigest.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
/* 1707 */         for (Segment s = this.head.next; s != this.head; s = s.next) {
/* 1708 */           messageDigest.update(s.data, s.pos, s.limit - s.pos);
/*      */         }
/*      */       } 
/* 1711 */       return ByteString.of(messageDigest.digest());
/* 1712 */     } catch (NoSuchAlgorithmException e) {
/* 1713 */       throw new AssertionError();
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString hmacSha1(ByteString key) {
/* 1719 */     return hmac("HmacSHA1", key);
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString hmacSha256(ByteString key) {
/* 1724 */     return hmac("HmacSHA256", key);
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString hmacSha512(ByteString key) {
/* 1729 */     return hmac("HmacSHA512", key);
/*      */   }
/*      */   
/*      */   private ByteString hmac(String algorithm, ByteString key) {
/*      */     try {
/* 1734 */       Mac mac = Mac.getInstance(algorithm);
/* 1735 */       mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
/* 1736 */       if (this.head != null) {
/* 1737 */         mac.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
/* 1738 */         for (Segment s = this.head.next; s != this.head; s = s.next) {
/* 1739 */           mac.update(s.data, s.pos, s.limit - s.pos);
/*      */         }
/*      */       } 
/* 1742 */       return ByteString.of(mac.doFinal());
/* 1743 */     } catch (NoSuchAlgorithmException e) {
/* 1744 */       throw new AssertionError();
/* 1745 */     } catch (InvalidKeyException e) {
/* 1746 */       throw new IllegalArgumentException(e);
/*      */     } 
/*      */   }
/*      */   
/*      */   public boolean equals(Object o) {
/* 1751 */     if (this == o) return true; 
/* 1752 */     if (!(o instanceof Buffer)) return false; 
/* 1753 */     Buffer that = (Buffer)o;
/* 1754 */     if (this.size != that.size) return false; 
/* 1755 */     if (this.size == 0L) return true;
/*      */     
/* 1757 */     Segment sa = this.head;
/* 1758 */     Segment sb = that.head;
/* 1759 */     int posA = sa.pos;
/* 1760 */     int posB = sb.pos;
/*      */     long pos;
/* 1762 */     for (pos = 0L; pos < this.size; pos += count) {
/* 1763 */       long count = Math.min(sa.limit - posA, sb.limit - posB);
/*      */       
/* 1765 */       for (int i = 0; i < count; i++) {
/* 1766 */         if (sa.data[posA++] != sb.data[posB++]) return false;
/*      */       
/*      */       } 
/* 1769 */       if (posA == sa.limit) {
/* 1770 */         sa = sa.next;
/* 1771 */         posA = sa.pos;
/*      */       } 
/*      */       
/* 1774 */       if (posB == sb.limit) {
/* 1775 */         sb = sb.next;
/* 1776 */         posB = sb.pos;
/*      */       } 
/*      */     } 
/*      */     
/* 1780 */     return true;
/*      */   }
/*      */   
/*      */   public int hashCode() {
/* 1784 */     Segment s = this.head;
/* 1785 */     if (s == null) return 0; 
/* 1786 */     int result = 1;
/*      */     while (true) {
/* 1788 */       for (int pos = s.pos, limit = s.limit; pos < limit; pos++) {
/* 1789 */         result = 31 * result + s.data[pos];
/*      */       }
/* 1791 */       s = s.next;
/* 1792 */       if (s == this.head) {
/* 1793 */         return result;
/*      */       }
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String toString() {
/* 1801 */     return snapshot().toString();
/*      */   }
/*      */ 
/*      */   
/*      */   public Buffer clone() {
/* 1806 */     Buffer result = new Buffer();
/* 1807 */     if (this.size == 0L) return result;
/*      */     
/* 1809 */     result.head = this.head.sharedCopy();
/* 1810 */     result.head.next = result.head.prev = result.head;
/* 1811 */     for (Segment s = this.head.next; s != this.head; s = s.next) {
/* 1812 */       result.head.prev.push(s.sharedCopy());
/*      */     }
/* 1814 */     result.size = this.size;
/* 1815 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   public final ByteString snapshot() {
/* 1820 */     if (this.size > 2147483647L) {
/* 1821 */       throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + this.size);
/*      */     }
/* 1823 */     return snapshot((int)this.size);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public final ByteString snapshot(int byteCount) {
/* 1830 */     if (byteCount == 0) return ByteString.EMPTY; 
/* 1831 */     return new SegmentedByteString(this, byteCount);
/*      */   }
/*      */   
/*      */   public final UnsafeCursor readUnsafe() {
/* 1835 */     return readUnsafe(new UnsafeCursor());
/*      */   }
/*      */   
/*      */   public final UnsafeCursor readUnsafe(UnsafeCursor unsafeCursor) {
/* 1839 */     if (unsafeCursor.buffer != null) {
/* 1840 */       throw new IllegalStateException("already attached to a buffer");
/*      */     }
/*      */     
/* 1843 */     unsafeCursor.buffer = this;
/* 1844 */     unsafeCursor.readWrite = false;
/* 1845 */     return unsafeCursor;
/*      */   }
/*      */   
/*      */   public final UnsafeCursor readAndWriteUnsafe() {
/* 1849 */     return readAndWriteUnsafe(new UnsafeCursor());
/*      */   }
/*      */   
/*      */   public final UnsafeCursor readAndWriteUnsafe(UnsafeCursor unsafeCursor) {
/* 1853 */     if (unsafeCursor.buffer != null) {
/* 1854 */       throw new IllegalStateException("already attached to a buffer");
/*      */     }
/*      */     
/* 1857 */     unsafeCursor.buffer = this;
/* 1858 */     unsafeCursor.readWrite = true;
/* 1859 */     return unsafeCursor;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static final class UnsafeCursor
/*      */     implements Closeable
/*      */   {
/*      */     public Buffer buffer;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public boolean readWrite;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     private Segment segment;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 2067 */     public long offset = -1L;
/*      */     public byte[] data;
/* 2069 */     public int start = -1;
/* 2070 */     public int end = -1;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public final int next() {
/* 2078 */       if (this.offset == this.buffer.size) throw new IllegalStateException(); 
/* 2079 */       if (this.offset == -1L) return seek(0L); 
/* 2080 */       return seek(this.offset + (this.end - this.start));
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public final int seek(long offset) {
/*      */       Segment next;
/*      */       long nextOffset;
/* 2089 */       if (offset < -1L || offset > this.buffer.size) {
/* 2090 */         throw new ArrayIndexOutOfBoundsException(
/* 2091 */             String.format("offset=%s > size=%s", new Object[] { Long.valueOf(offset), Long.valueOf(this.buffer.size) }));
/*      */       }
/*      */       
/* 2094 */       if (offset == -1L || offset == this.buffer.size) {
/* 2095 */         this.segment = null;
/* 2096 */         this.offset = offset;
/* 2097 */         this.data = null;
/* 2098 */         this.start = -1;
/* 2099 */         this.end = -1;
/* 2100 */         return -1;
/*      */       } 
/*      */ 
/*      */       
/* 2104 */       long min = 0L;
/* 2105 */       long max = this.buffer.size;
/* 2106 */       Segment head = this.buffer.head;
/* 2107 */       Segment tail = this.buffer.head;
/* 2108 */       if (this.segment != null) {
/* 2109 */         long segmentOffset = this.offset - (this.start - this.segment.pos);
/* 2110 */         if (segmentOffset > offset) {
/*      */           
/* 2112 */           max = segmentOffset;
/* 2113 */           tail = this.segment;
/*      */         } else {
/*      */           
/* 2116 */           min = segmentOffset;
/* 2117 */           head = this.segment;
/*      */         } 
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 2123 */       if (max - offset > offset - min) {
/*      */         
/* 2125 */         next = head;
/* 2126 */         nextOffset = min;
/* 2127 */         while (offset >= nextOffset + (next.limit - next.pos)) {
/* 2128 */           nextOffset += (next.limit - next.pos);
/* 2129 */           next = next.next;
/*      */         } 
/*      */       } else {
/*      */         
/* 2133 */         next = tail;
/* 2134 */         nextOffset = max;
/* 2135 */         while (nextOffset > offset) {
/* 2136 */           next = next.prev;
/* 2137 */           nextOffset -= (next.limit - next.pos);
/*      */         } 
/*      */       } 
/*      */ 
/*      */       
/* 2142 */       if (this.readWrite && next.shared) {
/* 2143 */         Segment unsharedNext = next.unsharedCopy();
/* 2144 */         if (this.buffer.head == next) {
/* 2145 */           this.buffer.head = unsharedNext;
/*      */         }
/* 2147 */         next = next.push(unsharedNext);
/* 2148 */         next.prev.pop();
/*      */       } 
/*      */ 
/*      */       
/* 2152 */       this.segment = next;
/* 2153 */       this.offset = offset;
/* 2154 */       this.data = next.data;
/* 2155 */       this.start = next.pos + (int)(offset - nextOffset);
/* 2156 */       this.end = next.limit;
/* 2157 */       return this.end - this.start;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public final long resizeBuffer(long newSize) {
/* 2178 */       if (this.buffer == null) {
/* 2179 */         throw new IllegalStateException("not attached to a buffer");
/*      */       }
/* 2181 */       if (!this.readWrite) {
/* 2182 */         throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers");
/*      */       }
/*      */       
/* 2185 */       long oldSize = this.buffer.size;
/* 2186 */       if (newSize <= oldSize) {
/* 2187 */         if (newSize < 0L) {
/* 2188 */           throw new IllegalArgumentException("newSize < 0: " + newSize);
/*      */         }
/*      */         
/* 2191 */         for (long bytesToSubtract = oldSize - newSize; bytesToSubtract > 0L; ) {
/* 2192 */           Segment tail = this.buffer.head.prev;
/* 2193 */           int tailSize = tail.limit - tail.pos;
/* 2194 */           if (tailSize <= bytesToSubtract) {
/* 2195 */             this.buffer.head = tail.pop();
/* 2196 */             SegmentPool.recycle(tail);
/* 2197 */             bytesToSubtract -= tailSize; continue;
/*      */           } 
/* 2199 */           tail.limit = (int)(tail.limit - bytesToSubtract);
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 2204 */         this.segment = null;
/* 2205 */         this.offset = newSize;
/* 2206 */         this.data = null;
/* 2207 */         this.start = -1;
/* 2208 */         this.end = -1;
/* 2209 */       } else if (newSize > oldSize) {
/*      */         
/* 2211 */         boolean needsToSeek = true;
/* 2212 */         for (long bytesToAdd = newSize - oldSize; bytesToAdd > 0L; ) {
/* 2213 */           Segment tail = this.buffer.writableSegment(1);
/* 2214 */           int segmentBytesToAdd = (int)Math.min(bytesToAdd, (8192 - tail.limit));
/* 2215 */           tail.limit += segmentBytesToAdd;
/* 2216 */           bytesToAdd -= segmentBytesToAdd;
/*      */ 
/*      */           
/* 2219 */           if (needsToSeek) {
/* 2220 */             this.segment = tail;
/* 2221 */             this.offset = oldSize;
/* 2222 */             this.data = tail.data;
/* 2223 */             this.start = tail.limit - segmentBytesToAdd;
/* 2224 */             this.end = tail.limit;
/* 2225 */             needsToSeek = false;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */       
/* 2230 */       this.buffer.size = newSize;
/*      */       
/* 2232 */       return oldSize;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public final long expandBuffer(int minByteCount) {
/* 2259 */       if (minByteCount <= 0) {
/* 2260 */         throw new IllegalArgumentException("minByteCount <= 0: " + minByteCount);
/*      */       }
/* 2262 */       if (minByteCount > 8192) {
/* 2263 */         throw new IllegalArgumentException("minByteCount > Segment.SIZE: " + minByteCount);
/*      */       }
/* 2265 */       if (this.buffer == null) {
/* 2266 */         throw new IllegalStateException("not attached to a buffer");
/*      */       }
/* 2268 */       if (!this.readWrite) {
/* 2269 */         throw new IllegalStateException("expandBuffer() only permitted for read/write buffers");
/*      */       }
/*      */       
/* 2272 */       long oldSize = this.buffer.size;
/* 2273 */       Segment tail = this.buffer.writableSegment(minByteCount);
/* 2274 */       int result = 8192 - tail.limit;
/* 2275 */       tail.limit = 8192;
/* 2276 */       this.buffer.size = oldSize + result;
/*      */ 
/*      */       
/* 2279 */       this.segment = tail;
/* 2280 */       this.offset = oldSize;
/* 2281 */       this.data = tail.data;
/* 2282 */       this.start = 8192 - result;
/* 2283 */       this.end = 8192;
/*      */       
/* 2285 */       return result;
/*      */     }
/*      */ 
/*      */     
/*      */     public void close() {
/* 2290 */       if (this.buffer == null) {
/* 2291 */         throw new IllegalStateException("not attached to a buffer");
/*      */       }
/*      */       
/* 2294 */       this.buffer = null;
/* 2295 */       this.segment = null;
/* 2296 */       this.offset = -1L;
/* 2297 */       this.data = null;
/* 2298 */       this.start = -1;
/* 2299 */       this.end = -1;
/*      */     }
/*      */   }
/*      */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Buffer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */