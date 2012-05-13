/**
 * Main logic class for the Automatic Context Expansion project. This application was created
 * as a project in course DD2476 information retrieval at KTH (2012). The application provides 
 * methods that attempt to expand a given query's context.  
 * @author Mattias Knutsson 
 * @author Sebastian Remnerud
 * @version 2012.05-09
 */

package kth.ace.solr;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

public class ContextExpander {
	private static final boolean DEBUG = true;

	public static final int SUMMATION_RANK = 1;
	public static final int INTERSECT_RANK = 2;
	public static final int MERGED_RANK = 3;
	private static final int MAX_SCORE = 1000;
	
	static CommonsHttpSolrServer server; // Solr server connection
	private int D = 50; // number of top ranking documents to retrieve
	private int W = 10; // number of top ranking words to retrieve
	private int NOR = 20; // Number of results to return to GUI 
									
	private int minDocsize = 600; // Minimum document size to consider
	private DWMatrix dwMatrix; // Document|Word Matrix

	private ArrayList<String> queryWords = new ArrayList<String>();
	private int queryType = SUMMATION_RANK;
	private String strQuery;

	/**
	 * Default constructor will set Solr server url to localhost on port 8983
	 */
	public ContextExpander() {
		String url = "http://localhost:8983/solr/";

		try {

			server = new CommonsHttpSolrServer(url);
		} catch (MalformedURLException e) {
			// TODO Send information to logger
			System.err.println("Default constructor");
		}
	}

	/**
	 * Construct new QueryRunnery
	 * 
	 * @param url
	 */
	public ContextExpander(String url) {
		try {
			server = new CommonsHttpSolrServer(url);
		} catch (MalformedURLException e) {

		}
	}

	private QueryResponse sendQuery() throws SolrServerException {

		SolrQuery query = new SolrQuery();
		query.setQuery(strQuery);
		query.set("qt", "tvrh");
		query.set("tv.tf_idf", "true");
		query.set("rows", D + "");
		query.set("fq", "docsize:[" + minDocsize + " TO *]");
		
		if(DEBUG){System.err.println(query.toString());};

		return server.query(query);
	}

	/**
	 * Post a user query setting all parameters.
	 * 
	 * @param query
	 * @param D
	 * @param W
	 * @param queryType
	 * @param minDocSize
	 * @return
	 */
	public LinkedList<Word> postUserQuery(String query, int D, int W,
			int queryType, int minDocSize) {
		this.D = D;
		this.W = W;
		this.queryType = queryType;
		this.minDocsize = minDocSize;

		for (String w : query.split(" ")) {
			queryWords.add(w);
		}

		return expandContext(query);

	}

	/**
	 * Default query TODO fix comment
	 * 
	 * @param query
	 * @return
	 */
	public LinkedList<Word> postUserQuery(String query) {
		for (String w : query.split(" ")) {
			queryWords.add(w.toLowerCase());
		}
		
		return expandContext(query);
	}

	/**
	 * TODO FIX COMMENT
	 * 
	 * @param query
	 * @return
	 */
	private LinkedList<Word> expandContext(String query) {

		Long time = System.currentTimeMillis();
		QueryResponse rsp;
		LinkedList<Word> result = null;
		parseQuery(query);

		try {
			rsp = sendQuery();
			result = runACERanker(rsp);
		} catch (SolrServerException e) {
			// TODO handle exception
			e.printStackTrace();
		}

		// TODO remove this Debug section
		if (DEBUG) {
			System.out.println("Time elapsed: "
					+ (System.currentTimeMillis() - time));
			// TODO send to web interface
		}

		if (result != null) {

			for (int i = 0; i < result.size(); i++) {
				for (String queryWord : queryWords) {
					if (result.get(i).getWord().equals(queryWord)) {
						result.remove(i);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Will parse a multiword query from the user given as a String
	 * 
	 * @param query
	 */
	private void parseQuery(String query) {
		strQuery = query.trim().replace(" ", " AND ");
	}

	private LinkedList<Word> runACERanker(QueryResponse rsp)
			throws SolrServerException {
		dwMatrix = new DWMatrix(rsp, D, W);
		SummationRank sr = new SummationRank();
		IntersectRank ir = new IntersectRank(server, strQuery);

		switch (queryType) {
		case SUMMATION_RANK:
			return sr.getRankedList(dwMatrix);
		case INTERSECT_RANK:
			return ir.getRankedList(dwMatrix);
		case MERGED_RANK:
			LinkedList<LinkedList<Word>> mtrx = new LinkedList<LinkedList<Word>>();
			mtrx.add(sr.getRankedList(dwMatrix));
			mtrx.add(ir.getRankedList(dwMatrix));
			return mergeRanks(mtrx);
		default:
			return sr.getRankedList(dwMatrix);
		}

	}

	/**
	 * Weight results from both summationrank and intersectrank.
	 * 
	 * @param summationRank
	 * @param intersectRank
	 * @return
	 */
	private LinkedList<Word> mergeRanks(LinkedList<LinkedList<Word>> scoreMatrix) {
		
		//Normalize all scores
		for(LinkedList<Word> lw : scoreMatrix)
		{
			double scoreSum = scoreSum(lw);
			for(Word w : lw)
			{
				double oldScore = w.getScore();
				w.setScore((w.getScore()/scoreSum)*MAX_SCORE);
			}
		}
		
		
		
		HashMap<String, Word> resultMap = new HashMap<String,Word>();
		//TODO Verify that references are handled correctly
		for(LinkedList<Word> lw : scoreMatrix)
		{
			for(Word w : lw)
			{
				if(resultMap.containsKey(w.getWord()))
				{
					Word tmp = resultMap.get(w.getWord());
					tmp.setScore(tmp.getScore()+w.getScore());
				}else{
					resultMap.put(w.getWord(),w);
				}
			}
		}
		LinkedList<Word> results = new LinkedList<Word>(resultMap.values());
		Collections.sort(results);
		
		System.gc(); //TODO Note.. garbage collection
		
		return results;
	}
	
	/**
	 * Sum up the score for all the the words in wordlist
	 * @param wordlist
	 * @return
	 */
	private double scoreSum(LinkedList<Word> wordlist)
	{
		int res = 0;
		for(Word w : wordlist)
		{
			res += w.getScore();
		}
		return res;
	}

	/**
	 * Set the minimum document size -
	 * 
	 * @param minDocSize
	 *            - number of chars in document.
	 */
	private void setMinDocSize(int minDocSize) {
		this.minDocsize = minDocSize;
	}

}