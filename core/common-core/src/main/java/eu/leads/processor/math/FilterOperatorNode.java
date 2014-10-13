package eu.leads.processor.math;

import eu.leads.processor.core.Tuple;
import eu.leads.processor.core.TupleUtils;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by vagvaz on 9/24/14.
 */
public class FilterOperatorNode {
   FilterOpType type;
   FilterOperatorNode left;
   FilterOperatorNode right;
   JsonObject value;
   public FilterOperatorNode(JsonElement node){
       type = FilterOpType.valueOf(node.asObject().getString("type"));
       if(type.equals(FilterOpType.FIELD) || type.equals(FilterOpType.CONST))
       {
          value = node.asObject();
          left = null;
          right = null;
       }
       else{
          left = new FilterOperatorNode(node.asObject().getObject("body").getElement("leftExpr"));
          right= new FilterOperatorNode(node.asObject().getObject("body").getElement("rightExpr"));
          value = new JsonObject();
          value.putString("type","CONST");
          JsonObject body = new JsonObject();
          body.putString("type","CONST");
          JsonObject datum = new JsonObject();
          datum.putString("type","BOOLEAN");
          datum.putObject("body",new JsonObject());
          datum.getObject("body").putString("type","BOOLEAN");
          body.putObject("datum",datum);
          value.putObject("body",body);
       }

   }


   public boolean accept(Tuple t){
         boolean result =false;
         if(left != null)
            left.accept(t);
         if(right != null)
            right.accept(t);

         switch (type) {
            case NOT:
               //TODO
               break;
            case AND: {
               boolean leftValue = left.getValueAsBoolean();
               boolean rightValue = right.getValueAsBoolean();
               result =  leftValue && rightValue;
            }
               break;
            case OR: {
               boolean leftValue = left.getValueAsBoolean();
               boolean rightValue = right.getValueAsBoolean();

               result =  leftValue || rightValue;
            }
               break;
            case EQUAL:
               result = left.getValue().equals(right.getValue());
               break;
            case IS_NULL:
               result = left.isValueNull();
               break;
            case NOT_EQUAL:
               result = !(left.getValue().equals(right.getValue()));
               break;
            case LTH:
               result = MathUtils.lessThan(left.getValueAsJson(),right.getValueAsJson());
               break;
            case LEQ:
               result = MathUtils.lessEqualThan(left.getValueAsJson(),right.getValueAsJson());
               break;
            case GTH:
               result = MathUtils.greaterThan(left.getValueAsJson(),right.getValueAsJson());
               break;
            case GEQ:
               result = MathUtils.greaterEqualThan(left.getValueAsJson(),right.getValueAsJson());
               break;
            case AGG_FUNCTION:
               break;
            case FUNCTION:
               break;
            case LIKE:
               result = MathUtils.like(left.getValueAsJson(),right.getValueAsJson(),value);
               break;
            case IN:
               //TODO
               break;
            case FIELD:
               this.value.getObject("body").putObject("datum", computeDatum(t));
               result = true;
               return result;
            case CONST:
               result = true;
               return result;
         }
      putBooleanDatum(result);
      return result;
   }

   private boolean getValueAsBoolean() {
      Object object =  value.getObject("body").getObject("datum").getObject("body").getValue("val");
      if(object instanceof Boolean)
         return (Boolean)object;
      else{
         if(object.toString().equals("") || object.toString().equals("0"))
            return false;
         else
            return true;
      }
   }

   private boolean isValueNull() {
      return value.getObject("body").getObject("datum").getObject("body").containsField("val");
   }

   private String getValue() {
      return value.getObject("body").getObject("datum").getObject("body").getValue("val").toString();
   }

   private JsonObject getValueAsJson() {
      return value;
   }

   private void putBooleanDatum(boolean val) {
      JsonObject result = new JsonObject();
      result.putString("type","BOOLEAN");
      result.putObject("body",new JsonObject());
      result.getObject("body").putString("type", result.getString("type"));
      result.getObject("body").putBoolean("val", val);
      value.getObject("body").putObject("datum", result);

   }

   private JsonObject computeDatum(Tuple t) {
      JsonObject result = new JsonObject();
      result.putString("type",value.getObject("body").getObject("column").getObject("dataType").getString("type"));
      result.putObject("body",new JsonObject());
      result.getObject("body").putString("type", result.getString("type"));
      result.getObject("body").putValue("val",t.getGenericAttribute(value.getObject("body").getObject("column").getString("name")));
      return result;
   }
}
