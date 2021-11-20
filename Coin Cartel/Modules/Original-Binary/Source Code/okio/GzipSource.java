/*     */ package okio;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.util.zip.CRC32;
/*     */ import java.util.zip.Inflater;
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
/*     */ public final class GzipSource
/*     */   implements Source
/*     */ {
/*     */   private static final byte FHCRC = 1;
/*     */   private static final byte FEXTRA = 2;
/*     */   private static final byte FNAME = 3;
/*     */   private static final byte FCOMMENT = 4;
/*     */   private static final byte SECTION_HEADER = 0;
/*     */   private static final byte SECTION_BODY = 1;
/*     */   private static final byte SECTION_TRAILER = 2;
/*     */   private static final byte SECTION_DONE = 3;
/*  39 */   private int section = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final BufferedSource source;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final Inflater inflater;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final InflaterSource inflaterSource;
/*     */ 
/*     */ 
/*     */   
/*  58 */   private final CRC32 crc = new CRC32();
/*     */   
/*     */   public GzipSource(Source source) {
/*  61 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/*  62 */     this.inflater = new Inflater(true);
/*  63 */     this.source = Okio.buffer(source);
/*  64 */     this.inflaterSource = new InflaterSource(this.source, this.inflater);
/*     */   }
/*     */   
/*     */   public long read(Buffer sink, long byteCount) throws IOException {
/*  68 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/*  69 */     if (byteCount == 0L) return 0L;
/*     */ 
/*     */     
/*  72 */     if (this.section == 0) {
/*  73 */       consumeHeader();
/*  74 */       this.section = 1;
/*     */     } 
/*     */ 
/*     */     
/*  78 */     if (this.section == 1) {
/*  79 */       long offset = sink.size;
/*  80 */       long result = this.inflaterSource.read(sink, byteCount);
/*  81 */       if (result != -1L) {
/*  82 */         updateCrc(sink, offset, result);
/*  83 */         return result;
/*     */       } 
/*  85 */       this.section = 2;
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  91 */     if (this.section == 2) {
/*  92 */       consumeTrailer();
/*  93 */       this.section = 3;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  99 */       if (!this.source.exhausted()) {
/* 100 */         throw new IOException("gzip finished without exhausting source");
/*     */       }
/*     */     } 
/*     */     
/* 104 */     return -1L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void consumeHeader() throws IOException {
/* 114 */     this.source.require(10L);
/* 115 */     byte flags = this.source.buffer().getByte(3L);
/* 116 */     boolean fhcrc = ((flags >> 1 & 0x1) == 1);
/* 117 */     if (fhcrc) updateCrc(this.source.buffer(), 0L, 10L);
/*     */     
/* 119 */     short id1id2 = this.source.readShort();
/* 120 */     checkEqual("ID1ID2", 8075, id1id2);
/* 121 */     this.source.skip(8L);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 127 */     if ((flags >> 2 & 0x1) == 1) {
/* 128 */       this.source.require(2L);
/* 129 */       if (fhcrc) updateCrc(this.source.buffer(), 0L, 2L); 
/* 130 */       int xlen = this.source.buffer().readShortLe();
/* 131 */       this.source.require(xlen);
/* 132 */       if (fhcrc) updateCrc(this.source.buffer(), 0L, xlen); 
/* 133 */       this.source.skip(xlen);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 140 */     if ((flags >> 3 & 0x1) == 1) {
/* 141 */       long index = this.source.indexOf((byte)0);
/* 142 */       if (index == -1L) throw new EOFException(); 
/* 143 */       if (fhcrc) updateCrc(this.source.buffer(), 0L, index + 1L); 
/* 144 */       this.source.skip(index + 1L);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 151 */     if ((flags >> 4 & 0x1) == 1) {
/* 152 */       long index = this.source.indexOf((byte)0);
/* 153 */       if (index == -1L) throw new EOFException(); 
/* 154 */       if (fhcrc) updateCrc(this.source.buffer(), 0L, index + 1L); 
/* 155 */       this.source.skip(index + 1L);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 162 */     if (fhcrc) {
/* 163 */       checkEqual("FHCRC", this.source.readShortLe(), (short)(int)this.crc.getValue());
/* 164 */       this.crc.reset();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void consumeTrailer() throws IOException {
/* 173 */     checkEqual("CRC", this.source.readIntLe(), (int)this.crc.getValue());
/* 174 */     checkEqual("ISIZE", this.source.readIntLe(), (int)this.inflater.getBytesWritten());
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/* 178 */     return this.source.timeout();
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 182 */     this.inflaterSource.close();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void updateCrc(Buffer buffer, long offset, long byteCount) {
/* 188 */     Segment s = buffer.head;
/* 189 */     for (; offset >= (s.limit - s.pos); s = s.next) {
/* 190 */       offset -= (s.limit - s.pos);
/*     */     }
/*     */ 
/*     */     
/* 194 */     for (; byteCount > 0L; s = s.next) {
/* 195 */       int pos = (int)(s.pos + offset);
/* 196 */       int toUpdate = (int)Math.min((s.limit - pos), byteCount);
/* 197 */       this.crc.update(s.data, pos, toUpdate);
/* 198 */       byteCount -= toUpdate;
/* 199 */       offset = 0L;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void checkEqual(String name, int expected, int actual) throws IOException {
/* 204 */     if (actual != expected)
/* 205 */       throw new IOException(String.format("%s: actual 0x%08x != expected 0x%08x", new Object[] { name, 
/* 206 */               Integer.valueOf(actual), Integer.valueOf(expected) })); 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\GzipSource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */