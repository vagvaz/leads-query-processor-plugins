package eu.leads.processor.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.util.*;

public class Tuple extends DataType {

    public Tuple(String value) {
            this.data = new JsonObject(value);
    }

    public Tuple(Tuple tl, Tuple tr, ArrayList<String> ignoreColumns) {
       super(tl.asJsonObject());

        for (String field : ignoreColumns) {
            if (data.containsField(field))
                data.removeField(field);
        }
        tr.removeAtrributes(ignoreColumns);

       data.mergeIn(tr.asJsonObject());
    }

    public String asString() {
        return data.toString();
    }

    /**
     * Getter for property 'fieldSet'.
     *
     * @return Value for property 'fieldSet'.
     */
    public Set<String> getFieldSet() {

        return data.getFieldNames();
    }

    public void setAttribute(String attributeName, String value) {
       data.putString(attributeName,value);
    }
    public void setNumberAttribute(String attributeName, Number value){
       data.putNumber(attributeName,value);
    }
    public void  setBooleanAttribute(String attributeName, boolean value){
       data.putBoolean(attributeName,value);
    }
   public void setObjectAttribute(String attributeName, JsonObject value){
      data.putObject(attributeName,value);
   }

    public String getAttribute(String column) {
        return data.getString(column).toString();
    }
    public Number getNumberAttribute(String column){return data.getNumber(column);}
    public boolean getBooleanAttribute(String column){return data.getBoolean(column);}
   public JsonObject getObjectAttribute(String column){return data.getObject(column);}
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return  data.toString();
    }

    public void keepOnly(List<String> columns) {
        Set<String> fields =  new HashSet<>(data.getFieldNames());
        Set<String> keep = new HashSet<String>();
        keep.addAll(columns);
        for(String field : fields){
           if(!keep.contains(field)){
              data.removeField(field);
           }
        }
    }

    public void removeAtrributes(List<String> columns) {
        for(String column : columns)
           data.removeField(column);
    }

    public String toPresentString() {
       return data.toString();
    }

    /**
     * Getter for property 'fieldNames'.
     *
     * @return Value for property 'fieldNames'.
     */
    public Set<String> getFieldNames() {
        return data.getFieldNames();
    }

    public boolean hasField(String attribute) {
        return data.containsField(attribute);
    }

   public void removeAttribute(String field) {
      data.removeField(field);
   }

   public void renameAttribute(String oldName, String newName) {
      if(oldName == newName)
         return;
      Object value = data.getValue(oldName);
      data.removeField(oldName);
      data.putValue(newName,value);
   }

   public Object getGenericAttribute(String attribute) {
      return data.getValue(attribute);
   }

   public void setAttribute(String name, Object tupleValue) {
      data.putValue(name,tupleValue);
   }

   public void renameAttributes(Map<String, String> toRename) {
      for(Map.Entry<String,String> entry : toRename.entrySet()){
         renameAttribute(entry.getKey(),entry.getValue());
      }
   }
}
