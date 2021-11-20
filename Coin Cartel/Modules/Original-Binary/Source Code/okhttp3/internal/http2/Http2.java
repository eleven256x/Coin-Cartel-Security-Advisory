/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.ByteString;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class Http2
/*     */ {
/*  25 */   static final ByteString CONNECTION_PREFACE = ByteString.encodeUtf8("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n");
/*     */   
/*     */   static final int INITIAL_MAX_FRAME_SIZE = 16384;
/*     */   
/*     */   static final byte TYPE_DATA = 0;
/*     */   
/*     */   static final byte TYPE_HEADERS = 1;
/*     */   
/*     */   static final byte TYPE_PRIORITY = 2;
/*     */   
/*     */   static final byte TYPE_RST_STREAM = 3;
/*     */   
/*     */   static final byte TYPE_SETTINGS = 4;
/*     */   static final byte TYPE_PUSH_PROMISE = 5;
/*     */   static final byte TYPE_PING = 6;
/*     */   static final byte TYPE_GOAWAY = 7;
/*     */   static final byte TYPE_WINDOW_UPDATE = 8;
/*     */   static final byte TYPE_CONTINUATION = 9;
/*     */   static final byte FLAG_NONE = 0;
/*     */   static final byte FLAG_ACK = 1;
/*     */   static final byte FLAG_END_STREAM = 1;
/*     */   static final byte FLAG_END_HEADERS = 4;
/*     */   static final byte FLAG_END_PUSH_PROMISE = 4;
/*     */   static final byte FLAG_PADDED = 8;
/*     */   static final byte FLAG_PRIORITY = 32;
/*     */   static final byte FLAG_COMPRESSED = 32;
/*  51 */   private static final String[] FRAME_NAMES = new String[] { "DATA", "HEADERS", "PRIORITY", "RST_STREAM", "SETTINGS", "PUSH_PROMISE", "PING", "GOAWAY", "WINDOW_UPDATE", "CONTINUATION" };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  68 */   static final String[] FLAGS = new String[64];
/*  69 */   static final String[] BINARY = new String[256];
/*     */   static {
/*  71 */     for (int i = 0; i < BINARY.length; i++) {
/*  72 */       BINARY[i] = Util.format("%8s", new Object[] { Integer.toBinaryString(i) }).replace(' ', '0');
/*     */     } 
/*     */     
/*  75 */     FLAGS[0] = "";
/*  76 */     FLAGS[1] = "END_STREAM";
/*     */     
/*  78 */     int[] prefixFlags = { 1 };
/*     */     
/*  80 */     FLAGS[8] = "PADDED";
/*  81 */     for (int prefixFlag : prefixFlags) {
/*  82 */       FLAGS[prefixFlag | 0x8] = FLAGS[prefixFlag] + "|PADDED";
/*     */     }
/*     */     
/*  85 */     FLAGS[4] = "END_HEADERS";
/*  86 */     FLAGS[32] = "PRIORITY";
/*  87 */     FLAGS[36] = "END_HEADERS|PRIORITY";
/*  88 */     int[] frameFlags = { 4, 32, 36 };
/*     */ 
/*     */ 
/*     */     
/*  92 */     for (int frameFlag : frameFlags) {
/*  93 */       for (int prefixFlag : prefixFlags) {
/*  94 */         FLAGS[prefixFlag | frameFlag] = FLAGS[prefixFlag] + '|' + FLAGS[frameFlag];
/*  95 */         FLAGS[prefixFlag | frameFlag | 0x8] = FLAGS[prefixFlag] + '|' + FLAGS[frameFlag] + "|PADDED";
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 100 */     for (int j = 0; j < FLAGS.length; j++) {
/* 101 */       if (FLAGS[j] == null) FLAGS[j] = BINARY[j];
/*     */     
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   static IllegalArgumentException illegalArgument(String message, Object... args) {
/* 109 */     throw new IllegalArgumentException(Util.format(message, args));
/*     */   }
/*     */   
/*     */   static IOException ioException(String message, Object... args) throws IOException {
/* 113 */     throw new IOException(Util.format(message, args));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static String frameLog(boolean inbound, int streamId, int length, byte type, byte flags) {
/* 135 */     String formattedType = (type < FRAME_NAMES.length) ? FRAME_NAMES[type] : Util.format("0x%02x", new Object[] { Byte.valueOf(type) });
/* 136 */     String formattedFlags = formatFlags(type, flags);
/* 137 */     return Util.format("%s 0x%08x %5d %-13s %s", new Object[] { inbound ? "<<" : ">>", Integer.valueOf(streamId), Integer.valueOf(length), formattedType, formattedFlags });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static String formatFlags(byte type, byte flags) {
/* 147 */     if (flags == 0) return ""; 
/* 148 */     switch (type) {
/*     */       case 4:
/*     */       case 6:
/* 151 */         return (flags == 1) ? "ACK" : BINARY[flags];
/*     */       case 2:
/*     */       case 3:
/*     */       case 7:
/*     */       case 8:
/* 156 */         return BINARY[flags];
/*     */     } 
/* 158 */     String result = (flags < FLAGS.length) ? FLAGS[flags] : BINARY[flags];
/*     */     
/* 160 */     if (type == 5 && (flags & 0x4) != 0)
/* 161 */       return result.replace("HEADERS", "PUSH_PROMISE"); 
/* 162 */     if (type == 0 && (flags & 0x20) != 0) {
/* 163 */       return result.replace("PRIORITY", "COMPRESSED");
/*     */     }
/* 165 */     return result;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Http2.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */