/*    */ package io.socket.parseqs;
/*    */ 
/*    */ import io.socket.global.Global;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class ParseQS
/*    */ {
/*    */   public static String encode(Map<String, String> obj) {
/* 14 */     StringBuilder str = new StringBuilder();
/* 15 */     for (Map.Entry<String, String> entry : obj.entrySet()) {
/* 16 */       if (str.length() > 0) str.append("&"); 
/* 17 */       str.append(Global.encodeURIComponent(entry.getKey())).append("=")
/* 18 */         .append(Global.encodeURIComponent(entry.getValue()));
/*    */     } 
/* 20 */     return str.toString();
/*    */   }
/*    */   
/*    */   public static Map<String, String> decode(String qs) {
/* 24 */     Map<String, String> qry = new HashMap<>();
/* 25 */     String[] pairs = qs.split("&");
/* 26 */     for (String _pair : pairs) {
/* 27 */       String[] pair = _pair.split("=");
/* 28 */       qry.put(Global.decodeURIComponent(pair[0]), (pair.length > 1) ? 
/* 29 */           Global.decodeURIComponent(pair[1]) : "");
/*    */     } 
/* 31 */     return qry;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\parseqs\ParseQS.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */