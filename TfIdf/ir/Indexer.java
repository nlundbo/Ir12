/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  


package ir;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

	/** The index to be built up by this indexer. */
	public Index index;
	public static HashSet<String> stopWord = new HashSet<String>();

	/** The next docID to be generated. */
	private int lastDocID = 0;
	private String stopWordDir="Stopwords.txt";


	/* ----------------------------------------------- */


	/** Generates a new document identifier as an integer. */
	private int generateDocID() {
		return lastDocID++;
	}

	/** Generates a new document identifier based on the file name. */
	private int generateDocID( String s ) {
		return s.hashCode();
	}


	/* ----------------------------------------------- */


	/**
	 *  Initializes the index as a HashedIndex.
	 */
	public Indexer() {
		buildStopWord();	

		index = new HashedIndex();
	}

	/** 
	 *  Initializes the index as a MegaIndex.
	 */
	public Indexer( LinkedList<String> indexfiles ) {
		buildStopWord();	

		index = new MegaIndex( indexfiles );
	}


	/* ----------------------------------------------- */


	/**
	 *  Tokenizes and indexes the file @code{f}. If @code{f} is a directory,
	 *  all its files and subdirectories are recursively processed.
	 */
	public void processFiles( File f ) {
		// do not try to index fs that cannot be read

		
		if ( f.canRead() ) {
			if ( f.isDirectory() ) {
				String[] fs = f.list();
				// an IO error could occur
				if ( fs != null ) {
					for ( int i=0; i<fs.length; i++ ) {
						processFiles( new File( f, fs[i] ));
					}
				}
			} else {
				System.err.println( "Indexing " + f.getPath() );
				// First register the document and get a docID
				int docID;
				if ( index instanceof HashedIndex ) {
					// For HashedIndex, use integers.
					docID = generateDocID();
				}
				else {
					// For MegaIndex, use hash codes based on file names.
					try {
						docID = generateDocID( f.getCanonicalPath() );
					}
					catch( IOException e ) {
						docID = generateDocID( f.getPath() );
					}
				}
				index.docIDs.put( "" + docID, f.getPath() );
				try {

					Reader reader = new FileReader( f );

					SimpleTokenizer tok = new SimpleTokenizer( reader );
					int offset = 0;
					int i =0;
					String newWord;
					while ( tok.hasMoreTokens() ) {
						newWord = tok.nextToken();
						if(!stopWord.contains(newWord))
							insertIntoIndex( docID, newWord, offset++ );
						else{
							i++;
						}
					}
					index.docLengths.put( "" + docID, offset );
					reader.close();
				}
				catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}


	private void buildStopWord() {
		try {
			FileInputStream fstream = new FileInputStream(stopWordDir);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				stopWord.add(strLine);
			}
			in.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());

		}


	}

	/* ----------------------------------------------- */




	/* ----------------------------------------------- */


	/**
	 *  Indexes one token.
	 */
	public void insertIntoIndex( int docID, String token, int offset ) {
		index.insert( token, docID, offset );
	}
}

