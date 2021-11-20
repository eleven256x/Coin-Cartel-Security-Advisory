/*     */ package okhttp3.internal.http2;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.ProtocolException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import okhttp3.Headers;
/*     */ import okhttp3.Interceptor;
/*     */ import okhttp3.OkHttpClient;
/*     */ import okhttp3.Protocol;
/*     */ import okhttp3.Request;
/*     */ import okhttp3.Response;
/*     */ import okhttp3.ResponseBody;
/*     */ import okhttp3.internal.Internal;
/*     */ import okhttp3.internal.Util;
/*     */ import okhttp3.internal.connection.StreamAllocation;
/*     */ import okhttp3.internal.http.HttpCodec;
/*     */ import okhttp3.internal.http.HttpHeaders;
/*     */ import okhttp3.internal.http.RealResponseBody;
/*     */ import okhttp3.internal.http.RequestLine;
/*     */ import okhttp3.internal.http.StatusLine;
/*     */ import okio.Buffer;
/*     */ import okio.ByteString;
/*     */ import okio.ForwardingSource;
/*     */ import okio.Okio;
/*     */ import okio.Sink;
/*     */ import okio.Source;
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
/*     */ public final class Http2Codec
/*     */   implements HttpCodec
/*     */ {
/*     */   private static final String CONNECTION = "connection";
/*     */   private static final String HOST = "host";
/*     */   private static final String KEEP_ALIVE = "keep-alive";
/*     */   private static final String PROXY_CONNECTION = "proxy-connection";
/*     */   private static final String TRANSFER_ENCODING = "transfer-encoding";
/*     */   private static final String TE = "te";
/*     */   private static final String ENCODING = "encoding";
/*     */   private static final String UPGRADE = "upgrade";
/*  69 */   private static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList((Object[])new String[] { "connection", "host", "keep-alive", "proxy-connection", "te", "transfer-encoding", "encoding", "upgrade", ":method", ":path", ":scheme", ":authority" });
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
/*  82 */   private static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList((Object[])new String[] { "connection", "host", "keep-alive", "proxy-connection", "te", "transfer-encoding", "encoding", "upgrade" });
/*     */ 
/*     */   
/*     */   private final Interceptor.Chain chain;
/*     */ 
/*     */   
/*     */   final StreamAllocation streamAllocation;
/*     */ 
/*     */   
/*     */   private final Http2Connection connection;
/*     */ 
/*     */   
/*     */   private Http2Stream stream;
/*     */   
/*     */   private final Protocol protocol;
/*     */ 
/*     */   
/*     */   public Http2Codec(OkHttpClient client, Interceptor.Chain chain, StreamAllocation streamAllocation, Http2Connection connection) {
/* 100 */     this.chain = chain;
/* 101 */     this.streamAllocation = streamAllocation;
/* 102 */     this.connection = connection;
/* 103 */     this
/*     */       
/* 105 */       .protocol = client.protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE) ? Protocol.H2_PRIOR_KNOWLEDGE : Protocol.HTTP_2;
/*     */   }
/*     */   
/*     */   public Sink createRequestBody(Request request, long contentLength) {
/* 109 */     return this.stream.getSink();
/*     */   }
/*     */   
/*     */   public void writeRequestHeaders(Request request) throws IOException {
/* 113 */     if (this.stream != null)
/*     */       return; 
/* 115 */     boolean hasRequestBody = (request.body() != null);
/* 116 */     List<Header> requestHeaders = http2HeadersList(request);
/* 117 */     this.stream = this.connection.newStream(requestHeaders, hasRequestBody);
/* 118 */     this.stream.readTimeout().timeout(this.chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
/* 119 */     this.stream.writeTimeout().timeout(this.chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
/*     */   }
/*     */   
/*     */   public void flushRequest() throws IOException {
/* 123 */     this.connection.flush();
/*     */   }
/*     */   
/*     */   public void finishRequest() throws IOException {
/* 127 */     this.stream.getSink().close();
/*     */   }
/*     */   
/*     */   public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
/* 131 */     Headers headers = this.stream.takeHeaders();
/* 132 */     Response.Builder responseBuilder = readHttp2HeadersList(headers, this.protocol);
/* 133 */     if (expectContinue && Internal.instance.code(responseBuilder) == 100) {
/* 134 */       return null;
/*     */     }
/* 136 */     return responseBuilder;
/*     */   }
/*     */   
/*     */   public static List<Header> http2HeadersList(Request request) {
/* 140 */     Headers headers = request.headers();
/* 141 */     List<Header> result = new ArrayList<>(headers.size() + 4);
/* 142 */     result.add(new Header(Header.TARGET_METHOD, request.method()));
/* 143 */     result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.url())));
/* 144 */     String host = request.header("Host");
/* 145 */     if (host != null) {
/* 146 */       result.add(new Header(Header.TARGET_AUTHORITY, host));
/*     */     }
/* 148 */     result.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));
/*     */     
/* 150 */     for (int i = 0, size = headers.size(); i < size; i++) {
/*     */       
/* 152 */       ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
/* 153 */       if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name.utf8())) {
/* 154 */         result.add(new Header(name, headers.value(i)));
/*     */       }
/*     */     } 
/* 157 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static Response.Builder readHttp2HeadersList(Headers headerBlock, Protocol protocol) throws IOException {
/* 163 */     StatusLine statusLine = null;
/* 164 */     Headers.Builder headersBuilder = new Headers.Builder();
/* 165 */     for (int i = 0, size = headerBlock.size(); i < size; i++) {
/* 166 */       String name = headerBlock.name(i);
/* 167 */       String value = headerBlock.value(i);
/* 168 */       if (name.equals(":status")) {
/* 169 */         statusLine = StatusLine.parse("HTTP/1.1 " + value);
/* 170 */       } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
/* 171 */         Internal.instance.addLenient(headersBuilder, name, value);
/*     */       } 
/*     */     } 
/* 174 */     if (statusLine == null) throw new ProtocolException("Expected ':status' header not present");
/*     */     
/* 176 */     return (new Response.Builder())
/* 177 */       .protocol(protocol)
/* 178 */       .code(statusLine.code)
/* 179 */       .message(statusLine.message)
/* 180 */       .headers(headersBuilder.build());
/*     */   }
/*     */   
/*     */   public ResponseBody openResponseBody(Response response) throws IOException {
/* 184 */     this.streamAllocation.eventListener.responseBodyStart(this.streamAllocation.call);
/* 185 */     String contentType = response.header("Content-Type");
/* 186 */     long contentLength = HttpHeaders.contentLength(response);
/* 187 */     StreamFinishingSource streamFinishingSource = new StreamFinishingSource(this.stream.getSource());
/* 188 */     return (ResponseBody)new RealResponseBody(contentType, contentLength, Okio.buffer((Source)streamFinishingSource));
/*     */   }
/*     */   
/*     */   public void cancel() {
/* 192 */     if (this.stream != null) this.stream.closeLater(ErrorCode.CANCEL); 
/*     */   }
/*     */   
/*     */   class StreamFinishingSource extends ForwardingSource {
/*     */     boolean completed = false;
/* 197 */     long bytesRead = 0L;
/*     */     
/*     */     StreamFinishingSource(Source delegate) {
/* 200 */       super(delegate);
/*     */     }
/*     */     
/*     */     public long read(Buffer sink, long byteCount) throws IOException {
/*     */       try {
/* 205 */         long read = delegate().read(sink, byteCount);
/* 206 */         if (read > 0L) {
/* 207 */           this.bytesRead += read;
/*     */         }
/* 209 */         return read;
/* 210 */       } catch (IOException e) {
/* 211 */         endOfInput(e);
/* 212 */         throw e;
/*     */       } 
/*     */     }
/*     */     
/*     */     public void close() throws IOException {
/* 217 */       super.close();
/* 218 */       endOfInput(null);
/*     */     }
/*     */     
/*     */     private void endOfInput(IOException e) {
/* 222 */       if (this.completed)
/* 223 */         return;  this.completed = true;
/* 224 */       Http2Codec.this.streamAllocation.streamFinished(false, Http2Codec.this, this.bytesRead, e);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Http2Codec.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */