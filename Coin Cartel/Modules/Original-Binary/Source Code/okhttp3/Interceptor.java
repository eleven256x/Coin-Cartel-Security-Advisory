package okhttp3;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public interface Interceptor {
  Response intercept(Chain paramChain) throws IOException;
  
  public static interface Chain {
    Request request();
    
    Response proceed(Request param1Request) throws IOException;
    
    @Nullable
    Connection connection();
    
    Call call();
    
    int connectTimeoutMillis();
    
    Chain withConnectTimeout(int param1Int, TimeUnit param1TimeUnit);
    
    int readTimeoutMillis();
    
    Chain withReadTimeout(int param1Int, TimeUnit param1TimeUnit);
    
    int writeTimeoutMillis();
    
    Chain withWriteTimeout(int param1Int, TimeUnit param1TimeUnit);
  }
}


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Interceptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */