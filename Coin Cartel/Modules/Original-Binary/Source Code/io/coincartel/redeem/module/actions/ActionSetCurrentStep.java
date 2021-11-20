/*    */ package io.coincartel.redeem.module.actions;
/*    */ import com.google.gson.Gson;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import io.coincartel.redeem.module.events.objects.Stepper;
/*    */ import io.socket.client.Socket;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IMacroAction;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionSetCurrentStep extends ScriptAction {
/*    */   final Gson gson;
/*    */   
/*    */   public ActionSetCurrentStep(Gson gson, Socket socket) {
/* 17 */     super(ScriptContext.MAIN, "setcurrentstep");
/* 18 */     this.gson = gson;
/* 19 */     this.socket = socket;
/*    */   }
/*    */   final Socket socket;
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/* 24 */     if (params.length > 1) {
/* 25 */       Key key = Key.getKey(provider.expand(macro, params[0], false));
/*    */       
/* 27 */       if (key == null) {
/* 28 */         provider.actionAddChatMessage("Error: key not found");
/* 29 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 32 */       Stepper stepper = key.getStepper();
/*    */       
/* 34 */       if (stepper == null) {
/* 35 */         provider.actionAddChatMessage("Error: stepper not found");
/* 36 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 39 */       stepper.setCurrentStep(Integer.valueOf(Integer.parseInt(provider.expand(macro, params[1], false))));
/*    */       
/* 41 */       this.socket.emit("updateStepper", new Object[] { key.getKey(), this.gson.toJson(key.getStepper()) });
/* 42 */       return (IReturnValue)new ReturnValue(true);
/*    */     } 
/* 44 */     return (IReturnValue)new ReturnValue(-1);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionSetCurrentStep.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */