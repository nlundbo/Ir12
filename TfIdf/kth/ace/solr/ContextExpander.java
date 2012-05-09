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
	public static final int MIXED_RANK = 3;
	
	static CommonsHttpSolrServer server; //Solr server connection
	private  int D = 50; // number of top ranking documents to retrieve
	private  int W = 10; // number of top ranking words to retrieve
	private  int NOR = 10; //Number of results to return to GUI
	private int minDocsize = 1; //Minimum document size to consider
	private DWMatrix dwMatrix; //Document|Word Matrix
	
	
	private int queryType = 1;
	private String strQuery;

	/**
	 * Default constructor will set Solr server url to localhost on port 8983
	 */
	public ContextExpander()
	{
		String url = "http://localhost:8983/solr/";
		
		try{
			
		server = new CommonsHttpSolrServer(url);
		}catch(MalformedURLException e)
		{
			//TODO Send information to logger	
			System.err.println("Default constructor");	
		}
	}
	
	/**
	 * Construct new QueryRunnery
	 * @param url
	 */
	public ContextExpander(String url){
		try{
		server = new CommonsHttpSolrServer(url);
		}catch(MalformedURLException e)
		{
			
			
		}
	}
	
	

	private QueryResponse sendQuery() throws SolrServerException {

		SolrQuery query = new SolrQuery();
		query.setQuery(strQuery);
		query.set("qt", "tvrh");
		query.set("tv.tf_idf", "true");
		query.set("rows", D+"" );
		query.set("fq", "docsize>"+minDocsize);
		System.err.println("Query " + query.toString());

		return server.query(query);
	}

	/**
	 * Post a user query setting all parameters.
	 * @param query
	 * @param D
	 * @param W
	 * @param queryType
	 * @param minDocSize
	 * @return
	 */
	public LinkedList<Word> postUserQuery(String query, int D, int W, int queryType, int minDocSize)
	{
		this.D = D;
		this.W = W;
		this.queryType = queryType;
		this.minDocsize = minDocSize;
		return expandContext(query);
		
	}
	
	/**
	 * Default query TODO fix comment
	 * @param query
	 * @return
	 */
	public LinkedList<Word> postUserQuery(String query)
	{
		return expandContext(query);
	}
	
	/**
	 *TODO FIX COMMENT
	 * @param query
	 * @return
	 */
	private LinkedList<Word> expandContext(String query) {
		
		Long time = System.currentTimeMillis();
		QueryResponse rsp;
		LinkedList<Word> result = null;
		parseQuery(query);
		
		try{
			rsp = sendQuery();
			result = runACERanker(rsp);
		}catch(SolrServerException e)
		{
			//TODO handle exception
			e.printStackTrace();
		}
		 

		//TODO remove this Debug section
		if(DEBUG){
		for (int i = 0; i<Math.min(result.size(),NOR);i++) {
			System.out.println(result.get(i).toString());
		}
		System.out.println("Time elapsed: "+(System.currentTimeMillis()-time));
		}
		
		//TODO Send retrieval time information to 
		//System.out.println("Time elapsed: "+(System.currentTimeMillis()-time));

		return result;
	}

	/**
	 * Will parse a multiword query from the user given as a String
	 * @param query
	 */
	private void parseQuery(String query) {
		strQuery = query.trim().replace(" ", " AND ");
	}


	private LinkedList<Word> runACERanker(QueryResponse rsp) throws SolrServerException {
		dwMatrix = new DWMatrix(rsp,D,W);
		SummationRank sr = new SummationRank();
		IntersectRank ir = new IntersectRank(server, strQuery);
		
		switch (queryType) {
		case SUMMATION_RANK:
			return sr.getRankedList(dwMatrix);
		case INTERSECT_RANK:
			return ir.getRankedList(dwMatrix);
		case MIXED_RANK:
			return mixedRank(sr.getRankedList(dwMatrix),ir.getRankedList(dwMatrix));
		default :
			return sr.getRankedList(dwMatrix);
		}
		
	}

	/**
	 * Weight results from both summationrank and intersectrank.
	 * @param summationRank
	 * @param intersectRank
	 * @return
	 */
	private LinkedList<Word> mixedRank(LinkedList<Word> summationRank,
			LinkedList<Word> intersectRank) {
		LinkedList<Word> results = new LinkedList<Word>();
		
		HashSet<String> inIntersect = new HashSet<String>();
		for (Word word : intersectRank) {
			inIntersect.add(word.getWord());
		}		
		
		for (Word word : summationRank){
			if(inIntersect.contains(word.getWord())) results.add(word);
		}
		
		return results;
	}

	/**
	 * Set the minimum document size - 
	 * @param minDocSize - number of chars in document.
	 */
	private void setMinDocSize(int minDocSize)
	{
		this.minDocsize = minDocSize;
	}
	
	
	
}