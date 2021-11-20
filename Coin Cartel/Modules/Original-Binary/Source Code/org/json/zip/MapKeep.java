/*     */ package org.json.zip;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import org.json.Kim;
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
/*     */ class MapKeep
/*     */   extends Keep
/*     */ {
/*     */   private Object[] list;
/*     */   private HashMap map;
/*     */   
/*     */   public MapKeep(int bits) {
/*  49 */     super(bits);
/*  50 */     this.list = new Object[this.capacity];
/*  51 */     this.map = new HashMap(this.capacity);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void compact() {
/*  60 */     int from = 0;
/*  61 */     int to = 0;
/*  62 */     while (from < this.capacity) {
/*  63 */       Object key = this.list[from];
/*  64 */       long usage = age(this.uses[from]);
/*  65 */       if (usage > 0L) {
/*  66 */         this.uses[to] = usage;
/*  67 */         this.list[to] = key;
/*  68 */         this.map.put(key, new Integer(to));
/*  69 */         to++;
/*     */       } else {
/*  71 */         this.map.remove(key);
/*     */       } 
/*  73 */       from++;
/*     */     } 
/*  75 */     if (to < this.capacity) {
/*  76 */       this.length = to;
/*     */     } else {
/*  78 */       this.map.clear();
/*  79 */       this.length = 0;
/*     */     } 
/*  81 */     this.power = 0;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int find(Object key) {
/*  93 */     Object o = this.map.get(key);
/*  94 */     return (o instanceof Integer) ? ((Integer)o).intValue() : -1;
/*     */   }
/*     */   
/*     */   public boolean postMortem(PostMortem pm) {
/*  98 */     MapKeep that = (MapKeep)pm;
/*  99 */     if (this.length != that.length) {
/* 100 */       JSONzip.log(this.length + " <> " + that.length);
/* 101 */       return false;
/*     */     } 
/* 103 */     for (int i = 0; i < this.length; i++) {
/*     */       boolean b;
/* 105 */       if (this.list[i] instanceof Kim) {
/* 106 */         b = ((Kim)this.list[i]).equals(that.list[i]);
/*     */       } else {
/* 108 */         Object o = this.list[i];
/* 109 */         Object q = that.list[i];
/* 110 */         if (o instanceof Number) {
/* 111 */           o = o.toString();
/*     */         }
/* 113 */         if (q instanceof Number) {
/* 114 */           q = q.toString();
/*     */         }
/* 116 */         b = o.equals(q);
/*     */       } 
/* 118 */       if (!b) {
/* 119 */         JSONzip.log("\n[" + i + "]\n " + this.list[i] + "\n " + that.list[i] + "\n " + this.uses[i] + "\n " + that.uses[i]);
/*     */ 
/*     */         
/* 122 */         return false;
/*     */       } 
/*     */     } 
/* 125 */     return true;
/*     */   }
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
/*     */   public void register(Object value) {
/* 140 */     if (this.length >= this.capacity) {
/* 141 */       compact();
/*     */     }
/* 143 */     this.list[this.length] = value;
/* 144 */     this.map.put(value, new Integer(this.length));
/* 145 */     this.uses[this.length] = 1L;
/*     */ 
/*     */ 
/*     */     
/* 149 */     this.length++;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Object value(int integer) {
/* 158 */     return this.list[integer];
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\MapKeep.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */