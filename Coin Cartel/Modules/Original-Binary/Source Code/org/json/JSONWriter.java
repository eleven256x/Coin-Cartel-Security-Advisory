/*     */ package org.json;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class JSONWriter
/*     */ {
/*     */   private static final int maxdepth = 200;
/*     */   private boolean comma;
/*     */   protected char mode;
/*     */   private final JSONObject[] stack;
/*     */   private int top;
/*     */   protected Writer writer;
/*     */   
/*     */   public JSONWriter(Writer w) {
/*  97 */     this.comma = false;
/*  98 */     this.mode = 'i';
/*  99 */     this.stack = new JSONObject[200];
/* 100 */     this.top = 0;
/* 101 */     this.writer = w;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private JSONWriter append(String string) throws JSONException {
/* 111 */     if (string == null) {
/* 112 */       throw new JSONException("Null pointer");
/*     */     }
/* 114 */     if (this.mode == 'o' || this.mode == 'a') {
/*     */       try {
/* 116 */         if (this.comma && this.mode == 'a') {
/* 117 */           this.writer.write(44);
/*     */         }
/* 119 */         this.writer.write(string);
/* 120 */       } catch (IOException e) {
/* 121 */         throw new JSONException(e);
/*     */       } 
/* 123 */       if (this.mode == 'o') {
/* 124 */         this.mode = 'k';
/*     */       }
/* 126 */       this.comma = true;
/* 127 */       return this;
/*     */     } 
/* 129 */     throw new JSONException("Value out of sequence.");
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
/*     */   public JSONWriter array() throws JSONException {
/* 142 */     if (this.mode == 'i' || this.mode == 'o' || this.mode == 'a') {
/* 143 */       push(null);
/* 144 */       append("[");
/* 145 */       this.comma = false;
/* 146 */       return this;
/*     */     } 
/* 148 */     throw new JSONException("Misplaced array.");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private JSONWriter end(char mode, char c) throws JSONException {
/* 159 */     if (this.mode != mode) {
/* 160 */       throw new JSONException((mode == 'a') ? "Misplaced endArray." : "Misplaced endObject.");
/*     */     }
/*     */ 
/*     */     
/* 164 */     pop(mode);
/*     */     try {
/* 166 */       this.writer.write(c);
/* 167 */     } catch (IOException e) {
/* 168 */       throw new JSONException(e);
/*     */     } 
/* 170 */     this.comma = true;
/* 171 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONWriter endArray() throws JSONException {
/* 181 */     return end('a', ']');
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONWriter endObject() throws JSONException {
/* 191 */     return end('k', '}');
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
/*     */   public JSONWriter key(String string) throws JSONException {
/* 203 */     if (string == null) {
/* 204 */       throw new JSONException("Null key.");
/*     */     }
/* 206 */     if (this.mode == 'k') {
/*     */       try {
/* 208 */         this.stack[this.top - 1].putOnce(string, Boolean.TRUE);
/* 209 */         if (this.comma) {
/* 210 */           this.writer.write(44);
/*     */         }
/* 212 */         this.writer.write(JSONObject.quote(string));
/* 213 */         this.writer.write(58);
/* 214 */         this.comma = false;
/* 215 */         this.mode = 'o';
/* 216 */         return this;
/* 217 */       } catch (IOException e) {
/* 218 */         throw new JSONException(e);
/*     */       } 
/*     */     }
/* 221 */     throw new JSONException("Misplaced key.");
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
/*     */   public JSONWriter object() throws JSONException {
/* 235 */     if (this.mode == 'i') {
/* 236 */       this.mode = 'o';
/*     */     }
/* 238 */     if (this.mode == 'o' || this.mode == 'a') {
/* 239 */       append("{");
/* 240 */       push(new JSONObject());
/* 241 */       this.comma = false;
/* 242 */       return this;
/*     */     } 
/* 244 */     throw new JSONException("Misplaced object.");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void pop(char c) throws JSONException {
/* 255 */     if (this.top <= 0) {
/* 256 */       throw new JSONException("Nesting error.");
/*     */     }
/* 258 */     char m = (this.stack[this.top - 1] == null) ? 'a' : 'k';
/* 259 */     if (m != c) {
/* 260 */       throw new JSONException("Nesting error.");
/*     */     }
/* 262 */     this.top--;
/* 263 */     this.mode = (this.top == 0) ? 'd' : ((this.stack[this.top - 1] == null) ? 'a' : 'k');
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
/*     */   private void push(JSONObject jo) throws JSONException {
/* 276 */     if (this.top >= 200) {
/* 277 */       throw new JSONException("Nesting too deep.");
/*     */     }
/* 279 */     this.stack[this.top] = jo;
/* 280 */     this.mode = (jo == null) ? 'a' : 'k';
/* 281 */     this.top++;
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
/*     */   public JSONWriter value(boolean b) throws JSONException {
/* 293 */     return append(b ? "true" : "false");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONWriter value(double d) throws JSONException {
/* 303 */     return value(new Double(d));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONWriter value(long l) throws JSONException {
/* 313 */     return append(Long.toString(l));
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
/*     */   public JSONWriter value(Object object) throws JSONException {
/* 325 */     return append(JSONObject.valueToString(object));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\JSONWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */