package eu.leads.processor.math;

import org.vertx.java.core.json.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vagvaz
 * Date: 11/7/13
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
//Mathematical Utilities
public class MathUtils {
    public static Object add(Object o1, Object o2, String type) {
        if (type.equalsIgnoreCase("double") || type.equals("float")) {
            return add((Double) o1, (Double) o2);
        } else if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("long")) {
            return add((Long) o1, (Long) o2);
        } else if (type.equalsIgnoreCase("string")) {
            return ((String) o1).concat((String) o2);
        } else if (type.equalsIgnoreCase("date")) {
            return null;
        }
        return null;
    }

    private static Long add(Long o1, Long o2) {
        return o1 + o2;
    }

    private static Double add(Double o1, Double o2) {
        return o1 + o2;
    }


    public static Object divide(Object o1, Object o2, String type) {
        if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")) {
            return add((Double) o1, (Double) o2);
        } else if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("long")) {
            return add((Long) o1, (Long) o2);
        } else if (type.equalsIgnoreCase("string")) {
            return "";
        } else if (type.equalsIgnoreCase("date")) {
            return null;
        }
        return null;
    }

    private static Long divide(Long o1, Long o2) {
        return o1 / o2;
    }

    private static Double divide(Double o1, Double o2) {
        return o1 / o2;
    }


    public static Object compare(Object o1, Object o2, String type) {
        if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")) {
            return compare((Double) o1, (Double) o2);
        } else if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("long")) {
            return compare((Long) o1, (Long) o2);
        } else if (type.equalsIgnoreCase("string")) {
            return ((String) o1).compareTo((String) o2);
        } else if (type.equalsIgnoreCase("date")) {
            return compare((Date) o1, (Date) o2);
        }
        return 0;
    }

    public static int compare(Long o1, Long o2) {
        return o1.compareTo(o2);
    }

    public static int compare(Double o1, Double o2) {
        return o1.compareTo(o2);
    }

    public static int compare(Date d1, Date d2) {
        return d1.compareTo(d2);
    }

    public static String handleType(String t1) {
        if (t1.equalsIgnoreCase("double") || t1.equalsIgnoreCase("float")) {
            return "double";
        } else if (t1.equalsIgnoreCase("int") || t1.equalsIgnoreCase("long"))
            return "long";
        else
            return "string";
    }

    public static String handleTypes(String t1, String t2) {
        if (t1.equalsIgnoreCase("double") || t1.equalsIgnoreCase("float")) {
            if (t2.equalsIgnoreCase("double") || t2.equalsIgnoreCase("float")) {
                return "double";
            } else if (t2.equalsIgnoreCase("int") || t2.equalsIgnoreCase("long")) {
                return "double";
            } else {
                return "string";
            }
        }
        return "string";
    }

    public static boolean isArithmentic(String type) {
        return type.equalsIgnoreCase("double") || type.equals("int") || type.equalsIgnoreCase("float") || type.equalsIgnoreCase("long");
    }

   public static boolean lessThan(JsonObject left, JsonObject right) {
      String type = left.getObject("body").getObject("datum").getString("type");
      if(type.startsWith("TEXT")){
         String leftValue = left.getObject("body").getObject("datum").getObject("body").getString("val");
         String rightValue = right.getObject("body").getObject("datum").getObject("body").getString("val");
         return leftValue.compareTo(rightValue) < 0;
      }
      else if (type.startsWith("INT")){
         Long leftValue = left.getObject("body").getObject("datum").getObject("body").getLong("val");
         Long rightValue = right.getObject("body").getObject("datum").getObject("body").getLong("val");
         return leftValue.compareTo(rightValue) < 0;
      }
      else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")){
         Number leftValue = left.getObject("body").getObject("datum").getObject("body").getNumber("val");
         Number rightValue = right.getObject("body").getObject("datum").getObject("body").getNumber("val");
         return leftValue.doubleValue() < rightValue.doubleValue();
      }
      else{
         System.out.println("Unknonw type " + type);
         Object leftValue = left.getObject("body").getObject("datum").getObject("body").getValue("val");
         Object rightValue = right.getObject("body").getObject("datum").getObject("body").getValue("val");
         return leftValue.toString().compareTo(rightValue.toString()) < 0;
      }
   }

   public static boolean lessEqualThan(JsonObject left, JsonObject right) {
      String type = left.getObject("body").getObject("datum").getString("type");
      if (type.startsWith("TEXT")) {

         String leftValue = left.getObject("body").getObject("datum").getObject("body").getString("val");
         String rightValue = right.getObject("body").getObject("datum").getObject("body").getString("val");
         return leftValue.compareTo(rightValue) <= 0;
      } else if (type.startsWith("INT")) {
         Long leftValue = left.getObject("body").getObject("datum").getObject("body").getLong("val");
         Long rightValue = right.getObject("body").getObject("datum").getObject("body").getLong("val");
         return leftValue.compareTo(rightValue) <= 0;
      } else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")) {
         Number leftValue = left.getObject("body").getObject("datum").getObject("body").getNumber("val");
         Number rightValue = right.getObject("body").getObject("datum").getObject("body").getNumber("val");
         return leftValue.doubleValue() <= rightValue.doubleValue();
      } else {
         System.out.println("Unknonw type " + type);
         Object leftValue = left.getObject("body").getObject("datum").getObject("body").getValue("val");
         Object rightValue = right.getObject("body").getObject("datum").getObject("body").getValue("val");
         return leftValue.toString().compareTo(rightValue.toString()) <= 0;
      }
   }

   public static boolean greaterThan(JsonObject left, JsonObject right) {
      return lessThan(right, left);
   }

   public static boolean greaterEqualThan(JsonObject left, JsonObject right) {
      return lessEqualThan(right,left);
   }

   public static boolean like(JsonObject leftValue, JsonObject rightValue, JsonObject value) {
      boolean result = false;
      byte[] patternBytes = null;
      if(leftValue.getString("type").equals("CONST")){
        JsonObject body = leftValue.getObject("body").getObject("datum").getObject("body");
        org.vertx.java.core.json.JsonArray bytes = body.getArray("bytes");
        int size = body.getInteger("size");
        patternBytes = new byte[size];
        for (int i = 0; i < size; i++) {
          patternBytes[i] = bytes.get(i);
        }


      }
     if(rightValue.getString("type").equals("CONST")){
       JsonObject body = rightValue.getObject("body").getObject("datum").getObject("body");
       org.vertx.java.core.json.JsonArray bytes = body.getArray("bytes");
       int size = body.getInteger("size");
       patternBytes = new byte[size+1];
       patternBytes[patternBytes.length-1]=0;
       if(bytes != null) {
         for (int i = 0; i < size; i++) {
           int intByte = bytes.get(i);
           patternBytes[i] = (byte)intByte;
         }

       }
     }
     String pattern = new String(patternBytes);
      pattern = pattern.replaceAll("%", "__");
      pattern.trim();
      pattern = "[" + pattern + "]";
//      Pattern regex = Pattern.compile(pattern);
      if(leftValue.getString("type").equals("FIELD"))
      {
         String testString = leftValue.getObject("body").getObject("datum").getObject("body").getString("val");
//         result =regex.matcher(testString).matches();
        if(pattern.startsWith("__") && pattern.endsWith("__")) {
          pattern = pattern.replaceAll("__", "");
          result = testString.contains(pattern);
        }else if(pattern.startsWith("*")){
          pattern = pattern.replaceAll("__","");
          result = testString.startsWith(pattern);
        }
        else{
          pattern = pattern.replaceAll("__","");
          result = testString.endsWith(pattern);
        }


      }
      if(rightValue.getString("type").equals("FIELD"))
      {
         String testString = rightValue.getObject("body").getObject("datum").getObject("body").getString("val");
//         result =regex.matcher(testString).matches();
        if(pattern.startsWith("*") && pattern.endsWith("*"))
          result = testString.contains(pattern);
        else if(pattern.startsWith("*")){
          result = testString.startsWith(pattern);
        }
        else{
          result = testString.endsWith(pattern);
        }

      }

//      if(value.getObject("body").getBoolean("not"))
//      {
//         return !result;
//      }
      return result;
   }

   public static Object getInitialValue(String type, String function) {
      Object result = null;
      if (type.startsWith("TEXT")) {
         result = new String();
      } else if (type.startsWith("INT")) {
         if(function.equals("sum")  || function.equals("count")){
            result = new Long(0);
         }
         else if(function.equals("max")){
            result = new Long(Long.MIN_VALUE);
         }
         else if (function.equals("min")){
            result = new Long(Long.MAX_VALUE);
         }
         else if (function.equals("avg")){
            Map<String,Object> tmp  = new HashMap<String,Object>();
            tmp.put("sum", new Long(0));
            tmp.put("count",new Long(0));
            result = tmp;
         }
      } else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")) {
         if(function.equals("sum") || function.equals("count")){
            result = new Double(0.0);
         }
         else if(function.equals("max")){
            result = new Double(Double.MIN_VALUE);
         }
         else if (function.equals("min")){
            result = new Double(Double.MAX_VALUE);
         }
         else if (function.equals("avg")){
            Map<String,Object> tmp  = new HashMap<String,Object>();
            tmp.put("sum",new Double(0.0));
            tmp.put("count",new Long(0));
            result = tmp;
         }
      }else if (type.startsWith("LONG")){
         if(function.equals("sum")  || function.equals("count")){
            result = new Long(0);
         }
         else if(function.equals("max")){
            result = new Long(Long.MIN_VALUE);
         }
         else if (function.equals("min")){
            result = new Long(Long.MAX_VALUE);
         }
         else if (function.equals("avg")){
            Map<String,Object> tmp  = new HashMap<String,Object>();
            tmp.put("sum",new Long(0));
            tmp.put("count",new Long(0));
            result = tmp;
         }
      }
      else {
         System.out.println("Unknonw type " + type);
         result = new Long(0);
      }

      return result;
   }

   public static Object updateFunctionValue(String function, String type, Object oldValue,Object currentValue) {
      Object result = oldValue;
      if(function.equals("sum") ){
         result = updateSumValue(type,oldValue,currentValue);
      }
      else if( function.equals("count")){
         result = updateCountValue(type,oldValue,currentValue);
      }
      else if(function.equals("max")){
         result = updateMaxValue(type,oldValue,currentValue);
      }
      else if (function.equals("min")){
         result = updateMinValue(type, oldValue, currentValue);
      }
      else if (function.equals("avg")){
         result = updateAvgValue(type,oldValue,currentValue);
      }
      else {
         System.out.println("Unknonw function " + type);
         result = updateCountValue(type,oldValue,currentValue);
      }

      return result;
   }

   private static Object updateAvgValue(String type, Object oldValue, Object currentValue) {
      Object result = oldValue;
      if(type.startsWith("TEXT")){
         result = currentValue;
      }
      else if (type.startsWith("INT")){
         int currentValueInt = (int)currentValue;
         Map<String,Object> oldMap = (Map<String, Object>) oldValue;
         oldMap.put("count",(Long)oldMap.get("count")+1);
         oldMap.put("sum",(Long)oldMap.get("sum")+currentValueInt);
         result = oldMap;
      }
      else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")){
         Map<String,Object> oldMap = (Map<String, Object>) oldValue;
         oldMap.put("count",(Long)oldMap.get("count")+1);
         oldMap.put("sum",(Double)oldMap.get("sum")+(Double)currentValue);
         result = oldMap;
      }
      else{
         System.out.println("Unknonw type " + type);
         Map<String,Object> oldMap = (Map<String, Object>) oldValue;
         oldMap.put("count",(Long)oldMap.get("count")+1);
         oldMap.put("sum",(Long)oldMap.get("sum")+((int)currentValue));
         result = oldMap;
      }
      return result;
   }

   private static Object updateMinValue(String type, Object oldValue, Object currentValue) {
      Object result = oldValue;
      if(type.startsWith("TEXT")){
         String old = (String) oldValue;
         String current = (String) currentValue;
         if(old.compareTo(current) > 0){
            result = current;
         }
      }
      else if (type.startsWith("INT")){
         Long old = (Long) oldValue;
         int current = (int) currentValue;
         if(current < old){
            result = new Long(current);
         }
      }
      else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")){
         Double old = (Double) oldValue;
         Double current = (Double) currentValue;
         if(old.compareTo(current) > 0){
            result = current;
         }
      }
      else{
         System.out.println("Unknonw type " + type);
        Long old = (Long) oldValue;
        int current = (int) currentValue;
        if(current < old){
          result = new Long(current);
        }
      }
      return result;
   }

   private static Object updateMaxValue(String type, Object oldValue, Object currentValue) {
      Object result = oldValue;
      if(type.startsWith("TEXT")){
         String old = (String) oldValue;
         String current = (String) currentValue;
         if(old.compareTo(current) < 0){
            result = current;
         }
      }
      else if (type.startsWith("INT")){
        Long old = (Long) oldValue;
        int current = (int) currentValue;
        if(current > old){
          result = new Long(current);
        }
      }
      else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")){
         Double old = (Double) oldValue;
         Double current = (Double) currentValue;
         if(old.compareTo(current) < 0){
            result = current;
         }
      }
      else{
         System.out.println("Unknonw type " + type);
         Long old = (Long) oldValue;
        int current = (int) currentValue;
        if(current > old){
          result = new Long(current);
        }
      }
      return result;

   }

   private static Object updateCountValue(String type, Object oldValue, Object currentValue) {
      Long result = (Long) oldValue;
      result += 1;
      return result;
   }

   private static Object updateSumValue(String type, Object oldValue, Object currentValue) {
      Object result = oldValue;
      if(type.startsWith("TEXT")){
         result = oldValue.toString().concat(currentValue.toString());
      }
      else if (type.startsWith("INT")){
         Long old = (Long) oldValue;
         int current = (int)currentValue;
         result = current + old;

      }
      else if (type.startsWith("FLOAT") || type.startsWith("DOUBLE")){
         Double old = (Double) oldValue;
         Double current = (Double) currentValue;
         result = current + old;
      }
      else{
         System.out.println("Unknonw type " + type);
        Long old = (Long) oldValue;
        int current = (int)currentValue;
        result = current + old;
      }
      return result;
   }

   public static Double computeAvg(Map<String, Object> avgMap) {
      long count = (long) avgMap.get("count");
      double sum = 0;
      if(avgMap.get("sum") instanceof Long){
         sum += ((Long)avgMap.get("sum")).doubleValue();
      }
      else if (avgMap.get("sum") instanceof Double){
        sum+= ((Double)avgMap.get("sum")).doubleValue();
      }
      else{
         sum += (Double)avgMap.get("sum");
      }
      return sum / count;
   }
}
