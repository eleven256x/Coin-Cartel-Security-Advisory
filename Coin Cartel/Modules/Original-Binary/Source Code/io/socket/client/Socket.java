/*     */ package io.socket.client;
/*     */ 
/*     */ import io.socket.emitter.Emitter;
/*     */ import io.socket.parser.Packet;
/*     */ import io.socket.thread.EventThread;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Queue;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public class Socket extends Emitter {
/*  20 */   private static final Logger logger = Logger.getLogger(Socket.class.getName());
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final String EVENT_CONNECT = "connect";
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final String EVENT_DISCONNECT = "disconnect";
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static final String EVENT_CONNECT_ERROR = "connect_error";
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final String EVENT_MESSAGE = "message";
/*     */ 
/*     */ 
/*     */   
/*  44 */   protected static Map<String, Integer> RESERVED_EVENTS = new HashMap<String, Integer>()
/*     */     {
/*     */     
/*     */     };
/*     */   
/*     */   String id;
/*     */   
/*     */   private volatile boolean connected;
/*     */   
/*     */   private int ids;
/*     */   
/*     */   private String nsp;
/*     */   
/*     */   private Manager io;
/*     */   
/*     */   private Map<String, String> auth;
/*     */   
/*  61 */   private Map<Integer, Ack> acks = new HashMap<>();
/*     */   private Queue<On.Handle> subs;
/*  63 */   private final Queue<List<Object>> receiveBuffer = new LinkedList<>();
/*  64 */   private final Queue<Packet<JSONArray>> sendBuffer = new LinkedList<>();
/*     */   
/*     */   public Socket(Manager io, String nsp, Manager.Options opts) {
/*  67 */     this.io = io;
/*  68 */     this.nsp = nsp;
/*  69 */     if (opts != null) {
/*  70 */       this.auth = opts.auth;
/*     */     }
/*     */   }
/*     */   
/*     */   private void subEvents() {
/*  75 */     if (this.subs != null)
/*     */       return; 
/*  77 */     final Manager io = this.io;
/*  78 */     this.subs = new LinkedList<On.Handle>()
/*     */       {
/*     */       
/*     */       };
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isActive() {
/* 107 */     return (this.subs != null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket open() {
/* 114 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 117 */             if (Socket.this.connected || Socket.this.io.isReconnecting())
/*     */               return; 
/* 119 */             Socket.this.subEvents();
/* 120 */             Socket.this.io.open();
/* 121 */             if (Manager.ReadyState.OPEN == Socket.this.io.readyState) Socket.this.onopen(); 
/*     */           }
/*     */         });
/* 124 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket connect() {
/* 131 */     return open();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket send(Object... args) {
/* 141 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 144 */             Socket.this.emit("message", args);
/*     */           }
/*     */         });
/* 147 */     return this;
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
/*     */   public Emitter emit(final String event, Object... args) {
/* 159 */     if (RESERVED_EVENTS.containsKey(event)) {
/* 160 */       throw new RuntimeException("'" + event + "' is a reserved event name");
/*     */     }
/*     */     
/* 163 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/*     */             Object[] _args;
/*     */             Ack ack;
/* 168 */             int lastIndex = args.length - 1;
/*     */             
/* 170 */             if (args.length > 0 && args[lastIndex] instanceof Ack) {
/* 171 */               _args = new Object[lastIndex];
/* 172 */               for (int i = 0; i < lastIndex; i++) {
/* 173 */                 _args[i] = args[i];
/*     */               }
/* 175 */               ack = (Ack)args[lastIndex];
/*     */             } else {
/* 177 */               _args = args;
/* 178 */               ack = null;
/*     */             } 
/*     */             
/* 181 */             Socket.this.emit(event, _args, ack);
/*     */           }
/*     */         });
/* 184 */     return this;
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
/*     */   public Emitter emit(final String event, final Object[] args, final Ack ack) {
/* 196 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 199 */             JSONArray jsonArgs = new JSONArray();
/* 200 */             jsonArgs.put(event);
/*     */             
/* 202 */             if (args != null) {
/* 203 */               for (Object arg : args) {
/* 204 */                 jsonArgs.put(arg);
/*     */               }
/*     */             }
/*     */             
/* 208 */             Packet<JSONArray> packet = new Packet(2, jsonArgs);
/*     */             
/* 210 */             if (ack != null) {
/* 211 */               Socket.logger.fine(String.format("emitting packet with ack id %d", new Object[] { Integer.valueOf(Socket.access$700(this.this$0)) }));
/* 212 */               Socket.this.acks.put(Integer.valueOf(Socket.this.ids), ack);
/* 213 */               packet.id = Socket.this.ids++;
/*     */             } 
/*     */             
/* 216 */             if (Socket.this.connected) {
/* 217 */               Socket.this.packet(packet);
/*     */             } else {
/* 219 */               Socket.this.sendBuffer.add(packet);
/*     */             } 
/*     */           }
/*     */         });
/* 223 */     return this;
/*     */   }
/*     */   
/*     */   private void packet(Packet packet) {
/* 227 */     packet.nsp = this.nsp;
/* 228 */     this.io.packet(packet);
/*     */   }
/*     */   
/*     */   private void onopen() {
/* 232 */     logger.fine("transport is open - connecting");
/*     */     
/* 234 */     if (this.auth != null) {
/* 235 */       packet(new Packet(0, new JSONObject(this.auth)));
/*     */     } else {
/* 237 */       packet(new Packet(0));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void onclose(String reason) {
/* 242 */     if (logger.isLoggable(Level.FINE)) {
/* 243 */       logger.fine(String.format("close (%s)", new Object[] { reason }));
/*     */     }
/* 245 */     this.connected = false;
/* 246 */     this.id = null;
/* 247 */     super.emit("disconnect", new Object[] { reason });
/*     */   }
/*     */   private void onpacket(Packet<?> packet) {
/*     */     Packet<JSONArray> p;
/* 251 */     if (!this.nsp.equals(packet.nsp))
/*     */       return; 
/* 253 */     switch (packet.type) {
/*     */       case 0:
/* 255 */         if (packet.data instanceof JSONObject && ((JSONObject)packet.data).has("sid"))
/*     */           try {
/* 257 */             onconnect(((JSONObject)packet.data).getString("sid"));
/*     */             return;
/* 259 */           } catch (JSONException jSONException) {
/*     */             break;
/* 261 */           }   super.emit("connect_error", new Object[] { new SocketIOException("It seems you are trying to reach a Socket.IO server in v2.x with a v3.x client, which is not possible") });
/*     */         break;
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       case 2:
/* 268 */         p = (Packet)packet;
/* 269 */         onevent(p);
/*     */         break;
/*     */ 
/*     */ 
/*     */       
/*     */       case 5:
/* 275 */         p = (Packet)packet;
/* 276 */         onevent(p);
/*     */         break;
/*     */ 
/*     */ 
/*     */       
/*     */       case 3:
/* 282 */         p = (Packet)packet;
/* 283 */         onack(p);
/*     */         break;
/*     */ 
/*     */ 
/*     */       
/*     */       case 6:
/* 289 */         p = (Packet)packet;
/* 290 */         onack(p);
/*     */         break;
/*     */ 
/*     */       
/*     */       case 1:
/* 295 */         ondisconnect();
/*     */         break;
/*     */       
/*     */       case 4:
/* 299 */         super.emit("connect_error", new Object[] { packet.data });
/*     */         break;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void onevent(Packet<JSONArray> packet) {
/* 305 */     List<Object> args = new ArrayList(Arrays.asList(toArray((JSONArray)packet.data)));
/* 306 */     if (logger.isLoggable(Level.FINE)) {
/* 307 */       logger.fine(String.format("emitting event %s", new Object[] { args }));
/*     */     }
/*     */     
/* 310 */     if (packet.id >= 0) {
/* 311 */       logger.fine("attaching ack callback to event");
/* 312 */       args.add(ack(packet.id));
/*     */     } 
/*     */     
/* 315 */     if (this.connected) {
/* 316 */       if (args.isEmpty())
/* 317 */         return;  String event = args.remove(0).toString();
/* 318 */       super.emit(event, args.toArray());
/*     */     } else {
/* 320 */       this.receiveBuffer.add(args);
/*     */     } 
/*     */   }
/*     */   
/*     */   private Ack ack(final int id) {
/* 325 */     final Socket self = this;
/* 326 */     final boolean[] sent = { false };
/* 327 */     return new Ack()
/*     */       {
/*     */         public void call(Object... args) {
/* 330 */           EventThread.exec(new Runnable()
/*     */               {
/*     */                 public void run() {
/* 333 */                   if (sent[0])
/* 334 */                     return;  sent[0] = true;
/* 335 */                   if (Socket.logger.isLoggable(Level.FINE)) {
/* 336 */                     Socket.logger.fine(String.format("sending ack %s", (args.length != 0) ? args : null));
/*     */                   }
/*     */                   
/* 339 */                   JSONArray jsonArgs = new JSONArray();
/* 340 */                   for (Object arg : args) {
/* 341 */                     jsonArgs.put(arg);
/*     */                   }
/*     */                   
/* 344 */                   Packet<JSONArray> packet = new Packet(3, jsonArgs);
/* 345 */                   packet.id = id;
/* 346 */                   self.packet(packet);
/*     */                 }
/*     */               });
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   private void onack(Packet<JSONArray> packet) {
/* 354 */     Ack fn = this.acks.remove(Integer.valueOf(packet.id));
/* 355 */     if (fn != null) {
/* 356 */       if (logger.isLoggable(Level.FINE)) {
/* 357 */         logger.fine(String.format("calling ack %s with %s", new Object[] { Integer.valueOf(packet.id), packet.data }));
/*     */       }
/* 359 */       fn.call(toArray((JSONArray)packet.data));
/*     */     }
/* 361 */     else if (logger.isLoggable(Level.FINE)) {
/* 362 */       logger.fine(String.format("bad ack %s", new Object[] { Integer.valueOf(packet.id) }));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void onconnect(String id) {
/* 368 */     this.connected = true;
/* 369 */     this.id = id;
/* 370 */     super.emit("connect", new Object[0]);
/* 371 */     emitBuffered();
/*     */   }
/*     */   
/*     */   private void emitBuffered() {
/*     */     List<Object> data;
/* 376 */     while ((data = this.receiveBuffer.poll()) != null) {
/* 377 */       String event = (String)data.get(0);
/* 378 */       super.emit(event, data.toArray());
/*     */     } 
/* 380 */     this.receiveBuffer.clear();
/*     */     
/*     */     Packet<JSONArray> packet;
/* 383 */     while ((packet = this.sendBuffer.poll()) != null) {
/* 384 */       packet(packet);
/*     */     }
/* 386 */     this.sendBuffer.clear();
/*     */   }
/*     */   
/*     */   private void ondisconnect() {
/* 390 */     if (logger.isLoggable(Level.FINE)) {
/* 391 */       logger.fine(String.format("server disconnect (%s)", new Object[] { this.nsp }));
/*     */     }
/* 393 */     destroy();
/* 394 */     onclose("io server disconnect");
/*     */   }
/*     */   
/*     */   private void destroy() {
/* 398 */     if (this.subs != null) {
/*     */       
/* 400 */       for (On.Handle sub : this.subs) {
/* 401 */         sub.destroy();
/*     */       }
/* 403 */       this.subs = null;
/*     */     } 
/*     */     
/* 406 */     this.io.destroy();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket close() {
/* 415 */     EventThread.exec(new Runnable()
/*     */         {
/*     */           public void run() {
/* 418 */             if (Socket.this.connected) {
/* 419 */               if (Socket.logger.isLoggable(Level.FINE)) {
/* 420 */                 Socket.logger.fine(String.format("performing disconnect (%s)", new Object[] { Socket.access$1200(this.this$0) }));
/*     */               }
/* 422 */               Socket.this.packet(new Packet(1));
/*     */             } 
/*     */             
/* 425 */             Socket.this.destroy();
/*     */             
/* 427 */             if (Socket.this.connected) {
/* 428 */               Socket.this.onclose("io client disconnect");
/*     */             }
/*     */           }
/*     */         });
/* 432 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Socket disconnect() {
/* 441 */     return close();
/*     */   }
/*     */   
/*     */   public Manager io() {
/* 445 */     return this.io;
/*     */   }
/*     */   
/*     */   public boolean connected() {
/* 449 */     return this.connected;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String id() {
/* 460 */     return this.id;
/*     */   }
/*     */   
/*     */   private static Object[] toArray(JSONArray array) {
/* 464 */     int length = array.length();
/* 465 */     Object[] data = new Object[length];
/* 466 */     for (int i = 0; i < length; i++) {
/*     */       Object v;
/*     */       try {
/* 469 */         v = array.get(i);
/* 470 */       } catch (JSONException e) {
/* 471 */         logger.log(Level.WARNING, "An error occured while retrieving data from JSONArray", (Throwable)e);
/* 472 */         v = null;
/*     */       } 
/* 474 */       data[i] = JSONObject.NULL.equals(v) ? null : v;
/*     */     } 
/* 476 */     return data;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\client\Socket.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */