/*    */ package okhttp3.internal.http;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class HttpMethod
/*    */ {
/*    */   public static boolean invalidatesCache(String method) {
/* 20 */     return (method.equals("POST") || method
/* 21 */       .equals("PATCH") || method
/* 22 */       .equals("PUT") || method
/* 23 */       .equals("DELETE") || method
/* 24 */       .equals("MOVE"));
/*    */   }
/*    */   
/*    */   public static boolean requiresRequestBody(String method) {
/* 28 */     return (method.equals("POST") || method
/* 29 */       .equals("PUT") || method
/* 30 */       .equals("PATCH") || method
/* 31 */       .equals("PROPPATCH") || method
/* 32 */       .equals("REPORT"));
/*    */   }
/*    */   
/*    */   public static boolean permitsRequestBody(String method) {
/* 36 */     return (!method.equals("GET") && !method.equals("HEAD"));
/*    */   }
/*    */   
/*    */   public static boolean redirectsWithBody(String method) {
/* 40 */     return method.equals("PROPFIND");
/*    */   }
/*    */ 
/*    */   
/*    */   public static boolean redirectsToGet(String method) {
/* 45 */     return !method.equals("PROPFIND");
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\HttpMethod.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */