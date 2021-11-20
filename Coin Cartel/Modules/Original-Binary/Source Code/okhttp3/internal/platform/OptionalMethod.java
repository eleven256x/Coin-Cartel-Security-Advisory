/*     */ package okhttp3.internal.platform;
/*     */ 
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class OptionalMethod<T>
/*     */ {
/*     */   private final Class<?> returnType;
/*     */   private final String methodName;
/*     */   private final Class[] methodParams;
/*     */   
/*     */   OptionalMethod(Class<?> returnType, String methodName, Class... methodParams) {
/*  46 */     this.returnType = returnType;
/*  47 */     this.methodName = methodName;
/*  48 */     this.methodParams = methodParams;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isSupported(T target) {
/*  55 */     return (getMethod(target.getClass()) != null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Object invokeOptional(T target, Object... args) throws InvocationTargetException {
/*  66 */     Method m = getMethod(target.getClass());
/*  67 */     if (m == null) {
/*  68 */       return null;
/*     */     }
/*     */     try {
/*  71 */       return m.invoke(target, args);
/*  72 */     } catch (IllegalAccessException e) {
/*  73 */       return null;
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
/*     */   public Object invokeOptionalWithoutCheckedException(T target, Object... args) {
/*     */     try {
/*  86 */       return invokeOptional(target, args);
/*  87 */     } catch (InvocationTargetException e) {
/*  88 */       Throwable targetException = e.getTargetException();
/*  89 */       if (targetException instanceof RuntimeException) {
/*  90 */         throw (RuntimeException)targetException;
/*     */       }
/*  92 */       AssertionError error = new AssertionError("Unexpected exception");
/*  93 */       error.initCause(targetException);
/*  94 */       throw error;
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
/*     */   public Object invoke(T target, Object... args) throws InvocationTargetException {
/* 106 */     Method m = getMethod(target.getClass());
/* 107 */     if (m == null) {
/* 108 */       throw new AssertionError("Method " + this.methodName + " not supported for object " + target);
/*     */     }
/*     */     try {
/* 111 */       return m.invoke(target, args);
/* 112 */     } catch (IllegalAccessException e) {
/*     */       
/* 114 */       AssertionError error = new AssertionError("Unexpectedly could not call: " + m);
/* 115 */       error.initCause(e);
/* 116 */       throw error;
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
/*     */   public Object invokeWithoutCheckedException(T target, Object... args) {
/*     */     try {
/* 129 */       return invoke(target, args);
/* 130 */     } catch (InvocationTargetException e) {
/* 131 */       Throwable targetException = e.getTargetException();
/* 132 */       if (targetException instanceof RuntimeException) {
/* 133 */         throw (RuntimeException)targetException;
/*     */       }
/* 135 */       AssertionError error = new AssertionError("Unexpected exception");
/* 136 */       error.initCause(targetException);
/* 137 */       throw error;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Method getMethod(Class<?> clazz) {
/* 147 */     Method method = null;
/* 148 */     if (this.methodName != null) {
/* 149 */       method = getPublicMethod(clazz, this.methodName, this.methodParams);
/* 150 */       if (method != null && this.returnType != null && 
/*     */         
/* 152 */         !this.returnType.isAssignableFrom(method.getReturnType()))
/*     */       {
/*     */         
/* 155 */         method = null;
/*     */       }
/*     */     } 
/* 158 */     return method;
/*     */   }
/*     */   
/*     */   private static Method getPublicMethod(Class<?> clazz, String methodName, Class[] parameterTypes) {
/* 162 */     Method method = null;
/*     */     try {
/* 164 */       method = clazz.getMethod(methodName, parameterTypes);
/* 165 */       if ((method.getModifiers() & 0x1) == 0) {
/* 166 */         method = null;
/*     */       }
/* 168 */     } catch (NoSuchMethodException noSuchMethodException) {}
/*     */ 
/*     */     
/* 171 */     return method;
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\OptionalMethod.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */