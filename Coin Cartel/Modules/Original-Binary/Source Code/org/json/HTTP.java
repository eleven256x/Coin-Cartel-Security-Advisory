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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class HTTP
/*     */ {
/*     */   public static final String CRLF = "\r\n";
/*     */   
/*     */   public static JSONObject toJSONObject(String string) throws JSONException {
/*  72 */     JSONObject jo = new JSONObject();
/*  73 */     HTTPTokener x = new HTTPTokener(string);
/*     */ 
/*     */     
/*  76 */     String token = x.nextToken();
/*  77 */     if (token.toUpperCase().startsWith("HTTP")) {
/*     */ 
/*     */ 
/*     */       
/*  81 */       jo.put("HTTP-Version", token);
/*  82 */       jo.put("Status-Code", x.nextToken());
/*  83 */       jo.put("Reason-Phrase", x.nextTo(false));
/*  84 */       x.next();
/*     */     
/*     */     }
/*     */     else {
/*     */ 
/*     */       
/*  90 */       jo.put("Method", token);
/*  91 */       jo.put("Request-URI", x.nextToken());
/*  92 */       jo.put("HTTP-Version", x.nextToken());
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/*  97 */     while (x.more()) {
/*  98 */       String name = x.nextTo(':');
/*  99 */       x.next(':');
/* 100 */       jo.put(name, x.nextTo(false));
/* 101 */       x.next();
/*     */     } 
/* 103 */     return jo;
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
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String toString(JSONObject jo) throws JSONException {
/* 128 */     Iterator keys = jo.keys();
/*     */     
/* 130 */     StringBuffer sb = new StringBuffer();
/* 131 */     if (jo.has("Status-Code") && jo.has("Reason-Phrase")) {
/* 132 */       sb.append(jo.getString("HTTP-Version"));
/* 133 */       sb.append(' ');
/* 134 */       sb.append(jo.getString("Status-Code"));
/* 135 */       sb.append(' ');
/* 136 */       sb.append(jo.getString("Reason-Phrase"));
/* 137 */     } else if (jo.has("Method") && jo.has("Request-URI")) {
/* 138 */       sb.append(jo.getString("Method"));
/* 139 */       sb.append(' ');
/* 140 */       sb.append('"');
/* 141 */       sb.append(jo.getString("Request-URI"));
/* 142 */       sb.append('"');
/* 143 */       sb.append(' ');
/* 144 */       sb.append(jo.getString("HTTP-Version"));
/*     */     } else {
/* 146 */       throw new JSONException("Not enough material for an HTTP header.");
/*     */     } 
/* 148 */     sb.append("\r\n");
/* 149 */     while (keys.hasNext()) {
/* 150 */       String string = keys.next().toString();
/* 151 */       if (!"HTTP-Version".equals(string) && !"Status-Code".equals(string) && !"Reason-Phrase".equals(string) && !"Method".equals(string) && !"Request-URI".equals(string) && !jo.isNull(string)) {
/*     */ 
/*     */         
/* 154 */         sb.append(string);
/* 155 */         sb.append(": ");
/* 156 */         sb.append(jo.getString(string));
/* 157 */         sb.append("\r\n");
/*     */       } 
/*     */     } 
/* 160 */     sb.append("\r\n");
/* 161 */     return sb.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\HTTP.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */