/*     */ package org.json.zip;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
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
/*     */ public class BitInputStream
/*     */   implements BitReader
/*     */ {
/*  40 */   static final int[] mask = new int[] { 0, 1, 3, 7, 15, 31, 63, 127, 255 };
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  45 */   private int available = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  50 */   private int unread = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private InputStream in;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  60 */   private long nrBits = 0L;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public BitInputStream(InputStream in) {
/*  70 */     this.in = in;
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
/*     */   
/*     */   public BitInputStream(InputStream in, int firstByte) {
/*  85 */     this.in = in;
/*  86 */     this.unread = firstByte;
/*  87 */     this.available = 8;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean bit() throws IOException {
/*  96 */     return (read(1) != 0);
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
/*     */   public long nrBits() {
/* 108 */     return this.nrBits;
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
/*     */   public boolean pad(int factor) throws IOException {
/* 122 */     int padding = factor - (int)(this.nrBits % factor);
/* 123 */     boolean result = true;
/*     */     
/* 125 */     for (int i = 0; i < padding; i++) {
/* 126 */       if (bit()) {
/* 127 */         result = false;
/*     */       }
/*     */     } 
/* 130 */     return result;
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
/*     */   public int read(int width) throws IOException {
/* 142 */     if (width == 0) {
/* 143 */       return 0;
/*     */     }
/* 145 */     if (width < 0 || width > 32) {
/* 146 */       throw new IOException("Bad read width.");
/*     */     }
/* 148 */     int result = 0;
/* 149 */     while (width > 0) {
/* 150 */       if (this.available == 0) {
/* 151 */         this.unread = this.in.read();
/* 152 */         if (this.unread < 0) {
/* 153 */           throw new IOException("Attempt to read past end.");
/*     */         }
/* 155 */         this.available = 8;
/*     */       } 
/* 157 */       int take = width;
/* 158 */       if (take > this.available) {
/* 159 */         take = this.available;
/*     */       }
/* 161 */       result |= (this.unread >>> this.available - take & mask[take]) << width - take;
/*     */       
/* 163 */       this.nrBits += take;
/* 164 */       this.available -= take;
/* 165 */       width -= take;
/*     */     } 
/* 167 */     return result;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\BitInputStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */