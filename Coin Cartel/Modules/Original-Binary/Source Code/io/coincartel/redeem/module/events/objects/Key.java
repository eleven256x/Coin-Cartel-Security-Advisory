/*     */ package io.coincartel.redeem.module.events.objects;
/*     */ 
/*     */ import com.google.gson.annotations.Expose;
/*     */ import com.google.gson.annotations.SerializedName;
/*     */ import java.io.Serializable;
/*     */ import java.util.HashMap;
/*     */ import net.eq2online.macros.core.Macros;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Key
/*     */   implements Serializable
/*     */ {
/*     */   @SerializedName("key")
/*     */   @Expose
/*     */   private String key;
/*     */   @SerializedName("quantityTotal")
/*     */   @Expose
/*     */   private Integer quantityTotal;
/*     */   @SerializedName("quantityRemaining")
/*     */   @Expose
/*     */   private Integer quantityRemaining;
/*     */   @SerializedName("minecraftUUID")
/*     */   @Expose
/*     */   private String minecraftUUID;
/*     */   @SerializedName("discordUsername")
/*     */   @Expose
/*     */   private String discordUsername;
/*     */   @SerializedName("stepper")
/*     */   @Expose
/*     */   private Stepper stepper;
/*     */   private static final long serialVersionUID = -6937044886086308966L;
/*  33 */   private static HashMap<String, Key> keys = new HashMap<>();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Key() {}
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Key(String key, Integer quantityTotal, String minecraftUUID, String discordUsername, Stepper stepper) {
/*  52 */     this.key = key;
/*  53 */     this.quantityTotal = quantityTotal;
/*  54 */     this.quantityRemaining = this.quantityRemaining;
/*  55 */     this.minecraftUUID = minecraftUUID;
/*  56 */     this.discordUsername = discordUsername;
/*  57 */     this.stepper = stepper;
/*     */   }
/*     */   
/*     */   public String getKey() {
/*  61 */     return this.key;
/*     */   }
/*     */   
/*     */   public void setKey(String key) {
/*  65 */     this.key = key;
/*     */   }
/*     */   
/*     */   public Integer getQuantityTotal() {
/*  69 */     return this.quantityTotal;
/*     */   }
/*     */   
/*     */   public void setQuantityTotal(Integer quantity) {
/*  73 */     this.quantityTotal = quantity;
/*     */   }
/*     */   
/*     */   public Integer getQuantityRemaining() {
/*  77 */     return this.quantityRemaining;
/*     */   }
/*     */   
/*     */   public void setQuantityRemaining(Integer quantity) {
/*  81 */     this.quantityTotal = quantity;
/*     */   }
/*     */   
/*     */   public String getMinecraftUUID() {
/*  85 */     return this.minecraftUUID;
/*     */   }
/*     */   
/*     */   public void setMinecraftUUID(String minecraftUUID) {
/*  89 */     this.minecraftUUID = minecraftUUID;
/*     */   }
/*     */   
/*     */   public String getDiscordUsername() {
/*  93 */     return this.discordUsername;
/*     */   }
/*     */   
/*     */   public void setDiscordUsername(String discordUsername) {
/*  97 */     this.discordUsername = discordUsername;
/*     */   }
/*     */   
/*     */   public Stepper getStepper() {
/* 101 */     return this.stepper;
/*     */   }
/*     */   
/*     */   public void setStepper(Stepper stepper) {
/* 105 */     this.stepper = stepper;
/*     */   }
/*     */   
/*     */   public static Key getKey(String keyID) {
/* 109 */     return keys.get(keyID);
/*     */   }
/*     */   
/*     */   public void callEvent() {
/* 113 */     keys.put(this.key, this);
/* 114 */     Macros.getInstance().getEventManager().sendEvent("onRedeem", 100, new String[] { this.key, String.valueOf(this.quantityTotal), String.valueOf(this.quantityRemaining), this.minecraftUUID, this.discordUsername });
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\events\objects\Key.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */