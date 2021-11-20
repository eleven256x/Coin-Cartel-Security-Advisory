package okhttp3;

import javax.annotation.Nullable;
import okio.ByteString;

public abstract class WebSocketListener {
  public void onOpen(WebSocket webSocket, Response response) {}
  
  public void onMessage(WebSocket webSocket, String text) {}
  
  public void onMessage(WebSocket webSocket, ByteString bytes) {}
  
  public void onClosing(WebSocket webSocket, int code, String reason) {}
  
  public void onClosed(WebSocket webSocket, int code, String reason) {}
  
  public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {}
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\WebSocketListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */