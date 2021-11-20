/*     */ package org.json;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class CDL
/*     */ {
/*     */   private static String getValue(JSONTokener x) throws JSONException {
/*     */     char c, q;
/*     */     StringBuffer sb;
/*     */     do {
/*  60 */       c = x.next();
/*  61 */     } while (c == ' ' || c == '\t');
/*  62 */     switch (c) {
/*     */       case '\000':
/*  64 */         return null;
/*     */       case '"':
/*     */       case '\'':
/*  67 */         q = c;
/*  68 */         sb = new StringBuffer();
/*     */         while (true) {
/*  70 */           c = x.next();
/*  71 */           if (c == q) {
/*     */             break;
/*     */           }
/*  74 */           if (c == '\000' || c == '\n' || c == '\r') {
/*  75 */             throw x.syntaxError("Missing close quote '" + q + "'.");
/*     */           }
/*  77 */           sb.append(c);
/*     */         } 
/*  79 */         return sb.toString();
/*     */       case ',':
/*  81 */         x.back();
/*  82 */         return "";
/*     */     } 
/*  84 */     x.back();
/*  85 */     return x.nextTo(',');
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static JSONArray rowToJSONArray(JSONTokener x) throws JSONException {
/*  96 */     JSONArray ja = new JSONArray();
/*     */     while (true) {
/*  98 */       String value = getValue(x);
/*  99 */       char c = x.next();
/* 100 */       if (value == null || (ja.length() == 0 && value.length() == 0 && c != ','))
/*     */       {
/* 102 */         return null;
/*     */       }
/* 104 */       ja.put(value);
/*     */       
/* 106 */       while (c != ',') {
/*     */ 
/*     */         
/* 109 */         if (c != ' ') {
/* 110 */           if (c == '\n' || c == '\r' || c == '\000') {
/* 111 */             return ja;
/*     */           }
/* 113 */           throw x.syntaxError("Bad character '" + c + "' (" + c + ").");
/*     */         } 
/*     */         
/* 116 */         c = x.next();
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
/*     */   public static JSONObject rowToJSONObject(JSONArray names, JSONTokener x) throws JSONException {
/* 133 */     JSONArray ja = rowToJSONArray(x);
/* 134 */     return (ja != null) ? ja.toJSONObject(names) : null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String rowToString(JSONArray ja) {
/* 145 */     StringBuffer sb = new StringBuffer();
/* 146 */     for (int i = 0; i < ja.length(); i++) {
/* 147 */       if (i > 0) {
/* 148 */         sb.append(',');
/*     */       }
/* 150 */       Object object = ja.opt(i);
/* 151 */       if (object != null) {
/* 152 */         String string = object.toString();
/* 153 */         if (string.length() > 0 && (string.indexOf(',') >= 0 || string.indexOf('\n') >= 0 || string.indexOf('\r') >= 0 || string.indexOf(false) >= 0 || string.charAt(0) == '"')) {
/*     */ 
/*     */           
/* 156 */           sb.append('"');
/* 157 */           int length = string.length();
/* 158 */           for (int j = 0; j < length; j++) {
/* 159 */             char c = string.charAt(j);
/* 160 */             if (c >= ' ' && c != '"') {
/* 161 */               sb.append(c);
/*     */             }
/*     */           } 
/* 164 */           sb.append('"');
/*     */         } else {
/* 166 */           sb.append(string);
/*     */         } 
/*     */       } 
/*     */     } 
/* 170 */     sb.append('\n');
/* 171 */     return sb.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static JSONArray toJSONArray(String string) throws JSONException {
/* 182 */     return toJSONArray(new JSONTokener(string));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static JSONArray toJSONArray(JSONTokener x) throws JSONException {
/* 193 */     return toJSONArray(rowToJSONArray(x), x);
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
/*     */   public static JSONArray toJSONArray(JSONArray names, String string) throws JSONException {
/* 206 */     return toJSONArray(names, new JSONTokener(string));
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
/*     */   public static JSONArray toJSONArray(JSONArray names, JSONTokener x) throws JSONException {
/* 219 */     if (names == null || names.length() == 0) {
/* 220 */       return null;
/*     */     }
/* 222 */     JSONArray ja = new JSONArray();
/*     */     while (true) {
/* 224 */       JSONObject jo = rowToJSONObject(names, x);
/* 225 */       if (jo == null) {
/*     */         break;
/*     */       }
/* 228 */       ja.put(jo);
/*     */     } 
/* 230 */     if (ja.length() == 0) {
/* 231 */       return null;
/*     */     }
/* 233 */     return ja;
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
/*     */   public static String toString(JSONArray ja) throws JSONException {
/* 246 */     JSONObject jo = ja.optJSONObject(0);
/* 247 */     if (jo != null) {
/* 248 */       JSONArray names = jo.names();
/* 249 */       if (names != null) {
/* 250 */         return rowToString(names) + toString(names, ja);
/*     */       }
/*     */     } 
/* 253 */     return null;
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
/*     */   public static String toString(JSONArray names, JSONArray ja) throws JSONException {
/* 267 */     if (names == null || names.length() == 0) {
/* 268 */       return null;
/*     */     }
/* 270 */     StringBuffer sb = new StringBuffer();
/* 271 */     for (int i = 0; i < ja.length(); i++) {
/* 272 */       JSONObject jo = ja.optJSONObject(i);
/* 273 */       if (jo != null) {
/* 274 */         sb.append(rowToString(jo.toJSONArray(names)));
/*     */       }
/*     */     } 
/* 277 */     return sb.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\CDL.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */