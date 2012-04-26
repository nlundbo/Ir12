package salvation;

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
	public String word; 
	public Double score; 

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
		return word;
	}
}
