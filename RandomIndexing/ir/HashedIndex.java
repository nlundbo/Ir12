/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

package ir;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.*;
import java.io.*;

/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

	/** The index as a hashtable. */
	private HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();
	private HashMap<Integer, Integer> docSize = new HashMap<Integer, Integer>();
	private HashMap<Integer, Double> pageRank;
	int N = 0;
	private static boolean PAGE = false;
	private static boolean DEBUG = false;

	// private static String PAGERANKFILE = "ir/tmp";
	// private static String PAGERANKFILE = "/var/tmp/MC3";
	private static String PAGERANKFILE = "/var/tmp/MC4";

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
	public PostingsList search(LinkedList<String> searchTerms, int queryType) {

		if (queryType == Index.RANKED_QUERY) {
			PostingsList pll = rankedSearch(searchTerms);
			for (PostingsEntry pl : pll.getList()) {
				System.out.println("result: " + docIDs.get("" + pl.docID));
		}

			PostingsEntry pl = pll.getList().getFirst();
			int docID = pl.docID;
			String file = docIDs.get("" + docID);
			System.err.println(file);
			try {
				FileReader reader = new FileReader(new File(file));
				SimpleTokenizer tok = new SimpleTokenizer(reader);
				int offset = 0;
				HashMap<String, Double> hm = new HashMap<String, Double>();
				while (tok.hasMoreTokens()) {
					String str = tok.nextToken();
					hm.put(str, new Double(tfIdf(str, docID)));
					offset++;
				}
				LinkedList<Word> ll = new LinkedList<Word>();
				for (String key : hm.keySet()) {
					ll.add(new Word(key, hm.get(key)));
				}

				Collections.sort(ll);
				int i = 0;
				for (Word m : ll) {
					i++;
					System.out.println("S: " + m.word + " V: " + m.tfidf);
					if (i > 10)
						break;
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return pll;
		} else if (searchTerms.size() == 1) {
			return getPostings(searchTerms.getFirst());
		} 
		
		return null;
	}
	
	private class Word implements Comparable<Word> {
		public String word;
		public Double tfidf;

		public Word() {
			word = "";
			tfidf = new Double(0.0);
		}

		public Word(String str, Double tfidf) {
			word = str;
			this.tfidf = tfidf;
		}

		public int compareTo(Word o) {
			return Double.compare(o.tfidf, tfidf);
		}
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
		System.out.println("tf: " + tf);
		System.out.println("idf: " + idf);
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
			if (PAGE) { // Page rank enabled
				double[] pr = new double[score.length];
				double sum = 0.0;
				Double tmp;
				// Fetch pageRank for our documents, and calculate squared sum
				for (int j = 0; j < score.length; ++j) {
					tmp = pageRank.get((Integer) docIdSet[j]);
					if (tmp == null) {
						tmp = 0.0;
					}
					pr[j] = tmp;
					sum += pr[j] * pr[j];
				}

				// Normalize page rank
				double norm = Math.sqrt(sum);
				for (int j = 0; j < score.length; ++j) {
					pr[j] = pr[j] / norm;
				}

				score[i] = score[i]
						/ Math.log10(docSize.get((Integer) docIdSet[i]))
						+ pr[i] * score[i]
						/ Math.log10(docSize.get((Integer) docIdSet[i]));
			} else { // disabled
				score[i] = score[i]
						/ Math.log10(docSize.get((Integer) docIdSet[i]));
			}
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

	
	private boolean phraseSearch(int offset,
			ArrayList<LinkedList<Integer>> wordPos, int idx) {
		if (idx == wordPos.size())
			return true;
		return wordPos.get(idx).contains(offset)
				&& phraseSearch(offset + 1, wordPos, idx + 1);
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
