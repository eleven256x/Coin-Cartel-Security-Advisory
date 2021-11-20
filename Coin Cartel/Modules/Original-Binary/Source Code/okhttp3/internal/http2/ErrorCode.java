/*    */ package okhttp3.internal.http2;
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
/*    */ public enum ErrorCode
/*    */ {
/* 21 */   NO_ERROR(0),
/*    */   
/* 23 */   PROTOCOL_ERROR(1),
/*    */   
/* 25 */   INTERNAL_ERROR(2),
/*    */   
/* 27 */   FLOW_CONTROL_ERROR(3),
/*    */   
/* 29 */   REFUSED_STREAM(7),
/*    */   
/* 31 */   CANCEL(8),
/*    */   
/* 33 */   COMPRESSION_ERROR(9),
/*    */   
/* 35 */   CONNECT_ERROR(10),
/*    */   
/* 37 */   ENHANCE_YOUR_CALM(11),
/*    */   
/* 39 */   INADEQUATE_SECURITY(12),
/*    */   
/* 41 */   HTTP_1_1_REQUIRED(13);
/*    */   
/*    */   public final int httpCode;
/*    */   
/*    */   ErrorCode(int httpCode) {
/* 46 */     this.httpCode = httpCode;
/*    */   }
/*    */   
/*    */   public static ErrorCode fromHttp2(int code) {
/* 50 */     for (ErrorCode errorCode : values()) {
/* 51 */       if (errorCode.httpCode == code) return errorCode; 
/*    */     } 
/* 53 */     return null;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\ErrorCode.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */