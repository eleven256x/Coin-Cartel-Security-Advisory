/*     */ package okio;
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
/*     */ public final class Utf8
/*     */ {
/*     */   public static long size(String string) {
/*  74 */     return size(string, 0, string.length());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static long size(String string, int beginIndex, int endIndex) {
/*  82 */     if (string == null) throw new IllegalArgumentException("string == null"); 
/*  83 */     if (beginIndex < 0) throw new IllegalArgumentException("beginIndex < 0: " + beginIndex); 
/*  84 */     if (endIndex < beginIndex) {
/*  85 */       throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
/*     */     }
/*  87 */     if (endIndex > string.length()) {
/*  88 */       throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string
/*  89 */           .length());
/*     */     }
/*     */     
/*  92 */     long result = 0L;
/*  93 */     for (int i = beginIndex; i < endIndex; ) {
/*  94 */       int c = string.charAt(i);
/*     */       
/*  96 */       if (c < 128) {
/*     */         
/*  98 */         result++;
/*  99 */         i++; continue;
/*     */       } 
/* 101 */       if (c < 2048) {
/*     */         
/* 103 */         result += 2L;
/* 104 */         i++; continue;
/*     */       } 
/* 106 */       if (c < 55296 || c > 57343) {
/*     */         
/* 108 */         result += 3L;
/* 109 */         i++;
/*     */         continue;
/*     */       } 
/* 112 */       int low = (i + 1 < endIndex) ? string.charAt(i + 1) : 0;
/* 113 */       if (c > 56319 || low < 56320 || low > 57343) {
/*     */         
/* 115 */         result++;
/* 116 */         i++;
/*     */         
/*     */         continue;
/*     */       } 
/* 120 */       result += 4L;
/* 121 */       i += 2;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 126 */     return result;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Utf8.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */