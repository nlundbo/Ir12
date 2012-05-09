package kth.ace.solr;
/**
 * SummationRank provides methods that rank words similarity to a query in attempt to do 
 * Automatic Context Expansion. The ranking for each word is done by summing 
 * relevance ranks for top relevant documents provided by the DWMatrix.
 * @author Sebastian Remnerud
 * @version 2012-05-09  
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;


public class SummationRank implements ACERanker {

	/**
	 * Default constructor
	 */
	public SummationRank(){}
	
	/**
	 * The method takes a Document Word matrix and ranks the words within according to their
	 * position. The lower the column index a words a appears the higher the score.
	 * The score for one word is calculated as the sum of scores for each row it appears in.
	 * @param DWMatrix 
	 * @return A word list sorted according to score.
	 */
	public LinkedList<Word> getRankedList(DWMatrix matrix) {
		LinkedList<LinkedList<Word>> dwMatrix = matrix.getMatrix();
		HashMap<String, Double> scoreBoard = new HashMap<String, Double>();

		for (LinkedList<Word> lw : dwMatrix) {
			int i = lw.size();
			for (Word w : lw) {
				if (scoreBoard.containsKey(w.getWord())) {
					scoreBoard.put(w.getWord(), scoreBoard.get(w.getWord()) + i);
				} else {
					scoreBoard.put(w.getWord(), new Double(i));
				}
				i--;
			}
		}

		LinkedList<Word> rankedList = new LinkedList<Word>();

		for (String key : scoreBoard.keySet()) {
			rankedList.add(new Word(key, scoreBoard.get(key)));
		}

		Collections.sort(rankedList);

		return rankedList;
	}
}
