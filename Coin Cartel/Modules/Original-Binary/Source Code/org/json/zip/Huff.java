/*     */ package org.json.zip;
/*     */ 
/*     */ import org.json.JSONException;
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
/*     */ public class Huff
/*     */   implements None, PostMortem
/*     */ {
/*     */   private final int domain;
/*     */   private final Symbol[] symbols;
/*     */   private Symbol table;
/*     */   private boolean upToDate = false;
/*     */   private int width;
/*     */   
/*     */   private static class Symbol
/*     */     implements PostMortem
/*     */   {
/*     */     public Symbol back;
/*     */     public Symbol next;
/*     */     public Symbol zero;
/*     */     public Symbol one;
/*     */     public final int integer;
/*     */     public long weight;
/*     */     
/*     */     public Symbol(int integer) {
/*  88 */       this.integer = integer;
/*  89 */       this.weight = 0L;
/*  90 */       this.next = null;
/*  91 */       this.back = null;
/*  92 */       this.one = null;
/*  93 */       this.zero = null;
/*     */     }
/*     */     
/*     */     public boolean postMortem(PostMortem pm) {
/*  97 */       boolean result = true;
/*  98 */       Symbol that = (Symbol)pm;
/*     */       
/* 100 */       if (this.integer != that.integer || this.weight != that.weight) {
/* 101 */         return false;
/*     */       }
/* 103 */       if (((this.back != null) ? true : false) != ((that.back != null) ? true : false)) {
/* 104 */         return false;
/*     */       }
/* 106 */       Symbol zero = this.zero;
/* 107 */       Symbol one = this.one;
/* 108 */       if (zero == null) {
/* 109 */         if (that.zero != null) {
/* 110 */           return false;
/*     */         }
/*     */       } else {
/* 113 */         result = zero.postMortem(that.zero);
/*     */       } 
/* 115 */       if (one == null) {
/* 116 */         if (that.one != null) {
/* 117 */           return false;
/*     */         }
/*     */       } else {
/* 120 */         result = one.postMortem(that.one);
/*     */       } 
/* 122 */       return result;
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
/*     */   public Huff(int domain) {
/* 134 */     this.domain = domain;
/* 135 */     int length = domain * 2 - 1;
/* 136 */     this.symbols = new Symbol[length];
/*     */     
/*     */     int i;
/*     */     
/* 140 */     for (i = 0; i < domain; i++) {
/* 141 */       this.symbols[i] = new Symbol(i);
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 146 */     for (i = domain; i < length; i++) {
/* 147 */       this.symbols[i] = new Symbol(-1);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void generate() {
/* 158 */     if (!this.upToDate) {
/*     */ 
/*     */ 
/*     */       
/* 162 */       Symbol symbol, head = this.symbols[0];
/*     */       
/* 164 */       Symbol previous = head;
/*     */ 
/*     */       
/* 167 */       this.table = null;
/* 168 */       head.next = null;
/* 169 */       for (int i = 1; i < this.domain; i++) {
/* 170 */         symbol = this.symbols[i];
/*     */ 
/*     */ 
/*     */         
/* 174 */         if (symbol.weight < head.weight) {
/* 175 */           symbol.next = head;
/* 176 */           head = symbol;
/*     */         } else {
/*     */           Symbol next;
/*     */ 
/*     */ 
/*     */           
/* 182 */           if (symbol.weight < previous.weight) {
/* 183 */             previous = head;
/*     */           }
/*     */ 
/*     */ 
/*     */ 
/*     */           
/*     */           while (true) {
/* 190 */             next = previous.next;
/* 191 */             if (next == null || symbol.weight < next.weight) {
/*     */               break;
/*     */             }
/* 194 */             previous = next;
/*     */           } 
/* 196 */           symbol.next = next;
/* 197 */           previous.next = symbol;
/* 198 */           previous = symbol;
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 205 */       int avail = this.domain;
/*     */ 
/*     */       
/* 208 */       previous = head;
/*     */       while (true) {
/* 210 */         Symbol next, first = head;
/* 211 */         Symbol second = first.next;
/* 212 */         head = second.next;
/* 213 */         symbol = this.symbols[avail];
/* 214 */         avail++;
/* 215 */         first.weight += second.weight;
/* 216 */         symbol.zero = first;
/* 217 */         symbol.one = second;
/* 218 */         symbol.back = null;
/* 219 */         first.back = symbol;
/* 220 */         second.back = symbol;
/* 221 */         if (head == null) {
/*     */           break;
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 227 */         if (symbol.weight < head.weight) {
/* 228 */           symbol.next = head;
/* 229 */           head = symbol;
/* 230 */           previous = head; continue;
/*     */         } 
/*     */         while (true) {
/* 233 */           next = previous.next;
/* 234 */           if (next == null || symbol.weight < next.weight) {
/*     */             break;
/*     */           }
/* 237 */           previous = next;
/*     */         } 
/* 239 */         symbol.next = next;
/* 240 */         previous.next = symbol;
/* 241 */         previous = symbol;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 248 */       this.table = symbol;
/* 249 */       this.upToDate = true;
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean postMortem(int integer) {
/* 254 */     int[] bits = new int[this.domain];
/* 255 */     Symbol symbol = this.symbols[integer];
/* 256 */     if (symbol.integer != integer) {
/* 257 */       return false;
/*     */     }
/* 259 */     int i = 0;
/*     */     while (true) {
/* 261 */       Symbol back = symbol.back;
/* 262 */       if (back == null) {
/*     */         break;
/*     */       }
/* 265 */       if (back.zero == symbol) {
/* 266 */         bits[i] = 0;
/* 267 */       } else if (back.one == symbol) {
/* 268 */         bits[i] = 1;
/*     */       } else {
/* 270 */         return false;
/*     */       } 
/* 272 */       i++;
/* 273 */       symbol = back;
/*     */     } 
/* 275 */     if (symbol != this.table) {
/* 276 */       return false;
/*     */     }
/* 278 */     this.width = 0;
/* 279 */     symbol = this.table;
/* 280 */     while (symbol.integer == -1) {
/* 281 */       i--;
/* 282 */       symbol = (bits[i] != 0) ? symbol.one : symbol.zero;
/*     */     } 
/* 284 */     return (symbol.integer == integer && i == 0);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean postMortem(PostMortem pm) {
/* 295 */     for (int integer = 0; integer < this.domain; integer++) {
/* 296 */       if (!postMortem(integer)) {
/* 297 */         JSONzip.log("\nBad huff ");
/* 298 */         JSONzip.logchar(integer, integer);
/* 299 */         return false;
/*     */       } 
/*     */     } 
/* 302 */     return this.table.postMortem(((Huff)pm).table);
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
/*     */   public int read(BitReader bitreader) throws JSONException {
/*     */     try {
/* 316 */       this.width = 0;
/* 317 */       Symbol symbol = this.table;
/* 318 */       while (symbol.integer == -1) {
/* 319 */         this.width++;
/* 320 */         symbol = bitreader.bit() ? symbol.one : symbol.zero;
/*     */       } 
/* 322 */       tick(symbol.integer);
/*     */ 
/*     */ 
/*     */       
/* 326 */       return symbol.integer;
/* 327 */     } catch (Throwable e) {
/* 328 */       throw new JSONException(e);
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
/*     */   public void tick(int value) {
/* 340 */     (this.symbols[value]).weight++;
/* 341 */     this.upToDate = false;
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
/*     */   public void tick(int from, int to) {
/* 354 */     for (int value = from; value <= to; value++) {
/* 355 */       tick(value);
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
/*     */ 
/*     */ 
/*     */   
/*     */   private void write(Symbol symbol, BitWriter bitwriter) throws JSONException {
/*     */     try {
/* 372 */       Symbol back = symbol.back;
/* 373 */       if (back != null) {
/* 374 */         this.width++;
/* 375 */         write(back, bitwriter);
/* 376 */         if (back.zero == symbol) {
/* 377 */           bitwriter.zero();
/*     */         } else {
/* 379 */           bitwriter.one();
/*     */         } 
/*     */       } 
/* 382 */     } catch (Throwable e) {
/* 383 */       throw new JSONException(e);
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
/*     */ 
/*     */ 
/*     */   
/*     */   public void write(int value, BitWriter bitwriter) throws JSONException {
/* 399 */     this.width = 0;
/* 400 */     write(this.symbols[value], bitwriter);
/* 401 */     tick(value);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\Huff.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */