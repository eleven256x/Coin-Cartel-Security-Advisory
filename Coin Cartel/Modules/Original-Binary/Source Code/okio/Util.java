/*    */ package okio;
/*    */ 
/*    */ import java.nio.charset.Charset;
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
/*    */ final class Util
/*    */ {
/* 22 */   public static final Charset UTF_8 = Charset.forName("UTF-8");
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static void checkOffsetAndCount(long size, long offset, long byteCount) {
/* 28 */     if ((offset | byteCount) < 0L || offset > size || size - offset < byteCount) {
/* 29 */       throw new ArrayIndexOutOfBoundsException(
/* 30 */           String.format("size=%s offset=%s byteCount=%s", new Object[] { Long.valueOf(size), Long.valueOf(offset), Long.valueOf(byteCount) }));
/*    */     }
/*    */   }
/*    */   
/*    */   public static short reverseBytesShort(short s) {
/* 35 */     int i = s & 0xFFFF;
/* 36 */     int reversed = (i & 0xFF00) >>> 8 | (i & 0xFF) << 8;
/*    */     
/* 38 */     return (short)reversed;
/*    */   }
/*    */   
/*    */   public static int reverseBytesInt(int i) {
/* 42 */     return (i & 0xFF000000) >>> 24 | (i & 0xFF0000) >>> 8 | (i & 0xFF00) << 8 | (i & 0xFF) << 24;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static long reverseBytesLong(long v) {
/* 49 */     return (v & 0xFF00000000000000L) >>> 56L | (v & 0xFF000000000000L) >>> 40L | (v & 0xFF0000000000L) >>> 24L | (v & 0xFF00000000L) >>> 8L | (v & 0xFF000000L) << 8L | (v & 0xFF0000L) << 24L | (v & 0xFF00L) << 40L | (v & 0xFFL) << 56L;
/*    */   }
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
/*    */   public static void sneakyRethrow(Throwable t) {
/* 65 */     sneakyThrow2(t);
/*    */   }
/*    */ 
/*    */   
/*    */   private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
/* 70 */     throw (T)t;
/*    */   }
/*    */ 
/*    */   
/*    */   public static boolean arrayRangeEquals(byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
/* 75 */     for (int i = 0; i < byteCount; i++) {
/* 76 */       if (a[i + aOffset] != b[i + bOffset]) return false; 
/*    */     } 
/* 78 */     return true;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Util.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */