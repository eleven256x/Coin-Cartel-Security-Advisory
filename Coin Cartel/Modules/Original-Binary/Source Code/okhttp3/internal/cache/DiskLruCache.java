/*      */ package okhttp3.internal.cache;
/*      */ 
/*      */ import java.io.Closeable;
/*      */ import java.io.File;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.Flushable;
/*      */ import java.io.IOException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.NoSuchElementException;
/*      */ import java.util.concurrent.Executor;
/*      */ import java.util.concurrent.LinkedBlockingQueue;
/*      */ import java.util.concurrent.ThreadPoolExecutor;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ import javax.annotation.Nullable;
/*      */ import okhttp3.internal.Util;
/*      */ import okhttp3.internal.io.FileSystem;
/*      */ import okhttp3.internal.platform.Platform;
/*      */ import okio.BufferedSink;
/*      */ import okio.BufferedSource;
/*      */ import okio.Okio;
/*      */ import okio.Sink;
/*      */ import okio.Source;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public final class DiskLruCache
/*      */   implements Closeable, Flushable
/*      */ {
/*      */   static final String JOURNAL_FILE = "journal";
/*      */   static final String JOURNAL_FILE_TEMP = "journal.tmp";
/*      */   static final String JOURNAL_FILE_BACKUP = "journal.bkp";
/*      */   static final String MAGIC = "libcore.io.DiskLruCache";
/*      */   static final String VERSION_1 = "1";
/*      */   static final long ANY_SEQUENCE_NUMBER = -1L;
/*   94 */   static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final String CLEAN = "CLEAN";
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private static final String DIRTY = "DIRTY";
/*      */ 
/*      */ 
/*      */   
/*      */   private static final String REMOVE = "REMOVE";
/*      */ 
/*      */ 
/*      */   
/*      */   private static final String READ = "READ";
/*      */ 
/*      */ 
/*      */   
/*      */   final FileSystem fileSystem;
/*      */ 
/*      */ 
/*      */   
/*      */   final File directory;
/*      */ 
/*      */ 
/*      */   
/*      */   private final File journalFile;
/*      */ 
/*      */ 
/*      */   
/*      */   private final File journalFileTmp;
/*      */ 
/*      */ 
/*      */   
/*      */   private final File journalFileBackup;
/*      */ 
/*      */ 
/*      */   
/*      */   private final int appVersion;
/*      */ 
/*      */ 
/*      */   
/*      */   private long maxSize;
/*      */ 
/*      */ 
/*      */   
/*      */   final int valueCount;
/*      */ 
/*      */ 
/*      */   
/*  148 */   private long size = 0L;
/*      */   BufferedSink journalWriter;
/*  150 */   final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<>(0, 0.75F, true);
/*      */ 
/*      */   
/*      */   int redundantOpCount;
/*      */   
/*      */   boolean hasJournalErrors;
/*      */   
/*      */   boolean initialized;
/*      */   
/*      */   boolean closed;
/*      */   
/*      */   boolean mostRecentTrimFailed;
/*      */   
/*      */   boolean mostRecentRebuildFailed;
/*      */   
/*  165 */   private long nextSequenceNumber = 0L;
/*      */   
/*      */   private final Executor executor;
/*      */   
/*  169 */   private final Runnable cleanupRunnable = new Runnable() {
/*      */       public void run() {
/*  171 */         synchronized (DiskLruCache.this) {
/*  172 */           if (((!DiskLruCache.this.initialized ? 1 : 0) | DiskLruCache.this.closed) != 0) {
/*      */             return;
/*      */           }
/*      */           
/*      */           try {
/*  177 */             DiskLruCache.this.trimToSize();
/*  178 */           } catch (IOException ignored) {
/*  179 */             DiskLruCache.this.mostRecentTrimFailed = true;
/*      */           } 
/*      */           
/*      */           try {
/*  183 */             if (DiskLruCache.this.journalRebuildRequired()) {
/*  184 */               DiskLruCache.this.rebuildJournal();
/*  185 */               DiskLruCache.this.redundantOpCount = 0;
/*      */             } 
/*  187 */           } catch (IOException e) {
/*  188 */             DiskLruCache.this.mostRecentRebuildFailed = true;
/*  189 */             DiskLruCache.this.journalWriter = Okio.buffer(Okio.blackhole());
/*      */           } 
/*      */         } 
/*      */       }
/*      */     };
/*      */ 
/*      */   
/*      */   DiskLruCache(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize, Executor executor) {
/*  197 */     this.fileSystem = fileSystem;
/*  198 */     this.directory = directory;
/*  199 */     this.appVersion = appVersion;
/*  200 */     this.journalFile = new File(directory, "journal");
/*  201 */     this.journalFileTmp = new File(directory, "journal.tmp");
/*  202 */     this.journalFileBackup = new File(directory, "journal.bkp");
/*  203 */     this.valueCount = valueCount;
/*  204 */     this.maxSize = maxSize;
/*  205 */     this.executor = executor;
/*      */   }
/*      */   
/*      */   public synchronized void initialize() throws IOException {
/*  209 */     assert Thread.holdsLock(this);
/*      */     
/*  211 */     if (this.initialized) {
/*      */       return;
/*      */     }
/*      */ 
/*      */     
/*  216 */     if (this.fileSystem.exists(this.journalFileBackup))
/*      */     {
/*  218 */       if (this.fileSystem.exists(this.journalFile)) {
/*  219 */         this.fileSystem.delete(this.journalFileBackup);
/*      */       } else {
/*  221 */         this.fileSystem.rename(this.journalFileBackup, this.journalFile);
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*  226 */     if (this.fileSystem.exists(this.journalFile)) {
/*      */       try {
/*  228 */         readJournal();
/*  229 */         processJournal();
/*  230 */         this.initialized = true;
/*      */         return;
/*  232 */       } catch (IOException journalIsCorrupt) {
/*  233 */         Platform.get().log(5, "DiskLruCache " + this.directory + " is corrupt: " + journalIsCorrupt
/*  234 */             .getMessage() + ", removing", journalIsCorrupt);
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         try {
/*  240 */           delete();
/*      */         } finally {
/*  242 */           this.closed = false;
/*      */         } 
/*      */       } 
/*      */     }
/*  246 */     rebuildJournal();
/*      */     
/*  248 */     this.initialized = true;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static DiskLruCache create(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize) {
/*  261 */     if (maxSize <= 0L) {
/*  262 */       throw new IllegalArgumentException("maxSize <= 0");
/*      */     }
/*  264 */     if (valueCount <= 0) {
/*  265 */       throw new IllegalArgumentException("valueCount <= 0");
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*  270 */     Executor executor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Util.threadFactory("OkHttp DiskLruCache", true));
/*      */     
/*  272 */     return new DiskLruCache(fileSystem, directory, appVersion, valueCount, maxSize, executor);
/*      */   }
/*      */   
/*      */   private void readJournal() throws IOException {
/*  276 */     BufferedSource source = Okio.buffer(this.fileSystem.source(this.journalFile));
/*      */     try {
/*  278 */       String magic = source.readUtf8LineStrict();
/*  279 */       String version = source.readUtf8LineStrict();
/*  280 */       String appVersionString = source.readUtf8LineStrict();
/*  281 */       String valueCountString = source.readUtf8LineStrict();
/*  282 */       String blank = source.readUtf8LineStrict();
/*  283 */       if (!"libcore.io.DiskLruCache".equals(magic) || 
/*  284 */         !"1".equals(version) || 
/*  285 */         !Integer.toString(this.appVersion).equals(appVersionString) || 
/*  286 */         !Integer.toString(this.valueCount).equals(valueCountString) || 
/*  287 */         !"".equals(blank)) {
/*  288 */         throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
/*      */       }
/*      */ 
/*      */       
/*  292 */       int lineCount = 0;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     }
/*      */     finally {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  310 */       Util.closeQuietly((Closeable)source);
/*      */     } 
/*      */   }
/*      */   
/*      */   private BufferedSink newJournalWriter() throws FileNotFoundException {
/*  315 */     Sink fileSink = this.fileSystem.appendingSink(this.journalFile);
/*  316 */     FaultHidingSink faultHidingSink = new FaultHidingSink(fileSink) {
/*      */         protected void onException(IOException e) {
/*  318 */           assert Thread.holdsLock(DiskLruCache.this);
/*  319 */           DiskLruCache.this.hasJournalErrors = true;
/*      */         }
/*      */       };
/*  322 */     return Okio.buffer((Sink)faultHidingSink);
/*      */   }
/*      */   private void readJournalLine(String line) throws IOException {
/*      */     String key;
/*  326 */     int firstSpace = line.indexOf(' ');
/*  327 */     if (firstSpace == -1) {
/*  328 */       throw new IOException("unexpected journal line: " + line);
/*      */     }
/*      */     
/*  331 */     int keyBegin = firstSpace + 1;
/*  332 */     int secondSpace = line.indexOf(' ', keyBegin);
/*      */     
/*  334 */     if (secondSpace == -1) {
/*  335 */       key = line.substring(keyBegin);
/*  336 */       if (firstSpace == "REMOVE".length() && line.startsWith("REMOVE")) {
/*  337 */         this.lruEntries.remove(key);
/*      */         return;
/*      */       } 
/*      */     } else {
/*  341 */       key = line.substring(keyBegin, secondSpace);
/*      */     } 
/*      */     
/*  344 */     Entry entry = this.lruEntries.get(key);
/*  345 */     if (entry == null) {
/*  346 */       entry = new Entry(key);
/*  347 */       this.lruEntries.put(key, entry);
/*      */     } 
/*      */     
/*  350 */     if (secondSpace != -1 && firstSpace == "CLEAN".length() && line.startsWith("CLEAN")) {
/*  351 */       String[] parts = line.substring(secondSpace + 1).split(" ");
/*  352 */       entry.readable = true;
/*  353 */       entry.currentEditor = null;
/*  354 */       entry.setLengths(parts);
/*  355 */     } else if (secondSpace == -1 && firstSpace == "DIRTY".length() && line.startsWith("DIRTY")) {
/*  356 */       entry.currentEditor = new Editor(entry);
/*  357 */     } else if (secondSpace != -1 || firstSpace != "READ".length() || !line.startsWith("READ")) {
/*      */ 
/*      */       
/*  360 */       throw new IOException("unexpected journal line: " + line);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void processJournal() throws IOException {
/*  369 */     this.fileSystem.delete(this.journalFileTmp);
/*  370 */     for (Iterator<Entry> i = this.lruEntries.values().iterator(); i.hasNext(); ) {
/*  371 */       Entry entry = i.next();
/*  372 */       if (entry.currentEditor == null) {
/*  373 */         for (int j = 0; j < this.valueCount; j++)
/*  374 */           this.size += entry.lengths[j]; 
/*      */         continue;
/*      */       } 
/*  377 */       entry.currentEditor = null;
/*  378 */       for (int t = 0; t < this.valueCount; t++) {
/*  379 */         this.fileSystem.delete(entry.cleanFiles[t]);
/*  380 */         this.fileSystem.delete(entry.dirtyFiles[t]);
/*      */       } 
/*  382 */       i.remove();
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   synchronized void rebuildJournal() throws IOException {
/*  392 */     if (this.journalWriter != null) {
/*  393 */       this.journalWriter.close();
/*      */     }
/*      */     
/*  396 */     BufferedSink writer = Okio.buffer(this.fileSystem.sink(this.journalFileTmp));
/*      */     try {
/*  398 */       writer.writeUtf8("libcore.io.DiskLruCache").writeByte(10);
/*  399 */       writer.writeUtf8("1").writeByte(10);
/*  400 */       writer.writeDecimalLong(this.appVersion).writeByte(10);
/*  401 */       writer.writeDecimalLong(this.valueCount).writeByte(10);
/*  402 */       writer.writeByte(10);
/*      */       
/*  404 */       for (Entry entry : this.lruEntries.values()) {
/*  405 */         if (entry.currentEditor != null) {
/*  406 */           writer.writeUtf8("DIRTY").writeByte(32);
/*  407 */           writer.writeUtf8(entry.key);
/*  408 */           writer.writeByte(10); continue;
/*      */         } 
/*  410 */         writer.writeUtf8("CLEAN").writeByte(32);
/*  411 */         writer.writeUtf8(entry.key);
/*  412 */         entry.writeLengths(writer);
/*  413 */         writer.writeByte(10);
/*      */       } 
/*      */     } finally {
/*      */       
/*  417 */       writer.close();
/*      */     } 
/*      */     
/*  420 */     if (this.fileSystem.exists(this.journalFile)) {
/*  421 */       this.fileSystem.rename(this.journalFile, this.journalFileBackup);
/*      */     }
/*  423 */     this.fileSystem.rename(this.journalFileTmp, this.journalFile);
/*  424 */     this.fileSystem.delete(this.journalFileBackup);
/*      */     
/*  426 */     this.journalWriter = newJournalWriter();
/*  427 */     this.hasJournalErrors = false;
/*  428 */     this.mostRecentRebuildFailed = false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized Snapshot get(String key) throws IOException {
/*  436 */     initialize();
/*      */     
/*  438 */     checkNotClosed();
/*  439 */     validateKey(key);
/*  440 */     Entry entry = this.lruEntries.get(key);
/*  441 */     if (entry == null || !entry.readable) return null;
/*      */     
/*  443 */     Snapshot snapshot = entry.snapshot();
/*  444 */     if (snapshot == null) return null;
/*      */     
/*  446 */     this.redundantOpCount++;
/*  447 */     this.journalWriter.writeUtf8("READ").writeByte(32).writeUtf8(key).writeByte(10);
/*  448 */     if (journalRebuildRequired()) {
/*  449 */       this.executor.execute(this.cleanupRunnable);
/*      */     }
/*      */     
/*  452 */     return snapshot;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public Editor edit(String key) throws IOException {
/*  459 */     return edit(key, -1L);
/*      */   }
/*      */   
/*      */   synchronized Editor edit(String key, long expectedSequenceNumber) throws IOException {
/*  463 */     initialize();
/*      */     
/*  465 */     checkNotClosed();
/*  466 */     validateKey(key);
/*  467 */     Entry entry = this.lruEntries.get(key);
/*  468 */     if (expectedSequenceNumber != -1L && (entry == null || entry.sequenceNumber != expectedSequenceNumber))
/*      */     {
/*  470 */       return null;
/*      */     }
/*  472 */     if (entry != null && entry.currentEditor != null) {
/*  473 */       return null;
/*      */     }
/*  475 */     if (this.mostRecentTrimFailed || this.mostRecentRebuildFailed) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  481 */       this.executor.execute(this.cleanupRunnable);
/*  482 */       return null;
/*      */     } 
/*      */ 
/*      */     
/*  486 */     this.journalWriter.writeUtf8("DIRTY").writeByte(32).writeUtf8(key).writeByte(10);
/*  487 */     this.journalWriter.flush();
/*      */     
/*  489 */     if (this.hasJournalErrors) {
/*  490 */       return null;
/*      */     }
/*      */     
/*  493 */     if (entry == null) {
/*  494 */       entry = new Entry(key);
/*  495 */       this.lruEntries.put(key, entry);
/*      */     } 
/*  497 */     Editor editor = new Editor(entry);
/*  498 */     entry.currentEditor = editor;
/*  499 */     return editor;
/*      */   }
/*      */ 
/*      */   
/*      */   public File getDirectory() {
/*  504 */     return this.directory;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized long getMaxSize() {
/*  511 */     return this.maxSize;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized void setMaxSize(long maxSize) {
/*  519 */     this.maxSize = maxSize;
/*  520 */     if (this.initialized) {
/*  521 */       this.executor.execute(this.cleanupRunnable);
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized long size() throws IOException {
/*  530 */     initialize();
/*  531 */     return this.size;
/*      */   }
/*      */   
/*      */   synchronized void completeEdit(Editor editor, boolean success) throws IOException {
/*  535 */     Entry entry = editor.entry;
/*  536 */     if (entry.currentEditor != editor) {
/*  537 */       throw new IllegalStateException();
/*      */     }
/*      */ 
/*      */     
/*  541 */     if (success && !entry.readable) {
/*  542 */       for (int j = 0; j < this.valueCount; j++) {
/*  543 */         if (!editor.written[j]) {
/*  544 */           editor.abort();
/*  545 */           throw new IllegalStateException("Newly created entry didn't create value for index " + j);
/*      */         } 
/*  547 */         if (!this.fileSystem.exists(entry.dirtyFiles[j])) {
/*  548 */           editor.abort();
/*      */           
/*      */           return;
/*      */         } 
/*      */       } 
/*      */     }
/*  554 */     for (int i = 0; i < this.valueCount; i++) {
/*  555 */       File dirty = entry.dirtyFiles[i];
/*  556 */       if (success) {
/*  557 */         if (this.fileSystem.exists(dirty)) {
/*  558 */           File clean = entry.cleanFiles[i];
/*  559 */           this.fileSystem.rename(dirty, clean);
/*  560 */           long oldLength = entry.lengths[i];
/*  561 */           long newLength = this.fileSystem.size(clean);
/*  562 */           entry.lengths[i] = newLength;
/*  563 */           this.size = this.size - oldLength + newLength;
/*      */         } 
/*      */       } else {
/*  566 */         this.fileSystem.delete(dirty);
/*      */       } 
/*      */     } 
/*      */     
/*  570 */     this.redundantOpCount++;
/*  571 */     entry.currentEditor = null;
/*  572 */     if (entry.readable | success) {
/*  573 */       entry.readable = true;
/*  574 */       this.journalWriter.writeUtf8("CLEAN").writeByte(32);
/*  575 */       this.journalWriter.writeUtf8(entry.key);
/*  576 */       entry.writeLengths(this.journalWriter);
/*  577 */       this.journalWriter.writeByte(10);
/*  578 */       if (success) {
/*  579 */         entry.sequenceNumber = this.nextSequenceNumber++;
/*      */       }
/*      */     } else {
/*  582 */       this.lruEntries.remove(entry.key);
/*  583 */       this.journalWriter.writeUtf8("REMOVE").writeByte(32);
/*  584 */       this.journalWriter.writeUtf8(entry.key);
/*  585 */       this.journalWriter.writeByte(10);
/*      */     } 
/*  587 */     this.journalWriter.flush();
/*      */     
/*  589 */     if (this.size > this.maxSize || journalRebuildRequired()) {
/*  590 */       this.executor.execute(this.cleanupRunnable);
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   boolean journalRebuildRequired() {
/*  599 */     int redundantOpCompactThreshold = 2000;
/*  600 */     return (this.redundantOpCount >= 2000 && this.redundantOpCount >= this.lruEntries
/*  601 */       .size());
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized boolean remove(String key) throws IOException {
/*  611 */     initialize();
/*      */     
/*  613 */     checkNotClosed();
/*  614 */     validateKey(key);
/*  615 */     Entry entry = this.lruEntries.get(key);
/*  616 */     if (entry == null) return false; 
/*  617 */     boolean removed = removeEntry(entry);
/*  618 */     if (removed && this.size <= this.maxSize) this.mostRecentTrimFailed = false; 
/*  619 */     return removed;
/*      */   }
/*      */   
/*      */   boolean removeEntry(Entry entry) throws IOException {
/*  623 */     if (entry.currentEditor != null) {
/*  624 */       entry.currentEditor.detach();
/*      */     }
/*      */     
/*  627 */     for (int i = 0; i < this.valueCount; i++) {
/*  628 */       this.fileSystem.delete(entry.cleanFiles[i]);
/*  629 */       this.size -= entry.lengths[i];
/*  630 */       entry.lengths[i] = 0L;
/*      */     } 
/*      */     
/*  633 */     this.redundantOpCount++;
/*  634 */     this.journalWriter.writeUtf8("REMOVE").writeByte(32).writeUtf8(entry.key).writeByte(10);
/*  635 */     this.lruEntries.remove(entry.key);
/*      */     
/*  637 */     if (journalRebuildRequired()) {
/*  638 */       this.executor.execute(this.cleanupRunnable);
/*      */     }
/*      */     
/*  641 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   public synchronized boolean isClosed() {
/*  646 */     return this.closed;
/*      */   }
/*      */   
/*      */   private synchronized void checkNotClosed() {
/*  650 */     if (isClosed()) {
/*  651 */       throw new IllegalStateException("cache is closed");
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   public synchronized void flush() throws IOException {
/*  657 */     if (!this.initialized)
/*      */       return; 
/*  659 */     checkNotClosed();
/*  660 */     trimToSize();
/*  661 */     this.journalWriter.flush();
/*      */   }
/*      */ 
/*      */   
/*      */   public synchronized void close() throws IOException {
/*  666 */     if (!this.initialized || this.closed) {
/*  667 */       this.closed = true;
/*      */       
/*      */       return;
/*      */     } 
/*  671 */     for (Entry entry : (Entry[])this.lruEntries.values().toArray((Object[])new Entry[this.lruEntries.size()])) {
/*  672 */       if (entry.currentEditor != null) {
/*  673 */         entry.currentEditor.abort();
/*      */       }
/*      */     } 
/*  676 */     trimToSize();
/*  677 */     this.journalWriter.close();
/*  678 */     this.journalWriter = null;
/*  679 */     this.closed = true;
/*      */   }
/*      */   
/*      */   void trimToSize() throws IOException {
/*  683 */     while (this.size > this.maxSize) {
/*  684 */       Entry toEvict = this.lruEntries.values().iterator().next();
/*  685 */       removeEntry(toEvict);
/*      */     } 
/*  687 */     this.mostRecentTrimFailed = false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void delete() throws IOException {
/*  695 */     close();
/*  696 */     this.fileSystem.deleteContents(this.directory);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized void evictAll() throws IOException {
/*  704 */     initialize();
/*      */     
/*  706 */     for (Entry entry : (Entry[])this.lruEntries.values().toArray((Object[])new Entry[this.lruEntries.size()])) {
/*  707 */       removeEntry(entry);
/*      */     }
/*  709 */     this.mostRecentTrimFailed = false;
/*      */   }
/*      */   
/*      */   private void validateKey(String key) {
/*  713 */     Matcher matcher = LEGAL_KEY_PATTERN.matcher(key);
/*  714 */     if (!matcher.matches()) {
/*  715 */       throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,120}: \"" + key + "\"");
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public synchronized Iterator<Snapshot> snapshots() throws IOException {
/*  736 */     initialize();
/*  737 */     return new Iterator<Snapshot>()
/*      */       {
/*  739 */         final Iterator<DiskLruCache.Entry> delegate = (new ArrayList<>(DiskLruCache.this.lruEntries.values())).iterator();
/*      */ 
/*      */         
/*      */         DiskLruCache.Snapshot nextSnapshot;
/*      */         
/*      */         DiskLruCache.Snapshot removeSnapshot;
/*      */ 
/*      */         
/*      */         public boolean hasNext() {
/*  748 */           if (this.nextSnapshot != null) return true;
/*      */           
/*  750 */           synchronized (DiskLruCache.this) {
/*      */             
/*  752 */             if (DiskLruCache.this.closed) return false;
/*      */             
/*  754 */             while (this.delegate.hasNext()) {
/*  755 */               DiskLruCache.Entry entry = this.delegate.next();
/*  756 */               if (!entry.readable)
/*  757 */                 continue;  DiskLruCache.Snapshot snapshot = entry.snapshot();
/*  758 */               if (snapshot == null)
/*  759 */                 continue;  this.nextSnapshot = snapshot;
/*  760 */               return true;
/*      */             } 
/*      */           } 
/*      */           
/*  764 */           return false;
/*      */         }
/*      */         
/*      */         public DiskLruCache.Snapshot next() {
/*  768 */           if (!hasNext()) throw new NoSuchElementException(); 
/*  769 */           this.removeSnapshot = this.nextSnapshot;
/*  770 */           this.nextSnapshot = null;
/*  771 */           return this.removeSnapshot;
/*      */         }
/*      */         
/*      */         public void remove() {
/*  775 */           if (this.removeSnapshot == null) throw new IllegalStateException("remove() before next()"); 
/*      */           try {
/*  777 */             DiskLruCache.this.remove(this.removeSnapshot.key);
/*  778 */           } catch (IOException iOException) {
/*      */ 
/*      */           
/*      */           } finally {
/*  782 */             this.removeSnapshot = null;
/*      */           } 
/*      */         }
/*      */       };
/*      */   }
/*      */   
/*      */   public final class Snapshot
/*      */     implements Closeable {
/*      */     private final String key;
/*      */     private final long sequenceNumber;
/*      */     private final Source[] sources;
/*      */     private final long[] lengths;
/*      */     
/*      */     Snapshot(String key, long sequenceNumber, Source[] sources, long[] lengths) {
/*  796 */       this.key = key;
/*  797 */       this.sequenceNumber = sequenceNumber;
/*  798 */       this.sources = sources;
/*  799 */       this.lengths = lengths;
/*      */     }
/*      */     
/*      */     public String key() {
/*  803 */       return this.key;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     @Nullable
/*      */     public DiskLruCache.Editor edit() throws IOException {
/*  811 */       return DiskLruCache.this.edit(this.key, this.sequenceNumber);
/*      */     }
/*      */ 
/*      */     
/*      */     public Source getSource(int index) {
/*  816 */       return this.sources[index];
/*      */     }
/*      */ 
/*      */     
/*      */     public long getLength(int index) {
/*  821 */       return this.lengths[index];
/*      */     }
/*      */     
/*      */     public void close() {
/*  825 */       for (Source in : this.sources) {
/*  826 */         Util.closeQuietly((Closeable)in);
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   public final class Editor
/*      */   {
/*      */     final DiskLruCache.Entry entry;
/*      */     final boolean[] written;
/*      */     private boolean done;
/*      */     
/*      */     Editor(DiskLruCache.Entry entry) {
/*  838 */       this.entry = entry;
/*  839 */       this.written = entry.readable ? null : new boolean[DiskLruCache.this.valueCount];
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     void detach() {
/*  849 */       if (this.entry.currentEditor == this) {
/*  850 */         for (int i = 0; i < DiskLruCache.this.valueCount; i++) {
/*      */           try {
/*  852 */             DiskLruCache.this.fileSystem.delete(this.entry.dirtyFiles[i]);
/*  853 */           } catch (IOException iOException) {}
/*      */         } 
/*      */ 
/*      */         
/*  857 */         this.entry.currentEditor = null;
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Source newSource(int index) {
/*  866 */       synchronized (DiskLruCache.this) {
/*  867 */         if (this.done) {
/*  868 */           throw new IllegalStateException();
/*      */         }
/*  870 */         if (!this.entry.readable || this.entry.currentEditor != this) {
/*  871 */           return null;
/*      */         }
/*      */         try {
/*  874 */           return DiskLruCache.this.fileSystem.source(this.entry.cleanFiles[index]);
/*  875 */         } catch (FileNotFoundException e) {
/*  876 */           return null;
/*      */         } 
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Sink newSink(int index) {
/*  887 */       synchronized (DiskLruCache.this) {
/*  888 */         Sink sink; if (this.done) {
/*  889 */           throw new IllegalStateException();
/*      */         }
/*  891 */         if (this.entry.currentEditor != this) {
/*  892 */           return Okio.blackhole();
/*      */         }
/*  894 */         if (!this.entry.readable) {
/*  895 */           this.written[index] = true;
/*      */         }
/*  897 */         File dirtyFile = this.entry.dirtyFiles[index];
/*      */         
/*      */         try {
/*  900 */           sink = DiskLruCache.this.fileSystem.sink(dirtyFile);
/*  901 */         } catch (FileNotFoundException e) {
/*  902 */           return Okio.blackhole();
/*      */         } 
/*  904 */         return (Sink)new FaultHidingSink(sink) {
/*      */             protected void onException(IOException e) {
/*  906 */               synchronized (DiskLruCache.this) {
/*  907 */                 DiskLruCache.Editor.this.detach();
/*      */               } 
/*      */             }
/*      */           };
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public void commit() throws IOException {
/*  919 */       synchronized (DiskLruCache.this) {
/*  920 */         if (this.done) {
/*  921 */           throw new IllegalStateException();
/*      */         }
/*  923 */         if (this.entry.currentEditor == this) {
/*  924 */           DiskLruCache.this.completeEdit(this, true);
/*      */         }
/*  926 */         this.done = true;
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public void abort() throws IOException {
/*  935 */       synchronized (DiskLruCache.this) {
/*  936 */         if (this.done) {
/*  937 */           throw new IllegalStateException();
/*      */         }
/*  939 */         if (this.entry.currentEditor == this) {
/*  940 */           DiskLruCache.this.completeEdit(this, false);
/*      */         }
/*  942 */         this.done = true;
/*      */       } 
/*      */     }
/*      */     
/*      */     public void abortUnlessCommitted() {
/*  947 */       synchronized (DiskLruCache.this) {
/*  948 */         if (!this.done && this.entry.currentEditor == this) {
/*      */           try {
/*  950 */             DiskLruCache.this.completeEdit(this, false);
/*  951 */           } catch (IOException iOException) {}
/*      */         }
/*      */       } 
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   private final class Entry
/*      */   {
/*      */     final String key;
/*      */     
/*      */     final long[] lengths;
/*      */     
/*      */     final File[] cleanFiles;
/*      */     
/*      */     final File[] dirtyFiles;
/*      */     
/*      */     boolean readable;
/*      */     
/*      */     DiskLruCache.Editor currentEditor;
/*      */     
/*      */     long sequenceNumber;
/*      */ 
/*      */     
/*      */     Entry(String key) {
/*  976 */       this.key = key;
/*      */       
/*  978 */       this.lengths = new long[DiskLruCache.this.valueCount];
/*  979 */       this.cleanFiles = new File[DiskLruCache.this.valueCount];
/*  980 */       this.dirtyFiles = new File[DiskLruCache.this.valueCount];
/*      */ 
/*      */       
/*  983 */       StringBuilder fileBuilder = (new StringBuilder(key)).append('.');
/*  984 */       int truncateTo = fileBuilder.length();
/*  985 */       for (int i = 0; i < DiskLruCache.this.valueCount; i++) {
/*  986 */         fileBuilder.append(i);
/*  987 */         this.cleanFiles[i] = new File(DiskLruCache.this.directory, fileBuilder.toString());
/*  988 */         fileBuilder.append(".tmp");
/*  989 */         this.dirtyFiles[i] = new File(DiskLruCache.this.directory, fileBuilder.toString());
/*  990 */         fileBuilder.setLength(truncateTo);
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     void setLengths(String[] strings) throws IOException {
/*  996 */       if (strings.length != DiskLruCache.this.valueCount) {
/*  997 */         throw invalidLengths(strings);
/*      */       }
/*      */       
/*      */       try {
/* 1001 */         for (int i = 0; i < strings.length; i++) {
/* 1002 */           this.lengths[i] = Long.parseLong(strings[i]);
/*      */         }
/* 1004 */       } catch (NumberFormatException e) {
/* 1005 */         throw invalidLengths(strings);
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     void writeLengths(BufferedSink writer) throws IOException {
/* 1011 */       for (long length : this.lengths) {
/* 1012 */         writer.writeByte(32).writeDecimalLong(length);
/*      */       }
/*      */     }
/*      */     
/*      */     private IOException invalidLengths(String[] strings) throws IOException {
/* 1017 */       throw new IOException("unexpected journal line: " + Arrays.toString(strings));
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     DiskLruCache.Snapshot snapshot() {
/* 1026 */       if (!Thread.holdsLock(DiskLruCache.this)) throw new AssertionError();
/*      */       
/* 1028 */       Source[] sources = new Source[DiskLruCache.this.valueCount];
/* 1029 */       long[] lengths = (long[])this.lengths.clone();
/*      */       try {
/* 1031 */         for (int i = 0; i < DiskLruCache.this.valueCount; i++) {
/* 1032 */           sources[i] = DiskLruCache.this.fileSystem.source(this.cleanFiles[i]);
/*      */         }
/* 1034 */         return new DiskLruCache.Snapshot(this.key, this.sequenceNumber, sources, lengths);
/* 1035 */       } catch (FileNotFoundException e) {
/*      */         
/* 1037 */         for (int i = 0; i < DiskLruCache.this.valueCount && 
/* 1038 */           sources[i] != null; i++) {
/* 1039 */           Util.closeQuietly((Closeable)sources[i]);
/*      */         }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         try {
/* 1047 */           DiskLruCache.this.removeEntry(this);
/* 1048 */         } catch (IOException iOException) {}
/*      */         
/* 1050 */         return null;
/*      */       } 
/*      */     }
/*      */   }
/*      */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\cache\DiskLruCache.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */