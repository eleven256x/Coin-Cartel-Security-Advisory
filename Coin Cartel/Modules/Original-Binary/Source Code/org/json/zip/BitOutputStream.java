/*     */ package org.json.zip;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
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
/*     */ public class BitOutputStream
/*     */   implements BitWriter
/*     */ {
/*  41 */   private long nrBits = 0L;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private OutputStream out;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int unwritten;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  56 */   private int vacant = 8;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public BitOutputStream(OutputStream out) {
/*  66 */     this.out = out;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public long nrBits() {
/*  75 */     return this.nrBits;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void one() throws IOException {
/*  84 */     write(1, 1);
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
/*     */ 
/*     */   
/*     */   public void pad(int factor) throws IOException {
/*  98 */     int padding = factor - (int)(this.nrBits % factor);
/*  99 */     int excess = padding & 0x7;
/* 100 */     if (excess > 0) {
/* 101 */       write(0, excess);
/* 102 */       padding -= excess;
/*     */     } 
/* 104 */     while (padding > 0) {
/* 105 */       write(0, 8);
/* 106 */       padding -= 8;
/*     */     } 
/* 108 */     this.out.flush();
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
/*     */   
/*     */   public void write(int bits, int width) throws IOException {
/* 121 */     if (bits == 0 && width == 0) {
/*     */       return;
/*     */     }
/* 124 */     if (width <= 0 || width > 32) {
/* 125 */       throw new IOException("Bad write width.");
/*     */     }
/* 127 */     while (width > 0) {
/* 128 */       int actual = width;
/* 129 */       if (actual > this.vacant) {
/* 130 */         actual = this.vacant;
/*     */       }
/* 132 */       this.unwritten |= (bits >>> width - actual & BitInputStream.mask[actual]) << this.vacant - actual;
/*     */       
/* 134 */       width -= actual;
/* 135 */       this.nrBits += actual;
/* 136 */       this.vacant -= actual;
/* 137 */       if (this.vacant == 0) {
/* 138 */         this.out.write(this.unwritten);
/* 139 */         this.unwritten = 0;
/* 140 */         this.vacant = 8;
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void zero() throws IOException {
/* 151 */     write(0, 1);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\BitOutputStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */