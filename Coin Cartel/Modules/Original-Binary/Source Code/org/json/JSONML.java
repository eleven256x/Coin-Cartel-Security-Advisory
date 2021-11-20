/*     */ package org.json;
/*     */ 
/*     */ import java.util.Iterator;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class JSONML
/*     */ {
/*     */   private static Object parse(XMLTokener x, boolean arrayForm, JSONArray ja) throws JSONException {
/*  56 */     String closeTag = null;
/*     */     
/*  58 */     JSONArray newja = null;
/*  59 */     JSONObject newjo = null;
/*     */     
/*  61 */     String tagName = null;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     label111: while (true) {
/*  70 */       if (!x.more()) {
/*  71 */         throw x.syntaxError("Bad XML");
/*     */       }
/*  73 */       Object token = x.nextContent();
/*  74 */       if (token == XML.LT) {
/*  75 */         token = x.nextToken();
/*  76 */         if (token instanceof Character) {
/*  77 */           if (token == XML.SLASH) {
/*     */ 
/*     */ 
/*     */             
/*  81 */             token = x.nextToken();
/*  82 */             if (!(token instanceof String)) {
/*  83 */               throw new JSONException("Expected a closing name instead of '" + token + "'.");
/*     */             }
/*     */ 
/*     */             
/*  87 */             if (x.nextToken() != XML.GT) {
/*  88 */               throw x.syntaxError("Misshaped close tag");
/*     */             }
/*  90 */             return token;
/*  91 */           }  if (token == XML.BANG) {
/*     */ 
/*     */ 
/*     */             
/*  95 */             char c = x.next();
/*  96 */             if (c == '-') {
/*  97 */               if (x.next() == '-') {
/*  98 */                 x.skipPast("-->"); continue;
/*     */               } 
/* 100 */               x.back(); continue;
/*     */             } 
/* 102 */             if (c == '[') {
/* 103 */               token = x.nextToken();
/* 104 */               if (token.equals("CDATA") && x.next() == '[') {
/* 105 */                 if (ja != null)
/* 106 */                   ja.put(x.nextCDATA()); 
/*     */                 continue;
/*     */               } 
/* 109 */               throw x.syntaxError("Expected 'CDATA['");
/*     */             } 
/*     */             
/* 112 */             int i = 1;
/*     */             while (true)
/* 114 */             { token = x.nextMeta();
/* 115 */               if (token == null)
/* 116 */                 throw x.syntaxError("Missing '>' after '<!'."); 
/* 117 */               if (token == XML.LT) {
/* 118 */                 i++;
/* 119 */               } else if (token == XML.GT) {
/* 120 */                 i--;
/*     */               } 
/* 122 */               if (i <= 0)
/*     */                 continue label111;  }  break;
/* 124 */           }  if (token == XML.QUEST) {
/*     */ 
/*     */ 
/*     */             
/* 128 */             x.skipPast("?>"); continue;
/*     */           } 
/* 130 */           throw x.syntaxError("Misshaped tag");
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 136 */         if (!(token instanceof String)) {
/* 137 */           throw x.syntaxError("Bad tagName '" + token + "'.");
/*     */         }
/* 139 */         tagName = (String)token;
/* 140 */         newja = new JSONArray();
/* 141 */         newjo = new JSONObject();
/* 142 */         if (arrayForm) {
/* 143 */           newja.put(tagName);
/* 144 */           if (ja != null) {
/* 145 */             ja.put(newja);
/*     */           }
/*     */         } else {
/* 148 */           newjo.put("tagName", tagName);
/* 149 */           if (ja != null) {
/* 150 */             ja.put(newjo);
/*     */           }
/*     */         } 
/* 153 */         token = null;
/*     */         while (true) {
/* 155 */           if (token == null) {
/* 156 */             token = x.nextToken();
/*     */           }
/* 158 */           if (token == null) {
/* 159 */             throw x.syntaxError("Misshaped tag");
/*     */           }
/* 161 */           if (!(token instanceof String)) {
/*     */             break;
/*     */           }
/*     */ 
/*     */ 
/*     */           
/* 167 */           String attribute = (String)token;
/* 168 */           if (!arrayForm && ("tagName".equals(attribute) || "childNode".equals(attribute))) {
/* 169 */             throw x.syntaxError("Reserved attribute.");
/*     */           }
/* 171 */           token = x.nextToken();
/* 172 */           if (token == XML.EQ) {
/* 173 */             token = x.nextToken();
/* 174 */             if (!(token instanceof String)) {
/* 175 */               throw x.syntaxError("Missing value");
/*     */             }
/* 177 */             newjo.accumulate(attribute, XML.stringToValue((String)token));
/* 178 */             token = null; continue;
/*     */           } 
/* 180 */           newjo.accumulate(attribute, "");
/*     */         } 
/*     */         
/* 183 */         if (arrayForm && newjo.length() > 0) {
/* 184 */           newja.put(newjo);
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 189 */         if (token == XML.SLASH) {
/* 190 */           if (x.nextToken() != XML.GT) {
/* 191 */             throw x.syntaxError("Misshaped tag");
/*     */           }
/* 193 */           if (ja == null) {
/* 194 */             if (arrayForm) {
/* 195 */               return newja;
/*     */             }
/* 197 */             return newjo;
/*     */           } 
/*     */ 
/*     */           
/*     */           continue;
/*     */         } 
/*     */         
/* 204 */         if (token != XML.GT) {
/* 205 */           throw x.syntaxError("Misshaped tag");
/*     */         }
/* 207 */         closeTag = (String)parse(x, arrayForm, newja);
/* 208 */         if (closeTag != null) {
/* 209 */           if (!closeTag.equals(tagName)) {
/* 210 */             throw x.syntaxError("Mismatched '" + tagName + "' and '" + closeTag + "'");
/*     */           }
/*     */           
/* 213 */           tagName = null;
/* 214 */           if (!arrayForm && newja.length() > 0) {
/* 215 */             newjo.put("childNodes", newja);
/*     */           }
/* 217 */           if (ja == null) {
/* 218 */             if (arrayForm) {
/* 219 */               return newja;
/*     */             }
/* 221 */             return newjo;
/*     */           } 
/*     */         } 
/*     */         
/*     */         continue;
/*     */       } 
/*     */       
/* 228 */       if (ja != null) {
/* 229 */         ja.put((token instanceof String) ? XML.stringToValue((String)token) : token);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static JSONArray toJSONArray(String string) throws JSONException {
/* 251 */     return toJSONArray(new XMLTokener(string));
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
/*     */   public static JSONArray toJSONArray(XMLTokener x) throws JSONException {
/* 268 */     return (JSONArray)parse(x, true, null);
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
/*     */   public static JSONObject toJSONObject(XMLTokener x) throws JSONException {
/* 286 */     return (JSONObject)parse(x, false, null);
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
/*     */   public static JSONObject toJSONObject(String string) throws JSONException {
/* 304 */     return toJSONObject(new XMLTokener(string));
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
/*     */   public static String toString(JSONArray ja) throws JSONException {
/*     */     int i;
/* 321 */     StringBuffer sb = new StringBuffer();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 327 */     String tagName = ja.getString(0);
/* 328 */     XML.noSpace(tagName);
/* 329 */     tagName = XML.escape(tagName);
/* 330 */     sb.append('<');
/* 331 */     sb.append(tagName);
/*     */     
/* 333 */     Object object = ja.opt(1);
/* 334 */     if (object instanceof JSONObject) {
/* 335 */       i = 2;
/* 336 */       JSONObject jo = (JSONObject)object;
/*     */ 
/*     */ 
/*     */       
/* 340 */       Iterator keys = jo.keys();
/* 341 */       while (keys.hasNext()) {
/* 342 */         String key = keys.next().toString();
/* 343 */         XML.noSpace(key);
/* 344 */         String value = jo.optString(key);
/* 345 */         if (value != null) {
/* 346 */           sb.append(' ');
/* 347 */           sb.append(XML.escape(key));
/* 348 */           sb.append('=');
/* 349 */           sb.append('"');
/* 350 */           sb.append(XML.escape(value));
/* 351 */           sb.append('"');
/*     */         } 
/*     */       } 
/*     */     } else {
/* 355 */       i = 1;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 360 */     int length = ja.length();
/* 361 */     if (i >= length)
/* 362 */     { sb.append('/');
/* 363 */       sb.append('>'); }
/*     */     else
/* 365 */     { sb.append('>');
/*     */       while (true)
/* 367 */       { object = ja.get(i);
/* 368 */         i++;
/* 369 */         if (object != null) {
/* 370 */           if (object instanceof String) {
/* 371 */             sb.append(XML.escape(object.toString()));
/* 372 */           } else if (object instanceof JSONObject) {
/* 373 */             sb.append(toString((JSONObject)object));
/* 374 */           } else if (object instanceof JSONArray) {
/* 375 */             sb.append(toString((JSONArray)object));
/*     */           } 
/*     */         }
/* 378 */         if (i >= length)
/* 379 */         { sb.append('<');
/* 380 */           sb.append('/');
/* 381 */           sb.append(tagName);
/* 382 */           sb.append('>');
/*     */           
/* 384 */           return sb.toString(); }  }  }  return sb.toString();
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
/*     */   public static String toString(JSONObject jo) throws JSONException {
/* 397 */     StringBuffer sb = new StringBuffer();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 409 */     String tagName = jo.optString("tagName");
/* 410 */     if (tagName == null) {
/* 411 */       return XML.escape(jo.toString());
/*     */     }
/* 413 */     XML.noSpace(tagName);
/* 414 */     tagName = XML.escape(tagName);
/* 415 */     sb.append('<');
/* 416 */     sb.append(tagName);
/*     */ 
/*     */ 
/*     */     
/* 420 */     Iterator keys = jo.keys();
/* 421 */     while (keys.hasNext()) {
/* 422 */       String key = keys.next().toString();
/* 423 */       if (!"tagName".equals(key) && !"childNodes".equals(key)) {
/* 424 */         XML.noSpace(key);
/* 425 */         String value = jo.optString(key);
/* 426 */         if (value != null) {
/* 427 */           sb.append(' ');
/* 428 */           sb.append(XML.escape(key));
/* 429 */           sb.append('=');
/* 430 */           sb.append('"');
/* 431 */           sb.append(XML.escape(value));
/* 432 */           sb.append('"');
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 439 */     JSONArray ja = jo.optJSONArray("childNodes");
/* 440 */     if (ja == null) {
/* 441 */       sb.append('/');
/* 442 */       sb.append('>');
/*     */     } else {
/* 444 */       sb.append('>');
/* 445 */       int length = ja.length();
/* 446 */       for (int i = 0; i < length; i++) {
/* 447 */         Object object = ja.get(i);
/* 448 */         if (object != null) {
/* 449 */           if (object instanceof String) {
/* 450 */             sb.append(XML.escape(object.toString()));
/* 451 */           } else if (object instanceof JSONObject) {
/* 452 */             sb.append(toString((JSONObject)object));
/* 453 */           } else if (object instanceof JSONArray) {
/* 454 */             sb.append(toString((JSONArray)object));
/*     */           } else {
/* 456 */             sb.append(object.toString());
/*     */           } 
/*     */         }
/*     */       } 
/* 460 */       sb.append('<');
/* 461 */       sb.append('/');
/* 462 */       sb.append(tagName);
/* 463 */       sb.append('>');
/*     */     } 
/* 465 */     return sb.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\JSONML.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */