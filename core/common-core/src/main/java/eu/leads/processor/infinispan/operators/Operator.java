package eu.leads.processor.infinispan.operators;


import org.vertx.java.core.json.JsonObject;

public interface Operator{
    public JsonObject getConfiguration();
    public void init(JsonObject config);
    public void execute();
    public void cleanup();
    public void setConfiguration(JsonObject config);
    public String getInput();
    public void setInput(String input);
    public String getOutput();
    public void setOutput(String output);
    public JsonObject getOperatorParameters();
    public void setOperatorParameters(JsonObject parameters);

}
