package okio;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface Sink extends Closeable, Flushable {
  void write(Buffer paramBuffer, long paramLong) throws IOException;
  
  void flush() throws IOException;
  
  Timeout timeout();
  
  void close() throws IOException;
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okio\Sink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */