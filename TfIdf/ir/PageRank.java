package ir;
/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  


import java.util.*;


import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;
    private static Random r = new Random();
    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    
    
    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
    	int noOfDocs = readDocs( filename );
    	computePagerank( noOfDocs );
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
    	
    	double [] x = powerIteration(numberOfDocs);
      	//double [] x = MC3(numberOfDocs);
    	
    	boolean redovisning = true;
    	if( redovisning ){
    		
    		LinkedList<Output> ll = new LinkedList<Output>();
    		for(int i =0 ; i< numberOfDocs; ++i){
    			ll.add(new Output(docName[i],x[i]) );
    		}
    		
    		Collections.sort(ll);
    		for(Output o : ll){
    			System.out.println(o);
    		}
    		/*
    		double max = -1.0;
    		int idx = -1;
    		for(int i =0 ; i< numberOfDocs; ++i){
    			if(x[i] > max){
    				max = x[i];
    				idx = i;
    			}
    			System.out.println(docName[i] + " : " + x[i]);
    			
    		}*/
    		//System.out.println("max pagerank: " + docName[idx] + "(" + max +")");
    		
    	}
    	
    }
    
    private class Output implements Comparable<Output>{
    	String name;
    	double pr;
    	Output(){    		
    	}
    	Output(String in, double pr){
    		name = in;
    		this.pr = pr;
    	}
        public int compareTo( Output other ) {
        	return Double.compare( other.pr, pr);
        }
        @Override
        public String toString(){
        	return name + " : " + pr;
        }
    	
    }
    
    
    int steps;
    double [] MC3(int numberOfDocs){
    	double [] x = new double[numberOfDocs];
    	int [] visits = new int[numberOfDocs];
    	
    	// n, m
    	int M = 1;
    	int totalSteps =0;
    	for(int m =0; m< M ; ++m){
    		for(int i =0; i< numberOfDocs; ++i){
    			// starting in node i;
    			steps = 0; 
    			MC4Rek(visits,i,numberOfDocs);    			
    			totalSteps += steps;
    			
    		}
    	}
    	for(int i = 0 ; i< numberOfDocs; ++i){
    		x[i] = (double)visits[i]/(double)(M*numberOfDocs);
    	}
    	return x;
    }
    
    double [] MC4(int numberOfDocs){
    	double [] x = new double[numberOfDocs];
    	int [] visits = new int[numberOfDocs];    	
    	// n, m
    	int M = 1;
    	int totalSteps =0;
    	for(int m =0; m< M ; ++m){    		
    		for(int i =0; i< numberOfDocs; ++i){
    			// starting in node i;
    			steps = 0; 
    			MC4Rek(visits,i,numberOfDocs);    			
    			totalSteps += steps;
    			
    		}
    	}
    	for(int i = 0 ; i< numberOfDocs; ++i){
    		x[i] = (double)visits[i]/(double)totalSteps;
    	}
    	return x;
    }
    
    void MC4Rek(int [] visits , int i,int numberOfDocs){
    	visits[i]++;
    	steps++;
    	if(steps > 50){    		
    		return; 
    	}
    	double prob = r.nextDouble();
    	if(prob > BORED){    	
	    	Hashtable<Integer,Boolean> ht = link.get(i);    	
	    	if(ht != null){    
	    		// finding the 'nextIndex'th in the set
	    		Set<Integer> s = ht.keySet();
	    		int nextIndex = r.nextInt(ht.size());
	    		int index = -1;
	    		int j = 0;
	    		for(Integer k : s){    			
	    			if(j == nextIndex){
	    				index = k;
	    			}
	    			j++;
	    		}
	    		MC4Rek(visits,index, numberOfDocs );
	    	}    	
    	}else{    		
    		MC4Rek(visits,r.nextInt(numberOfDocs), numberOfDocs);
    	}
	}
    
    

    double [] powerIteration(int numberOfDocs){
    	boolean DEBUG = false;
    	Set<Integer> key = link.keySet();
    	double [][] G = new double[numberOfDocs][numberOfDocs];
    	
    	for(int i =0; i< numberOfDocs;++i){
    		Hashtable ht = link.get(i);
    		if( ht != null){
	    		Set<Integer> fisk= ht.keySet();
	    		for(Integer j : fisk){
	    			Boolean b = (Boolean)ht.get(j);
	    			if(b){	    				
	    				G[i][j] = (1.0/out[i])*(1.0-BORED);	    		
	    			}
	    		}
    		}else{
    			
    			double apa = 1.0/(numberOfDocs-1.0)*(1.0-BORED);
    			for(int j = 0; j < numberOfDocs; ++j){
    				if(j != i){
    					G[i][j] = apa;
    				}    				
    			}
    		}
    	}
    	double mean = (1.0*BORED/numberOfDocs);
    	for(int i = 0; i< numberOfDocs; ++i){
    		double sum = 0.0;
    		for(int j= 0; j< numberOfDocs; ++j){
    			G[i][j] += mean;
    			sum +=G[i][j]; 
    		}
    	}
    	 
    	
    	// Power iteration
    	
    	double [] x = new double[numberOfDocs];    	
    	double acc=0.0;
    	for(int i = 0; i < numberOfDocs; ++i){
    		x[i] =  r.nextFloat();
    		acc += x[i];
    	}
    	double tmp = 0.0;
    	for(int i = 0; i < numberOfDocs; ++i){
    		x[i] =  x[i]/acc;
    	
    		
    	}
    	
    	
    	
    	double [] newx = new double[numberOfDocs];
    	
    	for(int k = 0; k< MAX_NUMBER_OF_ITERATIONS  && k < 50;++k){    		
    		mean = 0.0;
    		double meanMEAN = 0.0;
	    	for(int i =0; i< numberOfDocs; ++i){
	    		tmp = 0.0;
	    		for(int j =0 ; j< numberOfDocs; ++j){	    			
	    			tmp += x[j]*G[j][i];
	    		}
	    		newx[i] = tmp;
	    		
	    		mean += tmp;
	    		meanMEAN += x[i];
	    	}
	    	x = newx;
	    	double apa = Math.abs(mean - meanMEAN)/(double)numberOfDocs;
	    	
	    	if( apa < EPSILON && k> 10){
	    		//break; 
	    	}
	    	
    	}
    	return x;
    }


    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
