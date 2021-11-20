/*     */ package io.socket.parser;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Binary
/*     */ {
/*     */   private static final String KEY_PLACEHOLDER = "_placeholder";
/*     */   private static final String KEY_NUM = "num";
/*  19 */   private static final Logger logger = Logger.getLogger(Binary.class.getName());
/*     */ 
/*     */   
/*     */   public static DeconstructedPacket deconstructPacket(Packet packet) {
/*  23 */     List<byte[]> buffers = (List)new ArrayList<>();
/*     */     
/*  25 */     packet.data = (T)_deconstructPacket(packet.data, buffers);
/*  26 */     packet.attachments = buffers.size();
/*     */     
/*  28 */     DeconstructedPacket result = new DeconstructedPacket();
/*  29 */     result.packet = packet;
/*  30 */     result.buffers = buffers.<byte[]>toArray(new byte[buffers.size()][]);
/*  31 */     return result;
/*     */   }
/*     */   
/*     */   private static Object _deconstructPacket(Object data, List<byte[]> buffers) {
/*  35 */     if (data == null) return null;
/*     */     
/*  37 */     if (data instanceof byte[]) {
/*  38 */       JSONObject placeholder = new JSONObject();
/*     */       try {
/*  40 */         placeholder.put("_placeholder", true);
/*  41 */         placeholder.put("num", buffers.size());
/*  42 */       } catch (JSONException e) {
/*  43 */         logger.log(Level.WARNING, "An error occured while putting data to JSONObject", (Throwable)e);
/*  44 */         return null;
/*     */       } 
/*  46 */       buffers.add((byte[])data);
/*  47 */       return placeholder;
/*  48 */     }  if (data instanceof JSONArray) {
/*  49 */       JSONArray newData = new JSONArray();
/*  50 */       JSONArray _data = (JSONArray)data;
/*  51 */       int len = _data.length();
/*  52 */       for (int i = 0; i < len; i++) {
/*     */         try {
/*  54 */           newData.put(i, _deconstructPacket(_data.get(i), buffers));
/*  55 */         } catch (JSONException e) {
/*  56 */           logger.log(Level.WARNING, "An error occured while putting packet data to JSONObject", (Throwable)e);
/*  57 */           return null;
/*     */         } 
/*     */       } 
/*  60 */       return newData;
/*  61 */     }  if (data instanceof JSONObject) {
/*  62 */       JSONObject newData = new JSONObject();
/*  63 */       JSONObject _data = (JSONObject)data;
/*  64 */       Iterator<?> iterator = _data.keys();
/*  65 */       while (iterator.hasNext()) {
/*  66 */         String key = (String)iterator.next();
/*     */         try {
/*  68 */           newData.put(key, _deconstructPacket(_data.get(key), buffers));
/*  69 */         } catch (JSONException e) {
/*  70 */           logger.log(Level.WARNING, "An error occured while putting data to JSONObject", (Throwable)e);
/*  71 */           return null;
/*     */         } 
/*     */       } 
/*  74 */       return newData;
/*     */     } 
/*  76 */     return data;
/*     */   }
/*     */ 
/*     */   
/*     */   public static Packet reconstructPacket(Packet packet, byte[][] buffers) {
/*  81 */     packet.data = (T)_reconstructPacket(packet.data, buffers);
/*  82 */     packet.attachments = -1;
/*  83 */     return packet;
/*     */   }
/*     */   
/*     */   private static Object _reconstructPacket(Object data, byte[][] buffers) {
/*  87 */     if (data instanceof JSONArray) {
/*  88 */       JSONArray _data = (JSONArray)data;
/*  89 */       int len = _data.length();
/*  90 */       for (int i = 0; i < len; i++) {
/*     */         try {
/*  92 */           _data.put(i, _reconstructPacket(_data.get(i), buffers));
/*  93 */         } catch (JSONException e) {
/*  94 */           logger.log(Level.WARNING, "An error occured while putting packet data to JSONObject", (Throwable)e);
/*  95 */           return null;
/*     */         } 
/*     */       } 
/*  98 */       return _data;
/*  99 */     }  if (data instanceof JSONObject) {
/* 100 */       JSONObject _data = (JSONObject)data;
/* 101 */       if (_data.optBoolean("_placeholder")) {
/* 102 */         int num = _data.optInt("num", -1);
/* 103 */         return (num >= 0 && num < buffers.length) ? buffers[num] : null;
/*     */       } 
/* 105 */       Iterator<?> iterator = _data.keys();
/* 106 */       while (iterator.hasNext()) {
/* 107 */         String key = (String)iterator.next();
/*     */         try {
/* 109 */           _data.put(key, _reconstructPacket(_data.get(key), buffers));
/* 110 */         } catch (JSONException e) {
/* 111 */           logger.log(Level.WARNING, "An error occured while putting data to JSONObject", (Throwable)e);
/* 112 */           return null;
/*     */         } 
/*     */       } 
/* 115 */       return _data;
/*     */     } 
/* 117 */     return data;
/*     */   }
/*     */   
/*     */   public static class DeconstructedPacket {
/*     */     public Packet packet;
/*     */     public byte[][] buffers;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\parser\Binary.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */