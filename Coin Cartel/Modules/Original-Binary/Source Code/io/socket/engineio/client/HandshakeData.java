/*    */ package io.socket.engineio.client;
/*    */ 
/*    */ import org.json.JSONArray;
/*    */ import org.json.JSONException;
/*    */ import org.json.JSONObject;
/*    */ 
/*    */ 
/*    */ public class HandshakeData
/*    */ {
/*    */   public String sid;
/*    */   public String[] upgrades;
/*    */   public long pingInterval;
/*    */   public long pingTimeout;
/*    */   
/*    */   HandshakeData(String data) throws JSONException {
/* 16 */     this(new JSONObject(data));
/*    */   }
/*    */   
/*    */   HandshakeData(JSONObject data) throws JSONException {
/* 20 */     JSONArray upgrades = data.getJSONArray("upgrades");
/* 21 */     int length = upgrades.length();
/* 22 */     String[] tempUpgrades = new String[length];
/* 23 */     for (int i = 0; i < length; i++) {
/* 24 */       tempUpgrades[i] = upgrades.getString(i);
/*    */     }
/*    */     
/* 27 */     this.sid = data.getString("sid");
/* 28 */     this.upgrades = tempUpgrades;
/* 29 */     this.pingInterval = data.getLong("pingInterval");
/* 30 */     this.pingTimeout = data.getLong("pingTimeout");
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\HandshakeData.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */