/*     */ package okhttp3.internal.publicsuffix;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InterruptedIOException;
/*     */ import java.net.IDN;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.atomic.AtomicBoolean;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.platform.Platform;
/*     */ import okio.BufferedSource;
/*     */ import okio.GzipSource;
/*     */ import okio.Okio;
/*     */ import okio.Source;
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
/*     */ public final class PublicSuffixDatabase
/*     */ {
/*     */   public static final String PUBLIC_SUFFIX_RESOURCE = "publicsuffixes.gz";
/*  39 */   private static final byte[] WILDCARD_LABEL = new byte[] { 42 };
/*  40 */   private static final String[] EMPTY_RULE = new String[0];
/*  41 */   private static final String[] PREVAILING_RULE = new String[] { "*" };
/*     */   
/*     */   private static final byte EXCEPTION_MARKER = 33;
/*     */   
/*  45 */   private static final PublicSuffixDatabase instance = new PublicSuffixDatabase();
/*     */ 
/*     */   
/*  48 */   private final AtomicBoolean listRead = new AtomicBoolean(false);
/*     */ 
/*     */   
/*  51 */   private final CountDownLatch readCompleteLatch = new CountDownLatch(1);
/*     */ 
/*     */   
/*     */   private byte[] publicSuffixListBytes;
/*     */ 
/*     */   
/*     */   private byte[] publicSuffixExceptionListBytes;
/*     */ 
/*     */   
/*     */   public static PublicSuffixDatabase get() {
/*  61 */     return instance;
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
/*     */   public String getEffectiveTldPlusOne(String domain) {
/*     */     int firstLabelOffset;
/*  80 */     if (domain == null) throw new NullPointerException("domain == null");
/*     */ 
/*     */     
/*  83 */     String unicodeDomain = IDN.toUnicode(domain);
/*  84 */     String[] domainLabels = unicodeDomain.split("\\.");
/*  85 */     String[] rule = findMatchingRule(domainLabels);
/*  86 */     if (domainLabels.length == rule.length && rule[0].charAt(0) != '!')
/*     */     {
/*  88 */       return null;
/*     */     }
/*     */ 
/*     */     
/*  92 */     if (rule[0].charAt(0) == '!') {
/*     */       
/*  94 */       firstLabelOffset = domainLabels.length - rule.length;
/*     */     } else {
/*     */       
/*  97 */       firstLabelOffset = domainLabels.length - rule.length + 1;
/*     */     } 
/*     */     
/* 100 */     StringBuilder effectiveTldPlusOne = new StringBuilder();
/* 101 */     String[] punycodeLabels = domain.split("\\.");
/* 102 */     for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
/* 103 */       effectiveTldPlusOne.append(punycodeLabels[i]).append('.');
/*     */     }
/* 105 */     effectiveTldPlusOne.deleteCharAt(effectiveTldPlusOne.length() - 1);
/*     */     
/* 107 */     return effectiveTldPlusOne.toString();
/*     */   }
/*     */   
/*     */   private String[] findMatchingRule(String[] domainLabels) {
/* 111 */     if (!this.listRead.get() && this.listRead.compareAndSet(false, true)) {
/* 112 */       readTheListUninterruptibly();
/*     */     } else {
/*     */       try {
/* 115 */         this.readCompleteLatch.await();
/* 116 */       } catch (InterruptedException ignored) {
/* 117 */         Thread.currentThread().interrupt();
/*     */       } 
/*     */     } 
/*     */     
/* 121 */     synchronized (this) {
/* 122 */       if (this.publicSuffixListBytes == null) {
/* 123 */         throw new IllegalStateException("Unable to load publicsuffixes.gz resource from the classpath.");
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 129 */     byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length][];
/* 130 */     for (int i = 0; i < domainLabels.length; i++) {
/* 131 */       domainLabelsUtf8Bytes[i] = domainLabels[i].getBytes(Util.UTF_8);
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 136 */     String exactMatch = null;
/* 137 */     for (int j = 0; j < domainLabelsUtf8Bytes.length; j++) {
/* 138 */       String rule = binarySearchBytes(this.publicSuffixListBytes, domainLabelsUtf8Bytes, j);
/* 139 */       if (rule != null) {
/* 140 */         exactMatch = rule;
/*     */ 
/*     */ 
/*     */         
/*     */         break;
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 150 */     String wildcardMatch = null;
/* 151 */     if (domainLabelsUtf8Bytes.length > 1) {
/* 152 */       byte[][] labelsWithWildcard = (byte[][])domainLabelsUtf8Bytes.clone();
/* 153 */       for (int labelIndex = 0; labelIndex < labelsWithWildcard.length - 1; labelIndex++) {
/* 154 */         labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
/* 155 */         String rule = binarySearchBytes(this.publicSuffixListBytes, labelsWithWildcard, labelIndex);
/* 156 */         if (rule != null) {
/* 157 */           wildcardMatch = rule;
/*     */           
/*     */           break;
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 164 */     String exception = null;
/* 165 */     if (wildcardMatch != null) {
/* 166 */       for (int labelIndex = 0; labelIndex < domainLabelsUtf8Bytes.length - 1; labelIndex++) {
/* 167 */         String rule = binarySearchBytes(this.publicSuffixExceptionListBytes, domainLabelsUtf8Bytes, labelIndex);
/*     */         
/* 169 */         if (rule != null) {
/* 170 */           exception = rule;
/*     */           
/*     */           break;
/*     */         } 
/*     */       } 
/*     */     }
/* 176 */     if (exception != null) {
/*     */       
/* 178 */       exception = "!" + exception;
/* 179 */       return exception.split("\\.");
/* 180 */     }  if (exactMatch == null && wildcardMatch == null) {
/* 181 */       return PREVAILING_RULE;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 186 */     String[] exactRuleLabels = (exactMatch != null) ? exactMatch.split("\\.") : EMPTY_RULE;
/*     */ 
/*     */ 
/*     */     
/* 190 */     String[] wildcardRuleLabels = (wildcardMatch != null) ? wildcardMatch.split("\\.") : EMPTY_RULE;
/*     */     
/* 192 */     return (exactRuleLabels.length > wildcardRuleLabels.length) ? 
/* 193 */       exactRuleLabels : 
/* 194 */       wildcardRuleLabels;
/*     */   }
/*     */   
/*     */   private static String binarySearchBytes(byte[] bytesToSearch, byte[][] labels, int labelIndex) {
/* 198 */     int low = 0;
/* 199 */     int high = bytesToSearch.length;
/* 200 */     String match = null;
/* 201 */     while (low < high) {
/* 202 */       int compareResult, mid = (low + high) / 2;
/*     */ 
/*     */       
/* 205 */       while (mid > -1 && bytesToSearch[mid] != 10) {
/* 206 */         mid--;
/*     */       }
/* 208 */       mid++;
/*     */ 
/*     */       
/* 211 */       int end = 1;
/* 212 */       while (bytesToSearch[mid + end] != 10) {
/* 213 */         end++;
/*     */       }
/* 215 */       int publicSuffixLength = mid + end - mid;
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 220 */       int currentLabelIndex = labelIndex;
/* 221 */       int currentLabelByteIndex = 0;
/* 222 */       int publicSuffixByteIndex = 0;
/*     */       
/* 224 */       boolean expectDot = false;
/*     */       while (true) {
/*     */         int byte0;
/* 227 */         if (expectDot) {
/* 228 */           byte0 = 46;
/* 229 */           expectDot = false;
/*     */         } else {
/* 231 */           byte0 = labels[currentLabelIndex][currentLabelByteIndex] & 0xFF;
/*     */         } 
/*     */         
/* 234 */         int byte1 = bytesToSearch[mid + publicSuffixByteIndex] & 0xFF;
/*     */         
/* 236 */         compareResult = byte0 - byte1;
/* 237 */         if (compareResult != 0)
/*     */           break; 
/* 239 */         publicSuffixByteIndex++;
/* 240 */         currentLabelByteIndex++;
/* 241 */         if (publicSuffixByteIndex == publicSuffixLength)
/*     */           break; 
/* 243 */         if ((labels[currentLabelIndex]).length == currentLabelByteIndex) {
/*     */ 
/*     */           
/* 246 */           if (currentLabelIndex == labels.length - 1) {
/*     */             break;
/*     */           }
/* 249 */           currentLabelIndex++;
/* 250 */           currentLabelByteIndex = -1;
/* 251 */           expectDot = true;
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 256 */       if (compareResult < 0) {
/* 257 */         high = mid - 1; continue;
/* 258 */       }  if (compareResult > 0) {
/* 259 */         low = mid + end + 1;
/*     */         continue;
/*     */       } 
/* 262 */       int publicSuffixBytesLeft = publicSuffixLength - publicSuffixByteIndex;
/* 263 */       int labelBytesLeft = (labels[currentLabelIndex]).length - currentLabelByteIndex;
/* 264 */       for (int i = currentLabelIndex + 1; i < labels.length; i++) {
/* 265 */         labelBytesLeft += (labels[i]).length;
/*     */       }
/*     */       
/* 268 */       if (labelBytesLeft < publicSuffixBytesLeft) {
/* 269 */         high = mid - 1; continue;
/* 270 */       }  if (labelBytesLeft > publicSuffixBytesLeft) {
/* 271 */         low = mid + end + 1;
/*     */         continue;
/*     */       } 
/* 274 */       match = new String(bytesToSearch, mid, publicSuffixLength, Util.UTF_8);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 279 */     return match;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void readTheListUninterruptibly() {
/* 288 */     boolean interrupted = false;
/*     */     
/*     */     while (true) {
/*     */       try {
/* 292 */         readTheList();
/*     */         return;
/* 294 */       } catch (InterruptedIOException e) {
/* 295 */         Thread.interrupted();
/*     */       }
/* 297 */       catch (IOException e) {
/* 298 */         Platform.get().log(5, "Failed to read public suffix list", e);
/*     */ 
/*     */         
/*     */         return;
/*     */       } finally {
/* 303 */         if (interrupted) {
/* 304 */           Thread.currentThread().interrupt();
/*     */         }
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void readTheList() throws IOException {
/*     */     byte[] publicSuffixListBytes, publicSuffixExceptionListBytes;
/* 313 */     InputStream resource = PublicSuffixDatabase.class.getResourceAsStream("publicsuffixes.gz");
/* 314 */     if (resource == null)
/*     */       return; 
/* 316 */     BufferedSource bufferedSource = Okio.buffer((Source)new GzipSource(Okio.source(resource)));
/*     */     try {
/* 318 */       int totalBytes = bufferedSource.readInt();
/* 319 */       publicSuffixListBytes = new byte[totalBytes];
/* 320 */       bufferedSource.readFully(publicSuffixListBytes);
/*     */       
/* 322 */       int totalExceptionBytes = bufferedSource.readInt();
/* 323 */       publicSuffixExceptionListBytes = new byte[totalExceptionBytes];
/* 324 */       bufferedSource.readFully(publicSuffixExceptionListBytes);
/*     */     } finally {
/* 326 */       Util.closeQuietly((Closeable)bufferedSource);
/*     */     } 
/*     */     
/* 329 */     synchronized (this) {
/* 330 */       this.publicSuffixListBytes = publicSuffixListBytes;
/* 331 */       this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
/*     */     } 
/*     */     
/* 334 */     this.readCompleteLatch.countDown();
/*     */   }
/*     */ 
/*     */   
/*     */   void setListBytes(byte[] publicSuffixListBytes, byte[] publicSuffixExceptionListBytes) {
/* 339 */     this.publicSuffixListBytes = publicSuffixListBytes;
/* 340 */     this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
/* 341 */     this.listRead.set(true);
/* 342 */     this.readCompleteLatch.countDown();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\publicsuffix\PublicSuffixDatabase.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */