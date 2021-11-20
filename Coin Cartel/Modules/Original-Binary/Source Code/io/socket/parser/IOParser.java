/*     */ package io.socket.parser;
/*     */ 
/*     */ import io.socket.hasbinary.HasBinary;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONTokener;
/*     */ 
/*     */ public final class IOParser
/*     */   implements Parser
/*     */ {
/*  15 */   private static final Logger logger = Logger.getLogger(IOParser.class.getName());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final class Encoder
/*     */     implements Parser.Encoder
/*     */   {
/*     */     public void encode(Packet obj, Parser.Encoder.Callback callback) {
/*  25 */       if ((obj.type == 2 || obj.type == 3) && HasBinary.hasBinary(obj.data)) {
/*  26 */         obj.type = (obj.type == 2) ? 5 : 6;
/*     */       }
/*     */       
/*  29 */       if (IOParser.logger.isLoggable(Level.FINE)) {
/*  30 */         IOParser.logger.fine(String.format("encoding packet %s", new Object[] { obj }));
/*     */       }
/*     */       
/*  33 */       if (5 == obj.type || 6 == obj.type) {
/*  34 */         encodeAsBinary(obj, callback);
/*     */       } else {
/*  36 */         String encoding = encodeAsString(obj);
/*  37 */         callback.call((Object[])new String[] { encoding });
/*     */       } 
/*     */     }
/*     */     
/*     */     private String encodeAsString(Packet obj) {
/*  42 */       StringBuilder str = new StringBuilder("" + obj.type);
/*     */       
/*  44 */       if (5 == obj.type || 6 == obj.type) {
/*  45 */         str.append(obj.attachments);
/*  46 */         str.append("-");
/*     */       } 
/*     */       
/*  49 */       if (obj.nsp != null && obj.nsp.length() != 0 && !"/".equals(obj.nsp)) {
/*  50 */         str.append(obj.nsp);
/*  51 */         str.append(",");
/*     */       } 
/*     */       
/*  54 */       if (obj.id >= 0) {
/*  55 */         str.append(obj.id);
/*     */       }
/*     */       
/*  58 */       if (obj.data != null) {
/*  59 */         str.append(obj.data);
/*     */       }
/*     */       
/*  62 */       if (IOParser.logger.isLoggable(Level.FINE)) {
/*  63 */         IOParser.logger.fine(String.format("encoded %s as %s", new Object[] { obj, str }));
/*     */       }
/*  65 */       return str.toString();
/*     */     }
/*     */     
/*     */     private void encodeAsBinary(Packet obj, Parser.Encoder.Callback callback) {
/*  69 */       Binary.DeconstructedPacket deconstruction = Binary.deconstructPacket(obj);
/*  70 */       String pack = encodeAsString(deconstruction.packet);
/*  71 */       List<Object> buffers = new ArrayList(Arrays.asList((Object[])deconstruction.buffers));
/*     */       
/*  73 */       buffers.add(0, pack);
/*  74 */       callback.call(buffers.toArray());
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final class Decoder
/*     */     implements Parser.Decoder
/*     */   {
/*  85 */     IOParser.BinaryReconstructor reconstructor = null;
/*     */     
/*     */     private Parser.Decoder.Callback onDecodedCallback;
/*     */     
/*     */     public void add(String obj) {
/*  90 */       Packet packet = decodeString(obj);
/*  91 */       if (5 == packet.type || 6 == packet.type) {
/*  92 */         this.reconstructor = new IOParser.BinaryReconstructor(packet);
/*     */         
/*  94 */         if (this.reconstructor.reconPack.attachments == 0 && 
/*  95 */           this.onDecodedCallback != null) {
/*  96 */           this.onDecodedCallback.call(packet);
/*     */         
/*     */         }
/*     */       }
/* 100 */       else if (this.onDecodedCallback != null) {
/* 101 */         this.onDecodedCallback.call(packet);
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     public void add(byte[] obj) {
/* 108 */       if (this.reconstructor == null) {
/* 109 */         throw new RuntimeException("got binary data when not reconstructing a packet");
/*     */       }
/* 111 */       Packet packet = this.reconstructor.takeBinaryData(obj);
/* 112 */       if (packet != null) {
/* 113 */         this.reconstructor = null;
/* 114 */         if (this.onDecodedCallback != null) {
/* 115 */           this.onDecodedCallback.call(packet);
/*     */         }
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     private static Packet decodeString(String str) {
/* 122 */       int i = 0;
/* 123 */       int length = str.length();
/*     */       
/* 125 */       Packet<Object> p = new Packet(Character.getNumericValue(str.charAt(0)));
/*     */       
/* 127 */       if (p.type < 0 || p.type > Parser.types.length - 1) {
/* 128 */         throw new DecodingException("unknown packet type " + p.type);
/*     */       }
/*     */       
/* 131 */       if (5 == p.type || 6 == p.type) {
/* 132 */         if (!str.contains("-") || length <= i + 1) {
/* 133 */           throw new DecodingException("illegal attachments");
/*     */         }
/* 135 */         StringBuilder attachments = new StringBuilder();
/* 136 */         while (str.charAt(++i) != '-') {
/* 137 */           attachments.append(str.charAt(i));
/*     */         }
/* 139 */         p.attachments = Integer.parseInt(attachments.toString());
/*     */       } 
/*     */       
/* 142 */       if (length > i + 1 && '/' == str.charAt(i + 1)) {
/* 143 */         StringBuilder nsp = new StringBuilder();
/*     */         do {
/* 145 */           i++;
/* 146 */           char c = str.charAt(i);
/* 147 */           if (',' == c)
/* 148 */             break;  nsp.append(c);
/* 149 */         } while (i + 1 != length);
/*     */         
/* 151 */         p.nsp = nsp.toString();
/*     */       } else {
/* 153 */         p.nsp = "/";
/*     */       } 
/*     */       
/* 156 */       if (length > i + 1) {
/* 157 */         Character next = Character.valueOf(str.charAt(i + 1));
/* 158 */         if (Character.getNumericValue(next.charValue()) > -1) {
/* 159 */           StringBuilder id = new StringBuilder();
/*     */           do {
/* 161 */             i++;
/* 162 */             char c = str.charAt(i);
/* 163 */             if (Character.getNumericValue(c) < 0) {
/* 164 */               i--;
/*     */               break;
/*     */             } 
/* 167 */             id.append(c);
/* 168 */           } while (i + 1 != length);
/*     */           
/*     */           try {
/* 171 */             p.id = Integer.parseInt(id.toString());
/* 172 */           } catch (NumberFormatException e) {
/* 173 */             throw new DecodingException("invalid payload");
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 178 */       if (length > i + 1) {
/*     */         try {
/* 180 */           str.charAt(++i);
/* 181 */           p.data = (T)(new JSONTokener(str.substring(i))).nextValue();
/* 182 */         } catch (JSONException e) {
/* 183 */           IOParser.logger.log(Level.WARNING, "An error occured while retrieving data from JSONTokener", (Throwable)e);
/* 184 */           throw new DecodingException("invalid payload");
/*     */         } 
/*     */       }
/*     */       
/* 188 */       if (IOParser.logger.isLoggable(Level.FINE)) {
/* 189 */         IOParser.logger.fine(String.format("decoded %s as %s", new Object[] { str, p }));
/*     */       }
/* 191 */       return p;
/*     */     }
/*     */ 
/*     */     
/*     */     public void destroy() {
/* 196 */       if (this.reconstructor != null) {
/* 197 */         this.reconstructor.finishReconstruction();
/*     */       }
/* 199 */       this.onDecodedCallback = null;
/*     */     }
/*     */ 
/*     */     
/*     */     public void onDecoded(Parser.Decoder.Callback callback) {
/* 204 */       this.onDecodedCallback = callback;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   static class BinaryReconstructor
/*     */   {
/*     */     public Packet reconPack;
/*     */     
/*     */     List<byte[]> buffers;
/*     */     
/*     */     BinaryReconstructor(Packet packet) {
/* 216 */       this.reconPack = packet;
/* 217 */       this.buffers = (List)new ArrayList<>();
/*     */     }
/*     */     
/*     */     public Packet takeBinaryData(byte[] binData) {
/* 221 */       this.buffers.add(binData);
/* 222 */       if (this.buffers.size() == this.reconPack.attachments) {
/* 223 */         Packet packet = Binary.reconstructPacket(this.reconPack, this.buffers
/* 224 */             .<byte[]>toArray(new byte[this.buffers.size()][]));
/* 225 */         finishReconstruction();
/* 226 */         return packet;
/*     */       } 
/* 228 */       return null;
/*     */     }
/*     */     
/*     */     public void finishReconstruction() {
/* 232 */       this.reconPack = null;
/* 233 */       this.buffers = (List)new ArrayList<>();
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\parser\IOParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */