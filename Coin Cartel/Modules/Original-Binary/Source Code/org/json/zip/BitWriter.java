package org.json.zip;

import java.io.IOException;

public interface BitWriter {
  long nrBits();
  
  void one() throws IOException;
  
  void pad(int paramInt) throws IOException;
  
  void write(int paramInt1, int paramInt2) throws IOException;
  
  void zero() throws IOException;
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\BitWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */