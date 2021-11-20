package okhttp3.internal.cache;

import java.io.IOException;
import okio.Sink;

public interface CacheRequest {
  Sink body() throws IOException;
  
  void abort();
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\cache\CacheRequest.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */