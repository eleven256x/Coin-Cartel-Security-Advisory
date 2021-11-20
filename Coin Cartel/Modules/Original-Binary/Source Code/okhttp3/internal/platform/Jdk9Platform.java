/*     */ package okhttp3.internal.platform;
/*     */ 
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLParameters;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import javax.net.ssl.X509TrustManager;
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
/*     */ final class Jdk9Platform
/*     */   extends Platform
/*     */ {
/*     */   final Method setProtocolMethod;
/*     */   final Method getProtocolMethod;
/*     */   
/*     */   Jdk9Platform(Method setProtocolMethod, Method getProtocolMethod) {
/*  38 */     this.setProtocolMethod = setProtocolMethod;
/*  39 */     this.getProtocolMethod = getProtocolMethod;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
/*     */     try {
/*  46 */       SSLParameters sslParameters = sslSocket.getSSLParameters();
/*     */       
/*  48 */       List<String> names = alpnProtocolNames(protocols);
/*     */       
/*  50 */       this.setProtocolMethod.invoke(sslParameters, new Object[] { names
/*  51 */             .toArray(new String[names.size()]) });
/*     */       
/*  53 */       sslSocket.setSSLParameters(sslParameters);
/*  54 */     } catch (IllegalAccessException|InvocationTargetException e) {
/*  55 */       throw Util.assertionError("unable to set ssl parameters", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   public String getSelectedProtocol(SSLSocket socket) {
/*     */     try {
/*  62 */       String protocol = (String)this.getProtocolMethod.invoke(socket, new Object[0]);
/*     */ 
/*     */ 
/*     */       
/*  66 */       if (protocol == null || protocol.equals("")) {
/*  67 */         return null;
/*     */       }
/*     */       
/*  70 */       return protocol;
/*  71 */     } catch (InvocationTargetException e) {
/*  72 */       if (e.getCause() instanceof UnsupportedOperationException)
/*     */       {
/*     */         
/*  75 */         return null;
/*     */       }
/*     */       
/*  78 */       throw Util.assertionError("failed to get ALPN selected protocol", e);
/*  79 */     } catch (IllegalAccessException e) {
/*  80 */       throw Util.assertionError("failed to get ALPN selected protocol", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
/*  89 */     throw new UnsupportedOperationException("clientBuilder.sslSocketFactory(SSLSocketFactory) not supported on JDK 9+");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Jdk9Platform buildIfSupported() {
/*     */     try {
/*  97 */       Method setProtocolMethod = SSLParameters.class.getMethod("setApplicationProtocols", new Class[] { String[].class });
/*  98 */       Method getProtocolMethod = SSLSocket.class.getMethod("getApplicationProtocol", new Class[0]);
/*     */       
/* 100 */       return new Jdk9Platform(setProtocolMethod, getProtocolMethod);
/* 101 */     } catch (NoSuchMethodException noSuchMethodException) {
/*     */ 
/*     */ 
/*     */       
/* 105 */       return null;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\Jdk9Platform.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */