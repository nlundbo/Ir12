package kth.ace.solr;

import java.net.MalformedURLException;
import java.util.LinkedList;

import org.apache.solr.client.solrj.SolrServerException;

public class StandaloneQR {
	public static void main(String[] args) throws MalformedURLException,
	SolrServerException {
		
		LinkedList<Word> ace = new ContextExpander().postUserQuery("kokande vatten");
		
		

	}
}
