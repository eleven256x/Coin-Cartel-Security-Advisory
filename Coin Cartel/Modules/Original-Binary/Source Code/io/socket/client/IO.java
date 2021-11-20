/*     */ package io.socket.client;
/*     */ 
/*     */ import java.net.URI;
/*     */ import java.net.URISyntaxException;
/*     */ import java.net.URL;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.WebSocket;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class IO
/*     */ {
/*  18 */   private static final Logger logger = Logger.getLogger(IO.class.getName());
/*     */   
/*  20 */   private static final ConcurrentHashMap<String, Manager> managers = new ConcurrentHashMap<>();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  25 */   public static int protocol = 5;
/*     */   
/*     */   public static void setDefaultOkHttpWebSocketFactory(WebSocket.Factory factory) {
/*  28 */     Manager.defaultWebSocketFactory = factory;
/*     */   }
/*     */   
/*     */   public static void setDefaultOkHttpCallFactory(Call.Factory factory) {
/*  32 */     Manager.defaultCallFactory = factory;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static Socket socket(String uri) throws URISyntaxException {
/*  38 */     return socket(uri, (Options)null);
/*     */   }
/*     */   
/*     */   public static Socket socket(String uri, Options opts) throws URISyntaxException {
/*  42 */     return socket(new URI(uri), opts);
/*     */   }
/*     */   
/*     */   public static Socket socket(URI uri) {
/*  46 */     return socket(uri, (Options)null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Socket socket(URI uri, Options opts) {
/*     */     URI source;
/*     */     Manager io;
/*  57 */     if (opts == null) {
/*  58 */       opts = new Options();
/*     */     }
/*     */     
/*  61 */     URL parsed = Url.parse(uri);
/*     */     
/*     */     try {
/*  64 */       source = parsed.toURI();
/*  65 */     } catch (URISyntaxException e) {
/*  66 */       throw new RuntimeException(e);
/*     */     } 
/*  68 */     String id = Url.extractId(parsed);
/*  69 */     String path = parsed.getPath();
/*     */     
/*  71 */     boolean sameNamespace = (managers.containsKey(id) && ((Manager)managers.get(id)).nsps.containsKey(path));
/*  72 */     boolean newConnection = (opts.forceNew || !opts.multiplex || sameNamespace);
/*     */ 
/*     */     
/*  75 */     String query = parsed.getQuery();
/*  76 */     if (query != null && (opts.query == null || opts.query.isEmpty())) {
/*  77 */       opts.query = query;
/*     */     }
/*     */     
/*  80 */     if (newConnection) {
/*  81 */       if (logger.isLoggable(Level.FINE)) {
/*  82 */         logger.fine(String.format("ignoring socket cache for %s", new Object[] { source }));
/*     */       }
/*  84 */       io = new Manager(source, opts);
/*     */     } else {
/*  86 */       if (!managers.containsKey(id)) {
/*  87 */         if (logger.isLoggable(Level.FINE)) {
/*  88 */           logger.fine(String.format("new io instance for %s", new Object[] { source }));
/*     */         }
/*  90 */         managers.putIfAbsent(id, new Manager(source, opts));
/*     */       } 
/*  92 */       io = managers.get(id);
/*     */     } 
/*     */     
/*  95 */     return io.socket(parsed.getPath(), opts);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static class Options
/*     */     extends Manager.Options
/*     */   {
/*     */     public boolean forceNew;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public boolean multiplex = true;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public static SocketOptionBuilder builder() {
/* 121 */       return SocketOptionBuilder.builder();
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\client\IO.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */