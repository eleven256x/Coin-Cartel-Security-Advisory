/*    */ package io.coincartel.redeem.module.actions;
/*    */ import com.google.gson.Gson;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import io.coincartel.redeem.module.events.objects.Step;
/*    */ import io.coincartel.redeem.module.events.objects.Stepper;
/*    */ import io.socket.client.Socket;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IMacroAction;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ import net.eq2online.macros.scripting.parser.ScriptContext;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionEditStep extends ScriptAction {
/*    */   final Gson gson;
/*    */   
/*    */   public ActionEditStep(Gson gson, Socket socket) {
/* 19 */     super(ScriptContext.MAIN, "editstep");
/* 20 */     this.gson = gson;
/* 21 */     this.socket = socket;
/*    */   }
/*    */   final Socket socket;
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/* 26 */     if (params.length > 1) {
/* 27 */       Key key = Key.getKey(provider.expand(macro, params[0], false));
/*    */       
/* 29 */       if (key == null) {
/* 30 */         provider.actionAddChatMessage("Error: key not found");
/* 31 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 34 */       Stepper stepper = key.getStepper();
/*    */       
/* 36 */       if (stepper == null) {
/* 37 */         provider.actionAddChatMessage("Error: stepper not found");
/* 38 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 41 */       Step step = stepper.getStep(Integer.parseInt(provider.expand(macro, params[1], false)));
/*    */       
/* 43 */       if (params.length > 2) {
/* 44 */         step.setTitle(provider.expand(macro, params[2], false));
/*    */       }
/*    */       
/* 47 */       if (params.length > 3) {
/* 48 */         step.setMessage(provider.expand(macro, params[3], false));
/*    */       }
/*    */       
/* 51 */       if (params.length > 4) {
/* 52 */         step.setCompleted(Boolean.valueOf(Boolean.parseBoolean(provider.expand(macro, params[4], false))));
/*    */       }
/*    */       
/* 55 */       if (params.length > 5) {
/* 56 */         step.setCanProceed(Boolean.parseBoolean(provider.expand(macro, params[5], false)));
/*    */       }
/*    */       
/* 59 */       stepper.updateStep(Integer.parseInt(provider.expand(macro, params[1], false)), step);
/*    */       
/* 61 */       this.socket.emit("updateStepper", new Object[] { key.getKey(), this.gson.toJson(key.getStepper()) });
/*    */       
/* 63 */       return (IReturnValue)new ReturnValue(true);
/*    */     } 
/* 65 */     return (IReturnValue)new ReturnValue(false);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionEditStep.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */