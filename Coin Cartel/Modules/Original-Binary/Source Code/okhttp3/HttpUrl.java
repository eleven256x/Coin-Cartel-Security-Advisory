/*      */ package okhttp3;
/*      */ 
/*      */ import java.net.MalformedURLException;
/*      */ import java.net.URI;
/*      */ import java.net.URISyntaxException;
/*      */ import java.net.URL;
/*      */ import java.nio.charset.Charset;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.LinkedHashSet;
/*      */ import java.util.List;
/*      */ import java.util.Set;
/*      */ import javax.annotation.Nullable;
/*      */ import okhttp3.internal.Util;
/*      */ import okhttp3.internal.publicsuffix.PublicSuffixDatabase;
/*      */ import okio.Buffer;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public final class HttpUrl
/*      */ {
/*  289 */   private static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
/*      */ 
/*      */   
/*      */   static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
/*      */ 
/*      */   
/*      */   static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
/*      */ 
/*      */   
/*      */   static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
/*      */ 
/*      */   
/*      */   static final String PATH_SEGMENT_ENCODE_SET_URI = "[]";
/*      */   
/*      */   static final String QUERY_ENCODE_SET = " \"'<>#";
/*      */   
/*      */   static final String QUERY_COMPONENT_REENCODE_SET = " \"'<>#&=";
/*      */   
/*      */   static final String QUERY_COMPONENT_ENCODE_SET = " !\"#$&'(),/:;<=>?@[]\\^`{|}~";
/*      */   
/*      */   static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
/*      */   
/*      */   static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
/*      */   
/*      */   static final String FRAGMENT_ENCODE_SET = "";
/*      */   
/*      */   static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";
/*      */   
/*      */   final String scheme;
/*      */   
/*      */   private final String username;
/*      */   
/*      */   private final String password;
/*      */   
/*      */   final String host;
/*      */   
/*      */   final int port;
/*      */   
/*      */   private final List<String> pathSegments;
/*      */   
/*      */   @Nullable
/*      */   private final List<String> queryNamesAndValues;
/*      */   
/*      */   @Nullable
/*      */   private final String fragment;
/*      */   
/*      */   private final String url;
/*      */ 
/*      */   
/*      */   HttpUrl(Builder builder) {
/*  339 */     this.scheme = builder.scheme;
/*  340 */     this.username = percentDecode(builder.encodedUsername, false);
/*  341 */     this.password = percentDecode(builder.encodedPassword, false);
/*  342 */     this.host = builder.host;
/*  343 */     this.port = builder.effectivePort();
/*  344 */     this.pathSegments = percentDecode(builder.encodedPathSegments, false);
/*  345 */     this
/*      */       
/*  347 */       .queryNamesAndValues = (builder.encodedQueryNamesAndValues != null) ? percentDecode(builder.encodedQueryNamesAndValues, true) : null;
/*  348 */     this
/*      */       
/*  350 */       .fragment = (builder.encodedFragment != null) ? percentDecode(builder.encodedFragment, false) : null;
/*  351 */     this.url = builder.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   public URL url() {
/*      */     try {
/*  357 */       return new URL(this.url);
/*  358 */     } catch (MalformedURLException e) {
/*  359 */       throw new RuntimeException(e);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public URI uri() {
/*  377 */     String uri = newBuilder().reencodeForUri().toString();
/*      */     try {
/*  379 */       return new URI(uri);
/*  380 */     } catch (URISyntaxException e) {
/*      */       
/*      */       try {
/*  383 */         String stripped = uri.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F\\p{javaWhitespace}]", "");
/*  384 */         return URI.create(stripped);
/*  385 */       } catch (Exception e1) {
/*  386 */         throw new RuntimeException(e);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public String scheme() {
/*  393 */     return this.scheme;
/*      */   }
/*      */   
/*      */   public boolean isHttps() {
/*  397 */     return this.scheme.equals("https");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String encodedUsername() {
/*  412 */     if (this.username.isEmpty()) return ""; 
/*  413 */     int usernameStart = this.scheme.length() + 3;
/*  414 */     int usernameEnd = Util.delimiterOffset(this.url, usernameStart, this.url.length(), ":@");
/*  415 */     return this.url.substring(usernameStart, usernameEnd);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String username() {
/*  430 */     return this.username;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String encodedPassword() {
/*  445 */     if (this.password.isEmpty()) return ""; 
/*  446 */     int passwordStart = this.url.indexOf(':', this.scheme.length() + 3) + 1;
/*  447 */     int passwordEnd = this.url.indexOf('@');
/*  448 */     return this.url.substring(passwordStart, passwordEnd);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String password() {
/*  463 */     return this.password;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String host() {
/*  486 */     return this.host;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int port() {
/*  502 */     return this.port;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static int defaultPort(String scheme) {
/*  510 */     if (scheme.equals("http"))
/*  511 */       return 80; 
/*  512 */     if (scheme.equals("https")) {
/*  513 */       return 443;
/*      */     }
/*  515 */     return -1;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int pathSize() {
/*  531 */     return this.pathSegments.size();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String encodedPath() {
/*  546 */     int pathStart = this.url.indexOf('/', this.scheme.length() + 3);
/*  547 */     int pathEnd = Util.delimiterOffset(this.url, pathStart, this.url.length(), "?#");
/*  548 */     return this.url.substring(pathStart, pathEnd);
/*      */   }
/*      */   
/*      */   static void pathSegmentsToString(StringBuilder out, List<String> pathSegments) {
/*  552 */     for (int i = 0, size = pathSegments.size(); i < size; i++) {
/*  553 */       out.append('/');
/*  554 */       out.append(pathSegments.get(i));
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> encodedPathSegments() {
/*  570 */     int pathStart = this.url.indexOf('/', this.scheme.length() + 3);
/*  571 */     int pathEnd = Util.delimiterOffset(this.url, pathStart, this.url.length(), "?#");
/*  572 */     List<String> result = new ArrayList<>(); int i;
/*  573 */     for (i = pathStart; i < pathEnd; ) {
/*  574 */       i++;
/*  575 */       int segmentEnd = Util.delimiterOffset(this.url, i, pathEnd, '/');
/*  576 */       result.add(this.url.substring(i, segmentEnd));
/*  577 */       i = segmentEnd;
/*      */     } 
/*  579 */     return result;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> pathSegments() {
/*  594 */     return this.pathSegments;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public String encodedQuery() {
/*  613 */     if (this.queryNamesAndValues == null) return null; 
/*  614 */     int queryStart = this.url.indexOf('?') + 1;
/*  615 */     int queryEnd = Util.delimiterOffset(this.url, queryStart, this.url.length(), '#');
/*  616 */     return this.url.substring(queryStart, queryEnd);
/*      */   }
/*      */   
/*      */   static void namesAndValuesToQueryString(StringBuilder out, List<String> namesAndValues) {
/*  620 */     for (int i = 0, size = namesAndValues.size(); i < size; i += 2) {
/*  621 */       String name = namesAndValues.get(i);
/*  622 */       String value = namesAndValues.get(i + 1);
/*  623 */       if (i > 0) out.append('&'); 
/*  624 */       out.append(name);
/*  625 */       if (value != null) {
/*  626 */         out.append('=');
/*  627 */         out.append(value);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static List<String> queryStringToNamesAndValues(String encodedQuery) {
/*  639 */     List<String> result = new ArrayList<>();
/*  640 */     for (int pos = 0; pos <= encodedQuery.length(); ) {
/*  641 */       int ampersandOffset = encodedQuery.indexOf('&', pos);
/*  642 */       if (ampersandOffset == -1) ampersandOffset = encodedQuery.length();
/*      */       
/*  644 */       int equalsOffset = encodedQuery.indexOf('=', pos);
/*  645 */       if (equalsOffset == -1 || equalsOffset > ampersandOffset) {
/*  646 */         result.add(encodedQuery.substring(pos, ampersandOffset));
/*  647 */         result.add(null);
/*      */       } else {
/*  649 */         result.add(encodedQuery.substring(pos, equalsOffset));
/*  650 */         result.add(encodedQuery.substring(equalsOffset + 1, ampersandOffset));
/*      */       } 
/*  652 */       pos = ampersandOffset + 1;
/*      */     } 
/*  654 */     return result;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public String query() {
/*  673 */     if (this.queryNamesAndValues == null) return null; 
/*  674 */     StringBuilder result = new StringBuilder();
/*  675 */     namesAndValuesToQueryString(result, this.queryNamesAndValues);
/*  676 */     return result.toString();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int querySize() {
/*  694 */     return (this.queryNamesAndValues != null) ? (this.queryNamesAndValues.size() / 2) : 0;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public String queryParameter(String name) {
/*  711 */     if (this.queryNamesAndValues == null) return null; 
/*  712 */     for (int i = 0, size = this.queryNamesAndValues.size(); i < size; i += 2) {
/*  713 */       if (name.equals(this.queryNamesAndValues.get(i))) {
/*  714 */         return this.queryNamesAndValues.get(i + 1);
/*      */       }
/*      */     } 
/*  717 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Set<String> queryParameterNames() {
/*  734 */     if (this.queryNamesAndValues == null) return Collections.emptySet(); 
/*  735 */     Set<String> result = new LinkedHashSet<>();
/*  736 */     for (int i = 0, size = this.queryNamesAndValues.size(); i < size; i += 2) {
/*  737 */       result.add(this.queryNamesAndValues.get(i));
/*      */     }
/*  739 */     return Collections.unmodifiableSet(result);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> queryParameterValues(String name) {
/*  761 */     if (this.queryNamesAndValues == null) return Collections.emptyList(); 
/*  762 */     List<String> result = new ArrayList<>();
/*  763 */     for (int i = 0, size = this.queryNamesAndValues.size(); i < size; i += 2) {
/*  764 */       if (name.equals(this.queryNamesAndValues.get(i))) {
/*  765 */         result.add(this.queryNamesAndValues.get(i + 1));
/*      */       }
/*      */     } 
/*  768 */     return Collections.unmodifiableList(result);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String queryParameterName(int index) {
/*  789 */     if (this.queryNamesAndValues == null) throw new IndexOutOfBoundsException(); 
/*  790 */     return this.queryNamesAndValues.get(index * 2);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String queryParameterValue(int index) {
/*  811 */     if (this.queryNamesAndValues == null) throw new IndexOutOfBoundsException(); 
/*  812 */     return this.queryNamesAndValues.get(index * 2 + 1);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public String encodedFragment() {
/*  828 */     if (this.fragment == null) return null; 
/*  829 */     int fragmentStart = this.url.indexOf('#') + 1;
/*  830 */     return this.url.substring(fragmentStart);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public String fragment() {
/*  846 */     return this.fragment;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String redact() {
/*  855 */     return newBuilder("/...")
/*  856 */       .username("")
/*  857 */       .password("")
/*  858 */       .build()
/*  859 */       .toString();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public HttpUrl resolve(String link) {
/*  867 */     Builder builder = newBuilder(link);
/*  868 */     return (builder != null) ? builder.build() : null;
/*      */   }
/*      */   
/*      */   public Builder newBuilder() {
/*  872 */     Builder result = new Builder();
/*  873 */     result.scheme = this.scheme;
/*  874 */     result.encodedUsername = encodedUsername();
/*  875 */     result.encodedPassword = encodedPassword();
/*  876 */     result.host = this.host;
/*      */     
/*  878 */     result.port = (this.port != defaultPort(this.scheme)) ? this.port : -1;
/*  879 */     result.encodedPathSegments.clear();
/*  880 */     result.encodedPathSegments.addAll(encodedPathSegments());
/*  881 */     result.encodedQuery(encodedQuery());
/*  882 */     result.encodedFragment = encodedFragment();
/*  883 */     return result;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public Builder newBuilder(String link) {
/*      */     try {
/*  892 */       return (new Builder()).parse(this, link);
/*  893 */     } catch (IllegalArgumentException ignored) {
/*  894 */       return null;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public static HttpUrl parse(String url) {
/*      */     try {
/*  904 */       return get(url);
/*  905 */     } catch (IllegalArgumentException ignored) {
/*  906 */       return null;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static HttpUrl get(String url) {
/*  916 */     return (new Builder()).parse(null, url).build();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public static HttpUrl get(URL url) {
/*  924 */     return parse(url.toString());
/*      */   }
/*      */   @Nullable
/*      */   public static HttpUrl get(URI uri) {
/*  928 */     return parse(uri.toString());
/*      */   }
/*      */   
/*      */   public boolean equals(@Nullable Object other) {
/*  932 */     return (other instanceof HttpUrl && ((HttpUrl)other).url.equals(this.url));
/*      */   }
/*      */   
/*      */   public int hashCode() {
/*  936 */     return this.url.hashCode();
/*      */   }
/*      */   
/*      */   public String toString() {
/*  940 */     return this.url;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   @Nullable
/*      */   public String topPrivateDomain() {
/*  963 */     if (Util.verifyAsIpAddress(this.host)) return null; 
/*  964 */     return PublicSuffixDatabase.get().getEffectiveTldPlusOne(this.host);
/*      */   }
/*      */   
/*      */   public static final class Builder { @Nullable
/*      */     String scheme;
/*  969 */     String encodedUsername = "";
/*  970 */     String encodedPassword = ""; @Nullable
/*      */     String host;
/*  972 */     int port = -1;
/*  973 */     final List<String> encodedPathSegments = new ArrayList<>();
/*      */     @Nullable
/*      */     List<String> encodedQueryNamesAndValues;
/*      */     
/*      */     public Builder() {
/*  978 */       this.encodedPathSegments.add("");
/*      */     } @Nullable
/*      */     String encodedFragment; static final String INVALID_HOST = "Invalid URL host";
/*      */     public Builder scheme(String scheme) {
/*  982 */       if (scheme == null)
/*  983 */         throw new NullPointerException("scheme == null"); 
/*  984 */       if (scheme.equalsIgnoreCase("http")) {
/*  985 */         this.scheme = "http";
/*  986 */       } else if (scheme.equalsIgnoreCase("https")) {
/*  987 */         this.scheme = "https";
/*      */       } else {
/*  989 */         throw new IllegalArgumentException("unexpected scheme: " + scheme);
/*      */       } 
/*  991 */       return this;
/*      */     }
/*      */     
/*      */     public Builder username(String username) {
/*  995 */       if (username == null) throw new NullPointerException("username == null"); 
/*  996 */       this.encodedUsername = HttpUrl.canonicalize(username, " \"':;<=>@[]^`{}|/\\?#", false, false, false, true);
/*  997 */       return this;
/*      */     }
/*      */     
/*      */     public Builder encodedUsername(String encodedUsername) {
/* 1001 */       if (encodedUsername == null) throw new NullPointerException("encodedUsername == null"); 
/* 1002 */       this.encodedUsername = HttpUrl.canonicalize(encodedUsername, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
/*      */       
/* 1004 */       return this;
/*      */     }
/*      */     
/*      */     public Builder password(String password) {
/* 1008 */       if (password == null) throw new NullPointerException("password == null"); 
/* 1009 */       this.encodedPassword = HttpUrl.canonicalize(password, " \"':;<=>@[]^`{}|/\\?#", false, false, false, true);
/* 1010 */       return this;
/*      */     }
/*      */     
/*      */     public Builder encodedPassword(String encodedPassword) {
/* 1014 */       if (encodedPassword == null) throw new NullPointerException("encodedPassword == null"); 
/* 1015 */       this.encodedPassword = HttpUrl.canonicalize(encodedPassword, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true);
/*      */       
/* 1017 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder host(String host) {
/* 1025 */       if (host == null) throw new NullPointerException("host == null"); 
/* 1026 */       String encoded = canonicalizeHost(host, 0, host.length());
/* 1027 */       if (encoded == null) throw new IllegalArgumentException("unexpected host: " + host); 
/* 1028 */       this.host = encoded;
/* 1029 */       return this;
/*      */     }
/*      */     
/*      */     public Builder port(int port) {
/* 1033 */       if (port <= 0 || port > 65535) throw new IllegalArgumentException("unexpected port: " + port); 
/* 1034 */       this.port = port;
/* 1035 */       return this;
/*      */     }
/*      */     
/*      */     int effectivePort() {
/* 1039 */       return (this.port != -1) ? this.port : HttpUrl.defaultPort(this.scheme);
/*      */     }
/*      */     
/*      */     public Builder addPathSegment(String pathSegment) {
/* 1043 */       if (pathSegment == null) throw new NullPointerException("pathSegment == null"); 
/* 1044 */       push(pathSegment, 0, pathSegment.length(), false, false);
/* 1045 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder addPathSegments(String pathSegments) {
/* 1053 */       if (pathSegments == null) throw new NullPointerException("pathSegments == null"); 
/* 1054 */       return addPathSegments(pathSegments, false);
/*      */     }
/*      */     
/*      */     public Builder addEncodedPathSegment(String encodedPathSegment) {
/* 1058 */       if (encodedPathSegment == null) {
/* 1059 */         throw new NullPointerException("encodedPathSegment == null");
/*      */       }
/* 1061 */       push(encodedPathSegment, 0, encodedPathSegment.length(), false, true);
/* 1062 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public Builder addEncodedPathSegments(String encodedPathSegments) {
/* 1071 */       if (encodedPathSegments == null) {
/* 1072 */         throw new NullPointerException("encodedPathSegments == null");
/*      */       }
/* 1074 */       return addPathSegments(encodedPathSegments, true);
/*      */     }
/*      */     
/*      */     private Builder addPathSegments(String pathSegments, boolean alreadyEncoded) {
/* 1078 */       int offset = 0;
/*      */       while (true) {
/* 1080 */         int segmentEnd = Util.delimiterOffset(pathSegments, offset, pathSegments.length(), "/\\");
/* 1081 */         boolean addTrailingSlash = (segmentEnd < pathSegments.length());
/* 1082 */         push(pathSegments, offset, segmentEnd, addTrailingSlash, alreadyEncoded);
/* 1083 */         offset = segmentEnd + 1;
/* 1084 */         if (offset > pathSegments.length())
/* 1085 */           return this; 
/*      */       } 
/*      */     }
/*      */     public Builder setPathSegment(int index, String pathSegment) {
/* 1089 */       if (pathSegment == null) throw new NullPointerException("pathSegment == null"); 
/* 1090 */       String canonicalPathSegment = HttpUrl.canonicalize(pathSegment, 0, pathSegment
/* 1091 */           .length(), " \"<>^`{}|/\\?#", false, false, false, true, null);
/*      */       
/* 1093 */       if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
/* 1094 */         throw new IllegalArgumentException("unexpected path segment: " + pathSegment);
/*      */       }
/* 1096 */       this.encodedPathSegments.set(index, canonicalPathSegment);
/* 1097 */       return this;
/*      */     }
/*      */     
/*      */     public Builder setEncodedPathSegment(int index, String encodedPathSegment) {
/* 1101 */       if (encodedPathSegment == null) {
/* 1102 */         throw new NullPointerException("encodedPathSegment == null");
/*      */       }
/* 1104 */       String canonicalPathSegment = HttpUrl.canonicalize(encodedPathSegment, 0, encodedPathSegment
/* 1105 */           .length(), " \"<>^`{}|/\\?#", true, false, false, true, null);
/*      */       
/* 1107 */       this.encodedPathSegments.set(index, canonicalPathSegment);
/* 1108 */       if (isDot(canonicalPathSegment) || isDotDot(canonicalPathSegment)) {
/* 1109 */         throw new IllegalArgumentException("unexpected path segment: " + encodedPathSegment);
/*      */       }
/* 1111 */       return this;
/*      */     }
/*      */     
/*      */     public Builder removePathSegment(int index) {
/* 1115 */       this.encodedPathSegments.remove(index);
/* 1116 */       if (this.encodedPathSegments.isEmpty()) {
/* 1117 */         this.encodedPathSegments.add("");
/*      */       }
/* 1119 */       return this;
/*      */     }
/*      */     
/*      */     public Builder encodedPath(String encodedPath) {
/* 1123 */       if (encodedPath == null) throw new NullPointerException("encodedPath == null"); 
/* 1124 */       if (!encodedPath.startsWith("/")) {
/* 1125 */         throw new IllegalArgumentException("unexpected encodedPath: " + encodedPath);
/*      */       }
/* 1127 */       resolvePath(encodedPath, 0, encodedPath.length());
/* 1128 */       return this;
/*      */     }
/*      */     
/*      */     public Builder query(@Nullable String query) {
/* 1132 */       this
/*      */ 
/*      */         
/* 1135 */         .encodedQueryNamesAndValues = (query != null) ? HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(query, " \"'<>#", false, false, true, true)) : null;
/* 1136 */       return this;
/*      */     }
/*      */     
/*      */     public Builder encodedQuery(@Nullable String encodedQuery) {
/* 1140 */       this
/*      */ 
/*      */         
/* 1143 */         .encodedQueryNamesAndValues = (encodedQuery != null) ? HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(encodedQuery, " \"'<>#", true, false, true, true)) : null;
/* 1144 */       return this;
/*      */     }
/*      */ 
/*      */     
/*      */     public Builder addQueryParameter(String name, @Nullable String value) {
/* 1149 */       if (name == null) throw new NullPointerException("name == null"); 
/* 1150 */       if (this.encodedQueryNamesAndValues == null) this.encodedQueryNamesAndValues = new ArrayList<>(); 
/* 1151 */       this.encodedQueryNamesAndValues.add(
/* 1152 */           HttpUrl.canonicalize(name, " !\"#$&'(),/:;<=>?@[]\\^`{|}~", false, false, true, true));
/* 1153 */       this.encodedQueryNamesAndValues.add((value != null) ? 
/* 1154 */           HttpUrl.canonicalize(value, " !\"#$&'(),/:;<=>?@[]\\^`{|}~", false, false, true, true) : 
/* 1155 */           null);
/* 1156 */       return this;
/*      */     }
/*      */ 
/*      */     
/*      */     public Builder addEncodedQueryParameter(String encodedName, @Nullable String encodedValue) {
/* 1161 */       if (encodedName == null) throw new NullPointerException("encodedName == null"); 
/* 1162 */       if (this.encodedQueryNamesAndValues == null) this.encodedQueryNamesAndValues = new ArrayList<>(); 
/* 1163 */       this.encodedQueryNamesAndValues.add(
/* 1164 */           HttpUrl.canonicalize(encodedName, " \"'<>#&=", true, false, true, true));
/* 1165 */       this.encodedQueryNamesAndValues.add((encodedValue != null) ? 
/* 1166 */           HttpUrl.canonicalize(encodedValue, " \"'<>#&=", true, false, true, true) : 
/* 1167 */           null);
/* 1168 */       return this;
/*      */     }
/*      */     
/*      */     public Builder setQueryParameter(String name, @Nullable String value) {
/* 1172 */       removeAllQueryParameters(name);
/* 1173 */       addQueryParameter(name, value);
/* 1174 */       return this;
/*      */     }
/*      */     
/*      */     public Builder setEncodedQueryParameter(String encodedName, @Nullable String encodedValue) {
/* 1178 */       removeAllEncodedQueryParameters(encodedName);
/* 1179 */       addEncodedQueryParameter(encodedName, encodedValue);
/* 1180 */       return this;
/*      */     }
/*      */     
/*      */     public Builder removeAllQueryParameters(String name) {
/* 1184 */       if (name == null) throw new NullPointerException("name == null"); 
/* 1185 */       if (this.encodedQueryNamesAndValues == null) return this; 
/* 1186 */       String nameToRemove = HttpUrl.canonicalize(name, " !\"#$&'(),/:;<=>?@[]\\^`{|}~", false, false, true, true);
/*      */       
/* 1188 */       removeAllCanonicalQueryParameters(nameToRemove);
/* 1189 */       return this;
/*      */     }
/*      */     
/*      */     public Builder removeAllEncodedQueryParameters(String encodedName) {
/* 1193 */       if (encodedName == null) throw new NullPointerException("encodedName == null"); 
/* 1194 */       if (this.encodedQueryNamesAndValues == null) return this; 
/* 1195 */       removeAllCanonicalQueryParameters(
/* 1196 */           HttpUrl.canonicalize(encodedName, " \"'<>#&=", true, false, true, true));
/* 1197 */       return this;
/*      */     }
/*      */     
/*      */     private void removeAllCanonicalQueryParameters(String canonicalName) {
/* 1201 */       for (int i = this.encodedQueryNamesAndValues.size() - 2; i >= 0; i -= 2) {
/* 1202 */         if (canonicalName.equals(this.encodedQueryNamesAndValues.get(i))) {
/* 1203 */           this.encodedQueryNamesAndValues.remove(i + 1);
/* 1204 */           this.encodedQueryNamesAndValues.remove(i);
/* 1205 */           if (this.encodedQueryNamesAndValues.isEmpty()) {
/* 1206 */             this.encodedQueryNamesAndValues = null;
/*      */             return;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */     }
/*      */     
/*      */     public Builder fragment(@Nullable String fragment) {
/* 1214 */       this
/*      */         
/* 1216 */         .encodedFragment = (fragment != null) ? HttpUrl.canonicalize(fragment, "", false, false, false, false) : null;
/* 1217 */       return this;
/*      */     }
/*      */     
/*      */     public Builder encodedFragment(@Nullable String encodedFragment) {
/* 1221 */       this
/*      */         
/* 1223 */         .encodedFragment = (encodedFragment != null) ? HttpUrl.canonicalize(encodedFragment, "", true, false, false, false) : null;
/* 1224 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     Builder reencodeForUri() {
/*      */       int i;
/*      */       int size;
/* 1232 */       for (i = 0, size = this.encodedPathSegments.size(); i < size; i++) {
/* 1233 */         String pathSegment = this.encodedPathSegments.get(i);
/* 1234 */         this.encodedPathSegments.set(i, 
/* 1235 */             HttpUrl.canonicalize(pathSegment, "[]", true, true, false, true));
/*      */       } 
/* 1237 */       if (this.encodedQueryNamesAndValues != null) {
/* 1238 */         for (i = 0, size = this.encodedQueryNamesAndValues.size(); i < size; i++) {
/* 1239 */           String component = this.encodedQueryNamesAndValues.get(i);
/* 1240 */           if (component != null) {
/* 1241 */             this.encodedQueryNamesAndValues.set(i, 
/* 1242 */                 HttpUrl.canonicalize(component, "\\^`{|}", true, true, true, true));
/*      */           }
/*      */         } 
/*      */       }
/* 1246 */       if (this.encodedFragment != null) {
/* 1247 */         this.encodedFragment = HttpUrl.canonicalize(this.encodedFragment, " \"#<>\\^`{|}", true, true, false, false);
/*      */       }
/*      */       
/* 1250 */       return this;
/*      */     }
/*      */     
/*      */     public HttpUrl build() {
/* 1254 */       if (this.scheme == null) throw new IllegalStateException("scheme == null"); 
/* 1255 */       if (this.host == null) throw new IllegalStateException("host == null"); 
/* 1256 */       return new HttpUrl(this);
/*      */     }
/*      */     
/*      */     public String toString() {
/* 1260 */       StringBuilder result = new StringBuilder();
/* 1261 */       if (this.scheme != null) {
/* 1262 */         result.append(this.scheme);
/* 1263 */         result.append("://");
/*      */       } else {
/* 1265 */         result.append("//");
/*      */       } 
/*      */       
/* 1268 */       if (!this.encodedUsername.isEmpty() || !this.encodedPassword.isEmpty()) {
/* 1269 */         result.append(this.encodedUsername);
/* 1270 */         if (!this.encodedPassword.isEmpty()) {
/* 1271 */           result.append(':');
/* 1272 */           result.append(this.encodedPassword);
/*      */         } 
/* 1274 */         result.append('@');
/*      */       } 
/*      */       
/* 1277 */       if (this.host != null) {
/* 1278 */         if (this.host.indexOf(':') != -1) {
/*      */           
/* 1280 */           result.append('[');
/* 1281 */           result.append(this.host);
/* 1282 */           result.append(']');
/*      */         } else {
/* 1284 */           result.append(this.host);
/*      */         } 
/*      */       }
/*      */       
/* 1288 */       if (this.port != -1 || this.scheme != null) {
/* 1289 */         int effectivePort = effectivePort();
/* 1290 */         if (this.scheme == null || effectivePort != HttpUrl.defaultPort(this.scheme)) {
/* 1291 */           result.append(':');
/* 1292 */           result.append(effectivePort);
/*      */         } 
/*      */       } 
/*      */       
/* 1296 */       HttpUrl.pathSegmentsToString(result, this.encodedPathSegments);
/*      */       
/* 1298 */       if (this.encodedQueryNamesAndValues != null) {
/* 1299 */         result.append('?');
/* 1300 */         HttpUrl.namesAndValuesToQueryString(result, this.encodedQueryNamesAndValues);
/*      */       } 
/*      */       
/* 1303 */       if (this.encodedFragment != null) {
/* 1304 */         result.append('#');
/* 1305 */         result.append(this.encodedFragment);
/*      */       } 
/*      */       
/* 1308 */       return result.toString();
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     Builder parse(@Nullable HttpUrl base, String input) {
/* 1314 */       int pos = Util.skipLeadingAsciiWhitespace(input, 0, input.length());
/* 1315 */       int limit = Util.skipTrailingAsciiWhitespace(input, pos, input.length());
/*      */ 
/*      */       
/* 1318 */       int schemeDelimiterOffset = schemeDelimiterOffset(input, pos, limit);
/* 1319 */       if (schemeDelimiterOffset != -1) {
/* 1320 */         if (input.regionMatches(true, pos, "https:", 0, 6)) {
/* 1321 */           this.scheme = "https";
/* 1322 */           pos += "https:".length();
/* 1323 */         } else if (input.regionMatches(true, pos, "http:", 0, 5)) {
/* 1324 */           this.scheme = "http";
/* 1325 */           pos += "http:".length();
/*      */         } else {
/* 1327 */           throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but was '" + input
/* 1328 */               .substring(0, schemeDelimiterOffset) + "'");
/*      */         } 
/* 1330 */       } else if (base != null) {
/* 1331 */         this.scheme = base.scheme;
/*      */       } else {
/* 1333 */         throw new IllegalArgumentException("Expected URL scheme 'http' or 'https' but no colon was found");
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 1338 */       boolean hasUsername = false;
/* 1339 */       boolean hasPassword = false;
/* 1340 */       int slashCount = slashCount(input, pos, limit);
/* 1341 */       if (slashCount >= 2 || base == null || !base.scheme.equals(this.scheme)) {
/*      */         int componentDelimiterOffset;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 1351 */         pos += slashCount;
/*      */         
/*      */         while (true) {
/* 1354 */           componentDelimiterOffset = Util.delimiterOffset(input, pos, limit, "@/\\?#");
/*      */ 
/*      */           
/* 1357 */           int c = (componentDelimiterOffset != limit) ? input.charAt(componentDelimiterOffset) : -1;
/* 1358 */           switch (c) {
/*      */             
/*      */             case 64:
/* 1361 */               if (!hasPassword) {
/* 1362 */                 int passwordColonOffset = Util.delimiterOffset(input, pos, componentDelimiterOffset, ':');
/*      */                 
/* 1364 */                 String canonicalUsername = HttpUrl.canonicalize(input, pos, passwordColonOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true, null);
/*      */ 
/*      */                 
/* 1367 */                 this
/*      */                   
/* 1369 */                   .encodedUsername = hasUsername ? (this.encodedUsername + "%40" + canonicalUsername) : canonicalUsername;
/* 1370 */                 if (passwordColonOffset != componentDelimiterOffset) {
/* 1371 */                   hasPassword = true;
/* 1372 */                   this.encodedPassword = HttpUrl.canonicalize(input, passwordColonOffset + 1, componentDelimiterOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true, null);
/*      */                 } 
/*      */ 
/*      */                 
/* 1376 */                 hasUsername = true;
/*      */               } else {
/* 1378 */                 this.encodedPassword += "%40" + HttpUrl.canonicalize(input, pos, componentDelimiterOffset, " \"':;<=>@[]^`{}|/\\?#", true, false, false, true, null);
/*      */               } 
/*      */ 
/*      */               
/* 1382 */               pos = componentDelimiterOffset + 1;
/*      */             case -1:
/*      */             case 35:
/*      */             case 47:
/*      */             case 63:
/*      */             case 92:
/*      */               break;
/*      */           } 
/*      */         } 
/* 1391 */         int portColonOffset = portColonOffset(input, pos, componentDelimiterOffset);
/* 1392 */         if (portColonOffset + 1 < componentDelimiterOffset) {
/* 1393 */           this.host = canonicalizeHost(input, pos, portColonOffset);
/* 1394 */           this.port = parsePort(input, portColonOffset + 1, componentDelimiterOffset);
/* 1395 */           if (this.port == -1) {
/* 1396 */             throw new IllegalArgumentException("Invalid URL port: \"" + input
/* 1397 */                 .substring(portColonOffset + 1, componentDelimiterOffset) + '"');
/*      */           }
/*      */         } else {
/* 1400 */           this.host = canonicalizeHost(input, pos, portColonOffset);
/* 1401 */           this.port = HttpUrl.defaultPort(this.scheme);
/*      */         } 
/* 1403 */         if (this.host == null) {
/* 1404 */           throw new IllegalArgumentException("Invalid URL host: \"" + input
/* 1405 */               .substring(pos, portColonOffset) + '"');
/*      */         }
/* 1407 */         pos = componentDelimiterOffset;
/*      */       
/*      */       }
/*      */       else {
/*      */ 
/*      */         
/* 1413 */         this.encodedUsername = base.encodedUsername();
/* 1414 */         this.encodedPassword = base.encodedPassword();
/* 1415 */         this.host = base.host;
/* 1416 */         this.port = base.port;
/* 1417 */         this.encodedPathSegments.clear();
/* 1418 */         this.encodedPathSegments.addAll(base.encodedPathSegments());
/* 1419 */         if (pos == limit || input.charAt(pos) == '#') {
/* 1420 */           encodedQuery(base.encodedQuery());
/*      */         }
/*      */       } 
/*      */ 
/*      */       
/* 1425 */       int pathDelimiterOffset = Util.delimiterOffset(input, pos, limit, "?#");
/* 1426 */       resolvePath(input, pos, pathDelimiterOffset);
/* 1427 */       pos = pathDelimiterOffset;
/*      */ 
/*      */       
/* 1430 */       if (pos < limit && input.charAt(pos) == '?') {
/* 1431 */         int queryDelimiterOffset = Util.delimiterOffset(input, pos, limit, '#');
/* 1432 */         this.encodedQueryNamesAndValues = HttpUrl.queryStringToNamesAndValues(HttpUrl.canonicalize(input, pos + 1, queryDelimiterOffset, " \"'<>#", true, false, true, true, null));
/*      */         
/* 1434 */         pos = queryDelimiterOffset;
/*      */       } 
/*      */ 
/*      */       
/* 1438 */       if (pos < limit && input.charAt(pos) == '#') {
/* 1439 */         this.encodedFragment = HttpUrl.canonicalize(input, pos + 1, limit, "", true, false, false, false, null);
/*      */       }
/*      */ 
/*      */       
/* 1443 */       return this;
/*      */     }
/*      */ 
/*      */     
/*      */     private void resolvePath(String input, int pos, int limit) {
/* 1448 */       if (pos == limit) {
/*      */         return;
/*      */       }
/*      */       
/* 1452 */       char c = input.charAt(pos);
/* 1453 */       if (c == '/' || c == '\\') {
/*      */         
/* 1455 */         this.encodedPathSegments.clear();
/* 1456 */         this.encodedPathSegments.add("");
/* 1457 */         pos++;
/*      */       } else {
/*      */         
/* 1460 */         this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, "");
/*      */       } 
/*      */ 
/*      */       
/* 1464 */       for (int i = pos; i < limit; ) {
/* 1465 */         int pathSegmentDelimiterOffset = Util.delimiterOffset(input, i, limit, "/\\");
/* 1466 */         boolean segmentHasTrailingSlash = (pathSegmentDelimiterOffset < limit);
/* 1467 */         push(input, i, pathSegmentDelimiterOffset, segmentHasTrailingSlash, true);
/* 1468 */         i = pathSegmentDelimiterOffset;
/* 1469 */         if (segmentHasTrailingSlash) i++;
/*      */       
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/*      */     private void push(String input, int pos, int limit, boolean addTrailingSlash, boolean alreadyEncoded) {
/* 1476 */       String segment = HttpUrl.canonicalize(input, pos, limit, " \"<>^`{}|/\\?#", alreadyEncoded, false, false, true, null);
/*      */       
/* 1478 */       if (isDot(segment)) {
/*      */         return;
/*      */       }
/* 1481 */       if (isDotDot(segment)) {
/* 1482 */         pop();
/*      */         return;
/*      */       } 
/* 1485 */       if (((String)this.encodedPathSegments.get(this.encodedPathSegments.size() - 1)).isEmpty()) {
/* 1486 */         this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, segment);
/*      */       } else {
/* 1488 */         this.encodedPathSegments.add(segment);
/*      */       } 
/* 1490 */       if (addTrailingSlash) {
/* 1491 */         this.encodedPathSegments.add("");
/*      */       }
/*      */     }
/*      */     
/*      */     private boolean isDot(String input) {
/* 1496 */       return (input.equals(".") || input.equalsIgnoreCase("%2e"));
/*      */     }
/*      */     
/*      */     private boolean isDotDot(String input) {
/* 1500 */       return (input.equals("..") || input
/* 1501 */         .equalsIgnoreCase("%2e.") || input
/* 1502 */         .equalsIgnoreCase(".%2e") || input
/* 1503 */         .equalsIgnoreCase("%2e%2e"));
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     private void pop() {
/* 1517 */       String removed = this.encodedPathSegments.remove(this.encodedPathSegments.size() - 1);
/*      */ 
/*      */       
/* 1520 */       if (removed.isEmpty() && !this.encodedPathSegments.isEmpty()) {
/* 1521 */         this.encodedPathSegments.set(this.encodedPathSegments.size() - 1, "");
/*      */       } else {
/* 1523 */         this.encodedPathSegments.add("");
/*      */       } 
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     private static int schemeDelimiterOffset(String input, int pos, int limit) {
/* 1532 */       if (limit - pos < 2) return -1;
/*      */       
/* 1534 */       char c0 = input.charAt(pos);
/* 1535 */       if ((c0 < 'a' || c0 > 'z') && (c0 < 'A' || c0 > 'Z')) return -1;
/*      */       
/* 1537 */       for (int i = pos + 1; i < limit; ) {
/* 1538 */         char c = input.charAt(i);
/*      */         
/* 1540 */         if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.') {
/*      */           i++;
/*      */ 
/*      */           
/*      */           continue;
/*      */         } 
/*      */         
/* 1547 */         if (c == ':') {
/* 1548 */           return i;
/*      */         }
/* 1550 */         return -1;
/*      */       } 
/*      */ 
/*      */       
/* 1554 */       return -1;
/*      */     }
/*      */ 
/*      */     
/*      */     private static int slashCount(String input, int pos, int limit) {
/* 1559 */       int slashCount = 0;
/* 1560 */       while (pos < limit) {
/* 1561 */         char c = input.charAt(pos);
/* 1562 */         if (c == '\\' || c == '/') {
/* 1563 */           slashCount++;
/* 1564 */           pos++;
/*      */         } 
/*      */       } 
/*      */ 
/*      */       
/* 1569 */       return slashCount;
/*      */     }
/*      */ 
/*      */     
/*      */     private static int portColonOffset(String input, int pos, int limit) {
/* 1574 */       for (int i = pos; i < limit; i++) {
/* 1575 */         switch (input.charAt(i)) { case '[':
/*      */             do {  }
/* 1577 */             while (++i < limit && 
/* 1578 */               input.charAt(i) != ']');
/*      */             break;
/*      */           
/*      */           case ':':
/* 1582 */             return i; }
/*      */       
/*      */       } 
/* 1585 */       return limit;
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*      */     private static String canonicalizeHost(String input, int pos, int limit) {
/* 1591 */       String percentDecoded = HttpUrl.percentDecode(input, pos, limit, false);
/* 1592 */       return Util.canonicalizeHost(percentDecoded);
/*      */     }
/*      */ 
/*      */     
/*      */     private static int parsePort(String input, int pos, int limit) {
/*      */       try {
/* 1598 */         String portString = HttpUrl.canonicalize(input, pos, limit, "", false, false, false, true, null);
/* 1599 */         int i = Integer.parseInt(portString);
/* 1600 */         if (i > 0 && i <= 65535) return i; 
/* 1601 */         return -1;
/* 1602 */       } catch (NumberFormatException e) {
/* 1603 */         return -1;
/*      */       } 
/*      */     } }
/*      */ 
/*      */   
/*      */   static String percentDecode(String encoded, boolean plusIsSpace) {
/* 1609 */     return percentDecode(encoded, 0, encoded.length(), plusIsSpace);
/*      */   }
/*      */   
/*      */   private List<String> percentDecode(List<String> list, boolean plusIsSpace) {
/* 1613 */     int size = list.size();
/* 1614 */     List<String> result = new ArrayList<>(size);
/* 1615 */     for (int i = 0; i < size; i++) {
/* 1616 */       String s = list.get(i);
/* 1617 */       result.add((s != null) ? percentDecode(s, plusIsSpace) : null);
/*      */     } 
/* 1619 */     return Collections.unmodifiableList(result);
/*      */   }
/*      */   
/*      */   static String percentDecode(String encoded, int pos, int limit, boolean plusIsSpace) {
/* 1623 */     for (int i = pos; i < limit; i++) {
/* 1624 */       char c = encoded.charAt(i);
/* 1625 */       if (c == '%' || (c == '+' && plusIsSpace)) {
/*      */         
/* 1627 */         Buffer out = new Buffer();
/* 1628 */         out.writeUtf8(encoded, pos, i);
/* 1629 */         percentDecode(out, encoded, i, limit, plusIsSpace);
/* 1630 */         return out.readUtf8();
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/* 1635 */     return encoded.substring(pos, limit);
/*      */   }
/*      */   
/*      */   static void percentDecode(Buffer out, String encoded, int pos, int limit, boolean plusIsSpace) {
/*      */     int i;
/* 1640 */     for (i = pos; i < limit; i += Character.charCount(SYNTHETIC_LOCAL_VARIABLE_6)) {
/* 1641 */       int codePoint = encoded.codePointAt(i);
/* 1642 */       if (codePoint == 37 && i + 2 < limit) {
/* 1643 */         int d1 = Util.decodeHexDigit(encoded.charAt(i + 1));
/* 1644 */         int d2 = Util.decodeHexDigit(encoded.charAt(i + 2));
/* 1645 */         if (d1 != -1 && d2 != -1) {
/* 1646 */           out.writeByte((d1 << 4) + d2);
/* 1647 */           i += 2;
/*      */           continue;
/*      */         } 
/* 1650 */       } else if (codePoint == 43 && plusIsSpace) {
/* 1651 */         out.writeByte(32);
/*      */         continue;
/*      */       } 
/* 1654 */       out.writeUtf8CodePoint(codePoint);
/*      */       continue;
/*      */     } 
/*      */   }
/*      */   static boolean percentEncoded(String encoded, int pos, int limit) {
/* 1659 */     return (pos + 2 < limit && encoded
/* 1660 */       .charAt(pos) == '%' && 
/* 1661 */       Util.decodeHexDigit(encoded.charAt(pos + 1)) != -1 && 
/* 1662 */       Util.decodeHexDigit(encoded.charAt(pos + 2)) != -1);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   static String canonicalize(String input, int pos, int limit, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly, Charset charset) {
/*      */     int i;
/* 1686 */     for (i = pos; i < limit; i += Character.charCount(codePoint)) {
/* 1687 */       int codePoint = input.codePointAt(i);
/* 1688 */       if (codePoint < 32 || codePoint == 127 || (codePoint >= 128 && asciiOnly) || encodeSet
/*      */ 
/*      */         
/* 1691 */         .indexOf(codePoint) != -1 || (codePoint == 37 && (!alreadyEncoded || (strict && 
/* 1692 */         !percentEncoded(input, i, limit)))) || (codePoint == 43 && plusIsSpace)) {
/*      */ 
/*      */         
/* 1695 */         Buffer out = new Buffer();
/* 1696 */         out.writeUtf8(input, pos, i);
/* 1697 */         canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, charset);
/*      */         
/* 1699 */         return out.readUtf8();
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/* 1704 */     return input.substring(pos, limit);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly, Charset charset) {
/* 1710 */     Buffer encodedCharBuffer = null;
/*      */     int i;
/* 1712 */     for (i = pos; i < limit; i += Character.charCount(codePoint)) {
/* 1713 */       int codePoint = input.codePointAt(i);
/* 1714 */       if (!alreadyEncoded || (codePoint != 9 && codePoint != 10 && codePoint != 12 && codePoint != 13))
/*      */       {
/*      */         
/* 1717 */         if (codePoint == 43 && plusIsSpace) {
/*      */           
/* 1719 */           out.writeUtf8(alreadyEncoded ? "+" : "%2B");
/* 1720 */         } else if (codePoint < 32 || codePoint == 127 || (codePoint >= 128 && asciiOnly) || encodeSet
/*      */ 
/*      */           
/* 1723 */           .indexOf(codePoint) != -1 || (codePoint == 37 && (!alreadyEncoded || (strict && 
/* 1724 */           !percentEncoded(input, i, limit))))) {
/*      */           
/* 1726 */           if (encodedCharBuffer == null) {
/* 1727 */             encodedCharBuffer = new Buffer();
/*      */           }
/*      */           
/* 1730 */           if (charset == null || charset.equals(Util.UTF_8)) {
/* 1731 */             encodedCharBuffer.writeUtf8CodePoint(codePoint);
/*      */           } else {
/* 1733 */             encodedCharBuffer.writeString(input, i, i + Character.charCount(codePoint), charset);
/*      */           } 
/*      */           
/* 1736 */           while (!encodedCharBuffer.exhausted()) {
/* 1737 */             int b = encodedCharBuffer.readByte() & 0xFF;
/* 1738 */             out.writeByte(37);
/* 1739 */             out.writeByte(HEX_DIGITS[b >> 4 & 0xF]);
/* 1740 */             out.writeByte(HEX_DIGITS[b & 0xF]);
/*      */           } 
/*      */         } else {
/*      */           
/* 1744 */           out.writeUtf8CodePoint(codePoint);
/*      */         } 
/*      */       }
/*      */     } 
/*      */   }
/*      */   
/*      */   static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly, Charset charset) {
/* 1751 */     return canonicalize(input, 0, input
/* 1752 */         .length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, charset);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
/* 1758 */     return canonicalize(input, 0, input
/* 1759 */         .length(), encodeSet, alreadyEncoded, strict, plusIsSpace, asciiOnly, null);
/*      */   }
/*      */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\HttpUrl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */