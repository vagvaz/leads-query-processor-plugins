package test;

import org.junit.Test;

import eu.leads.processor.plugins.sentiment.SentimentAnalysisModule;

public class SentimentAnalysisModuleTest {

	String text = "For 90 minutes, the plucky underdogs gave a pretty good account of themselves. Not content to merely defend against their heavily favoured opponents, they threatened on a number of occasions to break the deadlock and pull off a major shock.\n"
			+ "But in stoppage time, they let their guard down for a brief but fatal second and allowed the opposition’s star striker just enough space on the right edge of the box to size up and curl a delightful left-footed shot into the far corner of the net for the winning goal.";
	SentimentAnalysisModule sentModule = new SentimentAnalysisModule(
			"classifiers/english.all.3class.distsim.crf.ser.gz");
	String[] entities = new String[] { "plucky underdogs", "striker" };

	@Test
	public void testGetOverallSentiment() {
		System.out.println(sentModule.getOverallSentiment(text));
	}

	@Test
	public void testGetSentimentForEntity() {
		for (String entity : entities)
			System.out.println(sentModule.getSentimentForEntity(entity, text));
	}

}
