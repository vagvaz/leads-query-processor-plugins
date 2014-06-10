package eu.leads.processor.plugins.sentiment;

import java.util.Set;


public interface SentimentAnalysis {
  public Sentiment getOverallSentiment(String text);

  public Sentiment getSentimentForEntity(String targetEntity, String text);

  public Set<Entity> getEntities(String text);
}
