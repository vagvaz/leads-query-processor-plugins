package eu.leads.processor.sentiment;

public class Entity {
    String name;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object ob) {
        Entity other = null;
        if (ob instanceof Entity) {
            other = (Entity) ob;
            return other.getName().equals(name);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Entity [name=" + name + ", type=" + type + "]";
    }
}
