/*    */ package io.socket.client;
/*    */ 
/*    */ import java.net.MalformedURLException;
/*    */ import java.net.URI;
/*    */ import java.net.URISyntaxException;
/*    */ import java.net.URL;
/*    */ import java.util.regex.Matcher;
/*    */ import java.util.regex.Pattern;
/*    */ 
/*    */ public class Url
/*    */ {
/* 12 */   private static Pattern PATTERN_HTTP = Pattern.compile("^http|ws$");
/* 13 */   private static Pattern PATTERN_HTTPS = Pattern.compile("^(http|ws)s$");
/*    */ 
/*    */ 
/*    */   
/* 17 */   private static Pattern PATTERN_AUTHORITY = Pattern.compile("^(.*@)?([^:]+)(:\\d+)?$");
/*    */ 
/*    */ 
/*    */   
/*    */   public static URL parse(String uri) throws URISyntaxException {
/* 22 */     return parse(new URI(uri));
/*    */   }
/*    */   
/*    */   public static URL parse(URI uri) {
/* 26 */     String protocol = uri.getScheme();
/* 27 */     if (protocol == null || !protocol.matches("^https?|wss?$")) {
/* 28 */       protocol = "https";
/*    */     }
/*    */     
/* 31 */     int port = uri.getPort();
/* 32 */     if (port == -1) {
/* 33 */       if (PATTERN_HTTP.matcher(protocol).matches()) {
/* 34 */         port = 80;
/* 35 */       } else if (PATTERN_HTTPS.matcher(protocol).matches()) {
/* 36 */         port = 443;
/*    */       } 
/*    */     }
/*    */     
/* 40 */     String path = uri.getRawPath();
/* 41 */     if (path == null || path.length() == 0) {
/* 42 */       path = "/";
/*    */     }
/*    */     
/* 45 */     String userInfo = uri.getRawUserInfo();
/* 46 */     String query = uri.getRawQuery();
/* 47 */     String fragment = uri.getRawFragment();
/* 48 */     String _host = uri.getHost();
/* 49 */     if (_host == null)
/*    */     {
/* 51 */       _host = extractHostFromAuthorityPart(uri.getRawAuthority());
/*    */     }
/*    */     try {
/* 54 */       return new URL(protocol + "://" + ((userInfo != null) ? (userInfo + "@") : "") + _host + ((port != -1) ? (":" + port) : "") + path + ((query != null) ? ("?" + query) : "") + ((fragment != null) ? ("#" + fragment) : ""));
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/*    */     }
/* 61 */     catch (MalformedURLException e) {
/* 62 */       throw new RuntimeException(e);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static String extractId(String url) throws MalformedURLException {
/* 67 */     return extractId(new URL(url));
/*    */   }
/*    */   
/*    */   public static String extractId(URL url) {
/* 71 */     String protocol = url.getProtocol();
/* 72 */     int port = url.getPort();
/* 73 */     if (port == -1) {
/* 74 */       if (PATTERN_HTTP.matcher(protocol).matches()) {
/* 75 */         port = 80;
/* 76 */       } else if (PATTERN_HTTPS.matcher(protocol).matches()) {
/* 77 */         port = 443;
/*    */       } 
/*    */     }
/* 80 */     return protocol + "://" + url.getHost() + ":" + port;
/*    */   }
/*    */ 
/*    */   
/*    */   private static String extractHostFromAuthorityPart(String authority) {
/* 85 */     if (authority == null) {
/* 86 */       throw new RuntimeException("unable to parse the host from the authority");
/*    */     }
/*    */     
/* 89 */     Matcher matcher = PATTERN_AUTHORITY.matcher(authority);
/*    */ 
/*    */     
/* 92 */     if (!matcher.matches()) {
/* 93 */       throw new RuntimeException("unable to parse the host from the authority");
/*    */     }
/*    */ 
/*    */     
/* 97 */     return matcher.group(2);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\client\Url.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */