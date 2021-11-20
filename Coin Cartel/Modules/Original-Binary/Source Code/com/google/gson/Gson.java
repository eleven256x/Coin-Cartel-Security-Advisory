/*     */ package com.google.gson;
/*     */ 
/*     */ import com.google.gson.internal.ConstructorConstructor;
/*     */ import com.google.gson.internal.Excluder;
/*     */ import com.google.gson.internal.Primitives;
/*     */ import com.google.gson.internal.Streams;
/*     */ import com.google.gson.internal.bind.ArrayTypeAdapter;
/*     */ import com.google.gson.internal.bind.CollectionTypeAdapterFactory;
/*     */ import com.google.gson.internal.bind.DateTypeAdapter;
/*     */ import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
/*     */ import com.google.gson.internal.bind.JsonTreeReader;
/*     */ import com.google.gson.internal.bind.JsonTreeWriter;
/*     */ import com.google.gson.internal.bind.MapTypeAdapterFactory;
/*     */ import com.google.gson.internal.bind.ObjectTypeAdapter;
/*     */ import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
/*     */ import com.google.gson.internal.bind.SqlDateTypeAdapter;
/*     */ import com.google.gson.internal.bind.TimeTypeAdapter;
/*     */ import com.google.gson.internal.bind.TypeAdapters;
/*     */ import com.google.gson.reflect.TypeToken;
/*     */ import com.google.gson.stream.JsonReader;
/*     */ import com.google.gson.stream.JsonToken;
/*     */ import com.google.gson.stream.JsonWriter;
/*     */ import com.google.gson.stream.MalformedJsonException;
/*     */ import java.io.EOFException;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.io.StringReader;
/*     */ import java.io.StringWriter;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.Type;
/*     */ import java.math.BigDecimal;
/*     */ import java.math.BigInteger;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.atomic.AtomicLong;
/*     */ import java.util.concurrent.atomic.AtomicLongArray;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class Gson
/*     */ {
/*     */   static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;
/*     */   static final boolean DEFAULT_LENIENT = false;
/*     */   static final boolean DEFAULT_PRETTY_PRINT = false;
/*     */   static final boolean DEFAULT_ESCAPE_HTML = true;
/*     */   static final boolean DEFAULT_SERIALIZE_NULLS = false;
/*     */   static final boolean DEFAULT_COMPLEX_MAP_KEYS = false;
/*     */   static final boolean DEFAULT_SPECIALIZE_FLOAT_VALUES = false;
/* 112 */   private static final TypeToken<?> NULL_KEY_SURROGATE = new TypeToken<Object>()
/*     */     {
/*     */     
/*     */     };
/*     */ 
/*     */ 
/*     */   
/*     */   private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";
/*     */ 
/*     */   
/* 122 */   private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls = new ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>>();
/*     */ 
/*     */   
/* 125 */   private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache = new ConcurrentHashMap<TypeToken<?>, TypeAdapter<?>>();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final List<TypeAdapterFactory> factories;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final ConstructorConstructor constructorConstructor;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final Excluder excluder;
/*     */ 
/*     */ 
/*     */   
/*     */   private final FieldNamingStrategy fieldNamingStrategy;
/*     */ 
/*     */ 
/*     */   
/*     */   private final boolean serializeNulls;
/*     */ 
/*     */ 
/*     */   
/*     */   private final boolean htmlSafe;
/*     */ 
/*     */ 
/*     */   
/*     */   private final boolean generateNonExecutableJson;
/*     */ 
/*     */ 
/*     */   
/*     */   private final boolean prettyPrinting;
/*     */ 
/*     */ 
/*     */   
/*     */   private final boolean lenient;
/*     */ 
/*     */ 
/*     */   
/*     */   private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Gson() {
/* 174 */     this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY, 
/* 175 */         Collections.emptyMap(), false, false, false, true, false, false, false, LongSerializationPolicy.DEFAULT, 
/*     */ 
/*     */         
/* 178 */         Collections.emptyList());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   Gson(Excluder excluder, FieldNamingStrategy fieldNamingStrategy, Map<Type, InstanceCreator<?>> instanceCreators, boolean serializeNulls, boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe, boolean prettyPrinting, boolean lenient, boolean serializeSpecialFloatingPointValues, LongSerializationPolicy longSerializationPolicy, List<TypeAdapterFactory> typeAdapterFactories) {
/* 187 */     this.constructorConstructor = new ConstructorConstructor(instanceCreators);
/* 188 */     this.excluder = excluder;
/* 189 */     this.fieldNamingStrategy = fieldNamingStrategy;
/* 190 */     this.serializeNulls = serializeNulls;
/* 191 */     this.generateNonExecutableJson = generateNonExecutableGson;
/* 192 */     this.htmlSafe = htmlSafe;
/* 193 */     this.prettyPrinting = prettyPrinting;
/* 194 */     this.lenient = lenient;
/*     */     
/* 196 */     List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>();
/*     */ 
/*     */     
/* 199 */     factories.add(TypeAdapters.JSON_ELEMENT_FACTORY);
/* 200 */     factories.add(ObjectTypeAdapter.FACTORY);
/*     */ 
/*     */     
/* 203 */     factories.add(excluder);
/*     */ 
/*     */     
/* 206 */     factories.addAll(typeAdapterFactories);
/*     */ 
/*     */     
/* 209 */     factories.add(TypeAdapters.STRING_FACTORY);
/* 210 */     factories.add(TypeAdapters.INTEGER_FACTORY);
/* 211 */     factories.add(TypeAdapters.BOOLEAN_FACTORY);
/* 212 */     factories.add(TypeAdapters.BYTE_FACTORY);
/* 213 */     factories.add(TypeAdapters.SHORT_FACTORY);
/* 214 */     TypeAdapter<Number> longAdapter = longAdapter(longSerializationPolicy);
/* 215 */     factories.add(TypeAdapters.newFactory(long.class, Long.class, longAdapter));
/* 216 */     factories.add(TypeAdapters.newFactory(double.class, Double.class, 
/* 217 */           doubleAdapter(serializeSpecialFloatingPointValues)));
/* 218 */     factories.add(TypeAdapters.newFactory(float.class, Float.class, 
/* 219 */           floatAdapter(serializeSpecialFloatingPointValues)));
/* 220 */     factories.add(TypeAdapters.NUMBER_FACTORY);
/* 221 */     factories.add(TypeAdapters.ATOMIC_INTEGER_FACTORY);
/* 222 */     factories.add(TypeAdapters.ATOMIC_BOOLEAN_FACTORY);
/* 223 */     factories.add(TypeAdapters.newFactory(AtomicLong.class, atomicLongAdapter(longAdapter)));
/* 224 */     factories.add(TypeAdapters.newFactory(AtomicLongArray.class, atomicLongArrayAdapter(longAdapter)));
/* 225 */     factories.add(TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY);
/* 226 */     factories.add(TypeAdapters.CHARACTER_FACTORY);
/* 227 */     factories.add(TypeAdapters.STRING_BUILDER_FACTORY);
/* 228 */     factories.add(TypeAdapters.STRING_BUFFER_FACTORY);
/* 229 */     factories.add(TypeAdapters.newFactory(BigDecimal.class, TypeAdapters.BIG_DECIMAL));
/* 230 */     factories.add(TypeAdapters.newFactory(BigInteger.class, TypeAdapters.BIG_INTEGER));
/* 231 */     factories.add(TypeAdapters.URL_FACTORY);
/* 232 */     factories.add(TypeAdapters.URI_FACTORY);
/* 233 */     factories.add(TypeAdapters.UUID_FACTORY);
/* 234 */     factories.add(TypeAdapters.CURRENCY_FACTORY);
/* 235 */     factories.add(TypeAdapters.LOCALE_FACTORY);
/* 236 */     factories.add(TypeAdapters.INET_ADDRESS_FACTORY);
/* 237 */     factories.add(TypeAdapters.BIT_SET_FACTORY);
/* 238 */     factories.add(DateTypeAdapter.FACTORY);
/* 239 */     factories.add(TypeAdapters.CALENDAR_FACTORY);
/* 240 */     factories.add(TimeTypeAdapter.FACTORY);
/* 241 */     factories.add(SqlDateTypeAdapter.FACTORY);
/* 242 */     factories.add(TypeAdapters.TIMESTAMP_FACTORY);
/* 243 */     factories.add(ArrayTypeAdapter.FACTORY);
/* 244 */     factories.add(TypeAdapters.CLASS_FACTORY);
/*     */ 
/*     */     
/* 247 */     factories.add(new CollectionTypeAdapterFactory(this.constructorConstructor));
/* 248 */     factories.add(new MapTypeAdapterFactory(this.constructorConstructor, complexMapKeySerialization));
/* 249 */     this.jsonAdapterFactory = new JsonAdapterAnnotationTypeAdapterFactory(this.constructorConstructor);
/* 250 */     factories.add(this.jsonAdapterFactory);
/* 251 */     factories.add(TypeAdapters.ENUM_FACTORY);
/* 252 */     factories.add(new ReflectiveTypeAdapterFactory(this.constructorConstructor, fieldNamingStrategy, excluder, this.jsonAdapterFactory));
/*     */ 
/*     */     
/* 255 */     this.factories = Collections.unmodifiableList(factories);
/*     */   }
/*     */   
/*     */   public Excluder excluder() {
/* 259 */     return this.excluder;
/*     */   }
/*     */   
/*     */   public FieldNamingStrategy fieldNamingStrategy() {
/* 263 */     return this.fieldNamingStrategy;
/*     */   }
/*     */   
/*     */   public boolean serializeNulls() {
/* 267 */     return this.serializeNulls;
/*     */   }
/*     */   
/*     */   public boolean htmlSafe() {
/* 271 */     return this.htmlSafe;
/*     */   }
/*     */   
/*     */   private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
/* 275 */     if (serializeSpecialFloatingPointValues) {
/* 276 */       return TypeAdapters.DOUBLE;
/*     */     }
/* 278 */     return new TypeAdapter<Number>() {
/*     */         public Double read(JsonReader in) throws IOException {
/* 280 */           if (in.peek() == JsonToken.NULL) {
/* 281 */             in.nextNull();
/* 282 */             return null;
/*     */           } 
/* 284 */           return Double.valueOf(in.nextDouble());
/*     */         }
/*     */         public void write(JsonWriter out, Number value) throws IOException {
/* 287 */           if (value == null) {
/* 288 */             out.nullValue();
/*     */             return;
/*     */           } 
/* 291 */           double doubleValue = value.doubleValue();
/* 292 */           Gson.checkValidFloatingPoint(doubleValue);
/* 293 */           out.value(value);
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
/* 299 */     if (serializeSpecialFloatingPointValues) {
/* 300 */       return TypeAdapters.FLOAT;
/*     */     }
/* 302 */     return new TypeAdapter<Number>() {
/*     */         public Float read(JsonReader in) throws IOException {
/* 304 */           if (in.peek() == JsonToken.NULL) {
/* 305 */             in.nextNull();
/* 306 */             return null;
/*     */           } 
/* 308 */           return Float.valueOf((float)in.nextDouble());
/*     */         }
/*     */         public void write(JsonWriter out, Number value) throws IOException {
/* 311 */           if (value == null) {
/* 312 */             out.nullValue();
/*     */             return;
/*     */           } 
/* 315 */           float floatValue = value.floatValue();
/* 316 */           Gson.checkValidFloatingPoint(floatValue);
/* 317 */           out.value(value);
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   static void checkValidFloatingPoint(double value) {
/* 323 */     if (Double.isNaN(value) || Double.isInfinite(value)) {
/* 324 */       throw new IllegalArgumentException(value + " is not a valid double value as per JSON specification. To override this behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy) {
/* 331 */     if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
/* 332 */       return TypeAdapters.LONG;
/*     */     }
/* 334 */     return new TypeAdapter<Number>() {
/*     */         public Number read(JsonReader in) throws IOException {
/* 336 */           if (in.peek() == JsonToken.NULL) {
/* 337 */             in.nextNull();
/* 338 */             return null;
/*     */           } 
/* 340 */           return Long.valueOf(in.nextLong());
/*     */         }
/*     */         public void write(JsonWriter out, Number value) throws IOException {
/* 343 */           if (value == null) {
/* 344 */             out.nullValue();
/*     */             return;
/*     */           } 
/* 347 */           out.value(value.toString());
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   private static TypeAdapter<AtomicLong> atomicLongAdapter(final TypeAdapter<Number> longAdapter) {
/* 353 */     return (new TypeAdapter<AtomicLong>() {
/*     */         public void write(JsonWriter out, AtomicLong value) throws IOException {
/* 355 */           longAdapter.write(out, Long.valueOf(value.get()));
/*     */         }
/*     */         public AtomicLong read(JsonReader in) throws IOException {
/* 358 */           Number value = longAdapter.read(in);
/* 359 */           return new AtomicLong(value.longValue());
/*     */         }
/* 361 */       }).nullSafe();
/*     */   }
/*     */   
/*     */   private static TypeAdapter<AtomicLongArray> atomicLongArrayAdapter(final TypeAdapter<Number> longAdapter) {
/* 365 */     return (new TypeAdapter<AtomicLongArray>() {
/*     */         public void write(JsonWriter out, AtomicLongArray value) throws IOException {
/* 367 */           out.beginArray();
/* 368 */           for (int i = 0, length = value.length(); i < length; i++) {
/* 369 */             longAdapter.write(out, Long.valueOf(value.get(i)));
/*     */           }
/* 371 */           out.endArray();
/*     */         }
/*     */         public AtomicLongArray read(JsonReader in) throws IOException {
/* 374 */           List<Long> list = new ArrayList<Long>();
/* 375 */           in.beginArray();
/* 376 */           while (in.hasNext()) {
/* 377 */             long value = ((Number)longAdapter.read(in)).longValue();
/* 378 */             list.add(Long.valueOf(value));
/*     */           } 
/* 380 */           in.endArray();
/* 381 */           int length = list.size();
/* 382 */           AtomicLongArray array = new AtomicLongArray(length);
/* 383 */           for (int i = 0; i < length; i++) {
/* 384 */             array.set(i, ((Long)list.get(i)).longValue());
/*     */           }
/* 386 */           return array;
/*     */         }
/* 388 */       }).nullSafe();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
/* 399 */     TypeAdapter<?> cached = this.typeTokenCache.get((type == null) ? NULL_KEY_SURROGATE : type);
/* 400 */     if (cached != null) {
/* 401 */       return (TypeAdapter)cached;
/*     */     }
/*     */     
/* 404 */     Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = this.calls.get();
/* 405 */     boolean requiresThreadLocalCleanup = false;
/* 406 */     if (threadCalls == null) {
/* 407 */       threadCalls = new HashMap<TypeToken<?>, FutureTypeAdapter<?>>();
/* 408 */       this.calls.set(threadCalls);
/* 409 */       requiresThreadLocalCleanup = true;
/*     */     } 
/*     */ 
/*     */     
/* 413 */     FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter<T>)threadCalls.get(type);
/* 414 */     if (ongoingCall != null) {
/* 415 */       return ongoingCall;
/*     */     }
/*     */     
/*     */     try {
/* 419 */       FutureTypeAdapter<T> call = new FutureTypeAdapter<T>();
/* 420 */       threadCalls.put(type, call);
/*     */       
/* 422 */       for (TypeAdapterFactory factory : this.factories) {
/* 423 */         TypeAdapter<T> candidate = factory.create(this, type);
/* 424 */         if (candidate != null) {
/* 425 */           call.setDelegate(candidate);
/* 426 */           this.typeTokenCache.put(type, candidate);
/* 427 */           return candidate;
/*     */         } 
/*     */       } 
/* 430 */       throw new IllegalArgumentException("GSON cannot handle " + type);
/*     */     } finally {
/* 432 */       threadCalls.remove(type);
/*     */       
/* 434 */       if (requiresThreadLocalCleanup) {
/* 435 */         this.calls.remove();
/*     */       }
/*     */     } 
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
/*     */     JsonAdapterAnnotationTypeAdapterFactory jsonAdapterAnnotationTypeAdapterFactory;
/* 493 */     if (!this.factories.contains(skipPast)) {
/* 494 */       jsonAdapterAnnotationTypeAdapterFactory = this.jsonAdapterFactory;
/*     */     }
/*     */     
/* 497 */     boolean skipPastFound = false;
/* 498 */     for (TypeAdapterFactory factory : this.factories) {
/* 499 */       if (!skipPastFound) {
/* 500 */         if (factory == jsonAdapterAnnotationTypeAdapterFactory) {
/* 501 */           skipPastFound = true;
/*     */         }
/*     */         
/*     */         continue;
/*     */       } 
/* 506 */       TypeAdapter<T> candidate = factory.create(this, type);
/* 507 */       if (candidate != null) {
/* 508 */         return candidate;
/*     */       }
/*     */     } 
/* 511 */     throw new IllegalArgumentException("GSON cannot serialize " + type);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public <T> TypeAdapter<T> getAdapter(Class<T> type) {
/* 521 */     return getAdapter(TypeToken.get(type));
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
/*     */   public JsonElement toJsonTree(Object src) {
/* 538 */     if (src == null) {
/* 539 */       return JsonNull.INSTANCE;
/*     */     }
/* 541 */     return toJsonTree(src, src.getClass());
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
/*     */   public JsonElement toJsonTree(Object src, Type typeOfSrc) {
/* 561 */     JsonTreeWriter writer = new JsonTreeWriter();
/* 562 */     toJson(src, typeOfSrc, (JsonWriter)writer);
/* 563 */     return writer.get();
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
/*     */   public String toJson(Object src) {
/* 580 */     if (src == null) {
/* 581 */       return toJson(JsonNull.INSTANCE);
/*     */     }
/* 583 */     return toJson(src, src.getClass());
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
/*     */   public String toJson(Object src, Type typeOfSrc) {
/* 602 */     StringWriter writer = new StringWriter();
/* 603 */     toJson(src, typeOfSrc, writer);
/* 604 */     return writer.toString();
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
/*     */   public void toJson(Object src, Appendable writer) throws JsonIOException {
/* 622 */     if (src != null) {
/* 623 */       toJson(src, src.getClass(), writer);
/*     */     } else {
/* 625 */       toJson(JsonNull.INSTANCE, writer);
/*     */     } 
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
/*     */   public void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
/*     */     try {
/* 647 */       JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
/* 648 */       toJson(src, typeOfSrc, jsonWriter);
/* 649 */     } catch (IOException e) {
/* 650 */       throw new JsonIOException(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
/* 661 */     TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
/* 662 */     boolean oldLenient = writer.isLenient();
/* 663 */     writer.setLenient(true);
/* 664 */     boolean oldHtmlSafe = writer.isHtmlSafe();
/* 665 */     writer.setHtmlSafe(this.htmlSafe);
/* 666 */     boolean oldSerializeNulls = writer.getSerializeNulls();
/* 667 */     writer.setSerializeNulls(this.serializeNulls);
/*     */     try {
/* 669 */       adapter.write(writer, src);
/* 670 */     } catch (IOException e) {
/* 671 */       throw new JsonIOException(e);
/*     */     } finally {
/* 673 */       writer.setLenient(oldLenient);
/* 674 */       writer.setHtmlSafe(oldHtmlSafe);
/* 675 */       writer.setSerializeNulls(oldSerializeNulls);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String toJson(JsonElement jsonElement) {
/* 687 */     StringWriter writer = new StringWriter();
/* 688 */     toJson(jsonElement, writer);
/* 689 */     return writer.toString();
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
/*     */   public void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
/*     */     try {
/* 702 */       JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
/* 703 */       toJson(jsonElement, jsonWriter);
/* 704 */     } catch (IOException e) {
/* 705 */       throw new JsonIOException(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JsonWriter newJsonWriter(Writer writer) throws IOException {
/* 713 */     if (this.generateNonExecutableJson) {
/* 714 */       writer.write(")]}'\n");
/*     */     }
/* 716 */     JsonWriter jsonWriter = new JsonWriter(writer);
/* 717 */     if (this.prettyPrinting) {
/* 718 */       jsonWriter.setIndent("  ");
/*     */     }
/* 720 */     jsonWriter.setSerializeNulls(this.serializeNulls);
/* 721 */     return jsonWriter;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JsonReader newJsonReader(Reader reader) {
/* 728 */     JsonReader jsonReader = new JsonReader(reader);
/* 729 */     jsonReader.setLenient(this.lenient);
/* 730 */     return jsonReader;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException {
/* 738 */     boolean oldLenient = writer.isLenient();
/* 739 */     writer.setLenient(true);
/* 740 */     boolean oldHtmlSafe = writer.isHtmlSafe();
/* 741 */     writer.setHtmlSafe(this.htmlSafe);
/* 742 */     boolean oldSerializeNulls = writer.getSerializeNulls();
/* 743 */     writer.setSerializeNulls(this.serializeNulls);
/*     */     try {
/* 745 */       Streams.write(jsonElement, writer);
/* 746 */     } catch (IOException e) {
/* 747 */       throw new JsonIOException(e);
/*     */     } finally {
/* 749 */       writer.setLenient(oldLenient);
/* 750 */       writer.setHtmlSafe(oldHtmlSafe);
/* 751 */       writer.setSerializeNulls(oldSerializeNulls);
/*     */     } 
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
/*     */   public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
/* 773 */     Object object = fromJson(json, classOfT);
/* 774 */     return Primitives.wrap(classOfT).cast(object);
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
/*     */   public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
/* 797 */     if (json == null) {
/* 798 */       return null;
/*     */     }
/* 800 */     StringReader reader = new StringReader(json);
/* 801 */     T target = fromJson(reader, typeOfT);
/* 802 */     return target;
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
/*     */   public <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
/* 824 */     JsonReader jsonReader = newJsonReader(json);
/* 825 */     Object object = fromJson(jsonReader, classOfT);
/* 826 */     assertFullConsumption(object, jsonReader);
/* 827 */     return Primitives.wrap(classOfT).cast(object);
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
/*     */   public <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
/* 851 */     JsonReader jsonReader = newJsonReader(json);
/* 852 */     T object = fromJson(jsonReader, typeOfT);
/* 853 */     assertFullConsumption(object, jsonReader);
/* 854 */     return object;
/*     */   }
/*     */   
/*     */   private static void assertFullConsumption(Object obj, JsonReader reader) {
/*     */     try {
/* 859 */       if (obj != null && reader.peek() != JsonToken.END_DOCUMENT) {
/* 860 */         throw new JsonIOException("JSON document was not fully consumed.");
/*     */       }
/* 862 */     } catch (MalformedJsonException e) {
/* 863 */       throw new JsonSyntaxException(e);
/* 864 */     } catch (IOException e) {
/* 865 */       throw new JsonIOException(e);
/*     */     } 
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
/*     */   public <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
/* 879 */     boolean isEmpty = true;
/* 880 */     boolean oldLenient = reader.isLenient();
/* 881 */     reader.setLenient(true);
/*     */     try {
/* 883 */       reader.peek();
/* 884 */       isEmpty = false;
/* 885 */       TypeToken<T> typeToken = TypeToken.get(typeOfT);
/* 886 */       TypeAdapter<T> typeAdapter = getAdapter(typeToken);
/* 887 */       T object = typeAdapter.read(reader);
/* 888 */       return object;
/* 889 */     } catch (EOFException e) {
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 894 */       if (isEmpty) {
/* 895 */         return null;
/*     */       }
/* 897 */       throw new JsonSyntaxException(e);
/* 898 */     } catch (IllegalStateException e) {
/* 899 */       throw new JsonSyntaxException(e);
/* 900 */     } catch (IOException e) {
/*     */       
/* 902 */       throw new JsonSyntaxException(e);
/*     */     } finally {
/* 904 */       reader.setLenient(oldLenient);
/*     */     } 
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
/*     */   public <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
/* 925 */     Object object = fromJson(json, classOfT);
/* 926 */     return Primitives.wrap(classOfT).cast(object);
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
/*     */   public <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
/* 949 */     if (json == null) {
/* 950 */       return null;
/*     */     }
/* 952 */     return fromJson((JsonReader)new JsonTreeReader(json), typeOfT);
/*     */   }
/*     */   
/*     */   static class FutureTypeAdapter<T> extends TypeAdapter<T> {
/*     */     private TypeAdapter<T> delegate;
/*     */     
/*     */     public void setDelegate(TypeAdapter<T> typeAdapter) {
/* 959 */       if (this.delegate != null) {
/* 960 */         throw new AssertionError();
/*     */       }
/* 962 */       this.delegate = typeAdapter;
/*     */     }
/*     */     
/*     */     public T read(JsonReader in) throws IOException {
/* 966 */       if (this.delegate == null) {
/* 967 */         throw new IllegalStateException();
/*     */       }
/* 969 */       return this.delegate.read(in);
/*     */     }
/*     */     
/*     */     public void write(JsonWriter out, T value) throws IOException {
/* 973 */       if (this.delegate == null) {
/* 974 */         throw new IllegalStateException();
/*     */       }
/* 976 */       this.delegate.write(out, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public String toString() {
/* 982 */     return "{serializeNulls:" + this.serializeNulls + 
/* 983 */       "factories:" + 
/* 984 */       this.factories + ",instanceCreators:" + 
/* 985 */       this.constructorConstructor + "}";
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\com\google\gson\Gson.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */