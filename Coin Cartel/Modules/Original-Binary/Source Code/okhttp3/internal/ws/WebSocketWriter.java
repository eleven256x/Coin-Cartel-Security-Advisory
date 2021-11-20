/*     */ package okhttp3.internal.ws;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.Random;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
/*     */ import okio.ByteString;
/*     */ import okio.Sink;
/*     */ import okio.Timeout;
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
/*     */ final class WebSocketWriter
/*     */ {
/*     */   final boolean isClient;
/*     */   final Random random;
/*     */   final BufferedSink sink;
/*     */   final Buffer sinkBuffer;
/*     */   boolean writerClosed;
/*  53 */   final Buffer buffer = new Buffer();
/*  54 */   final FrameSink frameSink = new FrameSink();
/*     */   
/*     */   boolean activeWriter;
/*     */   
/*     */   private final byte[] maskKey;
/*     */   private final Buffer.UnsafeCursor maskCursor;
/*     */   
/*     */   WebSocketWriter(boolean isClient, BufferedSink sink, Random random) {
/*  62 */     if (sink == null) throw new NullPointerException("sink == null"); 
/*  63 */     if (random == null) throw new NullPointerException("random == null"); 
/*  64 */     this.isClient = isClient;
/*  65 */     this.sink = sink;
/*  66 */     this.sinkBuffer = sink.buffer();
/*  67 */     this.random = random;
/*     */ 
/*     */     
/*  70 */     this.maskKey = isClient ? new byte[4] : null;
/*  71 */     this.maskCursor = isClient ? new Buffer.UnsafeCursor() : null;
/*     */   }
/*     */ 
/*     */   
/*     */   void writePing(ByteString payload) throws IOException {
/*  76 */     writeControlFrame(9, payload);
/*     */   }
/*     */ 
/*     */   
/*     */   void writePong(ByteString payload) throws IOException {
/*  81 */     writeControlFrame(10, payload);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   void writeClose(int code, ByteString reason) throws IOException {
/*  92 */     ByteString payload = ByteString.EMPTY;
/*  93 */     if (code != 0 || reason != null) {
/*  94 */       if (code != 0) {
/*  95 */         WebSocketProtocol.validateCloseCode(code);
/*     */       }
/*  97 */       Buffer buffer = new Buffer();
/*  98 */       buffer.writeShort(code);
/*  99 */       if (reason != null) {
/* 100 */         buffer.write(reason);
/*     */       }
/* 102 */       payload = buffer.readByteString();
/*     */     } 
/*     */     
/*     */     try {
/* 106 */       writeControlFrame(8, payload);
/*     */     } finally {
/* 108 */       this.writerClosed = true;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void writeControlFrame(int opcode, ByteString payload) throws IOException {
/* 113 */     if (this.writerClosed) throw new IOException("closed");
/*     */     
/* 115 */     int length = payload.size();
/* 116 */     if (length > 125L) {
/* 117 */       throw new IllegalArgumentException("Payload size must be less than or equal to 125");
/*     */     }
/*     */ 
/*     */     
/* 121 */     int b0 = 0x80 | opcode;
/* 122 */     this.sinkBuffer.writeByte(b0);
/*     */     
/* 124 */     int b1 = length;
/* 125 */     if (this.isClient) {
/* 126 */       b1 |= 0x80;
/* 127 */       this.sinkBuffer.writeByte(b1);
/*     */       
/* 129 */       this.random.nextBytes(this.maskKey);
/* 130 */       this.sinkBuffer.write(this.maskKey);
/*     */       
/* 132 */       if (length > 0) {
/* 133 */         long payloadStart = this.sinkBuffer.size();
/* 134 */         this.sinkBuffer.write(payload);
/*     */         
/* 136 */         this.sinkBuffer.readAndWriteUnsafe(this.maskCursor);
/* 137 */         this.maskCursor.seek(payloadStart);
/* 138 */         WebSocketProtocol.toggleMask(this.maskCursor, this.maskKey);
/* 139 */         this.maskCursor.close();
/*     */       } 
/*     */     } else {
/* 142 */       this.sinkBuffer.writeByte(b1);
/* 143 */       this.sinkBuffer.write(payload);
/*     */     } 
/*     */     
/* 146 */     this.sink.flush();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   Sink newMessageSink(int formatOpcode, long contentLength) {
/* 154 */     if (this.activeWriter) {
/* 155 */       throw new IllegalStateException("Another message writer is active. Did you call close()?");
/*     */     }
/* 157 */     this.activeWriter = true;
/*     */ 
/*     */     
/* 160 */     this.frameSink.formatOpcode = formatOpcode;
/* 161 */     this.frameSink.contentLength = contentLength;
/* 162 */     this.frameSink.isFirstFrame = true;
/* 163 */     this.frameSink.closed = false;
/*     */     
/* 165 */     return this.frameSink;
/*     */   }
/*     */ 
/*     */   
/*     */   void writeMessageFrame(int formatOpcode, long byteCount, boolean isFirstFrame, boolean isFinal) throws IOException {
/* 170 */     if (this.writerClosed) throw new IOException("closed");
/*     */     
/* 172 */     int b0 = isFirstFrame ? formatOpcode : 0;
/* 173 */     if (isFinal) {
/* 174 */       b0 |= 0x80;
/*     */     }
/* 176 */     this.sinkBuffer.writeByte(b0);
/*     */     
/* 178 */     int b1 = 0;
/* 179 */     if (this.isClient) {
/* 180 */       b1 |= 0x80;
/*     */     }
/* 182 */     if (byteCount <= 125L) {
/* 183 */       b1 |= (int)byteCount;
/* 184 */       this.sinkBuffer.writeByte(b1);
/* 185 */     } else if (byteCount <= 65535L) {
/* 186 */       b1 |= 0x7E;
/* 187 */       this.sinkBuffer.writeByte(b1);
/* 188 */       this.sinkBuffer.writeShort((int)byteCount);
/*     */     } else {
/* 190 */       b1 |= 0x7F;
/* 191 */       this.sinkBuffer.writeByte(b1);
/* 192 */       this.sinkBuffer.writeLong(byteCount);
/*     */     } 
/*     */     
/* 195 */     if (this.isClient) {
/* 196 */       this.random.nextBytes(this.maskKey);
/* 197 */       this.sinkBuffer.write(this.maskKey);
/*     */       
/* 199 */       if (byteCount > 0L) {
/* 200 */         long bufferStart = this.sinkBuffer.size();
/* 201 */         this.sinkBuffer.write(this.buffer, byteCount);
/*     */         
/* 203 */         this.sinkBuffer.readAndWriteUnsafe(this.maskCursor);
/* 204 */         this.maskCursor.seek(bufferStart);
/* 205 */         WebSocketProtocol.toggleMask(this.maskCursor, this.maskKey);
/* 206 */         this.maskCursor.close();
/*     */       } 
/*     */     } else {
/* 209 */       this.sinkBuffer.write(this.buffer, byteCount);
/*     */     } 
/*     */     
/* 212 */     this.sink.emit();
/*     */   }
/*     */   
/*     */   final class FrameSink implements Sink {
/*     */     int formatOpcode;
/*     */     long contentLength;
/*     */     boolean isFirstFrame;
/*     */     boolean closed;
/*     */     
/*     */     public void write(Buffer source, long byteCount) throws IOException {
/* 222 */       if (this.closed) throw new IOException("closed");
/*     */       
/* 224 */       WebSocketWriter.this.buffer.write(source, byteCount);
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 229 */       boolean deferWrite = (this.isFirstFrame && this.contentLength != -1L && WebSocketWriter.this.buffer.size() > this.contentLength - 8192L);
/*     */       
/* 231 */       long emitCount = WebSocketWriter.this.buffer.completeSegmentByteCount();
/* 232 */       if (emitCount > 0L && !deferWrite) {
/* 233 */         WebSocketWriter.this.writeMessageFrame(this.formatOpcode, emitCount, this.isFirstFrame, false);
/* 234 */         this.isFirstFrame = false;
/*     */       } 
/*     */     }
/*     */     
/*     */     public void flush() throws IOException {
/* 239 */       if (this.closed) throw new IOException("closed");
/*     */       
/* 241 */       WebSocketWriter.this.writeMessageFrame(this.formatOpcode, WebSocketWriter.this.buffer.size(), this.isFirstFrame, false);
/* 242 */       this.isFirstFrame = false;
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 246 */       return WebSocketWriter.this.sink.timeout();
/*     */     }
/*     */ 
/*     */     
/*     */     public void close() throws IOException {
/* 251 */       if (this.closed) throw new IOException("closed");
/*     */       
/* 253 */       WebSocketWriter.this.writeMessageFrame(this.formatOpcode, WebSocketWriter.this.buffer.size(), this.isFirstFrame, true);
/* 254 */       this.closed = true;
/* 255 */       WebSocketWriter.this.activeWriter = false;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\ws\WebSocketWriter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */