package eu.leads.processor.core.net;

import com.google.common.base.Strings;
import eu.leads.processor.core.ServiceCommand;
import eu.leads.processor.core.comp.ComponentMode;
import eu.leads.processor.core.comp.ComponentState;
import eu.leads.processor.core.comp.ServiceStatus;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by vagvaz on 7/8/14.
 */
public class MessageUtils {
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String COMTYPE = "comtype";

    public static JsonObject createLeadsMessage(JsonObject message, String from) {
        return createLeadsMessage(message, from, null, null);
    }

    public static JsonObject createLeadsMessage(JsonObject message, String from, String to) {
        return createLeadsMessage(message, from, to, null);
    }

    public static JsonObject createLeadsMessage(JsonObject message, String from, String to,
                                                   String type) {
        JsonObject result = message;
        if (!Strings.isNullOrEmpty(from)) {
            result.putString(FROM, from);
        }
        if (!Strings.isNullOrEmpty(to)) {
            result.putString(TO, to);
        }
        if (!Strings.isNullOrEmpty(type)) {
            result.putString(COMTYPE, type);
        }
        return result;
    }

    public static JsonObject createServiceStatusMessage(ServiceStatus status, String id,
                                                           String service) {
        JsonObject result = new JsonObject();
        result.putString("type", MessageTypeConstants.SERVICE_STATUS_REPLY);
        result.putString("status", status.toString());
        result.putString("id", id);
        result.putString("service", service);
        return result;
    }

    public static JsonObject createComponentStateMessage(ComponentState state, String id,
                                                            String componentType) {
        JsonObject result = new JsonObject();
        result.putString("type", MessageTypeConstants.COMPONENT_STATUS_REPLY);
        result.putString("status", state.toString());
        result.putString("id", id);
        result.putString("component", componentType);
        return result;
    }

    public static JsonObject createComponentModeMessage(ComponentMode mode, String id,
                                                           String componentType) {
        JsonObject result = new JsonObject();
        result.putString("type", MessageTypeConstants.COMPONENT_MODE_REPLY);
        result.putString("mode", mode.toString());
        result.putString("id", id);
        result.putString("component", componentType);
        return result;
    }

    public static JsonObject createServiceCommand(ServiceCommand cmd) {
        JsonObject result = new JsonObject();
        result.putString("type", MessageTypeConstants.SERVICE_CMD);
        result.putString("command", cmd.toString());
        return result;
    }
}
