package kth.ace.solr;
import java.util.LinkedList;


public interface ACERanker {
	
	public LinkedList<Word> getRankedList(DWMatrix matrix);
}
