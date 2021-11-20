/*    */ package io.socket.engineio.parser;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Packet<T>
/*    */ {
/*    */   public static final String OPEN = "open";
/*    */   public static final String CLOSE = "close";
/*    */   public static final String PING = "ping";
/*    */   public static final String PONG = "pong";
/*    */   public static final String UPGRADE = "upgrade";
/*    */   public static final String MESSAGE = "message";
/*    */   public static final String NOOP = "noop";
/*    */   public static final String ERROR = "error";
/*    */   public String type;
/*    */   public T data;
/*    */   
/*    */   public Packet(String type) {
/* 20 */     this(type, null);
/*    */   }
/*    */   
/*    */   public Packet(String type, T data) {
/* 24 */     this.type = type;
/* 25 */     this.data = data;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\parser\Packet.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */