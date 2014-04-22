import java.util.TreeMap;

public class Word
{
	String word;
	TreeMap<String, Double> pageUrlToScore;
	
	public Word(String word) 
	{
		this.word = word;
		this.pageUrlToScore = null;
	}
	
	public void initPageUrlToScore()
	{
		this.pageUrlToScore = new TreeMap<String,Double>();
	}
}
