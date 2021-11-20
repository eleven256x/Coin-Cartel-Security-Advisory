package okhttp3.internal.http;

import java.io.IOException;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Sink;

public interface HttpCodec {
  public static final int DISCARD_STREAM_TIMEOUT_MILLIS = 100;
  
  Sink createRequestBody(Request paramRequest, long paramLong);
  
  void writeRequestHeaders(Request paramRequest) throws IOException;
  
  void flushRequest() throws IOException;
  
  void finishRequest() throws IOException;
  
  Response.Builder readResponseHeaders(boolean paramBoolean) throws IOException;
  
  ResponseBody openResponseBody(Response paramResponse) throws IOException;
  
  void cancel();
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http\HttpCodec.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */