/*    */ package io.coincartel.redeem.module.actions;
/*    */ import com.google.gson.Gson;
/*    */ import io.socket.client.Socket;
/*    */ import java.net.HttpURLConnection;
/*    */ import java.net.URL;
/*    */ import net.eq2online.macros.scripting.VariableExpander;
/*    */ import net.eq2online.macros.scripting.api.APIVersion;
/*    */ import net.eq2online.macros.scripting.api.IMacro;
/*    */ import net.eq2online.macros.scripting.api.IMacroAction;
/*    */ import net.eq2online.macros.scripting.api.IReturnValue;
/*    */ import net.eq2online.macros.scripting.api.IScriptActionProvider;
/*    */ import net.eq2online.macros.scripting.api.ReturnValue;
/*    */ import net.eq2online.macros.scripting.parser.ScriptAction;
/*    */ import net.eq2online.macros.scripting.parser.ScriptContext;
/*    */ import net.minecraft.client.Minecraft;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ActionMarkDelivered extends ScriptAction {
/*    */   public ActionMarkDelivered(Gson gson, Socket socket) {
/* 20 */     super(ScriptContext.MAIN, "markdelivered");
/* 21 */     this.gson = gson;
/* 22 */     this.socket = socket;
/*    */   }
/*    */   final Gson gson;
/*    */   final Socket socket;
/*    */   
/*    */   public IReturnValue execute(IScriptActionProvider provider, IMacro macro, IMacroAction instance, String rawParams, String[] params) {
/*    */     try {
/* 29 */       String key = (new VariableExpander(provider, macro, params[0], false)).toString();
/* 30 */       String amount = (new VariableExpander(provider, macro, params[1], false)).toString();
/*    */       
/* 32 */       HttpURLConnection con = (HttpURLConnection)(new URL(ScriptActionBase.socketUrl + "api/key/delivered/" + key + "/" + amount)).openConnection();
/* 33 */       con.setRequestMethod("GET");
/* 34 */       con.setRequestProperty("User-Agent", "CoinCartel");
/* 35 */       con.setRequestProperty("Authorization", Minecraft.func_71410_x().func_110432_I().func_148254_d());
/* 36 */       return (IReturnValue)new ReturnValue((con.getResponseCode() == 200));
/*    */     }
/* 38 */     catch (Exception e) {
/* 39 */       return (IReturnValue)new ReturnValue(false);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ActionMarkDelivered.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */