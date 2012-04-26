package ir;
import java.util.*;
import java.io.*;
import ir.SimpleTokenizer;

public class StopWords{

	public static HashMap<String,Integer> counter = new HashMap<String,Integer>();

	
	public StopWords(){}

	public static void readData(String dir){
		File dokDir = new File( dir);		
		processFiles(dokDir);
	}

    public static void processFiles( File f ) {
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
				    
		
				    while ( tok.hasMoreTokens() ) {
						//.add(tok.nextToken());
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
		readData("../files/1000/");

	}
}