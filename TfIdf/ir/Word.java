package ir;

public class Word implements Comparable<Word> {
	public String word;
	public Double tfidf;

	public Word() {
		word = "";
		tfidf = new Double(0.0);
	}

	public Word(String str, Double tfidf) {
		word = str;
		this.tfidf = tfidf;
	}

	public int compareTo(Word o) {
		return Double.compare(o.tfidf, tfidf);
	}

	@Override
	public String toString() {
		return word;
	}
}
