package okhttp3;

import java.net.Socket;
import javax.annotation.Nullable;

public interface Connection {
  Route route();
  
  Socket socket();
  
  @Nullable
  Handshake handshake();
  
  Protocol protocol();
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Connection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */