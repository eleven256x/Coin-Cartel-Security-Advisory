/*      */ package org.json;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.io.StringWriter;
/*      */ import java.io.Writer;
/*      */ import java.lang.reflect.Field;
/*      */ import java.lang.reflect.Method;
/*      */ import java.lang.reflect.Modifier;
/*      */ import java.util.Collection;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.ResourceBundle;
/*      */ import java.util.Set;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class JSONObject
/*      */ {
/*      */   private final Map map;
/*      */   
/*      */   private static final class Null
/*      */   {
/*      */     private Null() {}
/*      */     
/*      */     protected final Object clone() {
/*  110 */       return this;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public boolean equals(Object object) {
/*  122 */       return (object == null || object == this);
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*      */     public String toString() {
/*  131 */       return "null";
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*  146 */   public static final Object NULL = new Null();
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject() {
/*  152 */     this.map = new HashMap();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(JSONObject jo, String[] names) {
/*  170 */     this();
/*  171 */     for (int i = 0; i < names.length; i++) {
/*      */       try {
/*  173 */         putOnce(names[i], jo.opt(names[i]));
/*  174 */       } catch (Exception ignore) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(JSONTokener x) throws JSONException {
/*  189 */     this();
/*      */ 
/*      */ 
/*      */     
/*  193 */     if (x.nextClean() != '{') {
/*  194 */       throw x.syntaxError("A JSONObject text must begin with '{'");
/*      */     }
/*      */     while (true) {
/*  197 */       char c = x.nextClean();
/*  198 */       switch (c) {
/*      */         case '\000':
/*  200 */           throw x.syntaxError("A JSONObject text must end with '}'");
/*      */         case '}':
/*      */           return;
/*      */       } 
/*  204 */       x.back();
/*  205 */       String key = x.nextValue().toString();
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  210 */       c = x.nextClean();
/*  211 */       if (c != ':') {
/*  212 */         throw x.syntaxError("Expected a ':' after a key");
/*      */       }
/*  214 */       putOnce(key, x.nextValue());
/*      */ 
/*      */ 
/*      */       
/*  218 */       switch (x.nextClean()) {
/*      */         case ',':
/*      */         case ';':
/*  221 */           if (x.nextClean() == '}') {
/*      */             return;
/*      */           }
/*  224 */           x.back(); continue;
/*      */         case '}':
/*      */           return;
/*      */       }  break;
/*      */     } 
/*  229 */     throw x.syntaxError("Expected a ',' or '}'");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(Map map) {
/*  243 */     this.map = new HashMap();
/*  244 */     if (map != null) {
/*  245 */       Iterator i = map.entrySet().iterator();
/*  246 */       while (i.hasNext()) {
/*  247 */         Map.Entry e = i.next();
/*  248 */         Object value = e.getValue();
/*  249 */         if (value != null) {
/*  250 */           this.map.put(e.getKey(), wrap(value));
/*      */         }
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(Object bean) {
/*  278 */     this();
/*  279 */     populateMap(bean);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(Object object, String[] names) {
/*  297 */     this();
/*  298 */     Class c = object.getClass();
/*  299 */     for (int i = 0; i < names.length; i++) {
/*  300 */       String name = names[i];
/*      */       try {
/*  302 */         putOpt(name, c.getField(name).get(object));
/*  303 */       } catch (Exception ignore) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(String source) throws JSONException {
/*  321 */     this(new JSONTokener(source));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject(String baseName, Locale locale) throws JSONException {
/*  335 */     this();
/*  336 */     ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  341 */     Enumeration keys = bundle.getKeys();
/*  342 */     while (keys.hasMoreElements()) {
/*  343 */       Object key = keys.nextElement();
/*  344 */       if (key instanceof String) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  350 */         String[] path = ((String)key).split("\\.");
/*  351 */         int last = path.length - 1;
/*  352 */         JSONObject target = this;
/*  353 */         for (int i = 0; i < last; i++) {
/*  354 */           String segment = path[i];
/*  355 */           JSONObject nextTarget = target.optJSONObject(segment);
/*  356 */           if (nextTarget == null) {
/*  357 */             nextTarget = new JSONObject();
/*  358 */             target.put(segment, nextTarget);
/*      */           } 
/*  360 */           target = nextTarget;
/*      */         } 
/*  362 */         target.put(path[last], bundle.getString((String)key));
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject accumulate(String key, Object value) throws JSONException {
/*  387 */     testValidity(value);
/*  388 */     Object object = opt(key);
/*  389 */     if (object == null) {
/*  390 */       put(key, (value instanceof JSONArray) ? (new JSONArray()).put(value) : value);
/*      */     
/*      */     }
/*  393 */     else if (object instanceof JSONArray) {
/*  394 */       ((JSONArray)object).put(value);
/*      */     } else {
/*  396 */       put(key, (new JSONArray()).put(object).put(value));
/*      */     } 
/*  398 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject append(String key, Object value) throws JSONException {
/*  417 */     testValidity(value);
/*  418 */     Object object = opt(key);
/*  419 */     if (object == null) {
/*  420 */       put(key, (new JSONArray()).put(value));
/*  421 */     } else if (object instanceof JSONArray) {
/*  422 */       put(key, ((JSONArray)object).put(value));
/*      */     } else {
/*  424 */       throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
/*      */     } 
/*      */     
/*  427 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String doubleToString(double d) {
/*  439 */     if (Double.isInfinite(d) || Double.isNaN(d)) {
/*  440 */       return "null";
/*      */     }
/*      */ 
/*      */ 
/*      */     
/*  445 */     String string = Double.toString(d);
/*  446 */     if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
/*      */       
/*  448 */       while (string.endsWith("0")) {
/*  449 */         string = string.substring(0, string.length() - 1);
/*      */       }
/*  451 */       if (string.endsWith(".")) {
/*  452 */         string = string.substring(0, string.length() - 1);
/*      */       }
/*      */     } 
/*  455 */     return string;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Object get(String key) throws JSONException {
/*  468 */     if (key == null) {
/*  469 */       throw new JSONException("Null key.");
/*      */     }
/*  471 */     Object object = opt(key);
/*  472 */     if (object == null) {
/*  473 */       throw new JSONException("JSONObject[" + quote(key) + "] not found.");
/*      */     }
/*  475 */     return object;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean getBoolean(String key) throws JSONException {
/*  489 */     Object object = get(key);
/*  490 */     if (object.equals(Boolean.FALSE) || (object instanceof String && ((String)object).equalsIgnoreCase("false")))
/*      */     {
/*      */       
/*  493 */       return false; } 
/*  494 */     if (object.equals(Boolean.TRUE) || (object instanceof String && ((String)object).equalsIgnoreCase("true")))
/*      */     {
/*      */       
/*  497 */       return true;
/*      */     }
/*  499 */     throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public double getDouble(String key) throws JSONException {
/*  514 */     Object object = get(key);
/*      */     try {
/*  516 */       return (object instanceof Number) ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
/*      */     }
/*  518 */     catch (Exception e) {
/*  519 */       throw new JSONException("JSONObject[" + quote(key) + "] is not a number.");
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int getInt(String key) throws JSONException {
/*  535 */     Object object = get(key);
/*      */     try {
/*  537 */       return (object instanceof Number) ? ((Number)object).intValue() : Integer.parseInt((String)object);
/*      */     }
/*  539 */     catch (Exception e) {
/*  540 */       throw new JSONException("JSONObject[" + quote(key) + "] is not an int.");
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONArray getJSONArray(String key) throws JSONException {
/*  555 */     Object object = get(key);
/*  556 */     if (object instanceof JSONArray) {
/*  557 */       return (JSONArray)object;
/*      */     }
/*  559 */     throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject getJSONObject(String key) throws JSONException {
/*  573 */     Object object = get(key);
/*  574 */     if (object instanceof JSONObject) {
/*  575 */       return (JSONObject)object;
/*      */     }
/*  577 */     throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public long getLong(String key) throws JSONException {
/*  592 */     Object object = get(key);
/*      */     try {
/*  594 */       return (object instanceof Number) ? ((Number)object).longValue() : Long.parseLong((String)object);
/*      */     }
/*  596 */     catch (Exception e) {
/*  597 */       throw new JSONException("JSONObject[" + quote(key) + "] is not a long.");
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String[] getNames(JSONObject jo) {
/*  608 */     int length = jo.length();
/*  609 */     if (length == 0) {
/*  610 */       return null;
/*      */     }
/*  612 */     Iterator iterator = jo.keys();
/*  613 */     String[] names = new String[length];
/*  614 */     int i = 0;
/*  615 */     while (iterator.hasNext()) {
/*  616 */       names[i] = iterator.next();
/*  617 */       i++;
/*      */     } 
/*  619 */     return names;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String[] getNames(Object object) {
/*  628 */     if (object == null) {
/*  629 */       return null;
/*      */     }
/*  631 */     Class klass = object.getClass();
/*  632 */     Field[] fields = klass.getFields();
/*  633 */     int length = fields.length;
/*  634 */     if (length == 0) {
/*  635 */       return null;
/*      */     }
/*  637 */     String[] names = new String[length];
/*  638 */     for (int i = 0; i < length; i++) {
/*  639 */       names[i] = fields[i].getName();
/*      */     }
/*  641 */     return names;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String getString(String key) throws JSONException {
/*  654 */     Object object = get(key);
/*  655 */     if (object instanceof String) {
/*  656 */       return (String)object;
/*      */     }
/*  658 */     throw new JSONException("JSONObject[" + quote(key) + "] not a string.");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean has(String key) {
/*  669 */     return this.map.containsKey(key);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject increment(String key) throws JSONException {
/*  685 */     Object value = opt(key);
/*  686 */     if (value == null) {
/*  687 */       put(key, 1);
/*  688 */     } else if (value instanceof Integer) {
/*  689 */       put(key, ((Integer)value).intValue() + 1);
/*  690 */     } else if (value instanceof Long) {
/*  691 */       put(key, ((Long)value).longValue() + 1L);
/*  692 */     } else if (value instanceof Double) {
/*  693 */       put(key, ((Double)value).doubleValue() + 1.0D);
/*  694 */     } else if (value instanceof Float) {
/*  695 */       put(key, (((Float)value).floatValue() + 1.0F));
/*      */     } else {
/*  697 */       throw new JSONException("Unable to increment [" + quote(key) + "].");
/*      */     } 
/*  699 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isNull(String key) {
/*  712 */     return NULL.equals(opt(key));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Iterator keys() {
/*  721 */     return keySet().iterator();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Set keySet() {
/*  730 */     return this.map.keySet();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int length() {
/*  739 */     return this.map.size();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONArray names() {
/*  750 */     JSONArray ja = new JSONArray();
/*  751 */     Iterator keys = keys();
/*  752 */     while (keys.hasNext()) {
/*  753 */       ja.put(keys.next());
/*      */     }
/*  755 */     return (ja.length() == 0) ? null : ja;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String numberToString(Number number) throws JSONException {
/*  768 */     if (number == null) {
/*  769 */       throw new JSONException("Null pointer");
/*      */     }
/*  771 */     testValidity(number);
/*      */ 
/*      */ 
/*      */     
/*  775 */     String string = number.toString();
/*  776 */     if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string.indexOf('E') < 0) {
/*      */       
/*  778 */       while (string.endsWith("0")) {
/*  779 */         string = string.substring(0, string.length() - 1);
/*      */       }
/*  781 */       if (string.endsWith(".")) {
/*  782 */         string = string.substring(0, string.length() - 1);
/*      */       }
/*      */     } 
/*  785 */     return string;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Object opt(String key) {
/*  796 */     return (key == null) ? null : this.map.get(key);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean optBoolean(String key) {
/*  808 */     return optBoolean(key, false);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean optBoolean(String key, boolean defaultValue) {
/*      */     try {
/*  824 */       return getBoolean(key);
/*  825 */     } catch (Exception e) {
/*  826 */       return defaultValue;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public double optDouble(String key) {
/*  840 */     return optDouble(key, Double.NaN);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public double optDouble(String key, double defaultValue) {
/*      */     try {
/*  856 */       return getDouble(key);
/*  857 */     } catch (Exception e) {
/*  858 */       return defaultValue;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int optInt(String key) {
/*  872 */     return optInt(key, 0);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int optInt(String key, int defaultValue) {
/*      */     try {
/*  888 */       return getInt(key);
/*  889 */     } catch (Exception e) {
/*  890 */       return defaultValue;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONArray optJSONArray(String key) {
/*  903 */     Object o = opt(key);
/*  904 */     return (o instanceof JSONArray) ? (JSONArray)o : null;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject optJSONObject(String key) {
/*  916 */     Object object = opt(key);
/*  917 */     return (object instanceof JSONObject) ? (JSONObject)object : null;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public long optLong(String key) {
/*  930 */     return optLong(key, 0L);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public long optLong(String key, long defaultValue) {
/*      */     try {
/*  946 */       return getLong(key);
/*  947 */     } catch (Exception e) {
/*  948 */       return defaultValue;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String optString(String key) {
/*  962 */     return optString(key, "");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String optString(String key, String defaultValue) {
/*  976 */     Object object = opt(key);
/*  977 */     return NULL.equals(object) ? defaultValue : object.toString();
/*      */   }
/*      */   
/*      */   private void populateMap(Object bean) {
/*  981 */     Class klass = bean.getClass();
/*      */ 
/*      */ 
/*      */     
/*  985 */     boolean includeSuperClass = (klass.getClassLoader() != null);
/*      */     
/*  987 */     Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
/*      */     
/*  989 */     for (int i = 0; i < methods.length; i++) {
/*      */       try {
/*  991 */         Method method = methods[i];
/*  992 */         if (Modifier.isPublic(method.getModifiers())) {
/*  993 */           String name = method.getName();
/*  994 */           String key = "";
/*  995 */           if (name.startsWith("get")) {
/*  996 */             if ("getClass".equals(name) || "getDeclaringClass".equals(name)) {
/*      */               
/*  998 */               key = "";
/*      */             } else {
/* 1000 */               key = name.substring(3);
/*      */             } 
/* 1002 */           } else if (name.startsWith("is")) {
/* 1003 */             key = name.substring(2);
/*      */           } 
/* 1005 */           if (key.length() > 0 && Character.isUpperCase(key.charAt(0)) && (method.getParameterTypes()).length == 0) {
/*      */ 
/*      */             
/* 1008 */             if (key.length() == 1) {
/* 1009 */               key = key.toLowerCase();
/* 1010 */             } else if (!Character.isUpperCase(key.charAt(1))) {
/* 1011 */               key = key.substring(0, 1).toLowerCase() + key.substring(1);
/*      */             } 
/*      */ 
/*      */             
/* 1015 */             Object result = method.invoke(bean, (Object[])null);
/* 1016 */             if (result != null) {
/* 1017 */               this.map.put(key, wrap(result));
/*      */             }
/*      */           } 
/*      */         } 
/* 1021 */       } catch (Exception ignore) {}
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, boolean value) throws JSONException {
/* 1038 */     put(key, value ? Boolean.TRUE : Boolean.FALSE);
/* 1039 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, Collection value) throws JSONException {
/* 1054 */     put(key, new JSONArray(value));
/* 1055 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, double value) throws JSONException {
/* 1070 */     put(key, new Double(value));
/* 1071 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, int value) throws JSONException {
/* 1086 */     put(key, new Integer(value));
/* 1087 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, long value) throws JSONException {
/* 1102 */     put(key, new Long(value));
/* 1103 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, Map value) throws JSONException {
/* 1118 */     put(key, new JSONObject(value));
/* 1119 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject put(String key, Object value) throws JSONException {
/* 1137 */     if (key == null) {
/* 1138 */       throw new NullPointerException("Null key.");
/*      */     }
/* 1140 */     if (value != null) {
/* 1141 */       testValidity(value);
/* 1142 */       this.map.put(key, value);
/*      */     } else {
/* 1144 */       remove(key);
/*      */     } 
/* 1146 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject putOnce(String key, Object value) throws JSONException {
/* 1161 */     if (key != null && value != null) {
/* 1162 */       if (opt(key) != null) {
/* 1163 */         throw new JSONException("Duplicate key \"" + key + "\"");
/*      */       }
/* 1165 */       put(key, value);
/*      */     } 
/* 1167 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONObject putOpt(String key, Object value) throws JSONException {
/* 1185 */     if (key != null && value != null) {
/* 1186 */       put(key, value);
/*      */     }
/* 1188 */     return this;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String quote(String string) {
/* 1202 */     StringWriter sw = new StringWriter();
/* 1203 */     synchronized (sw.getBuffer()) {
/*      */       
/* 1205 */       return quote(string, sw).toString();
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static Writer quote(String string, Writer w) throws IOException {
/* 1214 */     if (string == null || string.length() == 0) {
/* 1215 */       w.write("\"\"");
/* 1216 */       return w;
/*      */     } 
/*      */ 
/*      */     
/* 1220 */     char c = Character.MIN_VALUE;
/*      */ 
/*      */     
/* 1223 */     int len = string.length();
/*      */     
/* 1225 */     w.write(34);
/* 1226 */     for (int i = 0; i < len; i++) {
/* 1227 */       char b = c;
/* 1228 */       c = string.charAt(i);
/* 1229 */       switch (c) {
/*      */         case '"':
/*      */         case '\\':
/* 1232 */           w.write(92);
/* 1233 */           w.write(c);
/*      */           break;
/*      */         case '/':
/* 1236 */           if (b == '<') {
/* 1237 */             w.write(92);
/*      */           }
/* 1239 */           w.write(c);
/*      */           break;
/*      */         case '\b':
/* 1242 */           w.write("\\b");
/*      */           break;
/*      */         case '\t':
/* 1245 */           w.write("\\t");
/*      */           break;
/*      */         case '\n':
/* 1248 */           w.write("\\n");
/*      */           break;
/*      */         case '\f':
/* 1251 */           w.write("\\f");
/*      */           break;
/*      */         case '\r':
/* 1254 */           w.write("\\r");
/*      */           break;
/*      */         default:
/* 1257 */           if (c < ' ' || (c >= '' && c < ' ') || (c >= ' ' && c < '℀')) {
/*      */             
/* 1259 */             w.write("\\u");
/* 1260 */             String hhhh = Integer.toHexString(c);
/* 1261 */             w.write("0000", 0, 4 - hhhh.length());
/* 1262 */             w.write(hhhh); break;
/*      */           } 
/* 1264 */           w.write(c);
/*      */           break;
/*      */       } 
/*      */     } 
/* 1268 */     w.write(34);
/* 1269 */     return w;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Object remove(String key) {
/* 1281 */     return this.map.remove(key);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static Object stringToValue(String string) {
/* 1294 */     if (string.equals("")) {
/* 1295 */       return string;
/*      */     }
/* 1297 */     if (string.equalsIgnoreCase("true")) {
/* 1298 */       return Boolean.TRUE;
/*      */     }
/* 1300 */     if (string.equalsIgnoreCase("false")) {
/* 1301 */       return Boolean.FALSE;
/*      */     }
/* 1303 */     if (string.equalsIgnoreCase("null")) {
/* 1304 */       return NULL;
/*      */     }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1312 */     char b = string.charAt(0);
/* 1313 */     if ((b >= '0' && b <= '9') || b == '-') {
/*      */       try {
/* 1315 */         if (string.indexOf('.') > -1 || string.indexOf('e') > -1 || string.indexOf('E') > -1) {
/*      */           
/* 1317 */           Double d = Double.valueOf(string);
/* 1318 */           if (!d.isInfinite() && !d.isNaN()) {
/* 1319 */             return d;
/*      */           }
/*      */         } else {
/* 1322 */           Long myLong = new Long(string);
/* 1323 */           if (string.equals(myLong.toString())) {
/* 1324 */             if (myLong.longValue() == myLong.intValue()) {
/* 1325 */               return new Integer(myLong.intValue());
/*      */             }
/* 1327 */             return myLong;
/*      */           }
/*      */         
/*      */         } 
/* 1331 */       } catch (Exception ignore) {}
/*      */     }
/*      */     
/* 1334 */     return string;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static void testValidity(Object o) throws JSONException {
/* 1346 */     if (o != null) {
/* 1347 */       if (o instanceof Double) {
/* 1348 */         if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
/* 1349 */           throw new JSONException("JSON does not allow non-finite numbers.");
/*      */         }
/*      */       }
/* 1352 */       else if (o instanceof Float && ((
/* 1353 */         (Float)o).isInfinite() || ((Float)o).isNaN())) {
/* 1354 */         throw new JSONException("JSON does not allow non-finite numbers.");
/*      */       } 
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public JSONArray toJSONArray(JSONArray names) throws JSONException {
/* 1373 */     if (names == null || names.length() == 0) {
/* 1374 */       return null;
/*      */     }
/* 1376 */     JSONArray ja = new JSONArray();
/* 1377 */     for (int i = 0; i < names.length(); i++) {
/* 1378 */       ja.put(opt(names.getString(i)));
/*      */     }
/* 1380 */     return ja;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String toString() {
/*      */     try {
/* 1397 */       return toString(0);
/* 1398 */     } catch (Exception e) {
/* 1399 */       return null;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String toString(int indentFactor) throws JSONException {
/* 1418 */     StringWriter w = new StringWriter();
/* 1419 */     synchronized (w.getBuffer()) {
/* 1420 */       return write(w, indentFactor, 0).toString();
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static String valueToString(Object value) throws JSONException {
/* 1449 */     if (value == null || value.equals(null)) {
/* 1450 */       return "null";
/*      */     }
/* 1452 */     if (value instanceof JSONString) {
/*      */       Object object;
/*      */       try {
/* 1455 */         object = ((JSONString)value).toJSONString();
/* 1456 */       } catch (Exception e) {
/* 1457 */         throw new JSONException(e);
/*      */       } 
/* 1459 */       if (object instanceof String) {
/* 1460 */         return (String)object;
/*      */       }
/* 1462 */       throw new JSONException("Bad value from toJSONString: " + object);
/*      */     } 
/* 1464 */     if (value instanceof Number) {
/* 1465 */       return numberToString((Number)value);
/*      */     }
/* 1467 */     if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray)
/*      */     {
/* 1469 */       return value.toString();
/*      */     }
/* 1471 */     if (value instanceof Map) {
/* 1472 */       return (new JSONObject((Map)value)).toString();
/*      */     }
/* 1474 */     if (value instanceof Collection) {
/* 1475 */       return (new JSONArray((Collection)value)).toString();
/*      */     }
/* 1477 */     if (value.getClass().isArray()) {
/* 1478 */       return (new JSONArray(value)).toString();
/*      */     }
/* 1480 */     return quote(value.toString());
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static Object wrap(Object object) {
/*      */     try {
/* 1497 */       if (object == null) {
/* 1498 */         return NULL;
/*      */       }
/* 1500 */       if (object instanceof JSONObject || object instanceof JSONArray || NULL.equals(object) || object instanceof JSONString || object instanceof Byte || object instanceof Character || object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Boolean || object instanceof Float || object instanceof Double || object instanceof String)
/*      */       {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 1507 */         return object;
/*      */       }
/*      */       
/* 1510 */       if (object instanceof Collection) {
/* 1511 */         return new JSONArray((Collection)object);
/*      */       }
/* 1513 */       if (object.getClass().isArray()) {
/* 1514 */         return new JSONArray(object);
/*      */       }
/* 1516 */       if (object instanceof Map) {
/* 1517 */         return new JSONObject((Map)object);
/*      */       }
/* 1519 */       Package objectPackage = object.getClass().getPackage();
/* 1520 */       String objectPackageName = (objectPackage != null) ? objectPackage.getName() : "";
/*      */       
/* 1522 */       if (objectPackageName.startsWith("java.") || objectPackageName.startsWith("javax.") || object.getClass().getClassLoader() == null)
/*      */       {
/*      */         
/* 1525 */         return object.toString();
/*      */       }
/* 1527 */       return new JSONObject(object);
/* 1528 */     } catch (Exception exception) {
/* 1529 */       return null;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Writer write(Writer writer) throws JSONException {
/* 1543 */     return write(writer, 0, 0);
/*      */   }
/*      */ 
/*      */   
/*      */   static final Writer writeValue(Writer writer, Object value, int indentFactor, int indent) throws JSONException, IOException {
/* 1548 */     if (value == null || value.equals(null)) {
/* 1549 */       writer.write("null");
/* 1550 */     } else if (value instanceof JSONObject) {
/* 1551 */       ((JSONObject)value).write(writer, indentFactor, indent);
/* 1552 */     } else if (value instanceof JSONArray) {
/* 1553 */       ((JSONArray)value).write(writer, indentFactor, indent);
/* 1554 */     } else if (value instanceof Map) {
/* 1555 */       (new JSONObject((Map)value)).write(writer, indentFactor, indent);
/* 1556 */     } else if (value instanceof Collection) {
/* 1557 */       (new JSONArray((Collection)value)).write(writer, indentFactor, indent);
/*      */     }
/* 1559 */     else if (value.getClass().isArray()) {
/* 1560 */       (new JSONArray(value)).write(writer, indentFactor, indent);
/* 1561 */     } else if (value instanceof Number) {
/* 1562 */       writer.write(numberToString((Number)value));
/* 1563 */     } else if (value instanceof Boolean) {
/* 1564 */       writer.write(value.toString());
/* 1565 */     } else if (value instanceof JSONString) {
/*      */       Object o;
/*      */       try {
/* 1568 */         o = ((JSONString)value).toJSONString();
/* 1569 */       } catch (Exception e) {
/* 1570 */         throw new JSONException(e);
/*      */       } 
/* 1572 */       writer.write((o != null) ? o.toString() : quote(value.toString()));
/*      */     } else {
/* 1574 */       quote(value.toString(), writer);
/*      */     } 
/* 1576 */     return writer;
/*      */   }
/*      */   
/*      */   static final void indent(Writer writer, int indent) throws IOException {
/* 1580 */     for (int i = 0; i < indent; i++) {
/* 1581 */       writer.write(32);
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
/*      */     try {
/* 1597 */       boolean commanate = false;
/* 1598 */       int length = length();
/* 1599 */       Iterator keys = keys();
/* 1600 */       writer.write(123);
/*      */       
/* 1602 */       if (length == 1) {
/* 1603 */         Object key = keys.next();
/* 1604 */         writer.write(quote(key.toString()));
/* 1605 */         writer.write(58);
/* 1606 */         if (indentFactor > 0) {
/* 1607 */           writer.write(32);
/*      */         }
/* 1609 */         writeValue(writer, this.map.get(key), indentFactor, indent);
/* 1610 */       } else if (length != 0) {
/* 1611 */         int newindent = indent + indentFactor;
/* 1612 */         while (keys.hasNext()) {
/* 1613 */           Object key = keys.next();
/* 1614 */           if (commanate) {
/* 1615 */             writer.write(44);
/*      */           }
/* 1617 */           if (indentFactor > 0) {
/* 1618 */             writer.write(10);
/*      */           }
/* 1620 */           indent(writer, newindent);
/* 1621 */           writer.write(quote(key.toString()));
/* 1622 */           writer.write(58);
/* 1623 */           if (indentFactor > 0) {
/* 1624 */             writer.write(32);
/*      */           }
/* 1626 */           writeValue(writer, this.map.get(key), indentFactor, newindent);
/*      */           
/* 1628 */           commanate = true;
/*      */         } 
/* 1630 */         if (indentFactor > 0) {
/* 1631 */           writer.write(10);
/*      */         }
/* 1633 */         indent(writer, indent);
/*      */       } 
/* 1635 */       writer.write(125);
/* 1636 */       return writer;
/* 1637 */     } catch (IOException exception) {
/* 1638 */       throw new JSONException(exception);
/*      */     } 
/*      */   }
/*      */ }


/* Location:              D:\Users\imnot\OneDrive\Desktop\test\hello\module_example-0.1-26.1-0.15.4-mc1.12.1_deobfuscated.jar!\org\json\JSONObject.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       1.1.3
 */