/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   
 */

package ir;

import java.util.ArrayList;
import java.util.LinkedList;

import java.util.*;
import java.io.*;

/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

	@Override
	public void insert(String token, int docID, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PostingsList getPostings(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<String> search(LinkedList<String> searchterms,
			int queryType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}


}
