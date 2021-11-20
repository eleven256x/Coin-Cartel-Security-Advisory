/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSource;
/*     */ import okio.ByteString;
/*     */ import okio.Source;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class Http2Reader
/*     */   implements Closeable
/*     */ {
/*  60 */   static final Logger logger = Logger.getLogger(Http2.class.getName());
/*     */   
/*     */   private final BufferedSource source;
/*     */   
/*     */   private final ContinuationSource continuation;
/*     */   
/*     */   private final boolean client;
/*     */   
/*     */   final Hpack.Reader hpackReader;
/*     */   
/*     */   Http2Reader(BufferedSource source, boolean client) {
/*  71 */     this.source = source;
/*  72 */     this.client = client;
/*  73 */     this.continuation = new ContinuationSource(this.source);
/*  74 */     this.hpackReader = new Hpack.Reader(4096, this.continuation);
/*     */   }
/*     */   
/*     */   public void readConnectionPreface(Handler handler) throws IOException {
/*  78 */     if (this.client) {
/*     */       
/*  80 */       if (!nextFrame(true, handler)) {
/*  81 */         throw Http2.ioException("Required SETTINGS preface not received", new Object[0]);
/*     */       }
/*     */     } else {
/*     */       
/*  85 */       ByteString connectionPreface = this.source.readByteString(Http2.CONNECTION_PREFACE.size());
/*  86 */       if (logger.isLoggable(Level.FINE)) logger.fine(Util.format("<< CONNECTION %s", new Object[] { connectionPreface.hex() })); 
/*  87 */       if (!Http2.CONNECTION_PREFACE.equals(connectionPreface)) {
/*  88 */         throw Http2.ioException("Expected a connection header but was %s", new Object[] { connectionPreface.utf8() });
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public boolean nextFrame(boolean requireSettings, Handler handler) throws IOException {
/*     */     try {
/*  95 */       this.source.require(9L);
/*  96 */     } catch (IOException e) {
/*  97 */       return false;
/*     */     } 
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
/* 111 */     int length = readMedium(this.source);
/* 112 */     if (length < 0 || length > 16384) {
/* 113 */       throw Http2.ioException("FRAME_SIZE_ERROR: %s", new Object[] { Integer.valueOf(length) });
/*     */     }
/* 115 */     byte type = (byte)(this.source.readByte() & 0xFF);
/* 116 */     if (requireSettings && type != 4) {
/* 117 */       throw Http2.ioException("Expected a SETTINGS frame but was %s", new Object[] { Byte.valueOf(type) });
/*     */     }
/* 119 */     byte flags = (byte)(this.source.readByte() & 0xFF);
/* 120 */     int streamId = this.source.readInt() & Integer.MAX_VALUE;
/* 121 */     if (logger.isLoggable(Level.FINE)) logger.fine(Http2.frameLog(true, streamId, length, type, flags));
/*     */     
/* 123 */     switch (type)
/*     */     { case 0:
/* 125 */         readData(handler, length, flags, streamId);
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
/* 164 */         return true;case 1: readHeaders(handler, length, flags, streamId); return true;case 2: readPriority(handler, length, flags, streamId); return true;case 3: readRstStream(handler, length, flags, streamId); return true;case 4: readSettings(handler, length, flags, streamId); return true;case 5: readPushPromise(handler, length, flags, streamId); return true;case 6: readPing(handler, length, flags, streamId); return true;case 7: readGoAway(handler, length, flags, streamId); return true;case 8: readWindowUpdate(handler, length, flags, streamId); return true; }  this.source.skip(length); return true;
/*     */   }
/*     */ 
/*     */   
/*     */   private void readHeaders(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 169 */     if (streamId == 0) throw Http2.ioException("PROTOCOL_ERROR: TYPE_HEADERS streamId == 0", new Object[0]);
/*     */     
/* 171 */     boolean endStream = ((flags & 0x1) != 0);
/*     */     
/* 173 */     short padding = ((flags & 0x8) != 0) ? (short)(this.source.readByte() & 0xFF) : 0;
/*     */     
/* 175 */     if ((flags & 0x20) != 0) {
/* 176 */       readPriority(handler, streamId);
/* 177 */       length -= 5;
/*     */     } 
/*     */     
/* 180 */     length = lengthWithoutPadding(length, flags, padding);
/*     */     
/* 182 */     List<Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);
/*     */     
/* 184 */     handler.headers(endStream, streamId, -1, headerBlock);
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Header> readHeaderBlock(int length, short padding, byte flags, int streamId) throws IOException {
/* 189 */     this.continuation.length = this.continuation.left = length;
/* 190 */     this.continuation.padding = padding;
/* 191 */     this.continuation.flags = flags;
/* 192 */     this.continuation.streamId = streamId;
/*     */ 
/*     */ 
/*     */     
/* 196 */     this.hpackReader.readHeaders();
/* 197 */     return this.hpackReader.getAndResetHeaderList();
/*     */   }
/*     */ 
/*     */   
/*     */   private void readData(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 202 */     if (streamId == 0) throw Http2.ioException("PROTOCOL_ERROR: TYPE_DATA streamId == 0", new Object[0]);
/*     */ 
/*     */     
/* 205 */     boolean inFinished = ((flags & 0x1) != 0);
/* 206 */     boolean gzipped = ((flags & 0x20) != 0);
/* 207 */     if (gzipped) {
/* 208 */       throw Http2.ioException("PROTOCOL_ERROR: FLAG_COMPRESSED without SETTINGS_COMPRESS_DATA", new Object[0]);
/*     */     }
/*     */     
/* 211 */     short padding = ((flags & 0x8) != 0) ? (short)(this.source.readByte() & 0xFF) : 0;
/* 212 */     length = lengthWithoutPadding(length, flags, padding);
/*     */     
/* 214 */     handler.data(inFinished, streamId, this.source, length);
/* 215 */     this.source.skip(padding);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readPriority(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 220 */     if (length != 5) throw Http2.ioException("TYPE_PRIORITY length: %d != 5", new Object[] { Integer.valueOf(length) }); 
/* 221 */     if (streamId == 0) throw Http2.ioException("TYPE_PRIORITY streamId == 0", new Object[0]); 
/* 222 */     readPriority(handler, streamId);
/*     */   }
/*     */   
/*     */   private void readPriority(Handler handler, int streamId) throws IOException {
/* 226 */     int w1 = this.source.readInt();
/* 227 */     boolean exclusive = ((w1 & Integer.MIN_VALUE) != 0);
/* 228 */     int streamDependency = w1 & Integer.MAX_VALUE;
/* 229 */     int weight = (this.source.readByte() & 0xFF) + 1;
/* 230 */     handler.priority(streamId, streamDependency, weight, exclusive);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readRstStream(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 235 */     if (length != 4) throw Http2.ioException("TYPE_RST_STREAM length: %d != 4", new Object[] { Integer.valueOf(length) }); 
/* 236 */     if (streamId == 0) throw Http2.ioException("TYPE_RST_STREAM streamId == 0", new Object[0]); 
/* 237 */     int errorCodeInt = this.source.readInt();
/* 238 */     ErrorCode errorCode = ErrorCode.fromHttp2(errorCodeInt);
/* 239 */     if (errorCode == null) {
/* 240 */       throw Http2.ioException("TYPE_RST_STREAM unexpected error code: %d", new Object[] { Integer.valueOf(errorCodeInt) });
/*     */     }
/* 242 */     handler.rstStream(streamId, errorCode);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readSettings(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 247 */     if (streamId != 0) throw Http2.ioException("TYPE_SETTINGS streamId != 0", new Object[0]); 
/* 248 */     if ((flags & 0x1) != 0) {
/* 249 */       if (length != 0) throw Http2.ioException("FRAME_SIZE_ERROR ack frame should be empty!", new Object[0]); 
/* 250 */       handler.ackSettings();
/*     */       
/*     */       return;
/*     */     } 
/* 254 */     if (length % 6 != 0) throw Http2.ioException("TYPE_SETTINGS length %% 6 != 0: %s", new Object[] { Integer.valueOf(length) }); 
/* 255 */     Settings settings = new Settings();
/* 256 */     for (int i = 0; i < length; i += 6) {
/* 257 */       int id = this.source.readShort() & 0xFFFF;
/* 258 */       int value = this.source.readInt();
/*     */       
/* 260 */       switch (id) {
/*     */ 
/*     */         
/*     */         case 2:
/* 264 */           if (value != 0 && value != 1) {
/* 265 */             throw Http2.ioException("PROTOCOL_ERROR SETTINGS_ENABLE_PUSH != 0 or 1", new Object[0]);
/*     */           }
/*     */           break;
/*     */         case 3:
/* 269 */           id = 4;
/*     */           break;
/*     */         case 4:
/* 272 */           id = 7;
/* 273 */           if (value < 0) {
/* 274 */             throw Http2.ioException("PROTOCOL_ERROR SETTINGS_INITIAL_WINDOW_SIZE > 2^31 - 1", new Object[0]);
/*     */           }
/*     */           break;
/*     */         case 5:
/* 278 */           if (value < 16384 || value > 16777215) {
/* 279 */             throw Http2.ioException("PROTOCOL_ERROR SETTINGS_MAX_FRAME_SIZE: %s", new Object[] { Integer.valueOf(value) });
/*     */           }
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 287 */       settings.set(id, value);
/*     */     } 
/* 289 */     handler.settings(false, settings);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readPushPromise(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 294 */     if (streamId == 0) {
/* 295 */       throw Http2.ioException("PROTOCOL_ERROR: TYPE_PUSH_PROMISE streamId == 0", new Object[0]);
/*     */     }
/* 297 */     short padding = ((flags & 0x8) != 0) ? (short)(this.source.readByte() & 0xFF) : 0;
/* 298 */     int promisedStreamId = this.source.readInt() & Integer.MAX_VALUE;
/* 299 */     length -= 4;
/* 300 */     length = lengthWithoutPadding(length, flags, padding);
/* 301 */     List<Header> headerBlock = readHeaderBlock(length, padding, flags, streamId);
/* 302 */     handler.pushPromise(streamId, promisedStreamId, headerBlock);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readPing(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 307 */     if (length != 8) throw Http2.ioException("TYPE_PING length != 8: %s", new Object[] { Integer.valueOf(length) }); 
/* 308 */     if (streamId != 0) throw Http2.ioException("TYPE_PING streamId != 0", new Object[0]); 
/* 309 */     int payload1 = this.source.readInt();
/* 310 */     int payload2 = this.source.readInt();
/* 311 */     boolean ack = ((flags & 0x1) != 0);
/* 312 */     handler.ping(ack, payload1, payload2);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readGoAway(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 317 */     if (length < 8) throw Http2.ioException("TYPE_GOAWAY length < 8: %s", new Object[] { Integer.valueOf(length) }); 
/* 318 */     if (streamId != 0) throw Http2.ioException("TYPE_GOAWAY streamId != 0", new Object[0]); 
/* 319 */     int lastStreamId = this.source.readInt();
/* 320 */     int errorCodeInt = this.source.readInt();
/* 321 */     int opaqueDataLength = length - 8;
/* 322 */     ErrorCode errorCode = ErrorCode.fromHttp2(errorCodeInt);
/* 323 */     if (errorCode == null) {
/* 324 */       throw Http2.ioException("TYPE_GOAWAY unexpected error code: %d", new Object[] { Integer.valueOf(errorCodeInt) });
/*     */     }
/* 326 */     ByteString debugData = ByteString.EMPTY;
/* 327 */     if (opaqueDataLength > 0) {
/* 328 */       debugData = this.source.readByteString(opaqueDataLength);
/*     */     }
/* 330 */     handler.goAway(lastStreamId, errorCode, debugData);
/*     */   }
/*     */ 
/*     */   
/*     */   private void readWindowUpdate(Handler handler, int length, byte flags, int streamId) throws IOException {
/* 335 */     if (length != 4) throw Http2.ioException("TYPE_WINDOW_UPDATE length !=4: %s", new Object[] { Integer.valueOf(length) }); 
/* 336 */     long increment = this.source.readInt() & 0x7FFFFFFFL;
/* 337 */     if (increment == 0L) throw Http2.ioException("windowSizeIncrement was 0", new Object[] { Long.valueOf(increment) }); 
/* 338 */     handler.windowUpdate(streamId, increment);
/*     */   }
/*     */   
/*     */   public void close() throws IOException {
/* 342 */     this.source.close();
/*     */   }
/*     */   static interface Handler {
/*     */     void data(boolean param1Boolean, int param1Int1, BufferedSource param1BufferedSource, int param1Int2) throws IOException;
/*     */     void headers(boolean param1Boolean, int param1Int1, int param1Int2, List<Header> param1List);
/*     */     void rstStream(int param1Int, ErrorCode param1ErrorCode);
/*     */     void settings(boolean param1Boolean, Settings param1Settings);
/*     */     void ackSettings();
/*     */     void ping(boolean param1Boolean, int param1Int1, int param1Int2);
/*     */     void goAway(int param1Int, ErrorCode param1ErrorCode, ByteString param1ByteString);
/*     */     void windowUpdate(int param1Int, long param1Long);
/*     */     void priority(int param1Int1, int param1Int2, int param1Int3, boolean param1Boolean);
/*     */     void pushPromise(int param1Int1, int param1Int2, List<Header> param1List) throws IOException;
/*     */     void alternateService(int param1Int1, String param1String1, ByteString param1ByteString, String param1String2, int param1Int2, long param1Long); }
/*     */   static final class ContinuationSource implements Source { private final BufferedSource source; int length;
/*     */     byte flags;
/*     */     
/*     */     ContinuationSource(BufferedSource source) {
/* 360 */       this.source = source;
/*     */     }
/*     */     int streamId; int left; short padding;
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/* 364 */       while (this.left == 0) {
/* 365 */         this.source.skip(this.padding);
/* 366 */         this.padding = 0;
/* 367 */         if ((this.flags & 0x4) != 0) return -1L; 
/* 368 */         readContinuationHeader();
/*     */       } 
/*     */ 
/*     */       
/* 372 */       long read = this.source.read(sink, Math.min(byteCount, this.left));
/* 373 */       if (read == -1L) return -1L; 
/* 374 */       this.left = (int)(this.left - read);
/* 375 */       return read;
/*     */     }
/*     */     
/*     */     public Timeout timeout() {
/* 379 */       return this.source.timeout();
/*     */     }
/*     */ 
/*     */     
/*     */     public void close() throws IOException {}
/*     */     
/*     */     private void readContinuationHeader() throws IOException {
/* 386 */       int previousStreamId = this.streamId;
/*     */       
/* 388 */       this.length = this.left = Http2Reader.readMedium(this.source);
/* 389 */       byte type = (byte)(this.source.readByte() & 0xFF);
/* 390 */       this.flags = (byte)(this.source.readByte() & 0xFF);
/* 391 */       if (Http2Reader.logger.isLoggable(Level.FINE)) Http2Reader.logger.fine(Http2.frameLog(true, this.streamId, this.length, type, this.flags)); 
/* 392 */       this.streamId = this.source.readInt() & Integer.MAX_VALUE;
/* 393 */       if (type != 9) throw Http2.ioException("%s != TYPE_CONTINUATION", new Object[] { Byte.valueOf(type) }); 
/* 394 */       if (this.streamId != previousStreamId) throw Http2.ioException("TYPE_CONTINUATION streamId changed", new Object[0]); 
/*     */     } }
/*     */ 
/*     */   
/*     */   static int readMedium(BufferedSource source) throws IOException {
/* 399 */     return (source.readByte() & 0xFF) << 16 | (source
/* 400 */       .readByte() & 0xFF) << 8 | source
/* 401 */       .readByte() & 0xFF;
/*     */   }
/*     */ 
/*     */   
/*     */   static int lengthWithoutPadding(int length, byte flags, short padding) throws IOException {
/* 406 */     if ((flags & 0x8) != 0) length--; 
/* 407 */     if (padding > length) {
/* 408 */       throw Http2.ioException("PROTOCOL_ERROR padding %s > remaining length %s", new Object[] { Short.valueOf(padding), Integer.valueOf(length) });
/*     */     }
/* 410 */     return (short)(length - padding);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Http2Reader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */