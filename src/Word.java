import java.util.HashMap;


public class Word
{
	String word;
	HashMap<String, Double> pageUrlToScore;
	
	public Word(String word) 
	{
		this.word = word;
		this.pageUrlToScore = null;
	}
	
	public void initPageUrlToScore()
	{
		this.pageUrlToScore = new HashMap<String,Double>();
	}
}
