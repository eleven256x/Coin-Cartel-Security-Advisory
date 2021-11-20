/*    */ package io.coincartel.redeem.module.events;
/*    */ 
/*    */ import java.util.HashSet;
/*    */ import java.util.LinkedList;
/*    */ import java.util.List;
/*    */ import java.util.Set;
/*    */ import net.eq2online.macros.scripting.api.APIVersion;
/*    */ import net.eq2online.macros.scripting.api.IMacroEvent;
/*    */ import net.eq2online.macros.scripting.api.IMacroEventDefinition;
/*    */ import net.eq2online.macros.scripting.api.IMacroEventDispatcher;
/*    */ import net.eq2online.macros.scripting.api.IMacroEventManager;
/*    */ import net.eq2online.macros.scripting.api.IMacroEventProvider;
/*    */ import net.eq2online.macros.scripting.api.IMacroEventVariableProvider;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class onRedeem
/*    */   implements IMacroEventProvider, IMacroEventVariableProvider
/*    */ {
/*    */   private IMacroEvent onRedeem;
/*    */   private String key;
/*    */   private String quantityTotal;
/*    */   private String quantityRemaining;
/*    */   
/*    */   public IMacroEventDispatcher getDispatcher() {
/* 25 */     return null;
/*    */   } private String MinecraftUUID; private String discordUsername; public onRedeem(IMacroEvent e) {}
/*    */   public onRedeem() {}
/*    */   public void onInit() {}
/*    */   public void registerEvents(IMacroEventManager manager) {
/* 30 */     this.onRedeem = manager.registerEvent(this, new IMacroEventDefinition()
/*    */         {
/*    */           public String getName() {
/* 33 */             return "onRedeem";
/*    */           }
/*    */ 
/*    */           
/*    */           public String getPermissionGroup() {
/* 38 */             return null;
/*    */           }
/*    */         });
/* 41 */     this.onRedeem.setVariableProviderClass(getClass());
/*    */   }
/*    */ 
/*    */   
/*    */   public List<String> getHelp(IMacroEvent iMacroEvent) {
/* 46 */     List<String> lines = new LinkedList<>();
/* 47 */     lines.add("onRedeem Variables:");
/* 48 */     lines.add("%USEDKEY% -> Used key");
/* 49 */     lines.add("%QUANTITYTOTAL% -> 100");
/* 50 */     lines.add("%QUANTITYREMAINING% -> 90");
/* 51 */     lines.add("%USERUUID% -> Minecraft UUID");
/* 52 */     lines.add("%DISCORD% -> Discord#0000");
/* 53 */     return lines;
/*    */   }
/*    */   
/*    */   public void updateVariables(boolean clock) {}
/*    */   
/*    */   public Object getVariable(String variableName) {
/* 59 */     switch (variableName) {
/*    */       case "USEDKEY":
/* 61 */         return (this.key != null) ? this.key : "";
/*    */       case "QUANTITYTOTAL":
/* 63 */         return (this.quantityTotal != null) ? this.quantityTotal : "";
/*    */       case "QUANTITYREMAINING":
/* 65 */         return (this.quantityRemaining != null) ? this.quantityRemaining : "";
/*    */       case "USERUUID":
/* 67 */         return (this.MinecraftUUID != null) ? this.MinecraftUUID : "";
/*    */       case "DISCORD":
/* 69 */         return (this.discordUsername != null) ? this.discordUsername : "";
/*    */     } 
/* 71 */     return "";
/*    */   }
/*    */ 
/*    */   
/*    */   public Set getVariables() {
/* 76 */     Set<String> variables = new HashSet<>();
/* 77 */     variables.add("%USEDKEY%");
/* 78 */     variables.add("%QUANTITYTOTAL%");
/* 79 */     variables.add("%QUANTITYREMAINING%");
/* 80 */     variables.add("%USERUUID%");
/* 81 */     variables.add("%DISCORD%");
/* 82 */     return variables;
/*    */   }
/*    */   
/*    */   public void initInstance(String[] instanceVariables) {
/* 86 */     this.key = instanceVariables[0];
/* 87 */     this.quantityTotal = instanceVariables[1];
/* 88 */     this.quantityRemaining = instanceVariables[2];
/* 89 */     this.MinecraftUUID = instanceVariables[3];
/* 90 */     this.discordUsername = instanceVariables[4];
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\events\onRedeem.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */