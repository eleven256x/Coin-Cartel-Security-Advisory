/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
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
/*     */ final class Http2Writer
/*     */   implements Closeable
/*     */ {
/*  47 */   private static final Logger logger = Logger.getLogger(Http2.class.getName());
/*     */   
/*     */   private final BufferedSink sink;
/*     */   
/*     */   private final boolean client;
/*     */   private final Buffer hpackBuffer;
/*     */   private int maxFrameSize;
/*     */   private boolean closed;
/*     */   final Hpack.Writer hpackWriter;
/*     */   
/*     */   Http2Writer(BufferedSink sink, boolean client) {
/*  58 */     this.sink = sink;
/*  59 */     this.client = client;
/*  60 */     this.hpackBuffer = new Buffer();
/*  61 */     this.hpackWriter = new Hpack.Writer(this.hpackBuffer);
/*  62 */     this.maxFrameSize = 16384;
/*     */   }
/*     */   
/*     */   public synchronized void connectionPreface() throws IOException {
/*  66 */     if (this.closed) throw new IOException("closed"); 
/*  67 */     if (!this.client)
/*  68 */       return;  if (logger.isLoggable(Level.FINE)) {
/*  69 */       logger.fine(Util.format(">> CONNECTION %s", new Object[] { Http2.CONNECTION_PREFACE.hex() }));
/*     */     }
/*  71 */     this.sink.write(Http2.CONNECTION_PREFACE.toByteArray());
/*  72 */     this.sink.flush();
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void applyAndAckSettings(Settings peerSettings) throws IOException {
/*  77 */     if (this.closed) throw new IOException("closed"); 
/*  78 */     this.maxFrameSize = peerSettings.getMaxFrameSize(this.maxFrameSize);
/*  79 */     if (peerSettings.getHeaderTableSize() != -1) {
/*  80 */       this.hpackWriter.setHeaderTableSizeSetting(peerSettings.getHeaderTableSize());
/*     */     }
/*  82 */     int length = 0;
/*  83 */     byte type = 4;
/*  84 */     byte flags = 1;
/*  85 */     int streamId = 0;
/*  86 */     frameHeader(streamId, length, type, flags);
/*  87 */     this.sink.flush();
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
/*     */   public synchronized void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders) throws IOException {
/* 105 */     if (this.closed) throw new IOException("closed"); 
/* 106 */     this.hpackWriter.writeHeaders(requestHeaders);
/*     */     
/* 108 */     long byteCount = this.hpackBuffer.size();
/* 109 */     int length = (int)Math.min((this.maxFrameSize - 4), byteCount);
/* 110 */     byte type = 5;
/* 111 */     byte flags = (byteCount == length) ? 4 : 0;
/* 112 */     frameHeader(streamId, length + 4, type, flags);
/* 113 */     this.sink.writeInt(promisedStreamId & Integer.MAX_VALUE);
/* 114 */     this.sink.write(this.hpackBuffer, length);
/*     */     
/* 116 */     if (byteCount > length) writeContinuationFrames(streamId, byteCount - length); 
/*     */   }
/*     */   
/*     */   public synchronized void flush() throws IOException {
/* 120 */     if (this.closed) throw new IOException("closed"); 
/* 121 */     this.sink.flush();
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void synStream(boolean outFinished, int streamId, int associatedStreamId, List<Header> headerBlock) throws IOException {
/* 126 */     if (this.closed) throw new IOException("closed"); 
/* 127 */     headers(outFinished, streamId, headerBlock);
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void synReply(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
/* 132 */     if (this.closed) throw new IOException("closed"); 
/* 133 */     headers(outFinished, streamId, headerBlock);
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void headers(int streamId, List<Header> headerBlock) throws IOException {
/* 138 */     if (this.closed) throw new IOException("closed"); 
/* 139 */     headers(false, streamId, headerBlock);
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void rstStream(int streamId, ErrorCode errorCode) throws IOException {
/* 144 */     if (this.closed) throw new IOException("closed"); 
/* 145 */     if (errorCode.httpCode == -1) throw new IllegalArgumentException();
/*     */     
/* 147 */     int length = 4;
/* 148 */     byte type = 3;
/* 149 */     byte flags = 0;
/* 150 */     frameHeader(streamId, length, type, flags);
/* 151 */     this.sink.writeInt(errorCode.httpCode);
/* 152 */     this.sink.flush();
/*     */   }
/*     */ 
/*     */   
/*     */   public int maxDataLength() {
/* 157 */     return this.maxFrameSize;
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
/*     */   public synchronized void data(boolean outFinished, int streamId, Buffer source, int byteCount) throws IOException {
/* 170 */     if (this.closed) throw new IOException("closed"); 
/* 171 */     byte flags = 0;
/* 172 */     if (outFinished) flags = (byte)(flags | 0x1); 
/* 173 */     dataFrame(streamId, flags, source, byteCount);
/*     */   }
/*     */   
/*     */   void dataFrame(int streamId, byte flags, Buffer buffer, int byteCount) throws IOException {
/* 177 */     byte type = 0;
/* 178 */     frameHeader(streamId, byteCount, type, flags);
/* 179 */     if (byteCount > 0) {
/* 180 */       this.sink.write(buffer, byteCount);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void settings(Settings settings) throws IOException {
/* 186 */     if (this.closed) throw new IOException("closed"); 
/* 187 */     int length = settings.size() * 6;
/* 188 */     byte type = 4;
/* 189 */     byte flags = 0;
/* 190 */     int streamId = 0;
/* 191 */     frameHeader(streamId, length, type, flags);
/* 192 */     for (int i = 0; i < 10; i++) {
/* 193 */       if (settings.isSet(i)) {
/* 194 */         int id = i;
/* 195 */         if (id == 4) {
/* 196 */           id = 3;
/* 197 */         } else if (id == 7) {
/* 198 */           id = 4;
/*     */         } 
/* 200 */         this.sink.writeShort(id);
/* 201 */         this.sink.writeInt(settings.get(i));
/*     */       } 
/* 203 */     }  this.sink.flush();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized void ping(boolean ack, int payload1, int payload2) throws IOException {
/* 211 */     if (this.closed) throw new IOException("closed"); 
/* 212 */     int length = 8;
/* 213 */     byte type = 6;
/* 214 */     byte flags = ack ? 1 : 0;
/* 215 */     int streamId = 0;
/* 216 */     frameHeader(streamId, length, type, flags);
/* 217 */     this.sink.writeInt(payload1);
/* 218 */     this.sink.writeInt(payload2);
/* 219 */     this.sink.flush();
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
/*     */   public synchronized void goAway(int lastGoodStreamId, ErrorCode errorCode, byte[] debugData) throws IOException {
/* 232 */     if (this.closed) throw new IOException("closed"); 
/* 233 */     if (errorCode.httpCode == -1) throw Http2.illegalArgument("errorCode.httpCode == -1", new Object[0]); 
/* 234 */     int length = 8 + debugData.length;
/* 235 */     byte type = 7;
/* 236 */     byte flags = 0;
/* 237 */     int streamId = 0;
/* 238 */     frameHeader(streamId, length, type, flags);
/* 239 */     this.sink.writeInt(lastGoodStreamId);
/* 240 */     this.sink.writeInt(errorCode.httpCode);
/* 241 */     if (debugData.length > 0) {
/* 242 */       this.sink.write(debugData);
/*     */     }
/* 244 */     this.sink.flush();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized void windowUpdate(int streamId, long windowSizeIncrement) throws IOException {
/* 252 */     if (this.closed) throw new IOException("closed"); 
/* 253 */     if (windowSizeIncrement == 0L || windowSizeIncrement > 2147483647L)
/* 254 */       throw Http2.illegalArgument("windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL: %s", new Object[] {
/* 255 */             Long.valueOf(windowSizeIncrement)
/*     */           }); 
/* 257 */     int length = 4;
/* 258 */     byte type = 8;
/* 259 */     byte flags = 0;
/* 260 */     frameHeader(streamId, length, type, flags);
/* 261 */     this.sink.writeInt((int)windowSizeIncrement);
/* 262 */     this.sink.flush();
/*     */   }
/*     */   
/*     */   public void frameHeader(int streamId, int length, byte type, byte flags) throws IOException {
/* 266 */     if (logger.isLoggable(Level.FINE)) logger.fine(Http2.frameLog(false, streamId, length, type, flags)); 
/* 267 */     if (length > this.maxFrameSize) {
/* 268 */       throw Http2.illegalArgument("FRAME_SIZE_ERROR length > %d: %d", new Object[] { Integer.valueOf(this.maxFrameSize), Integer.valueOf(length) });
/*     */     }
/* 270 */     if ((streamId & Integer.MIN_VALUE) != 0) throw Http2.illegalArgument("reserved bit set: %s", new Object[] { Integer.valueOf(streamId) }); 
/* 271 */     writeMedium(this.sink, length);
/* 272 */     this.sink.writeByte(type & 0xFF);
/* 273 */     this.sink.writeByte(flags & 0xFF);
/* 274 */     this.sink.writeInt(streamId & Integer.MAX_VALUE);
/*     */   }
/*     */   
/*     */   public synchronized void close() throws IOException {
/* 278 */     this.closed = true;
/* 279 */     this.sink.close();
/*     */   }
/*     */   
/*     */   private static void writeMedium(BufferedSink sink, int i) throws IOException {
/* 283 */     sink.writeByte(i >>> 16 & 0xFF);
/* 284 */     sink.writeByte(i >>> 8 & 0xFF);
/* 285 */     sink.writeByte(i & 0xFF);
/*     */   }
/*     */   
/*     */   private void writeContinuationFrames(int streamId, long byteCount) throws IOException {
/* 289 */     while (byteCount > 0L) {
/* 290 */       int length = (int)Math.min(this.maxFrameSize, byteCount);
/* 291 */       byteCount -= length;
/* 292 */       frameHeader(streamId, length, (byte)9, (byteCount == 0L) ? 4 : 0);
/* 293 */       this.sink.write(this.hpackBuffer, length);
/*     */     } 
/*     */   }
/*     */   
/*     */   void headers(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
/* 298 */     if (this.closed) throw new IOException("closed"); 
/* 299 */     this.hpackWriter.writeHeaders(headerBlock);
/*     */     
/* 301 */     long byteCount = this.hpackBuffer.size();
/* 302 */     int length = (int)Math.min(this.maxFrameSize, byteCount);
/* 303 */     byte type = 1;
/* 304 */     byte flags = (byteCount == length) ? 4 : 0;
/* 305 */     if (outFinished) flags = (byte)(flags | 0x1); 
/* 306 */     frameHeader(streamId, length, type, flags);
/* 307 */     this.sink.write(this.hpackBuffer, length);
/*     */     
/* 309 */     if (byteCount > length) writeContinuationFrames(streamId, byteCount - length); 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Http2Writer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */