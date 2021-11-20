/*    */ package io.socket.hasbinary;
/*    */ 
/*    */ import java.util.Iterator;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ import org.json.JSONArray;
/*    */ import org.json.JSONException;
/*    */ import org.json.JSONObject;
/*    */ 
/*    */ 
/*    */ public class HasBinary
/*    */ {
/* 13 */   private static final Logger logger = Logger.getLogger(HasBinary.class.getName());
/*    */ 
/*    */ 
/*    */   
/*    */   public static boolean hasBinary(Object data) {
/* 18 */     return _hasBinary(data);
/*    */   }
/*    */   
/*    */   private static boolean _hasBinary(Object obj) {
/* 22 */     if (obj == null) return false;
/*    */     
/* 24 */     if (obj instanceof byte[]) {
/* 25 */       return true;
/*    */     }
/*    */     
/* 28 */     if (obj instanceof JSONArray) {
/* 29 */       JSONArray _obj = (JSONArray)obj;
/* 30 */       int length = _obj.length();
/* 31 */       for (int i = 0; i < length; i++) {
/*    */         Object v;
/*    */         try {
/* 34 */           v = _obj.isNull(i) ? null : _obj.get(i);
/* 35 */         } catch (JSONException e) {
/* 36 */           logger.log(Level.WARNING, "An error occured while retrieving data from JSONArray", (Throwable)e);
/* 37 */           return false;
/*    */         } 
/* 39 */         if (_hasBinary(v)) {
/* 40 */           return true;
/*    */         }
/*    */       } 
/* 43 */     } else if (obj instanceof JSONObject) {
/* 44 */       JSONObject _obj = (JSONObject)obj;
/* 45 */       Iterator<String> keys = _obj.keys();
/* 46 */       while (keys.hasNext()) {
/* 47 */         Object v; String key = keys.next();
/*    */         
/*    */         try {
/* 50 */           v = _obj.get(key);
/* 51 */         } catch (JSONException e) {
/* 52 */           logger.log(Level.WARNING, "An error occured while retrieving data from JSONObject", (Throwable)e);
/* 53 */           return false;
/*    */         } 
/* 55 */         if (_hasBinary(v)) {
/* 56 */           return true;
/*    */         }
/*    */       } 
/*    */     } 
/*    */     
/* 61 */     return false;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\hasbinary\HasBinary.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */