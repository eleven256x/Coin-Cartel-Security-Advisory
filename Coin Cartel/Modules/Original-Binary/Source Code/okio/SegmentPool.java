/*    */ package okio;
/*    */ 
/*    */ import javax.annotation.Nullable;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ final class SegmentPool
/*    */ {
/*    */   static final long MAX_SIZE = 65536L;
/*    */   @Nullable
/*    */   static Segment next;
/*    */   static long byteCount;
/*    */   
/*    */   static Segment take() {
/* 39 */     synchronized (SegmentPool.class) {
/* 40 */       if (next != null) {
/* 41 */         Segment result = next;
/* 42 */         next = result.next;
/* 43 */         result.next = null;
/* 44 */         byteCount -= 8192L;
/* 45 */         return result;
/*    */       } 
/*    */     } 
/* 48 */     return new Segment();
/*    */   }
/*    */   
/*    */   static void recycle(Segment segment) {
/* 52 */     if (segment.next != null || segment.prev != null) throw new IllegalArgumentException(); 
/* 53 */     if (segment.shared)
/* 54 */       return;  synchronized (SegmentPool.class) {
/* 55 */       if (byteCount + 8192L > 65536L)
/* 56 */         return;  byteCount += 8192L;
/* 57 */       segment.next = next;
/* 58 */       segment.pos = segment.limit = 0;
/* 59 */       next = segment;
/*    */     } 
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\SegmentPool.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */