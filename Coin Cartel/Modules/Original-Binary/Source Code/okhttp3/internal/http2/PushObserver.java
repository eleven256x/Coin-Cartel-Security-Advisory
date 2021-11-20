/*    */ package okhttp3.internal.http2;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.util.List;
/*    */ import okio.BufferedSource;
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
/*    */ public interface PushObserver
/*    */ {
/* 76 */   public static final PushObserver CANCEL = new PushObserver()
/*    */     {
/*    */       public boolean onRequest(int streamId, List<Header> requestHeaders) {
/* 79 */         return true;
/*    */       }
/*    */       
/*    */       public boolean onHeaders(int streamId, List<Header> responseHeaders, boolean last) {
/* 83 */         return true;
/*    */       }
/*    */ 
/*    */       
/*    */       public boolean onData(int streamId, BufferedSource source, int byteCount, boolean last) throws IOException {
/* 88 */         source.skip(byteCount);
/* 89 */         return true;
/*    */       }
/*    */       
/*    */       public void onReset(int streamId, ErrorCode errorCode) {}
/*    */     };
/*    */   
/*    */   boolean onRequest(int paramInt, List<Header> paramList);
/*    */   
/*    */   boolean onHeaders(int paramInt, List<Header> paramList, boolean paramBoolean);
/*    */   
/*    */   boolean onData(int paramInt1, BufferedSource paramBufferedSource, int paramInt2, boolean paramBoolean) throws IOException;
/*    */   
/*    */   void onReset(int paramInt, ErrorCode paramErrorCode);
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\PushObserver.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */