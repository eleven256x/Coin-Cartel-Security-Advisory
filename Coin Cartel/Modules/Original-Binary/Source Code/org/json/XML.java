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
/*     */ public class XML
/*     */ {
/*  39 */   public static final Character AMP = new Character('&');
/*     */ 
/*     */   
/*  42 */   public static final Character APOS = new Character('\'');
/*     */ 
/*     */   
/*  45 */   public static final Character BANG = new Character('!');
/*     */ 
/*     */   
/*  48 */   public static final Character EQ = new Character('=');
/*     */ 
/*     */   
/*  51 */   public static final Character GT = new Character('>');
/*     */ 
/*     */   
/*  54 */   public static final Character LT = new Character('<');
/*     */ 
/*     */   
/*  57 */   public static final Character QUEST = new Character('?');
/*     */ 
/*     */   
/*  60 */   public static final Character QUOT = new Character('"');
/*     */ 
/*     */   
/*  63 */   public static final Character SLASH = new Character('/');
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String escape(String string) {
/*  77 */     StringBuffer sb = new StringBuffer();
/*  78 */     for (int i = 0, length = string.length(); i < length; i++) {
/*  79 */       char c = string.charAt(i);
/*  80 */       switch (c) {
/*     */         case '&':
/*  82 */           sb.append("&amp;");
/*     */           break;
/*     */         case '<':
/*  85 */           sb.append("&lt;");
/*     */           break;
/*     */         case '>':
/*  88 */           sb.append("&gt;");
/*     */           break;
/*     */         case '"':
/*  91 */           sb.append("&quot;");
/*     */           break;
/*     */         case '\'':
/*  94 */           sb.append("&apos;");
/*     */           break;
/*     */         default:
/*  97 */           sb.append(c); break;
/*     */       } 
/*     */     } 
/* 100 */     return sb.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void noSpace(String string) throws JSONException {
/* 110 */     int length = string.length();
/* 111 */     if (length == 0) {
/* 112 */       throw new JSONException("Empty string.");
/*     */     }
/* 114 */     for (int i = 0; i < length; i++) {
/* 115 */       if (Character.isWhitespace(string.charAt(i))) {
/* 116 */         throw new JSONException("'" + string + "' contains a space character.");
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
/*     */   private static boolean parse(XMLTokener x, JSONObject context, String name) throws JSONException {
/* 134 */     JSONObject jsonobject = null;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 149 */     Object token = x.nextToken();
/*     */ 
/*     */ 
/*     */     
/* 153 */     if (token == BANG) {
/* 154 */       char c = x.next();
/* 155 */       if (c == '-') {
/* 156 */         if (x.next() == '-') {
/* 157 */           x.skipPast("-->");
/* 158 */           return false;
/*     */         } 
/* 160 */         x.back();
/* 161 */       } else if (c == '[') {
/* 162 */         token = x.nextToken();
/* 163 */         if ("CDATA".equals(token) && 
/* 164 */           x.next() == '[') {
/* 165 */           String string = x.nextCDATA();
/* 166 */           if (string.length() > 0) {
/* 167 */             context.accumulate("content", string);
/*     */           }
/* 169 */           return false;
/*     */         } 
/*     */         
/* 172 */         throw x.syntaxError("Expected 'CDATA['");
/*     */       } 
/* 174 */       int i = 1;
/*     */       while (true)
/* 176 */       { token = x.nextMeta();
/* 177 */         if (token == null)
/* 178 */           throw x.syntaxError("Missing '>' after '<!'."); 
/* 179 */         if (token == LT) {
/* 180 */           i++;
/* 181 */         } else if (token == GT) {
/* 182 */           i--;
/*     */         } 
/* 184 */         if (i <= 0)
/* 185 */           return false;  } 
/* 186 */     }  if (token == QUEST) {
/*     */ 
/*     */ 
/*     */       
/* 190 */       x.skipPast("?>");
/* 191 */       return false;
/* 192 */     }  if (token == SLASH) {
/*     */ 
/*     */ 
/*     */       
/* 196 */       token = x.nextToken();
/* 197 */       if (name == null) {
/* 198 */         throw x.syntaxError("Mismatched close tag " + token);
/*     */       }
/* 200 */       if (!token.equals(name)) {
/* 201 */         throw x.syntaxError("Mismatched " + name + " and " + token);
/*     */       }
/* 203 */       if (x.nextToken() != GT) {
/* 204 */         throw x.syntaxError("Misshaped close tag");
/*     */       }
/* 206 */       return true;
/*     */     } 
/* 208 */     if (token instanceof Character) {
/* 209 */       throw x.syntaxError("Misshaped tag");
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 214 */     String tagName = (String)token;
/* 215 */     token = null;
/* 216 */     jsonobject = new JSONObject();
/*     */     while (true) {
/* 218 */       if (token == null) {
/* 219 */         token = x.nextToken();
/*     */       }
/*     */ 
/*     */ 
/*     */       
/* 224 */       if (token instanceof String) {
/* 225 */         String string = (String)token;
/* 226 */         token = x.nextToken();
/* 227 */         if (token == EQ) {
/* 228 */           token = x.nextToken();
/* 229 */           if (!(token instanceof String)) {
/* 230 */             throw x.syntaxError("Missing value");
/*     */           }
/* 232 */           jsonobject.accumulate(string, stringToValue((String)token));
/*     */           
/* 234 */           token = null; continue;
/*     */         } 
/* 236 */         jsonobject.accumulate(string, "");
/*     */         continue;
/*     */       } 
/*     */       break;
/*     */     } 
/* 241 */     if (token == SLASH) {
/* 242 */       if (x.nextToken() != GT) {
/* 243 */         throw x.syntaxError("Misshaped tag");
/*     */       }
/* 245 */       if (jsonobject.length() > 0) {
/* 246 */         context.accumulate(tagName, jsonobject);
/*     */       } else {
/* 248 */         context.accumulate(tagName, "");
/*     */       } 
/* 250 */       return false;
/*     */     } 
/*     */ 
/*     */     
/* 254 */     if (token == GT) {
/*     */       while (true) {
/* 256 */         token = x.nextContent();
/* 257 */         if (token == null) {
/* 258 */           if (tagName != null) {
/* 259 */             throw x.syntaxError("Unclosed tag " + tagName);
/*     */           }
/* 261 */           return false;
/* 262 */         }  if (token instanceof String) {
/* 263 */           String string = (String)token;
/* 264 */           if (string.length() > 0) {
/* 265 */             jsonobject.accumulate("content", stringToValue(string));
/*     */           }
/*     */           
/*     */           continue;
/*     */         } 
/*     */         
/* 271 */         if (token == LT && 
/* 272 */           parse(x, jsonobject, tagName)) {
/* 273 */           if (jsonobject.length() == 0) {
/* 274 */             context.accumulate(tagName, "");
/* 275 */           } else if (jsonobject.length() == 1 && jsonobject.opt("content") != null) {
/*     */             
/* 277 */             context.accumulate(tagName, jsonobject.opt("content"));
/*     */           } else {
/*     */             
/* 280 */             context.accumulate(tagName, jsonobject);
/*     */           } 
/* 282 */           return false;
/*     */         } 
/*     */       } 
/*     */     }
/*     */     
/* 287 */     throw x.syntaxError("Misshaped tag");
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
/*     */   public static Object stringToValue(String string) {
/* 304 */     if ("true".equalsIgnoreCase(string)) {
/* 305 */       return Boolean.TRUE;
/*     */     }
/* 307 */     if ("false".equalsIgnoreCase(string)) {
/* 308 */       return Boolean.FALSE;
/*     */     }
/* 310 */     if ("null".equalsIgnoreCase(string)) {
/* 311 */       return JSONObject.NULL;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 318 */       char initial = string.charAt(0);
/* 319 */       if (initial == '-' || (initial >= '0' && initial <= '9')) {
/* 320 */         Long value = new Long(string);
/* 321 */         if (value.toString().equals(string)) {
/* 322 */           return value;
/*     */         }
/*     */       } 
/* 325 */     } catch (Exception ignore) {
/*     */       try {
/* 327 */         Double value = new Double(string);
/* 328 */         if (value.toString().equals(string)) {
/* 329 */           return value;
/*     */         }
/* 331 */       } catch (Exception ignoreAlso) {}
/*     */     } 
/*     */     
/* 334 */     return string;
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
/*     */   public static JSONObject toJSONObject(String string) throws JSONException {
/* 353 */     JSONObject jo = new JSONObject();
/* 354 */     XMLTokener x = new XMLTokener(string);
/* 355 */     while (x.more() && x.skipPast("<")) {
/* 356 */       parse(x, jo, null);
/*     */     }
/* 358 */     return jo;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String toString(Object object) throws JSONException {
/* 369 */     return toString(object, null);
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
/*     */   public static String toString(Object object, String tagName) throws JSONException {
/* 382 */     StringBuffer sb = new StringBuffer();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 391 */     if (object instanceof JSONObject) {
/*     */ 
/*     */ 
/*     */       
/* 395 */       if (tagName != null) {
/* 396 */         sb.append('<');
/* 397 */         sb.append(tagName);
/* 398 */         sb.append('>');
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 403 */       JSONObject jo = (JSONObject)object;
/* 404 */       Iterator keys = jo.keys();
/* 405 */       while (keys.hasNext()) {
/* 406 */         String key = keys.next().toString();
/* 407 */         Object value = jo.opt(key);
/* 408 */         if (value == null) {
/* 409 */           value = "";
/*     */         }
/* 411 */         if (value instanceof String) {
/* 412 */           String str = (String)value;
/*     */         } else {
/* 414 */           String str = null;
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 419 */         if ("content".equals(key)) {
/* 420 */           if (value instanceof JSONArray) {
/* 421 */             JSONArray ja = (JSONArray)value;
/* 422 */             int length = ja.length();
/* 423 */             for (int i = 0; i < length; i++) {
/* 424 */               if (i > 0) {
/* 425 */                 sb.append('\n');
/*     */               }
/* 427 */               sb.append(escape(ja.get(i).toString()));
/*     */             }  continue;
/*     */           } 
/* 430 */           sb.append(escape(value.toString()));
/*     */           
/*     */           continue;
/*     */         } 
/*     */         
/* 435 */         if (value instanceof JSONArray) {
/* 436 */           JSONArray ja = (JSONArray)value;
/* 437 */           int length = ja.length();
/* 438 */           for (int i = 0; i < length; i++) {
/* 439 */             value = ja.get(i);
/* 440 */             if (value instanceof JSONArray) {
/* 441 */               sb.append('<');
/* 442 */               sb.append(key);
/* 443 */               sb.append('>');
/* 444 */               sb.append(toString(value));
/* 445 */               sb.append("</");
/* 446 */               sb.append(key);
/* 447 */               sb.append('>');
/*     */             } else {
/* 449 */               sb.append(toString(value, key));
/*     */             } 
/*     */           }  continue;
/* 452 */         }  if ("".equals(value)) {
/* 453 */           sb.append('<');
/* 454 */           sb.append(key);
/* 455 */           sb.append("/>");
/*     */           
/*     */           continue;
/*     */         } 
/*     */         
/* 460 */         sb.append(toString(value, key));
/*     */       } 
/*     */       
/* 463 */       if (tagName != null) {
/*     */ 
/*     */ 
/*     */         
/* 467 */         sb.append("</");
/* 468 */         sb.append(tagName);
/* 469 */         sb.append('>');
/*     */       } 
/* 471 */       return sb.toString();
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 477 */     if (object.getClass().isArray()) {
/* 478 */       object = new JSONArray(object);
/*     */     }
/* 480 */     if (object instanceof JSONArray) {
/* 481 */       JSONArray ja = (JSONArray)object;
/* 482 */       int length = ja.length();
/* 483 */       for (int i = 0; i < length; i++) {
/* 484 */         sb.append(toString(ja.opt(i), (tagName == null) ? "array" : tagName));
/*     */       }
/* 486 */       return sb.toString();
/*     */     } 
/* 488 */     String string = (object == null) ? "null" : escape(object.toString());
/* 489 */     return (tagName == null) ? ("\"" + string + "\"") : ((string.length() == 0) ? ("<" + tagName + "/>") : ("<" + tagName + ">" + string + "</" + tagName + ">"));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\XML.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */