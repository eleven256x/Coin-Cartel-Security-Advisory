/*     */ package okhttp3;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import javax.annotation.Nullable;
/*     */ import okhttp3.internal.Util;
/*     */ import okio.Buffer;
/*     */ import okio.BufferedSink;
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
/*     */ public final class FormBody
/*     */   extends RequestBody
/*     */ {
/*  31 */   private static final MediaType CONTENT_TYPE = MediaType.get("application/x-www-form-urlencoded");
/*     */   
/*     */   private final List<String> encodedNames;
/*     */   private final List<String> encodedValues;
/*     */   
/*     */   FormBody(List<String> encodedNames, List<String> encodedValues) {
/*  37 */     this.encodedNames = Util.immutableList(encodedNames);
/*  38 */     this.encodedValues = Util.immutableList(encodedValues);
/*     */   }
/*     */ 
/*     */   
/*     */   public int size() {
/*  43 */     return this.encodedNames.size();
/*     */   }
/*     */   
/*     */   public String encodedName(int index) {
/*  47 */     return this.encodedNames.get(index);
/*     */   }
/*     */   
/*     */   public String name(int index) {
/*  51 */     return HttpUrl.percentDecode(encodedName(index), true);
/*     */   }
/*     */   
/*     */   public String encodedValue(int index) {
/*  55 */     return this.encodedValues.get(index);
/*     */   }
/*     */   
/*     */   public String value(int index) {
/*  59 */     return HttpUrl.percentDecode(encodedValue(index), true);
/*     */   }
/*     */   
/*     */   public MediaType contentType() {
/*  63 */     return CONTENT_TYPE;
/*     */   }
/*     */   
/*     */   public long contentLength() {
/*  67 */     return writeOrCountBytes(null, true);
/*     */   }
/*     */   
/*     */   public void writeTo(BufferedSink sink) throws IOException {
/*  71 */     writeOrCountBytes(sink, false);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private long writeOrCountBytes(@Nullable BufferedSink sink, boolean countBytes) {
/*     */     Buffer buffer;
/*  81 */     long byteCount = 0L;
/*     */ 
/*     */     
/*  84 */     if (countBytes) {
/*  85 */       buffer = new Buffer();
/*     */     } else {
/*  87 */       buffer = sink.buffer();
/*     */     } 
/*     */     
/*  90 */     for (int i = 0, size = this.encodedNames.size(); i < size; i++) {
/*  91 */       if (i > 0) buffer.writeByte(38); 
/*  92 */       buffer.writeUtf8(this.encodedNames.get(i));
/*  93 */       buffer.writeByte(61);
/*  94 */       buffer.writeUtf8(this.encodedValues.get(i));
/*     */     } 
/*     */     
/*  97 */     if (countBytes) {
/*  98 */       byteCount = buffer.size();
/*  99 */       buffer.clear();
/*     */     } 
/*     */     
/* 102 */     return byteCount;
/*     */   }
/*     */   
/*     */   public static final class Builder {
/* 106 */     private final List<String> names = new ArrayList<>();
/* 107 */     private final List<String> values = new ArrayList<>();
/*     */     private final Charset charset;
/*     */     
/*     */     public Builder() {
/* 111 */       this(null);
/*     */     }
/*     */     
/*     */     public Builder(Charset charset) {
/* 115 */       this.charset = charset;
/*     */     }
/*     */     
/*     */     public Builder add(String name, String value) {
/* 119 */       if (name == null) throw new NullPointerException("name == null"); 
/* 120 */       if (value == null) throw new NullPointerException("value == null");
/*     */       
/* 122 */       this.names.add(HttpUrl.canonicalize(name, " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, true, this.charset));
/* 123 */       this.values.add(HttpUrl.canonicalize(value, " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, false, true, true, this.charset));
/* 124 */       return this;
/*     */     }
/*     */     
/*     */     public Builder addEncoded(String name, String value) {
/* 128 */       if (name == null) throw new NullPointerException("name == null"); 
/* 129 */       if (value == null) throw new NullPointerException("value == null");
/*     */       
/* 131 */       this.names.add(HttpUrl.canonicalize(name, " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, false, true, true, this.charset));
/* 132 */       this.values.add(HttpUrl.canonicalize(value, " \"':;<=>@[]^`{}|/\\?#&!$(),~", true, false, true, true, this.charset));
/* 133 */       return this;
/*     */     }
/*     */     
/*     */     public FormBody build() {
/* 137 */       return new FormBody(this.names, this.values);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\FormBody.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */