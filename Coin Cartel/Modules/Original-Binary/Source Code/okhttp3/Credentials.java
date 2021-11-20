/*    */ package okhttp3;
/*    */ 
/*    */ import java.nio.charset.Charset;
/*    */ import okhttp3.internal.Util;
/*    */ import okio.ByteString;
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
/*    */ public final class Credentials
/*    */ {
/*    */   public static String basic(String username, String password) {
/* 30 */     return basic(username, password, Util.ISO_8859_1);
/*    */   }
/*    */   
/*    */   public static String basic(String username, String password, Charset charset) {
/* 34 */     String usernameAndPassword = username + ":" + password;
/* 35 */     String encoded = ByteString.encodeString(usernameAndPassword, charset).base64();
/* 36 */     return "Basic " + encoded;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Credentials.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */