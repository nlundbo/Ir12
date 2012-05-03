package ir;

import java.io.File;
import java.util.LinkedList;

/**
 * I make you indexes
 * See main
 * @author Mattias
 *
 */

public class MakingIndexes {


	Indexer indexer;
	LinkedList<String> indexFiles = new LinkedList<String>();
	LinkedList<String> dirNames = new LinkedList<String>();



	public MakingIndexes(String[] args) {
		long startTime = System.currentTimeMillis();
		decodeArgs(args);
		System.out.println("BÖrjar indexera");
		createIndexes();
		System.out.println("Indexering klart, börjar merga, split " + (System.currentTimeMillis()-startTime) + " ms!");
		mergeIndexes();
		System.out.println("Mergat klart, börjar spara, split "  + (System.currentTimeMillis()-startTime) + " ms!");
		saveToIndexes();
		System.out.println("Använd tid = " + (System.currentTimeMillis()-startTime) + " ms!");
		System.out.println("Klart!");
		
	}

	private void saveToIndexes() {
		indexer.index.cleanup();		
	}

	private void mergeIndexes() {
		File dokDir = new File( "index");

		
		if ( dokDir.canRead() ) {
			if ( dokDir.isDirectory() ) {
				
				File[] doc = dokDir.listFiles();
				LinkedList<String> fs = new LinkedList<String>();
				for (int i = 0; i < doc.length; i+=2) {
					fs.add(doc[i].toString().split("[\\.|\\\\]+")[1]);
				}
				
				indexer = new Indexer( fs );
				
				for (int i = 0; i < doc.length; i++) {
					doc[i].delete();
				}
			}
		}
		;
		


	}

	private void createIndexes() {
		
		for ( int i=0; i<dirNames.size(); i++ ) {
			indexer = new Indexer( indexFiles );		
			File dokDir = new File( dirNames.get( i ));
			System.out.println(dokDir.toString());
			indexer.processFiles( dokDir );
			System.out.println("saving "+dokDir.toString());
			saveToIndexes();
			System.out.println("Done saving");

		}		
	}




	private void decodeArgs( String[] args ) {
		int i=0, j=0;
		while ( i < args.length ) {
			if ( "-i".equals( args[i] )) {
				i++;

				if ( i < args.length ) {
					indexFiles.add( args[i++] );
				}
			} 
			else if ( "-d".equals( args[i] )) {
				i++;
				if ( i < args.length ) {
					dirNames.add( args[i++] );
				}
			}

		}
	}

	public static void main (String[] args){

		new MakingIndexes(args);
	}


}
