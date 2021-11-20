/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.IOException;
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
/*     */ public enum Protocol
/*     */ {
/*  33 */   HTTP_1_0("http/1.0"),
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  41 */   HTTP_1_1("http/1.1"),
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  51 */   SPDY_3("spdy/3.1"),
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
/*  62 */   HTTP_2("h2"),
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  71 */   H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  81 */   QUIC("quic");
/*     */   
/*     */   private final String protocol;
/*     */   
/*     */   Protocol(String protocol) {
/*  86 */     this.protocol = protocol;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Protocol get(String protocol) throws IOException {
/*  96 */     if (protocol.equals(HTTP_1_0.protocol)) return HTTP_1_0; 
/*  97 */     if (protocol.equals(HTTP_1_1.protocol)) return HTTP_1_1; 
/*  98 */     if (protocol.equals(H2_PRIOR_KNOWLEDGE.protocol)) return H2_PRIOR_KNOWLEDGE; 
/*  99 */     if (protocol.equals(HTTP_2.protocol)) return HTTP_2; 
/* 100 */     if (protocol.equals(SPDY_3.protocol)) return SPDY_3; 
/* 101 */     if (protocol.equals(QUIC.protocol)) return QUIC; 
/* 102 */     throw new IOException("Unexpected protocol: " + protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 113 */     return this.protocol;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Protocol.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */