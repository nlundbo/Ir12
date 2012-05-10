package kth.ace.Random;

import java.util.*;
import java.io.*;

import kth.ace.solr.Word;

public class RandomIndexing {

	private static boolean PAGE = false;
	private static boolean DEBUG = false;
	private final int CONTEXT = 2;
	
	private static final int NUM_ANS = 20;
	int parsedFiles = 0;



	private HashSet<String> words = new HashSet<String>();

	private HashMap<String,Integer> index = new HashMap<String,Integer>();
	private ArrayList< WordVector> wordVectors = new ArrayList<WordVector>();

	private ArrayList< WordVector> contextVectors = new ArrayList<WordVector>();

	private Random rnd = new Random();

	public RandomIndexing(){		
	}



	private void parseTokenizedDocument(ArrayList<String> tokens){
		
		for(int i =0; i< tokens.size(); ++i){			
			String tok = tokens.get(i);			

			if( index.get(tok) == null){
				WordVector a = new WordVector(rnd);
				WordVector b = new WordVector(); 
				int idx = wordVectors.size();
				wordVectors.add(a);
				contextVectors.add(b);				
				index.put(tok,idx);
			}		
		}
		calculateContextVectors(tokens);

	}

	private void calculateContextVectors(ArrayList<String> tokens){
		int start,stop;
		for(int i =0; i< tokens.size(); ++i){									
			start = (i - CONTEXT < 0)? 0:(i-CONTEXT);
			stop  = (i + CONTEXT > tokens.size()-1)? (tokens.size()-1): i + CONTEXT;		

			WordVector context = contextVectors.get( index.get( tokens.get(i) ));	
			
			for(int j=start; j <= stop; ++j){
				if(j != i){					
					context.addSparse( wordVectors.get( index.get( tokens.get(j) )) );
				}
			}

		}
	}


	public LinkedList<Word> getSynonyms(String term) {
		
		Set<String> keys = index.keySet();
		int idx;

		if( index.get(term) == null){
			return null;
		}else{
			idx = index.get(term);
		}

		System.out.println("index " + idx);

		WordVector wv = contextVectors.get(idx);
		System.out.println(wv);
		LinkedList<Word> ll = new LinkedList<Word>();

		for(String s : keys){
			WordVector t = contextVectors.get( index.get(s));
			double d = wv.getDistance(t);
			ll.add( new Word(s,d));
		}
		Collections.sort(ll, Word.getReverseComparator());		
		return ll;
	}


	public void readData(String dir){
		File dokDir = new File( dir);		
		processFiles(dokDir);
	}

    public void processFiles( File f ) {
    	parsedFiles++;

    	if(parsedFiles%100 == 0){
    		//System.gc();
    	}
	
		if ( f.canRead() ) {
		    if ( f.isDirectory() ) {
				String[] fs = f.list();			
				if ( fs != null ) {
			    	for ( int i=0; i<fs.length; i++ ) {
						processFiles( new File( f, fs[i] ));
			    	}
				}
		    } else {
				//System.err.println( "Indexing " + f.getPath() );					
				try {			
				    Reader reader = new FileReader( f );
				    SimpleTokenizer tok = new SimpleTokenizer( reader );
				    
				    ArrayList<String> strs = new ArrayList<String>();;

				    while ( tok.hasMoreTokens() ) {
						strs.add(tok.nextToken());
				    }
				    parseTokenizedDocument(strs);
				    reader.close();
				}
				catch ( IOException e ) {
				    e.printStackTrace();
				}
		    }
		}
    }



	public static final void main(String [] args){
	}


}
