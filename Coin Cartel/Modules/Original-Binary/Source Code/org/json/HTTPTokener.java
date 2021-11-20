/*    */ package org.json;
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
/*    */ public class HTTPTokener
/*    */   extends JSONTokener
/*    */ {
/*    */   public HTTPTokener(String string) {
/* 40 */     super(string);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public String nextToken() throws JSONException {
/* 52 */     StringBuffer sb = new StringBuffer();
/*    */     while (true) {
/* 54 */       char c = next();
/* 55 */       if (!Character.isWhitespace(c)) {
/* 56 */         if (c == '"' || c == '\'') {
/* 57 */           char q = c;
/*    */           while (true) {
/* 59 */             c = next();
/* 60 */             if (c < ' ') {
/* 61 */               throw syntaxError("Unterminated string.");
/*    */             }
/* 63 */             if (c == q) {
/* 64 */               return sb.toString();
/*    */             }
/* 66 */             sb.append(c);
/*    */           }  break;
/*    */         } 
/*    */         while (true) {
/* 70 */           if (c == '\000' || Character.isWhitespace(c)) {
/* 71 */             return sb.toString();
/*    */           }
/* 73 */           sb.append(c);
/* 74 */           c = next();
/*    */         } 
/*    */         break;
/*    */       } 
/*    */     } 
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\HTTPTokener.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */