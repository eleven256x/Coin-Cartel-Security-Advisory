/*     */ package okhttp3;
/*     */ 
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.Locale;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import javax.annotation.Nullable;
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
/*     */ public final class MediaType
/*     */ {
/*     */   private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
/*     */   private static final String QUOTED = "\"([^\"]*)\"";
/*  31 */   private static final Pattern TYPE_SUBTYPE = Pattern.compile("([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)/([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)");
/*  32 */   private static final Pattern PARAMETER = Pattern.compile(";\\s*(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)=(?:([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)|\"([^\"]*)\"))?");
/*     */   
/*     */   private final String mediaType;
/*     */   private final String type;
/*     */   private final String subtype;
/*     */   @Nullable
/*     */   private final String charset;
/*     */   
/*     */   private MediaType(String mediaType, String type, String subtype, @Nullable String charset) {
/*  41 */     this.mediaType = mediaType;
/*  42 */     this.type = type;
/*  43 */     this.subtype = subtype;
/*  44 */     this.charset = charset;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static MediaType get(String string) {
/*  53 */     Matcher typeSubtype = TYPE_SUBTYPE.matcher(string);
/*  54 */     if (!typeSubtype.lookingAt()) {
/*  55 */       throw new IllegalArgumentException("No subtype found for: \"" + string + '"');
/*     */     }
/*  57 */     String type = typeSubtype.group(1).toLowerCase(Locale.US);
/*  58 */     String subtype = typeSubtype.group(2).toLowerCase(Locale.US);
/*     */     
/*  60 */     String charset = null;
/*  61 */     Matcher parameter = PARAMETER.matcher(string); int s;
/*  62 */     for (s = typeSubtype.end(); s < string.length(); s = parameter.end()) {
/*  63 */       parameter.region(s, string.length());
/*  64 */       if (!parameter.lookingAt()) {
/*  65 */         throw new IllegalArgumentException("Parameter is not formatted correctly: \"" + string
/*  66 */             .substring(s) + "\" for: \"" + string + '"');
/*     */       }
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  72 */       String name = parameter.group(1);
/*  73 */       if (name != null && name.equalsIgnoreCase("charset")) {
/*     */         
/*  75 */         String charsetParameter, token = parameter.group(2);
/*  76 */         if (token != null) {
/*     */ 
/*     */ 
/*     */           
/*  80 */           charsetParameter = (token.startsWith("'") && token.endsWith("'") && token.length() > 2) ? token.substring(1, token.length() - 1) : token;
/*     */         } else {
/*     */           
/*  83 */           charsetParameter = parameter.group(3);
/*     */         } 
/*  85 */         if (charset != null && !charsetParameter.equalsIgnoreCase(charset)) {
/*  86 */           throw new IllegalArgumentException("Multiple charsets defined: \"" + charset + "\" and: \"" + charsetParameter + "\" for: \"" + string + '"');
/*     */         }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  94 */         charset = charsetParameter;
/*     */       } 
/*     */     } 
/*  97 */     return new MediaType(string, type, subtype, charset);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public static MediaType parse(String string) {
/*     */     try {
/* 106 */       return get(string);
/* 107 */     } catch (IllegalArgumentException ignored) {
/* 108 */       return null;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String type() {
/* 117 */     return this.type;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String subtype() {
/* 124 */     return this.subtype;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Charset charset() {
/* 131 */     return charset(null);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @Nullable
/*     */   public Charset charset(@Nullable Charset defaultValue) {
/*     */     try {
/* 140 */       return (this.charset != null) ? Charset.forName(this.charset) : defaultValue;
/* 141 */     } catch (IllegalArgumentException e) {
/* 142 */       return defaultValue;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String toString() {
/* 151 */     return this.mediaType;
/*     */   }
/*     */   
/*     */   public boolean equals(@Nullable Object other) {
/* 155 */     return (other instanceof MediaType && ((MediaType)other).mediaType.equals(this.mediaType));
/*     */   }
/*     */   
/*     */   public int hashCode() {
/* 159 */     return this.mediaType.hashCode();
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\okhttp3\MediaType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */