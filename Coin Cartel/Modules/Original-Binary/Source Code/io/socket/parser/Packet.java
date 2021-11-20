/*    */ package io.socket.parser;
/*    */ 
/*    */ 
/*    */ public class Packet<T>
/*    */ {
/*  6 */   public int type = -1;
/*  7 */   public int id = -1;
/*    */   public String nsp;
/*    */   public T data;
/*    */   public int attachments;
/*    */   
/*    */   public Packet() {}
/*    */   
/*    */   public Packet(int type) {
/* 15 */     this.type = type;
/*    */   }
/*    */   
/*    */   public Packet(int type, T data) {
/* 19 */     this.type = type;
/* 20 */     this.data = data;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\parser\Packet.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */