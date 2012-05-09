package kth.ace.solr;

/**
 * Simple class to keep a word with a corresponding score. 
 * The class overrides the compareTo method in order to compare two
 * words based on score.
 * 
 * @author Niklas Lundborg 
 * @version 2012-04-20
 *
 */
public class Word implements Comparable<Word> {
	private String word; 
	private Double score; 

	/**
	 * Default Constructor
	 */
	public Word() {
		word = "";
		score = new Double(0.0);
	}
	
	/**
	 * @param str
	 * @param score
	 */
	public Word(String str, Double score) {
		word = str;
		this.score = score;
	}

	public int compareTo(Word o) {
		return Double.compare(o.score, score);
	}

	@Override
	public String toString() {
		return word+ ": " +score;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}
}
