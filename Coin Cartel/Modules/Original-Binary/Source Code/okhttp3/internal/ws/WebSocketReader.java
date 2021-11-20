/*     */ package okhttp3.internal.ws;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.ProtocolException;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSource;
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
/*     */ final class WebSocketReader
/*     */ {
/*     */   final boolean isClient;
/*     */   final BufferedSource source;
/*     */   final FrameCallback frameCallback;
/*     */   boolean closed;
/*     */   int opcode;
/*     */   long frameLength;
/*     */   boolean isFinalFrame;
/*     */   boolean isControlFrame;
/*  72 */   private final Buffer controlFrameBuffer = new Buffer();
/*  73 */   private final Buffer messageFrameBuffer = new Buffer();
/*     */   
/*     */   private final byte[] maskKey;
/*     */   private final Buffer.UnsafeCursor maskCursor;
/*     */   
/*     */   WebSocketReader(boolean isClient, BufferedSource source, FrameCallback frameCallback) {
/*  79 */     if (source == null) throw new NullPointerException("source == null"); 
/*  80 */     if (frameCallback == null) throw new NullPointerException("frameCallback == null"); 
/*  81 */     this.isClient = isClient;
/*  82 */     this.source = source;
/*  83 */     this.frameCallback = frameCallback;
/*     */ 
/*     */     
/*  86 */     this.maskKey = isClient ? null : new byte[4];
/*  87 */     this.maskCursor = isClient ? null : new Buffer.UnsafeCursor();
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
/*     */   void processNextFrame() throws IOException {
/* 101 */     readHeader();
/* 102 */     if (this.isControlFrame) {
/* 103 */       readControlFrame();
/*     */     } else {
/* 105 */       readMessageFrame();
/*     */     } 
/*     */   }
/*     */   private void readHeader() throws IOException {
/*     */     int b0;
/* 110 */     if (this.closed) throw new IOException("closed");
/*     */ 
/*     */ 
/*     */     
/* 114 */     long timeoutBefore = this.source.timeout().timeoutNanos();
/* 115 */     this.source.timeout().clearTimeout();
/*     */     try {
/* 117 */       b0 = this.source.readByte() & 0xFF;
/*     */     } finally {
/* 119 */       this.source.timeout().timeout(timeoutBefore, TimeUnit.NANOSECONDS);
/*     */     } 
/*     */     
/* 122 */     this.opcode = b0 & 0xF;
/* 123 */     this.isFinalFrame = ((b0 & 0x80) != 0);
/* 124 */     this.isControlFrame = ((b0 & 0x8) != 0);
/*     */ 
/*     */     
/* 127 */     if (this.isControlFrame && !this.isFinalFrame) {
/* 128 */       throw new ProtocolException("Control frames must be final.");
/*     */     }
/*     */     
/* 131 */     boolean reservedFlag1 = ((b0 & 0x40) != 0);
/* 132 */     boolean reservedFlag2 = ((b0 & 0x20) != 0);
/* 133 */     boolean reservedFlag3 = ((b0 & 0x10) != 0);
/* 134 */     if (reservedFlag1 || reservedFlag2 || reservedFlag3)
/*     */     {
/* 136 */       throw new ProtocolException("Reserved flags are unsupported.");
/*     */     }
/*     */     
/* 139 */     int b1 = this.source.readByte() & 0xFF;
/*     */     
/* 141 */     boolean isMasked = ((b1 & 0x80) != 0);
/* 142 */     if (isMasked == this.isClient)
/*     */     {
/* 144 */       throw new ProtocolException(this.isClient ? 
/* 145 */           "Server-sent frames must not be masked." : 
/* 146 */           "Client-sent frames must be masked.");
/*     */     }
/*     */ 
/*     */     
/* 150 */     this.frameLength = (b1 & 0x7F);
/* 151 */     if (this.frameLength == 126L) {
/* 152 */       this.frameLength = this.source.readShort() & 0xFFFFL;
/* 153 */     } else if (this.frameLength == 127L) {
/* 154 */       this.frameLength = this.source.readLong();
/* 155 */       if (this.frameLength < 0L) {
/* 156 */         throw new ProtocolException("Frame length 0x" + 
/* 157 */             Long.toHexString(this.frameLength) + " > 0x7FFFFFFFFFFFFFFF");
/*     */       }
/*     */     } 
/*     */     
/* 161 */     if (this.isControlFrame && this.frameLength > 125L) {
/* 162 */       throw new ProtocolException("Control frame must be less than 125B.");
/*     */     }
/*     */     
/* 165 */     if (isMasked)
/*     */     {
/* 167 */       this.source.readFully(this.maskKey); } 
/*     */   } private void readControlFrame() throws IOException {
/*     */     int code;
/*     */     String reason;
/*     */     long bufferSize;
/* 172 */     if (this.frameLength > 0L) {
/* 173 */       this.source.readFully(this.controlFrameBuffer, this.frameLength);
/*     */       
/* 175 */       if (!this.isClient) {
/* 176 */         this.controlFrameBuffer.readAndWriteUnsafe(this.maskCursor);
/* 177 */         this.maskCursor.seek(0L);
/* 178 */         WebSocketProtocol.toggleMask(this.maskCursor, this.maskKey);
/* 179 */         this.maskCursor.close();
/*     */       } 
/*     */     } 
/*     */     
/* 183 */     switch (this.opcode) {
/*     */       case 9:
/* 185 */         this.frameCallback.onReadPing(this.controlFrameBuffer.readByteString());
/*     */         return;
/*     */       case 10:
/* 188 */         this.frameCallback.onReadPong(this.controlFrameBuffer.readByteString());
/*     */         return;
/*     */       case 8:
/* 191 */         code = 1005;
/* 192 */         reason = "";
/* 193 */         bufferSize = this.controlFrameBuffer.size();
/* 194 */         if (bufferSize == 1L)
/* 195 */           throw new ProtocolException("Malformed close payload length of 1."); 
/* 196 */         if (bufferSize != 0L) {
/* 197 */           code = this.controlFrameBuffer.readShort();
/* 198 */           reason = this.controlFrameBuffer.readUtf8();
/* 199 */           String codeExceptionMessage = WebSocketProtocol.closeCodeExceptionMessage(code);
/* 200 */           if (codeExceptionMessage != null) throw new ProtocolException(codeExceptionMessage); 
/*     */         } 
/* 202 */         this.frameCallback.onReadClose(code, reason);
/* 203 */         this.closed = true;
/*     */         return;
/*     */     } 
/* 206 */     throw new ProtocolException("Unknown control opcode: " + Integer.toHexString(this.opcode));
/*     */   }
/*     */ 
/*     */   
/*     */   private void readMessageFrame() throws IOException {
/* 211 */     int opcode = this.opcode;
/* 212 */     if (opcode != 1 && opcode != 2) {
/* 213 */       throw new ProtocolException("Unknown opcode: " + Integer.toHexString(opcode));
/*     */     }
/*     */     
/* 216 */     readMessage();
/*     */     
/* 218 */     if (opcode == 1) {
/* 219 */       this.frameCallback.onReadMessage(this.messageFrameBuffer.readUtf8());
/*     */     } else {
/* 221 */       this.frameCallback.onReadMessage(this.messageFrameBuffer.readByteString());
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void readUntilNonControlFrame() throws IOException {
/* 227 */     while (!this.closed) {
/* 228 */       readHeader();
/* 229 */       if (!this.isControlFrame) {
/*     */         break;
/*     */       }
/* 232 */       readControlFrame();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void readMessage() throws IOException {
/*     */     while (true) {
/* 243 */       if (this.closed) throw new IOException("closed");
/*     */       
/* 245 */       if (this.frameLength > 0L) {
/* 246 */         this.source.readFully(this.messageFrameBuffer, this.frameLength);
/*     */         
/* 248 */         if (!this.isClient) {
/* 249 */           this.messageFrameBuffer.readAndWriteUnsafe(this.maskCursor);
/* 250 */           this.maskCursor.seek(this.messageFrameBuffer.size() - this.frameLength);
/* 251 */           WebSocketProtocol.toggleMask(this.maskCursor, this.maskKey);
/* 252 */           this.maskCursor.close();
/*     */         } 
/*     */       } 
/*     */       
/* 256 */       if (this.isFinalFrame)
/*     */         break; 
/* 258 */       readUntilNonControlFrame();
/* 259 */       if (this.opcode != 0)
/* 260 */         throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(this.opcode)); 
/*     */     } 
/*     */   }
/*     */   
/*     */   public static interface FrameCallback {
/*     */     void onReadMessage(String param1String) throws IOException;
/*     */     
/*     */     void onReadMessage(ByteString param1ByteString) throws IOException;
/*     */     
/*     */     void onReadPing(ByteString param1ByteString);
/*     */     
/*     */     void onReadPong(ByteString param1ByteString);
/*     */     
/*     */     void onReadClose(int param1Int, String param1String);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\ws\WebSocketReader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */