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
/*    */ 
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class onStepAck
/*    */   implements IMacroEventProvider, IMacroEventVariableProvider
/*    */ {
/*    */   private IMacroEvent onStepAck;
/*    */   private String key;
/*    */   private String stepIndex;
/*    */   private String stepTitle;
/*    */   
/*    */   public IMacroEventDispatcher getDispatcher() {
/* 27 */     return null;
/*    */   } private String stepMessage; public onStepAck(IMacroEvent e) {}
/*    */   public onStepAck() {}
/*    */   public void onInit() {}
/*    */   public void registerEvents(IMacroEventManager manager) {
/* 32 */     this.onStepAck = manager.registerEvent(this, new IMacroEventDefinition()
/*    */         {
/*    */           public String getName() {
/* 35 */             return "onStepAck";
/*    */           }
/*    */ 
/*    */           
/*    */           public String getPermissionGroup() {
/* 40 */             return null;
/*    */           }
/*    */         });
/* 43 */     this.onStepAck.setVariableProviderClass(getClass());
/*    */   }
/*    */ 
/*    */   
/*    */   public List<String> getHelp(IMacroEvent iMacroEvent) {
/* 48 */     List<String> lines = new LinkedList<>();
/* 49 */     lines.add("onStepAck Variables:");
/* 50 */     lines.add("%USEDKEY%");
/* 51 */     lines.add("%STEPINDEX%");
/* 52 */     lines.add("%STEPTITLE%");
/* 53 */     lines.add("%STEPMESSAGE%");
/* 54 */     return lines;
/*    */   }
/*    */   
/*    */   public void updateVariables(boolean clock) {}
/*    */   
/*    */   public Object getVariable(String variableName) {
/* 60 */     switch (variableName) {
/*    */       case "USEDKEY":
/* 62 */         return (this.key != null) ? this.key : "";
/*    */       case "STEPINDEX":
/* 64 */         return (this.stepIndex != null) ? this.stepIndex : "";
/*    */       case "STEPTITLE":
/* 66 */         return (this.stepTitle != null) ? this.stepTitle : "";
/*    */       case "STEPMESSAGE":
/* 68 */         return (this.stepMessage != null) ? this.stepMessage : "";
/*    */     } 
/* 70 */     return "";
/*    */   }
/*    */ 
/*    */   
/*    */   public Set getVariables() {
/* 75 */     Set<String> variables = new HashSet<>();
/* 76 */     variables.add("%USEDKEY%");
/* 77 */     variables.add("%STEPINDEX%");
/* 78 */     variables.add("%STEPTITLE%");
/* 79 */     variables.add("%STEPMESSAGE%");
/* 80 */     return variables;
/*    */   }
/*    */   
/*    */   public void initInstance(String[] instanceVariables) {
/* 84 */     this.key = instanceVariables[0];
/* 85 */     this.stepIndex = instanceVariables[1];
/* 86 */     this.stepTitle = instanceVariables[2];
/* 87 */     this.stepMessage = instanceVariables[3];
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\events\onStepAck.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */