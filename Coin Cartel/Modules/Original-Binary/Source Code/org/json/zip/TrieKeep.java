/*     */ package org.json.zip;
/*     */ 
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
/*     */ class TrieKeep
/*     */   extends Keep
/*     */ {
/*     */   private int[] froms;
/*     */   private int[] thrus;
/*     */   private Node root;
/*     */   private Kim[] kims;
/*     */   
/*     */   class Node
/*     */     implements PostMortem
/*     */   {
/*     */     private int integer;
/*     */     private Node[] next;
/*     */     private final TrieKeep this$0;
/*     */     
/*     */     public Node(TrieKeep this$0) {
/*  45 */       this.this$0 = this$0;
/*  46 */       this.integer = -1;
/*  47 */       this.next = null;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Node get(int cell) {
/*  59 */       return (this.next == null) ? null : this.next[cell];
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Node get(byte cell) {
/*  71 */       return get(cell & 0xFF);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public boolean postMortem(PostMortem pm) {
/*  79 */       Node that = (Node)pm;
/*  80 */       if (that == null) {
/*  81 */         JSONzip.log("\nMisalign");
/*  82 */         return false;
/*     */       } 
/*  84 */       if (this.integer != that.integer) {
/*  85 */         JSONzip.log("\nInteger " + this.integer + " <> " + that.integer);
/*     */         
/*  87 */         return false;
/*     */       } 
/*  89 */       if (this.next == null) {
/*  90 */         if (that.next == null) {
/*  91 */           return true;
/*     */         }
/*  93 */         JSONzip.log("\nNext is null " + this.integer);
/*  94 */         return false;
/*     */       } 
/*  96 */       for (int i = 0; i < 256; i++) {
/*  97 */         Node node = this.next[i];
/*  98 */         if (node != null) {
/*  99 */           if (!node.postMortem(that.next[i])) {
/* 100 */             return false;
/*     */           }
/* 102 */         } else if (that.next[i] != null) {
/* 103 */           JSONzip.log("\nMisalign " + i);
/* 104 */           return false;
/*     */         } 
/*     */       } 
/* 107 */       return true;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void set(int cell, Node node) {
/* 119 */       if (this.next == null) {
/* 120 */         this.next = new Node[256];
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 127 */       this.next[cell] = node;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void set(byte cell, Node node) {
/* 139 */       set(cell & 0xFF, node);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Node vet(int cell) {
/* 151 */       Node node = get(cell);
/* 152 */       if (node == null) {
/* 153 */         node = new Node(this.this$0);
/* 154 */         set(cell, node);
/*     */       } 
/* 156 */       return node;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Node vet(byte cell) {
/* 168 */       return vet(cell & 0xFF);
/*     */     }
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
/*     */   
/*     */   public TrieKeep(int bits) {
/* 185 */     super(bits);
/* 186 */     this.froms = new int[this.capacity];
/* 187 */     this.thrus = new int[this.capacity];
/* 188 */     this.kims = new Kim[this.capacity];
/* 189 */     this.root = new Node(this);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Kim kim(int integer) {
/* 199 */     Kim kim = this.kims[integer];
/* 200 */     int from = this.froms[integer];
/* 201 */     int thru = this.thrus[integer];
/* 202 */     if (from != 0 || thru != kim.length) {
/* 203 */       kim = new Kim(kim, from, thru);
/* 204 */       this.froms[integer] = 0;
/* 205 */       this.thrus[integer] = kim.length;
/* 206 */       this.kims[integer] = kim;
/*     */     } 
/* 208 */     return kim;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int length(int integer) {
/* 219 */     return this.thrus[integer] - this.froms[integer];
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
/*     */   public int match(Kim kim, int from, int thru) {
/* 231 */     Node node = this.root;
/* 232 */     int best = -1;
/* 233 */     for (int at = from; at < thru; at++) {
/* 234 */       node = node.get(kim.get(at));
/* 235 */       if (node == null) {
/*     */         break;
/*     */       }
/* 238 */       if (node.integer != -1) {
/* 239 */         best = node.integer;
/*     */       }
/* 241 */       from++;
/*     */     } 
/* 243 */     return best;
/*     */   }
/*     */   
/*     */   public boolean postMortem(PostMortem pm) {
/* 247 */     boolean result = true;
/* 248 */     TrieKeep that = (TrieKeep)pm;
/* 249 */     if (this.length != that.length) {
/* 250 */       JSONzip.log("\nLength " + this.length + " <> " + that.length);
/* 251 */       return false;
/*     */     } 
/* 253 */     if (this.capacity != that.capacity) {
/* 254 */       JSONzip.log("\nCapacity " + this.capacity + " <> " + that.capacity);
/*     */       
/* 256 */       return false;
/*     */     } 
/* 258 */     for (int i = 0; i < this.length; i++) {
/* 259 */       Kim thiskim = kim(i);
/* 260 */       Kim thatkim = that.kim(i);
/* 261 */       if (!thiskim.equals(thatkim)) {
/* 262 */         JSONzip.log("\n[" + i + "] " + thiskim + " <> " + thatkim);
/* 263 */         result = false;
/*     */       } 
/*     */     } 
/* 266 */     return (result && this.root.postMortem(that.root));
/*     */   }
/*     */   
/*     */   public void registerMany(Kim kim) {
/* 270 */     int length = kim.length;
/* 271 */     int limit = this.capacity - this.length;
/* 272 */     if (limit > 40) {
/* 273 */       limit = 40;
/*     */     }
/* 275 */     int until = length - 2;
/* 276 */     for (int from = 0; from < until; from++) {
/* 277 */       int len = length - from;
/* 278 */       if (len > 10) {
/* 279 */         len = 10;
/*     */       }
/* 281 */       len += from;
/* 282 */       Node node = this.root;
/* 283 */       for (int at = from; at < len; at++) {
/* 284 */         Node next = node.vet(kim.get(at));
/* 285 */         if (next.integer == -1 && at - from >= 2) {
/*     */           
/* 287 */           next.integer = this.length;
/* 288 */           this.uses[this.length] = 1L;
/* 289 */           this.kims[this.length] = kim;
/* 290 */           this.froms[this.length] = from;
/* 291 */           this.thrus[this.length] = at + 1;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 299 */           this.length++;
/* 300 */           limit--;
/* 301 */           if (limit <= 0) {
/*     */             return;
/*     */           }
/*     */         } 
/* 305 */         node = next;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public void registerOne(Kim kim) {
/* 311 */     int integer = registerOne(kim, 0, kim.length);
/* 312 */     if (integer != -1) {
/* 313 */       this.kims[integer] = kim;
/*     */     }
/*     */   }
/*     */   
/*     */   public int registerOne(Kim kim, int from, int thru) {
/* 318 */     if (this.length < this.capacity) {
/* 319 */       Node node = this.root;
/* 320 */       for (int at = from; at < thru; at++) {
/* 321 */         node = node.vet(kim.get(at));
/*     */       }
/* 323 */       if (node.integer == -1) {
/* 324 */         int integer = this.length;
/* 325 */         node.integer = integer;
/* 326 */         this.uses[integer] = 1L;
/* 327 */         this.kims[integer] = kim;
/* 328 */         this.froms[integer] = from;
/* 329 */         this.thrus[integer] = thru;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 336 */         this.length++;
/* 337 */         return integer;
/*     */       } 
/*     */     } 
/* 340 */     return -1;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void reserve() {
/* 350 */     if (this.capacity - this.length < 40) {
/* 351 */       int from = 0;
/* 352 */       int to = 0;
/* 353 */       this.root = new Node(this);
/* 354 */       while (from < this.capacity) {
/* 355 */         if (this.uses[from] > 1L) {
/* 356 */           Kim kim = this.kims[from];
/* 357 */           int thru = this.thrus[from];
/* 358 */           Node node = this.root;
/* 359 */           for (int at = this.froms[from]; at < thru; at++) {
/* 360 */             Node next = node.vet(kim.get(at));
/* 361 */             node = next;
/*     */           } 
/* 363 */           node.integer = to;
/* 364 */           this.uses[to] = age(this.uses[from]);
/* 365 */           this.froms[to] = this.froms[from];
/* 366 */           this.thrus[to] = thru;
/* 367 */           this.kims[to] = kim;
/* 368 */           to++;
/*     */         } 
/* 370 */         from++;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 376 */       if (this.capacity - to < 40) {
/* 377 */         this.power = 0;
/* 378 */         this.root = new Node(this);
/* 379 */         to = 0;
/*     */       } 
/* 381 */       this.length = to;
/* 382 */       while (to < this.capacity) {
/* 383 */         this.uses[to] = 0L;
/* 384 */         this.kims[to] = null;
/* 385 */         this.froms[to] = 0;
/* 386 */         this.thrus[to] = 0;
/* 387 */         to++;
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public Object value(int integer) {
/* 394 */     return kim(integer);
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\zip\TrieKeep.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */