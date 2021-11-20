/*    */ package okhttp3;
/*    */ 
/*    */ import java.net.InetSocketAddress;
/*    */ import java.net.Proxy;
/*    */ import javax.annotation.Nullable;
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
/*    */ public final class Route
/*    */ {
/*    */   final Address address;
/*    */   final Proxy proxy;
/*    */   final InetSocketAddress inetSocketAddress;
/*    */   
/*    */   public Route(Address address, Proxy proxy, InetSocketAddress inetSocketAddress) {
/* 43 */     if (address == null) {
/* 44 */       throw new NullPointerException("address == null");
/*    */     }
/* 46 */     if (proxy == null) {
/* 47 */       throw new NullPointerException("proxy == null");
/*    */     }
/* 49 */     if (inetSocketAddress == null) {
/* 50 */       throw new NullPointerException("inetSocketAddress == null");
/*    */     }
/* 52 */     this.address = address;
/* 53 */     this.proxy = proxy;
/* 54 */     this.inetSocketAddress = inetSocketAddress;
/*    */   }
/*    */   
/*    */   public Address address() {
/* 58 */     return this.address;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Proxy proxy() {
/* 68 */     return this.proxy;
/*    */   }
/*    */   
/*    */   public InetSocketAddress socketAddress() {
/* 72 */     return this.inetSocketAddress;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public boolean requiresTunnel() {
/* 80 */     return (this.address.sslSocketFactory != null && this.proxy.type() == Proxy.Type.HTTP);
/*    */   }
/*    */   
/*    */   public boolean equals(@Nullable Object other) {
/* 84 */     return (other instanceof Route && ((Route)other).address
/* 85 */       .equals(this.address) && ((Route)other).proxy
/* 86 */       .equals(this.proxy) && ((Route)other).inetSocketAddress
/* 87 */       .equals(this.inetSocketAddress));
/*    */   }
/*    */   
/*    */   public int hashCode() {
/* 91 */     int result = 17;
/* 92 */     result = 31 * result + this.address.hashCode();
/* 93 */     result = 31 * result + this.proxy.hashCode();
/* 94 */     result = 31 * result + this.inetSocketAddress.hashCode();
/* 95 */     return result;
/*    */   }
/*    */   
/*    */   public String toString() {
/* 99 */     return "Route{" + this.inetSocketAddress + "}";
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\Route.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */