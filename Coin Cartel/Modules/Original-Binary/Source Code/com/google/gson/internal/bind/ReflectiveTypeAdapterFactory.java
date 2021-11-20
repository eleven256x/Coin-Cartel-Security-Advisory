/*     */ package com.google.gson.internal.bind;
/*     */ 
/*     */ import com.google.gson.FieldNamingStrategy;
/*     */ import com.google.gson.Gson;
/*     */ import com.google.gson.JsonSyntaxException;
/*     */ import com.google.gson.TypeAdapter;
/*     */ import com.google.gson.TypeAdapterFactory;
/*     */ import com.google.gson.annotations.JsonAdapter;
/*     */ import com.google.gson.annotations.SerializedName;
/*     */ import com.google.gson.internal.;
/*     */ import com.google.gson.internal.ConstructorConstructor;
/*     */ import com.google.gson.internal.Excluder;
/*     */ import com.google.gson.internal.ObjectConstructor;
/*     */ import com.google.gson.internal.Primitives;
/*     */ import com.google.gson.reflect.TypeToken;
/*     */ import com.google.gson.stream.JsonReader;
/*     */ import com.google.gson.stream.JsonToken;
/*     */ import com.google.gson.stream.JsonWriter;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Type;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class ReflectiveTypeAdapterFactory
/*     */   implements TypeAdapterFactory
/*     */ {
/*     */   private final ConstructorConstructor constructorConstructor;
/*     */   private final FieldNamingStrategy fieldNamingPolicy;
/*     */   private final Excluder excluder;
/*     */   private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
/*     */   
/*     */   public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy, Excluder excluder, JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory) {
/*  56 */     this.constructorConstructor = constructorConstructor;
/*  57 */     this.fieldNamingPolicy = fieldNamingPolicy;
/*  58 */     this.excluder = excluder;
/*  59 */     this.jsonAdapterFactory = jsonAdapterFactory;
/*     */   }
/*     */   
/*     */   public boolean excludeField(Field f, boolean serialize) {
/*  63 */     return excludeField(f, serialize, this.excluder);
/*     */   }
/*     */   
/*     */   static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
/*  67 */     return (!excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize));
/*     */   }
/*     */ 
/*     */   
/*     */   private List<String> getFieldNames(Field f) {
/*  72 */     SerializedName annotation = f.<SerializedName>getAnnotation(SerializedName.class);
/*  73 */     if (annotation == null) {
/*  74 */       String name = this.fieldNamingPolicy.translateName(f);
/*  75 */       return Collections.singletonList(name);
/*     */     } 
/*     */     
/*  78 */     String serializedName = annotation.value();
/*  79 */     String[] alternates = annotation.alternate();
/*  80 */     if (alternates.length == 0) {
/*  81 */       return Collections.singletonList(serializedName);
/*     */     }
/*     */     
/*  84 */     List<String> fieldNames = new ArrayList<String>(alternates.length + 1);
/*  85 */     fieldNames.add(serializedName);
/*  86 */     for (String alternate : alternates) {
/*  87 */       fieldNames.add(alternate);
/*     */     }
/*  89 */     return fieldNames;
/*     */   }
/*     */   
/*     */   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
/*  93 */     Class<? super T> raw = type.getRawType();
/*     */     
/*  95 */     if (!Object.class.isAssignableFrom(raw)) {
/*  96 */       return null;
/*     */     }
/*     */     
/*  99 */     ObjectConstructor<T> constructor = this.constructorConstructor.get(type);
/* 100 */     return new Adapter<T>(constructor, getBoundFields(gson, type, raw));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private BoundField createBoundField(final Gson context, final Field field, String name, final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
/* 106 */     final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
/*     */     
/* 108 */     JsonAdapter annotation = field.<JsonAdapter>getAnnotation(JsonAdapter.class);
/* 109 */     TypeAdapter<?> mapped = null;
/* 110 */     if (annotation != null) {
/* 111 */       mapped = this.jsonAdapterFactory.getTypeAdapter(this.constructorConstructor, context, fieldType, annotation);
/*     */     }
/*     */     
/* 114 */     final boolean jsonAdapterPresent = (mapped != null);
/* 115 */     if (mapped == null) mapped = context.getAdapter(fieldType);
/*     */     
/* 117 */     final TypeAdapter<?> typeAdapter = mapped;
/* 118 */     return new BoundField(name, serialize, deserialize)
/*     */       {
/*     */         void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException
/*     */         {
/* 122 */           Object fieldValue = field.get(value);
/*     */           
/* 124 */           TypeAdapter t = jsonAdapterPresent ? typeAdapter : new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
/* 125 */           t.write(writer, fieldValue);
/*     */         }
/*     */         
/*     */         void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
/* 129 */           Object fieldValue = typeAdapter.read(reader);
/* 130 */           if (fieldValue != null || !isPrimitive)
/* 131 */             field.set(value, fieldValue); 
/*     */         }
/*     */         
/*     */         public boolean writeField(Object value) throws IOException, IllegalAccessException {
/* 135 */           if (!this.serialized) return false; 
/* 136 */           Object fieldValue = field.get(value);
/* 137 */           return (fieldValue != value);
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
/* 143 */     Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
/* 144 */     if (raw.isInterface()) {
/* 145 */       return result;
/*     */     }
/*     */     
/* 148 */     Type declaredType = type.getType();
/* 149 */     while (raw != Object.class) {
/* 150 */       Field[] fields = raw.getDeclaredFields();
/* 151 */       for (Field field : fields) {
/* 152 */         boolean serialize = excludeField(field, true);
/* 153 */         boolean deserialize = excludeField(field, false);
/* 154 */         if (serialize || deserialize) {
/*     */ 
/*     */           
/* 157 */           field.setAccessible(true);
/* 158 */           Type fieldType = .Gson.Types.resolve(type.getType(), raw, field.getGenericType());
/* 159 */           List<String> fieldNames = getFieldNames(field);
/* 160 */           BoundField previous = null;
/* 161 */           for (int i = 0; i < fieldNames.size(); i++) {
/* 162 */             String name = fieldNames.get(i);
/* 163 */             if (i != 0) serialize = false; 
/* 164 */             BoundField boundField = createBoundField(context, field, name, 
/* 165 */                 TypeToken.get(fieldType), serialize, deserialize);
/* 166 */             BoundField replaced = result.put(name, boundField);
/* 167 */             if (previous == null) previous = replaced; 
/*     */           } 
/* 169 */           if (previous != null) {
/* 170 */             throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
/*     */           }
/*     */         } 
/*     */       } 
/* 174 */       type = TypeToken.get(.Gson.Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
/* 175 */       raw = type.getRawType();
/*     */     } 
/* 177 */     return result;
/*     */   }
/*     */   
/*     */   static abstract class BoundField {
/*     */     final String name;
/*     */     final boolean serialized;
/*     */     final boolean deserialized;
/*     */     
/*     */     protected BoundField(String name, boolean serialized, boolean deserialized) {
/* 186 */       this.name = name;
/* 187 */       this.serialized = serialized;
/* 188 */       this.deserialized = deserialized;
/*     */     }
/*     */     
/*     */     abstract boolean writeField(Object param1Object) throws IOException, IllegalAccessException;
/*     */     
/*     */     abstract void write(JsonWriter param1JsonWriter, Object param1Object) throws IOException, IllegalAccessException;
/*     */     
/*     */     abstract void read(JsonReader param1JsonReader, Object param1Object) throws IOException, IllegalAccessException; }
/*     */   
/*     */   public static final class Adapter<T> extends TypeAdapter<T> { private final ObjectConstructor<T> constructor;
/*     */     
/*     */     Adapter(ObjectConstructor<T> constructor, Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields) {
/* 200 */       this.constructor = constructor;
/* 201 */       this.boundFields = boundFields;
/*     */     }
/*     */     private final Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields;
/*     */     public T read(JsonReader in) throws IOException {
/* 205 */       if (in.peek() == JsonToken.NULL) {
/* 206 */         in.nextNull();
/* 207 */         return null;
/*     */       } 
/*     */       
/* 210 */       T instance = (T)this.constructor.construct();
/*     */       
/*     */       try {
/* 213 */         in.beginObject();
/* 214 */         while (in.hasNext()) {
/* 215 */           String name = in.nextName();
/* 216 */           ReflectiveTypeAdapterFactory.BoundField field = this.boundFields.get(name);
/* 217 */           if (field == null || !field.deserialized) {
/* 218 */             in.skipValue(); continue;
/*     */           } 
/* 220 */           field.read(in, instance);
/*     */         }
/*     */       
/* 223 */       } catch (IllegalStateException e) {
/* 224 */         throw new JsonSyntaxException(e);
/* 225 */       } catch (IllegalAccessException e) {
/* 226 */         throw new AssertionError(e);
/*     */       } 
/* 228 */       in.endObject();
/* 229 */       return instance;
/*     */     }
/*     */     
/*     */     public void write(JsonWriter out, T value) throws IOException {
/* 233 */       if (value == null) {
/* 234 */         out.nullValue();
/*     */         
/*     */         return;
/*     */       } 
/* 238 */       out.beginObject();
/*     */       try {
/* 240 */         for (ReflectiveTypeAdapterFactory.BoundField boundField : this.boundFields.values()) {
/* 241 */           if (boundField.writeField(value)) {
/* 242 */             out.name(boundField.name);
/* 243 */             boundField.write(out, value);
/*     */           } 
/*     */         } 
/* 246 */       } catch (IllegalAccessException e) {
/* 247 */         throw new AssertionError(e);
/*     */       } 
/* 249 */       out.endObject();
/*     */     } }
/*     */ 
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\com\google\gson\internal\bind\ReflectiveTypeAdapterFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */