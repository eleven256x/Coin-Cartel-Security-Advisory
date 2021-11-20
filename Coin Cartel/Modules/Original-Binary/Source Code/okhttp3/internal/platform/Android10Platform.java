/*    */ package okhttp3.internal.platform;
/*    */ 
/*    */ import android.annotation.SuppressLint;
/*    */ import android.net.ssl.SSLSockets;
/*    */ import java.io.IOException;
/*    */ import java.util.List;
/*    */ import javax.annotation.Nullable;
/*    */ import javax.net.ssl.SSLParameters;
/*    */ import javax.net.ssl.SSLSocket;
/*    */ import okhttp3.Protocol;
/*    */ import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ @SuppressLint({"NewApi"})
/*    */ class Android10Platform
/*    */   extends AndroidPlatform
/*    */ {
/*    */   Android10Platform(Class<?> sslParametersClass) {
/* 32 */     super(sslParametersClass, null, null, null, null);
/*    */   }
/*    */ 
/*    */   
/*    */   @SuppressLint({"NewApi"})
/*    */   @IgnoreJRERequirement
/*    */   public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) throws IOException {
/*    */     try {
/* 40 */       enableSessionTickets(sslSocket);
/*    */       
/* 42 */       SSLParameters sslParameters = sslSocket.getSSLParameters();
/*    */ 
/*    */       
/* 45 */       String[] protocolsArray = Platform.alpnProtocolNames(protocols).<String>toArray(new String[0]);
/* 46 */       sslParameters.setApplicationProtocols(protocolsArray);
/*    */       
/* 48 */       sslSocket.setSSLParameters(sslParameters);
/* 49 */     } catch (IllegalArgumentException iae) {
/*    */       
/* 51 */       throw new IOException("Android internal error", iae);
/*    */     } 
/*    */   }
/*    */   
/*    */   private void enableSessionTickets(SSLSocket sslSocket) {
/* 56 */     if (SSLSockets.isSupportedSocket(sslSocket))
/* 57 */       SSLSockets.setUseSessionTickets(sslSocket, true); 
/*    */   }
/*    */   
/*    */   @Nullable
/*    */   @IgnoreJRERequirement
/*    */   public String getSelectedProtocol(SSLSocket socket) {
/* 63 */     String alpnResult = socket.getApplicationProtocol();
/*    */     
/* 65 */     if (alpnResult == null || alpnResult.isEmpty()) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     return alpnResult;
/*    */   }
/*    */   @Nullable
/*    */   public static Platform buildIfSupported() {
/* 73 */     if (!Platform.isAndroid()) {
/* 74 */       return null;
/*    */     }
/*    */     
/*    */     try {
/* 78 */       if (getSdkInt() >= 29) {
/*    */         
/* 80 */         Class<?> sslParametersClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
/*    */         
/* 82 */         return new Android10Platform(sslParametersClass);
/*    */       } 
/* 84 */     } catch (ClassNotFoundException classNotFoundException) {}
/*    */ 
/*    */     
/* 87 */     return null;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\platform\Android10Platform.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */