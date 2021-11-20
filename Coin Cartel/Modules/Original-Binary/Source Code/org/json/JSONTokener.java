/*     */ package org.json;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.io.StringReader;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class JSONTokener
/*     */ {
/*     */   private long character;
/*     */   private boolean eof;
/*     */   private long index;
/*     */   private long line;
/*     */   private char previous;
/*     */   private Reader reader;
/*     */   private boolean usePrevious;
/*     */   
/*     */   public JSONTokener(Reader reader) {
/*  58 */     this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
/*     */ 
/*     */     
/*  61 */     this.eof = false;
/*  62 */     this.usePrevious = false;
/*  63 */     this.previous = Character.MIN_VALUE;
/*  64 */     this.index = 0L;
/*  65 */     this.character = 1L;
/*  66 */     this.line = 1L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONTokener(InputStream inputStream) throws JSONException {
/*  74 */     this(new InputStreamReader(inputStream));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONTokener(String s) {
/*  84 */     this(new StringReader(s));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void back() throws JSONException {
/*  94 */     if (this.usePrevious || this.index <= 0L) {
/*  95 */       throw new JSONException("Stepping back two steps is not supported");
/*     */     }
/*  97 */     this.index--;
/*  98 */     this.character--;
/*  99 */     this.usePrevious = true;
/* 100 */     this.eof = false;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static int dehexchar(char c) {
/* 111 */     if (c >= '0' && c <= '9') {
/* 112 */       return c - 48;
/*     */     }
/* 114 */     if (c >= 'A' && c <= 'F') {
/* 115 */       return c - 55;
/*     */     }
/* 117 */     if (c >= 'a' && c <= 'f') {
/* 118 */       return c - 87;
/*     */     }
/* 120 */     return -1;
/*     */   }
/*     */   
/*     */   public boolean end() {
/* 124 */     return (this.eof && !this.usePrevious);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean more() throws JSONException {
/* 134 */     next();
/* 135 */     if (end()) {
/* 136 */       return false;
/*     */     }
/* 138 */     back();
/* 139 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public char next() throws JSONException {
/*     */     int c;
/* 150 */     if (this.usePrevious) {
/* 151 */       this.usePrevious = false;
/* 152 */       c = this.previous;
/*     */     } else {
/*     */       try {
/* 155 */         c = this.reader.read();
/* 156 */       } catch (IOException exception) {
/* 157 */         throw new JSONException(exception);
/*     */       } 
/*     */       
/* 160 */       if (c <= 0) {
/* 161 */         this.eof = true;
/* 162 */         c = 0;
/*     */       } 
/*     */     } 
/* 165 */     this.index++;
/* 166 */     if (this.previous == '\r') {
/* 167 */       this.line++;
/* 168 */       this.character = (c == 10) ? 0L : 1L;
/* 169 */     } else if (c == 10) {
/* 170 */       this.line++;
/* 171 */       this.character = 0L;
/*     */     } else {
/* 173 */       this.character++;
/*     */     } 
/* 175 */     this.previous = (char)c;
/* 176 */     return this.previous;
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
/*     */   public char next(char c) throws JSONException {
/* 188 */     char n = next();
/* 189 */     if (n != c) {
/* 190 */       throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
/*     */     }
/*     */     
/* 193 */     return n;
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
/*     */   public String next(int n) throws JSONException {
/* 207 */     if (n == 0) {
/* 208 */       return "";
/*     */     }
/*     */     
/* 211 */     char[] chars = new char[n];
/* 212 */     int pos = 0;
/*     */     
/* 214 */     while (pos < n) {
/* 215 */       chars[pos] = next();
/* 216 */       if (end()) {
/* 217 */         throw syntaxError("Substring bounds error");
/*     */       }
/* 219 */       pos++;
/*     */     } 
/* 221 */     return new String(chars);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public char nextClean() throws JSONException {
/*     */     char c;
/*     */     do {
/* 232 */       c = next();
/* 233 */     } while (c != '\000' && c <= ' ');
/* 234 */     return c;
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
/*     */   public String nextString(char quote) throws JSONException {
/* 253 */     StringBuffer sb = new StringBuffer();
/*     */     while (true) {
/* 255 */       char c = next();
/* 256 */       switch (c) {
/*     */         case '\000':
/*     */         case '\n':
/*     */         case '\r':
/* 260 */           throw syntaxError("Unterminated string");
/*     */         case '\\':
/* 262 */           c = next();
/* 263 */           switch (c) {
/*     */             case 'b':
/* 265 */               sb.append('\b');
/*     */               continue;
/*     */             case 't':
/* 268 */               sb.append('\t');
/*     */               continue;
/*     */             case 'n':
/* 271 */               sb.append('\n');
/*     */               continue;
/*     */             case 'f':
/* 274 */               sb.append('\f');
/*     */               continue;
/*     */             case 'r':
/* 277 */               sb.append('\r');
/*     */               continue;
/*     */             case 'u':
/* 280 */               sb.append((char)Integer.parseInt(next(4), 16));
/*     */               continue;
/*     */             case '"':
/*     */             case '\'':
/*     */             case '/':
/*     */             case '\\':
/* 286 */               sb.append(c);
/*     */               continue;
/*     */           } 
/* 289 */           throw syntaxError("Illegal escape.");
/*     */       } 
/*     */ 
/*     */       
/* 293 */       if (c == quote) {
/* 294 */         return sb.toString();
/*     */       }
/* 296 */       sb.append(c);
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
/*     */   public String nextTo(char delimiter) throws JSONException {
/* 309 */     StringBuffer sb = new StringBuffer();
/*     */     while (true) {
/* 311 */       char c = next();
/* 312 */       if (c == delimiter || c == '\000' || c == '\n' || c == '\r') {
/* 313 */         if (c != '\000') {
/* 314 */           back();
/*     */         }
/* 316 */         return sb.toString().trim();
/*     */       } 
/* 318 */       sb.append(c);
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
/*     */   public String nextTo(String delimiters) throws JSONException {
/* 331 */     StringBuffer sb = new StringBuffer();
/*     */     while (true) {
/* 333 */       char c = next();
/* 334 */       if (delimiters.indexOf(c) >= 0 || c == '\000' || c == '\n' || c == '\r') {
/*     */         
/* 336 */         if (c != '\000') {
/* 337 */           back();
/*     */         }
/* 339 */         return sb.toString().trim();
/*     */       } 
/* 341 */       sb.append(c);
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
/*     */   public Object nextValue() throws JSONException {
/* 354 */     char c = nextClean();
/*     */ 
/*     */     
/* 357 */     switch (c) {
/*     */       case '"':
/*     */       case '\'':
/* 360 */         return nextString(c);
/*     */       case '{':
/* 362 */         back();
/* 363 */         return new JSONObject(this);
/*     */       case '[':
/* 365 */         back();
/* 366 */         return new JSONArray(this);
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 378 */     StringBuffer sb = new StringBuffer();
/* 379 */     while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
/* 380 */       sb.append(c);
/* 381 */       c = next();
/*     */     } 
/* 383 */     back();
/*     */     
/* 385 */     String string = sb.toString().trim();
/* 386 */     if ("".equals(string)) {
/* 387 */       throw syntaxError("Missing value");
/*     */     }
/* 389 */     return JSONObject.stringToValue(string);
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
/*     */   public char skipTo(char to) throws JSONException {
/*     */     char c;
/*     */     try {
/* 403 */       long startIndex = this.index;
/* 404 */       long startCharacter = this.character;
/* 405 */       long startLine = this.line;
/* 406 */       this.reader.mark(1000000);
/*     */       do {
/* 408 */         c = next();
/* 409 */         if (c == '\000') {
/* 410 */           this.reader.reset();
/* 411 */           this.index = startIndex;
/* 412 */           this.character = startCharacter;
/* 413 */           this.line = startLine;
/* 414 */           return c;
/*     */         } 
/* 416 */       } while (c != to);
/* 417 */     } catch (IOException exc) {
/* 418 */       throw new JSONException(exc);
/*     */     } 
/*     */     
/* 421 */     back();
/* 422 */     return c;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONException syntaxError(String message) {
/* 433 */     return new JSONException(message + toString());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 443 */     return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\JSONTokener.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */