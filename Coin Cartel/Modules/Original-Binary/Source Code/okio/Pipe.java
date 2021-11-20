/*     */ package okio;
/*     */ 
/*     */ import java.io.IOException;
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
/*     */ public final class Pipe
/*     */ {
/*     */   final long maxBufferSize;
/*  38 */   final Buffer buffer = new Buffer();
/*     */   boolean sinkClosed;
/*     */   boolean sourceClosed;
/*  41 */   private final Sink sink = new PipeSink();
/*  42 */   private final Source source = new PipeSource();
/*     */   
/*     */   public Pipe(long maxBufferSize) {
/*  45 */     if (maxBufferSize < 1L) {
/*  46 */       throw new IllegalArgumentException("maxBufferSize < 1: " + maxBufferSize);
/*     */     }
/*  48 */     this.maxBufferSize = maxBufferSize;
/*     */   }
/*     */   
/*     */   public final Source source() {
/*  52 */     return this.source;
/*     */   }
/*     */   
/*     */   public final Sink sink() {
/*  56 */     return this.sink;
/*     */   }
/*     */   
/*     */   final class PipeSink implements Sink {
/*  60 */     final Timeout timeout = new Timeout();
/*     */     
/*     */     public void write(Buffer source, long byteCount) throws IOException {
/*  63 */       synchronized (Pipe.this.buffer) {
/*  64 */         if (Pipe.this.sinkClosed) throw new IllegalStateException("closed");
/*     */         
/*  66 */         while (byteCount > 0L) {
/*  67 */           if (Pipe.this.sourceClosed) throw new IOException("source is closed");
/*     */           
/*  69 */           long bufferSpaceAvailable = Pipe.this.maxBufferSize - Pipe.this.buffer.size();
/*  70 */           if (bufferSpaceAvailable == 0L) {
/*  71 */             this.timeout.waitUntilNotified(Pipe.this.buffer);
/*     */             
/*     */             continue;
/*     */           } 
/*  75 */           long bytesToWrite = Math.min(bufferSpaceAvailable, byteCount);
/*  76 */           Pipe.this.buffer.write(source, bytesToWrite);
/*  77 */           byteCount -= bytesToWrite;
/*  78 */           Pipe.this.buffer.notifyAll();
/*     */         } 
/*     */       } 
/*     */     }
/*     */     
/*     */     public void flush() throws IOException {
/*  84 */       synchronized (Pipe.this.buffer) {
/*  85 */         if (Pipe.this.sinkClosed) throw new IllegalStateException("closed"); 
/*  86 */         if (Pipe.this.sourceClosed && Pipe.this.buffer.size() > 0L) throw new IOException("source is closed"); 
/*     */       } 
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/*  91 */       synchronized (Pipe.this.buffer) {
/*  92 */         if (Pipe.this.sinkClosed)
/*  93 */           return;  if (Pipe.this.sourceClosed && Pipe.this.buffer.size() > 0L) throw new IOException("source is closed"); 
/*  94 */         Pipe.this.sinkClosed = true;
/*  95 */         Pipe.this.buffer.notifyAll();
/*     */       } 
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 100 */       return this.timeout;
/*     */     }
/*     */   }
/*     */   
/*     */   final class PipeSource implements Source {
/* 105 */     final Timeout timeout = new Timeout();
/*     */     
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/* 108 */       synchronized (Pipe.this.buffer) {
/* 109 */         if (Pipe.this.sourceClosed) throw new IllegalStateException("closed");
/*     */         
/* 111 */         while (Pipe.this.buffer.size() == 0L) {
/* 112 */           if (Pipe.this.sinkClosed) return -1L; 
/* 113 */           this.timeout.waitUntilNotified(Pipe.this.buffer);
/*     */         } 
/*     */         
/* 116 */         long result = Pipe.this.buffer.read(sink, byteCount);
/* 117 */         Pipe.this.buffer.notifyAll();
/* 118 */         return result;
/*     */       } 
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 123 */       synchronized (Pipe.this.buffer) {
/* 124 */         Pipe.this.sourceClosed = true;
/* 125 */         Pipe.this.buffer.notifyAll();
/*     */       } 
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 130 */       return this.timeout;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Pipe.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */