/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.zip.CRC32;
/*     */ import java.util.zip.Deflater;
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
/*     */ public final class GzipSink
/*     */   implements Sink
/*     */ {
/*     */   private final BufferedSink sink;
/*     */   private final Deflater deflater;
/*     */   private final DeflaterSink deflaterSink;
/*     */   private boolean closed;
/*  54 */   private final CRC32 crc = new CRC32();
/*     */   
/*     */   public GzipSink(Sink sink) {
/*  57 */     if (sink == null) throw new IllegalArgumentException("sink == null"); 
/*  58 */     this.deflater = new Deflater(-1, true);
/*  59 */     this.sink = Okio.buffer(sink);
/*  60 */     this.deflaterSink = new DeflaterSink(this.sink, this.deflater);
/*     */     
/*  62 */     writeHeader();
/*     */   }
/*     */   
/*     */   public void write(Buffer source, long byteCount) throws IOException {
/*  66 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/*  67 */     if (byteCount == 0L)
/*     */       return; 
/*  69 */     updateCrc(source, byteCount);
/*  70 */     this.deflaterSink.write(source, byteCount);
/*     */   }
/*     */   
/*     */   public void flush() throws IOException {
/*  74 */     this.deflaterSink.flush();
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/*  78 */     return this.sink.timeout();
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/*  82 */     if (this.closed) {
/*     */       return;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  89 */     Throwable thrown = null;
/*     */     try {
/*  91 */       this.deflaterSink.finishDeflate();
/*  92 */       writeFooter();
/*  93 */     } catch (Throwable e) {
/*  94 */       thrown = e;
/*     */     } 
/*     */     
/*     */     try {
/*  98 */       this.deflater.end();
/*  99 */     } catch (Throwable e) {
/* 100 */       if (thrown == null) thrown = e;
/*     */     
/*     */     } 
/*     */     try {
/* 104 */       this.sink.close();
/* 105 */     } catch (Throwable e) {
/* 106 */       if (thrown == null) thrown = e; 
/*     */     } 
/* 108 */     this.closed = true;
/*     */     
/* 110 */     if (thrown != null) Util.sneakyRethrow(thrown);
/*     */   
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final Deflater deflater() {
/* 118 */     return this.deflater;
/*     */   }
/*     */ 
/*     */   
/*     */   private void writeHeader() {
/* 123 */     Buffer buffer = this.sink.buffer();
/* 124 */     buffer.writeShort(8075);
/* 125 */     buffer.writeByte(8);
/* 126 */     buffer.writeByte(0);
/* 127 */     buffer.writeInt(0);
/* 128 */     buffer.writeByte(0);
/* 129 */     buffer.writeByte(0);
/*     */   }
/*     */   
/*     */   private void writeFooter() throws IOException {
/* 133 */     this.sink.writeIntLe((int)this.crc.getValue());
/* 134 */     this.sink.writeIntLe((int)this.deflater.getBytesRead());
/*     */   }
/*     */ 
/*     */   
/*     */   private void updateCrc(Buffer buffer, long byteCount) {
/* 139 */     for (Segment head = buffer.head; byteCount > 0L; head = head.next) {
/* 140 */       int segmentLength = (int)Math.min(byteCount, (head.limit - head.pos));
/* 141 */       this.crc.update(head.data, head.pos, segmentLength);
/* 142 */       byteCount -= segmentLength;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\GzipSink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */