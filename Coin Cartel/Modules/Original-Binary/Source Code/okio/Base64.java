/*     */ package okio;
/*     */ 
/*     */ import java.io.UnsupportedEncodingException;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class Base64
/*     */ {
/*     */   public static byte[] decode(String in) {
/*  31 */     int limit = in.length();
/*  32 */     for (; limit > 0; limit--) {
/*  33 */       char c = in.charAt(limit - 1);
/*  34 */       if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
/*     */         break;
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/*  40 */     byte[] out = new byte[(int)(limit * 6L / 8L)];
/*  41 */     int outCount = 0;
/*  42 */     int inCount = 0;
/*     */     
/*  44 */     int word = 0;
/*  45 */     int pos = 0; while (true) { if (pos < limit)
/*  46 */       { int bits; char c = in.charAt(pos);
/*     */ 
/*     */         
/*  49 */         if (c >= 'A' && c <= 'Z')
/*     */         
/*     */         { 
/*     */           
/*  53 */           bits = c - 65; }
/*  54 */         else if (c >= 'a' && c <= 'z')
/*     */         
/*     */         { 
/*     */           
/*  58 */           bits = c - 71; }
/*  59 */         else if (c >= '0' && c <= '9')
/*     */         
/*     */         { 
/*     */           
/*  63 */           bits = c + 4; }
/*  64 */         else if (c == '+' || c == '-')
/*  65 */         { bits = 62; }
/*  66 */         else if (c == '/' || c == '_')
/*  67 */         { bits = 63; }
/*  68 */         else { if (c != '\n' && c != '\r' && c != ' ' && c != '\t')
/*     */           {
/*     */             
/*  71 */             return null;
/*     */           }
/*     */           pos++; }
/*     */         
/*  75 */         word = word << 6 | (byte)bits;
/*     */ 
/*     */         
/*  78 */         inCount++;
/*  79 */         if (inCount % 4 == 0) {
/*  80 */           out[outCount++] = (byte)(word >> 16);
/*  81 */           out[outCount++] = (byte)(word >> 8);
/*  82 */           out[outCount++] = (byte)word;
/*     */         }  }
/*     */       else { break; }
/*     */        pos++; }
/*  86 */      int lastWordChars = inCount % 4;
/*  87 */     if (lastWordChars == 1)
/*     */     {
/*  89 */       return null; } 
/*  90 */     if (lastWordChars == 2) {
/*     */       
/*  92 */       word <<= 12;
/*  93 */       out[outCount++] = (byte)(word >> 16);
/*  94 */     } else if (lastWordChars == 3) {
/*     */       
/*  96 */       word <<= 6;
/*  97 */       out[outCount++] = (byte)(word >> 16);
/*  98 */       out[outCount++] = (byte)(word >> 8);
/*     */     } 
/*     */ 
/*     */     
/* 102 */     if (outCount == out.length) return out;
/*     */ 
/*     */     
/* 105 */     byte[] prefix = new byte[outCount];
/* 106 */     System.arraycopy(out, 0, prefix, 0, outCount);
/* 107 */     return prefix;
/*     */   }
/*     */   
/* 110 */   private static final byte[] MAP = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 117 */   private static final byte[] URL_MAP = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String encode(byte[] in) {
/* 125 */     return encode(in, MAP);
/*     */   }
/*     */   
/*     */   public static String encodeUrl(byte[] in) {
/* 129 */     return encode(in, URL_MAP);
/*     */   }
/*     */   
/*     */   private static String encode(byte[] in, byte[] map) {
/* 133 */     int length = (in.length + 2) / 3 * 4;
/* 134 */     byte[] out = new byte[length];
/* 135 */     int index = 0, end = in.length - in.length % 3;
/* 136 */     for (int i = 0; i < end; i += 3) {
/* 137 */       out[index++] = map[(in[i] & 0xFF) >> 2];
/* 138 */       out[index++] = map[(in[i] & 0x3) << 4 | (in[i + 1] & 0xFF) >> 4];
/* 139 */       out[index++] = map[(in[i + 1] & 0xF) << 2 | (in[i + 2] & 0xFF) >> 6];
/* 140 */       out[index++] = map[in[i + 2] & 0x3F];
/*     */     } 
/* 142 */     switch (in.length % 3) {
/*     */       case 1:
/* 144 */         out[index++] = map[(in[end] & 0xFF) >> 2];
/* 145 */         out[index++] = map[(in[end] & 0x3) << 4];
/* 146 */         out[index++] = 61;
/* 147 */         out[index++] = 61;
/*     */         break;
/*     */       case 2:
/* 150 */         out[index++] = map[(in[end] & 0xFF) >> 2];
/* 151 */         out[index++] = map[(in[end] & 0x3) << 4 | (in[end + 1] & 0xFF) >> 4];
/* 152 */         out[index++] = map[(in[end + 1] & 0xF) << 2];
/* 153 */         out[index++] = 61;
/*     */         break;
/*     */     } 
/*     */     try {
/* 157 */       return new String(out, "US-ASCII");
/* 158 */     } catch (UnsupportedEncodingException e) {
/* 159 */       throw new AssertionError(e);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Base64.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */