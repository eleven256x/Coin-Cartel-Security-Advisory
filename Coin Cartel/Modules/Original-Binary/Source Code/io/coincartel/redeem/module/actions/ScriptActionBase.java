/*    */ package io.coincartel.redeem.module.actions;
/*    */ import com.google.gson.Gson;
/*    */ import com.google.gson.reflect.TypeToken;
/*    */ import io.coincartel.redeem.module.events.objects.Key;
/*    */ import io.coincartel.redeem.module.events.objects.StepAck;
/*    */ import io.coincartel.redeem.module.events.onRedeem;
/*    */ import io.coincartel.redeem.module.events.onStepAck;
/*    */ import io.socket.client.IO;
/*    */ import io.socket.client.Socket;
/*    */ import io.socket.engineio.client.Transport;
/*    */ import java.lang.reflect.Type;
/*    */ import java.net.URISyntaxException;
/*    */ import java.util.Collections;
/*    */ import java.util.LinkedList;
/*    */ import java.util.Map;
/*    */ import java.util.Scanner;
/*    */ import net.eq2online.macros.scripting.api.APIVersion;
/*    */ import net.eq2online.macros.scripting.api.IMacroEventProvider;
/*    */ import net.eq2online.macros.scripting.api.IScriptAction;
/*    */ import net.eq2online.macros.scripting.parser.ScriptAction;
/*    */ import net.eq2online.macros.scripting.parser.ScriptContext;
/*    */ import net.minecraft.client.Minecraft;
/*    */ 
/*    */ @APIVersion(26)
/*    */ public class ScriptActionBase extends ScriptAction {
/*    */   private final Type keyType;
/*    */   private final Type StepAckType;
/*    */   
/*    */   public ScriptActionBase() {
/* 30 */     super(ScriptContext.MAIN, "null");
/*    */     
/* 32 */     this.keyType = (new TypeToken<Key>() {  }).getType();
/* 33 */     this.StepAckType = (new TypeToken<StepAck>() {  }).getType();
/* 34 */   } static String socketUrl = (new Scanner(Thread.currentThread().getContextClassLoader().getResourceAsStream("serverUrl"), "UTF-8")).next();
/*    */   
/*    */   Gson gson;
/*    */   Socket socket;
/*    */   
/*    */   public void onInit() {
/*    */     try {
/* 41 */       this.gson = new Gson();
/* 42 */       this.socket = Socket();
/* 43 */       ScriptContext.MAIN.getCore().registerEventProvider((IMacroEventProvider)new onRedeem());
/* 44 */       ScriptContext.MAIN.getCore().registerEventProvider((IMacroEventProvider)new onStepAck());
/* 45 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionEditStep(this.gson, this.socket));
/* 46 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionGetCurrentStep());
/* 47 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionGetKey());
/* 48 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionGetLastStep());
/* 49 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionGetStep());
/* 50 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionMarkDelivered(this.gson, this.socket));
/* 51 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionNewStep(this.gson, this.socket));
/* 52 */       ScriptContext.MAIN.getCore().registerScriptAction((IScriptAction)new ActionSetCurrentStep(this.gson, this.socket));
/* 53 */     } catch (URISyntaxException e) {
/* 54 */       e.printStackTrace();
/*    */     } 
/*    */   }
/*    */   
/*    */   public Socket Socket() throws URISyntaxException {
/* 59 */     Socket socket = IO.socket(socketUrl);
/* 60 */     socket.io().on("transport", args -> {
/*    */           Transport transport = (Transport)args[0];
/*    */ 
/*    */           
/*    */           transport.on("requestHeaders", ());
/*    */         });
/*    */ 
/*    */     
/* 68 */     socket.on("onRedeem", args -> {
/*    */           System.out.println("onRedeem");
/*    */           ((Key)(new Gson()).fromJson(args[0].toString(), this.keyType)).callEvent();
/*    */         });
/* 72 */     socket.on("onStepAck", args -> {
/*    */           System.out.println("onStepAck");
/*    */           ((StepAck)(new Gson()).fromJson(args[0].toString(), this.StepAckType)).callEvent();
/*    */         });
/* 76 */     socket.on("disconnect", args -> System.out.println("Disconnected"));
/*    */ 
/*    */ 
/*    */     
/* 80 */     socket.connect();
/* 81 */     return socket;
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\io\coincartel\redeem\module\actions\ScriptActionBase.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */