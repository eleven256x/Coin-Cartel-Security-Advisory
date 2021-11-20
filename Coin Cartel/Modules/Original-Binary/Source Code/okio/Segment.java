/*     */ package okio;
/*     */ 
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
/*     */ final class Segment
/*     */ {
/*     */   static final int SIZE = 8192;
/*     */   static final int SHARE_MINIMUM = 1024;
/*     */   final byte[] data;
/*     */   int pos;
/*     */   int limit;
/*     */   boolean shared;
/*     */   boolean owner;
/*     */   Segment next;
/*     */   Segment prev;
/*     */   
/*     */   Segment() {
/*  63 */     this.data = new byte[8192];
/*  64 */     this.owner = true;
/*  65 */     this.shared = false;
/*     */   }
/*     */   
/*     */   Segment(byte[] data, int pos, int limit, boolean shared, boolean owner) {
/*  69 */     this.data = data;
/*  70 */     this.pos = pos;
/*  71 */     this.limit = limit;
/*  72 */     this.shared = shared;
/*  73 */     this.owner = owner;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   final Segment sharedCopy() {
/*  82 */     this.shared = true;
/*  83 */     return new Segment(this.data, this.pos, this.limit, true, false);
/*     */   }
/*     */ 
/*     */   
/*     */   final Segment unsharedCopy() {
/*  88 */     return new Segment((byte[])this.data.clone(), this.pos, this.limit, false, true);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public final Segment pop() {
/*  96 */     Segment result = (this.next != this) ? this.next : null;
/*  97 */     this.prev.next = this.next;
/*  98 */     this.next.prev = this.prev;
/*  99 */     this.next = null;
/* 100 */     this.prev = null;
/* 101 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final Segment push(Segment segment) {
/* 109 */     segment.prev = this;
/* 110 */     segment.next = this.next;
/* 111 */     this.next.prev = segment;
/* 112 */     this.next = segment;
/* 113 */     return segment;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final Segment split(int byteCount) {
/*     */     Segment prefix;
/* 125 */     if (byteCount <= 0 || byteCount > this.limit - this.pos) throw new IllegalArgumentException();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 133 */     if (byteCount >= 1024) {
/* 134 */       prefix = sharedCopy();
/*     */     } else {
/* 136 */       prefix = SegmentPool.take();
/* 137 */       System.arraycopy(this.data, this.pos, prefix.data, 0, byteCount);
/*     */     } 
/*     */     
/* 140 */     prefix.limit = prefix.pos + byteCount;
/* 141 */     this.pos += byteCount;
/* 142 */     this.prev.push(prefix);
/* 143 */     return prefix;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public final void compact() {
/* 151 */     if (this.prev == this) throw new IllegalStateException(); 
/* 152 */     if (!this.prev.owner)
/* 153 */       return;  int byteCount = this.limit - this.pos;
/* 154 */     int availableByteCount = 8192 - this.prev.limit + (this.prev.shared ? 0 : this.prev.pos);
/* 155 */     if (byteCount > availableByteCount)
/* 156 */       return;  writeTo(this.prev, byteCount);
/* 157 */     pop();
/* 158 */     SegmentPool.recycle(this);
/*     */   }
/*     */ 
/*     */   
/*     */   public final void writeTo(Segment sink, int byteCount) {
/* 163 */     if (!sink.owner) throw new IllegalArgumentException(); 
/* 164 */     if (sink.limit + byteCount > 8192) {
/*     */       
/* 166 */       if (sink.shared) throw new IllegalArgumentException(); 
/* 167 */       if (sink.limit + byteCount - sink.pos > 8192) throw new IllegalArgumentException(); 
/* 168 */       System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
/* 169 */       sink.limit -= sink.pos;
/* 170 */       sink.pos = 0;
/*     */     } 
/*     */     
/* 173 */     System.arraycopy(this.data, this.pos, sink.data, sink.limit, byteCount);
/* 174 */     sink.limit += byteCount;
/* 175 */     this.pos += byteCount;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Segment.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */