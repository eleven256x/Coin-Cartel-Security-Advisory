/*     */ package io.socket.emitter;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.ConcurrentLinkedQueue;
/*     */ import java.util.concurrent.ConcurrentMap;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Emitter
/*     */ {
/*  19 */   private ConcurrentMap<String, ConcurrentLinkedQueue<Listener>> callbacks = new ConcurrentHashMap<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Emitter on(String event, Listener fn) {
/*  29 */     ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
/*  30 */     if (callbacks == null) {
/*  31 */       callbacks = new ConcurrentLinkedQueue<>();
/*  32 */       ConcurrentLinkedQueue<Listener> tempCallbacks = this.callbacks.putIfAbsent(event, callbacks);
/*  33 */       if (tempCallbacks != null) {
/*  34 */         callbacks = tempCallbacks;
/*     */       }
/*     */     } 
/*  37 */     callbacks.add(fn);
/*  38 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Emitter once(String event, Listener fn) {
/*  49 */     on(event, new OnceListener(event, fn));
/*  50 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Emitter off() {
/*  59 */     this.callbacks.clear();
/*  60 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Emitter off(String event) {
/*  70 */     this.callbacks.remove(event);
/*  71 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Emitter off(String event, Listener fn) {
/*  82 */     ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
/*  83 */     if (callbacks != null) {
/*  84 */       Iterator<Listener> it = callbacks.iterator();
/*  85 */       while (it.hasNext()) {
/*  86 */         Listener internal = it.next();
/*  87 */         if (sameAs(fn, internal)) {
/*  88 */           it.remove();
/*     */           break;
/*     */         } 
/*     */       } 
/*     */     } 
/*  93 */     return this;
/*     */   }
/*     */   
/*     */   private static boolean sameAs(Listener fn, Listener internal) {
/*  97 */     if (fn.equals(internal))
/*  98 */       return true; 
/*  99 */     if (internal instanceof OnceListener) {
/* 100 */       return fn.equals(((OnceListener)internal).fn);
/*     */     }
/* 102 */     return false;
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
/*     */   public Emitter emit(String event, Object... args) {
/* 114 */     ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
/* 115 */     if (callbacks != null) {
/* 116 */       for (Listener fn : callbacks) {
/* 117 */         fn.call(args);
/*     */       }
/*     */     }
/* 120 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<Listener> listeners(String event) {
/* 130 */     ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
/* 131 */     return (callbacks != null) ? new ArrayList<>(callbacks) : new ArrayList<>(0);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean hasListeners(String event) {
/* 142 */     ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
/* 143 */     return (callbacks != null && !callbacks.isEmpty());
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private class OnceListener
/*     */     implements Listener
/*     */   {
/*     */     public final String event;
/*     */     
/*     */     public final Emitter.Listener fn;
/*     */ 
/*     */     
/*     */     public OnceListener(String event, Emitter.Listener fn) {
/* 157 */       this.event = event;
/* 158 */       this.fn = fn;
/*     */     }
/*     */ 
/*     */     
/*     */     public void call(Object... args) {
/* 163 */       Emitter.this.off(this.event, this);
/* 164 */       this.fn.call(args);
/*     */     }
/*     */   }
/*     */   
/*     */   public static interface Listener {
/*     */     void call(Object... param1VarArgs);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\emitter\Emitter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */