package ir;

import java.io.*;
import java.util.HashSet;

public class StopWord{

	private HashSet<String> stopWord = new HashSet<String>();

	public StopWord(){
		readFile("Stopwords.txt");
	}

	public StopWord(String dirName){
		readFile(dirName);
	}

	public boolean isStopWord(String s){
		return stopWord.contains(s);
	}

	private void readFile(String stopWordDir){
		try {
			FileInputStream fstream = new FileInputStream(stopWordDir);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				stopWord.add(strLine);
			}
			in.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());

		}

	}


}
