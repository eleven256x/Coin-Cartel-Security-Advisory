/*     */ package com.google.gson.internal;
/*     */ 
/*     */ import com.google.gson.InstanceCreator;
/*     */ import com.google.gson.JsonIOException;
/*     */ import com.google.gson.reflect.TypeToken;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.ParameterizedType;
/*     */ import java.lang.reflect.Type;
/*     */ import java.util.ArrayDeque;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.EnumSet;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.LinkedHashSet;
/*     */ import java.util.Map;
/*     */ import java.util.Queue;
/*     */ import java.util.Set;
/*     */ import java.util.SortedMap;
/*     */ import java.util.SortedSet;
/*     */ import java.util.TreeMap;
/*     */ import java.util.TreeSet;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.ConcurrentMap;
/*     */ import java.util.concurrent.ConcurrentNavigableMap;
/*     */ import java.util.concurrent.ConcurrentSkipListMap;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class ConstructorConstructor
/*     */ {
/*     */   private final Map<Type, InstanceCreator<?>> instanceCreators;
/*     */   
/*     */   public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators) {
/*  52 */     this.instanceCreators = instanceCreators;
/*     */   }
/*     */   
/*     */   public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
/*  56 */     final Type type = typeToken.getType();
/*  57 */     Class<? super T> rawType = typeToken.getRawType();
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  62 */     final InstanceCreator<T> typeCreator = (InstanceCreator<T>)this.instanceCreators.get(type);
/*  63 */     if (typeCreator != null) {
/*  64 */       return new ObjectConstructor<T>() {
/*     */           public T construct() {
/*  66 */             return (T)typeCreator.createInstance(type);
/*     */           }
/*     */         };
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  74 */     final InstanceCreator<T> rawTypeCreator = (InstanceCreator<T>)this.instanceCreators.get(rawType);
/*  75 */     if (rawTypeCreator != null) {
/*  76 */       return new ObjectConstructor<T>() {
/*     */           public T construct() {
/*  78 */             return (T)rawTypeCreator.createInstance(type);
/*     */           }
/*     */         };
/*     */     }
/*     */     
/*  83 */     ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
/*  84 */     if (defaultConstructor != null) {
/*  85 */       return defaultConstructor;
/*     */     }
/*     */     
/*  88 */     ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
/*  89 */     if (defaultImplementation != null) {
/*  90 */       return defaultImplementation;
/*     */     }
/*     */ 
/*     */     
/*  94 */     return newUnsafeAllocator(type, rawType);
/*     */   }
/*     */   
/*     */   private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType) {
/*     */     try {
/*  99 */       final Constructor<? super T> constructor = rawType.getDeclaredConstructor(new Class[0]);
/* 100 */       if (!constructor.isAccessible()) {
/* 101 */         constructor.setAccessible(true);
/*     */       }
/* 103 */       return new ObjectConstructor<T>()
/*     */         {
/*     */           public T construct() {
/*     */             try {
/* 107 */               Object[] args = null;
/* 108 */               return constructor.newInstance(args);
/* 109 */             } catch (InstantiationException e) {
/*     */               
/* 111 */               throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
/* 112 */             } catch (InvocationTargetException e) {
/*     */ 
/*     */               
/* 115 */               throw new RuntimeException("Failed to invoke " + constructor + " with no args", e
/* 116 */                   .getTargetException());
/* 117 */             } catch (IllegalAccessException e) {
/* 118 */               throw new AssertionError(e);
/*     */             } 
/*     */           }
/*     */         };
/* 122 */     } catch (NoSuchMethodException e) {
/* 123 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private <T> ObjectConstructor<T> newDefaultImplementationConstructor(final Type type, Class<? super T> rawType) {
/* 134 */     if (Collection.class.isAssignableFrom(rawType)) {
/* 135 */       if (SortedSet.class.isAssignableFrom(rawType))
/* 136 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 138 */               return (T)new TreeSet();
/*     */             }
/*     */           }; 
/* 141 */       if (EnumSet.class.isAssignableFrom(rawType)) {
/* 142 */         return new ObjectConstructor<T>()
/*     */           {
/*     */             public T construct() {
/* 145 */               if (type instanceof ParameterizedType) {
/* 146 */                 Type elementType = ((ParameterizedType)type).getActualTypeArguments()[0];
/* 147 */                 if (elementType instanceof Class) {
/* 148 */                   return (T)EnumSet.noneOf((Class<Enum>)elementType);
/*     */                 }
/* 150 */                 throw new JsonIOException("Invalid EnumSet type: " + type.toString());
/*     */               } 
/*     */               
/* 153 */               throw new JsonIOException("Invalid EnumSet type: " + type.toString());
/*     */             }
/*     */           };
/*     */       }
/* 157 */       if (Set.class.isAssignableFrom(rawType))
/* 158 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 160 */               return (T)new LinkedHashSet();
/*     */             }
/*     */           }; 
/* 163 */       if (Queue.class.isAssignableFrom(rawType)) {
/* 164 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 166 */               return (T)new ArrayDeque();
/*     */             }
/*     */           };
/*     */       }
/* 170 */       return new ObjectConstructor<T>() {
/*     */           public T construct() {
/* 172 */             return (T)new ArrayList();
/*     */           }
/*     */         };
/*     */     } 
/*     */ 
/*     */     
/* 178 */     if (Map.class.isAssignableFrom(rawType)) {
/* 179 */       if (ConcurrentNavigableMap.class.isAssignableFrom(rawType))
/* 180 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 182 */               return (T)new ConcurrentSkipListMap<Object, Object>();
/*     */             }
/*     */           }; 
/* 185 */       if (ConcurrentMap.class.isAssignableFrom(rawType))
/* 186 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 188 */               return (T)new ConcurrentHashMap<Object, Object>();
/*     */             }
/*     */           }; 
/* 191 */       if (SortedMap.class.isAssignableFrom(rawType))
/* 192 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 194 */               return (T)new TreeMap<Object, Object>();
/*     */             }
/*     */           }; 
/* 197 */       if (type instanceof ParameterizedType && !String.class.isAssignableFrom(
/* 198 */           TypeToken.get(((ParameterizedType)type).getActualTypeArguments()[0]).getRawType())) {
/* 199 */         return new ObjectConstructor<T>() {
/*     */             public T construct() {
/* 201 */               return (T)new LinkedHashMap<Object, Object>();
/*     */             }
/*     */           };
/*     */       }
/* 205 */       return new ObjectConstructor<T>() {
/*     */           public T construct() {
/* 207 */             return (T)new LinkedTreeMap<Object, Object>();
/*     */           }
/*     */         };
/*     */     } 
/*     */ 
/*     */     
/* 213 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private <T> ObjectConstructor<T> newUnsafeAllocator(final Type type, final Class<? super T> rawType) {
/* 218 */     return new ObjectConstructor<T>() {
/* 219 */         private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
/*     */         
/*     */         public T construct() {
/*     */           try {
/* 223 */             Object newInstance = this.unsafeAllocator.newInstance(rawType);
/* 224 */             return (T)newInstance;
/* 225 */           } catch (Exception e) {
/* 226 */             throw new RuntimeException("Unable to invoke no-args constructor for " + type + ". Register an InstanceCreator with Gson for this type may fix this problem.", e);
/*     */           } 
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */   
/*     */   public String toString() {
/* 234 */     return this.instanceCreators.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\com\google\gson\internal\ConstructorConstructor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */