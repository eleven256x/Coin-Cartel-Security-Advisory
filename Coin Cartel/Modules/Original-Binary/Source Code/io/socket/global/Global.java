/*    */ package io.socket.global;
/*    */ 
/*    */ import java.io.UnsupportedEncodingException;
/*    */ import java.net.URLDecoder;
/*    */ import java.net.URLEncoder;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Global
/*    */ {
/*    */   public static String encodeURIComponent(String str) {
/*    */     try {
/* 14 */       return URLEncoder.encode(str, "UTF-8")
/* 15 */         .replace("+", "%20")
/* 16 */         .replace("%21", "!")
/* 17 */         .replace("%27", "'")
/* 18 */         .replace("%28", "(")
/* 19 */         .replace("%29", ")")
/* 20 */         .replace("%7E", "~");
/* 21 */     } catch (UnsupportedEncodingException e) {
/* 22 */       throw new RuntimeException(e);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static String decodeURIComponent(String str) {
/*    */     try {
/* 28 */       return URLDecoder.decode(str, "UTF-8");
/* 29 */     } catch (UnsupportedEncodingException e) {
/* 30 */       throw new RuntimeException(e);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\global\Global.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */