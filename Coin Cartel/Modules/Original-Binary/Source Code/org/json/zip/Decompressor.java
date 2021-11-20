/*     */ package org.json.zip;
/*     */ 
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONObject;
/*     */ import org.json.Kim;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Decompressor
/*     */   extends JSONzip
/*     */ {
/*     */   BitReader bitreader;
/*     */   
/*     */   public Decompressor(BitReader bitreader) {
/*  57 */     this.bitreader = bitreader;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean bit() throws JSONException {
/*     */     try {
/*  69 */       boolean value = this.bitreader.bit();
/*     */ 
/*     */ 
/*     */       
/*  73 */       return value;
/*  74 */     } catch (Throwable e) {
/*  75 */       throw new JSONException(e);
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
/*     */   private Object getAndTick(Keep keep, BitReader bitreader) throws JSONException {
/*     */     try {
/*  92 */       int width = keep.bitsize();
/*  93 */       int integer = bitreader.read(width);
/*  94 */       Object value = keep.value(integer);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  99 */       if (integer >= keep.length) {
/* 100 */         throw new JSONException("Deep error.");
/*     */       }
/* 102 */       keep.tick(integer);
/* 103 */       return value;
/* 104 */     } catch (Throwable e) {
/* 105 */       throw new JSONException(e);
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
/*     */   public boolean pad(int factor) throws JSONException {
/*     */     try {
/* 119 */       return this.bitreader.pad(factor);
/* 120 */     } catch (Throwable e) {
/* 121 */       throw new JSONException(e);
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
/*     */   private int read(int width) throws JSONException {
/*     */     try {
/* 135 */       int value = this.bitreader.read(width);
/*     */ 
/*     */ 
/*     */       
/* 139 */       return value;
/* 140 */     } catch (Throwable e) {
/* 141 */       throw new JSONException(e);
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
/*     */   private JSONArray readArray(boolean stringy) throws JSONException {
/* 154 */     JSONArray jsonarray = new JSONArray();
/* 155 */     jsonarray.put(stringy ? readString() : readValue());
/*     */ 
/*     */ 
/*     */     
/*     */     while (true) {
/* 160 */       while (!bit()) {
/* 161 */         if (!bit()) {
/* 162 */           return jsonarray;
/*     */         }
/* 164 */         jsonarray.put(stringy ? readValue() : readString());
/*     */       } 
/* 166 */       jsonarray.put(stringy ? readString() : readValue());
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
/*     */   private Object readJSON() throws JSONException {
/* 178 */     switch (read(3)) {
/*     */       case 5:
/* 180 */         return readObject();
/*     */       case 6:
/* 182 */         return readArray(true);
/*     */       case 7:
/* 184 */         return readArray(false);
/*     */       case 0:
/* 186 */         return new JSONObject();
/*     */       case 1:
/* 188 */         return new JSONArray();
/*     */       case 2:
/* 190 */         return Boolean.TRUE;
/*     */       case 3:
/* 192 */         return Boolean.FALSE;
/*     */     } 
/* 194 */     return JSONObject.NULL;
/*     */   }
/*     */ 
/*     */   
/*     */   private String readName() throws JSONException {
/* 199 */     byte[] bytes = new byte[65536];
/* 200 */     int length = 0;
/* 201 */     if (!bit()) {
/*     */       while (true) {
/* 203 */         int c = this.namehuff.read(this.bitreader);
/* 204 */         if (c == 256) {
/*     */           break;
/*     */         }
/* 207 */         bytes[length] = (byte)c;
/* 208 */         length++;
/*     */       } 
/* 210 */       if (length == 0) {
/* 211 */         return "";
/*     */       }
/* 213 */       Kim kim = new Kim(bytes, length);
/* 214 */       this.namekeep.register(kim);
/* 215 */       return kim.toString();
/*     */     } 
/* 217 */     return getAndTick(this.namekeep, this.bitreader).toString();
/*     */   }
/*     */   
/*     */   private JSONObject readObject() throws JSONException {
/* 221 */     JSONObject jsonobject = new JSONObject();
/*     */ 
/*     */ 
/*     */     
/*     */     while (true) {
/* 226 */       String name = readName();
/* 227 */       jsonobject.put(name, !bit() ? readString() : readValue());
/* 228 */       if (!bit()) {
/* 229 */         return jsonobject;
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private String readString() throws JSONException {
/* 236 */     int from = 0;
/* 237 */     int thru = 0;
/* 238 */     int previousFrom = -1;
/* 239 */     int previousThru = 0;
/* 240 */     if (bit()) {
/* 241 */       return getAndTick(this.stringkeep, this.bitreader).toString();
/*     */     }
/* 243 */     byte[] bytes = new byte[65536];
/* 244 */     boolean one = bit();
/* 245 */     this.substringkeep.reserve();
/*     */     while (true) {
/* 247 */       while (one) {
/* 248 */         from = thru;
/* 249 */         Kim kim1 = (Kim)getAndTick(this.substringkeep, this.bitreader);
/* 250 */         thru = kim1.copy(bytes, from);
/* 251 */         if (previousFrom != -1) {
/* 252 */           this.substringkeep.registerOne(new Kim(bytes, previousFrom, previousThru + 1));
/*     */         }
/*     */         
/* 255 */         previousFrom = from;
/* 256 */         previousThru = thru;
/* 257 */         one = bit();
/*     */       } 
/* 259 */       from = -1;
/*     */       while (true) {
/* 261 */         int c = this.substringhuff.read(this.bitreader);
/* 262 */         if (c == 256) {
/*     */           break;
/*     */         }
/* 265 */         bytes[thru] = (byte)c;
/* 266 */         thru++;
/* 267 */         if (previousFrom != -1) {
/* 268 */           this.substringkeep.registerOne(new Kim(bytes, previousFrom, previousThru + 1));
/*     */           
/* 270 */           previousFrom = -1;
/*     */         } 
/*     */       } 
/* 273 */       if (!bit()) {
/*     */         break;
/*     */       }
/* 276 */       one = true;
/*     */     } 
/*     */     
/* 279 */     if (thru == 0) {
/* 280 */       return "";
/*     */     }
/* 282 */     Kim kim = new Kim(bytes, thru);
/* 283 */     this.stringkeep.register(kim);
/* 284 */     this.substringkeep.registerMany(kim);
/* 285 */     return kim.toString(); } private Object readValue() throws JSONException {
/*     */     byte[] bytes;
/*     */     int length;
/*     */     Object value;
/* 289 */     switch (read(2)) {
/*     */       case 0:
/* 291 */         return new Integer(read(!bit() ? 4 : (!bit() ? 7 : 14)));
/*     */       case 1:
/* 293 */         bytes = new byte[256];
/* 294 */         length = 0;
/*     */         while (true) {
/* 296 */           int c = read(4);
/* 297 */           if (c == endOfNumber) {
/*     */             break;
/*     */           }
/* 300 */           bytes[length] = bcd[c];
/* 301 */           length++;
/*     */         } 
/*     */         
/*     */         try {
/* 305 */           value = JSONObject.stringToValue(new String(bytes, 0, length, "US-ASCII"));
/*     */         }
/* 307 */         catch (UnsupportedEncodingException e) {
/* 308 */           throw new JSONException(e);
/*     */         } 
/* 310 */         this.values.register(value);
/* 311 */         return value;
/*     */       case 2:
/* 313 */         return getAndTick(this.values, this.bitreader);
/*     */       case 3:
/* 315 */         return readJSON();
/*     */     } 
/* 317 */     throw new JSONException("Impossible.");
/*     */   }
/*     */ 
/*     */   
/*     */   public Object unzip() throws JSONException {
/* 322 */     begin();
/* 323 */     return readJSON();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\Decompressor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */