package ir;

import java.util.*;
import java.io.*;
import ir.SimpleTokenizer;
/*
	javac -d . StopWords.java
	java ir.StopWords
*/
public class StopWords{

	public  HashMap<String,Integer> counter = new HashMap<String,Integer>();
	public  static final int NUM_STOP_WORDS = 30;

	public StopWords(){}

	public  void readData(String dir){
		File dokDir = new File( dir);		
		processFiles(dokDir);
	}

    public  void processFiles( File f ) {
		if ( f.canRead() ) {
		    if ( f.isDirectory() ) {
				String[] fs = f.list();			
				if ( fs != null ) {
			    	for ( int i=0; i<fs.length; i++ ) {
						processFiles( new File( f, fs[i] ));
			    	}
				}
		    } else {
				System.err.println( "Counting words: " + f.getPath() );					
				try {			
				    Reader reader = new FileReader( f );
				    SimpleTokenizer tok = new SimpleTokenizer( reader );

					Integer tmp =0;
					String token="";
				    while ( tok.hasMoreTokens() ) {
				    	token = tok.nextToken();
				    	//token.intern();
				    	tmp = counter.get(token);
				    	if(tmp==null)tmp=new Integer(0);
				    	tmp++;
						counter.put(token,tmp);
				    }

				    reader.close();
				}
				catch ( IOException e ) {
				    e.printStackTrace();
				}
		    }
		}
    }

    public LinkedList<Word> getStopWords(){
    	
    	Set<String> ks = counter.keySet();
		LinkedList<Word> ll = new LinkedList<Word>();

		for(String s : ks)
			ll.add( new Word(s, counter.get(s)) );

		Collections.sort(ll);
		return ll;
    }


	public static final void main(String [] args){
		StopWords s = new StopWords();
		s.readData("../files/1000/");
		s.readData("../files/2000/");
		s.readData("../files/3000/");
		s.readData("../files/4000/");
		s.readData("../files/5000/");
		s.readData("../files/6000/");
		s.readData("../files/7000/");
		s.readData("../files/8000/");
		s.readData("../files/9000/");
		s.readData("../files/10000/");
		LinkedList<Word> ll = s.getStopWords();
		for(int i =0; i< NUM_STOP_WORDS; ++i){
			System.out.println(ll.pop());
		}

	}


	private class Word implements Comparable<Word> {
		public String word;
		public Integer n;

		public Word() {
			word = "";
			n = new Integer(1);
		}

		public Word(String str, Integer n) {
			word = str;
			this.n = n;
		}

		public int compareTo(Word o) {
			return o.n.compareTo(n); //reversed.
		}
		@Override
		public String toString(){
			return "[ " + word + ", " + n + "]";
		}
	}
}