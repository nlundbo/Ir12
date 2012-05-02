package ir;
import java.util.*;


public class WordVector{
	public short [] v;
	private final int NUM_ONES = 8; // Must be 2-tupel
	private final int K = 1000;	
	private boolean isSparse = false;

	private ArrayList<Short> generateUniquePos(Random rnd){
		ArrayList<Short> out = new ArrayList<Short>(K);
		for(int i =0; i< NUM_ONES; ++i){
			int r = rnd.nextInt(K);
			while( out.contains(r)){				
				r = rnd.nextInt(K);				
			}			
			out.add((short)r);
		}
		return out;
	}


	public void add(WordVector wv){		
		for(int i =0; i< wv.size(); ++i){
			v[i] += wv.v[i];
		}
	}
	private short sign(short i){
		return (i>=0)?(short)1:(short)-1;
	}

	public void addSparse(WordVector sparseWV){
		for(int i = 0 ; i< sparseWV.size(); ++i){
			short tmp = sparseWV.v[i];
			v[ sign(tmp)*tmp ] += sign(tmp);
		}
	}

	private WordVector addToNew(WordVector b){		
		WordVector out = new WordVector();
		for(int i =0; i< b.size(); ++i){
			out.v[i] = (short) (this.v[i] + b.v[i]);
		}
		return out;
	}

	public int size(){		
		return v.length;
	}

	public double getDistance(WordVector b){
		return getEuclidianDistance(b);
	}

	private double getEuclidianDistance(WordVector b){
		double sum =0.0;
		double tmp; 
		for(int i =0; i< size(); ++i){
			tmp = v[i] - b.v[i];
			sum += tmp*tmp;
		}
		return Math.sqrt(sum);
	}

	private double getScalarProduct(WordVector b){
		double sum =0.0;
		for(int i =0; i < size(); ++i)
			sum += v[i]*b.v[i];
		
		return sum;
	}

	

	public WordVector(){		
		isSparse = false;
		v = new short[K];
		for(int i =0; i< K; ++i)
			v[i] = 0;
	}

	public WordVector(Random rnd){
		isSparse = true;
		v = new short[NUM_ONES];	
		ArrayList<Short> pos = generateUniquePos(rnd);
		for(int i =0; i< pos.size(); ++i){
			v[i] = (i>NUM_ONES/2)?(short) (-pos.get(i) ): (short) (pos.get(i));
		}
	}

	public static void main(String [] args){
		Random rnd = new Random();
		WordVector a = new WordVector(rnd);
		System.out.println(a);
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i =0; i< v.length; ++i){
			if(i != v.length -1){
				sb.append(v[i]+", ");
			}else{
				sb.append(v[i]+"]");
			}
		}
		return sb.toString();
	}
}	