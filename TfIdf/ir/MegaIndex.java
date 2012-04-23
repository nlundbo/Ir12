/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import com.larvalabs.megamap.MegaMapManager;
import com.larvalabs.megamap.MegaMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;


public class MegaIndex implements Index {

    /** 
     *  The index as a hash map that can also extend to secondary 
     *	memory if necessary. 
     */
    private MegaMap index;


    /** 
     *  The MegaMapManager is the user's entry point for creating and
     *  saving MegaMaps on disk.
     */
    private MegaMapManager manager;


    /** The directory where to place index files on disk. */
    private static final String path = "/var/tmp/index";


    /**
     *  Create a new index and invent a name for it.
     */
    public MegaIndex() {
	try {
	    manager = MegaMapManager.getMegaMapManager();
	    index = manager.createMegaMap( generateFilename(), path, true, false );
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     *  Create a MegaIndex, possibly from a list of smaller
     *  indexes.
     */
    public MegaIndex( LinkedList<String> indexfiles ) {
	try {
	    manager = MegaMapManager.getMegaMapManager();
	    if ( indexfiles.size() == 0 ) {
		// No index file names specified. Construct a new index and
		// invent a name for it.
		index = manager.createMegaMap( generateFilename(), path, true, false );
		
	    }
	    else if ( indexfiles.size() == 1 ) {
		// Read the specified index from file
		index = manager.createMegaMap( indexfiles.get(0), path, true, false );
		HashMap<String,String> m = (HashMap<String,String>)index.get( "..docIDs" );
		if ( m == null ) {
		    System.err.println( "Couldn't retrieve the associations between docIDs and document names" );
		}
		else {
		    docIDs.putAll( m );
		}
	    }
	    else {
		// Merge the specified index files into a large index.
		MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
		for ( int k=0; k<indexfiles.size(); k++ ) {
		    System.err.println( indexfiles.get(k) );
		    indexesToBeMerged[k] = manager.createMegaMap( indexfiles.get(k), path, true, false );
		}
		index = merge( indexesToBeMerged );
		for ( int k=0; k<indexfiles.size(); k++ ) {
		    manager.removeMegaMap( indexfiles.get(k) );
		}
	    }
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }


    /**
     *  Generates unique names for index files
     */
    String generateFilename() {
	String s = "index_" + Math.abs((new java.util.Date()).hashCode());
	System.err.println( s );
	return s;
    }


    /**
     *   It is ABSOLUTELY ESSENTIAL to run this method before terminating 
     *   the JVM, otherwise the index files might become corrupted.
     */
    public void cleanup() {
	// Save the docID-filename association list in the MegaMap as well
	index.put( "..docIDs", docIDs );
	// Shutdown the MegaMap thread gracefully
	manager.shutdown();
    }



    /**
     *  Returns the dictionary (the set of terms in the index)
     *  as a HashSet.
     */
    public Set getDictionary() {
    	return index.getKeys();
    }


    /**
     *  Merges several indexes into one.
     */
    MegaMap merge( MegaMap[] indexes ) {
	try {
	    MegaMap res = manager.createMegaMap( generateFilename(), path, true, false );
	    
	    for(int i = 0; i <indexes.length; ++i ){
			HashMap<String,String> m = (HashMap<String,String>)indexes[i].get( "..docIDs" );
			if ( m == null ) {
			    System.err.println( "Couldn't retrieve the associations between docIDs and document names" );
			}
			else {
			    docIDs.putAll( m );
			}

	    	Set<String> words = (Set<String>) indexes[i].getKeys();
	    	for(String s : words){	    		
	    		if(res.hasKey(s)){
	    			PostingsList pl0 = getPostings(res,s);
	    			PostingsList pli = getPostings(indexes[i],s);
	    			pl0.mergeList(pli);	    	
	    			res.put(s,pl0);
	    		}else{	    			
	    			res.put(s, getPostings(indexes[i],s));
	    		}    	    
	    	}
	    
	    	 
	    }
	    
	    return res;
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
    	int score = 0; //todo
    	PostingsList pl;
    	if(index.hasKey(token)){
    		pl = getPostings(token);    			
    	}else{    		
			pl = new PostingsList();
			index.put(token,pl);    		
    	}
		
		pl.addPostingsEntry(docID,score,offset);    	
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	try {
	    return (PostingsList)index.get( token );
	}
	catch( Exception e ) {
	    return new PostingsList();
	}
    }
    public static PostingsList getPostings(MegaMap index, String token){
    	try {
    	    return (PostingsList)index.get( token );
    	}
    	catch( Exception e ) {
    	    return new PostingsList();
    	}
    }

    /**
     *  Searches the index for postings matching the query in @code{searchterms}.
     */
    public PostingsList search( LinkedList<String> searchTerms, int queryType ) {
    
    	if(searchTerms.size() == 1 ){
    		return getPostings(searchTerms.getFirst());
    	}else if(queryType == Index.INTERSECTION_QUERY){ // many words
    		return startIntersection(searchTerms);    		
    	}else if(queryType == Index.PHRASE_QUERY){ // phrase search
    		return startPhraseSearch(searchTerms);
    	}
    		
    	
    	return null;	
    }
    
    // ##############################################################
    // OUR PRIVATE METHODS ctrl c +v from hashed
    // ##############################################################
    
    private PostingsList startPhraseSearch(LinkedList<String> searchTerms){
		PostingsList ids = startIntersection(searchTerms);
		PostingsList ans = new PostingsList();
		
		for(int i = 0; i < ids.size(); ++i){ // for each doc
			ArrayList<LinkedList<Integer>> words = new ArrayList<LinkedList<Integer>>();	
			int docId = ids.get(i).docID;
			
			// Initialize words
			for(String s : searchTerms){
				PostingsList tmp = getPostings(s);
				for(PostingsEntry pe : tmp.getList()){
					if(pe.docID == docId){
						words.add(pe.wordPos);
					}
				}				 
			}
			
			for(Integer j : words.get(0) ){
				if(phraseSearch(j+1, words,1)){
					ans.add(ids.get(i).docID);
				}
			}
		}
		return ans;
    	
    }
    
    private boolean phraseSearch(int offset, ArrayList<LinkedList<Integer>> wordPos, int idx){
    	if(idx == wordPos.size() )return true;    	
    	return wordPos.get(idx).contains(offset) && phraseSearch(offset+1,wordPos,idx+1);    	
    }
    
    
    
    
    
    private PostingsList startIntersection(LinkedList<String> searchterms){
    	PostingsList ans = new PostingsList();
		ans = getPostings(searchterms.getFirst());
		PostingsList b = null;
		for(int i = 1; i< searchterms.size(); i++){				
			b = getPostings(searchterms.get(i));
			ans = intersection(ans,b);    			
		}
		return ans;	
    }
    
    private PostingsList intersection(PostingsList p1, PostingsList p2){
    	
    	PostingsList ans = new PostingsList();
    	int i=0,j=0;
    	PostingsEntry docA,docB;
    	while(i != p1.size() && j != p2.size()){
    		docA = p1.get(i);
    		docB = p2.get(j);
    		if(docA.docID == docB.docID){    			
    			ans.add( docA.docID);
    			i++;
    			j++;
    		}else if( docA.docID < docB.docID){
    			i++;
    		}else{
    			j++;
    			
    		}   		
    	}
    	return ans;
    }
    
    
    
    
    

}










 



