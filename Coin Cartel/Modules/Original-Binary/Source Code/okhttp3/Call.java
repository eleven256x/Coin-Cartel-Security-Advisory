package okhttp3;

import java.io.IOException;
import okio.Timeout;

public interface Call extends Cloneable {
  Request request();
  
  Response execute() throws IOException;
  
  void enqueue(Callback paramCallback);
  
  void cancel();
  
  boolean isExecuted();
  
  boolean isCanceled();
  
  Timeout timeout();
  
  Call clone();
  
  public static interface Factory {
    Call newCall(Request param1Request);
  }
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Call.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */