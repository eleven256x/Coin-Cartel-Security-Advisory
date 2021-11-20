/*     */ package org.json;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Kim
/*     */ {
/*  69 */   private byte[] bytes = null;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  74 */   private int hashcode = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  80 */   public int length = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  85 */   private String string = null;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Kim(byte[] bytes, int from, int thru) {
/* 102 */     int sum = 1;
/*     */     
/* 104 */     this.hashcode = 0;
/* 105 */     this.length = thru - from;
/* 106 */     if (this.length > 0) {
/* 107 */       this.bytes = new byte[this.length];
/* 108 */       for (int at = 0; at < this.length; at++) {
/* 109 */         int value = bytes[at + from] & 0xFF;
/* 110 */         sum += value;
/* 111 */         this.hashcode += sum;
/* 112 */         this.bytes[at] = (byte)value;
/*     */       } 
/* 114 */       this.hashcode += sum << 16;
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
/*     */   public Kim(byte[] bytes, int length) {
/* 127 */     this(bytes, 0, length);
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
/*     */   public Kim(Kim kim, int from, int thru) {
/* 143 */     this(kim.bytes, from, thru);
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
/*     */   public Kim(String string) throws JSONException {
/* 155 */     int stringLength = string.length();
/* 156 */     this.hashcode = 0;
/* 157 */     this.length = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 162 */     if (stringLength > 0) {
/* 163 */       for (int i = 0; i < stringLength; i++) {
/* 164 */         int c = string.charAt(i);
/* 165 */         if (c <= 127) {
/* 166 */           this.length++;
/* 167 */         } else if (c <= 16383) {
/* 168 */           this.length += 2;
/*     */         } else {
/* 170 */           if (c >= 55296 && c <= 57343) {
/* 171 */             i++;
/* 172 */             int d = string.charAt(i);
/* 173 */             if (c > 56319 || d < 56320 || d > 57343) {
/* 174 */               throw new JSONException("Bad UTF16");
/*     */             }
/*     */           } 
/* 177 */           this.length += 3;
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 184 */       this.bytes = new byte[this.length];
/* 185 */       int at = 0;
/*     */       
/* 187 */       int sum = 1;
/* 188 */       for (int j = 0; j < stringLength; j++) {
/* 189 */         int character = string.charAt(j);
/* 190 */         if (character <= 127) {
/* 191 */           this.bytes[at] = (byte)character;
/* 192 */           sum += character;
/* 193 */           this.hashcode += sum;
/* 194 */           at++;
/* 195 */         } else if (character <= 16383) {
/* 196 */           int b = 0x80 | character >>> 7;
/* 197 */           this.bytes[at] = (byte)b;
/* 198 */           sum += b;
/* 199 */           this.hashcode += sum;
/* 200 */           at++;
/* 201 */           b = character & 0x7F;
/* 202 */           this.bytes[at] = (byte)b;
/* 203 */           sum += b;
/* 204 */           this.hashcode += sum;
/* 205 */           at++;
/*     */         } else {
/* 207 */           if (character >= 55296 && character <= 56319) {
/* 208 */             j++;
/* 209 */             character = ((character & 0x3FF) << 10 | string.charAt(j) & 0x3FF) + 65536;
/*     */           } 
/*     */           
/* 212 */           int b = 0x80 | character >>> 14;
/* 213 */           this.bytes[at] = (byte)b;
/* 214 */           sum += b;
/* 215 */           this.hashcode += sum;
/* 216 */           at++;
/* 217 */           b = 0x80 | character >>> 7 & 0xFF;
/* 218 */           this.bytes[at] = (byte)b;
/* 219 */           sum += b;
/* 220 */           this.hashcode += sum;
/* 221 */           at++;
/* 222 */           b = character & 0x7F;
/* 223 */           this.bytes[at] = (byte)b;
/* 224 */           sum += b;
/* 225 */           this.hashcode += sum;
/* 226 */           at++;
/*     */         } 
/*     */       } 
/* 229 */       this.hashcode += sum << 16;
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
/*     */   public int characterAt(int at) throws JSONException {
/* 245 */     int c = get(at);
/* 246 */     if ((c & 0x80) == 0) {
/* 247 */       return c;
/*     */     }
/*     */     
/* 250 */     int c1 = get(at + 1);
/* 251 */     if ((c1 & 0x80) == 0) {
/* 252 */       int character = (c & 0x7F) << 7 | c1;
/* 253 */       if (character > 127) {
/* 254 */         return character;
/*     */       }
/*     */     } else {
/* 257 */       int c2 = get(at + 2);
/* 258 */       int character = (c & 0x7F) << 14 | (c1 & 0x7F) << 7 | c2;
/* 259 */       if ((c2 & 0x80) == 0 && character > 16383 && character <= 1114111 && (character < 55296 || character > 57343))
/*     */       {
/* 261 */         return character;
/*     */       }
/*     */     } 
/* 264 */     throw new JSONException("Bad character at " + at);
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
/*     */   public static int characterSize(int character) throws JSONException {
/* 278 */     if (character < 0 || character > 1114111) {
/* 279 */       throw new JSONException("Bad character " + character);
/*     */     }
/* 281 */     return (character <= 127) ? 1 : ((character <= 16383) ? 2 : 3);
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
/*     */   public int copy(byte[] bytes, int at) {
/* 294 */     System.arraycopy(this.bytes, 0, bytes, at, this.length);
/* 295 */     return at + this.length;
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
/*     */   public boolean equals(Object obj) {
/* 308 */     if (!(obj instanceof Kim)) {
/* 309 */       return false;
/*     */     }
/* 311 */     Kim that = (Kim)obj;
/* 312 */     if (this == that) {
/* 313 */       return true;
/*     */     }
/* 315 */     if (this.hashcode != that.hashcode) {
/* 316 */       return false;
/*     */     }
/* 318 */     return Arrays.equals(this.bytes, that.bytes);
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
/*     */   public int get(int at) throws JSONException {
/* 330 */     if (at < 0 || at > this.length) {
/* 331 */       throw new JSONException("Bad character at " + at);
/*     */     }
/* 333 */     return this.bytes[at] & 0xFF;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int hashCode() {
/* 340 */     return this.hashcode;
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
/*     */   public String toString() throws JSONException {
/* 353 */     if (this.string == null) {
/*     */       
/* 355 */       int length = 0;
/* 356 */       char[] chars = new char[this.length];
/* 357 */       for (int at = 0; at < this.length; at += characterSize(c)) {
/* 358 */         int c = characterAt(at);
/* 359 */         if (c < 65536) {
/* 360 */           chars[length] = (char)c;
/* 361 */           length++;
/*     */         } else {
/* 363 */           chars[length] = (char)(0xD800 | c - 65536 >>> 10);
/* 364 */           length++;
/* 365 */           chars[length] = (char)(0xDC00 | c & 0x3FF);
/* 366 */           length++;
/*     */         } 
/*     */       } 
/* 369 */       this.string = new String(chars, 0, length);
/*     */     } 
/* 371 */     return this.string;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\Kim.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */