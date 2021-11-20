/*     */ package okhttp3.internal.cache2;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
/*     */ import okio.ByteString;
/*     */ import okio.Source;
/*     */ import okio.Timeout;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ final class Relay
/*     */ {
/*     */   private static final int SOURCE_UPSTREAM = 1;
/*     */   private static final int SOURCE_FILE = 2;
/*  45 */   static final ByteString PREFIX_CLEAN = ByteString.encodeUtf8("OkHttp cache v1\n");
/*  46 */   static final ByteString PREFIX_DIRTY = ByteString.encodeUtf8("OkHttp DIRTY :(\n");
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static final long FILE_HEADER_SIZE = 32L;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   RandomAccessFile file;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   Thread upstreamReader;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   Source upstream;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  79 */   final Buffer upstreamBuffer = new Buffer();
/*     */ 
/*     */ 
/*     */   
/*     */   long upstreamPos;
/*     */ 
/*     */ 
/*     */   
/*     */   boolean complete;
/*     */ 
/*     */ 
/*     */   
/*     */   private final ByteString metadata;
/*     */ 
/*     */   
/*  94 */   final Buffer buffer = new Buffer();
/*     */ 
/*     */ 
/*     */   
/*     */   final long bufferMaxSize;
/*     */ 
/*     */ 
/*     */   
/*     */   int sourceCount;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Relay(RandomAccessFile file, Source upstream, long upstreamPos, ByteString metadata, long bufferMaxSize) {
/* 108 */     this.file = file;
/* 109 */     this.upstream = upstream;
/* 110 */     this.complete = (upstream == null);
/* 111 */     this.upstreamPos = upstreamPos;
/* 112 */     this.metadata = metadata;
/* 113 */     this.bufferMaxSize = bufferMaxSize;
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
/*     */   public static Relay edit(File file, Source upstream, ByteString metadata, long bufferMaxSize) throws IOException {
/* 126 */     RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
/* 127 */     Relay result = new Relay(randomAccessFile, upstream, 0L, metadata, bufferMaxSize);
/*     */ 
/*     */     
/* 130 */     randomAccessFile.setLength(0L);
/* 131 */     result.writeHeader(PREFIX_DIRTY, -1L, -1L);
/*     */     
/* 133 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Relay read(File file) throws IOException {
/* 144 */     RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
/* 145 */     FileOperator fileOperator = new FileOperator(randomAccessFile.getChannel());
/*     */ 
/*     */     
/* 148 */     Buffer header = new Buffer();
/* 149 */     fileOperator.read(0L, header, 32L);
/* 150 */     ByteString prefix = header.readByteString(PREFIX_CLEAN.size());
/* 151 */     if (!prefix.equals(PREFIX_CLEAN)) throw new IOException("unreadable cache file"); 
/* 152 */     long upstreamSize = header.readLong();
/* 153 */     long metadataSize = header.readLong();
/*     */ 
/*     */     
/* 156 */     Buffer metadataBuffer = new Buffer();
/* 157 */     fileOperator.read(32L + upstreamSize, metadataBuffer, metadataSize);
/* 158 */     ByteString metadata = metadataBuffer.readByteString();
/*     */ 
/*     */     
/* 161 */     return new Relay(randomAccessFile, null, upstreamSize, metadata, 0L);
/*     */   }
/*     */ 
/*     */   
/*     */   private void writeHeader(ByteString prefix, long upstreamSize, long metadataSize) throws IOException {
/* 166 */     Buffer header = new Buffer();
/* 167 */     header.write(prefix);
/* 168 */     header.writeLong(upstreamSize);
/* 169 */     header.writeLong(metadataSize);
/* 170 */     if (header.size() != 32L) throw new IllegalArgumentException();
/*     */     
/* 172 */     FileOperator fileOperator = new FileOperator(this.file.getChannel());
/* 173 */     fileOperator.write(0L, header, 32L);
/*     */   }
/*     */   
/*     */   private void writeMetadata(long upstreamSize) throws IOException {
/* 177 */     Buffer metadataBuffer = new Buffer();
/* 178 */     metadataBuffer.write(this.metadata);
/*     */     
/* 180 */     FileOperator fileOperator = new FileOperator(this.file.getChannel());
/* 181 */     fileOperator.write(32L + upstreamSize, metadataBuffer, this.metadata.size());
/*     */   }
/*     */ 
/*     */   
/*     */   void commit(long upstreamSize) throws IOException {
/* 186 */     writeMetadata(upstreamSize);
/* 187 */     this.file.getChannel().force(false);
/*     */ 
/*     */     
/* 190 */     writeHeader(PREFIX_CLEAN, upstreamSize, this.metadata.size());
/* 191 */     this.file.getChannel().force(false);
/*     */ 
/*     */     
/* 194 */     synchronized (this) {
/* 195 */       this.complete = true;
/*     */     } 
/*     */     
/* 198 */     Util.closeQuietly((Closeable)this.upstream);
/* 199 */     this.upstream = null;
/*     */   }
/*     */   
/*     */   boolean isClosed() {
/* 203 */     return (this.file == null);
/*     */   }
/*     */   
/*     */   public ByteString metadata() {
/* 207 */     return this.metadata;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Source newSource() {
/* 216 */     synchronized (this) {
/* 217 */       if (this.file == null) return null; 
/* 218 */       this.sourceCount++;
/*     */     } 
/*     */     
/* 221 */     return new RelaySource();
/*     */   }
/*     */   
/*     */   class RelaySource implements Source {
/* 225 */     private final Timeout timeout = new Timeout();
/*     */ 
/*     */     
/* 228 */     private FileOperator fileOperator = new FileOperator(Relay.this.file.getChannel());
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private long sourcePos;
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
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/*     */       // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: getfield fileOperator : Lokhttp3/internal/cache2/FileOperator;
/*     */       //   4: ifnonnull -> 17
/*     */       //   7: new java/lang/IllegalStateException
/*     */       //   10: dup
/*     */       //   11: ldc 'closed'
/*     */       //   13: invokespecial <init> : (Ljava/lang/String;)V
/*     */       //   16: athrow
/*     */       //   17: aload_0
/*     */       //   18: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   21: dup
/*     */       //   22: astore #4
/*     */       //   24: monitorenter
/*     */       //   25: aload_0
/*     */       //   26: getfield sourcePos : J
/*     */       //   29: aload_0
/*     */       //   30: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   33: getfield upstreamPos : J
/*     */       //   36: dup2
/*     */       //   37: lstore #5
/*     */       //   39: lcmp
/*     */       //   40: ifne -> 103
/*     */       //   43: aload_0
/*     */       //   44: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   47: getfield complete : Z
/*     */       //   50: ifeq -> 60
/*     */       //   53: ldc2_w -1
/*     */       //   56: aload #4
/*     */       //   58: monitorexit
/*     */       //   59: lreturn
/*     */       //   60: aload_0
/*     */       //   61: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   64: getfield upstreamReader : Ljava/lang/Thread;
/*     */       //   67: ifnull -> 84
/*     */       //   70: aload_0
/*     */       //   71: getfield timeout : Lokio/Timeout;
/*     */       //   74: aload_0
/*     */       //   75: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   78: invokevirtual waitUntilNotified : (Ljava/lang/Object;)V
/*     */       //   81: goto -> 25
/*     */       //   84: aload_0
/*     */       //   85: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   88: invokestatic currentThread : ()Ljava/lang/Thread;
/*     */       //   91: putfield upstreamReader : Ljava/lang/Thread;
/*     */       //   94: iconst_1
/*     */       //   95: istore #7
/*     */       //   97: aload #4
/*     */       //   99: monitorexit
/*     */       //   100: goto -> 196
/*     */       //   103: lload #5
/*     */       //   105: aload_0
/*     */       //   106: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   109: getfield buffer : Lokio/Buffer;
/*     */       //   112: invokevirtual size : ()J
/*     */       //   115: lsub
/*     */       //   116: lstore #8
/*     */       //   118: aload_0
/*     */       //   119: getfield sourcePos : J
/*     */       //   122: lload #8
/*     */       //   124: lcmp
/*     */       //   125: ifge -> 137
/*     */       //   128: iconst_2
/*     */       //   129: istore #7
/*     */       //   131: aload #4
/*     */       //   133: monitorexit
/*     */       //   134: goto -> 196
/*     */       //   137: lload_2
/*     */       //   138: lload #5
/*     */       //   140: aload_0
/*     */       //   141: getfield sourcePos : J
/*     */       //   144: lsub
/*     */       //   145: invokestatic min : (JJ)J
/*     */       //   148: lstore #10
/*     */       //   150: aload_0
/*     */       //   151: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   154: getfield buffer : Lokio/Buffer;
/*     */       //   157: aload_1
/*     */       //   158: aload_0
/*     */       //   159: getfield sourcePos : J
/*     */       //   162: lload #8
/*     */       //   164: lsub
/*     */       //   165: lload #10
/*     */       //   167: invokevirtual copyTo : (Lokio/Buffer;JJ)Lokio/Buffer;
/*     */       //   170: pop
/*     */       //   171: aload_0
/*     */       //   172: dup
/*     */       //   173: getfield sourcePos : J
/*     */       //   176: lload #10
/*     */       //   178: ladd
/*     */       //   179: putfield sourcePos : J
/*     */       //   182: lload #10
/*     */       //   184: aload #4
/*     */       //   186: monitorexit
/*     */       //   187: lreturn
/*     */       //   188: astore #12
/*     */       //   190: aload #4
/*     */       //   192: monitorexit
/*     */       //   193: aload #12
/*     */       //   195: athrow
/*     */       //   196: iload #7
/*     */       //   198: iconst_2
/*     */       //   199: if_icmpne -> 247
/*     */       //   202: lload_2
/*     */       //   203: lload #5
/*     */       //   205: aload_0
/*     */       //   206: getfield sourcePos : J
/*     */       //   209: lsub
/*     */       //   210: invokestatic min : (JJ)J
/*     */       //   213: lstore #13
/*     */       //   215: aload_0
/*     */       //   216: getfield fileOperator : Lokhttp3/internal/cache2/FileOperator;
/*     */       //   219: ldc2_w 32
/*     */       //   222: aload_0
/*     */       //   223: getfield sourcePos : J
/*     */       //   226: ladd
/*     */       //   227: aload_1
/*     */       //   228: lload #13
/*     */       //   230: invokevirtual read : (JLokio/Buffer;J)V
/*     */       //   233: aload_0
/*     */       //   234: dup
/*     */       //   235: getfield sourcePos : J
/*     */       //   238: lload #13
/*     */       //   240: ladd
/*     */       //   241: putfield sourcePos : J
/*     */       //   244: lload #13
/*     */       //   246: lreturn
/*     */       //   247: aload_0
/*     */       //   248: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   251: getfield upstream : Lokio/Source;
/*     */       //   254: aload_0
/*     */       //   255: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   258: getfield upstreamBuffer : Lokio/Buffer;
/*     */       //   261: aload_0
/*     */       //   262: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   265: getfield bufferMaxSize : J
/*     */       //   268: invokeinterface read : (Lokio/Buffer;J)J
/*     */       //   273: lstore #13
/*     */       //   275: lload #13
/*     */       //   277: ldc2_w -1
/*     */       //   280: lcmp
/*     */       //   281: ifne -> 338
/*     */       //   284: aload_0
/*     */       //   285: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   288: lload #5
/*     */       //   290: invokevirtual commit : (J)V
/*     */       //   293: ldc2_w -1
/*     */       //   296: lstore #15
/*     */       //   298: aload_0
/*     */       //   299: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   302: dup
/*     */       //   303: astore #17
/*     */       //   305: monitorenter
/*     */       //   306: aload_0
/*     */       //   307: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   310: aconst_null
/*     */       //   311: putfield upstreamReader : Ljava/lang/Thread;
/*     */       //   314: aload_0
/*     */       //   315: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   318: invokevirtual notifyAll : ()V
/*     */       //   321: aload #17
/*     */       //   323: monitorexit
/*     */       //   324: goto -> 335
/*     */       //   327: astore #18
/*     */       //   329: aload #17
/*     */       //   331: monitorexit
/*     */       //   332: aload #18
/*     */       //   334: athrow
/*     */       //   335: lload #15
/*     */       //   337: lreturn
/*     */       //   338: lload #13
/*     */       //   340: lload_2
/*     */       //   341: invokestatic min : (JJ)J
/*     */       //   344: lstore #15
/*     */       //   346: aload_0
/*     */       //   347: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   350: getfield upstreamBuffer : Lokio/Buffer;
/*     */       //   353: aload_1
/*     */       //   354: lconst_0
/*     */       //   355: lload #15
/*     */       //   357: invokevirtual copyTo : (Lokio/Buffer;JJ)Lokio/Buffer;
/*     */       //   360: pop
/*     */       //   361: aload_0
/*     */       //   362: dup
/*     */       //   363: getfield sourcePos : J
/*     */       //   366: lload #15
/*     */       //   368: ladd
/*     */       //   369: putfield sourcePos : J
/*     */       //   372: aload_0
/*     */       //   373: getfield fileOperator : Lokhttp3/internal/cache2/FileOperator;
/*     */       //   376: ldc2_w 32
/*     */       //   379: lload #5
/*     */       //   381: ladd
/*     */       //   382: aload_0
/*     */       //   383: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   386: getfield upstreamBuffer : Lokio/Buffer;
/*     */       //   389: invokevirtual clone : ()Lokio/Buffer;
/*     */       //   392: lload #13
/*     */       //   394: invokevirtual write : (JLokio/Buffer;J)V
/*     */       //   397: aload_0
/*     */       //   398: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   401: dup
/*     */       //   402: astore #17
/*     */       //   404: monitorenter
/*     */       //   405: aload_0
/*     */       //   406: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   409: getfield buffer : Lokio/Buffer;
/*     */       //   412: aload_0
/*     */       //   413: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   416: getfield upstreamBuffer : Lokio/Buffer;
/*     */       //   419: lload #13
/*     */       //   421: invokevirtual write : (Lokio/Buffer;J)V
/*     */       //   424: aload_0
/*     */       //   425: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   428: getfield buffer : Lokio/Buffer;
/*     */       //   431: invokevirtual size : ()J
/*     */       //   434: aload_0
/*     */       //   435: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   438: getfield bufferMaxSize : J
/*     */       //   441: lcmp
/*     */       //   442: ifle -> 473
/*     */       //   445: aload_0
/*     */       //   446: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   449: getfield buffer : Lokio/Buffer;
/*     */       //   452: aload_0
/*     */       //   453: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   456: getfield buffer : Lokio/Buffer;
/*     */       //   459: invokevirtual size : ()J
/*     */       //   462: aload_0
/*     */       //   463: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   466: getfield bufferMaxSize : J
/*     */       //   469: lsub
/*     */       //   470: invokevirtual skip : (J)V
/*     */       //   473: aload_0
/*     */       //   474: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   477: dup
/*     */       //   478: getfield upstreamPos : J
/*     */       //   481: lload #13
/*     */       //   483: ladd
/*     */       //   484: putfield upstreamPos : J
/*     */       //   487: aload #17
/*     */       //   489: monitorexit
/*     */       //   490: goto -> 501
/*     */       //   493: astore #19
/*     */       //   495: aload #17
/*     */       //   497: monitorexit
/*     */       //   498: aload #19
/*     */       //   500: athrow
/*     */       //   501: lload #15
/*     */       //   503: lstore #20
/*     */       //   505: aload_0
/*     */       //   506: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   509: dup
/*     */       //   510: astore #18
/*     */       //   512: monitorenter
/*     */       //   513: aload_0
/*     */       //   514: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   517: aconst_null
/*     */       //   518: putfield upstreamReader : Ljava/lang/Thread;
/*     */       //   521: aload_0
/*     */       //   522: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   525: invokevirtual notifyAll : ()V
/*     */       //   528: aload #18
/*     */       //   530: monitorexit
/*     */       //   531: goto -> 542
/*     */       //   534: astore #22
/*     */       //   536: aload #18
/*     */       //   538: monitorexit
/*     */       //   539: aload #22
/*     */       //   541: athrow
/*     */       //   542: lload #20
/*     */       //   544: lreturn
/*     */       //   545: astore #23
/*     */       //   547: aload_0
/*     */       //   548: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   551: dup
/*     */       //   552: astore #24
/*     */       //   554: monitorenter
/*     */       //   555: aload_0
/*     */       //   556: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   559: aconst_null
/*     */       //   560: putfield upstreamReader : Ljava/lang/Thread;
/*     */       //   563: aload_0
/*     */       //   564: getfield this$0 : Lokhttp3/internal/cache2/Relay;
/*     */       //   567: invokevirtual notifyAll : ()V
/*     */       //   570: aload #24
/*     */       //   572: monitorexit
/*     */       //   573: goto -> 584
/*     */       //   576: astore #25
/*     */       //   578: aload #24
/*     */       //   580: monitorexit
/*     */       //   581: aload #25
/*     */       //   583: athrow
/*     */       //   584: aload #23
/*     */       //   586: athrow
/*     */       // Line number table:
/*     */       //   Java source line number -> byte code offset
/*     */       //   #252	-> 0
/*     */       //   #258	-> 17
/*     */       //   #260	-> 25
/*     */       //   #262	-> 43
/*     */       //   #265	-> 60
/*     */       //   #266	-> 70
/*     */       //   #267	-> 81
/*     */       //   #271	-> 84
/*     */       //   #272	-> 94
/*     */       //   #273	-> 97
/*     */       //   #276	-> 103
/*     */       //   #279	-> 118
/*     */       //   #280	-> 128
/*     */       //   #281	-> 131
/*     */       //   #285	-> 137
/*     */       //   #286	-> 150
/*     */       //   #287	-> 171
/*     */       //   #288	-> 182
/*     */       //   #289	-> 188
/*     */       //   #292	-> 196
/*     */       //   #293	-> 202
/*     */       //   #294	-> 215
/*     */       //   #295	-> 233
/*     */       //   #296	-> 244
/*     */       //   #302	-> 247
/*     */       //   #305	-> 275
/*     */       //   #306	-> 284
/*     */       //   #307	-> 293
/*     */       //   #332	-> 298
/*     */       //   #333	-> 306
/*     */       //   #334	-> 314
/*     */       //   #335	-> 321
/*     */       //   #307	-> 335
/*     */       //   #311	-> 338
/*     */       //   #312	-> 346
/*     */       //   #313	-> 361
/*     */       //   #316	-> 372
/*     */       //   #317	-> 389
/*     */       //   #316	-> 394
/*     */       //   #319	-> 397
/*     */       //   #321	-> 405
/*     */       //   #322	-> 424
/*     */       //   #323	-> 445
/*     */       //   #327	-> 473
/*     */       //   #328	-> 487
/*     */       //   #330	-> 501
/*     */       //   #332	-> 505
/*     */       //   #333	-> 513
/*     */       //   #334	-> 521
/*     */       //   #335	-> 528
/*     */       //   #330	-> 542
/*     */       //   #332	-> 545
/*     */       //   #333	-> 555
/*     */       //   #334	-> 563
/*     */       //   #335	-> 570
/*     */       // Local variable table:
/*     */       //   start	length	slot	name	descriptor
/*     */       //   97	6	7	source	I
/*     */       //   131	6	7	source	I
/*     */       //   118	70	8	bufferPos	J
/*     */       //   150	38	10	bytesToRead	J
/*     */       //   39	149	5	upstreamPos	J
/*     */       //   215	32	13	bytesToRead	J
/*     */       //   275	270	13	upstreamBytesRead	J
/*     */       //   346	199	15	bytesRead	J
/*     */       //   0	587	0	this	Lokhttp3/internal/cache2/Relay$RelaySource;
/*     */       //   0	587	1	sink	Lokio/Buffer;
/*     */       //   0	587	2	byteCount	J
/*     */       //   196	391	5	upstreamPos	J
/*     */       //   196	391	7	source	I
/*     */       // Exception table:
/*     */       //   from	to	target	type
/*     */       //   25	59	188	finally
/*     */       //   60	100	188	finally
/*     */       //   103	134	188	finally
/*     */       //   137	187	188	finally
/*     */       //   188	193	188	finally
/*     */       //   247	298	545	finally
/*     */       //   306	324	327	finally
/*     */       //   327	332	327	finally
/*     */       //   338	505	545	finally
/*     */       //   405	490	493	finally
/*     */       //   493	498	493	finally
/*     */       //   513	531	534	finally
/*     */       //   534	539	534	finally
/*     */       //   545	547	545	finally
/*     */       //   555	573	576	finally
/*     */       //   576	581	576	finally
/*     */     }
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Timeout timeout() {
/* 340 */       return this.timeout;
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 344 */       if (this.fileOperator == null)
/* 345 */         return;  this.fileOperator = null;
/*     */       
/* 347 */       RandomAccessFile fileToClose = null;
/* 348 */       synchronized (Relay.this) {
/* 349 */         Relay.this.sourceCount--;
/* 350 */         if (Relay.this.sourceCount == 0) {
/* 351 */           fileToClose = Relay.this.file;
/* 352 */           Relay.this.file = null;
/*     */         } 
/*     */       } 
/*     */       
/* 356 */       if (fileToClose != null)
/* 357 */         Util.closeQuietly(fileToClose); 
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\cache2\Relay.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */