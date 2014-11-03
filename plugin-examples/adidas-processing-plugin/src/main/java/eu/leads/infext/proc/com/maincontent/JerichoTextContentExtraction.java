package eu.leads.infext.proc.com.maincontent;

import net.htmlparser.jericho.Source;

public class JerichoTextContentExtraction {

	public String extractText(String content) {
		Source source = new Source(content);
		String text = source.getTextExtractor().toString();
		return text;
	}
}
