/*    */ package org.json.zip;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ abstract class Keep
/*    */   implements None, PostMortem
/*    */ {
/*    */   protected int capacity;
/*    */   protected int length;
/*    */   protected int power;
/*    */   protected long[] uses;
/*    */   
/*    */   public Keep(int bits) {
/* 42 */     this.capacity = JSONzip.twos[bits];
/* 43 */     this.length = 0;
/* 44 */     this.power = 0;
/* 45 */     this.uses = new long[this.capacity];
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static long age(long use) {
/* 56 */     return (use >= 32L) ? 16L : (use / 2L);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public int bitsize() {
/* 65 */     while (JSONzip.twos[this.power] < this.length) {
/* 66 */       this.power++;
/*    */     }
/* 68 */     return this.power;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void tick(int integer) {
/* 75 */     this.uses[integer] = this.uses[integer] + 1L;
/*    */   }
/*    */   
/*    */   public abstract Object value(int paramInt);
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\Keep.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */