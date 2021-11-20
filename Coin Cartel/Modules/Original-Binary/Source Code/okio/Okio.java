/*     */ package okio;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.io.OutputStream;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.OpenOption;
/*     */ import java.nio.file.Path;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.annotation.Nullable;
/*     */ import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class Okio
/*     */ {
/*  40 */   static final Logger logger = Logger.getLogger(Okio.class.getName());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static BufferedSource buffer(Source source) {
/*  51 */     return new RealBufferedSource(source);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static BufferedSink buffer(Sink sink) {
/*  60 */     return new RealBufferedSink(sink);
/*     */   }
/*     */ 
/*     */   
/*     */   public static Sink sink(OutputStream out) {
/*  65 */     return sink(out, new Timeout());
/*     */   }
/*     */   
/*     */   private static Sink sink(final OutputStream out, final Timeout timeout) {
/*  69 */     if (out == null) throw new IllegalArgumentException("out == null"); 
/*  70 */     if (timeout == null) throw new IllegalArgumentException("timeout == null");
/*     */     
/*  72 */     return new Sink() {
/*     */         public void write(Buffer source, long byteCount) throws IOException {
/*  74 */           Util.checkOffsetAndCount(source.size, 0L, byteCount);
/*  75 */           while (byteCount > 0L) {
/*  76 */             timeout.throwIfReached();
/*  77 */             Segment head = source.head;
/*  78 */             int toCopy = (int)Math.min(byteCount, (head.limit - head.pos));
/*  79 */             out.write(head.data, head.pos, toCopy);
/*     */             
/*  81 */             head.pos += toCopy;
/*  82 */             byteCount -= toCopy;
/*  83 */             source.size -= toCopy;
/*     */             
/*  85 */             if (head.pos == head.limit) {
/*  86 */               source.head = head.pop();
/*  87 */               SegmentPool.recycle(head);
/*     */             } 
/*     */           } 
/*     */         }
/*     */         
/*     */         public void flush() throws IOException {
/*  93 */           out.flush();
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/*  97 */           out.close();
/*     */         }
/*     */         
/*     */         public Timeout timeout() {
/* 101 */           return timeout;
/*     */         }
/*     */         
/*     */         public String toString() {
/* 105 */           return "sink(" + out + ")";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Sink sink(Socket socket) throws IOException {
/* 116 */     if (socket == null) throw new IllegalArgumentException("socket == null"); 
/* 117 */     if (socket.getOutputStream() == null) throw new IOException("socket's output stream == null"); 
/* 118 */     AsyncTimeout timeout = timeout(socket);
/* 119 */     Sink sink = sink(socket.getOutputStream(), timeout);
/* 120 */     return timeout.sink(sink);
/*     */   }
/*     */ 
/*     */   
/*     */   public static Source source(InputStream in) {
/* 125 */     return source(in, new Timeout());
/*     */   }
/*     */   
/*     */   private static Source source(final InputStream in, final Timeout timeout) {
/* 129 */     if (in == null) throw new IllegalArgumentException("in == null"); 
/* 130 */     if (timeout == null) throw new IllegalArgumentException("timeout == null");
/*     */     
/* 132 */     return new Source() {
/*     */         public long read(Buffer sink, long byteCount) throws IOException {
/* 134 */           if (byteCount < 0L) throw new IllegalArgumentException("byteCount < 0: " + byteCount); 
/* 135 */           if (byteCount == 0L) return 0L; 
/*     */           try {
/* 137 */             timeout.throwIfReached();
/* 138 */             Segment tail = sink.writableSegment(1);
/* 139 */             int maxToCopy = (int)Math.min(byteCount, (8192 - tail.limit));
/* 140 */             int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
/* 141 */             if (bytesRead == -1) return -1L; 
/* 142 */             tail.limit += bytesRead;
/* 143 */             sink.size += bytesRead;
/* 144 */             return bytesRead;
/* 145 */           } catch (AssertionError e) {
/* 146 */             if (Okio.isAndroidGetsocknameError(e)) throw new IOException(e); 
/* 147 */             throw e;
/*     */           } 
/*     */         }
/*     */         
/*     */         public void close() throws IOException {
/* 152 */           in.close();
/*     */         }
/*     */         
/*     */         public Timeout timeout() {
/* 156 */           return timeout;
/*     */         }
/*     */         
/*     */         public String toString() {
/* 160 */           return "source(" + in + ")";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */   
/*     */   public static Source source(File file) throws FileNotFoundException {
/* 167 */     if (file == null) throw new IllegalArgumentException("file == null"); 
/* 168 */     return source(new FileInputStream(file));
/*     */   }
/*     */ 
/*     */   
/*     */   @IgnoreJRERequirement
/*     */   public static Source source(Path path, OpenOption... options) throws IOException {
/* 174 */     if (path == null) throw new IllegalArgumentException("path == null"); 
/* 175 */     return source(Files.newInputStream(path, options));
/*     */   }
/*     */ 
/*     */   
/*     */   public static Sink sink(File file) throws FileNotFoundException {
/* 180 */     if (file == null) throw new IllegalArgumentException("file == null"); 
/* 181 */     return sink(new FileOutputStream(file));
/*     */   }
/*     */ 
/*     */   
/*     */   public static Sink appendingSink(File file) throws FileNotFoundException {
/* 186 */     if (file == null) throw new IllegalArgumentException("file == null"); 
/* 187 */     return sink(new FileOutputStream(file, true));
/*     */   }
/*     */ 
/*     */   
/*     */   @IgnoreJRERequirement
/*     */   public static Sink sink(Path path, OpenOption... options) throws IOException {
/* 193 */     if (path == null) throw new IllegalArgumentException("path == null"); 
/* 194 */     return sink(Files.newOutputStream(path, options));
/*     */   }
/*     */ 
/*     */   
/*     */   public static Sink blackhole() {
/* 199 */     return new Sink() {
/*     */         public void write(Buffer source, long byteCount) throws IOException {
/* 201 */           source.skip(byteCount);
/*     */         }
/*     */ 
/*     */         
/*     */         public void flush() throws IOException {}
/*     */         
/*     */         public Timeout timeout() {
/* 208 */           return Timeout.NONE;
/*     */         }
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         public void close() throws IOException {}
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Source source(Socket socket) throws IOException {
/* 222 */     if (socket == null) throw new IllegalArgumentException("socket == null"); 
/* 223 */     if (socket.getInputStream() == null) throw new IOException("socket's input stream == null"); 
/* 224 */     AsyncTimeout timeout = timeout(socket);
/* 225 */     Source source = source(socket.getInputStream(), timeout);
/* 226 */     return timeout.source(source);
/*     */   }
/*     */   
/*     */   private static AsyncTimeout timeout(final Socket socket) {
/* 230 */     return new AsyncTimeout() {
/*     */         protected IOException newTimeoutException(@Nullable IOException cause) {
/* 232 */           InterruptedIOException ioe = new SocketTimeoutException("timeout");
/* 233 */           if (cause != null) {
/* 234 */             ioe.initCause(cause);
/*     */           }
/* 236 */           return ioe;
/*     */         }
/*     */         
/*     */         protected void timedOut() {
/*     */           try {
/* 241 */             socket.close();
/* 242 */           } catch (Exception e) {
/* 243 */             Okio.logger.log(Level.WARNING, "Failed to close timed out socket " + socket, e);
/* 244 */           } catch (AssertionError e) {
/* 245 */             if (Okio.isAndroidGetsocknameError(e)) {
/*     */ 
/*     */               
/* 248 */               Okio.logger.log(Level.WARNING, "Failed to close timed out socket " + socket, e);
/*     */             } else {
/* 250 */               throw e;
/*     */             } 
/*     */           } 
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static boolean isAndroidGetsocknameError(AssertionError e) {
/* 262 */     return (e.getCause() != null && e.getMessage() != null && e
/* 263 */       .getMessage().contains("getsockname failed"));
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Okio.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */