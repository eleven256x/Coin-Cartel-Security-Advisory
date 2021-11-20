/*    */ package org.json;
/*    */ 
/*    */ import java.util.Enumeration;
/*    */ import java.util.Iterator;
/*    */ import java.util.Properties;
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
/*    */ public class Property
/*    */ {
/*    */   public static JSONObject toJSONObject(Properties properties) throws JSONException {
/* 44 */     JSONObject jo = new JSONObject();
/* 45 */     if (properties != null && !properties.isEmpty()) {
/* 46 */       Enumeration enumProperties = properties.propertyNames();
/* 47 */       while (enumProperties.hasMoreElements()) {
/* 48 */         String name = (String)enumProperties.nextElement();
/* 49 */         jo.put(name, properties.getProperty(name));
/*    */       } 
/*    */     } 
/* 52 */     return jo;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static Properties toProperties(JSONObject jo) throws JSONException {
/* 63 */     Properties properties = new Properties();
/* 64 */     if (jo != null) {
/* 65 */       Iterator keys = jo.keys();
/*    */       
/* 67 */       while (keys.hasNext()) {
/* 68 */         String name = keys.next().toString();
/* 69 */         properties.put(name, jo.getString(name));
/*    */       } 
/*    */     } 
/* 72 */     return properties;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\Property.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */