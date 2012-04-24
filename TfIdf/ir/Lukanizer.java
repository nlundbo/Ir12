import java.io.*;
public class Lukanizer{

	private static String readFileAsString(String filePath) {
		try{
	    	byte[] buffer = new byte[(int) new File(filePath).length()];
	    	BufferedInputStream f = null;
	    	try {
	        	f = new BufferedInputStream(new FileInputStream(filePath));
	        	f.read(buffer);
	    	} finally {
	        	if (f != null) try { f.close(); } catch (IOException ignored) { }
	    	}
	    	return new String(buffer);
		}catch(Exception e){
			
		}
		return "";
	}

	public static void main(String args []){
		String s = readFileAsString("../50/1.txt");
		String ss="";
		for( int i =0; i< s.length(); ++i){
			         
		}
		System.out.println(s);
	}
}