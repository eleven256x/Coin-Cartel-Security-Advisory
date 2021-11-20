/*    */ package io.socket.yeast;
/*    */ 
/*    */ import java.util.Date;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class Yeast
/*    */ {
/* 11 */   private static char[] alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_".toCharArray();
/*    */   
/* 13 */   private static int length = alphabet.length;
/* 14 */   private static int seed = 0;
/*    */   private static String prev;
/* 16 */   private static Map<Character, Integer> map = new HashMap<>(length);
/*    */   static {
/* 18 */     for (int i = 0; i < length; i++) {
/* 19 */       map.put(Character.valueOf(alphabet[i]), Integer.valueOf(i));
/*    */     }
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public static String encode(long num) {
/* 26 */     StringBuilder encoded = new StringBuilder();
/* 27 */     long dividedNum = num;
/*    */     do {
/* 29 */       encoded.insert(0, alphabet[(int)(dividedNum % length)]);
/* 30 */       dividedNum /= length;
/* 31 */     } while (dividedNum > 0L);
/*    */     
/* 33 */     return encoded.toString();
/*    */   }
/*    */   
/*    */   public static long decode(String str) {
/* 37 */     long decoded = 0L;
/*    */     
/* 39 */     for (char c : str.toCharArray()) {
/* 40 */       decoded = decoded * length + ((Integer)map.get(Character.valueOf(c))).intValue();
/*    */     }
/*    */     
/* 43 */     return decoded;
/*    */   }
/*    */   
/*    */   public static String yeast() {
/* 47 */     String now = encode((new Date()).getTime());
/*    */     
/* 49 */     if (!now.equals(prev)) {
/* 50 */       seed = 0;
/* 51 */       prev = now;
/* 52 */       return now;
/*    */     } 
/*    */     
/* 55 */     return now + "." + encode(seed++);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\socket\yeast\Yeast.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */