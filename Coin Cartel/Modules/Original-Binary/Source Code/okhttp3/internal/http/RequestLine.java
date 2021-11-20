/*    */ package okhttp3.internal.http;
/*    */ 
/*    */ import java.net.Proxy;
/*    */ import okhttp3.HttpUrl;
/*    */ import okhttp3.Request;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class RequestLine
/*    */ {
/*    */   public static String get(Request request, Proxy.Type proxyType) {
/* 33 */     StringBuilder result = new StringBuilder();
/* 34 */     result.append(request.method());
/* 35 */     result.append(' ');
/*    */     
/* 37 */     if (includeAuthorityInRequestLine(request, proxyType)) {
/* 38 */       result.append(request.url());
/*    */     } else {
/* 40 */       result.append(requestPath(request.url()));
/*    */     } 
/*    */     
/* 43 */     result.append(" HTTP/1.1");
/* 44 */     return result.toString();
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   private static boolean includeAuthorityInRequestLine(Request request, Proxy.Type proxyType) {
/* 52 */     return (!request.isHttps() && proxyType == Proxy.Type.HTTP);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static String requestPath(HttpUrl url) {
/* 60 */     String path = url.encodedPath();
/* 61 */     String query = url.encodedQuery();
/* 62 */     return (query != null) ? (path + '?' + query) : path;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\RequestLine.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */