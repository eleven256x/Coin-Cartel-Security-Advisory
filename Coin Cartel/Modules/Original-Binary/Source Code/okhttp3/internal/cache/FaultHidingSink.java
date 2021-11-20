/*    */ package okhttp3.internal.cache;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import okio.Buffer;
/*    */ import okio.ForwardingSink;
/*    */ import okio.Sink;
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
/*    */ class FaultHidingSink
/*    */   extends ForwardingSink
/*    */ {
/*    */   private boolean hasErrors;
/*    */   
/*    */   FaultHidingSink(Sink delegate) {
/* 28 */     super(delegate);
/*    */   }
/*    */   
/*    */   public void write(Buffer source, long byteCount) throws IOException {
/* 32 */     if (this.hasErrors) {
/* 33 */       source.skip(byteCount);
/*    */       return;
/*    */     } 
/*    */     try {
/* 37 */       super.write(source, byteCount);
/* 38 */     } catch (IOException e) {
/* 39 */       this.hasErrors = true;
/* 40 */       onException(e);
/*    */     } 
/*    */   }
/*    */   
/*    */   public void flush() throws IOException {
/* 45 */     if (this.hasErrors)
/*    */       return;  try {
/* 47 */       super.flush();
/* 48 */     } catch (IOException e) {
/* 49 */       this.hasErrors = true;
/* 50 */       onException(e);
/*    */     } 
/*    */   }
/*    */   
/*    */   public void close() throws IOException {
/* 55 */     if (this.hasErrors)
/*    */       return;  try {
/* 57 */       super.close();
/* 58 */     } catch (IOException e) {
/* 59 */       this.hasErrors = true;
/* 60 */       onException(e);
/*    */     } 
/*    */   }
/*    */   
/*    */   protected void onException(IOException e) {}
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\cache\FaultHidingSink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */