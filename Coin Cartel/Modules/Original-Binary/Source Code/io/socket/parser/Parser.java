/*    */ package io.socket.parser;
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
/*    */ public interface Parser
/*    */ {
/*    */   public static final int CONNECT = 0;
/*    */   public static final int DISCONNECT = 1;
/*    */   public static final int EVENT = 2;
/*    */   public static final int ACK = 3;
/*    */   public static final int CONNECT_ERROR = 4;
/*    */   public static final int BINARY_EVENT = 5;
/*    */   public static final int BINARY_ACK = 6;
/*    */   public static final int protocol = 5;
/* 45 */   public static final String[] types = new String[] { "CONNECT", "DISCONNECT", "EVENT", "ACK", "ERROR", "BINARY_EVENT", "BINARY_ACK" };
/*    */   
/*    */   public static interface Decoder {
/*    */     void add(String param1String);
/*    */     
/*    */     void add(byte[] param1ArrayOfbyte);
/*    */     
/*    */     void destroy();
/*    */     
/*    */     void onDecoded(Callback param1Callback);
/*    */     
/*    */     public static interface Callback {
/*    */       void call(Packet param2Packet);
/*    */     }
/*    */   }
/*    */   
/*    */   public static interface Encoder {
/*    */     void encode(Packet param1Packet, Callback param1Callback);
/*    */     
/*    */     public static interface Callback {
/*    */       void call(Object[] param2ArrayOfObject);
/*    */     }
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\parser\Parser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */