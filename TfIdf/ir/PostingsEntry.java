/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public LinkedList<Integer> wordPos;
    public double score;
       

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
    	return Double.compare( other.score, score );
    }
    // Note, no creation of wordPos, this is intended.
    public PostingsEntry(int docID){
    	this.docID = docID;    	
    	this.score = 0;
    }
   
   
    public PostingsEntry(int docID, double score,int offset){
    	this.docID = docID;
    	wordPos = new LinkedList<Integer>();
    	this.score = score;
    	wordPos.add(offset);
    }
    
    public void addWordPos(int offset){
    	wordPos.addLast(offset);
    }
    
    
    public boolean equals(Object aThat){
    	if ( this == aThat ) return true;
    	if ( !(aThat instanceof PostingsEntry) ) return false;
    	PostingsEntry that = (PostingsEntry)aThat;
    	return this.docID == that.docID;    	    
    }
}

    
