/*     */ package okhttp3.internal.http;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.ProtocolException;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.Response;
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
/*     */ public final class StatusLine
/*     */ {
/*     */   public static final int HTTP_TEMP_REDIRECT = 307;
/*     */   public static final int HTTP_PERM_REDIRECT = 308;
/*     */   public static final int HTTP_CONTINUE = 100;
/*     */   public final Protocol protocol;
/*     */   public final int code;
/*     */   public final String message;
/*     */   
/*     */   public StatusLine(Protocol protocol, int code, String message) {
/*  35 */     this.protocol = protocol;
/*  36 */     this.code = code;
/*  37 */     this.message = message;
/*     */   }
/*     */   
/*     */   public static StatusLine get(Response response) {
/*  41 */     return new StatusLine(response.protocol(), response.code(), response.message());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static StatusLine parse(String statusLine) throws IOException {
/*     */     int code, codeStart;
/*     */     Protocol protocol;
/*  51 */     if (statusLine.startsWith("HTTP/1.")) {
/*  52 */       if (statusLine.length() < 9 || statusLine.charAt(8) != ' ') {
/*  53 */         throw new ProtocolException("Unexpected status line: " + statusLine);
/*     */       }
/*  55 */       int httpMinorVersion = statusLine.charAt(7) - 48;
/*  56 */       codeStart = 9;
/*  57 */       if (httpMinorVersion == 0) {
/*  58 */         protocol = Protocol.HTTP_1_0;
/*  59 */       } else if (httpMinorVersion == 1) {
/*  60 */         protocol = Protocol.HTTP_1_1;
/*     */       } else {
/*  62 */         throw new ProtocolException("Unexpected status line: " + statusLine);
/*     */       } 
/*  64 */     } else if (statusLine.startsWith("ICY ")) {
/*     */       
/*  66 */       protocol = Protocol.HTTP_1_0;
/*  67 */       codeStart = 4;
/*     */     } else {
/*  69 */       throw new ProtocolException("Unexpected status line: " + statusLine);
/*     */     } 
/*     */ 
/*     */     
/*  73 */     if (statusLine.length() < codeStart + 3) {
/*  74 */       throw new ProtocolException("Unexpected status line: " + statusLine);
/*     */     }
/*     */     
/*     */     try {
/*  78 */       code = Integer.parseInt(statusLine.substring(codeStart, codeStart + 3));
/*  79 */     } catch (NumberFormatException e) {
/*  80 */       throw new ProtocolException("Unexpected status line: " + statusLine);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/*  85 */     String message = "";
/*  86 */     if (statusLine.length() > codeStart + 3) {
/*  87 */       if (statusLine.charAt(codeStart + 3) != ' ') {
/*  88 */         throw new ProtocolException("Unexpected status line: " + statusLine);
/*     */       }
/*  90 */       message = statusLine.substring(codeStart + 4);
/*     */     } 
/*     */     
/*  93 */     return new StatusLine(protocol, code, message);
/*     */   }
/*     */   
/*     */   public String toString() {
/*  97 */     StringBuilder result = new StringBuilder();
/*  98 */     result.append((this.protocol == Protocol.HTTP_1_0) ? "HTTP/1.0" : "HTTP/1.1");
/*  99 */     result.append(' ').append(this.code);
/* 100 */     if (this.message != null) {
/* 101 */       result.append(' ').append(this.message);
/*     */     }
/* 103 */     return result.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\StatusLine.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */