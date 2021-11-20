/*     */ package okhttp3.internal.platform;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Field;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.net.Socket;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import java.security.Security;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.annotation.Nullable;
/*     */ import javax.net.ssl.SSLContext;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import javax.net.ssl.X509TrustManager;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.internal.tls.BasicCertificateChainCleaner;
/*     */ import okhttp3.internal.tls.BasicTrustRootIndex;
/*     */ import okhttp3.internal.tls.CertificateChainCleaner;
/*     */ import okhttp3.internal.tls.TrustRootIndex;
/*     */ import okio.Buffer;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Platform
/*     */ {
/*  78 */   private static final Platform PLATFORM = findPlatform();
/*     */   public static final int INFO = 4;
/*     */   public static final int WARN = 5;
/*  81 */   private static final Logger logger = Logger.getLogger(OkHttpClient.class.getName());
/*     */   
/*     */   public static Platform get() {
/*  84 */     return PLATFORM;
/*     */   }
/*     */ 
/*     */   
/*     */   public String getPrefix() {
/*  89 */     return "OkHttp";
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   protected X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
/*     */     try {
/*  97 */       Class<?> sslContextClass = Class.forName("sun.security.ssl.SSLContextImpl");
/*  98 */       Object context = readFieldOrNull(sslSocketFactory, sslContextClass, "context");
/*  99 */       if (context == null) return null; 
/* 100 */       return readFieldOrNull(context, X509TrustManager.class, "trustManager");
/* 101 */     } catch (ClassNotFoundException e) {
/* 102 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void configureTlsExtensions(SSLSocket sslSocket, @Nullable String hostname, List<Protocol> protocols) throws IOException {}
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void afterHandshake(SSLSocket sslSocket) {}
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public String getSelectedProtocol(SSLSocket socket) {
/* 124 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout) throws IOException {
/* 129 */     socket.connect(address, connectTimeout);
/*     */   }
/*     */   
/*     */   public void log(int level, String message, @Nullable Throwable t) {
/* 133 */     Level logLevel = (level == 5) ? Level.WARNING : Level.INFO;
/* 134 */     logger.log(logLevel, message, t);
/*     */   }
/*     */   
/*     */   public boolean isCleartextTrafficPermitted(String hostname) {
/* 138 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Object getStackTraceForCloseable(String closer) {
/* 147 */     if (logger.isLoggable(Level.FINE)) {
/* 148 */       return new Throwable(closer);
/*     */     }
/* 150 */     return null;
/*     */   }
/*     */   
/*     */   public void logCloseableLeak(String message, Object stackTrace) {
/* 154 */     if (stackTrace == null) {
/* 155 */       message = message + " To see where this was allocated, set the OkHttpClient logger level to FINE: Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);";
/*     */     }
/*     */     
/* 158 */     log(5, message, (Throwable)stackTrace);
/*     */   }
/*     */   
/*     */   public static List<String> alpnProtocolNames(List<Protocol> protocols) {
/* 162 */     List<String> names = new ArrayList<>(protocols.size());
/* 163 */     for (int i = 0, size = protocols.size(); i < size; i++) {
/* 164 */       Protocol protocol = protocols.get(i);
/* 165 */       if (protocol != Protocol.HTTP_1_0)
/* 166 */         names.add(protocol.toString()); 
/*     */     } 
/* 168 */     return names;
/*     */   }
/*     */   
/*     */   public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
/* 172 */     return (CertificateChainCleaner)new BasicCertificateChainCleaner(buildTrustRootIndex(trustManager));
/*     */   }
/*     */   
/*     */   public CertificateChainCleaner buildCertificateChainCleaner(SSLSocketFactory sslSocketFactory) {
/* 176 */     X509TrustManager trustManager = trustManager(sslSocketFactory);
/*     */     
/* 178 */     if (trustManager == null) {
/* 179 */       throw new IllegalStateException("Unable to extract the trust manager on " + 
/* 180 */           get() + ", sslSocketFactory is " + sslSocketFactory
/*     */           
/* 182 */           .getClass());
/*     */     }
/*     */     
/* 185 */     return buildCertificateChainCleaner(trustManager);
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean isConscryptPreferred() {
/* 190 */     if ("conscrypt".equals(System.getProperty("okhttp.platform"))) {
/* 191 */       return true;
/*     */     }
/*     */ 
/*     */     
/* 195 */     String preferredProvider = Security.getProviders()[0].getName();
/* 196 */     return "Conscrypt".equals(preferredProvider);
/*     */   }
/*     */ 
/*     */   
/*     */   private static Platform findPlatform() {
/* 201 */     if (isAndroid()) {
/* 202 */       return findAndroidPlatform();
/*     */     }
/* 204 */     return findJvmPlatform();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isAndroid() {
/* 211 */     return "Dalvik".equals(System.getProperty("java.vm.name"));
/*     */   }
/*     */   
/*     */   private static Platform findJvmPlatform() {
/* 215 */     if (isConscryptPreferred()) {
/* 216 */       Platform conscrypt = ConscryptPlatform.buildIfSupported();
/*     */       
/* 218 */       if (conscrypt != null) {
/* 219 */         return conscrypt;
/*     */       }
/*     */     } 
/*     */     
/* 223 */     Platform jdk9 = Jdk9Platform.buildIfSupported();
/*     */     
/* 225 */     if (jdk9 != null) {
/* 226 */       return jdk9;
/*     */     }
/*     */     
/* 229 */     Platform jdkWithJettyBoot = JdkWithJettyBootPlatform.buildIfSupported();
/*     */     
/* 231 */     if (jdkWithJettyBoot != null) {
/* 232 */       return jdkWithJettyBoot;
/*     */     }
/*     */ 
/*     */     
/* 236 */     return new Platform();
/*     */   }
/*     */   
/*     */   private static Platform findAndroidPlatform() {
/* 240 */     Platform android10 = Android10Platform.buildIfSupported();
/*     */     
/* 242 */     if (android10 != null) {
/* 243 */       return android10;
/*     */     }
/*     */     
/* 246 */     Platform android = AndroidPlatform.buildIfSupported();
/*     */     
/* 248 */     if (android == null) {
/* 249 */       throw new NullPointerException("No platform found on Android");
/*     */     }
/*     */     
/* 252 */     return android;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static byte[] concatLengthPrefixed(List<Protocol> protocols) {
/* 260 */     Buffer result = new Buffer();
/* 261 */     for (int i = 0, size = protocols.size(); i < size; i++) {
/* 262 */       Protocol protocol = protocols.get(i);
/* 263 */       if (protocol != Protocol.HTTP_1_0) {
/* 264 */         result.writeByte(protocol.toString().length());
/* 265 */         result.writeUtf8(protocol.toString());
/*     */       } 
/* 267 */     }  return result.readByteArray();
/*     */   }
/*     */   @Nullable
/*     */   static <T> T readFieldOrNull(Object instance, Class<T> fieldType, String fieldName) {
/* 271 */     for (Class<?> c = instance.getClass(); c != Object.class; c = c.getSuperclass()) {
/*     */       
/* 273 */       try { Field field = c.getDeclaredField(fieldName);
/* 274 */         field.setAccessible(true);
/* 275 */         Object value = field.get(instance);
/* 276 */         if (value == null || !fieldType.isInstance(value)) return null; 
/* 277 */         return fieldType.cast(value); }
/* 278 */       catch (NoSuchFieldException noSuchFieldException) {  }
/* 279 */       catch (IllegalAccessException e)
/* 280 */       { throw new AssertionError(); }
/*     */     
/*     */     } 
/*     */ 
/*     */     
/* 285 */     if (!fieldName.equals("delegate")) {
/* 286 */       Object delegate = readFieldOrNull(instance, Object.class, "delegate");
/* 287 */       if (delegate != null) return readFieldOrNull(delegate, fieldType, fieldName);
/*     */     
/*     */     } 
/* 290 */     return null;
/*     */   }
/*     */   
/*     */   public SSLContext getSSLContext() {
/* 294 */     String jvmVersion = System.getProperty("java.specification.version");
/* 295 */     if ("1.7".equals(jvmVersion)) {
/*     */       
/*     */       try {
/* 298 */         return SSLContext.getInstance("TLSv1.2");
/* 299 */       } catch (NoSuchAlgorithmException noSuchAlgorithmException) {}
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 305 */       return SSLContext.getInstance("TLS");
/* 306 */     } catch (NoSuchAlgorithmException e) {
/* 307 */       throw new IllegalStateException("No TLS provider", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public TrustRootIndex buildTrustRootIndex(X509TrustManager trustManager) {
/* 312 */     return (TrustRootIndex)new BasicTrustRootIndex(trustManager.getAcceptedIssuers());
/*     */   }
/*     */ 
/*     */   
/*     */   public void configureSslSocketFactory(SSLSocketFactory socketFactory) {}
/*     */   
/*     */   public String toString() {
/* 319 */     return getClass().getSimpleName();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\Platform.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */