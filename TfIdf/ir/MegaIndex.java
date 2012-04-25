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

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;

public class MegaIndex implements Index {

	/**
	 * The index as a hash map that can also extend to secondary memory if
	 * necessary.
	 */
	private MegaMap index;

	/**
	 * The MegaMapManager is the user's entry point for creating and saving
	 * MegaMaps on disk.
	 */
	private MegaMapManager manager;

	/** The directory where to place index files on disk. */
	private static final String path = "./index";

	private static boolean DEBUG = false;

	private final int D = 50; // number of top ranking documents to retrieve
	private final int K = 20; // number of top ranking words to retrieve

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
	 * Create a MegaIndex, possibly from a list of smaller indexes.
	 */
	public MegaIndex(LinkedList<String> indexfiles) {
		try {
			manager = MegaMapManager.getMegaMapManager();
			if (indexfiles.size() == 0) {
				// No index file names specified. Construct a new index and
				// invent a name for it.
				index = manager.createMegaMap(generateFilename(), path, true,
						false);

			} else if (indexfiles.size() == 1) {
				// Read the specified index from file
				index = manager.createMegaMap(indexfiles.get(0), path, true,
						false);
				HashMap<String, String> m = (HashMap<String, String>) index
						.get("..docIDs");
				HashMap<String, Integer> dl = (HashMap<String, Integer>) index
						.get("..docLengths");

				if (m == null) {
					System.err
							.println("Couldn't retrieve the associations between docIDs and document names");
				} else {
					docIDs.putAll(m);
				}
				if (dl == null) {
					System.err.println("Couldn't retrieve docLengths");
				} else {
					docLengths.putAll(dl);
				}
			} else {
				// Merge the specified index files into a large index.
				MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
				for (int k = 0; k < indexfiles.size(); k++) {
					System.err.println(indexfiles.get(k));
					indexesToBeMerged[k] = manager.createMegaMap(
							indexfiles.get(k), path, true, false);
				}
				index = merge(indexesToBeMerged);
				for (int k = 0; k < indexfiles.size(); k++) {
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
	String generateFilename() {
		String s = "index_" + Math.abs((new java.util.Date()).hashCode());
		System.err.println(s);
		return s;
	}

	/**
	 * It is ABSOLUTELY ESSENTIAL to run this method before terminating the JVM,
	 * otherwise the index files might become corrupted.
	 */
	public void cleanup() {
		// Save the docID-filename association list in the MegaMap as well
		index.put("..docIDs", docIDs);
		index.put("..docLengths", docLengths);
		// Shutdown the MegaMap thread gracefully
		manager.shutdown();
	}

	/**
	 * Returns the dictionary (the set of terms in the index) as a HashSet.
	 */
	@SuppressWarnings("rawtypes")
	public Set getDictionary() {
		return index.getKeys();
	}

	/**
	 * Merges several indexes into one.
	 */
	MegaMap merge(MegaMap[] indexes) {
		try {
			MegaMap res = manager.createMegaMap(generateFilename(), path, true,
					false);

			for (int i = 0; i < indexes.length; ++i) {
				HashMap<String, String> m = (HashMap<String, String>) indexes[i]
						.get("..docIDs");
				if (m == null) {
					System.err
							.println("Couldn't retrieve the associations between docIDs and document names");
				} else {
					docIDs.putAll(m);
				}

				Set<String> words = (Set<String>) indexes[i].getKeys();
				for (String s : words) {
					if (res.hasKey(s)) {
						PostingsList pl0 = getPostings(res, s);
						PostingsList pli = getPostings(indexes[i], s);
						pl0.mergeList(pli);
						res.put(s, pl0);
					} else {
						res.put(s, getPostings(indexes[i], s));
					}
				}

			}

			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Inserts this token in the hashtable.
	 */
	public void insert(String token, int docID, int offset) {
		int score = 0; // todo
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

	public static PostingsList getPostings(MegaMap index, String token) {
		try {
			return (PostingsList) index.get(token);
		} catch (Exception e) {
			return new PostingsList();
		}
	}

	/**
	 * Searches the index for postings matching the query in @code{searchterms}.
	 */
	public LinkedList<String> search(LinkedList<String> searchTerms,
			int queryType) {

		boolean meidi = false;
		LinkedList<String> returnList = new LinkedList<String>();
		if (queryType == Index.RANKED_QUERY) {
			PostingsList pll = rankedSearch(searchTerms);
			for (PostingsEntry pl : pll.getList()) {
				System.out.println("result: " + docIDs.get("" + pl.docID));
			}

			LinkedList<LinkedList<Word>> DKMatrix = new LinkedList<LinkedList<Word>>();

			// TODO Get List docIDs from resulting postings list
			for (int i = 0; i < D && i < pll.getList().size(); i++) {
				PostingsEntry pl = pll.getList().get(i);
				int docID = pl.docID;
				String file = docIDs.get("" + docID);

				// Get corresponding file given the docID TODO do it for every
				// docID
				System.err.println(file);
				// TODO Ignore documents shorter than 5 words
				DKMatrix.add(getTopWords(docID, file));
			}
			LinkedList<Word> ll = new LinkedList<Word>();
			if (!meidi) {
				ll = wordSumOfPos(DKMatrix, 5);
			} else {
				ll = intersectionRank(DKMatrix, searchTerms.getFirst());
			}
			for (Word w : ll) {
				returnList.add("S: " + w.word + " V: " + w.tfidf);
			}

			return returnList;
		}

		return null;
	}

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
							w.tfidf
									+ intersection(getPostings(query),
											getPostings(w.toString())).size());
					System.out.println("Score for "
							+ w.toString()
							+ " is "
							+ w.tfidf
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
					+ " with score " + wordList.getFirst().tfidf);
		}
		return wordList;
	}

	/**
	 * Take a doc/word matrix and rank them according to the sum of ranking
	 * positions. Example. The matrix consists of <i>k</i> columns and the
	 * <i>r</i> rows. The word W occurs in columns 1, 5 and 4 in different rows.
	 * most
	 * 
	 * @return
	 */
	public LinkedList<Word> wordSumOfPos(LinkedList<LinkedList<Word>> DKMatrix,
			int n) {
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
		LinkedList<Word> returnList = new LinkedList<Word>();

		for (int i = 0; i < n; i++) {
			returnList.addLast(rankedList.get(i));
		}
		return returnList;
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
			int offset = 0;

			// Hashmap containing word/score pairs
			hm = new HashMap<String, Double>();

			while (tok.hasMoreTokens()) {
				String str = tok.nextToken();
				hm.put(str, new Double(tfIdf(str, docID)));
				offset++;
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

		for (int i = 0; i < K; i++) {
			returnlist.addLast(ll.pop());
		}

		return returnlist;
	}

	public double tfIdf(String term, int docID) {
		PostingsList p1 = getPostings(term); // Documents containing term
		int NP = docLengths.size(); // Number of documents
		int df = getPostings(term).size(); // Number of documents where term
		// occur

		double idf = Math.log10(NP / df); // Inverse term frequency
		double tf = p1.getDocIdList(docID) == null ? 0
				: p1.getDocIdList(docID).wordPos.size();
		// Number of occurence of the term in document (based on docID)
		double wf = (1 + Math.log10(tf)); // Weighted tf

		return wf * idf;

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
				for (PostingsEntry pe : tmp.getList()) {
					docIds.add(pe.docID);
				}
			}
		}

		Object[] docIdSet = docIds.toArray();
		for (Object foo : docIdSet) {
			System.out.println("DOCIDSET" + foo.toString());
		}

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
					PostingsEntry pe = currentTerm.getDocIdList(docID);
					double tmp_tf = 0;
					if (pe != null)
						tmp_tf = (double) pe.wordPos.size();
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
	 * ##################################### COSINE CALCULATION
	 * #####################################
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
