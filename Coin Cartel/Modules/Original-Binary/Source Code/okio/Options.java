/*     */ package okio;
/*     */ 
/*     */ import java.util.AbstractList;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import java.util.RandomAccess;
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
/*     */ public final class Options
/*     */   extends AbstractList<ByteString>
/*     */   implements RandomAccess
/*     */ {
/*     */   final ByteString[] byteStrings;
/*     */   final int[] trie;
/*     */   
/*     */   private Options(ByteString[] byteStrings, int[] trie) {
/*  31 */     this.byteStrings = byteStrings;
/*  32 */     this.trie = trie;
/*     */   }
/*     */   
/*     */   public static Options of(ByteString... byteStrings) {
/*  36 */     if (byteStrings.length == 0)
/*     */     {
/*  38 */       return new Options(new ByteString[0], new int[] { 0, -1 });
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*  43 */     List<ByteString> list = new ArrayList<>(Arrays.asList(byteStrings));
/*  44 */     Collections.sort(list);
/*  45 */     List<Integer> indexes = new ArrayList<>(); int i;
/*  46 */     for (i = 0; i < list.size(); i++) {
/*  47 */       indexes.add(Integer.valueOf(-1));
/*     */     }
/*  49 */     for (i = 0; i < list.size(); i++) {
/*  50 */       int sortedIndex = Collections.binarySearch((List)list, byteStrings[i]);
/*  51 */       indexes.set(sortedIndex, Integer.valueOf(i));
/*     */     } 
/*  53 */     if (((ByteString)list.get(0)).size() == 0) {
/*  54 */       throw new IllegalArgumentException("the empty byte string is not a supported option");
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  60 */     for (int a = 0; a < list.size(); a++) {
/*  61 */       ByteString prefix = list.get(a);
/*  62 */       for (int b = a + 1; b < list.size(); ) {
/*  63 */         ByteString byteString = list.get(b);
/*  64 */         if (!byteString.startsWith(prefix))
/*  65 */           break;  if (byteString.size() == prefix.size()) {
/*  66 */           throw new IllegalArgumentException("duplicate option: " + byteString);
/*     */         }
/*  68 */         if (((Integer)indexes.get(b)).intValue() > ((Integer)indexes.get(a)).intValue()) {
/*  69 */           list.remove(b);
/*  70 */           indexes.remove(b); continue;
/*     */         } 
/*  72 */         b++;
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/*  77 */     Buffer trieBytes = new Buffer();
/*  78 */     buildTrieRecursive(0L, trieBytes, 0, list, 0, list.size(), indexes);
/*     */     
/*  80 */     int[] trie = new int[intCount(trieBytes)];
/*  81 */     for (int j = 0; j < trie.length; j++) {
/*  82 */       trie[j] = trieBytes.readInt();
/*     */     }
/*  84 */     if (!trieBytes.exhausted()) {
/*  85 */       throw new AssertionError();
/*     */     }
/*     */     
/*  88 */     return new Options((ByteString[])byteStrings.clone(), trie);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void buildTrieRecursive(long nodeOffset, Buffer node, int byteStringOffset, List<ByteString> byteStrings, int fromIndex, int toIndex, List<Integer> indexes) {
/* 123 */     if (fromIndex >= toIndex) throw new AssertionError(); 
/* 124 */     for (int i = fromIndex; i < toIndex; i++) {
/* 125 */       if (((ByteString)byteStrings.get(i)).size() < byteStringOffset) throw new AssertionError();
/*     */     
/*     */     } 
/* 128 */     ByteString from = byteStrings.get(fromIndex);
/* 129 */     ByteString to = byteStrings.get(toIndex - 1);
/* 130 */     int prefixIndex = -1;
/*     */ 
/*     */     
/* 133 */     if (byteStringOffset == from.size()) {
/* 134 */       prefixIndex = ((Integer)indexes.get(fromIndex)).intValue();
/* 135 */       fromIndex++;
/* 136 */       from = byteStrings.get(fromIndex);
/*     */     } 
/*     */     
/* 139 */     if (from.getByte(byteStringOffset) != to.getByte(byteStringOffset)) {
/*     */       
/* 141 */       int selectChoiceCount = 1;
/* 142 */       for (int j = fromIndex + 1; j < toIndex; j++) {
/* 143 */         if (((ByteString)byteStrings.get(j - 1)).getByte(byteStringOffset) != ((ByteString)byteStrings
/* 144 */           .get(j)).getByte(byteStringOffset)) {
/* 145 */           selectChoiceCount++;
/*     */         }
/*     */       } 
/*     */ 
/*     */       
/* 150 */       long childNodesOffset = nodeOffset + intCount(node) + 2L + (selectChoiceCount * 2);
/*     */       
/* 152 */       node.writeInt(selectChoiceCount);
/* 153 */       node.writeInt(prefixIndex);
/*     */       
/* 155 */       for (int k = fromIndex; k < toIndex; k++) {
/* 156 */         byte rangeByte = ((ByteString)byteStrings.get(k)).getByte(byteStringOffset);
/* 157 */         if (k == fromIndex || rangeByte != ((ByteString)byteStrings.get(k - 1)).getByte(byteStringOffset)) {
/* 158 */           node.writeInt(rangeByte & 0xFF);
/*     */         }
/*     */       } 
/*     */       
/* 162 */       Buffer childNodes = new Buffer();
/* 163 */       int rangeStart = fromIndex;
/* 164 */       while (rangeStart < toIndex) {
/* 165 */         byte rangeByte = ((ByteString)byteStrings.get(rangeStart)).getByte(byteStringOffset);
/* 166 */         int rangeEnd = toIndex;
/* 167 */         for (int m = rangeStart + 1; m < toIndex; m++) {
/* 168 */           if (rangeByte != ((ByteString)byteStrings.get(m)).getByte(byteStringOffset)) {
/* 169 */             rangeEnd = m;
/*     */             
/*     */             break;
/*     */           } 
/*     */         } 
/* 174 */         if (rangeStart + 1 == rangeEnd && byteStringOffset + 1 == ((ByteString)byteStrings
/* 175 */           .get(rangeStart)).size()) {
/*     */           
/* 177 */           node.writeInt(((Integer)indexes.get(rangeStart)).intValue());
/*     */         } else {
/*     */           
/* 180 */           node.writeInt((int)(-1L * (childNodesOffset + intCount(childNodes))));
/* 181 */           buildTrieRecursive(childNodesOffset, childNodes, byteStringOffset + 1, byteStrings, rangeStart, rangeEnd, indexes);
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 191 */         rangeStart = rangeEnd;
/*     */       } 
/*     */       
/* 194 */       node.write(childNodes, childNodes.size());
/*     */     }
/*     */     else {
/*     */       
/* 198 */       int scanByteCount = 0;
/* 199 */       for (int j = byteStringOffset, max = Math.min(from.size(), to.size()); j < max && 
/* 200 */         from.getByte(j) == to.getByte(j); j++) {
/* 201 */         scanByteCount++;
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 208 */       long childNodesOffset = nodeOffset + intCount(node) + 2L + scanByteCount + 1L;
/*     */       
/* 210 */       node.writeInt(-scanByteCount);
/* 211 */       node.writeInt(prefixIndex);
/*     */       
/* 213 */       for (int k = byteStringOffset; k < byteStringOffset + scanByteCount; k++) {
/* 214 */         node.writeInt(from.getByte(k) & 0xFF);
/*     */       }
/*     */       
/* 217 */       if (fromIndex + 1 == toIndex) {
/*     */         
/* 219 */         if (byteStringOffset + scanByteCount != ((ByteString)byteStrings.get(fromIndex)).size()) {
/* 220 */           throw new AssertionError();
/*     */         }
/* 222 */         node.writeInt(((Integer)indexes.get(fromIndex)).intValue());
/*     */       } else {
/*     */         
/* 225 */         Buffer childNodes = new Buffer();
/* 226 */         node.writeInt((int)(-1L * (childNodesOffset + intCount(childNodes))));
/* 227 */         buildTrieRecursive(childNodesOffset, childNodes, byteStringOffset + scanByteCount, byteStrings, fromIndex, toIndex, indexes);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 235 */         node.write(childNodes, childNodes.size());
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public ByteString get(int i) {
/* 241 */     return this.byteStrings[i];
/*     */   }
/*     */   
/*     */   public final int size() {
/* 245 */     return this.byteStrings.length;
/*     */   }
/*     */   
/*     */   private static int intCount(Buffer trieBytes) {
/* 249 */     return (int)(trieBytes.size() / 4L);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Options.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */