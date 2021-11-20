/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.Arrays;
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
/*     */ final class SegmentedByteString
/*     */   extends ByteString
/*     */ {
/*     */   final transient byte[][] segments;
/*     */   final transient int[] directory;
/*     */   
/*     */   SegmentedByteString(Buffer buffer, int byteCount) {
/*  57 */     super(null);
/*  58 */     Util.checkOffsetAndCount(buffer.size, 0L, byteCount);
/*     */ 
/*     */     
/*  61 */     int offset = 0;
/*  62 */     int segmentCount = 0; Segment s;
/*  63 */     for (s = buffer.head; offset < byteCount; s = s.next) {
/*  64 */       if (s.limit == s.pos) {
/*  65 */         throw new AssertionError("s.limit == s.pos");
/*     */       }
/*  67 */       offset += s.limit - s.pos;
/*  68 */       segmentCount++;
/*     */     } 
/*     */ 
/*     */     
/*  72 */     this.segments = new byte[segmentCount][];
/*  73 */     this.directory = new int[segmentCount * 2];
/*  74 */     offset = 0;
/*  75 */     segmentCount = 0;
/*  76 */     for (s = buffer.head; offset < byteCount; s = s.next) {
/*  77 */       this.segments[segmentCount] = s.data;
/*  78 */       offset += s.limit - s.pos;
/*  79 */       if (offset > byteCount) {
/*  80 */         offset = byteCount;
/*     */       }
/*  82 */       this.directory[segmentCount] = offset;
/*  83 */       this.directory[segmentCount + this.segments.length] = s.pos;
/*  84 */       s.shared = true;
/*  85 */       segmentCount++;
/*     */     } 
/*     */   }
/*     */   
/*     */   public String utf8() {
/*  90 */     return toByteString().utf8();
/*     */   }
/*     */   
/*     */   public String string(Charset charset) {
/*  94 */     return toByteString().string(charset);
/*     */   }
/*     */   
/*     */   public String base64() {
/*  98 */     return toByteString().base64();
/*     */   }
/*     */   
/*     */   public String hex() {
/* 102 */     return toByteString().hex();
/*     */   }
/*     */   
/*     */   public ByteString toAsciiLowercase() {
/* 106 */     return toByteString().toAsciiLowercase();
/*     */   }
/*     */   
/*     */   public ByteString toAsciiUppercase() {
/* 110 */     return toByteString().toAsciiUppercase();
/*     */   }
/*     */   
/*     */   public ByteString md5() {
/* 114 */     return toByteString().md5();
/*     */   }
/*     */   
/*     */   public ByteString sha1() {
/* 118 */     return toByteString().sha1();
/*     */   }
/*     */   
/*     */   public ByteString sha256() {
/* 122 */     return toByteString().sha256();
/*     */   }
/*     */   
/*     */   public ByteString hmacSha1(ByteString key) {
/* 126 */     return toByteString().hmacSha1(key);
/*     */   }
/*     */   
/*     */   public ByteString hmacSha256(ByteString key) {
/* 130 */     return toByteString().hmacSha256(key);
/*     */   }
/*     */   
/*     */   public String base64Url() {
/* 134 */     return toByteString().base64Url();
/*     */   }
/*     */   
/*     */   public ByteString substring(int beginIndex) {
/* 138 */     return toByteString().substring(beginIndex);
/*     */   }
/*     */   
/*     */   public ByteString substring(int beginIndex, int endIndex) {
/* 142 */     return toByteString().substring(beginIndex, endIndex);
/*     */   }
/*     */   
/*     */   public byte getByte(int pos) {
/* 146 */     Util.checkOffsetAndCount(this.directory[this.segments.length - 1], pos, 1L);
/* 147 */     int segment = segment(pos);
/* 148 */     int segmentOffset = (segment == 0) ? 0 : this.directory[segment - 1];
/* 149 */     int segmentPos = this.directory[segment + this.segments.length];
/* 150 */     return this.segments[segment][pos - segmentOffset + segmentPos];
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private int segment(int pos) {
/* 156 */     int i = Arrays.binarySearch(this.directory, 0, this.segments.length, pos + 1);
/* 157 */     return (i >= 0) ? i : (i ^ 0xFFFFFFFF);
/*     */   }
/*     */   
/*     */   public int size() {
/* 161 */     return this.directory[this.segments.length - 1];
/*     */   }
/*     */   
/*     */   public byte[] toByteArray() {
/* 165 */     byte[] result = new byte[this.directory[this.segments.length - 1]];
/* 166 */     int segmentOffset = 0;
/* 167 */     for (int s = 0, segmentCount = this.segments.length; s < segmentCount; s++) {
/* 168 */       int segmentPos = this.directory[segmentCount + s];
/* 169 */       int nextSegmentOffset = this.directory[s];
/* 170 */       System.arraycopy(this.segments[s], segmentPos, result, segmentOffset, nextSegmentOffset - segmentOffset);
/*     */       
/* 172 */       segmentOffset = nextSegmentOffset;
/*     */     } 
/* 174 */     return result;
/*     */   }
/*     */   
/*     */   public ByteBuffer asByteBuffer() {
/* 178 */     return ByteBuffer.wrap(toByteArray()).asReadOnlyBuffer();
/*     */   }
/*     */   
/*     */   public void write(OutputStream out) throws IOException {
/* 182 */     if (out == null) throw new IllegalArgumentException("out == null"); 
/* 183 */     int segmentOffset = 0;
/* 184 */     for (int s = 0, segmentCount = this.segments.length; s < segmentCount; s++) {
/* 185 */       int segmentPos = this.directory[segmentCount + s];
/* 186 */       int nextSegmentOffset = this.directory[s];
/* 187 */       out.write(this.segments[s], segmentPos, nextSegmentOffset - segmentOffset);
/* 188 */       segmentOffset = nextSegmentOffset;
/*     */     } 
/*     */   }
/*     */   
/*     */   void write(Buffer buffer) {
/* 193 */     int segmentOffset = 0;
/* 194 */     for (int s = 0, segmentCount = this.segments.length; s < segmentCount; s++) {
/* 195 */       int segmentPos = this.directory[segmentCount + s];
/* 196 */       int nextSegmentOffset = this.directory[s];
/* 197 */       Segment segment = new Segment(this.segments[s], segmentPos, segmentPos + nextSegmentOffset - segmentOffset, true, false);
/*     */       
/* 199 */       if (buffer.head == null) {
/* 200 */         buffer.head = segment.next = segment.prev = segment;
/*     */       } else {
/* 202 */         buffer.head.prev.push(segment);
/*     */       } 
/* 204 */       segmentOffset = nextSegmentOffset;
/*     */     } 
/* 206 */     buffer.size += segmentOffset;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
/* 211 */     if (offset < 0 || offset > size() - byteCount) return false;
/*     */     
/* 213 */     for (int s = segment(offset); byteCount > 0; s++) {
/* 214 */       int segmentOffset = (s == 0) ? 0 : this.directory[s - 1];
/* 215 */       int segmentSize = this.directory[s] - segmentOffset;
/* 216 */       int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
/* 217 */       int segmentPos = this.directory[this.segments.length + s];
/* 218 */       int arrayOffset = offset - segmentOffset + segmentPos;
/* 219 */       if (!other.rangeEquals(otherOffset, this.segments[s], arrayOffset, stepSize)) return false; 
/* 220 */       offset += stepSize;
/* 221 */       otherOffset += stepSize;
/* 222 */       byteCount -= stepSize;
/*     */     } 
/* 224 */     return true;
/*     */   }
/*     */   
/*     */   public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
/* 228 */     if (offset < 0 || offset > size() - byteCount || otherOffset < 0 || otherOffset > other.length - byteCount)
/*     */     {
/* 230 */       return false;
/*     */     }
/*     */     
/* 233 */     for (int s = segment(offset); byteCount > 0; s++) {
/* 234 */       int segmentOffset = (s == 0) ? 0 : this.directory[s - 1];
/* 235 */       int segmentSize = this.directory[s] - segmentOffset;
/* 236 */       int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
/* 237 */       int segmentPos = this.directory[this.segments.length + s];
/* 238 */       int arrayOffset = offset - segmentOffset + segmentPos;
/* 239 */       if (!Util.arrayRangeEquals(this.segments[s], arrayOffset, other, otherOffset, stepSize)) return false; 
/* 240 */       offset += stepSize;
/* 241 */       otherOffset += stepSize;
/* 242 */       byteCount -= stepSize;
/*     */     } 
/* 244 */     return true;
/*     */   }
/*     */   
/*     */   public int indexOf(byte[] other, int fromIndex) {
/* 248 */     return toByteString().indexOf(other, fromIndex);
/*     */   }
/*     */   
/*     */   public int lastIndexOf(byte[] other, int fromIndex) {
/* 252 */     return toByteString().lastIndexOf(other, fromIndex);
/*     */   }
/*     */ 
/*     */   
/*     */   private ByteString toByteString() {
/* 257 */     return new ByteString(toByteArray());
/*     */   }
/*     */   
/*     */   byte[] internalArray() {
/* 261 */     return toByteArray();
/*     */   }
/*     */   
/*     */   public boolean equals(Object o) {
/* 265 */     if (o == this) return true; 
/* 266 */     return (o instanceof ByteString && ((ByteString)o)
/* 267 */       .size() == size() && 
/* 268 */       rangeEquals(0, (ByteString)o, 0, size()));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 272 */     int result = this.hashCode;
/* 273 */     if (result != 0) return result;
/*     */ 
/*     */     
/* 276 */     result = 1;
/* 277 */     int segmentOffset = 0;
/* 278 */     for (int s = 0, segmentCount = this.segments.length; s < segmentCount; s++) {
/* 279 */       byte[] segment = this.segments[s];
/* 280 */       int segmentPos = this.directory[segmentCount + s];
/* 281 */       int nextSegmentOffset = this.directory[s];
/* 282 */       int segmentSize = nextSegmentOffset - segmentOffset;
/* 283 */       for (int i = segmentPos, limit = segmentPos + segmentSize; i < limit; i++) {
/* 284 */         result = 31 * result + segment[i];
/*     */       }
/* 286 */       segmentOffset = nextSegmentOffset;
/*     */     } 
/* 288 */     return this.hashCode = result;
/*     */   }
/*     */   
/*     */   public String toString() {
/* 292 */     return toByteString().toString();
/*     */   }
/*     */   
/*     */   private Object writeReplace() {
/* 296 */     return toByteString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\SegmentedByteString.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */