/*     */ package io.socket.engineio.parser;
/*     */ 
/*     */ import java.io.UnsupportedEncodingException;
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
/*     */ public class Base64
/*     */ {
/*     */   public static final int DEFAULT = 0;
/*     */   public static final int NO_PADDING = 1;
/*     */   public static final int NO_WRAP = 2;
/*     */   public static final int CRLF = 4;
/*     */   public static final int URL_SAFE = 8;
/*     */   public static final int NO_CLOSE = 16;
/*     */   
/*     */   static abstract class Coder
/*     */   {
/*     */     public byte[] output;
/*     */     public int op;
/*     */     
/*     */     public abstract boolean process(byte[] param1ArrayOfbyte, int param1Int1, int param1Int2, boolean param1Boolean);
/*     */     
/*     */     public abstract int maxOutputSize(int param1Int);
/*     */   }
/*     */   
/*     */   public static byte[] decode(String str, int flags) {
/* 107 */     return decode(str.getBytes(), flags);
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
/*     */ 
/*     */   
/*     */   public static byte[] decode(byte[] input, int flags) {
/* 124 */     return decode(input, 0, input.length, flags);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static byte[] decode(byte[] input, int offset, int len, int flags) {
/* 145 */     Decoder decoder = new Decoder(flags, new byte[len * 3 / 4]);
/* 146 */     if (!decoder.process(input, offset, len, true)) {
/* 147 */       throw new IllegalArgumentException("bad base-64");
/*     */     }
/*     */     
/* 150 */     if (decoder.op == decoder.output.length) {
/* 151 */       return decoder.output;
/*     */     }
/*     */ 
/*     */     
/* 155 */     byte[] temp = new byte[decoder.op];
/* 156 */     System.arraycopy(decoder.output, 0, temp, 0, decoder.op);
/* 157 */     return temp;
/*     */   }
/*     */ 
/*     */   
/*     */   static class Decoder
/*     */     extends Coder
/*     */   {
/* 164 */     private static final int[] DECODE = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
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
/* 186 */     private static final int[] DECODE_WEBSAFE = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private static final int SKIP = -1;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private static final int EQUALS = -2;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private int state;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private int value;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private final int[] alphabet;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Decoder(int flags, byte[] output) {
/* 220 */       this.output = output;
/* 221 */       this.alphabet = ((flags & 0x8) == 0) ? DECODE : DECODE_WEBSAFE;
/* 222 */       this.state = 0;
/* 223 */       this.value = 0;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public int maxOutputSize(int len) {
/* 230 */       return len * 3 / 4 + 10;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public boolean process(byte[] input, int offset, int len, boolean finish) {
/* 239 */       if (this.state == 6) return false; 
/* 240 */       int p = offset;
/* 241 */       len += offset;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 247 */       int state = this.state;
/* 248 */       int value = this.value;
/* 249 */       int op = 0;
/* 250 */       byte[] output = this.output;
/* 251 */       int[] alphabet = this.alphabet;
/* 252 */       while (p < len) {
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
/* 267 */         if (state == 0) {
/* 268 */           while (p + 4 <= len && (value = alphabet[input[p] & 0xFF] << 18 | alphabet[input[p + 1] & 0xFF] << 12 | alphabet[input[p + 2] & 0xFF] << 6 | alphabet[input[p + 3] & 0xFF]) >= 0) {
/*     */ 
/*     */ 
/*     */ 
/*     */             
/* 273 */             output[op + 2] = (byte)value;
/* 274 */             output[op + 1] = (byte)(value >> 8);
/* 275 */             output[op] = (byte)(value >> 16);
/* 276 */             op += 3;
/* 277 */             p += 4;
/*     */           } 
/* 279 */           if (p >= len) {
/*     */             break;
/*     */           }
/*     */         } 
/*     */ 
/*     */         
/* 285 */         int d = alphabet[input[p++] & 0xFF];
/* 286 */         switch (state) {
/*     */           case 0:
/* 288 */             if (d >= 0) {
/* 289 */               value = d;
/* 290 */               state++; continue;
/* 291 */             }  if (d != -1) {
/* 292 */               this.state = 6;
/* 293 */               return false;
/*     */             } 
/*     */           
/*     */           case 1:
/* 297 */             if (d >= 0) {
/* 298 */               value = value << 6 | d;
/* 299 */               state++; continue;
/* 300 */             }  if (d != -1) {
/* 301 */               this.state = 6;
/* 302 */               return false;
/*     */             } 
/*     */           
/*     */           case 2:
/* 306 */             if (d >= 0) {
/* 307 */               value = value << 6 | d;
/* 308 */               state++; continue;
/* 309 */             }  if (d == -2) {
/*     */ 
/*     */               
/* 312 */               output[op++] = (byte)(value >> 4);
/* 313 */               state = 4; continue;
/* 314 */             }  if (d != -1) {
/* 315 */               this.state = 6;
/* 316 */               return false;
/*     */             } 
/*     */           
/*     */           case 3:
/* 320 */             if (d >= 0) {
/*     */               
/* 322 */               value = value << 6 | d;
/* 323 */               output[op + 2] = (byte)value;
/* 324 */               output[op + 1] = (byte)(value >> 8);
/* 325 */               output[op] = (byte)(value >> 16);
/* 326 */               op += 3;
/* 327 */               state = 0; continue;
/* 328 */             }  if (d == -2) {
/*     */ 
/*     */               
/* 331 */               output[op + 1] = (byte)(value >> 2);
/* 332 */               output[op] = (byte)(value >> 10);
/* 333 */               op += 2;
/* 334 */               state = 5; continue;
/* 335 */             }  if (d != -1) {
/* 336 */               this.state = 6;
/* 337 */               return false;
/*     */             } 
/*     */           
/*     */           case 4:
/* 341 */             if (d == -2) {
/* 342 */               state++; continue;
/* 343 */             }  if (d != -1) {
/* 344 */               this.state = 6;
/* 345 */               return false;
/*     */             } 
/*     */           
/*     */           case 5:
/* 349 */             if (d != -1) {
/* 350 */               this.state = 6;
/* 351 */               return false;
/*     */             } 
/*     */         } 
/*     */       
/*     */       } 
/* 356 */       if (!finish) {
/*     */ 
/*     */         
/* 359 */         this.state = state;
/* 360 */         this.value = value;
/* 361 */         this.op = op;
/* 362 */         return true;
/*     */       } 
/*     */ 
/*     */       
/* 366 */       switch (state) {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         case 1:
/* 373 */           this.state = 6;
/* 374 */           return false;
/*     */ 
/*     */         
/*     */         case 2:
/* 378 */           output[op++] = (byte)(value >> 4);
/*     */           break;
/*     */ 
/*     */         
/*     */         case 3:
/* 383 */           output[op++] = (byte)(value >> 10);
/* 384 */           output[op++] = (byte)(value >> 2);
/*     */           break;
/*     */         
/*     */         case 4:
/* 388 */           this.state = 6;
/* 389 */           return false;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 395 */       this.state = state;
/* 396 */       this.op = op;
/* 397 */       return true;
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
/*     */   public static String encodeToString(byte[] input, int flags) {
/*     */     try {
/* 414 */       return new String(encode(input, flags), "US-ASCII");
/* 415 */     } catch (UnsupportedEncodingException e) {
/*     */       
/* 417 */       throw new AssertionError(e);
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
/*     */   public static String encodeToString(byte[] input, int offset, int len, int flags) {
/*     */     try {
/* 434 */       return new String(encode(input, offset, len, flags), "US-ASCII");
/* 435 */     } catch (UnsupportedEncodingException e) {
/*     */       
/* 437 */       throw new AssertionError(e);
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
/*     */   public static byte[] encode(byte[] input, int flags) {
/* 450 */     return encode(input, 0, input.length, flags);
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
/*     */   public static byte[] encode(byte[] input, int offset, int len, int flags) {
/* 465 */     Encoder encoder = new Encoder(flags, null);
/*     */     
/* 467 */     int output_len = len / 3 * 4;
/*     */     
/* 469 */     if (encoder.do_padding) {
/* 470 */       if (len % 3 > 0) {
/* 471 */         output_len += 4;
/*     */       }
/*     */     } else {
/* 474 */       switch (len % 3) {
/*     */         case 1:
/* 476 */           output_len += 2; break;
/* 477 */         case 2: output_len += 3;
/*     */           break;
/*     */       } 
/*     */     } 
/* 481 */     if (encoder.do_newline && len > 0) {
/* 482 */       output_len += ((len - 1) / 57 + 1) * (encoder.do_cr ? 2 : 1);
/*     */     }
/*     */     
/* 485 */     encoder.output = new byte[output_len];
/* 486 */     encoder.process(input, offset, len, true);
/* 487 */     assert encoder.op == output_len;
/* 488 */     return encoder.output;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static class Encoder
/*     */     extends Coder
/*     */   {
/*     */     public static final int LINE_GROUPS = 19;
/*     */ 
/*     */ 
/*     */     
/* 501 */     private static final byte[] ENCODE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 511 */     private static final byte[] ENCODE_WEBSAFE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95 };
/*     */     
/*     */     private final byte[] tail;
/*     */     
/*     */     int tailLen;
/*     */     
/*     */     private int count;
/*     */     
/*     */     public final boolean do_padding;
/*     */     public final boolean do_newline;
/*     */     public final boolean do_cr;
/*     */     private final byte[] alphabet;
/*     */     
/*     */     public Encoder(int flags, byte[] output) {
/* 525 */       this.output = output;
/* 526 */       this.do_padding = ((flags & 0x1) == 0);
/* 527 */       this.do_newline = ((flags & 0x2) == 0);
/* 528 */       this.do_cr = ((flags & 0x4) != 0);
/* 529 */       this.alphabet = ((flags & 0x8) == 0) ? ENCODE : ENCODE_WEBSAFE;
/* 530 */       this.tail = new byte[2];
/* 531 */       this.tailLen = 0;
/* 532 */       this.count = this.do_newline ? 19 : -1;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public int maxOutputSize(int len) {
/* 539 */       return len * 8 / 5 + 10;
/*     */     }
/*     */     
/*     */     public boolean process(byte[] input, int offset, int len, boolean finish) {
/* 543 */       byte[] alphabet = this.alphabet;
/* 544 */       byte[] output = this.output;
/* 545 */       int op = 0;
/* 546 */       int count = this.count;
/* 547 */       int p = offset;
/* 548 */       len += offset;
/* 549 */       int v = -1;
/*     */ 
/*     */ 
/*     */       
/* 553 */       switch (this.tailLen) {
/*     */ 
/*     */ 
/*     */         
/*     */         case 1:
/* 558 */           if (p + 2 <= len) {
/*     */ 
/*     */             
/* 561 */             v = (this.tail[0] & 0xFF) << 16 | (input[p++] & 0xFF) << 8 | input[p++] & 0xFF;
/*     */ 
/*     */             
/* 564 */             this.tailLen = 0;
/*     */           } 
/*     */           break;
/*     */         case 2:
/* 568 */           if (p + 1 <= len) {
/*     */             
/* 570 */             v = (this.tail[0] & 0xFF) << 16 | (this.tail[1] & 0xFF) << 8 | input[p++] & 0xFF;
/*     */ 
/*     */             
/* 573 */             this.tailLen = 0;
/*     */           } 
/*     */           break;
/*     */       } 
/* 577 */       if (v != -1) {
/* 578 */         output[op++] = alphabet[v >> 18 & 0x3F];
/* 579 */         output[op++] = alphabet[v >> 12 & 0x3F];
/* 580 */         output[op++] = alphabet[v >> 6 & 0x3F];
/* 581 */         output[op++] = alphabet[v & 0x3F];
/* 582 */         if (--count == 0) {
/* 583 */           if (this.do_cr) output[op++] = 13; 
/* 584 */           output[op++] = 10;
/* 585 */           count = 19;
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 592 */       while (p + 3 <= len) {
/* 593 */         v = (input[p] & 0xFF) << 16 | (input[p + 1] & 0xFF) << 8 | input[p + 2] & 0xFF;
/*     */ 
/*     */         
/* 596 */         output[op] = alphabet[v >> 18 & 0x3F];
/* 597 */         output[op + 1] = alphabet[v >> 12 & 0x3F];
/* 598 */         output[op + 2] = alphabet[v >> 6 & 0x3F];
/* 599 */         output[op + 3] = alphabet[v & 0x3F];
/* 600 */         p += 3;
/* 601 */         op += 4;
/* 602 */         if (--count == 0) {
/* 603 */           if (this.do_cr) output[op++] = 13; 
/* 604 */           output[op++] = 10;
/* 605 */           count = 19;
/*     */         } 
/*     */       } 
/* 608 */       if (finish) {
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 613 */         if (p - this.tailLen == len - 1) {
/* 614 */           int t = 0;
/* 615 */           v = (((this.tailLen > 0) ? this.tail[t++] : input[p++]) & 0xFF) << 4;
/* 616 */           this.tailLen -= t;
/* 617 */           output[op++] = alphabet[v >> 6 & 0x3F];
/* 618 */           output[op++] = alphabet[v & 0x3F];
/* 619 */           if (this.do_padding) {
/* 620 */             output[op++] = 61;
/* 621 */             output[op++] = 61;
/*     */           } 
/* 623 */           if (this.do_newline) {
/* 624 */             if (this.do_cr) output[op++] = 13; 
/* 625 */             output[op++] = 10;
/*     */           } 
/* 627 */         } else if (p - this.tailLen == len - 2) {
/* 628 */           int t = 0;
/* 629 */           v = (((this.tailLen > 1) ? this.tail[t++] : input[p++]) & 0xFF) << 10 | (((this.tailLen > 0) ? this.tail[t++] : input[p++]) & 0xFF) << 2;
/*     */           
/* 631 */           this.tailLen -= t;
/* 632 */           output[op++] = alphabet[v >> 12 & 0x3F];
/* 633 */           output[op++] = alphabet[v >> 6 & 0x3F];
/* 634 */           output[op++] = alphabet[v & 0x3F];
/* 635 */           if (this.do_padding) {
/* 636 */             output[op++] = 61;
/*     */           }
/* 638 */           if (this.do_newline) {
/* 639 */             if (this.do_cr) output[op++] = 13; 
/* 640 */             output[op++] = 10;
/*     */           } 
/* 642 */         } else if (this.do_newline && op > 0 && count != 19) {
/* 643 */           if (this.do_cr) output[op++] = 13; 
/* 644 */           output[op++] = 10;
/*     */         } 
/* 646 */         assert this.tailLen == 0;
/* 647 */         assert p == len;
/*     */ 
/*     */       
/*     */       }
/* 651 */       else if (p == len - 1) {
/* 652 */         this.tail[this.tailLen++] = input[p];
/* 653 */       } else if (p == len - 2) {
/* 654 */         this.tail[this.tailLen++] = input[p];
/* 655 */         this.tail[this.tailLen++] = input[p + 1];
/*     */       } 
/*     */       
/* 658 */       this.op = op;
/* 659 */       this.count = count;
/* 660 */       return true;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\parser\Base64.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */