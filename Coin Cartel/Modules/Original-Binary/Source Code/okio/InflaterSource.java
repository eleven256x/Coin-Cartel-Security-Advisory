/*     */ package okio;
/*     */ 
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.util.zip.DataFormatException;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class InflaterSource
/*     */   implements Source
/*     */ {
/*     */   private final BufferedSource source;
/*     */   private final Inflater inflater;
/*     */   private int bufferBytesHeldByInflater;
/*     */   private boolean closed;
/*     */   
/*     */   public InflaterSource(Source source, Inflater inflater) {
/*  40 */     this(Okio.buffer(source), inflater);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   InflaterSource(BufferedSource source, Inflater inflater) {
/*  49 */     if (source == null) throw new IllegalArgumentException("source == null"); 
/*  50 */     if (inflater == null) throw new IllegalArgumentException("inflater == null"); 
/*  51 */     this.source = source;
/*  52 */     this.inflater = inflater;
/*     */   }
/*     */ 
/*     */   
/*     */   public long read(Buffer sink, long byteCount) throws IOException {
/*  57 */     if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/*  58 */     if (this.closed) throw new IllegalStateException("closed"); 
/*  59 */     if (byteCount == 0L) return 0L;
/*     */     
/*     */     while (true) {
/*  62 */       boolean sourceExhausted = refill();
/*     */ 
/*     */       
/*     */       try {
/*  66 */         Segment tail = sink.writableSegment(1);
/*  67 */         int toRead = (int)Math.min(byteCount, (8192 - tail.limit));
/*  68 */         int bytesInflated = this.inflater.inflate(tail.data, tail.limit, toRead);
/*  69 */         if (bytesInflated > 0) {
/*  70 */           tail.limit += bytesInflated;
/*  71 */           sink.size += bytesInflated;
/*  72 */           return bytesInflated;
/*     */         } 
/*  74 */         if (this.inflater.finished() || this.inflater.needsDictionary()) {
/*  75 */           releaseInflatedBytes();
/*  76 */           if (tail.pos == tail.limit) {
/*     */             
/*  78 */             sink.head = tail.pop();
/*  79 */             SegmentPool.recycle(tail);
/*     */           } 
/*  81 */           return -1L;
/*     */         } 
/*  83 */         if (sourceExhausted) throw new EOFException("source exhausted prematurely"); 
/*  84 */       } catch (DataFormatException e) {
/*  85 */         throw new IOException(e);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final boolean refill() throws IOException {
/*  96 */     if (!this.inflater.needsInput()) return false;
/*     */     
/*  98 */     releaseInflatedBytes();
/*  99 */     if (this.inflater.getRemaining() != 0) throw new IllegalStateException("?");
/*     */ 
/*     */     
/* 102 */     if (this.source.exhausted()) return true;
/*     */ 
/*     */     
/* 105 */     Segment head = (this.source.buffer()).head;
/* 106 */     this.bufferBytesHeldByInflater = head.limit - head.pos;
/* 107 */     this.inflater.setInput(head.data, head.pos, this.bufferBytesHeldByInflater);
/* 108 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private void releaseInflatedBytes() throws IOException {
/* 113 */     if (this.bufferBytesHeldByInflater == 0)
/* 114 */       return;  int toRelease = this.bufferBytesHeldByInflater - this.inflater.getRemaining();
/* 115 */     this.bufferBytesHeldByInflater -= toRelease;
/* 116 */     this.source.skip(toRelease);
/*     */   }
/*     */   
/*     */   public Timeout timeout() {
/* 120 */     return this.source.timeout();
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 124 */     if (this.closed)
/* 125 */       return;  this.inflater.end();
/* 126 */     this.closed = true;
/* 127 */     this.source.close();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\InflaterSource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */