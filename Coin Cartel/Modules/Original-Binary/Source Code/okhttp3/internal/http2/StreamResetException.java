/*    */ package okhttp3.internal.http2;
/*    */ 
/*    */ import java.io.IOException;
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
/*    */ public final class StreamResetException
/*    */   extends IOException
/*    */ {
/*    */   public final ErrorCode errorCode;
/*    */   
/*    */   public StreamResetException(ErrorCode errorCode) {
/* 25 */     super("stream was reset: " + errorCode);
/* 26 */     this.errorCode = errorCode;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\StreamResetException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */