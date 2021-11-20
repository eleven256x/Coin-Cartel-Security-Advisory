/*    */ package okhttp3.internal.http2;
/*    */ 
/*    */ import okhttp3.Headers;
/*    */ import okhttp3.internal.Util;
/*    */ import okio.ByteString;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class Header
/*    */ {
/* 25 */   public static final ByteString PSEUDO_PREFIX = ByteString.encodeUtf8(":");
/*    */   
/*    */   public static final String RESPONSE_STATUS_UTF8 = ":status";
/*    */   
/*    */   public static final String TARGET_METHOD_UTF8 = ":method";
/*    */   public static final String TARGET_PATH_UTF8 = ":path";
/*    */   public static final String TARGET_SCHEME_UTF8 = ":scheme";
/*    */   public static final String TARGET_AUTHORITY_UTF8 = ":authority";
/* 33 */   public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(":status");
/* 34 */   public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(":method");
/* 35 */   public static final ByteString TARGET_PATH = ByteString.encodeUtf8(":path");
/* 36 */   public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(":scheme");
/* 37 */   public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(":authority");
/*    */ 
/*    */   
/*    */   public final ByteString name;
/*    */   
/*    */   public final ByteString value;
/*    */   
/*    */   final int hpackSize;
/*    */ 
/*    */   
/*    */   public Header(String name, String value) {
/* 48 */     this(ByteString.encodeUtf8(name), ByteString.encodeUtf8(value));
/*    */   }
/*    */   
/*    */   public Header(ByteString name, String value) {
/* 52 */     this(name, ByteString.encodeUtf8(value));
/*    */   }
/*    */   
/*    */   public Header(ByteString name, ByteString value) {
/* 56 */     this.name = name;
/* 57 */     this.value = value;
/* 58 */     this.hpackSize = 32 + name.size() + value.size();
/*    */   }
/*    */   
/*    */   public boolean equals(Object other) {
/* 62 */     if (other instanceof Header) {
/* 63 */       Header that = (Header)other;
/* 64 */       return (this.name.equals(that.name) && this.value
/* 65 */         .equals(that.value));
/*    */     } 
/* 67 */     return false;
/*    */   }
/*    */   
/*    */   public int hashCode() {
/* 71 */     int result = 17;
/* 72 */     result = 31 * result + this.name.hashCode();
/* 73 */     result = 31 * result + this.value.hashCode();
/* 74 */     return result;
/*    */   }
/*    */   
/*    */   public String toString() {
/* 78 */     return Util.format("%s: %s", new Object[] { this.name.utf8(), this.value.utf8() });
/*    */   }
/*    */   
/*    */   static interface Listener {
/*    */     void onHeaders(Headers param1Headers);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\http2\Header.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */