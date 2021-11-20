/*     */ package com.google.gson;
/*     */ 
/*     */ import java.lang.reflect.Type;
/*     */ import java.sql.Date;
/*     */ import java.sql.Timestamp;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.Locale;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ final class DefaultDateTypeAdapter
/*     */   implements JsonSerializer<Date>, JsonDeserializer<Date>
/*     */ {
/*     */   private final DateFormat enUsFormat;
/*     */   private final DateFormat localFormat;
/*     */   
/*     */   DefaultDateTypeAdapter() {
/*  45 */     this(DateFormat.getDateTimeInstance(2, 2, Locale.US), 
/*  46 */         DateFormat.getDateTimeInstance(2, 2));
/*     */   }
/*     */   
/*     */   DefaultDateTypeAdapter(String datePattern) {
/*  50 */     this(new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
/*     */   }
/*     */   
/*     */   DefaultDateTypeAdapter(int style) {
/*  54 */     this(DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
/*     */   }
/*     */   
/*     */   public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
/*  58 */     this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US), 
/*  59 */         DateFormat.getDateTimeInstance(dateStyle, timeStyle));
/*     */   }
/*     */   
/*     */   DefaultDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat) {
/*  63 */     this.enUsFormat = enUsFormat;
/*  64 */     this.localFormat = localFormat;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
/*  71 */     synchronized (this.localFormat) {
/*  72 */       String dateFormatAsString = this.enUsFormat.format(src);
/*  73 */       return new JsonPrimitive(dateFormatAsString);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
/*  80 */     if (!(json instanceof JsonPrimitive)) {
/*  81 */       throw new JsonParseException("The date should be a string value");
/*     */     }
/*  83 */     Date date = deserializeToDate(json);
/*  84 */     if (typeOfT == Date.class)
/*  85 */       return date; 
/*  86 */     if (typeOfT == Timestamp.class)
/*  87 */       return new Timestamp(date.getTime()); 
/*  88 */     if (typeOfT == Date.class) {
/*  89 */       return new Date(date.getTime());
/*     */     }
/*  91 */     throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
/*     */   }
/*     */ 
/*     */   
/*     */   private Date deserializeToDate(JsonElement json) {
/*  96 */     synchronized (this.localFormat) {
/*     */       
/*  98 */       return this.localFormat.parse(json.getAsString());
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 113 */     StringBuilder sb = new StringBuilder();
/* 114 */     sb.append(DefaultDateTypeAdapter.class.getSimpleName());
/* 115 */     sb.append('(').append(this.localFormat.getClass().getSimpleName()).append(')');
/* 116 */     return sb.toString();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\com\google\gson\DefaultDateTypeAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */