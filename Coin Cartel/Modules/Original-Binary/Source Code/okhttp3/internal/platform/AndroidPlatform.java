/*     */ package okhttp3.internal.platform;
/*     */ 
/*     */ import android.os.Build;
/*     */ import android.util.Log;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.net.Socket;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import java.security.Security;
/*     */ import java.security.cert.Certificate;
/*     */ import java.security.cert.TrustAnchor;
/*     */ import java.security.cert.X509Certificate;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLContext;
/*     */ import javax.net.ssl.SSLPeerUnverifiedException;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import javax.net.ssl.X509TrustManager;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.tls.CertificateChainCleaner;
/*     */ import okhttp3.internal.tls.TrustRootIndex;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class AndroidPlatform
/*     */   extends Platform
/*     */ {
/*     */   private static final int MAX_LOG_LENGTH = 4000;
/*     */   private final Class<?> sslParametersClass;
/*     */   private final OptionalMethod<Socket> setUseSessionTickets;
/*     */   private final OptionalMethod<Socket> setHostname;
/*     */   private final OptionalMethod<Socket> getAlpnSelectedProtocol;
/*     */   private final OptionalMethod<Socket> setAlpnProtocols;
/*  58 */   private final CloseGuard closeGuard = CloseGuard.get();
/*     */ 
/*     */ 
/*     */   
/*     */   AndroidPlatform(Class<?> sslParametersClass, OptionalMethod<Socket> setUseSessionTickets, OptionalMethod<Socket> setHostname, OptionalMethod<Socket> getAlpnSelectedProtocol, OptionalMethod<Socket> setAlpnProtocols) {
/*  63 */     this.sslParametersClass = sslParametersClass;
/*  64 */     this.setUseSessionTickets = setUseSessionTickets;
/*  65 */     this.setHostname = setHostname;
/*  66 */     this.getAlpnSelectedProtocol = getAlpnSelectedProtocol;
/*  67 */     this.setAlpnProtocols = setAlpnProtocols;
/*     */   }
/*     */ 
/*     */   
/*     */   public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout) throws IOException {
/*     */     try {
/*  73 */       socket.connect(address, connectTimeout);
/*  74 */     } catch (AssertionError e) {
/*  75 */       if (Util.isAndroidGetsocknameError(e)) throw new IOException(e); 
/*  76 */       throw e;
/*  77 */     } catch (SecurityException e) {
/*     */ 
/*     */       
/*  80 */       IOException ioException = new IOException("Exception in connect");
/*  81 */       ioException.initCause(e);
/*  82 */       throw ioException;
/*  83 */     } catch (ClassCastException e) {
/*     */ 
/*     */       
/*  86 */       if (Build.VERSION.SDK_INT == 26) {
/*  87 */         IOException ioException = new IOException("Exception in connect");
/*  88 */         ioException.initCause(e);
/*  89 */         throw ioException;
/*     */       } 
/*  91 */       throw e;
/*     */     } 
/*     */   }
/*     */   
/*     */   @Nullable
/*     */   protected X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
/*  97 */     Object context = readFieldOrNull(sslSocketFactory, this.sslParametersClass, "sslParameters");
/*  98 */     if (context == null) {
/*     */       
/*     */       try {
/*     */         
/* 102 */         Class<?> gmsSslParametersClass = Class.forName("com.google.android.gms.org.conscrypt.SSLParametersImpl", false, sslSocketFactory
/*     */             
/* 104 */             .getClass().getClassLoader());
/* 105 */         context = readFieldOrNull(sslSocketFactory, gmsSslParametersClass, "sslParameters");
/* 106 */       } catch (ClassNotFoundException e) {
/* 107 */         return super.trustManager(sslSocketFactory);
/*     */       } 
/*     */     }
/*     */     
/* 111 */     X509TrustManager x509TrustManager = readFieldOrNull(context, X509TrustManager.class, "x509TrustManager");
/*     */     
/* 113 */     if (x509TrustManager != null) return x509TrustManager;
/*     */     
/* 115 */     return readFieldOrNull(context, X509TrustManager.class, "trustManager");
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) throws IOException {
/* 121 */     if (hostname != null) {
/* 122 */       this.setUseSessionTickets.invokeOptionalWithoutCheckedException(sslSocket, new Object[] { Boolean.valueOf(true) });
/* 123 */       this.setHostname.invokeOptionalWithoutCheckedException(sslSocket, new Object[] { hostname });
/*     */     } 
/*     */ 
/*     */     
/* 127 */     if (this.setAlpnProtocols != null && this.setAlpnProtocols.isSupported(sslSocket)) {
/* 128 */       Object[] parameters = { concatLengthPrefixed(protocols) };
/* 129 */       this.setAlpnProtocols.invokeWithoutCheckedException(sslSocket, parameters);
/*     */     } 
/*     */   }
/*     */   @Nullable
/*     */   public String getSelectedProtocol(SSLSocket socket) {
/* 134 */     if (this.getAlpnSelectedProtocol == null) return null; 
/* 135 */     if (!this.getAlpnSelectedProtocol.isSupported(socket)) return null;
/*     */     
/* 137 */     byte[] alpnResult = (byte[])this.getAlpnSelectedProtocol.invokeWithoutCheckedException(socket, new Object[0]);
/* 138 */     return (alpnResult != null) ? new String(alpnResult, Util.UTF_8) : null;
/*     */   }
/*     */   
/*     */   public void log(int level, String message, @Nullable Throwable t) {
/* 142 */     int logLevel = (level == 5) ? 5 : 3;
/* 143 */     if (t != null) message = message + '\n' + Log.getStackTraceString(t);
/*     */ 
/*     */     
/* 146 */     for (int i = 0, length = message.length(); i < length; ) {
/* 147 */       int newline = message.indexOf('\n', i);
/* 148 */       newline = (newline != -1) ? newline : length;
/*     */       while (true) {
/* 150 */         int end = Math.min(newline, i + 4000);
/* 151 */         Log.println(logLevel, "OkHttp", message.substring(i, end));
/* 152 */         i = end;
/* 153 */         if (i >= newline)
/*     */           i++; 
/*     */       } 
/*     */     } 
/*     */   } public Object getStackTraceForCloseable(String closer) {
/* 158 */     return this.closeGuard.createAndOpen(closer);
/*     */   }
/*     */   
/*     */   public void logCloseableLeak(String message, Object stackTrace) {
/* 162 */     boolean reported = this.closeGuard.warnIfOpen(stackTrace);
/* 163 */     if (!reported)
/*     */     {
/* 165 */       log(5, message, (Throwable)null);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isCleartextTrafficPermitted(String hostname) {
/* 172 */     if (Build.VERSION.SDK_INT < 23) {
/* 173 */       return super.isCleartextTrafficPermitted(hostname);
/*     */     }
/*     */     try {
/* 176 */       Class<?> networkPolicyClass = Class.forName("android.security.NetworkSecurityPolicy");
/* 177 */       Method getInstanceMethod = networkPolicyClass.getMethod("getInstance", new Class[0]);
/* 178 */       Object networkSecurityPolicy = getInstanceMethod.invoke((Object)null, new Object[0]);
/* 179 */       return api24IsCleartextTrafficPermitted(hostname, networkPolicyClass, networkSecurityPolicy);
/* 180 */     } catch (ClassNotFoundException|NoSuchMethodException e) {
/* 181 */       return super.isCleartextTrafficPermitted(hostname);
/* 182 */     } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
/* 183 */       throw Util.assertionError("unable to determine cleartext support", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean api24IsCleartextTrafficPermitted(String hostname, Class<?> networkPolicyClass, Object networkSecurityPolicy) throws InvocationTargetException, IllegalAccessException {
/*     */     try {
/* 191 */       Method isCleartextTrafficPermittedMethod = networkPolicyClass.getMethod("isCleartextTrafficPermitted", new Class[] { String.class });
/* 192 */       return ((Boolean)isCleartextTrafficPermittedMethod.invoke(networkSecurityPolicy, new Object[] { hostname })).booleanValue();
/* 193 */     } catch (NoSuchMethodException e) {
/* 194 */       return api23IsCleartextTrafficPermitted(hostname, networkPolicyClass, networkSecurityPolicy);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean api23IsCleartextTrafficPermitted(String hostname, Class<?> networkPolicyClass, Object networkSecurityPolicy) throws InvocationTargetException, IllegalAccessException {
/*     */     try {
/* 202 */       Method isCleartextTrafficPermittedMethod = networkPolicyClass.getMethod("isCleartextTrafficPermitted", new Class[0]);
/* 203 */       return ((Boolean)isCleartextTrafficPermittedMethod.invoke(networkSecurityPolicy, new Object[0])).booleanValue();
/* 204 */     } catch (NoSuchMethodException e) {
/* 205 */       return super.isCleartextTrafficPermitted(hostname);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static boolean supportsAlpn() {
/* 215 */     if (Security.getProvider("GMSCore_OpenSSL") != null) {
/* 216 */       return true;
/*     */     }
/*     */     try {
/* 219 */       Class.forName("android.net.Network");
/* 220 */       return true;
/* 221 */     } catch (ClassNotFoundException classNotFoundException) {
/*     */       
/* 223 */       return false;
/*     */     } 
/*     */   }
/*     */   public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
/*     */     try {
/* 228 */       Class<?> extensionsClass = Class.forName("android.net.http.X509TrustManagerExtensions");
/* 229 */       Constructor<?> constructor = extensionsClass.getConstructor(new Class[] { X509TrustManager.class });
/* 230 */       Object extensions = constructor.newInstance(new Object[] { trustManager });
/* 231 */       Method checkServerTrusted = extensionsClass.getMethod("checkServerTrusted", new Class[] { X509Certificate[].class, String.class, String.class });
/*     */       
/* 233 */       return new AndroidCertificateChainCleaner(extensions, checkServerTrusted);
/* 234 */     } catch (Exception e) {
/* 235 */       return super.buildCertificateChainCleaner(trustManager);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static Platform buildIfSupported() {
/* 240 */     if (!Platform.isAndroid()) {
/* 241 */       return null;
/*     */     }
/*     */     
/*     */     try {
/*     */       Class<?> sslParametersClass;
/*     */       
/*     */       try {
/* 248 */         sslParametersClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
/* 249 */       } catch (ClassNotFoundException e) {
/*     */         
/* 251 */         sslParametersClass = Class.forName("org.apache.harmony.xnet.provider.jsse.SSLParametersImpl");
/*     */       } 
/*     */ 
/*     */       
/* 255 */       OptionalMethod<Socket> setUseSessionTickets = new OptionalMethod<>(null, "setUseSessionTickets", new Class[] { boolean.class });
/*     */       
/* 257 */       OptionalMethod<Socket> setHostname = new OptionalMethod<>(null, "setHostname", new Class[] { String.class });
/*     */       
/* 259 */       OptionalMethod<Socket> getAlpnSelectedProtocol = null;
/* 260 */       OptionalMethod<Socket> setAlpnProtocols = null;
/*     */       
/* 262 */       if (supportsAlpn()) {
/* 263 */         getAlpnSelectedProtocol = new OptionalMethod<>(byte[].class, "getAlpnSelectedProtocol", new Class[0]);
/*     */         
/* 265 */         setAlpnProtocols = new OptionalMethod<>(null, "setAlpnProtocols", new Class[] { byte[].class });
/*     */       } 
/*     */ 
/*     */       
/* 269 */       return new AndroidPlatform(sslParametersClass, setUseSessionTickets, setHostname, getAlpnSelectedProtocol, setAlpnProtocols);
/*     */     }
/* 271 */     catch (ClassNotFoundException classNotFoundException) {
/*     */ 
/*     */ 
/*     */       
/* 275 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public TrustRootIndex buildTrustRootIndex(X509TrustManager trustManager) {
/*     */     try {
/* 284 */       Method method = trustManager.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", new Class[] { X509Certificate.class });
/*     */       
/* 286 */       method.setAccessible(true);
/* 287 */       return new AndroidTrustRootIndex(trustManager, method);
/* 288 */     } catch (NoSuchMethodException e) {
/* 289 */       return super.buildTrustRootIndex(trustManager);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   static final class AndroidCertificateChainCleaner
/*     */     extends CertificateChainCleaner
/*     */   {
/*     */     private final Object x509TrustManagerExtensions;
/*     */     
/*     */     private final Method checkServerTrusted;
/*     */ 
/*     */     
/*     */     AndroidCertificateChainCleaner(Object x509TrustManagerExtensions, Method checkServerTrusted) {
/* 303 */       this.x509TrustManagerExtensions = x509TrustManagerExtensions;
/* 304 */       this.checkServerTrusted = checkServerTrusted;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     public List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException {
/*     */       try {
/* 311 */         X509Certificate[] certificates = chain.<X509Certificate>toArray(new X509Certificate[chain.size()]);
/* 312 */         return (List<Certificate>)this.checkServerTrusted.invoke(this.x509TrustManagerExtensions, new Object[] { certificates, "RSA", hostname });
/*     */       }
/* 314 */       catch (InvocationTargetException e) {
/* 315 */         SSLPeerUnverifiedException exception = new SSLPeerUnverifiedException(e.getMessage());
/* 316 */         exception.initCause(e);
/* 317 */         throw exception;
/* 318 */       } catch (IllegalAccessException e) {
/* 319 */         throw new AssertionError(e);
/*     */       } 
/*     */     }
/*     */     
/*     */     public boolean equals(Object other) {
/* 324 */       return other instanceof AndroidCertificateChainCleaner;
/*     */     }
/*     */     
/*     */     public int hashCode() {
/* 328 */       return 0;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   static final class CloseGuard
/*     */   {
/*     */     private final Method getMethod;
/*     */     
/*     */     private final Method openMethod;
/*     */     
/*     */     private final Method warnIfOpenMethod;
/*     */ 
/*     */     
/*     */     CloseGuard(Method getMethod, Method openMethod, Method warnIfOpenMethod) {
/* 343 */       this.getMethod = getMethod;
/* 344 */       this.openMethod = openMethod;
/* 345 */       this.warnIfOpenMethod = warnIfOpenMethod;
/*     */     }
/*     */     
/*     */     Object createAndOpen(String closer) {
/* 349 */       if (this.getMethod != null) {
/*     */         try {
/* 351 */           Object closeGuardInstance = this.getMethod.invoke((Object)null, new Object[0]);
/* 352 */           this.openMethod.invoke(closeGuardInstance, new Object[] { closer });
/* 353 */           return closeGuardInstance;
/* 354 */         } catch (Exception exception) {}
/*     */       }
/*     */       
/* 357 */       return null;
/*     */     }
/*     */     
/*     */     boolean warnIfOpen(Object closeGuardInstance) {
/* 361 */       boolean reported = false;
/* 362 */       if (closeGuardInstance != null) {
/*     */         try {
/* 364 */           this.warnIfOpenMethod.invoke(closeGuardInstance, new Object[0]);
/* 365 */           reported = true;
/* 366 */         } catch (Exception exception) {}
/*     */       }
/*     */       
/* 369 */       return reported;
/*     */     }
/*     */ 
/*     */     
/*     */     static CloseGuard get() {
/*     */       Method getMethod;
/*     */       Method openMethod;
/*     */       Method warnIfOpenMethod;
/*     */       try {
/* 378 */         Class<?> closeGuardClass = Class.forName("dalvik.system.CloseGuard");
/* 379 */         getMethod = closeGuardClass.getMethod("get", new Class[0]);
/* 380 */         openMethod = closeGuardClass.getMethod("open", new Class[] { String.class });
/* 381 */         warnIfOpenMethod = closeGuardClass.getMethod("warnIfOpen", new Class[0]);
/* 382 */       } catch (Exception ignored) {
/* 383 */         getMethod = null;
/* 384 */         openMethod = null;
/* 385 */         warnIfOpenMethod = null;
/*     */       } 
/* 387 */       return new CloseGuard(getMethod, openMethod, warnIfOpenMethod);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static final class AndroidTrustRootIndex
/*     */     implements TrustRootIndex
/*     */   {
/*     */     private final X509TrustManager trustManager;
/*     */ 
/*     */     
/*     */     private final Method findByIssuerAndSignatureMethod;
/*     */ 
/*     */ 
/*     */     
/*     */     AndroidTrustRootIndex(X509TrustManager trustManager, Method findByIssuerAndSignatureMethod) {
/* 405 */       this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod;
/* 406 */       this.trustManager = trustManager;
/*     */     }
/*     */     
/*     */     public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
/*     */       try {
/* 411 */         TrustAnchor trustAnchor = (TrustAnchor)this.findByIssuerAndSignatureMethod.invoke(this.trustManager, new Object[] { cert });
/*     */         
/* 413 */         return (trustAnchor != null) ? 
/* 414 */           trustAnchor.getTrustedCert() : 
/* 415 */           null;
/* 416 */       } catch (IllegalAccessException e) {
/* 417 */         throw Util.assertionError("unable to get issues and signature", e);
/* 418 */       } catch (InvocationTargetException e) {
/* 419 */         return null;
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean equals(Object obj) {
/* 425 */       if (obj == this) {
/* 426 */         return true;
/*     */       }
/* 428 */       if (!(obj instanceof AndroidTrustRootIndex)) {
/* 429 */         return false;
/*     */       }
/* 431 */       AndroidTrustRootIndex that = (AndroidTrustRootIndex)obj;
/* 432 */       return (this.trustManager.equals(that.trustManager) && this.findByIssuerAndSignatureMethod
/* 433 */         .equals(that.findByIssuerAndSignatureMethod));
/*     */     }
/*     */ 
/*     */     
/*     */     public int hashCode() {
/* 438 */       return this.trustManager.hashCode() + 31 * this.findByIssuerAndSignatureMethod.hashCode();
/*     */     }
/*     */   }
/*     */   
/*     */   public SSLContext getSSLContext() {
/*     */     boolean tryTls12;
/*     */     try {
/* 445 */       tryTls12 = (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22);
/* 446 */     } catch (NoClassDefFoundError e) {
/*     */ 
/*     */       
/* 449 */       tryTls12 = true;
/*     */     } 
/*     */     
/* 452 */     if (tryTls12) {
/*     */       try {
/* 454 */         return SSLContext.getInstance("TLSv1.2");
/* 455 */       } catch (NoSuchAlgorithmException noSuchAlgorithmException) {}
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 461 */       return SSLContext.getInstance("TLS");
/* 462 */     } catch (NoSuchAlgorithmException e) {
/* 463 */       throw new IllegalStateException("No TLS provider", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   static int getSdkInt() {
/*     */     try {
/* 469 */       return Build.VERSION.SDK_INT;
/* 470 */     } catch (NoClassDefFoundError ignored) {
/*     */       
/* 472 */       return 0;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\AndroidPlatform.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */