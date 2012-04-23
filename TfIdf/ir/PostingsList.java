/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {    
	
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
    private int df; 
        
    /**  Number of postings in this list  */
    public int size() {
    	return list.size();
    }
    public int getDf(){
    	return df;
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
    	return list.get( i );
    }
    
    public void addList(LinkedList<PostingsEntry> list){
    	this.list = list; 
    }
    
    public PostingsEntry getDocIdList(int docId){
    	for(PostingsEntry pe : list){
    		if(pe.docID == docId){
    			return pe;
    		}
    	}
    	return null;
    }
    
    public void add(int docID){
    	list.add(new PostingsEntry(docID));
    }
    
    public LinkedList<PostingsEntry> getList(){
    		return list;
    }
    
    public void addPostingsEntry(int docID, double score , int offset ){
    	
    	addPostingsEntry(new PostingsEntry(docID,score, offset));
    }
    
    
    public void addPostingsEntry(PostingsEntry toAdd){
    	
    	df++; // Adding a occurence of this word, incrementing df
    	boolean found = false;
    	int lastIdx = -1, curr = list.size();
    	
    	int docID = toAdd.docID;
    	double score = toAdd.score;
    	
		for(int i =0 ; i< list.size(); ++i){
			PostingsEntry pe = list.get(i);
    		
    		if(pe.docID == docID){  // never will be true in the case of merge.    			
    			pe.addWordPos(toAdd.wordPos.getFirst()); // toAdd will only have one offset in its list
    			found = true;
    			break;
    		}
    		if((lastIdx < docID || lastIdx == -1 )  && pe.docID > docID){
    			curr = i;
    			break;
    		}
    		lastIdx = pe.docID;
    	}
    	
    	if(!found){
    		
    		PostingsEntry tmp = toAdd;
    		list.add(curr,tmp);
    	}
    	    	
    }

    
    public void mergeList(PostingsList pl){
    	for(PostingsEntry pe : pl.getList()){
    		addPostingsEntry(pe);
    	}
    }
    @Override
    public String toString(){
    	return list.toString();
    }
    
}
	

			   
