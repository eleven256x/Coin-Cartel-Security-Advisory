/*    */ package io.socket.backo;
/*    */ 
/*    */ import java.math.BigDecimal;
/*    */ import java.math.BigInteger;
/*    */ 
/*    */ public class Backoff
/*    */ {
/*  8 */   private long ms = 100L;
/*  9 */   private long max = 10000L;
/* 10 */   private int factor = 2;
/*    */   
/*    */   private double jitter;
/*    */   
/*    */   private int attempts;
/*    */ 
/*    */   
/*    */   public long duration() {
/* 18 */     BigInteger ms = BigInteger.valueOf(this.ms).multiply(BigInteger.valueOf(this.factor).pow(this.attempts++));
/* 19 */     if (this.jitter != 0.0D) {
/* 20 */       double rand = Math.random();
/*    */ 
/*    */       
/* 23 */       BigInteger deviation = BigDecimal.valueOf(rand).multiply(BigDecimal.valueOf(this.jitter)).multiply(new BigDecimal(ms)).toBigInteger();
/* 24 */       ms = (((int)Math.floor(rand * 10.0D) & 0x1) == 0) ? ms.subtract(deviation) : ms.add(deviation);
/*    */     } 
/* 26 */     return ms.min(BigInteger.valueOf(this.max)).longValue();
/*    */   }
/*    */   
/*    */   public void reset() {
/* 30 */     this.attempts = 0;
/*    */   }
/*    */   
/*    */   public Backoff setMin(long min) {
/* 34 */     this.ms = min;
/* 35 */     return this;
/*    */   }
/*    */   
/*    */   public Backoff setMax(long max) {
/* 39 */     this.max = max;
/* 40 */     return this;
/*    */   }
/*    */   
/*    */   public Backoff setFactor(int factor) {
/* 44 */     this.factor = factor;
/* 45 */     return this;
/*    */   }
/*    */   
/*    */   public Backoff setJitter(double jitter) {
/* 49 */     this.jitter = jitter;
/* 50 */     return this;
/*    */   }
/*    */   
/*    */   public int getAttempts() {
/* 54 */     return this.attempts;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\backo\Backoff.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */