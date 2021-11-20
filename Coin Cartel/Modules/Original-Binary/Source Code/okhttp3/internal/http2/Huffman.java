/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import okio.BufferedSink;
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
/*     */ class Huffman
/*     */ {
/*  37 */   private static final int[] CODES = new int[] { 8184, 8388568, 268435426, 268435427, 268435428, 268435429, 268435430, 268435431, 268435432, 16777194, 1073741820, 268435433, 268435434, 1073741821, 268435435, 268435436, 268435437, 268435438, 268435439, 268435440, 268435441, 268435442, 1073741822, 268435443, 268435444, 268435445, 268435446, 268435447, 268435448, 268435449, 268435450, 268435451, 20, 1016, 1017, 4090, 8185, 21, 248, 2042, 1018, 1019, 249, 2043, 250, 22, 23, 24, 0, 1, 2, 25, 26, 27, 28, 29, 30, 31, 92, 251, 32764, 32, 4091, 1020, 8186, 33, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 252, 115, 253, 8187, 524272, 8188, 16380, 34, 32765, 3, 35, 4, 36, 5, 37, 38, 39, 6, 116, 117, 40, 41, 42, 7, 43, 118, 44, 8, 9, 45, 119, 120, 121, 122, 123, 32766, 2044, 16381, 8189, 268435452, 1048550, 4194258, 1048551, 1048552, 4194259, 4194260, 4194261, 8388569, 4194262, 8388570, 8388571, 8388572, 8388573, 8388574, 16777195, 8388575, 16777196, 16777197, 4194263, 8388576, 16777198, 8388577, 8388578, 8388579, 8388580, 2097116, 4194264, 8388581, 4194265, 8388582, 8388583, 16777199, 4194266, 2097117, 1048553, 4194267, 4194268, 8388584, 8388585, 2097118, 8388586, 4194269, 4194270, 16777200, 2097119, 4194271, 8388587, 8388588, 2097120, 2097121, 4194272, 2097122, 8388589, 4194273, 8388590, 8388591, 1048554, 4194274, 4194275, 4194276, 8388592, 4194277, 4194278, 8388593, 67108832, 67108833, 1048555, 524273, 4194279, 8388594, 4194280, 33554412, 67108834, 67108835, 67108836, 134217694, 134217695, 67108837, 16777201, 33554413, 524274, 2097123, 67108838, 134217696, 134217697, 67108839, 134217698, 16777202, 2097124, 2097125, 67108840, 67108841, 268435453, 134217699, 134217700, 134217701, 1048556, 16777203, 1048557, 2097126, 4194281, 2097127, 2097128, 8388595, 4194282, 4194283, 33554414, 33554415, 16777204, 16777205, 67108842, 8388596, 67108843, 134217702, 67108844, 67108845, 134217703, 134217704, 134217705, 134217706, 134217707, 268435454, 134217708, 134217709, 134217710, 134217711, 134217712, 67108846 };
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
/*     */ 
/*     */ 
/*     */   
/*  65 */   private static final byte[] CODE_LENGTHS = new byte[] { 13, 23, 28, 28, 28, 28, 28, 28, 28, 24, 30, 28, 28, 30, 28, 28, 28, 28, 28, 28, 28, 28, 30, 28, 28, 28, 28, 28, 28, 28, 28, 28, 6, 10, 10, 12, 13, 6, 8, 11, 10, 10, 8, 11, 8, 6, 6, 6, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 8, 15, 6, 12, 10, 13, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 7, 8, 13, 19, 13, 14, 6, 15, 5, 6, 5, 6, 5, 6, 6, 6, 5, 7, 7, 6, 6, 6, 5, 6, 7, 6, 5, 5, 6, 7, 7, 7, 7, 7, 15, 11, 14, 13, 28, 20, 22, 20, 20, 22, 22, 22, 23, 22, 23, 23, 23, 23, 23, 24, 23, 24, 24, 22, 23, 24, 23, 23, 23, 23, 21, 22, 23, 22, 23, 23, 24, 22, 21, 20, 22, 22, 23, 23, 21, 23, 22, 22, 24, 21, 22, 23, 23, 21, 21, 22, 21, 23, 22, 23, 23, 20, 22, 22, 22, 23, 22, 22, 23, 26, 26, 20, 19, 22, 23, 22, 25, 26, 26, 26, 27, 27, 26, 24, 25, 19, 21, 26, 27, 27, 26, 27, 24, 21, 21, 26, 26, 28, 27, 27, 27, 20, 24, 20, 21, 22, 21, 21, 23, 22, 22, 25, 25, 24, 24, 26, 23, 26, 27, 26, 26, 27, 27, 27, 27, 27, 28, 27, 27, 27, 27, 27, 26 };
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
/*  79 */   private static final Huffman INSTANCE = new Huffman();
/*     */   
/*     */   public static Huffman get() {
/*  82 */     return INSTANCE;
/*     */   }
/*     */   
/*  85 */   private final Node root = new Node();
/*     */   
/*     */   private Huffman() {
/*  88 */     buildTree();
/*     */   }
/*     */   
/*     */   void encode(ByteString data, BufferedSink sink) throws IOException {
/*  92 */     long current = 0L;
/*  93 */     int n = 0;
/*     */     
/*  95 */     for (int i = 0; i < data.size(); i++) {
/*  96 */       int b = data.getByte(i) & 0xFF;
/*  97 */       int code = CODES[b];
/*  98 */       int nbits = CODE_LENGTHS[b];
/*     */       
/* 100 */       current <<= nbits;
/* 101 */       current |= code;
/* 102 */       n += nbits;
/*     */       
/* 104 */       while (n >= 8) {
/* 105 */         n -= 8;
/* 106 */         sink.writeByte((int)(current >> n));
/*     */       } 
/*     */     } 
/*     */     
/* 110 */     if (n > 0) {
/* 111 */       current <<= 8 - n;
/* 112 */       current |= (255 >>> n);
/* 113 */       sink.writeByte((int)current);
/*     */     } 
/*     */   }
/*     */   
/*     */   int encodedLength(ByteString bytes) {
/* 118 */     long len = 0L;
/*     */     
/* 120 */     for (int i = 0; i < bytes.size(); i++) {
/* 121 */       int b = bytes.getByte(i) & 0xFF;
/* 122 */       len += CODE_LENGTHS[b];
/*     */     } 
/*     */     
/* 125 */     return (int)(len + 7L >> 3L);
/*     */   }
/*     */   
/*     */   byte[] decode(byte[] buf) {
/* 129 */     ByteArrayOutputStream baos = new ByteArrayOutputStream();
/* 130 */     Node node = this.root;
/* 131 */     int current = 0;
/* 132 */     int nbits = 0;
/* 133 */     for (int i = 0; i < buf.length; i++) {
/* 134 */       int b = buf[i] & 0xFF;
/* 135 */       current = current << 8 | b;
/* 136 */       nbits += 8;
/* 137 */       while (nbits >= 8) {
/* 138 */         int c = current >>> nbits - 8 & 0xFF;
/* 139 */         node = node.children[c];
/* 140 */         if (node.children == null) {
/*     */           
/* 142 */           baos.write(node.symbol);
/* 143 */           nbits -= node.terminalBits;
/* 144 */           node = this.root;
/*     */           continue;
/*     */         } 
/* 147 */         nbits -= 8;
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 152 */     while (nbits > 0) {
/* 153 */       int c = current << 8 - nbits & 0xFF;
/* 154 */       node = node.children[c];
/* 155 */       if (node.children != null || node.terminalBits > nbits) {
/*     */         break;
/*     */       }
/* 158 */       baos.write(node.symbol);
/* 159 */       nbits -= node.terminalBits;
/* 160 */       node = this.root;
/*     */     } 
/*     */     
/* 163 */     return baos.toByteArray();
/*     */   }
/*     */   
/*     */   private void buildTree() {
/* 167 */     for (int i = 0; i < CODE_LENGTHS.length; i++) {
/* 168 */       addCode(i, CODES[i], CODE_LENGTHS[i]);
/*     */     }
/*     */   }
/*     */   
/*     */   private void addCode(int sym, int code, byte len) {
/* 173 */     Node terminal = new Node(sym, len);
/*     */     
/* 175 */     Node current = this.root;
/* 176 */     while (len > 8) {
/* 177 */       len = (byte)(len - 8);
/* 178 */       int j = code >>> len & 0xFF;
/* 179 */       if (current.children == null) {
/* 180 */         throw new IllegalStateException("invalid dictionary: prefix not unique");
/*     */       }
/* 182 */       if (current.children[j] == null) {
/* 183 */         current.children[j] = new Node();
/*     */       }
/* 185 */       current = current.children[j];
/*     */     } 
/*     */     
/* 188 */     int shift = 8 - len;
/* 189 */     int start = code << shift & 0xFF;
/* 190 */     int end = 1 << shift;
/* 191 */     for (int i = start; i < start + end; i++) {
/* 192 */       current.children[i] = terminal;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static final class Node
/*     */   {
/*     */     final Node[] children;
/*     */ 
/*     */     
/*     */     final int symbol;
/*     */     
/*     */     final int terminalBits;
/*     */ 
/*     */     
/*     */     Node() {
/* 209 */       this.children = new Node[256];
/* 210 */       this.symbol = 0;
/* 211 */       this.terminalBits = 0;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     Node(int symbol, int bits) {
/* 221 */       this.children = null;
/* 222 */       this.symbol = symbol;
/* 223 */       int b = bits & 0x7;
/* 224 */       this.terminalBits = (b == 0) ? 8 : b;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Huffman.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */