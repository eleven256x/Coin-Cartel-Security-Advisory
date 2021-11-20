/*    */ package io.coincartel.redeem.module.actions;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import io.coincartel.redeem.module.events.objects.Stepper;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IMacroAction;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionGetLastStep extends ScriptAction {
/*    */   public ActionGetLastStep() {
/* 13 */     super(ScriptContext.MAIN, "getlaststep");
/*    */   }
/*    */ 
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/* 18 */     ReturnValue retVal = new ReturnValue(-1);
/*    */     
/* 20 */     if (params.length > 0) {
/* 21 */       Key key = Key.getKey(provider.expand(macro, params[0], false));
/*    */       
/* 23 */       if (key == null) {
/* 24 */         provider.actionAddChatMessage("Error: key not found");
/* 25 */         return (IReturnValue)retVal;
/*    */       } 
/*    */       
/* 28 */       Stepper stepper = key.getStepper();
/*    */       
/* 30 */       if (stepper == null) {
/* 31 */         provider.actionAddChatMessage("Error: stepper not found");
/* 32 */         return (IReturnValue)retVal;
/*    */       } 
/*    */       
/* 35 */       int result = stepper.getSteps().size() - 1;
/*    */       
/* 37 */       if (params.length > 1) {
/* 38 */         String variableName = params[1].toLowerCase();
/* 39 */         if (Variable.couldBeInt(variableName)) {
/* 40 */           provider.setVariable(macro, variableName, result);
/*    */         }
/*    */       } 
/*    */       
/* 44 */       if (instance.hasOutVar()) {
/* 45 */         retVal = new ReturnValue(result);
/*    */       }
/*    */     } 
/*    */ 
/*    */     
/* 50 */     return (IReturnValue)retVal;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionGetLastStep.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */