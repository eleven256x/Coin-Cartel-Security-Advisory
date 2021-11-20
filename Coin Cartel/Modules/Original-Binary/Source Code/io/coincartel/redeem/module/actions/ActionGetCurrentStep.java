/*    */ package io.coincartel.redeem.module.actions;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import io.coincartel.redeem.module.events.objects.Step;
/*    */ import io.coincartel.redeem.module.events.objects.Stepper;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionGetCurrentStep extends ScriptAction {
/*    */   public ActionGetCurrentStep() {
/* 13 */     super(ScriptContext.MAIN, "getcurrentstep");
/*    */   }
/*    */ 
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/* 18 */     if (params.length > 1) {
/* 19 */       Key key = Key.getKey(provider.expand(macro, params[0], false));
/*    */       
/* 21 */       if (key == null) {
/* 22 */         provider.actionAddChatMessage("Error: key not found");
/* 23 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 26 */       Stepper stepper = key.getStepper();
/*    */       
/* 28 */       if (stepper == null) {
/* 29 */         provider.actionAddChatMessage("Error: stepper not found");
/* 30 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 33 */       Step step = stepper.getStep(stepper.getCurrentStep().intValue());
/*    */       
/* 35 */       if (params.length > 2) {
/* 36 */         provider.setVariable(macro, provider.expand(macro, params[2], false), step.getTitle());
/*    */       }
/*    */       
/* 39 */       if (params.length > 3) {
/* 40 */         provider.setVariable(macro, provider.expand(macro, params[3], false), step.getMessage());
/*    */       }
/*    */       
/* 43 */       if (params.length > 4) {
/* 44 */         provider.setVariable(macro, provider.expand(macro, params[4], false), String.valueOf(step.getCompleted()), step.getCompleted().booleanValue() ? 1 : 0, step.getCompleted().booleanValue());
/*    */       }
/*    */       
/* 47 */       return (IReturnValue)new ReturnValue(true);
/*    */     } 
/* 49 */     return (IReturnValue)new ReturnValue(-1);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionGetCurrentStep.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */