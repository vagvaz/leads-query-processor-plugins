package eu.leads.infext.proc.com.keyword;

import eu.leads.PropertiesSingleton;
import eu.leads.processor.sentiment.Sentiment;
import eu.leads.processor.sentiment.SentimentAnalysisModule;

public class SentimentScore {
	
	private String classifierName = PropertiesSingleton.getResourcesDir()+"/classifiers/english.all.3class.distsim.crf.ser.gz";
	private SentimentAnalysisModule sentimentModuleEn = new SentimentAnalysisModule(classifierName);

	public SentimentScore() {
		
	}
	
	public Sentiment getSentimentForEntity(String targetEntity, String text, String lang) {
		Sentiment sentiment = null;
		if(lang != null && lang.equals("en")) {
			sentiment = sentimentModuleEn.getSentimentForEntity(targetEntity, text);
			System.out.println(sentiment);
		}
		return sentiment;
	}
	
	public static void main(String[] args) {
		String text = "For 90 minutes, the plucky underdogs gave a pretty good account of themselves. Not content to merely defend against their heavily favoured opponents, they threatened on a number of occasions to break the deadlock and pull off a major shock.\n"
				+ "But in stoppage time, they let their guard down for a brief but fatal second and allowed the opposition’s star striker just enough space on the right edge of the box to size up and curl a delightful left-footed shot into the far corner of the net for the winning goal.";
		String tex2 = "I love dogs";
		String tex3 = "What a mighty difference two years can make. All these years, Adidas had a hard time trying to take a bite of the performance running footwear market; but the Boost launch last year suddenly changed all that. A singular cushioning platform has led to a reversal of fortunes for the German brand, leading to a tectonic shift in the running consumer’s perception – with a heavy tilt in its favour.";
		String[] entities = new String[] { "plucky underdogs", "striker" };
		String[] entitie2 = new String[] { "dogs" };
		String[] entitie3 = new String[] { "cushioning", "difference" };
		SentimentScore sentimentScore = new SentimentScore();
		for (String entity : entitie3)
			System.out.println(sentimentScore.getSentimentForEntity(entity, tex3, "en"));
	}
}
