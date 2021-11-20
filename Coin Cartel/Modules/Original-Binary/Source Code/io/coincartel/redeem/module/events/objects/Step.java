/*    */ package io.coincartel.redeem.module.events.objects;
/*    */ 
/*    */ import com.google.gson.annotations.Expose;
/*    */ import com.google.gson.annotations.SerializedName;
/*    */ import java.io.Serializable;
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
/*    */ public class Step
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = -2285777339818335204L;
/*    */   @SerializedName("title")
/*    */   @Expose
/*    */   private String title;
/*    */   @SerializedName("message")
/*    */   @Expose
/*    */   private String message;
/*    */   @SerializedName("completed")
/*    */   @Expose
/*    */   private Boolean completed;
/*    */   @SerializedName("canProceed")
/*    */   @Expose
/*    */   private Boolean canProceed;
/*    */   
/*    */   public Step() {}
/*    */   
/*    */   public Step(String title, String message, Boolean completed, Boolean canProceed) {
/* 38 */     this.title = title;
/* 39 */     this.message = message;
/* 40 */     this.completed = completed;
/* 41 */     this.canProceed = canProceed;
/*    */   }
/*    */   
/*    */   public String getTitle() {
/* 45 */     return this.title;
/*    */   }
/*    */   
/*    */   public void setTitle(String title) {
/* 49 */     this.title = title;
/*    */   }
/*    */   
/*    */   public String getMessage() {
/* 53 */     return this.message;
/*    */   }
/*    */   
/*    */   public void setMessage(String message) {
/* 57 */     this.message = message;
/*    */   }
/*    */   
/*    */   public Boolean getCompleted() {
/* 61 */     return this.completed;
/*    */   }
/*    */   
/*    */   public void setCompleted(Boolean completed) {
/* 65 */     this.completed = completed;
/*    */   }
/*    */   
/*    */   public Boolean getCanProceed() {
/* 69 */     return this.canProceed;
/*    */   }
/*    */   public void setCanProceed(boolean canProceed) {
/* 72 */     this.canProceed = Boolean.valueOf(canProceed);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\events\objects\Step.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */