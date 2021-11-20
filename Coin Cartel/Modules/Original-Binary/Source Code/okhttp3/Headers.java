/*     */ package okhttp3;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TreeMap;
/*     */ import java.util.TreeSet;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.http.HttpDate;
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
/*     */ public final class Headers
/*     */ {
/*     */   private final String[] namesAndValues;
/*     */   
/*     */   Headers(Builder builder) {
/*  54 */     this.namesAndValues = builder.namesAndValues.<String>toArray(new String[builder.namesAndValues.size()]);
/*     */   }
/*     */   
/*     */   private Headers(String[] namesAndValues) {
/*  58 */     this.namesAndValues = namesAndValues;
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public String get(String name) {
/*  63 */     return get(this.namesAndValues, name);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Date getDate(String name) {
/*  71 */     String value = get(name);
/*  72 */     return (value != null) ? HttpDate.parse(value) : null;
/*     */   }
/*     */ 
/*     */   
/*     */   public int size() {
/*  77 */     return this.namesAndValues.length / 2;
/*     */   }
/*     */ 
/*     */   
/*     */   public String name(int index) {
/*  82 */     return this.namesAndValues[index * 2];
/*     */   }
/*     */ 
/*     */   
/*     */   public String value(int index) {
/*  87 */     return this.namesAndValues[index * 2 + 1];
/*     */   }
/*     */ 
/*     */   
/*     */   public Set<String> names() {
/*  92 */     TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
/*  93 */     for (int i = 0, size = size(); i < size; i++) {
/*  94 */       result.add(name(i));
/*     */     }
/*  96 */     return Collections.unmodifiableSet(result);
/*     */   }
/*     */ 
/*     */   
/*     */   public List<String> values(String name) {
/* 101 */     List<String> result = null;
/* 102 */     for (int i = 0, size = size(); i < size; i++) {
/* 103 */       if (name.equalsIgnoreCase(name(i))) {
/* 104 */         if (result == null) result = new ArrayList<>(2); 
/* 105 */         result.add(value(i));
/*     */       } 
/*     */     } 
/* 108 */     return (result != null) ? 
/* 109 */       Collections.<String>unmodifiableList(result) : 
/* 110 */       Collections.<String>emptyList();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public long byteCount() {
/* 121 */     long result = (this.namesAndValues.length * 2);
/*     */     
/* 123 */     for (int i = 0, size = this.namesAndValues.length; i < size; i++) {
/* 124 */       result += this.namesAndValues[i].length();
/*     */     }
/*     */     
/* 127 */     return result;
/*     */   }
/*     */   
/*     */   public Builder newBuilder() {
/* 131 */     Builder result = new Builder();
/* 132 */     Collections.addAll(result.namesAndValues, this.namesAndValues);
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
/*     */   public boolean equals(@Nullable Object other) {
/* 163 */     return (other instanceof Headers && 
/* 164 */       Arrays.equals((Object[])((Headers)other).namesAndValues, (Object[])this.namesAndValues));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 168 */     return Arrays.hashCode((Object[])this.namesAndValues);
/*     */   }
/*     */   
/*     */   public String toString() {
/* 172 */     StringBuilder result = new StringBuilder();
/* 173 */     for (int i = 0, size = size(); i < size; i++) {
/* 174 */       result.append(name(i)).append(": ").append(value(i)).append("\n");
/*     */     }
/* 176 */     return result.toString();
/*     */   }
/*     */   
/*     */   public Map<String, List<String>> toMultimap() {
/* 180 */     Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
/* 181 */     for (int i = 0, size = size(); i < size; i++) {
/* 182 */       String name = name(i).toLowerCase(Locale.US);
/* 183 */       List<String> values = result.get(name);
/* 184 */       if (values == null) {
/* 185 */         values = new ArrayList<>(2);
/* 186 */         result.put(name, values);
/*     */       } 
/* 188 */       values.add(value(i));
/*     */     } 
/* 190 */     return result;
/*     */   }
/*     */   
/*     */   private static String get(String[] namesAndValues, String name) {
/* 194 */     for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
/* 195 */       if (name.equalsIgnoreCase(namesAndValues[i])) {
/* 196 */         return namesAndValues[i + 1];
/*     */       }
/*     */     } 
/* 199 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Headers of(String... namesAndValues) {
/* 207 */     if (namesAndValues == null) throw new NullPointerException("namesAndValues == null"); 
/* 208 */     if (namesAndValues.length % 2 != 0) {
/* 209 */       throw new IllegalArgumentException("Expected alternating header names and values");
/*     */     }
/*     */ 
/*     */     
/* 213 */     namesAndValues = (String[])namesAndValues.clone(); int i;
/* 214 */     for (i = 0; i < namesAndValues.length; i++) {
/* 215 */       if (namesAndValues[i] == null) throw new IllegalArgumentException("Headers cannot be null"); 
/* 216 */       namesAndValues[i] = namesAndValues[i].trim();
/*     */     } 
/*     */ 
/*     */     
/* 220 */     for (i = 0; i < namesAndValues.length; i += 2) {
/* 221 */       String name = namesAndValues[i];
/* 222 */       String value = namesAndValues[i + 1];
/* 223 */       checkName(name);
/* 224 */       checkValue(value, name);
/*     */     } 
/*     */     
/* 227 */     return new Headers(namesAndValues);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Headers of(Map<String, String> headers) {
/* 234 */     if (headers == null) throw new NullPointerException("headers == null");
/*     */ 
/*     */     
/* 237 */     String[] namesAndValues = new String[headers.size() * 2];
/* 238 */     int i = 0;
/* 239 */     for (Map.Entry<String, String> header : headers.entrySet()) {
/* 240 */       if (header.getKey() == null || header.getValue() == null) {
/* 241 */         throw new IllegalArgumentException("Headers cannot be null");
/*     */       }
/* 243 */       String name = ((String)header.getKey()).trim();
/* 244 */       String value = ((String)header.getValue()).trim();
/* 245 */       checkName(name);
/* 246 */       checkValue(value, name);
/* 247 */       namesAndValues[i] = name;
/* 248 */       namesAndValues[i + 1] = value;
/* 249 */       i += 2;
/*     */     } 
/*     */     
/* 252 */     return new Headers(namesAndValues);
/*     */   }
/*     */   
/*     */   static void checkName(String name) {
/* 256 */     if (name == null) throw new NullPointerException("name == null"); 
/* 257 */     if (name.isEmpty()) throw new IllegalArgumentException("name is empty"); 
/* 258 */     for (int i = 0, length = name.length(); i < length; i++) {
/* 259 */       char c = name.charAt(i);
/* 260 */       if (c <= ' ' || c >= '')
/* 261 */         throw new IllegalArgumentException(Util.format("Unexpected char %#04x at %d in header name: %s", new Object[] {
/* 262 */                 Integer.valueOf(c), Integer.valueOf(i), name
/*     */               })); 
/*     */     } 
/*     */   }
/*     */   
/*     */   static void checkValue(String value, String name) {
/* 268 */     if (value == null) throw new NullPointerException("value for name " + name + " == null"); 
/* 269 */     for (int i = 0, length = value.length(); i < length; i++) {
/* 270 */       char c = value.charAt(i);
/* 271 */       if ((c <= '\037' && c != '\t') || c >= '')
/* 272 */         throw new IllegalArgumentException(Util.format("Unexpected char %#04x at %d in %s value: %s", new Object[] {
/* 273 */                 Integer.valueOf(c), Integer.valueOf(i), name, value
/*     */               })); 
/*     */     } 
/*     */   }
/*     */   
/*     */   public static final class Builder {
/* 279 */     final List<String> namesAndValues = new ArrayList<>(20);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     Builder addLenient(String line) {
/* 286 */       int index = line.indexOf(":", 1);
/* 287 */       if (index != -1)
/* 288 */         return addLenient(line.substring(0, index), line.substring(index + 1)); 
/* 289 */       if (line.startsWith(":"))
/*     */       {
/*     */         
/* 292 */         return addLenient("", line.substring(1));
/*     */       }
/* 294 */       return addLenient("", line);
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder add(String line) {
/* 300 */       int index = line.indexOf(":");
/* 301 */       if (index == -1) {
/* 302 */         throw new IllegalArgumentException("Unexpected header: " + line);
/*     */       }
/* 304 */       return add(line.substring(0, index).trim(), line.substring(index + 1));
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder add(String name, String value) {
/* 311 */       Headers.checkName(name);
/* 312 */       Headers.checkValue(value, name);
/* 313 */       return addLenient(name, value);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder addUnsafeNonAscii(String name, String value) {
/* 321 */       Headers.checkName(name);
/* 322 */       return addLenient(name, value);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder addAll(Headers headers) {
/* 329 */       int size = headers.size();
/* 330 */       for (int i = 0; i < size; i++) {
/* 331 */         addLenient(headers.name(i), headers.value(i));
/*     */       }
/*     */       
/* 334 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder add(String name, Date value) {
/* 342 */       if (value == null) throw new NullPointerException("value for name " + name + " == null"); 
/* 343 */       add(name, HttpDate.format(value));
/* 344 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder set(String name, Date value) {
/* 352 */       if (value == null) throw new NullPointerException("value for name " + name + " == null"); 
/* 353 */       set(name, HttpDate.format(value));
/* 354 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     Builder addLenient(String name, String value) {
/* 362 */       this.namesAndValues.add(name);
/* 363 */       this.namesAndValues.add(value.trim());
/* 364 */       return this;
/*     */     }
/*     */     
/*     */     public Builder removeAll(String name) {
/* 368 */       for (int i = 0; i < this.namesAndValues.size(); i += 2) {
/* 369 */         if (name.equalsIgnoreCase(this.namesAndValues.get(i))) {
/* 370 */           this.namesAndValues.remove(i);
/* 371 */           this.namesAndValues.remove(i);
/* 372 */           i -= 2;
/*     */         } 
/*     */       } 
/* 375 */       return this;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Builder set(String name, String value) {
/* 383 */       Headers.checkName(name);
/* 384 */       Headers.checkValue(value, name);
/* 385 */       removeAll(name);
/* 386 */       addLenient(name, value);
/* 387 */       return this;
/*     */     }
/*     */ 
/*     */     
/*     */     public String get(String name) {
/* 392 */       for (int i = this.namesAndValues.size() - 2; i >= 0; i -= 2) {
/* 393 */         if (name.equalsIgnoreCase(this.namesAndValues.get(i))) {
/* 394 */           return this.namesAndValues.get(i + 1);
/*     */         }
/*     */       } 
/* 397 */       return null;
/*     */     }
/*     */     
/*     */     public Headers build() {
/* 401 */       return new Headers(this);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Headers.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */