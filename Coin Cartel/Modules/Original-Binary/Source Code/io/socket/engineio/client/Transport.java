/*     */ package io.socket.engineio.client;public abstract class Transport extends Emitter { public static final String EVENT_OPEN = "open"; public static final String EVENT_CLOSE = "close"; public static final String EVENT_PACKET = "packet"; public static final String EVENT_DRAIN = "drain"; public static final String EVENT_ERROR = "error"; public static final String EVENT_REQUEST_HEADERS = "requestHeaders"; public static final String EVENT_RESPONSE_HEADERS = "responseHeaders";
/*     */   public boolean writable;
/*     */   public String name;
/*     */   public Map<String, String> query;
/*     */   protected boolean secure;
/*     */   protected boolean timestampRequests;
/*     */   protected int port;
/*     */   protected String path;
/*     */   protected String hostname;
/*     */   protected String timestampParam;
/*     */   protected Socket socket;
/*     */   protected ReadyState readyState;
/*     */   protected WebSocket.Factory webSocketFactory;
/*     */   protected Call.Factory callFactory;
/*     */   protected Map<String, List<String>> extraHeaders;
/*     */   
/*  17 */   protected enum ReadyState { OPENING, OPEN, CLOSED, PAUSED;
/*     */ 
/*     */     
/*     */     public String toString() {
/*  21 */       return super.toString().toLowerCase();
/*     */     } }
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
/*     */   public Transport(Options opts) {
/*  50 */     this.path = opts.path;
/*  51 */     this.hostname = opts.hostname;
/*  52 */     this.port = opts.port;
/*  53 */     this.secure = opts.secure;
/*  54 */     this.query = opts.query;
/*  55 */     this.timestampParam = opts.timestampParam;
/*  56 */     this.timestampRequests = opts.timestampRequests;
/*  57 */     this.socket = opts.socket;
/*  58 */     this.webSocketFactory = opts.webSocketFactory;
/*  59 */     this.callFactory = opts.callFactory;
/*  60 */     this.extraHeaders = opts.extraHeaders;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Transport onError(String msg, Exception desc) {
/*  65 */     Exception err = new EngineIOException(msg, desc);
/*  66 */     emit("error", new Object[] { err });
/*  67 */     return this;
/*     */   }
/*     */   
/*     */   public Transport open() {
/*  71 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/*  74 */             if (Transport.this.readyState == Transport.ReadyState.CLOSED || Transport.this.readyState == null) {
/*  75 */               Transport.this.readyState = Transport.ReadyState.OPENING;
/*  76 */               Transport.this.doOpen();
/*     */             } 
/*     */           }
/*     */         });
/*  80 */     return this;
/*     */   }
/*     */   
/*     */   public Transport close() {
/*  84 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/*  87 */             if (Transport.this.readyState == Transport.ReadyState.OPENING || Transport.this.readyState == Transport.ReadyState.OPEN) {
/*  88 */               Transport.this.doClose();
/*  89 */               Transport.this.onClose();
/*     */             } 
/*     */           }
/*     */         });
/*  93 */     return this;
/*     */   }
/*     */   
/*     */   public void send(final Packet[] packets) {
/*  97 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 100 */             if (Transport.this.readyState == Transport.ReadyState.OPEN) {
/* 101 */               Transport.this.write(packets);
/*     */             } else {
/* 103 */               throw new RuntimeException("Transport not open");
/*     */             } 
/*     */           }
/*     */         });
/*     */   }
/*     */   
/*     */   protected void onOpen() {
/* 110 */     this.readyState = ReadyState.OPEN;
/* 111 */     this.writable = true;
/* 112 */     emit("open", new Object[0]);
/*     */   }
/*     */   
/*     */   protected void onData(String data) {
/* 116 */     onPacket(Parser.decodePacket(data));
/*     */   }
/*     */   
/*     */   protected void onData(byte[] data) {
/* 120 */     onPacket(Parser.decodePacket(data));
/*     */   }
/*     */   
/*     */   protected void onPacket(Packet packet) {
/* 124 */     emit("packet", new Object[] { packet });
/*     */   }
/*     */   
/*     */   protected void onClose() {
/* 128 */     this.readyState = ReadyState.CLOSED;
/* 129 */     emit("close", new Object[0]);
/*     */   }
/*     */ 
/*     */   
/*     */   protected abstract void write(Packet[] paramArrayOfPacket);
/*     */   
/*     */   protected abstract void doOpen();
/*     */   
/*     */   protected abstract void doClose();
/*     */   
/*     */   public static class Options
/*     */   {
/*     */     public String hostname;
/*     */     public String path;
/*     */     public String timestampParam;
/*     */     public boolean secure;
/*     */     public boolean timestampRequests;
/* 146 */     public int port = -1;
/* 147 */     public int policyPort = -1;
/*     */     public Map<String, String> query;
/*     */     protected Socket socket;
/*     */     public WebSocket.Factory webSocketFactory;
/*     */     public Call.Factory callFactory;
/*     */     public Map<String, List<String>> extraHeaders;
/*     */   } }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\Transport.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */