package eu.leads.processor.plugins.sentiment;

public class Sentiment {
  String tag;
  double value;

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Sentiment [tag=" + tag + ", value=" + value + "]";
  }
}
