/*     */ package org.json.zip;
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
/*     */ public abstract class JSONzip
/*     */   implements None, PostMortem
/*     */ {
/*  52 */   public static final int[] twos = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  60 */   public static final byte[] bcd = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 46, 45, 43, 69 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final long int4 = 16L;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final long int7 = 128L;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final long int14 = 16384L;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int end = 256;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  87 */   public static final int endOfNumber = bcd.length;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int maxSubstringLength = 10;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int minSubstringLength = 3;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final boolean probe = false;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int substringLimit = 40;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipEmptyObject = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipEmptyArray = 1;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipTrue = 2;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipFalse = 3;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipNull = 4;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipObject = 5;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipArrayString = 6;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int zipArrayValue = 7;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected final Huff namehuff;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected final MapKeep namekeep;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected final MapKeep stringkeep;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected final Huff substringhuff;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected final TrieKeep substringkeep;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected final MapKeep values;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected JSONzip() {
/* 185 */     this.namehuff = new Huff(257);
/* 186 */     this.namekeep = new MapKeep(9);
/* 187 */     this.stringkeep = new MapKeep(11);
/* 188 */     this.substringhuff = new Huff(257);
/* 189 */     this.substringkeep = new TrieKeep(12);
/* 190 */     this.values = new MapKeep(10);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 197 */     this.namehuff.tick(32, 125);
/* 198 */     this.namehuff.tick(97, 122);
/* 199 */     this.namehuff.tick(256);
/* 200 */     this.namehuff.tick(256);
/* 201 */     this.substringhuff.tick(32, 125);
/* 202 */     this.substringhuff.tick(97, 122);
/* 203 */     this.substringhuff.tick(256);
/* 204 */     this.substringhuff.tick(256);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected void begin() {
/* 211 */     this.namehuff.generate();
/* 212 */     this.substringhuff.generate();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void log() {
/* 219 */     log("\n");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void log(int integer) {
/* 228 */     log(integer + " ");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void log(int integer, int width) {
/* 238 */     log(integer + ":" + width + " ");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void log(String string) {
/* 247 */     System.out.print(string);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void logchar(int integer, int width) {
/* 257 */     if (integer > 32 && integer <= 125) {
/* 258 */       log("'" + (char)integer + "':" + width + " ");
/*     */     } else {
/* 260 */       log(integer, width);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean postMortem(PostMortem pm) {
/* 273 */     JSONzip that = (JSONzip)pm;
/* 274 */     return (this.namehuff.postMortem(that.namehuff) && this.namekeep.postMortem(that.namekeep) && this.stringkeep.postMortem(that.stringkeep) && this.substringhuff.postMortem(that.substringhuff) && this.substringkeep.postMortem(that.substringkeep) && this.values.postMortem(that.values));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\JSONzip.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */