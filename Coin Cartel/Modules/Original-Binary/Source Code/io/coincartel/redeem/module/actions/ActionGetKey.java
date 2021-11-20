/*    */ package io.coincartel.redeem.module.actions;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionGetKey extends ScriptAction {
/*    */   public ActionGetKey() {
/* 11 */     super(ScriptContext.MAIN, "getkey");
/*    */   }
/*    */ 
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/* 16 */     if (params.length > 0) {
/* 17 */       Key key = Key.getKey(provider.expand(macro, params[0], false));
/*    */       
/* 19 */       if (key == null) {
/* 20 */         provider.actionAddChatMessage("Error: key not found");
/* 21 */         return (IReturnValue)new ReturnValue(false);
/*    */       } 
/*    */       
/* 24 */       if (params.length > 1) {
/* 25 */         provider.setVariable(macro, provider.expand(macro, params[1], false), key.getQuantityTotal().intValue());
/*    */       }
/*    */       
/* 28 */       if (params.length > 2) {
/* 29 */         provider.setVariable(macro, provider.expand(macro, params[2], false), key.getQuantityRemaining().intValue());
/*    */       }
/*    */       
/* 32 */       if (params.length > 3) {
/* 33 */         provider.setVariable(macro, provider.expand(macro, params[3], false), key.getMinecraftUUID());
/*    */       }
/*    */       
/* 36 */       if (params.length > 4) {
/* 37 */         provider.setVariable(macro, provider.expand(macro, params[4], false), key.getDiscordUsername());
/*    */       }
/*    */     } 
/*    */     
/* 41 */     return (IReturnValue)new ReturnValue(false);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionGetKey.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */