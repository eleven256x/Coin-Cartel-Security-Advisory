/*     */ package com.google.gson.internal.bind;
/*     */ 
/*     */ import com.google.gson.Gson;
/*     */ import com.google.gson.JsonArray;
/*     */ import com.google.gson.JsonElement;
/*     */ import com.google.gson.JsonIOException;
/*     */ import com.google.gson.JsonNull;
/*     */ import com.google.gson.JsonObject;
/*     */ import com.google.gson.JsonPrimitive;
/*     */ import com.google.gson.JsonSyntaxException;
/*     */ import com.google.gson.TypeAdapter;
/*     */ import com.google.gson.TypeAdapterFactory;
/*     */ import com.google.gson.annotations.SerializedName;
/*     */ import com.google.gson.internal.LazilyParsedNumber;
/*     */ import com.google.gson.reflect.TypeToken;
/*     */ import com.google.gson.stream.JsonReader;
/*     */ import com.google.gson.stream.JsonToken;
/*     */ import com.google.gson.stream.JsonWriter;
/*     */ import java.io.IOException;
/*     */ import java.math.BigDecimal;
/*     */ import java.math.BigInteger;
/*     */ import java.net.InetAddress;
/*     */ import java.net.URI;
/*     */ import java.net.URISyntaxException;
/*     */ import java.net.URL;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.BitSet;
/*     */ import java.util.Calendar;
/*     */ import java.util.Currency;
/*     */ import java.util.Date;
/*     */ import java.util.GregorianCalendar;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.UUID;
/*     */ import java.util.concurrent.atomic.AtomicBoolean;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import java.util.concurrent.atomic.AtomicIntegerArray;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class TypeAdapters
/*     */ {
/*     */   private TypeAdapters() {
/*  65 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   
/*  69 */   public static final TypeAdapter<Class> CLASS = new TypeAdapter<Class>()
/*     */     {
/*     */       public void write(JsonWriter out, Class value) throws IOException {
/*  72 */         if (value == null) {
/*  73 */           out.nullValue();
/*     */         } else {
/*  75 */           throw new UnsupportedOperationException("Attempted to serialize java.lang.Class: " + value
/*  76 */               .getName() + ". Forgot to register a type adapter?");
/*     */         } 
/*     */       }
/*     */       
/*     */       public Class read(JsonReader in) throws IOException {
/*  81 */         if (in.peek() == JsonToken.NULL) {
/*  82 */           in.nextNull();
/*  83 */           return null;
/*     */         } 
/*  85 */         throw new UnsupportedOperationException("Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?");
/*     */       }
/*     */     };
/*     */ 
/*     */   
/*  90 */   public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);
/*     */   
/*  92 */   public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<BitSet>() {
/*     */       public BitSet read(JsonReader in) throws IOException {
/*  94 */         if (in.peek() == JsonToken.NULL) {
/*  95 */           in.nextNull();
/*  96 */           return null;
/*     */         } 
/*     */         
/*  99 */         BitSet bitset = new BitSet();
/* 100 */         in.beginArray();
/* 101 */         int i = 0;
/* 102 */         JsonToken tokenType = in.peek();
/* 103 */         while (tokenType != JsonToken.END_ARRAY) {
/*     */           boolean set; String stringValue;
/* 105 */           switch (tokenType) {
/*     */             case NUMBER:
/* 107 */               set = (in.nextInt() != 0);
/*     */               break;
/*     */             case BOOLEAN:
/* 110 */               set = in.nextBoolean();
/*     */               break;
/*     */             case STRING:
/* 113 */               stringValue = in.nextString();
/*     */               try {
/* 115 */                 set = (Integer.parseInt(stringValue) != 0);
/* 116 */               } catch (NumberFormatException e) {
/* 117 */                 throw new JsonSyntaxException("Error: Expecting: bitset number value (1, 0), Found: " + stringValue);
/*     */               } 
/*     */               break;
/*     */             
/*     */             default:
/* 122 */               throw new JsonSyntaxException("Invalid bitset value type: " + tokenType);
/*     */           } 
/* 124 */           if (set) {
/* 125 */             bitset.set(i);
/*     */           }
/* 127 */           i++;
/* 128 */           tokenType = in.peek();
/*     */         } 
/* 130 */         in.endArray();
/* 131 */         return bitset;
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, BitSet src) throws IOException {
/* 135 */         if (src == null) {
/* 136 */           out.nullValue();
/*     */           
/*     */           return;
/*     */         } 
/* 140 */         out.beginArray();
/* 141 */         for (int i = 0; i < src.length(); i++) {
/* 142 */           int value = src.get(i) ? 1 : 0;
/* 143 */           out.value(value);
/*     */         } 
/* 145 */         out.endArray();
/*     */       }
/*     */     };
/*     */   
/* 149 */   public static final TypeAdapterFactory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);
/*     */   
/* 151 */   public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>()
/*     */     {
/*     */       public Boolean read(JsonReader in) throws IOException {
/* 154 */         if (in.peek() == JsonToken.NULL) {
/* 155 */           in.nextNull();
/* 156 */           return null;
/* 157 */         }  if (in.peek() == JsonToken.STRING)
/*     */         {
/* 159 */           return Boolean.valueOf(Boolean.parseBoolean(in.nextString()));
/*     */         }
/* 161 */         return Boolean.valueOf(in.nextBoolean());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Boolean value) throws IOException {
/* 165 */         out.value(value);
/*     */       }
/*     */     };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/* 173 */   public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter<Boolean>() {
/*     */       public Boolean read(JsonReader in) throws IOException {
/* 175 */         if (in.peek() == JsonToken.NULL) {
/* 176 */           in.nextNull();
/* 177 */           return null;
/*     */         } 
/* 179 */         return Boolean.valueOf(in.nextString());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Boolean value) throws IOException {
/* 183 */         out.value((value == null) ? "null" : value.toString());
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 188 */   public static final TypeAdapterFactory BOOLEAN_FACTORY = newFactory(boolean.class, (Class)Boolean.class, (TypeAdapter)BOOLEAN);
/*     */   
/* 190 */   public static final TypeAdapter<Number> BYTE = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 193 */         if (in.peek() == JsonToken.NULL) {
/* 194 */           in.nextNull();
/* 195 */           return null;
/*     */         } 
/*     */         try {
/* 198 */           int intValue = in.nextInt();
/* 199 */           return Byte.valueOf((byte)intValue);
/* 200 */         } catch (NumberFormatException e) {
/* 201 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 206 */         out.value(value);
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 211 */   public static final TypeAdapterFactory BYTE_FACTORY = newFactory(byte.class, (Class)Byte.class, (TypeAdapter)BYTE);
/*     */   
/* 213 */   public static final TypeAdapter<Number> SHORT = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 216 */         if (in.peek() == JsonToken.NULL) {
/* 217 */           in.nextNull();
/* 218 */           return null;
/*     */         } 
/*     */         try {
/* 221 */           return Short.valueOf((short)in.nextInt());
/* 222 */         } catch (NumberFormatException e) {
/* 223 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 228 */         out.value(value);
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 233 */   public static final TypeAdapterFactory SHORT_FACTORY = newFactory(short.class, (Class)Short.class, (TypeAdapter)SHORT);
/*     */   
/* 235 */   public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 238 */         if (in.peek() == JsonToken.NULL) {
/* 239 */           in.nextNull();
/* 240 */           return null;
/*     */         } 
/*     */         try {
/* 243 */           return Integer.valueOf(in.nextInt());
/* 244 */         } catch (NumberFormatException e) {
/* 245 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 250 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 254 */   public static final TypeAdapterFactory INTEGER_FACTORY = newFactory(int.class, (Class)Integer.class, (TypeAdapter)INTEGER);
/*     */   
/* 256 */   public static final TypeAdapter<AtomicInteger> ATOMIC_INTEGER = (new TypeAdapter<AtomicInteger>() {
/*     */       public AtomicInteger read(JsonReader in) throws IOException {
/*     */         try {
/* 259 */           return new AtomicInteger(in.nextInt());
/* 260 */         } catch (NumberFormatException e) {
/* 261 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       public void write(JsonWriter out, AtomicInteger value) throws IOException {
/* 265 */         out.value(value.get());
/*     */       }
/* 267 */     }).nullSafe();
/*     */   
/* 269 */   public static final TypeAdapterFactory ATOMIC_INTEGER_FACTORY = newFactory(AtomicInteger.class, ATOMIC_INTEGER);
/*     */   
/* 271 */   public static final TypeAdapter<AtomicBoolean> ATOMIC_BOOLEAN = (new TypeAdapter<AtomicBoolean>() {
/*     */       public AtomicBoolean read(JsonReader in) throws IOException {
/* 273 */         return new AtomicBoolean(in.nextBoolean());
/*     */       }
/*     */       public void write(JsonWriter out, AtomicBoolean value) throws IOException {
/* 276 */         out.value(value.get());
/*     */       }
/* 278 */     }).nullSafe();
/*     */   
/* 280 */   public static final TypeAdapterFactory ATOMIC_BOOLEAN_FACTORY = newFactory(AtomicBoolean.class, ATOMIC_BOOLEAN);
/*     */   
/* 282 */   public static final TypeAdapter<AtomicIntegerArray> ATOMIC_INTEGER_ARRAY = (new TypeAdapter<AtomicIntegerArray>() {
/*     */       public AtomicIntegerArray read(JsonReader in) throws IOException {
/* 284 */         List<Integer> list = new ArrayList<Integer>();
/* 285 */         in.beginArray();
/* 286 */         while (in.hasNext()) {
/*     */           try {
/* 288 */             int integer = in.nextInt();
/* 289 */             list.add(Integer.valueOf(integer));
/* 290 */           } catch (NumberFormatException e) {
/* 291 */             throw new JsonSyntaxException(e);
/*     */           } 
/*     */         } 
/* 294 */         in.endArray();
/* 295 */         int length = list.size();
/* 296 */         AtomicIntegerArray array = new AtomicIntegerArray(length);
/* 297 */         for (int i = 0; i < length; i++) {
/* 298 */           array.set(i, ((Integer)list.get(i)).intValue());
/*     */         }
/* 300 */         return array;
/*     */       }
/*     */       public void write(JsonWriter out, AtomicIntegerArray value) throws IOException {
/* 303 */         out.beginArray();
/* 304 */         for (int i = 0, length = value.length(); i < length; i++) {
/* 305 */           out.value(value.get(i));
/*     */         }
/* 307 */         out.endArray();
/*     */       }
/* 309 */     }).nullSafe();
/*     */   
/* 311 */   public static final TypeAdapterFactory ATOMIC_INTEGER_ARRAY_FACTORY = newFactory(AtomicIntegerArray.class, ATOMIC_INTEGER_ARRAY);
/*     */   
/* 313 */   public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 316 */         if (in.peek() == JsonToken.NULL) {
/* 317 */           in.nextNull();
/* 318 */           return null;
/*     */         } 
/*     */         try {
/* 321 */           return Long.valueOf(in.nextLong());
/* 322 */         } catch (NumberFormatException e) {
/* 323 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 328 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 332 */   public static final TypeAdapter<Number> FLOAT = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 335 */         if (in.peek() == JsonToken.NULL) {
/* 336 */           in.nextNull();
/* 337 */           return null;
/*     */         } 
/* 339 */         return Float.valueOf((float)in.nextDouble());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 343 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 347 */   public static final TypeAdapter<Number> DOUBLE = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 350 */         if (in.peek() == JsonToken.NULL) {
/* 351 */           in.nextNull();
/* 352 */           return null;
/*     */         } 
/* 354 */         return Double.valueOf(in.nextDouble());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 358 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 362 */   public static final TypeAdapter<Number> NUMBER = new TypeAdapter<Number>()
/*     */     {
/*     */       public Number read(JsonReader in) throws IOException {
/* 365 */         JsonToken jsonToken = in.peek();
/* 366 */         switch (jsonToken) {
/*     */           case NULL:
/* 368 */             in.nextNull();
/* 369 */             return null;
/*     */           case NUMBER:
/* 371 */             return (Number)new LazilyParsedNumber(in.nextString());
/*     */         } 
/* 373 */         throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
/*     */       }
/*     */ 
/*     */       
/*     */       public void write(JsonWriter out, Number value) throws IOException {
/* 378 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 382 */   public static final TypeAdapterFactory NUMBER_FACTORY = newFactory(Number.class, NUMBER);
/*     */   
/* 384 */   public static final TypeAdapter<Character> CHARACTER = new TypeAdapter<Character>()
/*     */     {
/*     */       public Character read(JsonReader in) throws IOException {
/* 387 */         if (in.peek() == JsonToken.NULL) {
/* 388 */           in.nextNull();
/* 389 */           return null;
/*     */         } 
/* 391 */         String str = in.nextString();
/* 392 */         if (str.length() != 1) {
/* 393 */           throw new JsonSyntaxException("Expecting character, got: " + str);
/*     */         }
/* 395 */         return Character.valueOf(str.charAt(0));
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Character value) throws IOException {
/* 399 */         out.value((value == null) ? null : String.valueOf(value));
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 404 */   public static final TypeAdapterFactory CHARACTER_FACTORY = newFactory(char.class, (Class)Character.class, (TypeAdapter)CHARACTER);
/*     */   
/* 406 */   public static final TypeAdapter<String> STRING = new TypeAdapter<String>()
/*     */     {
/*     */       public String read(JsonReader in) throws IOException {
/* 409 */         JsonToken peek = in.peek();
/* 410 */         if (peek == JsonToken.NULL) {
/* 411 */           in.nextNull();
/* 412 */           return null;
/*     */         } 
/*     */         
/* 415 */         if (peek == JsonToken.BOOLEAN) {
/* 416 */           return Boolean.toString(in.nextBoolean());
/*     */         }
/* 418 */         return in.nextString();
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, String value) throws IOException {
/* 422 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 426 */   public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter<BigDecimal>() {
/*     */       public BigDecimal read(JsonReader in) throws IOException {
/* 428 */         if (in.peek() == JsonToken.NULL) {
/* 429 */           in.nextNull();
/* 430 */           return null;
/*     */         } 
/*     */         try {
/* 433 */           return new BigDecimal(in.nextString());
/* 434 */         } catch (NumberFormatException e) {
/* 435 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, BigDecimal value) throws IOException {
/* 440 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 444 */   public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter<BigInteger>() {
/*     */       public BigInteger read(JsonReader in) throws IOException {
/* 446 */         if (in.peek() == JsonToken.NULL) {
/* 447 */           in.nextNull();
/* 448 */           return null;
/*     */         } 
/*     */         try {
/* 451 */           return new BigInteger(in.nextString());
/* 452 */         } catch (NumberFormatException e) {
/* 453 */           throw new JsonSyntaxException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, BigInteger value) throws IOException {
/* 458 */         out.value(value);
/*     */       }
/*     */     };
/*     */   
/* 462 */   public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);
/*     */   
/* 464 */   public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>()
/*     */     {
/*     */       public StringBuilder read(JsonReader in) throws IOException {
/* 467 */         if (in.peek() == JsonToken.NULL) {
/* 468 */           in.nextNull();
/* 469 */           return null;
/*     */         } 
/* 471 */         return new StringBuilder(in.nextString());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, StringBuilder value) throws IOException {
/* 475 */         out.value((value == null) ? null : value.toString());
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 480 */   public static final TypeAdapterFactory STRING_BUILDER_FACTORY = newFactory(StringBuilder.class, STRING_BUILDER);
/*     */   
/* 482 */   public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>()
/*     */     {
/*     */       public StringBuffer read(JsonReader in) throws IOException {
/* 485 */         if (in.peek() == JsonToken.NULL) {
/* 486 */           in.nextNull();
/* 487 */           return null;
/*     */         } 
/* 489 */         return new StringBuffer(in.nextString());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, StringBuffer value) throws IOException {
/* 493 */         out.value((value == null) ? null : value.toString());
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 498 */   public static final TypeAdapterFactory STRING_BUFFER_FACTORY = newFactory(StringBuffer.class, STRING_BUFFER);
/*     */   
/* 500 */   public static final TypeAdapter<URL> URL = new TypeAdapter<URL>()
/*     */     {
/*     */       public URL read(JsonReader in) throws IOException {
/* 503 */         if (in.peek() == JsonToken.NULL) {
/* 504 */           in.nextNull();
/* 505 */           return null;
/*     */         } 
/* 507 */         String nextString = in.nextString();
/* 508 */         return "null".equals(nextString) ? null : new URL(nextString);
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, URL value) throws IOException {
/* 512 */         out.value((value == null) ? null : value.toExternalForm());
/*     */       }
/*     */     };
/*     */   
/* 516 */   public static final TypeAdapterFactory URL_FACTORY = newFactory(URL.class, URL);
/*     */   
/* 518 */   public static final TypeAdapter<URI> URI = new TypeAdapter<URI>()
/*     */     {
/*     */       public URI read(JsonReader in) throws IOException {
/* 521 */         if (in.peek() == JsonToken.NULL) {
/* 522 */           in.nextNull();
/* 523 */           return null;
/*     */         } 
/*     */         try {
/* 526 */           String nextString = in.nextString();
/* 527 */           return "null".equals(nextString) ? null : new URI(nextString);
/* 528 */         } catch (URISyntaxException e) {
/* 529 */           throw new JsonIOException(e);
/*     */         } 
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, URI value) throws IOException {
/* 534 */         out.value((value == null) ? null : value.toASCIIString());
/*     */       }
/*     */     };
/*     */   
/* 538 */   public static final TypeAdapterFactory URI_FACTORY = newFactory(URI.class, URI);
/*     */   
/* 540 */   public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>()
/*     */     {
/*     */       public InetAddress read(JsonReader in) throws IOException {
/* 543 */         if (in.peek() == JsonToken.NULL) {
/* 544 */           in.nextNull();
/* 545 */           return null;
/*     */         } 
/*     */         
/* 548 */         return InetAddress.getByName(in.nextString());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, InetAddress value) throws IOException {
/* 552 */         out.value((value == null) ? null : value.getHostAddress());
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 557 */   public static final TypeAdapterFactory INET_ADDRESS_FACTORY = newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);
/*     */   
/* 559 */   public static final TypeAdapter<UUID> UUID = new TypeAdapter<UUID>()
/*     */     {
/*     */       public UUID read(JsonReader in) throws IOException {
/* 562 */         if (in.peek() == JsonToken.NULL) {
/* 563 */           in.nextNull();
/* 564 */           return null;
/*     */         } 
/* 566 */         return UUID.fromString(in.nextString());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, UUID value) throws IOException {
/* 570 */         out.value((value == null) ? null : value.toString());
/*     */       }
/*     */     };
/*     */   
/* 574 */   public static final TypeAdapterFactory UUID_FACTORY = newFactory(UUID.class, UUID);
/*     */   
/* 576 */   public static final TypeAdapter<Currency> CURRENCY = (new TypeAdapter<Currency>()
/*     */     {
/*     */       public Currency read(JsonReader in) throws IOException {
/* 579 */         return Currency.getInstance(in.nextString());
/*     */       }
/*     */       
/*     */       public void write(JsonWriter out, Currency value) throws IOException {
/* 583 */         out.value(value.getCurrencyCode());
/*     */       }
/* 585 */     }).nullSafe();
/* 586 */   public static final TypeAdapterFactory CURRENCY_FACTORY = newFactory(Currency.class, CURRENCY);
/*     */   
/* 588 */   public static final TypeAdapterFactory TIMESTAMP_FACTORY = new TypeAdapterFactory()
/*     */     {
/*     */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 591 */         if (typeToken.getRawType() != Timestamp.class) {
/* 592 */           return null;
/*     */         }
/*     */         
/* 595 */         final TypeAdapter<Date> dateTypeAdapter = gson.getAdapter(Date.class);
/* 596 */         return (TypeAdapter)new TypeAdapter<Timestamp>() {
/*     */             public Timestamp read(JsonReader in) throws IOException {
/* 598 */               Date date = (Date)dateTypeAdapter.read(in);
/* 599 */               return (date != null) ? new Timestamp(date.getTime()) : null;
/*     */             }
/*     */             
/*     */             public void write(JsonWriter out, Timestamp value) throws IOException {
/* 603 */               dateTypeAdapter.write(out, value);
/*     */             }
/*     */           };
/*     */       }
/*     */     };
/*     */   
/* 609 */   public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>()
/*     */     {
/*     */       private static final String YEAR = "year";
/*     */       private static final String MONTH = "month";
/*     */       private static final String DAY_OF_MONTH = "dayOfMonth";
/*     */       private static final String HOUR_OF_DAY = "hourOfDay";
/*     */       private static final String MINUTE = "minute";
/*     */       private static final String SECOND = "second";
/*     */       
/*     */       public Calendar read(JsonReader in) throws IOException {
/* 619 */         if (in.peek() == JsonToken.NULL) {
/* 620 */           in.nextNull();
/* 621 */           return null;
/*     */         } 
/* 623 */         in.beginObject();
/* 624 */         int year = 0;
/* 625 */         int month = 0;
/* 626 */         int dayOfMonth = 0;
/* 627 */         int hourOfDay = 0;
/* 628 */         int minute = 0;
/* 629 */         int second = 0;
/* 630 */         while (in.peek() != JsonToken.END_OBJECT) {
/* 631 */           String name = in.nextName();
/* 632 */           int value = in.nextInt();
/* 633 */           if ("year".equals(name)) {
/* 634 */             year = value; continue;
/* 635 */           }  if ("month".equals(name)) {
/* 636 */             month = value; continue;
/* 637 */           }  if ("dayOfMonth".equals(name)) {
/* 638 */             dayOfMonth = value; continue;
/* 639 */           }  if ("hourOfDay".equals(name)) {
/* 640 */             hourOfDay = value; continue;
/* 641 */           }  if ("minute".equals(name)) {
/* 642 */             minute = value; continue;
/* 643 */           }  if ("second".equals(name)) {
/* 644 */             second = value;
/*     */           }
/*     */         } 
/* 647 */         in.endObject();
/* 648 */         return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
/*     */       }
/*     */ 
/*     */       
/*     */       public void write(JsonWriter out, Calendar value) throws IOException {
/* 653 */         if (value == null) {
/* 654 */           out.nullValue();
/*     */           return;
/*     */         } 
/* 657 */         out.beginObject();
/* 658 */         out.name("year");
/* 659 */         out.value(value.get(1));
/* 660 */         out.name("month");
/* 661 */         out.value(value.get(2));
/* 662 */         out.name("dayOfMonth");
/* 663 */         out.value(value.get(5));
/* 664 */         out.name("hourOfDay");
/* 665 */         out.value(value.get(11));
/* 666 */         out.name("minute");
/* 667 */         out.value(value.get(12));
/* 668 */         out.name("second");
/* 669 */         out.value(value.get(13));
/* 670 */         out.endObject();
/*     */       }
/*     */     };
/*     */ 
/*     */   
/* 675 */   public static final TypeAdapterFactory CALENDAR_FACTORY = newFactoryForMultipleTypes(Calendar.class, (Class)GregorianCalendar.class, CALENDAR);
/*     */   
/* 677 */   public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>()
/*     */     {
/*     */       public Locale read(JsonReader in) throws IOException {
/* 680 */         if (in.peek() == JsonToken.NULL) {
/* 681 */           in.nextNull();
/* 682 */           return null;
/*     */         } 
/* 684 */         String locale = in.nextString();
/* 685 */         StringTokenizer tokenizer = new StringTokenizer(locale, "_");
/* 686 */         String language = null;
/* 687 */         String country = null;
/* 688 */         String variant = null;
/* 689 */         if (tokenizer.hasMoreElements()) {
/* 690 */           language = tokenizer.nextToken();
/*     */         }
/* 692 */         if (tokenizer.hasMoreElements()) {
/* 693 */           country = tokenizer.nextToken();
/*     */         }
/* 695 */         if (tokenizer.hasMoreElements()) {
/* 696 */           variant = tokenizer.nextToken();
/*     */         }
/* 698 */         if (country == null && variant == null)
/* 699 */           return new Locale(language); 
/* 700 */         if (variant == null) {
/* 701 */           return new Locale(language, country);
/*     */         }
/* 703 */         return new Locale(language, country, variant);
/*     */       }
/*     */ 
/*     */       
/*     */       public void write(JsonWriter out, Locale value) throws IOException {
/* 708 */         out.value((value == null) ? null : value.toString());
/*     */       }
/*     */     };
/*     */   
/* 712 */   public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);
/*     */   
/* 714 */   public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() { public JsonElement read(JsonReader in) throws IOException { String number; JsonArray array;
/*     */         JsonObject object;
/* 716 */         switch (in.peek()) {
/*     */           case STRING:
/* 718 */             return (JsonElement)new JsonPrimitive(in.nextString());
/*     */           case NUMBER:
/* 720 */             number = in.nextString();
/* 721 */             return (JsonElement)new JsonPrimitive((Number)new LazilyParsedNumber(number));
/*     */           case BOOLEAN:
/* 723 */             return (JsonElement)new JsonPrimitive(Boolean.valueOf(in.nextBoolean()));
/*     */           case NULL:
/* 725 */             in.nextNull();
/* 726 */             return (JsonElement)JsonNull.INSTANCE;
/*     */           case BEGIN_ARRAY:
/* 728 */             array = new JsonArray();
/* 729 */             in.beginArray();
/* 730 */             while (in.hasNext()) {
/* 731 */               array.add(read(in));
/*     */             }
/* 733 */             in.endArray();
/* 734 */             return (JsonElement)array;
/*     */           case BEGIN_OBJECT:
/* 736 */             object = new JsonObject();
/* 737 */             in.beginObject();
/* 738 */             while (in.hasNext()) {
/* 739 */               object.add(in.nextName(), read(in));
/*     */             }
/* 741 */             in.endObject();
/* 742 */             return (JsonElement)object;
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 748 */         throw new IllegalArgumentException(); }
/*     */ 
/*     */ 
/*     */       
/*     */       public void write(JsonWriter out, JsonElement value) throws IOException {
/* 753 */         if (value == null || value.isJsonNull()) {
/* 754 */           out.nullValue();
/* 755 */         } else if (value.isJsonPrimitive()) {
/* 756 */           JsonPrimitive primitive = value.getAsJsonPrimitive();
/* 757 */           if (primitive.isNumber()) {
/* 758 */             out.value(primitive.getAsNumber());
/* 759 */           } else if (primitive.isBoolean()) {
/* 760 */             out.value(primitive.getAsBoolean());
/*     */           } else {
/* 762 */             out.value(primitive.getAsString());
/*     */           }
/*     */         
/* 765 */         } else if (value.isJsonArray()) {
/* 766 */           out.beginArray();
/* 767 */           for (JsonElement e : value.getAsJsonArray()) {
/* 768 */             write(out, e);
/*     */           }
/* 770 */           out.endArray();
/*     */         }
/* 772 */         else if (value.isJsonObject()) {
/* 773 */           out.beginObject();
/* 774 */           for (Map.Entry<String, JsonElement> e : (Iterable<Map.Entry<String, JsonElement>>)value.getAsJsonObject().entrySet()) {
/* 775 */             out.name(e.getKey());
/* 776 */             write(out, e.getValue());
/*     */           } 
/* 778 */           out.endObject();
/*     */         } else {
/*     */           
/* 781 */           throw new IllegalArgumentException("Couldn't write " + value.getClass());
/*     */         } 
/*     */       } }
/*     */   ;
/*     */ 
/*     */   
/* 787 */   public static final TypeAdapterFactory JSON_ELEMENT_FACTORY = newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);
/*     */   
/*     */   private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
/* 790 */     private final Map<String, T> nameToConstant = new HashMap<String, T>();
/* 791 */     private final Map<T, String> constantToName = new HashMap<T, String>();
/*     */     
/*     */     public EnumTypeAdapter(Class<T> classOfT) {
/*     */       try {
/* 795 */         for (Enum enum_ : (Enum[])classOfT.getEnumConstants()) {
/* 796 */           String name = enum_.name();
/* 797 */           SerializedName annotation = classOfT.getField(name).<SerializedName>getAnnotation(SerializedName.class);
/* 798 */           if (annotation != null) {
/* 799 */             name = annotation.value();
/* 800 */             for (String alternate : annotation.alternate()) {
/* 801 */               this.nameToConstant.put(alternate, (T)enum_);
/*     */             }
/*     */           } 
/* 804 */           this.nameToConstant.put(name, (T)enum_);
/* 805 */           this.constantToName.put((T)enum_, name);
/*     */         } 
/* 807 */       } catch (NoSuchFieldException e) {
/* 808 */         throw new AssertionError(e);
/*     */       } 
/*     */     }
/*     */     public T read(JsonReader in) throws IOException {
/* 812 */       if (in.peek() == JsonToken.NULL) {
/* 813 */         in.nextNull();
/* 814 */         return null;
/*     */       } 
/* 816 */       return this.nameToConstant.get(in.nextString());
/*     */     }
/*     */     
/*     */     public void write(JsonWriter out, T value) throws IOException {
/* 820 */       out.value((value == null) ? null : this.constantToName.get(value));
/*     */     }
/*     */   }
/*     */   
/* 824 */   public static final TypeAdapterFactory ENUM_FACTORY = new TypeAdapterFactory()
/*     */     {
/*     */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 827 */         Class<? super T> rawType = typeToken.getRawType();
/* 828 */         if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
/* 829 */           return null;
/*     */         }
/* 831 */         if (!rawType.isEnum()) {
/* 832 */           rawType = rawType.getSuperclass();
/*     */         }
/* 834 */         return new TypeAdapters.EnumTypeAdapter<T>(rawType);
/*     */       }
/*     */     };
/*     */ 
/*     */   
/*     */   public static <TT> TypeAdapterFactory newFactory(final TypeToken<TT> type, final TypeAdapter<TT> typeAdapter) {
/* 840 */     return new TypeAdapterFactory()
/*     */       {
/*     */         public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 843 */           return typeToken.equals(type) ? typeAdapter : null;
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */   
/*     */   public static <TT> TypeAdapterFactory newFactory(final Class<TT> type, final TypeAdapter<TT> typeAdapter) {
/* 850 */     return new TypeAdapterFactory()
/*     */       {
/*     */         public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 853 */           return (typeToken.getRawType() == type) ? typeAdapter : null;
/*     */         }
/*     */         public String toString() {
/* 856 */           return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */   
/*     */   public static <TT> TypeAdapterFactory newFactory(final Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter) {
/* 863 */     return new TypeAdapterFactory()
/*     */       {
/*     */         public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 866 */           Class<? super T> rawType = typeToken.getRawType();
/* 867 */           return (rawType == unboxed || rawType == boxed) ? typeAdapter : null;
/*     */         }
/*     */         public String toString() {
/* 870 */           return "Factory[type=" + boxed.getName() + "+" + unboxed
/* 871 */             .getName() + ",adapter=" + typeAdapter + "]";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */   
/*     */   public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(final Class<TT> base, final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter) {
/* 878 */     return new TypeAdapterFactory()
/*     */       {
/*     */         public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 881 */           Class<? super T> rawType = typeToken.getRawType();
/* 882 */           return (rawType == base || rawType == sub) ? typeAdapter : null;
/*     */         }
/*     */         public String toString() {
/* 885 */           return "Factory[type=" + base.getName() + "+" + sub
/* 886 */             .getName() + ",adapter=" + typeAdapter + "]";
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static <T1> TypeAdapterFactory newTypeHierarchyFactory(final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
/* 897 */     return new TypeAdapterFactory()
/*     */       {
/*     */         public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
/* 900 */           final Class<? super T2> requestedType = typeToken.getRawType();
/* 901 */           if (!clazz.isAssignableFrom(requestedType)) {
/* 902 */             return null;
/*     */           }
/* 904 */           return new TypeAdapter<T1>() {
/*     */               public void write(JsonWriter out, T1 value) throws IOException {
/* 906 */                 typeAdapter.write(out, value);
/*     */               }
/*     */               
/*     */               public T1 read(JsonReader in) throws IOException {
/* 910 */                 T1 result = (T1)typeAdapter.read(in);
/* 911 */                 if (result != null && !requestedType.isInstance(result)) {
/* 912 */                   throw new JsonSyntaxException("Expected a " + requestedType.getName() + " but was " + result
/* 913 */                       .getClass().getName());
/*     */                 }
/* 915 */                 return result;
/*     */               }
/*     */             };
/*     */         }
/*     */         public String toString() {
/* 920 */           return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
/*     */         }
/*     */       };
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\com\google\gson\internal\bind\TypeAdapters.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */