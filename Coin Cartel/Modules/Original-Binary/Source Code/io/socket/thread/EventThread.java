/*    */ package io.socket.thread;
/*    */ 
/*    */ import java.util.concurrent.ExecutorService;
/*    */ import java.util.concurrent.Executors;
/*    */ import java.util.concurrent.ThreadFactory;
/*    */ import java.util.logging.Level;
/*    */ import java.util.logging.Logger;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class EventThread
/*    */   extends Thread
/*    */ {
/* 16 */   private static final Logger logger = Logger.getLogger(EventThread.class.getName());
/*    */   
/* 18 */   private static final ThreadFactory THREAD_FACTORY = new ThreadFactory()
/*    */     {
/*    */       public Thread newThread(Runnable runnable) {
/* 21 */         EventThread.thread = new EventThread(runnable);
/* 22 */         EventThread.thread.setName("EventThread");
/* 23 */         EventThread.thread.setDaemon(Thread.currentThread().isDaemon());
/* 24 */         return EventThread.thread;
/*    */       }
/*    */     };
/*    */ 
/*    */   
/*    */   private static EventThread thread;
/*    */   
/*    */   private static ExecutorService service;
/* 32 */   private static int counter = 0;
/*    */ 
/*    */   
/*    */   private EventThread(Runnable runnable) {
/* 36 */     super(runnable);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static boolean isCurrent() {
/* 45 */     return (currentThread() == thread);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static void exec(Runnable task) {
/* 54 */     if (isCurrent()) {
/* 55 */       task.run();
/*    */     } else {
/* 57 */       nextTick(task);
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static void nextTick(final Runnable task) {
/*    */     ExecutorService executor;
/* 68 */     synchronized (EventThread.class) {
/* 69 */       counter++;
/* 70 */       if (service == null) {
/* 71 */         service = Executors.newSingleThreadExecutor(THREAD_FACTORY);
/*    */       }
/* 73 */       executor = service;
/*    */     } 
/*    */     
/* 76 */     executor.execute(new Runnable()
/*    */         {
/*    */           public void run() {
/*    */             try {
/* 80 */               task.run();
/* 81 */             } catch (Throwable t) {
/* 82 */               EventThread.logger.log(Level.SEVERE, "Task threw exception", t);
/* 83 */               throw t;
/*    */             } finally {
/* 85 */               synchronized (EventThread.class) {
/*    */                 EventThread.counter--;
/* 87 */                 if (EventThread.counter == 0) {
/* 88 */                   EventThread.service.shutdown();
/* 89 */                   EventThread.service = null;
/* 90 */                   EventThread.thread = null;
/*    */                 } 
/*    */               } 
/*    */             } 
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\thread\EventThread.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */