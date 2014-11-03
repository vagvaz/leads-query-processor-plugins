package eu.leads.infext.proc.com.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import eu.leads.datastore.datastruct.UrlTimestamp;
import eu.leads.infext.proc.com.keyword.RelevanceScore;
import eu.leads.infext.proc.com.keyword.model.Relevance;


public class DocumentKeywordSearch {

    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    private StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
    // 1. create the index
    private Directory index = new RAMDirectory();
    private IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
    
    private IndexWriter w = null;
    
    public DocumentKeywordSearch() {
		try {
			w = new IndexWriter(index, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    /*
     * Returns documents when the keywords are found together with relevance score
     */
	public HashMap<UrlTimestamp,Double> searchKeywords(String [] keywords) {
		
		HashMap<UrlTimestamp,Double> docsWithKeywords = new HashMap<>();
		
		String querystr = "";
		
		int i;
		for(i=0; i<keywords.length-1; i++) {
			keywords[i] = keywords[i].toLowerCase();
			querystr += keywords[i] + " AND ";
		}
		querystr += keywords[i];
		keywords[i] = keywords[i].toLowerCase();

	    // the "title" arg specifies the default field to use
	    // when no field is explicitly specified in the query.
	    Query q;
		try {
			q = new QueryParser(Version.LUCENE_4_9, "text", analyzer).parse(querystr);
			
			if(w.isLocked(index)) w.close();
			
		    // 3. search
		    int hitsPerPage = 20;
		    IndexReader reader;
				
		    reader = DirectoryReader.open(index);
			
		    IndexSearcher searcher = new IndexSearcher(reader);
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		    searcher.search(q, collector);
		    List<Integer> hitDocs = new ArrayList<>();
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    for(i=0; i<hits.length; ++i) {
		    	int docId = hits[i].doc;
		    	hitDocs.add(docId);
		    }
		    
		    for(i=0; i<reader.maxDoc(); i++) {
		    	Terms vector = reader.getTermVector(i, "text");
		    	TermsEnum termsEnum = vector.iterator(TermsEnum.EMPTY);
		    	BytesRef term;
		    	
		    	Document d = reader.document(i);
		    	
		    	if(hitDocs.contains(i)) {
			    	String text = d.getField("text").stringValue();
			    	int articleLen = getWordLength(text);
			    	
				    Integer[][] keywordsPositionsArray = new Integer[keywords.length][];
			    	
				    int k=0;
			    	while((term=termsEnum.next())!=null){
			            String docTerm = term.utf8ToString();
			            
			            Integer[] keywordPositions = null;
			            
			            //Check if the current term is the same as the query term and if so
			            //retrieve all positions (can be multiple occurrences of a term in a field) corresponding to the term
			            if (Arrays.asList(keywords).contains(docTerm)) {
			                //Retrieve the term frequency in the current document		            
			            	DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions(null, null, DocsAndPositionsEnum.FLAG_OFFSETS);
			                docPosEnum.nextDoc();
			                int freq=docPosEnum.freq();
			                //System.out.println(docTerm+" freq = "+freq);
			                
			                keywordPositions = new Integer[freq];
			                
			                for(int j=0; j<freq; j++){
			                    int position=docPosEnum.nextPosition();
			                    int start=docPosEnum.startOffset();
			                    int end=docPosEnum.endOffset();
			                    //System.out.println(docTerm+": "+position+","+start+","+end);
			                    //Store start, end and position in a list
			                    
			                    keywordPositions[j] = position;
			                }
			                
			                keywordsPositionsArray[k] = keywordPositions;
			                k++;
			            }
			    	}
			    	
			    	//System.out.println("articleLen: "+articleLen);
			    	
			    	RelevanceScore relevanceScore = new RelevanceScore();
			    	Relevance relevance = relevanceScore.getRelevanceForEntity(articleLen, keywordsPositionsArray);
			    	
			    	docsWithKeywords.put(new UrlTimestamp(d.get("type"), d.get("id")),relevance.getValue());
		    	}
		    }
		    
		    
		    // 4. display results
		    for(i=0; i<hits.length; ++i) {
		    	int docId = hits[i].doc;
		        
		    	Fields fields = reader.getTermVectors(docId);  
		    	fields.terms("text");
		    	for(String field : fields)
		    		System.out.println(field);
				
		    	Document d = searcher.doc(docId);
		    	
		    	d.getFields("text");
		    }

		    // reader can only be closed when there
		    // is no need to access the documents any more.
		    reader.close();
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return docsWithKeywords;
	}
	
	public boolean addDocument(String part, String partId, String content) {
		try {
		    addDoc(w, part, partId, content);
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private int getWordLength(String text) {
		String trimmed = text.trim();
		int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
		return words;
	}
	
	private void addDoc(IndexWriter w, String part, String partId, String text) throws IOException {
	    Document doc = new Document();
	    doc.add(new StringField("type", part, Field.Store.YES));
	    doc.add(new StringField("id", partId, Field.Store.YES));
	    FieldType type = new FieldType();
	    type.setIndexed(true);
	    type.setStored(true);
	    type.setStoreTermVectors(true);
	    type.setStoreTermVectorOffsets(true);
	    type.setStoreTermVectorPositions(true);
	    doc.add(new Field("text", text, type));
	    // use a string field for isbn because we don't want it tokenized
	    // doc.add(new StringField("isbn", isbn, Field.Store.YES));
	    w.addDocument(doc);
	}
	
	/////////////////////////////////////////////////////
	public static void main(String[] args) {
		DocumentKeywordSearch dks = new DocumentKeywordSearch();
		//dks.addDocument("article", "000", "(CNN) -- President Obama plans to speak Tuesday morning about the U.S. strategy against ISIS, following overnight strikes in Syria, a White House official said.  The president will speak at 10 a.m. ET at the White House before leaving for New York.  The United States and several Arab nations rained bombs on ISIS targets in Syria -- the first U.S. military offensive in the war-torn country and a forceful message to the militant group that the U.S. would not stand by idly while it carried out its rampage of terror.  Bahrain, Saudi Arabia, the United Arab Emirates, Jordan and Qatar all assisted with the operation, the U.S. military said.  The United States also took action -- on its own -- against another terrorist organization, the Khorasan Group, the military said. The group was formed by senior al Qaeda members based in Pakistan who traveled to Syria to direct operations, CNN national security analyst Peter Bergen has reported, citing British and U.S. officials.  The Khorasan Group was actively plotting against a U.S. homeland target and Western targets, a senior U.S. official said Tuesday.  What is the Khorasan Group?   Tomahawk missiles launched against ISIS Major problems in the fight against ISIS  The airstrikes against ISIS focused on the city of Raqqa, the declared capital of ISIS' self-proclaimed Islamic State. But other areas were hit as well.  The operation began with a flurry of Tomahawk missiles launched from the sea, followed by attacks from bomber and fighter aircraft, a senior U.S. military official told CNN.  The goal: Taking out ISIS' ability to command, train and resupply its militants.  In all, 200 pieces of ordnance were dropped by coalition members, and four dozen aircraft were used, a U.S. official told CNN. About 150 weapons used were precision-guided munitions. The United States fired 47 Tomahawk missiles, eight of them against Khorasan targets.  The number of casualties was not immediately clear. But U.S. Central Command said the strikes damaged or destroyed ISIS targets including fighters, training compounds, command-and-control facilities, a finance center and supply trucks.  CNN national security analyst Fran Townsend said attacks on infrastructure are just the beginning.  Eventually, she said, there will likely be a real campaign to go after leadership targets.  Celebration amid fear  For months, civilians in Raqqa have been living under the harsh rule of ISIS after militants took over their city, which had been one of Syria's most liberal cities. The group now controls much of their lives, imposing a strict brand of Sharia law and doling out barbaric punishments, such as beheadings and crucifixions.  Abo Ismail, an opposition activist inside Raqqa, said Tuesday that residents were elated to see the U.S. attacking ISIS targets there.  But at the same time, he said, ISIS has increased security in the city.  I would dance in the streets, but I am too afraid, Ismail said.   'There are five Arab nations involved' Military: U.S. ready to strike ISIS in Syria  A U.S. intelligence official said that while law enforcement is aware the airstrikes against ISIS in Syria could incite a response, there is no evidence to suggest any terrorist strike is in the works against the United States.  Arab countries on board  Perhaps the most surprising part of the strikes against ISIS was which countries joined the United States.  It's a remarkable diplomatic achievement, said CNN political commentator Peter Beinart. I don't think it was expected that there would be this much Arab support.  Former CIA counterterrorism official Philip Mudd said the inclusion of Sunni-majority countries fighting a radical Sunni militant group sends a strong message.  Prominent religious leaders have said ISIS is not representative of Islam, and now you have countries that are coming to the fore to attack it, he said.  Bahrain's state-run media said the country's air force carried out earlier this morning along with the air forces of the Gulf Cooperation Council (GCC), allied and friendly countries, air strikes against a number of selected targets of terrorist groups and organisations, and destroyed them, an authorised source at the Bahrain Defence Force (BDF)'s General Headquarters said.  British Defense Secretary Michael Fallon said, The UK supports the airstrikes launched by the U.S. and regional allies last night, the Defense Ministry said on Twitter. The government continues to discuss what further contribution the UK may make to international efforts to tackle the threat from ISIL, the ministry added, using another acronym that refers to the same group.  Syria informed  The Syrian regime was notified of the U.S. plan to take direct action against ISIS inside Syria, a senior State Department official told CNN on Tuesday.   U.S. begins airstrikes in Syria  The United States did not seek the regime's permission, nor did it coordinate with the Syrian government, the source said, adding that Secretary of State John Kerry did not send a letter to the regime.  On state-run media, Syria said its U.N. representative was informed Monday that the U.S. and some of its allies would target ISIS. Syria also said its foreign minister, Walid al-Moallem, received a letter from his American counterpart delivered by the Iraqi foreign minister which informed him that 'the U.S. will target the positions of the ISIS terrorist organization, some of which are in Syria.'  A 'punch in the nose'  Until now, ISIS has been able to take over cities and operate in Syria with near impunity. Now, it's coming under attack.  This is the punch in the nose to the bully that we talked about on the playground, former Delta Force officer James Reese said. ISIS is the bully, and we just punched him in the nose.  The United States has been conducting airstrikes against ISIS in Iraq, but never before against the militant group in Syria.  Syrian opposition: Finally  With the airstrikes, the United States enters a new level of engagement in the ongoing Syrian civil war.  For three years, Syrian rebels have been clamoring for Western military help as they battle regime forces and seek an end to four decades of al-Assad family rule. But the United States has resisted military action in Syria.  The difference now? ISIS, its bloody takeover of stretches of Iraq and Syria, and its threat to Americans.  I have made it clear that we will hunt down terrorists who threaten our country, wherever they are, President Barack Obama said in a September 10 speech.  That means I will not hesitate to take action against ISIL in Syria, as well as Iraq. This is a core principle of my presidency: If you threaten America, you will find no safe haven.  The Free Syria Foreign Mission said it was elated by the U.S. strikes.  Thank God. What a momentous day -- a day that we have been looking forward to for so, so long, the Syrian opposition group said. It's a big step forward, but we are nonetheless cleareyed that it will be a prolonged campaign to defeat ISIS.  Ironically, the U.S.-led offensive might please the Syrian dictator, Bashar al-Assad, as much as it does the Syrian opposition.  It helps him because we're taking out one of the threats to his regime, said retired Air Force intelligence officer Lt. Col. Rick Francona.  If we destroy ISIS, which we're committed to do ... that takes the biggest player off the table. And all he has to worry about is the smaller, less effective al Qaeda in Syria -- al-Nusra -- and the (rebel) Free Syrian Army, both of whom he has bested in the past couple of years.  Too little, too late?  But some say the United States waited too long to act against ISIS in Syria.  The airstrikes have come much too late in the case of Syria, where the IS militants have had over a year to entrench themselves within the region -- especially the province of Raqqa, said Natasha Underhill, an expert on Middle East terrorism at Nottingham Trent University.  She said ISIS is deeply entrenched in both Syria and Iraq, and it may take a lot more than airstrikes to make a dent in their campaign of creating an even larger caliphate across the Middle East.  Acting without Congress  The White House says it doesn't need any new authority to carry out such attacks. It says it's using an existing authorization to combat al Qaeda to expand its airstrike campaign.  But some lawmakers say it's Congress' role, not the President's, to declare war. They said they were open to holding a vote on military action against ISIS -- but not until after the midterm elections in November.  It was one thing to attack in Iraq, where you had a government that wanted us to, Beinart said. But Congress did not vote for U.S. airstrikes in Syria, and we don't have a government requesting us to do that.  Not over yet  Mudd said Tuesday's attacks were just the start.  This is not a definitive blow, the former CIA official said.  When this gets interesting to me ... is six months down the road, when a second-tier ISIS commander starts to create some sort of cell to recruit foreigners from Europe or the United States or Canada into Syria. Do we still have the will and capability, and the intelligence, to locate that person, or that group of people, and put lead on the target?  READ: U.S.-led airstrikes on ISIS: What you need to know");
		//dks.addDocument("article", "001", "Louis van Gaal has revealed that Ryan Giggs has been given the key responsibility of delivering the team talks at Manchester United on the club’s forthcoming opponents.  SHARE Giggs, Van Gaal’s assistant manager at United, is told to compile a detailed presentation based on the scouting reports of Marcel Bout.  Bout has worked for Van Gaal at previous clubs, including AZ Alkmaar and Bayern Munich, and has the role of chief opposition scout. He has watched United’s opponents on Sunday, Leicester City, for example, on three occasions already this season.  Giggs is handed Bout’s reports and then has to put together his own presentation based on them. The former United player then has to go to Van Gaal’s office to present his plan to the manager, who signs it off, before he delivers his analysis to the players in their team meeting.  Giggs gives a presentation to me,” Van Gaal has confirmed. I check it and he presents it to the players.”  Read more: Man United playing cautious game over possible return of Cristiano Ronaldo from Real Madrid  Read more: Manchester United can send Radamel Falcao back to Monaco if he injures his knee again   Van Gaal has partly devolved the role to Giggs because it is the way he grooms his assistants – Jose Mourinho was given the same task at Barcelona – but also because he has been impressed by the former player’s attention to detail.  It is a sign of Giggs’s value at United – where he was caretaker manager at the end of last season and has made no secret of the fact that he would eventually like to succeed Van Gaal – that he has been given the role as part of his duties.  After Giggs delivers his presentation Van Gaal said he then evaluates our training sessions in which we simulate our opponent”.  He added of Leicester: I have analysed them three times. I have four books about the games they have played already and the game they have lost already to a minor club [Shrewsbury in the League Cup].  I prepare all my matches very thoroughly. I know everything about the team, about individual players, about the substitutes. I know what the atmosphere will be in the stadium, how they will take the first kicks, everything.”  Van Gaal conceded that there is a danger of giving players too much information with his detailed approach.  That is the problem in the beginning,” he said.  I am training my players in the brain and that’s why it is so difficult. And of course, you are right, I give a lot of information and that is what they have to get to know, what they have to use of that information.  Because they have their own identity and I have said that already to different players; that they have to keep their own identity.  But information, every human being in this room gets information, and you have to pick up the right information and apply that.");
		dks.addDocument("ecom_prod_name", "000", "adidas supernova Glide 6 Boost Women's Neon Pink/Zero Metallic/Black");
		System.out.println(dks.searchKeywords(new String [] {"adidas","Boost"}));
//		System.out.println(dks.searchKeywords(new String [] {"president","komorowski"}));
//		System.out.println(dks.searchKeywords(new String [] {"syria"}));
//		System.out.println(dks.searchKeywords(new String [] {"ryan","giggs"}));
	}
	
}
