/*    */ package io.coincartel.redeem.module.events.objects;
/*    */ 
/*    */ import com.google.gson.annotations.Expose;
/*    */ import com.google.gson.annotations.SerializedName;
/*    */ import java.io.Serializable;
/*    */ import net.eq2online.macros.core.Macros;
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
/*    */ public class StepAck
/*    */   implements Serializable
/*    */ {
/*    */   @SerializedName("key")
/*    */   @Expose
/*    */   private String key;
/*    */   @SerializedName("stepIndex")
/*    */   @Expose
/*    */   private Integer stepIndex;
/*    */   @SerializedName("stepTitle")
/*    */   @Expose
/*    */   private String stepTitle;
/*    */   @SerializedName("stepMessage")
/*    */   @Expose
/*    */   private String stepMessage;
/*    */   private static final long serialVersionUID = -6937044886086308967L;
/*    */   
/*    */   public StepAck() {}
/*    */   
/*    */   public StepAck(String key, Integer stepIndex, String stepTitle, String stepMessage) {
/* 42 */     this.key = key;
/* 43 */     this.stepIndex = stepIndex;
/* 44 */     this.stepTitle = stepTitle;
/* 45 */     this.stepMessage = stepMessage;
/*    */   }
/*    */   
/*    */   public void callEvent() {
/* 49 */     Macros.getInstance().getEventManager().sendEvent("onStepAck", 100, new String[] { this.key, String.valueOf(this.stepIndex), this.stepTitle, this.stepMessage });
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\events\objects\StepAck.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */