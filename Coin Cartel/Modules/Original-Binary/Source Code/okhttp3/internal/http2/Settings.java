/*     */ package okhttp3.internal.http2;
/*     */ 
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
/*     */ public final class Settings
/*     */ {
/*     */   static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;
/*     */   static final int HEADER_TABLE_SIZE = 1;
/*     */   static final int ENABLE_PUSH = 2;
/*     */   static final int MAX_CONCURRENT_STREAMS = 4;
/*     */   static final int MAX_FRAME_SIZE = 5;
/*     */   static final int MAX_HEADER_LIST_SIZE = 6;
/*     */   static final int INITIAL_WINDOW_SIZE = 7;
/*     */   static final int COUNT = 10;
/*     */   private int set;
/*  51 */   private final int[] values = new int[10];
/*     */   
/*     */   void clear() {
/*  54 */     this.set = 0;
/*  55 */     Arrays.fill(this.values, 0);
/*     */   }
/*     */   
/*     */   Settings set(int id, int value) {
/*  59 */     if (id < 0 || id >= this.values.length) {
/*  60 */       return this;
/*     */     }
/*     */     
/*  63 */     int bit = 1 << id;
/*  64 */     this.set |= bit;
/*  65 */     this.values[id] = value;
/*  66 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   boolean isSet(int id) {
/*  71 */     int bit = 1 << id;
/*  72 */     return ((this.set & bit) != 0);
/*     */   }
/*     */ 
/*     */   
/*     */   int get(int id) {
/*  77 */     return this.values[id];
/*     */   }
/*     */ 
/*     */   
/*     */   int size() {
/*  82 */     return Integer.bitCount(this.set);
/*     */   }
/*     */ 
/*     */   
/*     */   int getHeaderTableSize() {
/*  87 */     int bit = 2;
/*  88 */     return ((bit & this.set) != 0) ? this.values[1] : -1;
/*     */   }
/*     */ 
/*     */   
/*     */   boolean getEnablePush(boolean defaultValue) {
/*  93 */     int bit = 4;
/*  94 */     return ((((bit & this.set) != 0) ? this.values[2] : (defaultValue ? true : false)) == true);
/*     */   }
/*     */   
/*     */   int getMaxConcurrentStreams(int defaultValue) {
/*  98 */     int bit = 16;
/*  99 */     return ((bit & this.set) != 0) ? this.values[4] : defaultValue;
/*     */   }
/*     */   
/*     */   int getMaxFrameSize(int defaultValue) {
/* 103 */     int bit = 32;
/* 104 */     return ((bit & this.set) != 0) ? this.values[5] : defaultValue;
/*     */   }
/*     */   
/*     */   int getMaxHeaderListSize(int defaultValue) {
/* 108 */     int bit = 64;
/* 109 */     return ((bit & this.set) != 0) ? this.values[6] : defaultValue;
/*     */   }
/*     */   
/*     */   int getInitialWindowSize() {
/* 113 */     int bit = 128;
/* 114 */     return ((bit & this.set) != 0) ? this.values[7] : 65535;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void merge(Settings other) {
/* 122 */     for (int i = 0; i < 10; i++) {
/* 123 */       if (other.isSet(i))
/* 124 */         set(i, other.get(i)); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Settings.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */