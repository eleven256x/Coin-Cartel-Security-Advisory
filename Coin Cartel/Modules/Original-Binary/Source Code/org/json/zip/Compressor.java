/*     */ package org.json.zip;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Compressor
/*     */   extends JSONzip
/*     */ {
/*     */   final BitWriter bitwriter;
/*     */   
/*     */   public Compressor(BitWriter bitwriter) {
/*  69 */     this.bitwriter = bitwriter;
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
/*     */   private static int bcd(char digit) {
/*  82 */     if (digit >= '0' && digit <= '9') {
/*  83 */       return digit - 48;
/*     */     }
/*  85 */     switch (digit) {
/*     */       case '.':
/*  87 */         return 10;
/*     */       case '-':
/*  89 */         return 11;
/*     */       case '+':
/*  91 */         return 12;
/*     */     } 
/*  93 */     return 13;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void flush() throws JSONException {
/* 104 */     pad(8);
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
/*     */   private void one() throws JSONException {
/* 116 */     write(1, 1);
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
/*     */   public void pad(int factor) throws JSONException {
/*     */     try {
/* 130 */       this.bitwriter.pad(factor);
/* 131 */     } catch (Throwable e) {
/* 132 */       throw new JSONException(e);
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
/*     */   private void write(int integer, int width) throws JSONException {
/*     */     try {
/* 147 */       this.bitwriter.write(integer, width);
/*     */ 
/*     */     
/*     */     }
/* 151 */     catch (Throwable e) {
/* 152 */       throw new JSONException(e);
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
/*     */   private void write(int integer, Huff huff) throws JSONException {
/* 167 */     huff.write(integer, this.bitwriter);
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
/*     */   private void write(Kim kim, Huff huff) throws JSONException {
/* 180 */     write(kim, 0, kim.length, huff);
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
/*     */   private void write(Kim kim, int from, int thru, Huff huff) throws JSONException {
/* 198 */     for (int at = from; at < thru; at++) {
/* 199 */       write(kim.get(at), huff);
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
/*     */   private void writeAndTick(int integer, Keep keep) throws JSONException {
/* 214 */     int width = keep.bitsize();
/* 215 */     keep.tick(integer);
/*     */ 
/*     */ 
/*     */     
/* 219 */     write(integer, width);
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
/*     */   private void writeArray(JSONArray jsonarray) throws JSONException {
/* 235 */     boolean stringy = false;
/* 236 */     int length = jsonarray.length();
/* 237 */     if (length == 0) {
/* 238 */       write(1, 3);
/*     */     } else {
/* 240 */       Object value = jsonarray.get(0);
/* 241 */       if (value == null) {
/* 242 */         value = JSONObject.NULL;
/*     */       }
/* 244 */       if (value instanceof String) {
/* 245 */         stringy = true;
/* 246 */         write(6, 3);
/* 247 */         writeString((String)value);
/*     */       } else {
/* 249 */         write(7, 3);
/* 250 */         writeValue(value);
/*     */       } 
/* 252 */       for (int i = 1; i < length; i++) {
/*     */ 
/*     */ 
/*     */         
/* 256 */         value = jsonarray.get(i);
/* 257 */         if (value == null) {
/* 258 */           value = JSONObject.NULL;
/*     */         }
/* 260 */         if (value instanceof String != stringy) {
/* 261 */           zero();
/*     */         }
/* 263 */         one();
/* 264 */         if (value instanceof String) {
/* 265 */           writeString((String)value);
/*     */         } else {
/* 267 */           writeValue(value);
/*     */         } 
/*     */       } 
/* 270 */       zero();
/* 271 */       zero();
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
/*     */   private void writeJSON(Object value) throws JSONException {
/* 286 */     if (JSONObject.NULL.equals(value)) {
/* 287 */       write(4, 3);
/* 288 */     } else if (Boolean.FALSE.equals(value)) {
/* 289 */       write(3, 3);
/* 290 */     } else if (Boolean.TRUE.equals(value)) {
/* 291 */       write(2, 3);
/*     */     } else {
/* 293 */       if (value instanceof Map) {
/* 294 */         value = new JSONObject((Map)value);
/* 295 */       } else if (value instanceof Collection) {
/* 296 */         value = new JSONArray((Collection)value);
/* 297 */       } else if (value.getClass().isArray()) {
/* 298 */         value = new JSONArray(value);
/*     */       } 
/* 300 */       if (value instanceof JSONObject) {
/* 301 */         writeObject((JSONObject)value);
/* 302 */       } else if (value instanceof JSONArray) {
/* 303 */         writeArray((JSONArray)value);
/*     */       } else {
/* 305 */         throw new JSONException("Unrecognized object");
/*     */       } 
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
/*     */   private void writeName(String name) throws JSONException {
/* 322 */     Kim kim = new Kim(name);
/* 323 */     int integer = this.namekeep.find(kim);
/* 324 */     if (integer != -1) {
/* 325 */       one();
/* 326 */       writeAndTick(integer, this.namekeep);
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 331 */       zero();
/* 332 */       write(kim, this.namehuff);
/* 333 */       write(256, this.namehuff);
/* 334 */       this.namekeep.register(kim);
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
/*     */   private void writeObject(JSONObject jsonobject) throws JSONException {
/* 350 */     boolean first = true;
/* 351 */     Iterator keys = jsonobject.keys();
/* 352 */     while (keys.hasNext()) {
/*     */ 
/*     */ 
/*     */       
/* 356 */       Object key = keys.next();
/* 357 */       if (key instanceof String) {
/* 358 */         if (first) {
/* 359 */           first = false;
/* 360 */           write(5, 3);
/*     */         } else {
/* 362 */           one();
/*     */         } 
/* 364 */         writeName((String)key);
/* 365 */         Object value = jsonobject.get((String)key);
/* 366 */         if (value instanceof String) {
/* 367 */           zero();
/* 368 */           writeString((String)value); continue;
/*     */         } 
/* 370 */         one();
/* 371 */         writeValue(value);
/*     */       } 
/*     */     } 
/*     */     
/* 375 */     if (first) {
/* 376 */       write(0, 3);
/*     */     } else {
/* 378 */       zero();
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
/*     */   private void writeString(String string) throws JSONException {
/* 392 */     if (string.length() == 0) {
/* 393 */       zero();
/* 394 */       zero();
/* 395 */       write(256, this.substringhuff);
/* 396 */       zero();
/*     */     } else {
/* 398 */       Kim kim = new Kim(string);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 403 */       int integer = this.stringkeep.find(kim);
/* 404 */       if (integer != -1) {
/* 405 */         one();
/* 406 */         writeAndTick(integer, this.stringkeep);
/*     */       
/*     */       }
/*     */       else {
/*     */ 
/*     */         
/* 412 */         writeSubstring(kim);
/* 413 */         this.stringkeep.register(kim);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void writeSubstring(Kim kim) throws JSONException {
/* 425 */     this.substringkeep.reserve();
/* 426 */     zero();
/* 427 */     int from = 0;
/* 428 */     int thru = kim.length;
/* 429 */     int until = thru - 3;
/* 430 */     int previousFrom = -1;
/* 431 */     int previousThru = 0;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     while (true) {
/* 437 */       int integer = -1; int at;
/* 438 */       for (at = from; at <= until; at++) {
/* 439 */         integer = this.substringkeep.match(kim, at, thru);
/* 440 */         if (integer != -1) {
/*     */           break;
/*     */         }
/*     */       } 
/* 444 */       if (integer == -1) {
/*     */         break;
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 452 */       if (from != at) {
/* 453 */         zero();
/* 454 */         write(kim, from, at, this.substringhuff);
/* 455 */         write(256, this.substringhuff);
/* 456 */         if (previousFrom != -1) {
/* 457 */           this.substringkeep.registerOne(kim, previousFrom, previousThru);
/*     */           
/* 459 */           previousFrom = -1;
/*     */         } 
/*     */       } 
/* 462 */       one();
/* 463 */       writeAndTick(integer, this.substringkeep);
/* 464 */       from = at + this.substringkeep.length(integer);
/* 465 */       if (previousFrom != -1) {
/* 466 */         this.substringkeep.registerOne(kim, previousFrom, previousThru);
/*     */         
/* 468 */         previousFrom = -1;
/*     */       } 
/* 470 */       previousFrom = at;
/* 471 */       previousThru = from + 1;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 476 */     zero();
/* 477 */     if (from < thru) {
/* 478 */       write(kim, from, thru, this.substringhuff);
/* 479 */       if (previousFrom != -1) {
/* 480 */         this.substringkeep.registerOne(kim, previousFrom, previousThru);
/*     */       }
/*     */     } 
/* 483 */     write(256, this.substringhuff);
/* 484 */     zero();
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 489 */     this.substringkeep.registerMany(kim);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void writeValue(Object value) throws JSONException {
/* 500 */     if (value instanceof Number) {
/* 501 */       String string = JSONObject.numberToString((Number)value);
/* 502 */       int integer = this.values.find(string);
/* 503 */       if (integer != -1) {
/* 504 */         write(2, 2);
/* 505 */         writeAndTick(integer, this.values);
/*     */         return;
/*     */       } 
/* 508 */       if (value instanceof Integer || value instanceof Long) {
/* 509 */         long longer = ((Number)value).longValue();
/* 510 */         if (longer >= 0L && longer < 16384L) {
/* 511 */           write(0, 2);
/* 512 */           if (longer < 16L) {
/* 513 */             zero();
/* 514 */             write((int)longer, 4);
/*     */             return;
/*     */           } 
/* 517 */           one();
/* 518 */           if (longer < 128L) {
/* 519 */             zero();
/* 520 */             write((int)longer, 7);
/*     */             return;
/*     */           } 
/* 523 */           one();
/* 524 */           write((int)longer, 14);
/*     */           return;
/*     */         } 
/*     */       } 
/* 528 */       write(1, 2);
/* 529 */       for (int i = 0; i < string.length(); i++) {
/* 530 */         write(bcd(string.charAt(i)), 4);
/*     */       }
/* 532 */       write(endOfNumber, 4);
/* 533 */       this.values.register(string);
/*     */     } else {
/* 535 */       write(3, 2);
/* 536 */       writeJSON(value);
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
/*     */   private void zero() throws JSONException {
/* 551 */     write(0, 1);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void zip(JSONObject jsonobject) throws JSONException {
/* 561 */     begin();
/* 562 */     writeJSON(jsonobject);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void zip(JSONArray jsonarray) throws JSONException {
/* 572 */     begin();
/* 573 */     writeJSON(jsonarray);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\Compressor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */