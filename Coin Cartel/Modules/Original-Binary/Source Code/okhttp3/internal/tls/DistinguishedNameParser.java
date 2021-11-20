/*     */ package okhttp3.internal.tls;
/*     */ 
/*     */ import javax.security.auth.x500.X500Principal;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class DistinguishedNameParser
/*     */ {
/*     */   private final String dn;
/*     */   private final int length;
/*     */   private int pos;
/*     */   private int beg;
/*     */   private int end;
/*     */   private int cur;
/*     */   private char[] chars;
/*     */   
/*     */   DistinguishedNameParser(X500Principal principal) {
/*  42 */     this.dn = principal.getName("RFC2253");
/*  43 */     this.length = this.dn.length();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private String nextAT() {
/*  50 */     for (; this.pos < this.length && this.chars[this.pos] == ' '; this.pos++);
/*     */     
/*  52 */     if (this.pos == this.length) {
/*  53 */       return null;
/*     */     }
/*     */ 
/*     */     
/*  57 */     this.beg = this.pos;
/*     */ 
/*     */     
/*  60 */     this.pos++;
/*  61 */     for (; this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] != ' '; this.pos++);
/*     */ 
/*     */ 
/*     */     
/*  65 */     if (this.pos >= this.length) {
/*  66 */       throw new IllegalStateException("Unexpected end of DN: " + this.dn);
/*     */     }
/*     */ 
/*     */     
/*  70 */     this.end = this.pos;
/*     */ 
/*     */ 
/*     */     
/*  74 */     if (this.chars[this.pos] == ' ') {
/*  75 */       for (; this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] == ' '; this.pos++);
/*     */ 
/*     */       
/*  78 */       if (this.chars[this.pos] != '=' || this.pos == this.length) {
/*  79 */         throw new IllegalStateException("Unexpected end of DN: " + this.dn);
/*     */       }
/*     */     } 
/*     */     
/*  83 */     this.pos++;
/*     */ 
/*     */ 
/*     */     
/*  87 */     for (; this.pos < this.length && this.chars[this.pos] == ' '; this.pos++);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  92 */     if (this.end - this.beg > 4 && this.chars[this.beg + 3] == '.' && (this.chars[this.beg] == 'O' || this.chars[this.beg] == 'o') && (this.chars[this.beg + 1] == 'I' || this.chars[this.beg + 1] == 'i') && (this.chars[this.beg + 2] == 'D' || this.chars[this.beg + 2] == 'd'))
/*     */     {
/*     */ 
/*     */       
/*  96 */       this.beg += 4;
/*     */     }
/*     */     
/*  99 */     return new String(this.chars, this.beg, this.end - this.beg);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private String quotedAV() {
/* 105 */     this.beg = ++this.pos;
/* 106 */     this.end = this.beg;
/*     */     
/*     */     while (true) {
/* 109 */       if (this.pos == this.length) {
/* 110 */         throw new IllegalStateException("Unexpected end of DN: " + this.dn);
/*     */       }
/*     */       
/* 113 */       if (this.chars[this.pos] == '"') {
/*     */         
/* 115 */         this.pos++; break;
/*     */       } 
/* 117 */       if (this.chars[this.pos] == '\\') {
/* 118 */         this.chars[this.end] = getEscaped();
/*     */       } else {
/*     */         
/* 121 */         this.chars[this.end] = this.chars[this.pos];
/*     */       } 
/* 123 */       this.pos++;
/* 124 */       this.end++;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 129 */     for (; this.pos < this.length && this.chars[this.pos] == ' '; this.pos++);
/*     */ 
/*     */     
/* 132 */     return new String(this.chars, this.beg, this.end - this.beg);
/*     */   }
/*     */ 
/*     */   
/*     */   private String hexAV() {
/* 137 */     if (this.pos + 4 >= this.length)
/*     */     {
/* 139 */       throw new IllegalStateException("Unexpected end of DN: " + this.dn);
/*     */     }
/*     */     
/* 142 */     this.beg = this.pos;
/* 143 */     this.pos++;
/*     */ 
/*     */ 
/*     */     
/*     */     while (true) {
/* 148 */       if (this.pos == this.length || this.chars[this.pos] == '+' || this.chars[this.pos] == ',' || this.chars[this.pos] == ';') {
/*     */         
/* 150 */         this.end = this.pos;
/*     */         
/*     */         break;
/*     */       } 
/* 154 */       if (this.chars[this.pos] == ' ') {
/* 155 */         this.end = this.pos;
/* 156 */         this.pos++;
/*     */ 
/*     */         
/* 159 */         for (; this.pos < this.length && this.chars[this.pos] == ' '; this.pos++);
/*     */         break;
/*     */       } 
/* 162 */       if (this.chars[this.pos] >= 'A' && this.chars[this.pos] <= 'F') {
/* 163 */         this.chars[this.pos] = (char)(this.chars[this.pos] + 32);
/*     */       }
/*     */       
/* 166 */       this.pos++;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 171 */     int hexLen = this.end - this.beg;
/* 172 */     if (hexLen < 5 || (hexLen & 0x1) == 0) {
/* 173 */       throw new IllegalStateException("Unexpected end of DN: " + this.dn);
/*     */     }
/*     */ 
/*     */     
/* 177 */     byte[] encoded = new byte[hexLen / 2];
/* 178 */     for (int i = 0, p = this.beg + 1; i < encoded.length; p += 2, i++) {
/* 179 */       encoded[i] = (byte)getByte(p);
/*     */     }
/*     */     
/* 182 */     return new String(this.chars, this.beg, hexLen);
/*     */   }
/*     */ 
/*     */   
/*     */   private String escapedAV() {
/* 187 */     this.beg = this.pos;
/* 188 */     this.end = this.pos;
/*     */     while (true) {
/* 190 */       if (this.pos >= this.length)
/*     */       {
/* 192 */         return new String(this.chars, this.beg, this.end - this.beg);
/*     */       }
/*     */       
/* 195 */       switch (this.chars[this.pos]) {
/*     */         
/*     */         case '+':
/*     */         case ',':
/*     */         case ';':
/* 200 */           return new String(this.chars, this.beg, this.end - this.beg);
/*     */         
/*     */         case '\\':
/* 203 */           this.chars[this.end++] = getEscaped();
/* 204 */           this.pos++;
/*     */           continue;
/*     */ 
/*     */         
/*     */         case ' ':
/* 209 */           this.cur = this.end;
/*     */           
/* 211 */           this.pos++;
/* 212 */           this.chars[this.end++] = ' ';
/*     */           
/* 214 */           for (; this.pos < this.length && this.chars[this.pos] == ' '; this.pos++) {
/* 215 */             this.chars[this.end++] = ' ';
/*     */           }
/* 217 */           if (this.pos == this.length || this.chars[this.pos] == ',' || this.chars[this.pos] == '+' || this.chars[this.pos] == ';')
/*     */           {
/*     */             
/* 220 */             return new String(this.chars, this.beg, this.cur - this.beg);
/*     */           }
/*     */           continue;
/*     */       } 
/* 224 */       this.chars[this.end++] = this.chars[this.pos];
/* 225 */       this.pos++;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private char getEscaped() {
/* 232 */     this.pos++;
/* 233 */     if (this.pos == this.length) {
/* 234 */       throw new IllegalStateException("Unexpected end of DN: " + this.dn);
/*     */     }
/*     */     
/* 237 */     switch (this.chars[this.pos]) {
/*     */       
/*     */       case ' ':
/*     */       case '"':
/*     */       case '#':
/*     */       case '%':
/*     */       case '*':
/*     */       case '+':
/*     */       case ',':
/*     */       case ';':
/*     */       case '<':
/*     */       case '=':
/*     */       case '>':
/*     */       case '\\':
/*     */       case '_':
/* 252 */         return this.chars[this.pos];
/*     */     } 
/*     */ 
/*     */     
/* 256 */     return getUTF8();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private char getUTF8() {
/* 263 */     int res = getByte(this.pos);
/* 264 */     this.pos++;
/*     */     
/* 266 */     if (res < 128)
/* 267 */       return (char)res; 
/* 268 */     if (res >= 192 && res <= 247) {
/*     */       int count;
/*     */       
/* 271 */       if (res <= 223) {
/* 272 */         count = 1;
/* 273 */         res &= 0x1F;
/* 274 */       } else if (res <= 239) {
/* 275 */         count = 2;
/* 276 */         res &= 0xF;
/*     */       } else {
/* 278 */         count = 3;
/* 279 */         res &= 0x7;
/*     */       } 
/*     */ 
/*     */       
/* 283 */       for (int i = 0; i < count; i++) {
/* 284 */         this.pos++;
/* 285 */         if (this.pos == this.length || this.chars[this.pos] != '\\') {
/* 286 */           return '?';
/*     */         }
/* 288 */         this.pos++;
/*     */         
/* 290 */         int b = getByte(this.pos);
/* 291 */         this.pos++;
/* 292 */         if ((b & 0xC0) != 128) {
/* 293 */           return '?';
/*     */         }
/*     */         
/* 296 */         res = (res << 6) + (b & 0x3F);
/*     */       } 
/* 298 */       return (char)res;
/*     */     } 
/* 300 */     return '?';
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int getByte(int position) {
/* 311 */     if (position + 1 >= this.length) {
/* 312 */       throw new IllegalStateException("Malformed DN: " + this.dn);
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 317 */     int b1 = this.chars[position];
/* 318 */     if (b1 >= 48 && b1 <= 57) {
/* 319 */       b1 -= 48;
/* 320 */     } else if (b1 >= 97 && b1 <= 102) {
/* 321 */       b1 -= 87;
/* 322 */     } else if (b1 >= 65 && b1 <= 70) {
/* 323 */       b1 -= 55;
/*     */     } else {
/* 325 */       throw new IllegalStateException("Malformed DN: " + this.dn);
/*     */     } 
/*     */     
/* 328 */     int b2 = this.chars[position + 1];
/* 329 */     if (b2 >= 48 && b2 <= 57) {
/* 330 */       b2 -= 48;
/* 331 */     } else if (b2 >= 97 && b2 <= 102) {
/* 332 */       b2 -= 87;
/* 333 */     } else if (b2 >= 65 && b2 <= 70) {
/* 334 */       b2 -= 55;
/*     */     } else {
/* 336 */       throw new IllegalStateException("Malformed DN: " + this.dn);
/*     */     } 
/*     */     
/* 339 */     return (b1 << 4) + b2;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String findMostSpecific(String attributeType) {
/* 350 */     this.pos = 0;
/* 351 */     this.beg = 0;
/* 352 */     this.end = 0;
/* 353 */     this.cur = 0;
/* 354 */     this.chars = this.dn.toCharArray();
/*     */     
/* 356 */     String attType = nextAT();
/* 357 */     if (attType == null) {
/* 358 */       return null;
/*     */     }
/*     */     while (true) {
/* 361 */       String attValue = "";
/*     */       
/* 363 */       if (this.pos == this.length) {
/* 364 */         return null;
/*     */       }
/*     */       
/* 367 */       switch (this.chars[this.pos]) {
/*     */         case '"':
/* 369 */           attValue = quotedAV();
/*     */           break;
/*     */         case '#':
/* 372 */           attValue = hexAV();
/*     */           break;
/*     */         
/*     */         case '+':
/*     */         case ',':
/*     */         case ';':
/*     */           break;
/*     */         default:
/* 380 */           attValue = escapedAV();
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 386 */       if (attributeType.equalsIgnoreCase(attType)) {
/* 387 */         return attValue;
/*     */       }
/*     */       
/* 390 */       if (this.pos >= this.length) {
/* 391 */         return null;
/*     */       }
/*     */       
/* 394 */       if (this.chars[this.pos] != ',' && this.chars[this.pos] != ';' && 
/* 395 */         this.chars[this.pos] != '+') {
/* 396 */         throw new IllegalStateException("Malformed DN: " + this.dn);
/*     */       }
/*     */       
/* 399 */       this.pos++;
/* 400 */       attType = nextAT();
/* 401 */       if (attType == null)
/* 402 */         throw new IllegalStateException("Malformed DN: " + this.dn); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\tls\DistinguishedNameParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */