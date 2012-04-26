package ir;

import java.util.LinkedList;

import com.larvalabs.megamap.MegaMapException;
import com.larvalabs.megamap.MegaMapManager;
import com.larvalabs.megamap.MegaMap;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

/**
 * <b> Class MegaIndex</b> The basis for this class was implemented by Johan
 * Boye 2012 for course DD2476 information retrieval at KTH. The class uses
 * megamap to store large indices to file.
 * @author Niklas Lundborg, Mattias Knutsson, Sebastian Remnerud, Meidi Tönisson
 */
public class MegaIndex implements Index {
	private static final boolean DEBUG = false; // Set to true for Debug prints

	// The index as a hashmap that can also extend to secondary memory if
	// necessary.
	private MegaMap index;

	// The MegaMapManager is the user's entry point for creating and saving
	// MegaMaps on disk.
	private MegaMapManager manager;

	// The directory where to place index files on disk.
	private static final String path = "./index";

	private final int D = 1; // number of top ranking documents to retrieve
	private final int K = 5; // number of top ranking words to retrieve
	private final int NOR = 5; //Number of results to return to GUI
	
	/**
	 * Create a new index and invent a name for it.
	 */
	public MegaIndex() {
		try {
			manager = MegaMapManager.getMegaMapManager();
			index = manager
					.createMegaMap(generateFilename(), path, true, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a MegaIndex from a list of smaller indexes.
	 */
	public MegaIndex(LinkedList<String> indexfiles) {
		try {
			manager = MegaMapManager.getMegaMapManager();

			// If the list is empty create new index
			if (indexfiles.size() == 0) {
				index = manager.createMegaMap(generateFilename(), path, true,
						false);

				// If there is only one index file read in the index
			} else if (indexfiles.size() == 1) {

				// Read index from file
				index = manager.createMegaMap(indexfiles.get(0), path, true,
						false);

				// Retrieve DocLengths
				HashMap<String, Integer> dl = (HashMap<String,Integer>) index.get("..docLengths");
				if (dl == null) {
					System.err
							.println("Couldn't retrieve docLengths");
				} else {
					docLengths.putAll(dl);
				}
				
				// Retrieve docIDs
				HashMap<String, String> m = (HashMap<String, String>) index
						.get("..docIDs");

				if (m == null) {
					System.err
							.println("Couldn't retrieve the associations between docIDs and document names");
				} else {
					docIDs.putAll(m);
				}

				// If there is more than one index file we have merge the
				// indices
			} else {

				// Read in all the indices
				MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
				for (int k = 0; k < indexfiles.size(); k++) {
					System.err.println(indexfiles.get(k));
					indexesToBeMerged[k] = manager.createMegaMap(
							indexfiles.get(k), path, true, false);
				}

				// Merge the specified index files into a large index
				index = merge(indexesToBeMerged);
				for (int k = 0; k < indexfiles.size(); k++) {

					// Retrieve docLengths
					docLengths.putAll((HashMap<String, Integer>) indexesToBeMerged[k]
							.get("..docLengths"));
					// Inserted to get filenames instead of docIDs
					docIDs.putAll((HashMap<String, String>) indexesToBeMerged[k]
							.get("..docIDs"));
					manager.removeMegaMap(indexfiles.get(k));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates unique names for index files
	 */
	private String generateFilename() {
		String s = "index_" + Math.abs((new java.util.Date()).hashCode());
		System.err.println(s);
		return s;
	}

	/**
	 * Merge several indices together.
	 * 
	 * @param indexes
	 *            to be merged as MegaMaps
	 * @return MegaMap containing the merged index
	 */
	MegaMap merge(MegaMap[] indexes) {
		try {

			MegaMap mergedIndex = manager.createMegaMap(generateFilename(),
					path, true, false);

			// Copy the first index to mergedIndex
			copyIndex(indexes[0], mergedIndex);

			// Merge remaining indexes with res
			for (int i = 1; i < indexes.length; i++) {
				merge(mergedIndex, indexes[i]);
			}

			return mergedIndex;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Copy all the postingsList from one MegaMap to another.
	 * 
	 * @param MegaMap
	 *            from
	 * @param MegaMap
	 *            to - has to be initalized.
	 */
	void copyIndex(MegaMap from, MegaMap to) {
		for (Object oKey : from.getKeys()) // object key
		{
			String key = (String) oKey;

			if (key.equals("..docIDs") || key.equals("..docLengths")) {
				continue;
			}
			try {
				to.put(key, (PostingsList) (from.get(key)));
			} catch (MegaMapException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Merge index2 into index1 including
	 * 
	 * @param index1
	 * @param index2
	 * @throws MegaMapException
	 */
	@SuppressWarnings("unchecked")
	void merge(MegaMap index1, MegaMap index2) {
		try {

			// Iterate over index2
			for (Object oKey : index2.getKeys()) {
				String key = (String) oKey; // Convert key to String

				// Handle special element (docID -filename mapping and
				// docLengths)
				if (key.equals("..docIDs") || key.equals("..docLengths")) {
					continue;
				}

				// If term already exists in index1
				if (index1.hasKey(key)) {
					PostingsList pl1 = (PostingsList) index1.get(key);
					PostingsList pl2 = (PostingsList) index2.get(key);

					int i1 = 0; // index for pl1
					int i2 = 0; // index for pl2

					while (i2 < pl2.size()) {
						while (i1 < pl1.size() && i2 < pl2.size()) {
							int docID1 = pl1.get(i1).docID;
							int docID2 = pl2.get(i2).docID;

							if (docID1 == docID2) {
								i1++;
								i2++;
							} else if (docID1 > docID2) {
								pl1.add(pl2.get(i2), i1);
								i2++;
								i1++;
							} else if (docID1 < docID2) {
								i1++;
							}
						}
						if (i2 < pl2.size())
							pl1.addLast(pl2.get(i2));
						i2++;
					}

				} else {
					index1.put(key, (PostingsList) index2.get(key));
				}

			}

		} catch (MegaMapException e) {
			System.err.println("Could not cast to HashMap");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the postings for a specific term, or null if the term is not in
	 * the index.
	 */
	public PostingsList getPostings(String token) {
		try {
			return (PostingsList) index.get(token);
		} catch (Exception e) {
			return new PostingsList();
		}
	}

	/**
	 * Inserts this token in the hashtable.
	 */
	public void insert(String token, int docID, int offset) {
		int score = 0;
		PostingsList pl;
		if (index.hasKey(token)) {
			pl = getPostings(token);
		} else {
			pl = new PostingsList();
			index.put(token, pl);
		}

		pl.addPostingsEntry(docID, score, offset);
	}
	/**
	 * Main search method will return a list of that is considered to be in 
	 * the same context as query word.
	 */
	public LinkedList<String> search(LinkedList<String> searchTerms,
			int queryType) {
		boolean iRank = false; //Intersectionrank
		
		LinkedList<String> returnList = new LinkedList<String>();
		if (queryType == Index.RANKED_QUERY) {

		
			PostingsList pll = rankedSearch(searchTerms);// evalRankQuery(searchTerms);

			for (PostingsEntry pl : pll.getPostings()) {
				System.out.println("result: " + docIDs.get("" + pl.docID));
			}

			LinkedList<LinkedList<Word>> DKMatrix = new LinkedList<LinkedList<Word>>();

			for (int i = 0; i < D && i < pll.getPostings().size(); i++) {
				PostingsEntry pl = pll.getPostings().get(i);
				int docID = pl.docID;
				String file = docIDs.get("" + docID);

				// Get corresponding file given the docID TODO do it for every
				// docID
				System.err.println(file);
				// TODO Ignore documents shorter than 5 words
				DKMatrix.add(getTopWords(docID, file));
			}
			LinkedList<Word> ll = new LinkedList<Word>();
			if (!iRank) {
				ll = summationRank(DKMatrix);
			} else {
				ll = intersectionRank(DKMatrix, searchTerms.getFirst());
			}
			int i = 0;
			for (Word w : ll) {
				if(i > NOR){break;}
				returnList.add("S: " + w.word + " V: " + w.score);
				i++;
			}

			return returnList;
		}

		return null;

	}

	/**
	 * Get hashmap containing word/tfidf pairs for a given document.
	 * 
	 * @param docID
	 * @param file
	 * @return Hashmap containing word/tfidf pairs - null if error
	 */
	private LinkedList<Word> getTopWords(int docID, String file) {
		HashMap<String, Double> hm = null;

		try {
			System.err.println(file);
			FileReader reader = new FileReader(new File(file));
			SimpleTokenizer tok = new SimpleTokenizer(reader);

			// Hashmap containing word/score pairs
			hm = new HashMap<String, Double>();

			while (tok.hasMoreTokens()) {
				String str = tok.nextToken();
				if(!Indexer.stopWord.contains(str)){
				hm.put(str, new Double(tfIdf(str, docID)));
				}
			}
			
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

		LinkedList<Word> ll = new LinkedList<Word>();
		for (String key : hm.keySet()) {
			ll.add(new Word(key, hm.get(key)));
		}

		Collections.sort(ll);

		LinkedList<Word> returnlist = new LinkedList<Word>();

		for (int i = 0; i < Math.min(K, ll.size()); i++) {
			returnlist.addLast(ll.pop());
		}

		return returnlist;
	}

	/**
	 * Take a doc/word matrix and rank them according to the sum of ranking
	 * positions. Example. The matrix consists of <i>k</i> columns and the
	 * <i>r</i> rows. The word W occurs in columns 1, 5 and 4 in different rows.
	 * most
	 * 
	 * @return
	 */
	public LinkedList<Word> summationRank(
			LinkedList<LinkedList<Word>> DKMatrix) {
		HashMap<String, Double> scoreBoard = new HashMap<String, Double>();

		for (LinkedList<Word> lw : DKMatrix) {
			int i = lw.size();
			for (Word w : lw) {
				if (scoreBoard.containsKey(w.word)) {
					scoreBoard.put(w.word, scoreBoard.get(w.word) + i);
				} else {
					scoreBoard.put(w.word, new Double(i));
				}
				i--;
			}
		}

		LinkedList<Word> rankedList = new LinkedList<Word>();

		for (String key : scoreBoard.keySet()) {
			rankedList.add(new Word(key, scoreBoard.get(key)));
		}
		
		Collections.sort(rankedList);
		
		return rankedList;
	}

	/**
	 * The intersectionrank takes a matrix consisting of the K topranked words
	 * from the D topranked documents. From this matrix all unique words are
	 * retrieved. For each unique word an intersection is made between the word
	 * and the query word. The co-occurence score is calculated as the number
	 * documents retrieved from that intersection.
	 * 
	 * @param DKMatrix
	 *            - Doc/Word matrix
	 * @param query
	 * @return LinkedList of Words containing a co-occurence score.
	 */
	public LinkedList<Word> intersectionRank(
			LinkedList<LinkedList<Word>> DKMatrix, String query) {
		boolean onlyIntersection = false; // otherwise, multiply by tfidf
		HashMap<String, Double> wordScores = new HashMap<String, Double>();

		for (LinkedList<Word> ll : DKMatrix) {
			for (Word w : ll) {
				if (onlyIntersection && getPostings(query) != null
						&& getPostings(w.toString()) != null) {
					wordScores.put(
							w.toString(),
							Math.log10((double) intersection(
									getPostings(query),
									getPostings(w.toString())).size()));
					System.out.println("Intersections for "
							+ w.toString()
							+ " is "
							+ intersection(getPostings(query),
									getPostings(w.toString())).size());
				} else if (getPostings(query) != null
						&& getPostings(w.toString()) != null) {
					wordScores.put(
							w.toString(),
							w.score
									+ intersection(getPostings(query),
											getPostings(w.toString())).size());
					System.out.println("Score for "
							+ w.toString()
							+ " is "
							+ w.score
							+ intersection(getPostings(query),
									getPostings(w.toString())).size()
							+ ": "
							+ intersection(getPostings(query),
									getPostings(w.toString())).size()
							+ " intersections");
				} else if (getPostings(query) == null
						&& getPostings(w.toString()) == null) {
					System.err.println("Postingslist for " + query + " and "
							+ w.toString() + " is null");
				} else if (getPostings(query) == null) {
					System.err
							.println("Postingslist for " + query + " is null");
				} else {
					System.err.println("Postingslist for " + w.toString()
							+ " is null");
				}
			}
		}
		// LinkedList<String> returnList = new LinkedList<String>();
		LinkedList<Word> wordList = new LinkedList<Word>();
		Set<String> wordSet = wordScores.keySet();
		for (String s : wordSet) {
			Word w = new Word(s, wordScores.get(s));
			wordList.add(w);
		}
		Collections.sort(wordList);
		if (wordList.getFirst() != null) {
			System.out.println("The best hit is " + wordList.getFirst()
					+ " with score " + wordList.getFirst().score);
		}
		return wordList;
	}

	private PostingsList rankedSearch(LinkedList<String> searchTerms) {
		HashMap<String, Integer> terms = new HashMap<String, Integer>();
		LinkedList<PostingsList> pl = new LinkedList<PostingsList>();

		// Get unique search terms, and count their frequency
		for (String s : searchTerms) {
			int tmp = 0;

			if (terms.containsKey(s)) {
				tmp = terms.get(s);
			}

			terms.put(s, ++tmp);
		}

		Set<String> key = terms.keySet();

		HashSet<Integer> docIds = new HashSet<Integer>();

		// Get all postingslist for the search, and add all docId's
		for (String s : key) {
			PostingsList tmp = getPostings(s);
			pl.add(tmp);
			if (tmp != null) {
				for (PostingsEntry pe : tmp.getPostings()) {
					docIds.add(pe.docID);
				}
			}
		}

		Object[] docIdSet = docIds.toArray();

		double[] qv = new double[terms.size()]; // query vector
		double[][] dm = new double[docIds.size()][terms.size()]; // document
		// matrix

		int i = 0;
		for (String s : key) { // compute tf-idf for query
			PostingsList currentTerm = pl.get(i); // This term's postingsList
			if (currentTerm != null) {
				double df = (double) currentTerm.size() + 1;
				double idf = Math.log10((docLengths.size() + 1.0) / df);
				qv[i] = idf * (1 + Math.log10((double) terms.get(s)));
				// qv[i] = idf *(double)terms.get(s);
				for (int j = 0; j < dm.length; ++j) {
					// Vi måste ha PE för DOCID och Term
					int docID = (Integer) docIdSet[j];
					PostingsEntry pe = currentTerm.getEntryByDocID(docID);
					double tmp_tf = 0;
					if (pe != null)
						tmp_tf = (double) pe.getTF();
					if (tmp_tf > 0) {
						tmp_tf = 1.0 + Math.log10(tmp_tf);
					}
					dm[j][i] = idf * tmp_tf;
				}
			} else {
				for (int j = 0; j < dm.length; ++j) {
					dm[j][i] = 0;
				}
			}
			i++;
		}

		// calculate score
		double[] score = cos(qv, dm);

		for (i = 0; i < score.length; ++i) {
			System.err.println();
			score[i] = score[i]
					/ Math.log10(docLengths.get(docIdSet[i].toString()));

		}

		if (DEBUG) {
			// Print scores
			System.out.println("scores: ");
			for (i = 0; i < score.length; ++i)
				System.out.print(score[i] + " ");
			System.out.println();
			// print vectors
			System.out.print("Query v: ");
			for (i = 0; i < qv.length; ++i) {
				System.out.print(qv[i] + " ");
			}
			System.out.println();
			for (i = 0; i < dm.length; ++i) {
				System.out.print("Doc v: ");
				for (int j = 0; j < dm[i].length; ++j) {
					System.out.print(dm[i][j] + " ");
				}
				System.out.println();
			}

		}
		// Sort output
		LinkedList<PostingsEntry> pes = new LinkedList<PostingsEntry>();
		for (i = 0; i < dm.length; ++i) {
			pes.add(new PostingsEntry((Integer) docIdSet[i], score[i], 0));
		}
		PostingsList ret = new PostingsList();
		Collections.sort(pes);
		ret.addList(pes);

		return ret;
	}

	/**
	 * Calculate the tf-Idf score for a given term and document
	 * 
	 * @param term
	 * @param docID
	 * @return
	 */
	public double tfIdf(String term, int docID) {
		PostingsList p1 = getPostings(term); // Documents containing term
		int NP = docLengths.size(); // Number of documents
		int df = getPostings(term).size(); // Number of documents where term
		// occur

		double idf = Math.log10(NP / df); // Inverse term frequency
		double tf = p1.getEntryByDocID(docID) == null ? 0 : p1
				.getEntryByDocID(docID).offsets.size();
		// Number of occurence of the term in document (based on docID)
		double wf = (1 + Math.log10(tf)); // Weighted tf

		return wf * idf;

	}

	/**
	 * Will make an intersection between postingsLists p1 and p2 i.e. find all
	 * entries which have the same docID for both p1 and p2.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private PostingsList intersection(PostingsList p1, PostingsList p2) {

		PostingsList ans = new PostingsList();
		int i = 0, j = 0;
		PostingsEntry docA, docB;
		while (i != p1.size() && j != p2.size()) {
			docA = p1.get(i);
			docB = p2.get(j);
			if (docA.docID == docB.docID) {
				ans.add(docA.docID);
				i++;
				j++;
			} else if (docA.docID < docB.docID) {
				i++;
			} else {
				j++;

			}
		}
		return ans;
	}

	/**
	 * It is essential to run the cleanup before terminating the JVM, otherwise
	 * the index files might become corrupted.
	 */
	public void cleanup() {
		// Save the docID-filename association list in the MegaMap as well
		index.put("..docIDs", docIDs);
		index.put("..docLengths", docLengths);
		// Shutdown the MegaMap thread gracefully
		manager.shutdown();
	}

	/**
	 * Calculate the cosine score between a query vector and document vector.
	 * 
	 * @param qv
	 * @param dm
	 * @return
	 */
	private double[] cos(double[] qv, double[][] dm) {
		double qnorm = 0;
		for (int i = 0; i < qv.length; ++i) {
			qnorm += qv[i] * qv[i];
		}
		qnorm = Math.sqrt(qnorm);
		double[] dnorm = new double[dm.length];
		double[] score = new double[dm.length];
		for (int i = 0; i < dm.length; ++i) {
			dnorm[i] = 0.0;
			for (int j = 0; j < dm[i].length; ++j) {
				dnorm[i] += dm[i][j] * dm[i][j];
			}
			dnorm[i] = Math.sqrt(dnorm[i]);
		}

		for (int i = 0; i < dm.length; ++i) {
			score[i] = 0.0;
			for (int j = 0; j < dm[i].length; ++j) {
				score[i] += qv[j] * dm[i][j];// (qnorm*dnorm[i]);
			}
			score[i] = score[i];// (qnorm);//*dnorm[i]);
		}
		return score;
	}

}
