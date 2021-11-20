/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.zip.Deflater;
/*     */ import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
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
/*     */ public final class DeflaterSink
/*     */   implements Sink
/*     */ {
/*     */   private final BufferedSink sink;
/*     */   private final Deflater deflater;
/*     */   private boolean closed;
/*     */   
/*     */   public DeflaterSink(Sink sink, Deflater deflater) {
/*  44 */     this(Okio.buffer(sink), deflater);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   DeflaterSink(BufferedSink sink, Deflater deflater) {
/*  53 */     if (sink == null) throw new IllegalArgumentException("source == null"); 
/*  54 */     if (deflater == null) throw new IllegalArgumentException("inflater == null"); 
/*  55 */     this.sink = sink;
/*  56 */     this.deflater = deflater;
/*     */   }
/*     */   
/*     */   public void write(Buffer source, long byteCount) throws IOException {
/*  60 */     Util.checkOffsetAndCount(source.size, 0L, byteCount);
/*  61 */     while (byteCount > 0L) {
/*     */       
/*  63 */       Segment head = source.head;
/*  64 */       int toDeflate = (int)Math.min(byteCount, (head.limit - head.pos));
/*  65 */       this.deflater.setInput(head.data, head.pos, toDeflate);
/*     */ 
/*     */       
/*  68 */       deflate(false);
/*     */ 
/*     */       
/*  71 */       source.size -= toDeflate;
/*  72 */       head.pos += toDeflate;
/*  73 */       if (head.pos == head.limit) {
/*  74 */         source.head = head.pop();
/*  75 */         SegmentPool.recycle(head);
/*     */       } 
/*     */       
/*  78 */       byteCount -= toDeflate;
/*     */     } 
/*     */   }
/*     */   
/*     */   @IgnoreJRERequirement
/*     */   private void deflate(boolean syncFlush) throws IOException {
/*  84 */     Buffer buffer = this.sink.buffer();
/*     */     while (true) {
/*  86 */       Segment s = buffer.writableSegment(1);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  94 */       int deflated = syncFlush ? this.deflater.deflate(s.data, s.limit, 8192 - s.limit, 2) : this.deflater.deflate(s.data, s.limit, 8192 - s.limit);
/*     */       
/*  96 */       if (deflated > 0) {
/*  97 */         s.limit += deflated;
/*  98 */         buffer.size += deflated;
/*  99 */         this.sink.emitCompleteSegments(); continue;
/* 100 */       }  if (this.deflater.needsInput()) {
/* 101 */         if (s.pos == s.limit) {
/*     */           
/* 103 */           buffer.head = s.pop();
/* 104 */           SegmentPool.recycle(s);
/*     */         } 
/*     */         return;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public void flush() throws IOException {
/* 112 */     deflate(true);
/* 113 */     this.sink.flush();
/*     */   }
/*     */   
/*     */   void finishDeflate() throws IOException {
/* 117 */     this.deflater.finish();
/* 118 */     deflate(false);
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 122 */     if (this.closed) {
/*     */       return;
/*     */     }
/*     */     
/* 126 */     Throwable thrown = null;
/*     */     try {
/* 128 */       finishDeflate();
/* 129 */     } catch (Throwable e) {
/* 130 */       thrown = e;
/*     */     } 
/*     */     
/*     */     try {
/* 134 */       this.deflater.end();
/* 135 */     } catch (Throwable e) {
/* 136 */       if (thrown == null) thrown = e;
/*     */     
/*     */     } 
/*     */     try {
/* 140 */       this.sink.close();
/* 141 */     } catch (Throwable e) {
/* 142 */       if (thrown == null) thrown = e; 
/*     */     } 
/* 144 */     this.closed = true;
/*     */     
/* 146 */     if (thrown != null) Util.sneakyRethrow(thrown); 
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/* 150 */     return this.sink.timeout();
/*     */   }
/*     */   
/*     */   public String toString() {
/* 154 */     return "DeflaterSink(" + this.sink + ")";
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\DeflaterSink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */