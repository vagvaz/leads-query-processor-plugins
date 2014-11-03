package eu.leads.infext.proc.com.maincontent;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class BoilerpipeMainContentExtraction {
	
	private ArticleExtractor extractor = de.l3s.boilerpipe.extractors.ArticleExtractor.INSTANCE;

	public String extractArticle(String content) {
		try {
			String maincontent = extractor.getText(content);
			return maincontent;
		} catch (BoilerpipeProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		BoilerpipeMainContentExtraction extr = new BoilerpipeMainContentExtraction();
		System.out.print(extr.extractArticle("bleble."));
		System.out.print("done");
	}
	
}
