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

	/** The index as a hashtable. */
	private HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();
	private HashMap<Integer, Integer> docSize = new HashMap<Integer, Integer>();
	
	int N = 0;
	private static boolean PAGE = false;
	private static boolean DEBUG = false;

	private final int D = 50; // number of top ranking documents to retrieve
	private final int K = 20; // number of top ranking words to retrieve


	/**
	 * Inserts this token in the index.
	 */
	public void insert(String token, int docID, int offset) {
		int i = 0;
		if (docSize.containsKey(docID)) {
			i = docSize.get(docID);
		}
		docSize.put(docID, i + 1);
		N++;
		int score = 0; // todo
		PostingsList pl;
		if (index.containsKey(token)) {
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
		return index.get(token);
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
			for (int i = 0; i < D && i < pll.getList().size() ; i++) {
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
			if(!meidi) {
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

	public LinkedList<Word> intersectionRank(LinkedList<LinkedList<Word>> DKMatrix, String query) {
		boolean onlyIntersection = false; // otherwise, multiply by tfidf
		HashMap<String,Double> wordScores = new HashMap<String,Double>();

		for (LinkedList<Word> ll : DKMatrix) {
			for (Word w : ll) {
				if(onlyIntersection && getPostings(query)!=null && getPostings(w.toString())!=null){
					wordScores.put(w.toString(), Math.log10((double)intersection(getPostings(query),getPostings(w.toString())).size()));
					System.out.println("Intersections for "+w.toString() + " is "+intersection(getPostings(query),getPostings(w.toString())).size());
				} else if(getPostings(query)!=null && getPostings(w.toString())!=null){
					wordScores.put(w.toString(), w.tfidf+intersection(getPostings(query),getPostings(w.toString())).size());
					System.out.println("Score for "+w.toString() + " is "+w.tfidf+intersection(getPostings(query),getPostings(w.toString())).size() + ": "+intersection(getPostings(query),getPostings(w.toString())).size() + " intersections");
				} else if(getPostings(query)==null&&getPostings(w.toString())==null) {
					System.err.println("Postingslist for "+ query+" and "+w.toString()+" is null");
				} else if(getPostings(query)==null){
					System.err.println("Postingslist for "+ query+" is null");
				} else {
					System.err.println("Postingslist for "+ w.toString()+" is null");
				}
			}
		}
		//LinkedList<String> returnList = new LinkedList<String>();
		LinkedList<Word> wordList = new LinkedList<Word>();
		Set<String> wordSet = wordScores.keySet();
		for(String s : wordSet) {
			Word w = new Word(s,wordScores.get(s));
			wordList.add(w);
		}
		Collections.sort(wordList);
		if(wordList.getFirst()!=null) {
			System.out.println("The best hit is "+wordList.getFirst() + " with score "+wordList.getFirst().tfidf);
		}
		return wordList;
	}

	/**
	 * Take a doc/word matrix and rank them according to the 
	 * sum of ranking positions.  
	 * Example. 
	 * The matrix consists of <i>k</i> columns and the <i>r</i> rows. 
	 * The word W occurs in columns 1, 5 and 4 in different rows. 
	 * The score for W would then be  (k-1)+(k-5)+(k-4)=3k-10 
	 * Get the <i>n</i> most 
	 * @return
	 */
	public LinkedList<Word> wordSumOfPos(LinkedList<LinkedList<Word>> DKMatrix, int n)
	{
		HashMap<String,Double> scoreBoard = new HashMap<String, Double>();

		for(LinkedList<Word> lw: DKMatrix)
		{
			int i = lw.size();
			for(Word w: lw)
			{
				if(scoreBoard.containsKey(w.word))
				{
					scoreBoard.put(w.word, scoreBoard.get(w.word)+i);
				}else
				{
					scoreBoard.put(w.word, new Double(i));
				}
				i--;
			}
		}

		LinkedList<Word> rankedList = new LinkedList<Word>();

		for(String key : scoreBoard.keySet())
		{
			rankedList.add(new Word(key,scoreBoard.get(key)));
		}
		Collections.sort(rankedList);
		LinkedList<Word> returnList = new LinkedList<Word>();

		for(int i = 0 ; i < n ; i++)
		{
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
		//		System.out.println("tf: " + tf);
		//		System.out.println("idf: " + idf);
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

		double[] qv = new double[terms.size()]; // query vector
		double[][] dm = new double[docIds.size()][terms.size()]; // document
		// matrix

		int i = 0;
		for (String s : key) { // compute tf-idf for query
			PostingsList currentTerm = pl.get(i); // This term's postingsList
			if (currentTerm != null) {
				double df = (double) currentTerm.size() + 1;
				double idf = Math.log10((docSize.size() + 1.0) / df);
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
				score[i] = score[i]
						/ Math.log10(docSize.get((Integer) docIdSet[i]));
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
	 * No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}
}
