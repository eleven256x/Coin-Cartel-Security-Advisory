/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
/*     */ import okio.BufferedSource;
/*     */ import okio.ByteString;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class Hpack
/*     */ {
/*     */   private static final int PREFIX_4_BITS = 15;
/*     */   private static final int PREFIX_5_BITS = 31;
/*     */   private static final int PREFIX_6_BITS = 63;
/*     */   private static final int PREFIX_7_BITS = 127;
/*  47 */   static final Header[] STATIC_HEADER_TABLE = new Header[] { new Header(Header.TARGET_AUTHORITY, ""), new Header(Header.TARGET_METHOD, "GET"), new Header(Header.TARGET_METHOD, "POST"), new Header(Header.TARGET_PATH, "/"), new Header(Header.TARGET_PATH, "/index.html"), new Header(Header.TARGET_SCHEME, "http"), new Header(Header.TARGET_SCHEME, "https"), new Header(Header.RESPONSE_STATUS, "200"), new Header(Header.RESPONSE_STATUS, "204"), new Header(Header.RESPONSE_STATUS, "206"), new Header(Header.RESPONSE_STATUS, "304"), new Header(Header.RESPONSE_STATUS, "400"), new Header(Header.RESPONSE_STATUS, "404"), new Header(Header.RESPONSE_STATUS, "500"), new Header("accept-charset", ""), new Header("accept-encoding", "gzip, deflate"), new Header("accept-language", ""), new Header("accept-ranges", ""), new Header("accept", ""), new Header("access-control-allow-origin", ""), new Header("age", ""), new Header("allow", ""), new Header("authorization", ""), new Header("cache-control", ""), new Header("content-disposition", ""), new Header("content-encoding", ""), new Header("content-language", ""), new Header("content-length", ""), new Header("content-location", ""), new Header("content-range", ""), new Header("content-type", ""), new Header("cookie", ""), new Header("date", ""), new Header("etag", ""), new Header("expect", ""), new Header("expires", ""), new Header("from", ""), new Header("host", ""), new Header("if-match", ""), new Header("if-modified-since", ""), new Header("if-none-match", ""), new Header("if-range", ""), new Header("if-unmodified-since", ""), new Header("last-modified", ""), new Header("link", ""), new Header("location", ""), new Header("max-forwards", ""), new Header("proxy-authenticate", ""), new Header("proxy-authorization", ""), new Header("range", ""), new Header("referer", ""), new Header("refresh", ""), new Header("retry-after", ""), new Header("server", ""), new Header("set-cookie", ""), new Header("strict-transport-security", ""), new Header("transfer-encoding", ""), new Header("user-agent", ""), new Header("vary", ""), new Header("via", ""), new Header("www-authenticate", "") };
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
/*     */   static final class Reader
/*     */   {
/* 117 */     private final List<Header> headerList = new ArrayList<>();
/*     */     
/*     */     private final BufferedSource source;
/*     */     
/*     */     private final int headerTableSizeSetting;
/*     */     
/*     */     private int maxDynamicTableByteCount;
/* 124 */     Header[] dynamicTable = new Header[8];
/*     */     
/* 126 */     int nextHeaderIndex = this.dynamicTable.length - 1;
/* 127 */     int headerCount = 0;
/* 128 */     int dynamicTableByteCount = 0;
/*     */     
/*     */     Reader(int headerTableSizeSetting, Source source) {
/* 131 */       this(headerTableSizeSetting, headerTableSizeSetting, source);
/*     */     }
/*     */     
/*     */     Reader(int headerTableSizeSetting, int maxDynamicTableByteCount, Source source) {
/* 135 */       this.headerTableSizeSetting = headerTableSizeSetting;
/* 136 */       this.maxDynamicTableByteCount = maxDynamicTableByteCount;
/* 137 */       this.source = Okio.buffer(source);
/*     */     }
/*     */     
/*     */     int maxDynamicTableByteCount() {
/* 141 */       return this.maxDynamicTableByteCount;
/*     */     }
/*     */     
/*     */     private void adjustDynamicTableByteCount() {
/* 145 */       if (this.maxDynamicTableByteCount < this.dynamicTableByteCount) {
/* 146 */         if (this.maxDynamicTableByteCount == 0) {
/* 147 */           clearDynamicTable();
/*     */         } else {
/* 149 */           evictToRecoverBytes(this.dynamicTableByteCount - this.maxDynamicTableByteCount);
/*     */         } 
/*     */       }
/*     */     }
/*     */     
/*     */     private void clearDynamicTable() {
/* 155 */       Arrays.fill((Object[])this.dynamicTable, (Object)null);
/* 156 */       this.nextHeaderIndex = this.dynamicTable.length - 1;
/* 157 */       this.headerCount = 0;
/* 158 */       this.dynamicTableByteCount = 0;
/*     */     }
/*     */ 
/*     */     
/*     */     private int evictToRecoverBytes(int bytesToRecover) {
/* 163 */       int entriesToEvict = 0;
/* 164 */       if (bytesToRecover > 0) {
/*     */         
/* 166 */         for (int j = this.dynamicTable.length - 1; j >= this.nextHeaderIndex && bytesToRecover > 0; j--) {
/* 167 */           bytesToRecover -= (this.dynamicTable[j]).hpackSize;
/* 168 */           this.dynamicTableByteCount -= (this.dynamicTable[j]).hpackSize;
/* 169 */           this.headerCount--;
/* 170 */           entriesToEvict++;
/*     */         } 
/* 172 */         System.arraycopy(this.dynamicTable, this.nextHeaderIndex + 1, this.dynamicTable, this.nextHeaderIndex + 1 + entriesToEvict, this.headerCount);
/*     */         
/* 174 */         this.nextHeaderIndex += entriesToEvict;
/*     */       } 
/* 176 */       return entriesToEvict;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     void readHeaders() throws IOException {
/* 184 */       while (!this.source.exhausted()) {
/* 185 */         int b = this.source.readByte() & 0xFF;
/* 186 */         if (b == 128)
/* 187 */           throw new IOException("index == 0"); 
/* 188 */         if ((b & 0x80) == 128) {
/* 189 */           int i = readInt(b, 127);
/* 190 */           readIndexedHeader(i - 1); continue;
/* 191 */         }  if (b == 64) {
/* 192 */           readLiteralHeaderWithIncrementalIndexingNewName(); continue;
/* 193 */         }  if ((b & 0x40) == 64) {
/* 194 */           int i = readInt(b, 63);
/* 195 */           readLiteralHeaderWithIncrementalIndexingIndexedName(i - 1); continue;
/* 196 */         }  if ((b & 0x20) == 32) {
/* 197 */           this.maxDynamicTableByteCount = readInt(b, 31);
/* 198 */           if (this.maxDynamicTableByteCount < 0 || this.maxDynamicTableByteCount > this.headerTableSizeSetting)
/*     */           {
/* 200 */             throw new IOException("Invalid dynamic table size update " + this.maxDynamicTableByteCount);
/*     */           }
/* 202 */           adjustDynamicTableByteCount(); continue;
/* 203 */         }  if (b == 16 || b == 0) {
/* 204 */           readLiteralHeaderWithoutIndexingNewName(); continue;
/*     */         } 
/* 206 */         int index = readInt(b, 15);
/* 207 */         readLiteralHeaderWithoutIndexingIndexedName(index - 1);
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     public List<Header> getAndResetHeaderList() {
/* 213 */       List<Header> result = new ArrayList<>(this.headerList);
/* 214 */       this.headerList.clear();
/* 215 */       return result;
/*     */     }
/*     */     
/*     */     private void readIndexedHeader(int index) throws IOException {
/* 219 */       if (isStaticHeader(index)) {
/* 220 */         Header staticEntry = Hpack.STATIC_HEADER_TABLE[index];
/* 221 */         this.headerList.add(staticEntry);
/*     */       } else {
/* 223 */         int dynamicTableIndex = dynamicTableIndex(index - Hpack.STATIC_HEADER_TABLE.length);
/* 224 */         if (dynamicTableIndex < 0 || dynamicTableIndex >= this.dynamicTable.length) {
/* 225 */           throw new IOException("Header index too large " + (index + 1));
/*     */         }
/* 227 */         this.headerList.add(this.dynamicTable[dynamicTableIndex]);
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     private int dynamicTableIndex(int index) {
/* 233 */       return this.nextHeaderIndex + 1 + index;
/*     */     }
/*     */     
/*     */     private void readLiteralHeaderWithoutIndexingIndexedName(int index) throws IOException {
/* 237 */       ByteString name = getName(index);
/* 238 */       ByteString value = readByteString();
/* 239 */       this.headerList.add(new Header(name, value));
/*     */     }
/*     */     
/*     */     private void readLiteralHeaderWithoutIndexingNewName() throws IOException {
/* 243 */       ByteString name = Hpack.checkLowercase(readByteString());
/* 244 */       ByteString value = readByteString();
/* 245 */       this.headerList.add(new Header(name, value));
/*     */     }
/*     */ 
/*     */     
/*     */     private void readLiteralHeaderWithIncrementalIndexingIndexedName(int nameIndex) throws IOException {
/* 250 */       ByteString name = getName(nameIndex);
/* 251 */       ByteString value = readByteString();
/* 252 */       insertIntoDynamicTable(-1, new Header(name, value));
/*     */     }
/*     */     
/*     */     private void readLiteralHeaderWithIncrementalIndexingNewName() throws IOException {
/* 256 */       ByteString name = Hpack.checkLowercase(readByteString());
/* 257 */       ByteString value = readByteString();
/* 258 */       insertIntoDynamicTable(-1, new Header(name, value));
/*     */     }
/*     */     
/*     */     private ByteString getName(int index) throws IOException {
/* 262 */       if (isStaticHeader(index)) {
/* 263 */         return (Hpack.STATIC_HEADER_TABLE[index]).name;
/*     */       }
/* 265 */       int dynamicTableIndex = dynamicTableIndex(index - Hpack.STATIC_HEADER_TABLE.length);
/* 266 */       if (dynamicTableIndex < 0 || dynamicTableIndex >= this.dynamicTable.length) {
/* 267 */         throw new IOException("Header index too large " + (index + 1));
/*     */       }
/*     */       
/* 270 */       return (this.dynamicTable[dynamicTableIndex]).name;
/*     */     }
/*     */ 
/*     */     
/*     */     private boolean isStaticHeader(int index) {
/* 275 */       return (index >= 0 && index <= Hpack.STATIC_HEADER_TABLE.length - 1);
/*     */     }
/*     */ 
/*     */     
/*     */     private void insertIntoDynamicTable(int index, Header entry) {
/* 280 */       this.headerList.add(entry);
/*     */       
/* 282 */       int delta = entry.hpackSize;
/* 283 */       if (index != -1) {
/* 284 */         delta -= (this.dynamicTable[dynamicTableIndex(index)]).hpackSize;
/*     */       }
/*     */ 
/*     */       
/* 288 */       if (delta > this.maxDynamicTableByteCount) {
/* 289 */         clearDynamicTable();
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 294 */       int bytesToRecover = this.dynamicTableByteCount + delta - this.maxDynamicTableByteCount;
/* 295 */       int entriesEvicted = evictToRecoverBytes(bytesToRecover);
/*     */       
/* 297 */       if (index == -1) {
/* 298 */         if (this.headerCount + 1 > this.dynamicTable.length) {
/* 299 */           Header[] doubled = new Header[this.dynamicTable.length * 2];
/* 300 */           System.arraycopy(this.dynamicTable, 0, doubled, this.dynamicTable.length, this.dynamicTable.length);
/* 301 */           this.nextHeaderIndex = this.dynamicTable.length - 1;
/* 302 */           this.dynamicTable = doubled;
/*     */         } 
/* 304 */         index = this.nextHeaderIndex--;
/* 305 */         this.dynamicTable[index] = entry;
/* 306 */         this.headerCount++;
/*     */       } else {
/* 308 */         index += dynamicTableIndex(index) + entriesEvicted;
/* 309 */         this.dynamicTable[index] = entry;
/*     */       } 
/* 311 */       this.dynamicTableByteCount += delta;
/*     */     }
/*     */     
/*     */     private int readByte() throws IOException {
/* 315 */       return this.source.readByte() & 0xFF;
/*     */     }
/*     */     
/*     */     int readInt(int firstByte, int prefixMask) throws IOException {
/* 319 */       int b, prefix = firstByte & prefixMask;
/* 320 */       if (prefix < prefixMask) {
/* 321 */         return prefix;
/*     */       }
/*     */ 
/*     */       
/* 325 */       int result = prefixMask;
/* 326 */       int shift = 0;
/*     */       while (true) {
/* 328 */         b = readByte();
/* 329 */         if ((b & 0x80) != 0) {
/* 330 */           result += (b & 0x7F) << shift;
/* 331 */           shift += 7; continue;
/*     */         }  break;
/* 333 */       }  result += b << shift;
/*     */ 
/*     */ 
/*     */       
/* 337 */       return result;
/*     */     }
/*     */ 
/*     */     
/*     */     ByteString readByteString() throws IOException {
/* 342 */       int firstByte = readByte();
/* 343 */       boolean huffmanDecode = ((firstByte & 0x80) == 128);
/* 344 */       int length = readInt(firstByte, 127);
/*     */       
/* 346 */       if (huffmanDecode) {
/* 347 */         return ByteString.of(Huffman.get().decode(this.source.readByteArray(length)));
/*     */       }
/* 349 */       return this.source.readByteString(length);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/* 354 */   static final Map<ByteString, Integer> NAME_TO_FIRST_INDEX = nameToFirstIndex();
/*     */   
/*     */   private static Map<ByteString, Integer> nameToFirstIndex() {
/* 357 */     Map<ByteString, Integer> result = new LinkedHashMap<>(STATIC_HEADER_TABLE.length);
/* 358 */     for (int i = 0; i < STATIC_HEADER_TABLE.length; i++) {
/* 359 */       if (!result.containsKey((STATIC_HEADER_TABLE[i]).name)) {
/* 360 */         result.put((STATIC_HEADER_TABLE[i]).name, Integer.valueOf(i));
/*     */       }
/*     */     } 
/* 363 */     return Collections.unmodifiableMap(result);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final class Writer
/*     */   {
/*     */     private static final int SETTINGS_HEADER_TABLE_SIZE = 4096;
/*     */ 
/*     */     
/*     */     private static final int SETTINGS_HEADER_TABLE_SIZE_LIMIT = 16384;
/*     */ 
/*     */     
/*     */     private final Buffer out;
/*     */ 
/*     */     
/*     */     private final boolean useCompression;
/*     */ 
/*     */     
/* 383 */     private int smallestHeaderTableSizeSetting = Integer.MAX_VALUE;
/*     */     
/*     */     private boolean emitDynamicTableSizeUpdate;
/*     */     
/*     */     int headerTableSizeSetting;
/*     */     
/*     */     int maxDynamicTableByteCount;
/* 390 */     Header[] dynamicTable = new Header[8];
/*     */     
/* 392 */     int nextHeaderIndex = this.dynamicTable.length - 1;
/* 393 */     int headerCount = 0;
/* 394 */     int dynamicTableByteCount = 0;
/*     */     
/*     */     Writer(Buffer out) {
/* 397 */       this(4096, true, out);
/*     */     }
/*     */     
/*     */     Writer(int headerTableSizeSetting, boolean useCompression, Buffer out) {
/* 401 */       this.headerTableSizeSetting = headerTableSizeSetting;
/* 402 */       this.maxDynamicTableByteCount = headerTableSizeSetting;
/* 403 */       this.useCompression = useCompression;
/* 404 */       this.out = out;
/*     */     }
/*     */     
/*     */     private void clearDynamicTable() {
/* 408 */       Arrays.fill((Object[])this.dynamicTable, (Object)null);
/* 409 */       this.nextHeaderIndex = this.dynamicTable.length - 1;
/* 410 */       this.headerCount = 0;
/* 411 */       this.dynamicTableByteCount = 0;
/*     */     }
/*     */ 
/*     */     
/*     */     private int evictToRecoverBytes(int bytesToRecover) {
/* 416 */       int entriesToEvict = 0;
/* 417 */       if (bytesToRecover > 0) {
/*     */         
/* 419 */         for (int j = this.dynamicTable.length - 1; j >= this.nextHeaderIndex && bytesToRecover > 0; j--) {
/* 420 */           bytesToRecover -= (this.dynamicTable[j]).hpackSize;
/* 421 */           this.dynamicTableByteCount -= (this.dynamicTable[j]).hpackSize;
/* 422 */           this.headerCount--;
/* 423 */           entriesToEvict++;
/*     */         } 
/* 425 */         System.arraycopy(this.dynamicTable, this.nextHeaderIndex + 1, this.dynamicTable, this.nextHeaderIndex + 1 + entriesToEvict, this.headerCount);
/*     */         
/* 427 */         Arrays.fill((Object[])this.dynamicTable, this.nextHeaderIndex + 1, this.nextHeaderIndex + 1 + entriesToEvict, (Object)null);
/* 428 */         this.nextHeaderIndex += entriesToEvict;
/*     */       } 
/* 430 */       return entriesToEvict;
/*     */     }
/*     */     
/*     */     private void insertIntoDynamicTable(Header entry) {
/* 434 */       int delta = entry.hpackSize;
/*     */ 
/*     */       
/* 437 */       if (delta > this.maxDynamicTableByteCount) {
/* 438 */         clearDynamicTable();
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 443 */       int bytesToRecover = this.dynamicTableByteCount + delta - this.maxDynamicTableByteCount;
/* 444 */       evictToRecoverBytes(bytesToRecover);
/*     */       
/* 446 */       if (this.headerCount + 1 > this.dynamicTable.length) {
/* 447 */         Header[] doubled = new Header[this.dynamicTable.length * 2];
/* 448 */         System.arraycopy(this.dynamicTable, 0, doubled, this.dynamicTable.length, this.dynamicTable.length);
/* 449 */         this.nextHeaderIndex = this.dynamicTable.length - 1;
/* 450 */         this.dynamicTable = doubled;
/*     */       } 
/* 452 */       int index = this.nextHeaderIndex--;
/* 453 */       this.dynamicTable[index] = entry;
/* 454 */       this.headerCount++;
/* 455 */       this.dynamicTableByteCount += delta;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     void writeHeaders(List<Header> headerBlock) throws IOException {
/* 461 */       if (this.emitDynamicTableSizeUpdate) {
/* 462 */         if (this.smallestHeaderTableSizeSetting < this.maxDynamicTableByteCount)
/*     */         {
/* 464 */           writeInt(this.smallestHeaderTableSizeSetting, 31, 32);
/*     */         }
/* 466 */         this.emitDynamicTableSizeUpdate = false;
/* 467 */         this.smallestHeaderTableSizeSetting = Integer.MAX_VALUE;
/* 468 */         writeInt(this.maxDynamicTableByteCount, 31, 32);
/*     */       } 
/*     */       
/* 471 */       for (int i = 0, size = headerBlock.size(); i < size; i++) {
/* 472 */         Header header = headerBlock.get(i);
/* 473 */         ByteString name = header.name.toAsciiLowercase();
/* 474 */         ByteString value = header.value;
/* 475 */         int headerIndex = -1;
/* 476 */         int headerNameIndex = -1;
/*     */         
/* 478 */         Integer staticIndex = Hpack.NAME_TO_FIRST_INDEX.get(name);
/* 479 */         if (staticIndex != null) {
/* 480 */           headerNameIndex = staticIndex.intValue() + 1;
/* 481 */           if (headerNameIndex > 1 && headerNameIndex < 8)
/*     */           {
/*     */ 
/*     */ 
/*     */             
/* 486 */             if (Util.equal((Hpack.STATIC_HEADER_TABLE[headerNameIndex - 1]).value, value)) {
/* 487 */               headerIndex = headerNameIndex;
/* 488 */             } else if (Util.equal((Hpack.STATIC_HEADER_TABLE[headerNameIndex]).value, value)) {
/* 489 */               headerIndex = headerNameIndex + 1;
/*     */             } 
/*     */           }
/*     */         } 
/*     */         
/* 494 */         if (headerIndex == -1) {
/* 495 */           for (int j = this.nextHeaderIndex + 1, length = this.dynamicTable.length; j < length; j++) {
/* 496 */             if (Util.equal((this.dynamicTable[j]).name, name)) {
/* 497 */               if (Util.equal((this.dynamicTable[j]).value, value)) {
/* 498 */                 headerIndex = j - this.nextHeaderIndex + Hpack.STATIC_HEADER_TABLE.length; break;
/*     */               } 
/* 500 */               if (headerNameIndex == -1) {
/* 501 */                 headerNameIndex = j - this.nextHeaderIndex + Hpack.STATIC_HEADER_TABLE.length;
/*     */               }
/*     */             } 
/*     */           } 
/*     */         }
/*     */         
/* 507 */         if (headerIndex != -1) {
/*     */           
/* 509 */           writeInt(headerIndex, 127, 128);
/* 510 */         } else if (headerNameIndex == -1) {
/*     */           
/* 512 */           this.out.writeByte(64);
/* 513 */           writeByteString(name);
/* 514 */           writeByteString(value);
/* 515 */           insertIntoDynamicTable(header);
/* 516 */         } else if (name.startsWith(Header.PSEUDO_PREFIX) && !Header.TARGET_AUTHORITY.equals(name)) {
/*     */ 
/*     */           
/* 519 */           writeInt(headerNameIndex, 15, 0);
/* 520 */           writeByteString(value);
/*     */         } else {
/*     */           
/* 523 */           writeInt(headerNameIndex, 63, 64);
/* 524 */           writeByteString(value);
/* 525 */           insertIntoDynamicTable(header);
/*     */         } 
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     void writeInt(int value, int prefixMask, int bits) {
/* 533 */       if (value < prefixMask) {
/* 534 */         this.out.writeByte(bits | value);
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 539 */       this.out.writeByte(bits | prefixMask);
/* 540 */       value -= prefixMask;
/*     */ 
/*     */       
/* 543 */       while (value >= 128) {
/* 544 */         int b = value & 0x7F;
/* 545 */         this.out.writeByte(b | 0x80);
/* 546 */         value >>>= 7;
/*     */       } 
/* 548 */       this.out.writeByte(value);
/*     */     }
/*     */     
/*     */     void writeByteString(ByteString data) throws IOException {
/* 552 */       if (this.useCompression && Huffman.get().encodedLength(data) < data.size()) {
/* 553 */         Buffer huffmanBuffer = new Buffer();
/* 554 */         Huffman.get().encode(data, (BufferedSink)huffmanBuffer);
/* 555 */         ByteString huffmanBytes = huffmanBuffer.readByteString();
/* 556 */         writeInt(huffmanBytes.size(), 127, 128);
/* 557 */         this.out.write(huffmanBytes);
/*     */       } else {
/* 559 */         writeInt(data.size(), 127, 0);
/* 560 */         this.out.write(data);
/*     */       } 
/*     */     }
/*     */     
/*     */     void setHeaderTableSizeSetting(int headerTableSizeSetting) {
/* 565 */       this.headerTableSizeSetting = headerTableSizeSetting;
/* 566 */       int effectiveHeaderTableSize = Math.min(headerTableSizeSetting, 16384);
/*     */ 
/*     */       
/* 569 */       if (this.maxDynamicTableByteCount == effectiveHeaderTableSize)
/*     */         return; 
/* 571 */       if (effectiveHeaderTableSize < this.maxDynamicTableByteCount) {
/* 572 */         this.smallestHeaderTableSizeSetting = Math.min(this.smallestHeaderTableSizeSetting, effectiveHeaderTableSize);
/*     */       }
/*     */       
/* 575 */       this.emitDynamicTableSizeUpdate = true;
/* 576 */       this.maxDynamicTableByteCount = effectiveHeaderTableSize;
/* 577 */       adjustDynamicTableByteCount();
/*     */     }
/*     */     
/*     */     private void adjustDynamicTableByteCount() {
/* 581 */       if (this.maxDynamicTableByteCount < this.dynamicTableByteCount) {
/* 582 */         if (this.maxDynamicTableByteCount == 0) {
/* 583 */           clearDynamicTable();
/*     */         } else {
/* 585 */           evictToRecoverBytes(this.dynamicTableByteCount - this.maxDynamicTableByteCount);
/*     */         } 
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static ByteString checkLowercase(ByteString name) throws IOException {
/* 596 */     for (int i = 0, length = name.size(); i < length; i++) {
/* 597 */       byte c = name.getByte(i);
/* 598 */       if (c >= 65 && c <= 90) {
/* 599 */         throw new IOException("PROTOCOL_ERROR response malformed: mixed case name: " + name.utf8());
/*     */       }
/*     */     } 
/* 602 */     return name;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Hpack.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */