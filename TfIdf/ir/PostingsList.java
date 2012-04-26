package salvation;



import java.util.LinkedList;
import java.io.Serializable;

/**
 * <b> Class PostingsList</b> The basis for this class was implemented by Johan
 * Boye 2012 for course DD2476 information retrieval at KTH.
 */
public class PostingsList implements Serializable {

	private boolean VERBOSE = true;
	// Will contain list of all entries given a certain word
	private LinkedList<PostingsEntry> postings = new LinkedList<PostingsEntry>();
	private int DF; // Document frequency i.e length of postingslist

	/**
	 * Retrieve all Postingsentries.
	 * 
	 * @return LinkedList of PostingsEntry
	 */
	public LinkedList<PostingsEntry> getPostings() {
		return postings;
	}

	/**
	 * Retrieve entry from the ith position in the postingslist.
	 * 
	 * @param i
	 *            - index to retrieve from
	 * @return PostingsEntry - will return null if index out of bounds for
	 *         PostingsList.
	 */
	public PostingsEntry get(int i) {
		try {
			return postings.get(i);
		} catch (IndexOutOfBoundsException e) {
			if (VERBOSE) {
				System.err.println("Index out of bounds: index=" + i
						+ " postingList size = " + postings.size());
			}

			return null;
		}
	}

	/**
	 * Get the number of entries in postingslist
	 * 
	 * @return
	 */
	public int size() {
		return postings.size();
	}

	public void addPostingsEntry(int docID, double score, int offset) {

		addPostingsEntry(new PostingsEntry(docID, score, offset));
	}

	/**
	 * Add a given PostingsEntry to postingsList
	 * 
	 * @param entry
	 */
	public void addPostingsEntry(PostingsEntry entry) {

		DF++; // Adding a occurrence of this word, incrementing df
		boolean found = false;
		int lastIdx = -1, curr = postings.size();

		int docID = entry.docID;
		double score = entry.score;

		for (int i = 0; i < postings.size(); ++i) {
			PostingsEntry pe = postings.get(i);

			if (pe.docID == docID) { // never will be true in the case of merge.
				pe.addOffset(entry.offsets.getFirst()); // entry will only have
														// one offset in its
														// list
				found = true;
				break;
			}
			if ((lastIdx < docID || lastIdx == -1) && pe.docID > docID) {
				curr = i;
				break;
			}
			lastIdx = pe.docID;
		}

		if (!found) {

			PostingsEntry tmp = entry;
			postings.add(curr, tmp);
		}

	}

	/**
	 * Retrieve an entry given a docId
	 * 
	 * @param docId
	 * @return Entry given docID - null if it does not exist
	 */
	public PostingsEntry getEntryByDocID(int docId) {
		for (PostingsEntry pe : postings) {
			if (pe.docID == docId) {
				return pe;
			}
		}
		return null;
	}

	/**
	 * Add an existing entry at a specific index
	 * 
	 * @param entry
	 * @param index
	 */
	public void add(PostingsEntry entry, int index) {
		postings.add(index, entry);
	}

	/**
	 * Add a new entry with the given docID
	 * @param docID
	 */
	public void add(int docID){
    	postings.add(new PostingsEntry(docID));
    }
	
	/**
	 * Add an entry last in the postingslist
	 * 
	 * @param entry
	 */
	public void addLast(PostingsEntry entry) {
		postings.addLast(entry);
	}
	
	/**
	 * Add an entire list to postingslist.
	 * Note that this will overwrite any existing postings. 
	 * @param list
	 */
	public void addList(LinkedList<PostingsEntry> list){
	    	postings = list; 
	 }
}
