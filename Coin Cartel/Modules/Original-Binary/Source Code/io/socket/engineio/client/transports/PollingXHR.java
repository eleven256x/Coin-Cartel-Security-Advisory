/*     */ package io.socket.engineio.client.transports;
/*     */ 
/*     */ import io.socket.emitter.Emitter;
/*     */ import io.socket.engineio.client.Transport;
/*     */ import io.socket.thread.EventThread;
/*     */ import java.io.IOException;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TreeMap;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.Callback;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.MediaType;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.RequestBody;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.ResponseBody;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PollingXHR
/*     */   extends Polling
/*     */ {
/*  28 */   private static final Logger logger = Logger.getLogger(PollingXHR.class.getName());
/*     */   
/*  30 */   private static boolean LOGGABLE_FINE = logger.isLoggable(Level.FINE);
/*     */   
/*     */   public PollingXHR(Transport.Options opts) {
/*  33 */     super(opts);
/*     */   }
/*     */   
/*     */   protected Request request() {
/*  37 */     return request((Request.Options)null);
/*     */   }
/*     */   
/*     */   protected Request request(Request.Options opts) {
/*  41 */     if (opts == null) {
/*  42 */       opts = new Request.Options();
/*     */     }
/*  44 */     opts.uri = uri();
/*  45 */     opts.callFactory = this.callFactory;
/*  46 */     opts.extraHeaders = this.extraHeaders;
/*     */     
/*  48 */     Request req = new Request(opts);
/*     */     
/*  50 */     final PollingXHR self = this;
/*  51 */     req.on("requestHeaders", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args)
/*     */           {
/*  55 */             self.emit("requestHeaders", new Object[] { args[0] });
/*     */           }
/*  57 */         }).on("responseHeaders", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/*  60 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  63 */                     self.emit("responseHeaders", new Object[] { this.val$args[0] });
/*     */                   }
/*     */                 });
/*     */           }
/*     */         });
/*  68 */     return req;
/*     */   }
/*     */ 
/*     */   
/*     */   protected void doWrite(String data, final Runnable fn) {
/*  73 */     Request.Options opts = new Request.Options();
/*  74 */     opts.method = "POST";
/*  75 */     opts.data = data;
/*  76 */     opts.extraHeaders = this.extraHeaders;
/*  77 */     Request req = request(opts);
/*  78 */     final PollingXHR self = this;
/*  79 */     req.on("success", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/*  82 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  85 */                     fn.run();
/*     */                   }
/*     */                 });
/*     */           }
/*     */         });
/*  90 */     req.on("error", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/*  93 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/*  96 */                     Exception err = (args.length > 0 && args[0] instanceof Exception) ? (Exception)args[0] : null;
/*  97 */                     self.onError("xhr post error", err);
/*     */                   }
/*     */                 });
/*     */           }
/*     */         });
/* 102 */     req.create();
/*     */   }
/*     */ 
/*     */   
/*     */   protected void doPoll() {
/* 107 */     logger.fine("xhr poll");
/* 108 */     Request req = request();
/* 109 */     final PollingXHR self = this;
/* 110 */     req.on("data", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/* 113 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/* 116 */                     Object arg = (args.length > 0) ? args[0] : null;
/* 117 */                     self.onData((String)arg);
/*     */                   }
/*     */                 });
/*     */           }
/*     */         });
/* 122 */     req.on("error", new Emitter.Listener()
/*     */         {
/*     */           public void call(Object... args) {
/* 125 */             EventThread.exec(new Runnable()
/*     */                 {
/*     */                   public void run() {
/* 128 */                     Exception err = (args.length > 0 && args[0] instanceof Exception) ? (Exception)args[0] : null;
/* 129 */                     self.onError("xhr poll error", err);
/*     */                   }
/*     */                 });
/*     */           }
/*     */         });
/* 134 */     req.create();
/*     */   }
/*     */ 
/*     */   
/*     */   public static class Request
/*     */     extends Emitter
/*     */   {
/*     */     public static final String EVENT_SUCCESS = "success";
/*     */     public static final String EVENT_DATA = "data";
/*     */     public static final String EVENT_ERROR = "error";
/*     */     public static final String EVENT_REQUEST_HEADERS = "requestHeaders";
/*     */     public static final String EVENT_RESPONSE_HEADERS = "responseHeaders";
/*     */     private static final String TEXT_CONTENT_TYPE = "text/plain;charset=UTF-8";
/* 147 */     private static final MediaType TEXT_MEDIA_TYPE = MediaType.parse("text/plain;charset=UTF-8");
/*     */     
/*     */     private String method;
/*     */     
/*     */     private String uri;
/*     */     
/*     */     private String data;
/*     */     private Call.Factory callFactory;
/*     */     private Map<String, List<String>> extraHeaders;
/*     */     private Response response;
/*     */     private Call requestCall;
/*     */     
/*     */     public Request(Options opts) {
/* 160 */       this.method = (opts.method != null) ? opts.method : "GET";
/* 161 */       this.uri = opts.uri;
/* 162 */       this.data = opts.data;
/* 163 */       this.callFactory = (opts.callFactory != null) ? opts.callFactory : (Call.Factory)new OkHttpClient();
/* 164 */       this.extraHeaders = opts.extraHeaders;
/*     */     }
/*     */     
/*     */     public void create() {
/* 168 */       final Request self = this;
/* 169 */       if (PollingXHR.LOGGABLE_FINE) PollingXHR.logger.fine(String.format("xhr open %s: %s", new Object[] { this.method, this.uri })); 
/* 170 */       Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
/* 171 */       if (this.extraHeaders != null) {
/* 172 */         headers.putAll(this.extraHeaders);
/*     */       }
/* 174 */       if ("POST".equals(this.method)) {
/* 175 */         headers.put("Content-type", new LinkedList<>(Collections.singletonList("text/plain;charset=UTF-8")));
/*     */       }
/*     */       
/* 178 */       headers.put("Accept", new LinkedList<>(Collections.singletonList("*/*")));
/*     */       
/* 180 */       onRequestHeaders(headers);
/*     */       
/* 182 */       if (PollingXHR.LOGGABLE_FINE) {
/* 183 */         PollingXHR.logger.fine(String.format("sending xhr with url %s | data %s", new Object[] { this.uri, this.data }));
/*     */       }
/*     */       
/* 186 */       okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
/* 187 */       for (Map.Entry<String, List<String>> header : headers.entrySet()) {
/* 188 */         for (String v : header.getValue()) {
/* 189 */           requestBuilder.addHeader(header.getKey(), v);
/*     */         }
/*     */       } 
/* 192 */       RequestBody body = null;
/* 193 */       if (this.data != null) {
/* 194 */         body = RequestBody.create(TEXT_MEDIA_TYPE, this.data);
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 200 */       okhttp3.Request request = requestBuilder.url(HttpUrl.parse(self.uri)).method(self.method, body).build();
/*     */       
/* 202 */       this.requestCall = this.callFactory.newCall(request);
/* 203 */       this.requestCall.enqueue(new Callback()
/*     */           {
/*     */             public void onFailure(Call call, IOException e) {
/* 206 */               self.onError(e);
/*     */             }
/*     */ 
/*     */             
/*     */             public void onResponse(Call call, Response response) throws IOException {
/* 211 */               self.response = response;
/* 212 */               self.onResponseHeaders(response.headers().toMultimap());
/*     */               
/*     */               try {
/* 215 */                 if (response.isSuccessful()) {
/* 216 */                   self.onLoad();
/*     */                 } else {
/* 218 */                   self.onError(new IOException(Integer.toString(response.code())));
/*     */                 } 
/*     */               } finally {
/* 221 */                 response.close();
/*     */               } 
/*     */             }
/*     */           });
/*     */     }
/*     */     
/*     */     private void onSuccess() {
/* 228 */       emit("success", new Object[0]);
/*     */     }
/*     */     
/*     */     private void onData(String data) {
/* 232 */       emit("data", new Object[] { data });
/* 233 */       onSuccess();
/*     */     }
/*     */     
/*     */     private void onError(Exception err) {
/* 237 */       emit("error", new Object[] { err });
/*     */     }
/*     */     
/*     */     private void onRequestHeaders(Map<String, List<String>> headers) {
/* 241 */       emit("requestHeaders", new Object[] { headers });
/*     */     }
/*     */     
/*     */     private void onResponseHeaders(Map<String, List<String>> headers) {
/* 245 */       emit("responseHeaders", new Object[] { headers });
/*     */     }
/*     */     
/*     */     private void onLoad() {
/* 249 */       ResponseBody body = this.response.body();
/*     */       
/*     */       try {
/* 252 */         onData(body.string());
/* 253 */       } catch (IOException e) {
/* 254 */         onError(e);
/*     */       } 
/*     */     }
/*     */     
/*     */     public static class Options {
/*     */       public String uri;
/*     */       public String method;
/*     */       public String data;
/*     */       public Call.Factory callFactory;
/*     */       public Map<String, List<String>> extraHeaders;
/*     */     }
/*     */   }
/*     */   
/*     */   public static class Options {
/*     */     public String uri;
/*     */     public String method;
/*     */     public String data;
/*     */     public Call.Factory callFactory;
/*     */     public Map<String, List<String>> extraHeaders;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\engineio\client\transports\PollingXHR.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */