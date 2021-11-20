/*    */ package com.google.gson.internal.bind;
/*    */ 
/*    */ import com.google.gson.Gson;
/*    */ import com.google.gson.JsonSyntaxException;
/*    */ import com.google.gson.TypeAdapter;
/*    */ import com.google.gson.TypeAdapterFactory;
/*    */ import com.google.gson.internal.bind.util.ISO8601Utils;
/*    */ import com.google.gson.reflect.TypeToken;
/*    */ import com.google.gson.stream.JsonReader;
/*    */ import com.google.gson.stream.JsonToken;
/*    */ import com.google.gson.stream.JsonWriter;
/*    */ import java.io.IOException;
/*    */ import java.text.DateFormat;
/*    */ import java.text.ParseException;
/*    */ import java.text.ParsePosition;
/*    */ import java.util.Date;
/*    */ import java.util.Locale;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class DateTypeAdapter
/*    */   extends TypeAdapter<Date>
/*    */ {
/* 42 */   public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory()
/*    */     {
/*    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
/* 45 */         return (typeToken.getRawType() == Date.class) ? new DateTypeAdapter() : null;
/*    */       }
/*    */     };
/*    */ 
/*    */   
/* 50 */   private final DateFormat enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
/*    */   
/* 52 */   private final DateFormat localFormat = DateFormat.getDateTimeInstance(2, 2);
/*    */   
/*    */   public Date read(JsonReader in) throws IOException {
/* 55 */     if (in.peek() == JsonToken.NULL) {
/* 56 */       in.nextNull();
/* 57 */       return null;
/*    */     } 
/* 59 */     return deserializeToDate(in.nextString());
/*    */   }
/*    */   
/*    */   private synchronized Date deserializeToDate(String json) {
/*    */     try {
/* 64 */       return this.localFormat.parse(json);
/* 65 */     } catch (ParseException parseException) {
/*    */       
/*    */       try {
/* 68 */         return this.enUsFormat.parse(json);
/* 69 */       } catch (ParseException parseException1) {
/*    */         
/*    */         try {
/* 72 */           return ISO8601Utils.parse(json, new ParsePosition(0));
/* 73 */         } catch (ParseException e) {
/* 74 */           throw new JsonSyntaxException(json, e);
/*    */         } 
/*    */       } 
/*    */     } 
/*    */   } public synchronized void write(JsonWriter out, Date value) throws IOException {
/* 79 */     if (value == null) {
/* 80 */       out.nullValue();
/*    */       return;
/*    */     } 
/* 83 */     String dateFormatAsString = this.enUsFormat.format(value);
/* 84 */     out.value(dateFormatAsString);
/*    */   }
/*    */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\com\google\gson\internal\bind\DateTypeAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */