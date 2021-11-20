/*     */ package org.json;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.StringWriter;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.Array;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
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
/*     */ public class JSONArray
/*     */ {
/*     */   private final ArrayList myArrayList;
/*     */   
/*     */   public JSONArray() {
/*  91 */     this.myArrayList = new ArrayList();
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
/*     */   public JSONArray(JSONTokener x) throws JSONException {
/* 103 */     this();
/* 104 */     if (x.nextClean() != '[') {
/* 105 */       throw x.syntaxError("A JSONArray text must start with '['");
/*     */     }
/* 107 */     if (x.nextClean() != ']') {
/* 108 */       x.back();
/*     */       while (true) {
/* 110 */         if (x.nextClean() == ',') {
/* 111 */           x.back();
/* 112 */           this.myArrayList.add(JSONObject.NULL);
/*     */         } else {
/* 114 */           x.back();
/* 115 */           this.myArrayList.add(x.nextValue());
/*     */         } 
/* 117 */         switch (x.nextClean()) {
/*     */           case ',':
/* 119 */             if (x.nextClean() == ']') {
/*     */               return;
/*     */             }
/* 122 */             x.back(); continue;
/*     */           case ']':
/*     */             return;
/*     */         }  break;
/*     */       } 
/* 127 */       throw x.syntaxError("Expected a ',' or ']'");
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
/*     */ 
/*     */   
/*     */   public JSONArray(String source) throws JSONException {
/* 144 */     this(new JSONTokener(source));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray(Collection collection) {
/* 154 */     this.myArrayList = new ArrayList();
/* 155 */     if (collection != null) {
/* 156 */       Iterator iter = collection.iterator();
/* 157 */       while (iter.hasNext()) {
/* 158 */         this.myArrayList.add(JSONObject.wrap(iter.next()));
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray(Object array) throws JSONException {
/* 170 */     this();
/* 171 */     if (array.getClass().isArray()) {
/* 172 */       int length = Array.getLength(array);
/* 173 */       for (int i = 0; i < length; i++) {
/* 174 */         put(JSONObject.wrap(Array.get(array, i)));
/*     */       }
/*     */     } else {
/* 177 */       throw new JSONException("JSONArray initial value should be a string or collection or array.");
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
/*     */   public Object get(int index) throws JSONException {
/* 192 */     Object object = opt(index);
/* 193 */     if (object == null) {
/* 194 */       throw new JSONException("JSONArray[" + index + "] not found.");
/*     */     }
/* 196 */     return object;
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
/*     */   
/*     */   public boolean getBoolean(int index) throws JSONException {
/* 211 */     Object object = get(index);
/* 212 */     if (object.equals(Boolean.FALSE) || (object instanceof String && ((String)object).equalsIgnoreCase("false")))
/*     */     {
/*     */       
/* 215 */       return false; } 
/* 216 */     if (object.equals(Boolean.TRUE) || (object instanceof String && ((String)object).equalsIgnoreCase("true")))
/*     */     {
/*     */       
/* 219 */       return true;
/*     */     }
/* 221 */     throw new JSONException("JSONArray[" + index + "] is not a boolean.");
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
/*     */   public double getDouble(int index) throws JSONException {
/* 235 */     Object object = get(index);
/*     */     try {
/* 237 */       return (object instanceof Number) ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
/*     */     }
/* 239 */     catch (Exception e) {
/* 240 */       throw new JSONException("JSONArray[" + index + "] is not a number.");
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
/*     */   public int getInt(int index) throws JSONException {
/* 254 */     Object object = get(index);
/*     */     try {
/* 256 */       return (object instanceof Number) ? ((Number)object).intValue() : Integer.parseInt((String)object);
/*     */     }
/* 258 */     catch (Exception e) {
/* 259 */       throw new JSONException("JSONArray[" + index + "] is not a number.");
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
/*     */   public JSONArray getJSONArray(int index) throws JSONException {
/* 274 */     Object object = get(index);
/* 275 */     if (object instanceof JSONArray) {
/* 276 */       return (JSONArray)object;
/*     */     }
/* 278 */     throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
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
/*     */   public JSONObject getJSONObject(int index) throws JSONException {
/* 292 */     Object object = get(index);
/* 293 */     if (object instanceof JSONObject) {
/* 294 */       return (JSONObject)object;
/*     */     }
/* 296 */     throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
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
/*     */   public long getLong(int index) throws JSONException {
/* 310 */     Object object = get(index);
/*     */     try {
/* 312 */       return (object instanceof Number) ? ((Number)object).longValue() : Long.parseLong((String)object);
/*     */     }
/* 314 */     catch (Exception e) {
/* 315 */       throw new JSONException("JSONArray[" + index + "] is not a number.");
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
/*     */   public String getString(int index) throws JSONException {
/* 329 */     Object object = get(index);
/* 330 */     if (object instanceof String) {
/* 331 */       return (String)object;
/*     */     }
/* 333 */     throw new JSONException("JSONArray[" + index + "] not a string.");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isNull(int index) {
/* 344 */     return JSONObject.NULL.equals(opt(index));
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
/*     */   
/*     */   public String join(String separator) throws JSONException {
/* 359 */     int len = length();
/* 360 */     StringBuffer sb = new StringBuffer();
/*     */     
/* 362 */     for (int i = 0; i < len; i++) {
/* 363 */       if (i > 0) {
/* 364 */         sb.append(separator);
/*     */       }
/* 366 */       sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
/*     */     } 
/* 368 */     return sb.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int length() {
/* 377 */     return this.myArrayList.size();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Object opt(int index) {
/* 388 */     return (index < 0 || index >= length()) ? null : this.myArrayList.get(index);
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
/*     */   public boolean optBoolean(int index) {
/* 402 */     return optBoolean(index, false);
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
/*     */   
/*     */   public boolean optBoolean(int index, boolean defaultValue) {
/*     */     try {
/* 418 */       return getBoolean(index);
/* 419 */     } catch (Exception e) {
/* 420 */       return defaultValue;
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
/*     */   public double optDouble(int index) {
/* 434 */     return optDouble(index, Double.NaN);
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
/*     */   
/*     */   public double optDouble(int index, double defaultValue) {
/*     */     try {
/* 450 */       return getDouble(index);
/* 451 */     } catch (Exception e) {
/* 452 */       return defaultValue;
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
/*     */   public int optInt(int index) {
/* 466 */     return optInt(index, 0);
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
/*     */   
/*     */   public int optInt(int index, int defaultValue) {
/*     */     try {
/* 482 */       return getInt(index);
/* 483 */     } catch (Exception e) {
/* 484 */       return defaultValue;
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
/*     */   public JSONArray optJSONArray(int index) {
/* 497 */     Object o = opt(index);
/* 498 */     return (o instanceof JSONArray) ? (JSONArray)o : null;
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
/*     */   public JSONObject optJSONObject(int index) {
/* 511 */     Object o = opt(index);
/* 512 */     return (o instanceof JSONObject) ? (JSONObject)o : null;
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
/*     */   public long optLong(int index) {
/* 525 */     return optLong(index, 0L);
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
/*     */   
/*     */   public long optLong(int index, long defaultValue) {
/*     */     try {
/* 541 */       return getLong(index);
/* 542 */     } catch (Exception e) {
/* 543 */       return defaultValue;
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
/*     */   public String optString(int index) {
/* 557 */     return optString(index, "");
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
/*     */   public String optString(int index, String defaultValue) {
/* 571 */     Object object = opt(index);
/* 572 */     return JSONObject.NULL.equals(object) ? defaultValue : object.toString();
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
/*     */   public JSONArray put(boolean value) {
/* 584 */     put(value ? Boolean.TRUE : Boolean.FALSE);
/* 585 */     return this;
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
/*     */   public JSONArray put(Collection value) {
/* 597 */     put(new JSONArray(value));
/* 598 */     return this;
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
/*     */   public JSONArray put(double value) throws JSONException {
/* 611 */     Double d = new Double(value);
/* 612 */     JSONObject.testValidity(d);
/* 613 */     put(d);
/* 614 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int value) {
/* 625 */     put(new Integer(value));
/* 626 */     return this;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(long value) {
/* 637 */     put(new Long(value));
/* 638 */     return this;
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
/*     */   public JSONArray put(Map value) {
/* 650 */     put(new JSONObject(value));
/* 651 */     return this;
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
/*     */   public JSONArray put(Object value) {
/* 664 */     this.myArrayList.add(value);
/* 665 */     return this;
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
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, boolean value) throws JSONException {
/* 682 */     put(index, value ? Boolean.TRUE : Boolean.FALSE);
/* 683 */     return this;
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
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, Collection value) throws JSONException {
/* 699 */     put(index, new JSONArray(value));
/* 700 */     return this;
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
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, double value) throws JSONException {
/* 717 */     put(index, new Double(value));
/* 718 */     return this;
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
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, int value) throws JSONException {
/* 735 */     put(index, new Integer(value));
/* 736 */     return this;
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
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, long value) throws JSONException {
/* 753 */     put(index, new Long(value));
/* 754 */     return this;
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
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, Map value) throws JSONException {
/* 771 */     put(index, new JSONObject(value));
/* 772 */     return this;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public JSONArray put(int index, Object value) throws JSONException {
/* 792 */     JSONObject.testValidity(value);
/* 793 */     if (index < 0) {
/* 794 */       throw new JSONException("JSONArray[" + index + "] not found.");
/*     */     }
/* 796 */     if (index < length()) {
/* 797 */       this.myArrayList.set(index, value);
/*     */     } else {
/* 799 */       while (index != length()) {
/* 800 */         put(JSONObject.NULL);
/*     */       }
/* 802 */       put(value);
/*     */     } 
/* 804 */     return this;
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
/*     */   public Object remove(int index) {
/* 816 */     Object o = opt(index);
/* 817 */     this.myArrayList.remove(index);
/* 818 */     return o;
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
/*     */ 
/*     */   
/*     */   public JSONObject toJSONObject(JSONArray names) throws JSONException {
/* 834 */     if (names == null || names.length() == 0 || length() == 0) {
/* 835 */       return null;
/*     */     }
/* 837 */     JSONObject jo = new JSONObject();
/* 838 */     for (int i = 0; i < names.length(); i++) {
/* 839 */       jo.put(names.getString(i), opt(i));
/*     */     }
/* 841 */     return jo;
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
/*     */   
/*     */   public String toString() {
/*     */     try {
/* 857 */       return toString(0);
/* 858 */     } catch (Exception e) {
/* 859 */       return null;
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
/*     */ 
/*     */   
/*     */   public String toString(int indentFactor) throws JSONException {
/* 876 */     StringWriter sw = new StringWriter();
/* 877 */     synchronized (sw.getBuffer()) {
/* 878 */       return write(sw, indentFactor, 0).toString();
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
/*     */   public Writer write(Writer writer) throws JSONException {
/* 892 */     return write(writer, 0, 0);
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
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
/*     */     try {
/* 911 */       boolean commanate = false;
/* 912 */       int length = length();
/* 913 */       writer.write(91);
/*     */       
/* 915 */       if (length == 1) {
/* 916 */         JSONObject.writeValue(writer, this.myArrayList.get(0), indentFactor, indent);
/*     */       }
/* 918 */       else if (length != 0) {
/* 919 */         int newindent = indent + indentFactor;
/*     */         
/* 921 */         for (int i = 0; i < length; i++) {
/* 922 */           if (commanate) {
/* 923 */             writer.write(44);
/*     */           }
/* 925 */           if (indentFactor > 0) {
/* 926 */             writer.write(10);
/*     */           }
/* 928 */           JSONObject.indent(writer, newindent);
/* 929 */           JSONObject.writeValue(writer, this.myArrayList.get(i), indentFactor, newindent);
/*     */           
/* 931 */           commanate = true;
/*     */         } 
/* 933 */         if (indentFactor > 0) {
/* 934 */           writer.write(10);
/*     */         }
/* 936 */         JSONObject.indent(writer, indent);
/*     */       } 
/* 938 */       writer.write(93);
/* 939 */       return writer;
/* 940 */     } catch (IOException e) {
/* 941 */       throw new JSONException(e);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\JSONArray.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */