package org.json.zip;

import java.io.IOException;

public interface BitReader {
  boolean bit() throws IOException;
  
  long nrBits();
  
  boolean pad(int paramInt) throws IOException;
  
  int read(int paramInt) throws IOException;
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\BitReader.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */