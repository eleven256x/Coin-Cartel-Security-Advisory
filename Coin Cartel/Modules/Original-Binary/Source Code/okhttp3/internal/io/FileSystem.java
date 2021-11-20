/*    */ package okhttp3.internal.io;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.IOException;
/*    */ import okio.Okio;
/*    */ import okio.Sink;
/*    */ import okio.Source;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public interface FileSystem
/*    */ {
/* 42 */   public static final FileSystem SYSTEM = new FileSystem() {
/*    */       public Source source(File file) throws FileNotFoundException {
/* 44 */         return Okio.source(file);
/*    */       }
/*    */       
/*    */       public Sink sink(File file) throws FileNotFoundException {
/*    */         try {
/* 49 */           return Okio.sink(file);
/* 50 */         } catch (FileNotFoundException e) {
/*    */           
/* 52 */           file.getParentFile().mkdirs();
/* 53 */           return Okio.sink(file);
/*    */         } 
/*    */       }
/*    */       
/*    */       public Sink appendingSink(File file) throws FileNotFoundException {
/*    */         try {
/* 59 */           return Okio.appendingSink(file);
/* 60 */         } catch (FileNotFoundException e) {
/*    */           
/* 62 */           file.getParentFile().mkdirs();
/* 63 */           return Okio.appendingSink(file);
/*    */         } 
/*    */       }
/*    */ 
/*    */       
/*    */       public void delete(File file) throws IOException {
/* 69 */         if (!file.delete() && file.exists()) {
/* 70 */           throw new IOException("failed to delete " + file);
/*    */         }
/*    */       }
/*    */       
/*    */       public boolean exists(File file) {
/* 75 */         return file.exists();
/*    */       }
/*    */       
/*    */       public long size(File file) {
/* 79 */         return file.length();
/*    */       }
/*    */       
/*    */       public void rename(File from, File to) throws IOException {
/* 83 */         delete(to);
/* 84 */         if (!from.renameTo(to)) {
/* 85 */           throw new IOException("failed to rename " + from + " to " + to);
/*    */         }
/*    */       }
/*    */       
/*    */       public void deleteContents(File directory) throws IOException {
/* 90 */         File[] files = directory.listFiles();
/* 91 */         if (files == null) {
/* 92 */           throw new IOException("not a readable directory: " + directory);
/*    */         }
/* 94 */         for (File file : files) {
/* 95 */           if (file.isDirectory()) {
/* 96 */             deleteContents(file);
/*    */           }
/* 98 */           if (!file.delete())
/* 99 */             throw new IOException("failed to delete " + file); 
/*    */         } 
/*    */       }
/*    */     };
/*    */   
/*    */   Source source(File paramFile) throws FileNotFoundException;
/*    */   
/*    */   Sink sink(File paramFile) throws FileNotFoundException;
/*    */   
/*    */   Sink appendingSink(File paramFile) throws FileNotFoundException;
/*    */   
/*    */   void delete(File paramFile) throws IOException;
/*    */   
/*    */   boolean exists(File paramFile);
/*    */   
/*    */   long size(File paramFile);
/*    */   
/*    */   void rename(File paramFile1, File paramFile2) throws IOException;
/*    */   
/*    */   void deleteContents(File paramFile) throws IOException;
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\io\FileSystem.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */