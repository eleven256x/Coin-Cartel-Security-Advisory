package okhttp3;

import javax.annotation.Nullable;
import okio.ByteString;

public interface WebSocket {
  Request request();
  
  long queueSize();
  
  boolean send(String paramString);
  
  boolean send(ByteString paramByteString);
  
  boolean close(int paramInt, @Nullable String paramString);
  
  void cancel();
  
  public static interface Factory {
    WebSocket newWebSocket(Request param1Request, WebSocketListener param1WebSocketListener);
  }
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\WebSocket.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */