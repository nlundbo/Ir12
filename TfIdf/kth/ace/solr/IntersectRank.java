package kth.ace.solr;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;


public class IntersectRank implements ACERanker {
	private String orgQuery; //Original query 
	private CommonsHttpSolrServer server;
	
	
	
	public IntersectRank(CommonsHttpSolrServer server, String query)
	{
		this.server = server;
		this.orgQuery = query;
	}
	
	
	public LinkedList<Word> getRankedList(DWMatrix matrix) {
		int p = 0;
		LinkedList<LinkedList<Word>> dwMatrix = matrix.getMatrix();
		LinkedList<Word> result = new LinkedList<Word>();
		HashSet<String> uniqueWords = new HashSet<String>();
		for (LinkedList<Word> ll : dwMatrix) {
			for (Word word : ll) {
				if(!uniqueWords.contains(word.getWord())){
					uniqueWords.add(word.getWord());
					try {
						SolrQuery query = new SolrQuery();
						query.setQuery(orgQuery+" AND "+"\""+word.getWord()+"\"");
						query.set("fl", "numFound");
						double hits = server.query(query).getResults().getNumFound();
						result.add(new Word(word.getWord(),hits));
						System.err.println(p);
						p++;
						
					} catch (SolrServerException  e) {
						//TODO handle more appropriately.
						System.err.println("SolrServerException during intersectRank returning n");
						e.printStackTrace();
						return null;
					}
					
				}
			}
		}

		Collections.sort(result);
		return result;
	}

	/**
	 * Set a new query String. Note that this will overwrite the original query. 
	 * @param query
	 */
	public void setQuery(String query)
	{
		orgQuery = query;
	}
}
