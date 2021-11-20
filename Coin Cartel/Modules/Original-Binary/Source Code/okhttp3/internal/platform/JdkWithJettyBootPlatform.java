/*     */ package okhttp3.internal.platform;
/*     */ 
/*     */ import java.lang.reflect.InvocationHandler;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.lang.reflect.Proxy;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.internal.Util;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class JdkWithJettyBootPlatform
/*     */   extends Platform
/*     */ {
/*     */   private final Method putMethod;
/*     */   private final Method getMethod;
/*     */   private final Method removeMethod;
/*     */   private final Class<?> clientProviderClass;
/*     */   private final Class<?> serverProviderClass;
/*     */   
/*     */   JdkWithJettyBootPlatform(Method putMethod, Method getMethod, Method removeMethod, Class<?> clientProviderClass, Class<?> serverProviderClass) {
/*  42 */     this.putMethod = putMethod;
/*  43 */     this.getMethod = getMethod;
/*  44 */     this.removeMethod = removeMethod;
/*  45 */     this.clientProviderClass = clientProviderClass;
/*  46 */     this.serverProviderClass = serverProviderClass;
/*     */   }
/*     */ 
/*     */   
/*     */   public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
/*  51 */     List<String> names = alpnProtocolNames(protocols);
/*     */     
/*     */     try {
/*  54 */       Object provider = Proxy.newProxyInstance(Platform.class.getClassLoader(), new Class[] { this.clientProviderClass, this.serverProviderClass }, new JettyNegoProvider(names));
/*     */       
/*  56 */       this.putMethod.invoke(null, new Object[] { sslSocket, provider });
/*  57 */     } catch (InvocationTargetException|IllegalAccessException e) {
/*  58 */       throw Util.assertionError("unable to set alpn", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void afterHandshake(SSLSocket sslSocket) {
/*     */     try {
/*  64 */       this.removeMethod.invoke(null, new Object[] { sslSocket });
/*  65 */     } catch (IllegalAccessException|InvocationTargetException e) {
/*  66 */       throw Util.assertionError("unable to remove alpn", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public String getSelectedProtocol(SSLSocket socket) {
/*     */     try {
/*  73 */       JettyNegoProvider provider = (JettyNegoProvider)Proxy.getInvocationHandler(this.getMethod.invoke(null, new Object[] { socket }));
/*  74 */       if (!provider.unsupported && provider.selected == null) {
/*  75 */         Platform.get().log(4, "ALPN callback dropped: HTTP/2 is disabled. Is alpn-boot on the boot class path?", null);
/*     */         
/*  77 */         return null;
/*     */       } 
/*  79 */       return provider.unsupported ? null : provider.selected;
/*  80 */     } catch (InvocationTargetException|IllegalAccessException e) {
/*  81 */       throw Util.assertionError("unable to get selected protocol", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public static Platform buildIfSupported() {
/*     */     try {
/*  88 */       String negoClassName = "org.eclipse.jetty.alpn.ALPN";
/*  89 */       Class<?> negoClass = Class.forName(negoClassName);
/*  90 */       Class<?> providerClass = Class.forName(negoClassName + "$Provider");
/*  91 */       Class<?> clientProviderClass = Class.forName(negoClassName + "$ClientProvider");
/*  92 */       Class<?> serverProviderClass = Class.forName(negoClassName + "$ServerProvider");
/*  93 */       Method putMethod = negoClass.getMethod("put", new Class[] { SSLSocket.class, providerClass });
/*  94 */       Method getMethod = negoClass.getMethod("get", new Class[] { SSLSocket.class });
/*  95 */       Method removeMethod = negoClass.getMethod("remove", new Class[] { SSLSocket.class });
/*  96 */       return new JdkWithJettyBootPlatform(putMethod, getMethod, removeMethod, clientProviderClass, serverProviderClass);
/*     */     }
/*  98 */     catch (ClassNotFoundException|NoSuchMethodException classNotFoundException) {
/*     */ 
/*     */       
/* 101 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private static class JettyNegoProvider
/*     */     implements InvocationHandler
/*     */   {
/*     */     private final List<String> protocols;
/*     */     
/*     */     boolean unsupported;
/*     */     
/*     */     String selected;
/*     */ 
/*     */     
/*     */     JettyNegoProvider(List<String> protocols) {
/* 117 */       this.protocols = protocols;
/*     */     }
/*     */     
/*     */     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
/* 121 */       String arrayOfString[], methodName = method.getName();
/* 122 */       Class<?> returnType = method.getReturnType();
/* 123 */       if (args == null) {
/* 124 */         arrayOfString = Util.EMPTY_STRING_ARRAY;
/*     */       }
/* 126 */       if (methodName.equals("supports") && boolean.class == returnType)
/* 127 */         return Boolean.valueOf(true); 
/* 128 */       if (methodName.equals("unsupported") && void.class == returnType) {
/* 129 */         this.unsupported = true;
/* 130 */         return null;
/* 131 */       }  if (methodName.equals("protocols") && arrayOfString.length == 0)
/* 132 */         return this.protocols; 
/* 133 */       if ((methodName.equals("selectProtocol") || methodName.equals("select")) && String.class == returnType && arrayOfString.length == 1 && arrayOfString[0] instanceof List) {
/*     */         
/* 135 */         List<String> peerProtocols = (List<String>)arrayOfString[0];
/*     */         
/* 137 */         for (int i = 0, size = peerProtocols.size(); i < size; i++) {
/* 138 */           if (this.protocols.contains(peerProtocols.get(i))) {
/* 139 */             return this.selected = peerProtocols.get(i);
/*     */           }
/*     */         } 
/* 142 */         return this.selected = this.protocols.get(0);
/* 143 */       }  if ((methodName.equals("protocolSelected") || methodName.equals("selected")) && arrayOfString.length == 1) {
/*     */         
/* 145 */         this.selected = arrayOfString[0];
/* 146 */         return null;
/*     */       } 
/* 148 */       return method.invoke(this, (Object[])arrayOfString);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\JdkWithJettyBootPlatform.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */