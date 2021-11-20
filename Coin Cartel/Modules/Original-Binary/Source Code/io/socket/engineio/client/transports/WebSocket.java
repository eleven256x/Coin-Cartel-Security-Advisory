/*     */ package io.socket.engineio.client.transports;
/*     */ 
/*     */ import io.socket.engineio.client.Transport;
/*     */ import io.socket.engineio.parser.Packet;
/*     */ import io.socket.engineio.parser.Parser;
/*     */ import io.socket.parseqs.ParseQS;
/*     */ import io.socket.thread.EventThread;
/*     */ import io.socket.yeast.Yeast;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TreeMap;
/*     */ import java.util.logging.Logger;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.WebSocketListener;
/*     */ import okio.ByteString;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class WebSocket
/*     */   extends Transport
/*     */ {
/*     */   public static final String NAME = "websocket";
/*  27 */   private static final Logger logger = Logger.getLogger(PollingXHR.class.getName());
/*     */   
/*     */   private okhttp3.WebSocket ws;
/*     */   
/*     */   public WebSocket(Transport.Options opts) {
/*  32 */     super(opts);
/*  33 */     this.name = "websocket";
/*     */   }
/*     */   
/*     */   protected void doOpen() {
/*  37 */     Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
/*  38 */     if (this.extraHeaders != null) {
/*  39 */       headers.putAll(this.extraHeaders);
/*     */     }
/*  41 */     emit("requestHeaders", new Object[] { headers });
/*     */     
/*  43 */     final WebSocket self = this;
/*  44 */     okhttp3.WebSocket.Factory factory = (this.webSocketFactory != null) ? this.webSocketFactory : (okhttp3.WebSocket.Factory)new OkHttpClient();
/*  45 */     Request.Builder builder = (new Request.Builder()).url(uri());
/*  46 */     for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
/*  47 */       for (String v : entry.getValue()) {
/*  48 */         builder.addHeader(entry.getKey(), v);
/*     */       }
/*     */     } 
/*  51 */     Request request = builder.build();
/*  52 */     this.ws = factory.newWebSocket(request, new WebSocketListener()
/*     */         {
/*     */           public void onOpen(okhttp3.WebSocket webSocket, Response response) {
/*  55 */             final Map<String, List<String>> headers = response.headers().toMultimap();
/*  56 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  59 */                     self.emit("responseHeaders", new Object[] { this.val$headers });
/*  60 */                     self.onOpen();
/*     */                   }
/*     */                 });
/*     */           }
/*     */ 
/*     */           
/*     */           public void onMessage(okhttp3.WebSocket webSocket, final String text) {
/*  67 */             if (text == null) {
/*     */               return;
/*     */             }
/*  70 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  73 */                     self.onData(text);
/*     */                   }
/*     */                 });
/*     */           }
/*     */ 
/*     */           
/*     */           public void onMessage(okhttp3.WebSocket webSocket, final ByteString bytes) {
/*  80 */             if (bytes == null) {
/*     */               return;
/*     */             }
/*  83 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  86 */                     self.onData(bytes.toByteArray());
/*     */                   }
/*     */                 });
/*     */           }
/*     */ 
/*     */           
/*     */           public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
/*  93 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  96 */                     self.onClose();
/*     */                   }
/*     */                 });
/*     */           }
/*     */ 
/*     */           
/*     */           public void onFailure(okhttp3.WebSocket webSocket, final Throwable t, Response response) {
/* 103 */             if (!(t instanceof Exception)) {
/*     */               return;
/*     */             }
/* 106 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/* 109 */                     self.onError("websocket error", (Exception)t);
/*     */                   }
/*     */                 });
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   protected void write(Packet[] packets) {
/* 117 */     final WebSocket self = this;
/* 118 */     this.writable = false;
/*     */     
/* 120 */     final Runnable done = new Runnable()
/*     */       {
/*     */         
/*     */         public void run()
/*     */         {
/* 125 */           EventThread.nextTick(new Runnable()
/*     */               {
/*     */                 public void run() {
/* 128 */                   self.writable = true;
/* 129 */                   self.emit("drain", new Object[0]);
/*     */                 }
/*     */               });
/*     */         }
/*     */       };
/*     */     
/* 135 */     final int[] total = { packets.length };
/* 136 */     for (Packet packet : packets) {
/* 137 */       if (this.readyState != Transport.ReadyState.OPENING && this.readyState != Transport.ReadyState.OPEN) {
/*     */         break;
/*     */       }
/*     */ 
/*     */       
/* 142 */       Parser.encodePacket(packet, new Parser.EncodeCallback()
/*     */           {
/*     */             public void call(Object packet) {
/*     */               try {
/* 146 */                 if (packet instanceof String) {
/* 147 */                   self.ws.send((String)packet);
/* 148 */                 } else if (packet instanceof byte[]) {
/* 149 */                   self.ws.send(ByteString.of((byte[])packet));
/*     */                 } 
/* 151 */               } catch (IllegalStateException e) {
/* 152 */                 WebSocket.logger.fine("websocket closed before we could write");
/*     */               } 
/*     */               
/* 155 */               total[0] = total[0] - 1; if (0 == total[0] - 1) done.run(); 
/*     */             }
/*     */           });
/*     */     } 
/*     */   }
/*     */   
/*     */   protected void doClose() {
/* 162 */     if (this.ws != null) {
/* 163 */       this.ws.close(1000, "");
/* 164 */       this.ws = null;
/*     */     } 
/*     */   }
/*     */   
/*     */   protected String uri() {
/* 169 */     Map<String, String> query = this.query;
/* 170 */     if (query == null) {
/* 171 */       query = new HashMap<>();
/*     */     }
/* 173 */     String schema = this.secure ? "wss" : "ws";
/* 174 */     String port = "";
/*     */     
/* 176 */     if (this.port > 0 && (("wss".equals(schema) && this.port != 443) || ("ws"
/* 177 */       .equals(schema) && this.port != 80))) {
/* 178 */       port = ":" + this.port;
/*     */     }
/*     */     
/* 181 */     if (this.timestampRequests) {
/* 182 */       query.put(this.timestampParam, Yeast.yeast());
/*     */     }
/*     */     
/* 185 */     String derivedQuery = ParseQS.encode(query);
/* 186 */     if (derivedQuery.length() > 0) {
/* 187 */       derivedQuery = "?" + derivedQuery;
/*     */     }
/*     */     
/* 190 */     boolean ipv6 = this.hostname.contains(":");
/* 191 */     return schema + "://" + (ipv6 ? ("[" + this.hostname + "]") : this.hostname) + port + this.path + derivedQuery;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\transports\WebSocket.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */