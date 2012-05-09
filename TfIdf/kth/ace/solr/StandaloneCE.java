/**
 * Standalone class for the Context Expander. 
 * 
 */
package kth.ace.solr;

import java.util.LinkedList;
import java.util.Scanner;


public class StandaloneCE {
	public static void main(String[] args){
		System.out.println("Welcome to the Automatic Context Expander for Solr");
		Scanner sc = new Scanner(System.in);
		String query = "";
		System.out.print("Enter Query >>");
		while(!(query=sc.nextLine()).equals("/q")){
			LinkedList<Word> ace = new ContextExpander().postUserQuery(query);
			System.out.println();
			System.out.print("Enter Query >>");
		}
	}
}
