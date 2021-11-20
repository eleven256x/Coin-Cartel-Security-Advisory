/*     */ package io.socket.engineio.client.transports;
/*     */ 
/*     */ import io.socket.emitter.Emitter;
/*     */ import io.socket.engineio.client.Transport;
/*     */ import io.socket.engineio.parser.Packet;
/*     */ import io.socket.engineio.parser.Parser;
/*     */ import io.socket.parseqs.ParseQS;
/*     */ import io.socket.thread.EventThread;
/*     */ import io.socket.yeast.Yeast;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ 
/*     */ public abstract class Polling
/*     */   extends Transport
/*     */ {
/*  19 */   private static final Logger logger = Logger.getLogger(Polling.class.getName());
/*     */   
/*     */   public static final String NAME = "polling";
/*     */   
/*     */   public static final String EVENT_POLL = "poll";
/*     */   
/*     */   public static final String EVENT_POLL_COMPLETE = "pollComplete";
/*     */   
/*     */   private boolean polling;
/*     */   
/*     */   public Polling(Transport.Options opts) {
/*  30 */     super(opts);
/*  31 */     this.name = "polling";
/*     */   }
/*     */   
/*     */   protected void doOpen() {
/*  35 */     poll();
/*     */   }
/*     */   
/*     */   public void pause(final Runnable onPause) {
/*  39 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/*  42 */             final Polling self = Polling.this;
/*     */             
/*  44 */             Polling.this.readyState = Transport.ReadyState.PAUSED;
/*     */             
/*  46 */             final Runnable pause = new Runnable()
/*     */               {
/*     */                 public void run() {
/*  49 */                   Polling.logger.fine("paused");
/*  50 */                   self.readyState = Transport.ReadyState.PAUSED;
/*  51 */                   onPause.run();
/*     */                 }
/*     */               };
/*     */             
/*  55 */             if (Polling.this.polling || !Polling.this.writable) {
/*  56 */               final int[] total = { 0 };
/*     */               
/*  58 */               if (Polling.this.polling) {
/*  59 */                 Polling.logger.fine("we are currently polling - waiting to pause");
/*  60 */                 total[0] = total[0] + 1;
/*  61 */                 Polling.this.once("pollComplete", new Emitter.Listener()
/*     */                     {
/*     */                       public void call(Object... args) {
/*  64 */                         Polling.logger.fine("pre-pause polling complete");
/*  65 */                         total[0] = total[0] - 1; if (total[0] - 1 == 0) {
/*  66 */                           pause.run();
/*     */                         }
/*     */                       }
/*     */                     });
/*     */               } 
/*     */               
/*  72 */               if (!Polling.this.writable) {
/*  73 */                 Polling.logger.fine("we are currently writing - waiting to pause");
/*  74 */                 total[0] = total[0] + 1;
/*  75 */                 Polling.this.once("drain", new Emitter.Listener()
/*     */                     {
/*     */                       public void call(Object... args) {
/*  78 */                         Polling.logger.fine("pre-pause writing complete");
/*  79 */                         total[0] = total[0] - 1; if (total[0] - 1 == 0) {
/*  80 */                           pause.run();
/*     */                         }
/*     */                       }
/*     */                     });
/*     */               } 
/*     */             } else {
/*  86 */               pause.run();
/*     */             } 
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   private void poll() {
/*  93 */     logger.fine("polling");
/*  94 */     this.polling = true;
/*  95 */     doPoll();
/*  96 */     emit("poll", new Object[0]);
/*     */   }
/*     */ 
/*     */   
/*     */   protected void onData(String data) {
/* 101 */     _onData(data);
/*     */   }
/*     */ 
/*     */   
/*     */   protected void onData(byte[] data) {
/* 106 */     _onData(data);
/*     */   }
/*     */   
/*     */   private void _onData(Object data) {
/* 110 */     final Polling self = this;
/* 111 */     if (logger.isLoggable(Level.FINE)) {
/* 112 */       logger.fine(String.format("polling got data %s", new Object[] { data }));
/*     */     }
/* 114 */     Parser.DecodePayloadCallback callback = new Parser.DecodePayloadCallback()
/*     */       {
/*     */         public boolean call(Packet packet, int index, int total) {
/* 117 */           if (self.readyState == Transport.ReadyState.OPENING && "open".equals(packet.type)) {
/* 118 */             self.onOpen();
/*     */           }
/*     */           
/* 121 */           if ("close".equals(packet.type)) {
/* 122 */             self.onClose();
/* 123 */             return false;
/*     */           } 
/*     */           
/* 126 */           self.onPacket(packet);
/* 127 */           return true;
/*     */         }
/*     */       };
/*     */     
/* 131 */     Parser.decodePayload((String)data, callback);
/*     */     
/* 133 */     if (this.readyState != Transport.ReadyState.CLOSED) {
/* 134 */       this.polling = false;
/* 135 */       emit("pollComplete", new Object[0]);
/*     */       
/* 137 */       if (this.readyState == Transport.ReadyState.OPEN) {
/* 138 */         poll();
/*     */       }
/* 140 */       else if (logger.isLoggable(Level.FINE)) {
/* 141 */         logger.fine(String.format("ignoring poll - transport state '%s'", new Object[] { this.readyState }));
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected void doClose() {
/* 148 */     final Polling self = this;
/*     */     
/* 150 */     Emitter.Listener close = new Emitter.Listener()
/*     */       {
/*     */         public void call(Object... args) {
/* 153 */           Polling.logger.fine("writing close packet");
/* 154 */           self.write(new Packet[] { new Packet("close") });
/*     */         }
/*     */       };
/*     */     
/* 158 */     if (this.readyState == Transport.ReadyState.OPEN) {
/* 159 */       logger.fine("transport open - closing");
/* 160 */       close.call(new Object[0]);
/*     */     }
/*     */     else {
/*     */       
/* 164 */       logger.fine("transport not open - deferring close");
/* 165 */       once("open", close);
/*     */     } 
/*     */   }
/*     */   
/*     */   protected void write(Packet[] packets) {
/* 170 */     final Polling self = this;
/* 171 */     this.writable = false;
/* 172 */     final Runnable callbackfn = new Runnable()
/*     */       {
/*     */         public void run() {
/* 175 */           self.writable = true;
/* 176 */           self.emit("drain", new Object[0]);
/*     */         }
/*     */       };
/*     */     
/* 180 */     Parser.encodePayload(packets, new Parser.EncodeCallback<String>()
/*     */         {
/*     */           public void call(String data) {
/* 183 */             self.doWrite(data, callbackfn);
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   protected String uri() {
/* 189 */     Map<String, String> query = this.query;
/* 190 */     if (query == null) {
/* 191 */       query = new HashMap<>();
/*     */     }
/* 193 */     String schema = this.secure ? "https" : "http";
/* 194 */     String port = "";
/*     */     
/* 196 */     if (this.timestampRequests) {
/* 197 */       query.put(this.timestampParam, Yeast.yeast());
/*     */     }
/*     */     
/* 200 */     String derivedQuery = ParseQS.encode(query);
/*     */     
/* 202 */     if (this.port > 0 && (("https".equals(schema) && this.port != 443) || ("http"
/* 203 */       .equals(schema) && this.port != 80))) {
/* 204 */       port = ":" + this.port;
/*     */     }
/*     */     
/* 207 */     if (derivedQuery.length() > 0) {
/* 208 */       derivedQuery = "?" + derivedQuery;
/*     */     }
/*     */     
/* 211 */     boolean ipv6 = this.hostname.contains(":");
/* 212 */     return schema + "://" + (ipv6 ? ("[" + this.hostname + "]") : this.hostname) + port + this.path + derivedQuery;
/*     */   }
/*     */   
/*     */   protected abstract void doWrite(String paramString, Runnable paramRunnable);
/*     */   
/*     */   protected abstract void doPoll();
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\transports\Polling.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */