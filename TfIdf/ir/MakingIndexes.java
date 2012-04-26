package ir;

import java.io.File;
import java.util.LinkedList;

public class MakingIndexes {


	Indexer indexer;
	LinkedList<String> indexFiles = new LinkedList<String>();
	LinkedList<String> dirNames = new LinkedList<String>();



	public MakingIndexes(String[] args) {
		
		decodeArgs(args);
		System.out.println("BÖrjar indexera");
		createIndexes();
		System.out.println("Indexering klart, börjar merga");
		//mergeIndexes();
		System.out.println("Klart!");
	}

	private void saveToIndexes() {
		indexer.index.cleanup();		
	}

	private void mergeIndexes() {


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
