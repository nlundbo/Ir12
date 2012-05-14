package kth.ace.solr;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;


public class DWMatrix {
	private LinkedList<LinkedList<Word>> Matrix;
	private int D ; // number of top ranking documents to retrieve
	private int W ; // number of top ranking words to retrieve
	
	/**
	 * Constructor for the DWMatrix, takes a QueryResponse and generates DWMatrix.
	 * @param solrRsp - QueryResponse
	 * @param D - Number of documents in matrix i.e No rows
	 * @param W - Number of words from each document i.e No columns
	 */
	public DWMatrix(QueryResponse solrRsp , int D , int W)
	{
		this.D = D;
		this.W = W;
		Matrix = cropMatrix(parseTfIdfs(solrRsp));
	}
	
	/**
	 * Will parse the QueryResponse and generate a full Matrix from all results.
	 * Note that the basis for this parsing is inspired by a post on stackoverflow.com (2012-05-08)
	 * For further info see <code>notes.txt.</code>
	 * <b>Note.</b> This method does some casts that are not necessarily safe. 
	 * @param solrRsp
	 * @return A matrix containing fully parsed TfIdf matrix from QueryResponse.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private LinkedList<LinkedList<Word>> parseTfIdfs(QueryResponse solrRsp) {
		LinkedList<LinkedList<Word>> tfidf = new LinkedList<LinkedList<Word>>();
		NamedList<Object> e = solrRsp.getResponse();
		Iterator<Entry<String, Object>> termVectors = ((NamedList) e.get("termVectors")).iterator();

		while (termVectors.hasNext()) {
			Entry<String, Object> docTermVector = termVectors.next();
			for (Iterator<Entry<String, Object>> document = ((NamedList) docTermVector
					.getValue()).iterator(); document.hasNext();) {
				Entry<String, Object> fieldEntry = document.next();
				if (fieldEntry.getKey().equals("text")) {
					LinkedList<Word> tempLinkedList = new LinkedList<Word>();
					for (Iterator<Entry<String, Object>> tvInfoIt = ((NamedList) fieldEntry
							.getValue()).iterator(); tvInfoIt.hasNext();) {
						Entry<String, Object> wordAndArrayList = tvInfoIt
						.next();
						NamedList theValue = (NamedList) wordAndArrayList
						.getValue();
						tempLinkedList.add(new Word(wordAndArrayList.getKey(),
								Double.valueOf(theValue.get("tf-idf")
										.toString())));

					}
					Collections.sort(tempLinkedList);
					tfidf.add(tempLinkedList);
				}
			}
			docTermVector = termVectors.next();
		}
		return tfidf;

	}
	
	/**
	 * The method will crop the matrix according to requested <code>D</code> 
	 * and <code>W</code>
	 * @param tfIdfMatrix - {@literal LinkedList<LinkedList<Word>>}
	 * @return Matrix with dimensions [D,W]
	 */
	private LinkedList<LinkedList<Word>> cropMatrix(LinkedList<LinkedList<Word>> tfIdfMatrix) {
		LinkedList<LinkedList<Word>> DWMatrix = new LinkedList<LinkedList<Word>>();


		for (LinkedList<Word> ll : tfIdfMatrix) {
			LinkedList<Word> addList = new LinkedList<Word>();
			for (int i = 0; i < Math.min(W, ll.size()); i++) {
				Word tmp = ll.pop();
				tmp.setScore(W-1.0);

				addList.addLast(tmp);
			}
			DWMatrix.addLast(addList);
		}

		return DWMatrix;
	}
	
	/**
	 * Retrieve the DWMatrix
	 * @return {@literal LinkedList<LinkedList<Word>>}
	 */
	public LinkedList<LinkedList<Word>> getMatrix()
	{
		return Matrix;
	}
	
}
