/*     */ package okhttp3.internal.connection;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.net.Proxy;
/*     */ import java.net.SocketAddress;
/*     */ import java.net.SocketException;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ import java.util.NoSuchElementException;
/*     */ import okhttp3.Address;
/*     */ import okhttp3.Call;
/*     */ import okhttp3.EventListener;
/*     */ import okhttp3.HttpUrl;
/*     */ import okhttp3.Route;
/*     */ import okhttp3.internal.Util;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class RouteSelector
/*     */ {
/*     */   private final Address address;
/*     */   private final RouteDatabase routeDatabase;
/*     */   private final Call call;
/*     */   private final EventListener eventListener;
/*  47 */   private List<Proxy> proxies = Collections.emptyList();
/*     */   
/*     */   private int nextProxyIndex;
/*     */   
/*  51 */   private List<InetSocketAddress> inetSocketAddresses = Collections.emptyList();
/*     */ 
/*     */   
/*  54 */   private final List<Route> postponedRoutes = new ArrayList<>();
/*     */ 
/*     */   
/*     */   public RouteSelector(Address address, RouteDatabase routeDatabase, Call call, EventListener eventListener) {
/*  58 */     this.address = address;
/*  59 */     this.routeDatabase = routeDatabase;
/*  60 */     this.call = call;
/*  61 */     this.eventListener = eventListener;
/*     */     
/*  63 */     resetNextProxy(address.url(), address.proxy());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean hasNext() {
/*  70 */     return (hasNextProxy() || !this.postponedRoutes.isEmpty());
/*     */   }
/*     */   
/*     */   public Selection next() throws IOException {
/*  74 */     if (!hasNext()) {
/*  75 */       throw new NoSuchElementException();
/*     */     }
/*     */ 
/*     */     
/*  79 */     List<Route> routes = new ArrayList<>();
/*  80 */     while (hasNextProxy()) {
/*     */ 
/*     */ 
/*     */       
/*  84 */       Proxy proxy = nextProxy();
/*  85 */       for (int i = 0, size = this.inetSocketAddresses.size(); i < size; i++) {
/*  86 */         Route route = new Route(this.address, proxy, this.inetSocketAddresses.get(i));
/*  87 */         if (this.routeDatabase.shouldPostpone(route)) {
/*  88 */           this.postponedRoutes.add(route);
/*     */         } else {
/*  90 */           routes.add(route);
/*     */         } 
/*     */       } 
/*     */       
/*  94 */       if (!routes.isEmpty()) {
/*     */         break;
/*     */       }
/*     */     } 
/*     */     
/*  99 */     if (routes.isEmpty()) {
/*     */       
/* 101 */       routes.addAll(this.postponedRoutes);
/* 102 */       this.postponedRoutes.clear();
/*     */     } 
/*     */     
/* 105 */     return new Selection(routes);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void connectFailed(Route failedRoute, IOException failure) {
/* 113 */     if (failedRoute.proxy().type() != Proxy.Type.DIRECT && this.address.proxySelector() != null)
/*     */     {
/* 115 */       this.address.proxySelector().connectFailed(this.address
/* 116 */           .url().uri(), failedRoute.proxy().address(), failure);
/*     */     }
/*     */     
/* 119 */     this.routeDatabase.failed(failedRoute);
/*     */   }
/*     */ 
/*     */   
/*     */   private void resetNextProxy(HttpUrl url, Proxy proxy) {
/* 124 */     if (proxy != null) {
/*     */       
/* 126 */       this.proxies = Collections.singletonList(proxy);
/*     */     } else {
/*     */       
/* 129 */       List<Proxy> proxiesOrNull = this.address.proxySelector().select(url.uri());
/* 130 */       this
/*     */         
/* 132 */         .proxies = (proxiesOrNull != null && !proxiesOrNull.isEmpty()) ? Util.immutableList(proxiesOrNull) : Util.immutableList((Object[])new Proxy[] { Proxy.NO_PROXY });
/*     */     } 
/* 134 */     this.nextProxyIndex = 0;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean hasNextProxy() {
/* 139 */     return (this.nextProxyIndex < this.proxies.size());
/*     */   }
/*     */ 
/*     */   
/*     */   private Proxy nextProxy() throws IOException {
/* 144 */     if (!hasNextProxy()) {
/* 145 */       throw new SocketException("No route to " + this.address.url().host() + "; exhausted proxy configurations: " + this.proxies);
/*     */     }
/*     */     
/* 148 */     Proxy result = this.proxies.get(this.nextProxyIndex++);
/* 149 */     resetNextInetSocketAddress(result);
/* 150 */     return result;
/*     */   }
/*     */   
/*     */   private void resetNextInetSocketAddress(Proxy proxy) throws IOException {
/*     */     String socketHost;
/*     */     int socketPort;
/* 156 */     this.inetSocketAddresses = new ArrayList<>();
/*     */ 
/*     */ 
/*     */     
/* 160 */     if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.SOCKS) {
/* 161 */       socketHost = this.address.url().host();
/* 162 */       socketPort = this.address.url().port();
/*     */     } else {
/* 164 */       SocketAddress proxyAddress = proxy.address();
/* 165 */       if (!(proxyAddress instanceof InetSocketAddress)) {
/* 166 */         throw new IllegalArgumentException("Proxy.address() is not an InetSocketAddress: " + proxyAddress
/* 167 */             .getClass());
/*     */       }
/* 169 */       InetSocketAddress proxySocketAddress = (InetSocketAddress)proxyAddress;
/* 170 */       socketHost = getHostString(proxySocketAddress);
/* 171 */       socketPort = proxySocketAddress.getPort();
/*     */     } 
/*     */     
/* 174 */     if (socketPort < 1 || socketPort > 65535) {
/* 175 */       throw new SocketException("No route to " + socketHost + ":" + socketPort + "; port is out of range");
/*     */     }
/*     */ 
/*     */     
/* 179 */     if (proxy.type() == Proxy.Type.SOCKS) {
/* 180 */       this.inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
/*     */     } else {
/* 182 */       this.eventListener.dnsStart(this.call, socketHost);
/*     */ 
/*     */       
/* 185 */       List<InetAddress> addresses = this.address.dns().lookup(socketHost);
/* 186 */       if (addresses.isEmpty()) {
/* 187 */         throw new UnknownHostException(this.address.dns() + " returned no addresses for " + socketHost);
/*     */       }
/*     */       
/* 190 */       this.eventListener.dnsEnd(this.call, socketHost, addresses);
/*     */       
/* 192 */       for (int i = 0, size = addresses.size(); i < size; i++) {
/* 193 */         InetAddress inetAddress = addresses.get(i);
/* 194 */         this.inetSocketAddresses.add(new InetSocketAddress(inetAddress, socketPort));
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static String getHostString(InetSocketAddress socketAddress) {
/* 205 */     InetAddress address = socketAddress.getAddress();
/* 206 */     if (address == null)
/*     */     {
/*     */ 
/*     */       
/* 210 */       return socketAddress.getHostName();
/*     */     }
/*     */ 
/*     */     
/* 214 */     return address.getHostAddress();
/*     */   }
/*     */   
/*     */   public static final class Selection
/*     */   {
/*     */     private final List<Route> routes;
/* 220 */     private int nextRouteIndex = 0;
/*     */     
/*     */     Selection(List<Route> routes) {
/* 223 */       this.routes = routes;
/*     */     }
/*     */     
/*     */     public boolean hasNext() {
/* 227 */       return (this.nextRouteIndex < this.routes.size());
/*     */     }
/*     */     
/*     */     public Route next() {
/* 231 */       if (!hasNext()) {
/* 232 */         throw new NoSuchElementException();
/*     */       }
/* 234 */       return this.routes.get(this.nextRouteIndex++);
/*     */     }
/*     */     
/*     */     public List<Route> getAll() {
/* 238 */       return new ArrayList<>(this.routes);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\internal\connection\RouteSelector.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */