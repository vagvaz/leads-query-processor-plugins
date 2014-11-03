package eu.leads.infext.proc.com.keyword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.leads.infext.proc.com.keyword.model.Relevance;

public class RelevanceScore {

	public Relevance getRelevanceForEntity(int articleLen, Integer []... keywordPositionsArray) {
		
		double relevanceCounter = 0.0;
		
		boolean countDistance = keywordPositionsArray.length == 1 ? false : true;
		
		System.out.print("relevance = ");
		
		for(Integer [] keywordPositions : keywordPositionsArray) {
			if(keywordPositions != null) {
				List<Integer> otherKeywordsPositions = new ArrayList<>();
				for(Integer [] keywordPositions2 : keywordPositionsArray)
					if(keywordPositions2 != keywordPositions && keywordPositions2 != null)
						otherKeywordsPositions.addAll(Arrays.asList(keywordPositions2));
				
				for(int keywordPos : keywordPositions) {
					double locationScore = locationScore(articleLen, keywordPos);
					double distanceScore = countDistance ? distanceScore(keywordPos, otherKeywordsPositions) : 1.0;
					relevanceCounter += locationScore*distanceScore;
					System.out.print(locationScore+"*"+distanceScore+" + ");
				}
			}
		}
		System.out.println();
		
		if(articleLen > 100)
			relevanceCounter *= 100.0/articleLen;
		
		return new Relevance("", relevanceCounter);
	}

	private double locationScore(double articleLen, double position) {
		return 1.0 - (0.6 * position / Math.max(articleLen,100.0));
	}
	
	private double distanceScore(int position, List<Integer> otherKeywordsPositions) {
		return 1.0 / Math.min( 0.5*(1+getSmallestDistance(position, otherKeywordsPositions)) , 10.0);
	}
	
	private int getSmallestDistance(int position, List<Integer> otherKeywordsPositions) {
		if(otherKeywordsPositions.isEmpty())
			return 20;
		Integer [] positions = otherKeywordsPositions.toArray(new Integer[otherKeywordsPositions.size()]);
		for(int i=0; i<positions.length; i++) {
			positions[i] -= position;
			positions[i] = Math.abs(positions[i]);
		}
		return Collections.min(Arrays.asList(positions));
	}
	
}
