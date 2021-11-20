/*    */ package io.coincartel.redeem.module.actions;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import io.coincartel.redeem.module.events.objects.Step;
/*    */ import io.coincartel.redeem.module.events.objects.Stepper;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ import net.eq2online.macros.scripting.parser.ScriptCore;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionGetStep extends ScriptAction {
/*    */   public ActionGetStep() {
/* 14 */     super(ScriptContext.MAIN, "getstep");
/*    */   }
/*    */ 
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/* 19 */     if (params.length > 1) {
/* 20 */       Key key = Key.getKey(provider.expand(macro, params[0], false));
/*    */       
/* 22 */       if (key == null) {
/* 23 */         provider.actionAddChatMessage("Error: key not found");
/* 24 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 27 */       Stepper stepper = key.getStepper();
/*    */       
/* 29 */       if (stepper == null) {
/* 30 */         provider.actionAddChatMessage("Error: stepper not found");
/* 31 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 34 */       Step step = stepper.getStep(ScriptCore.tryParseInt(provider.expand(macro, params[1], false), 0));
/*    */       
/* 36 */       if (params.length > 2) {
/* 37 */         String variableName = params[2].toLowerCase();
/* 38 */         provider.setVariable(macro, variableName, step.getTitle());
/*    */       } 
/*    */       
/* 41 */       if (params.length > 3) {
/* 42 */         provider.setVariable(macro, params[3].toLowerCase(), step.getMessage());
/*    */       }
/*    */       
/* 45 */       if (params.length > 4) {
/* 46 */         String variableName = params[4].toLowerCase();
/* 47 */         provider.setVariable(macro, variableName, String.valueOf(step.getCompleted()), step.getCompleted().booleanValue() ? 1 : 0, step.getCompleted().booleanValue());
/*    */       } 
/*    */       
/* 50 */       return (IReturnValue)new ReturnValue(true);
/*    */     } 
/* 52 */     return (IReturnValue)new ReturnValue(false);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionGetStep.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */