
package salvation;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * <b> Class PostingsEntry</b>
 * Based on implementation made by Johan Boye 2012 for course DD2476 information retrieval at KTH.
 * Holds information on a certain entry such as docID , offsets, tf-idf score.
 */
public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

	public int docID; // Document ID
	public double score; // Tf-idf score
	public LinkedList<Integer> offsets; // List of offsets
	public int TF; // Term frequency of the given entry

	/**
	 * Default constructor
	 * 
	 * @param docID
	 * @param score
	 */
	public PostingsEntry(int docID, double score) {
		this.docID = docID;
		this.score = score;
		offsets = new LinkedList<Integer>();
	}
	
	public PostingsEntry(int docID, double score,int offset){
    	this.docID = docID;
    	offsets = new LinkedList<Integer>();
    	this.score = score;
    	addOffset(offset);
    }
	
    
    /**
     * Construct a minimal instance of entry. 
     * Note that no creation of the an offset i made.
     * @param docID
     */
	public PostingsEntry(int docID){
    	this.docID = docID;    	
    	this.score = 0;
    }
   

	/**
	 * Compare two Postingsentries based on their score.
	 */
	public int compareTo(PostingsEntry other) {
		return Double.compare(other.score, score);
	}

	public int getDocID() {
		return docID;
	}

	public void setDocID(int docID) {
		this.docID = docID;
	}

	/**
	 * Retrieve the score of the entry.
	 * @return
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * Set a score for the given entry.
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Retrieve all offsets for this entry.
	 * @return
	 */
	public LinkedList<Integer> getOffsets() {
		return offsets;
	}

	/**
	 * Initalize the offsetlist with a new object.
	 */
	public void newOffsetList()
	{
		offsets = new LinkedList<Integer>();
	}
	
	/**
	 * Will add an offset to the offset last in list assuming that offsets
	 * are added in an increasing fashion and will otherwise become unsorted.
	 * Note that if offsetlist is not initalized this will cast nullpointer.
	 * TODO Catch handle exception
	 * @param offset
	 */
	public void addOffset(Integer offset) {
		offsets.addLast(offset);
		TF++;
	}
	
	

	public int getTF() {
		return TF;
	}

}
