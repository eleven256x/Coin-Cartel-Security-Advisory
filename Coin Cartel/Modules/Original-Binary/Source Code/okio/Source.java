package okio;

import java.io.Closeable;
import java.io.IOException;

public interface Source extends Closeable {
  long read(Buffer paramBuffer, long paramLong) throws IOException;
  
  Timeout timeout();
  
  void close() throws IOException;
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Source.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */