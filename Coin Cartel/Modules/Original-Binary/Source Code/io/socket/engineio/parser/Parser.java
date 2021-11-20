/*     */ package io.socket.engineio.parser;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Parser
/*     */ {
/*     */   public static final int PROTOCOL = 4;
/*     */   private static final char SEPARATOR = '\036';
/*  12 */   private static final Map<String, Integer> packets = new HashMap<String, Integer>()
/*     */     {
/*     */     
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  22 */   private static final Map<Integer, String> packetslist = new HashMap<>();
/*     */   static {
/*  24 */     for (Map.Entry<String, Integer> entry : packets.entrySet()) {
/*  25 */       packetslist.put(entry.getValue(), entry.getKey());
/*     */     }
/*     */   }
/*     */   
/*  29 */   private static final Packet<String> err = new Packet<>("error", "parser error");
/*     */ 
/*     */ 
/*     */   
/*     */   public static void encodePacket(Packet packet, EncodeCallback<T> callback) {
/*  34 */     if (packet.data instanceof byte[]) {
/*  35 */       callback.call(packet.data);
/*     */     } else {
/*  37 */       String type = String.valueOf(packets.get(packet.type));
/*  38 */       String content = (packet.data != null) ? String.valueOf(packet.data) : "";
/*  39 */       callback.call((T)(type + content));
/*     */     } 
/*     */   }
/*     */   
/*     */   private static void encodePacketAsBase64(Packet packet, EncodeCallback<String> callback) {
/*  44 */     if (packet.data instanceof byte[]) {
/*  45 */       byte[] data = (byte[])packet.data;
/*  46 */       String value = "b" + Base64.encodeToString(data, 0);
/*  47 */       callback.call(value);
/*     */     } else {
/*  49 */       encodePacket(packet, callback);
/*     */     } 
/*     */   }
/*     */   public static Packet<String> decodePacket(String data) {
/*     */     int type;
/*  54 */     if (data == null) {
/*  55 */       return err;
/*     */     }
/*     */ 
/*     */     
/*     */     try {
/*  60 */       type = Character.getNumericValue(data.charAt(0));
/*  61 */     } catch (IndexOutOfBoundsException e) {
/*  62 */       type = -1;
/*     */     } 
/*     */     
/*  65 */     if (type < 0 || type >= packetslist.size()) {
/*  66 */       return err;
/*     */     }
/*     */     
/*  69 */     if (data.length() > 1) {
/*  70 */       return new Packet<>(packetslist.get(Integer.valueOf(type)), data.substring(1));
/*     */     }
/*  72 */     return new Packet<>(packetslist.get(Integer.valueOf(type)));
/*     */   }
/*     */ 
/*     */   
/*     */   public static Packet decodeBase64Packet(String data) {
/*  77 */     if (data == null) {
/*  78 */       return err;
/*     */     }
/*     */     
/*  81 */     if (data.charAt(0) == 'b') {
/*  82 */       return new Packet<>("message", Base64.decode(data.substring(1), 0));
/*     */     }
/*  84 */     return decodePacket(data);
/*     */   }
/*     */ 
/*     */   
/*     */   public static Packet<byte[]> decodePacket(byte[] data) {
/*  89 */     return (Packet)new Packet<>("message", data);
/*     */   }
/*     */   
/*     */   public static void encodePayload(Packet[] packets, EncodeCallback<String> callback) {
/*  93 */     if (packets.length == 0) {
/*  94 */       callback.call("0:");
/*     */       
/*     */       return;
/*     */     } 
/*  98 */     final StringBuilder result = new StringBuilder();
/*     */     
/* 100 */     for (int i = 0, l = packets.length; i < l; i++) {
/* 101 */       final boolean isLast = (i == l - 1);
/* 102 */       encodePacketAsBase64(packets[i], new EncodeCallback<String>()
/*     */           {
/*     */             public void call(String message) {
/* 105 */               result.append(message);
/* 106 */               if (!isLast) {
/* 107 */                 result.append('\036');
/*     */               }
/*     */             }
/*     */           });
/*     */     } 
/*     */     
/* 113 */     callback.call(result.toString());
/*     */   }
/*     */   
/*     */   public static void decodePayload(String data, DecodePayloadCallback<String> callback) {
/* 117 */     if (data == null || data.length() == 0) {
/* 118 */       callback.call(err, 0, 1);
/*     */       
/*     */       return;
/*     */     } 
/* 122 */     String[] messages = data.split(String.valueOf('\036'));
/*     */     
/* 124 */     for (int i = 0, l = messages.length; i < l; i++) {
/* 125 */       Packet<String> packet = decodeBase64Packet(messages[i]);
/* 126 */       if (err.type.equals(packet.type) && ((String)err.data).equals(packet.data)) {
/* 127 */         callback.call(err, 0, 1);
/*     */         
/*     */         return;
/*     */       } 
/* 131 */       boolean ret = callback.call(packet, i, l);
/* 132 */       if (!ret)
/*     */         return; 
/*     */     } 
/*     */   }
/*     */   
/*     */   public static interface EncodeCallback<T> {
/*     */     void call(T param1T);
/*     */   }
/*     */   
/*     */   public static interface DecodePayloadCallback<T> {
/*     */     boolean call(Packet<T> param1Packet, int param1Int1, int param1Int2);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\parser\Parser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */