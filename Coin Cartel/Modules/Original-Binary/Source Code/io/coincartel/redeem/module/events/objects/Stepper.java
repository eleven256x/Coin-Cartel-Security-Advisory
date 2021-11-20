/*    */ package io.coincartel.redeem.module.events.objects;
/*    */ 
/*    */ import com.google.gson.annotations.Expose;
/*    */ import com.google.gson.annotations.SerializedName;
/*    */ import java.io.Serializable;
/*    */ import java.util.List;
/*    */ 
/*    */ public class Stepper
/*    */   implements Serializable {
/*    */   private static final long serialVersionUID = 2211001002298758762L;
/*    */   @SerializedName("currentStep")
/*    */   @Expose
/*    */   private Integer currentStep;
/*    */   @SerializedName("steps")
/*    */   @Expose
/* 16 */   private List<Step> steps = null;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Stepper() {}
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public Stepper(Integer currentStep, List<Step> steps) {
/* 32 */     this.currentStep = currentStep;
/* 33 */     this.steps = steps;
/*    */   }
/*    */   
/*    */   public Integer getCurrentStep() {
/* 37 */     return this.currentStep;
/*    */   }
/*    */   
/*    */   public void setCurrentStep(Integer currentStep) {
/* 41 */     this.currentStep = currentStep;
/*    */   }
/*    */   
/*    */   public List<Step> getSteps() {
/* 45 */     return this.steps;
/*    */   }
/*    */   
/*    */   public void setSteps(List<Step> steps) {
/* 49 */     this.steps = steps;
/*    */   }
/*    */   
/*    */   public Step getStep(int step) {
/* 53 */     return this.steps.get(step);
/*    */   }
/*    */   
/*    */   public Step updateStep(int step, Step updatedStep) {
/* 57 */     return this.steps.set(step, updatedStep);
/*    */   }
/*    */   
/*    */   public void addStep(String title, String message) {
/* 61 */     this.steps.add(new Step(title, message, Boolean.valueOf(false), Boolean.valueOf(false)));
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\events\objects\Stepper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */