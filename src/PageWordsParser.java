import java.util.ArrayList;
import java.util.TreeSet;


public class PageWordsParser
{
	private String content;
	private ArrayList<String> uniqueWords;
	private String[] allWords;
	private int[] scores;
	
	public PageWordsParser(String content)
	{
		this.content = content.toLowerCase();
		this.uniqueWords = null;
		this.allWords = null;
		this.scores = null;
	}
	
	public void extractWords()
	{
		TreeSet<String> uniqueWordsSet = new TreeSet<String>();
		this.allWords = this.content.split(" ");
		
		for (String word : this.allWords)
		{
			if (!word.isEmpty()) // do we really need this? aka if we split "     " with " " we get empty strings or nothing?
			{
				uniqueWordsSet.add(word);
			}
		}
		
		this.uniqueWords = new ArrayList<>(uniqueWordsSet);
	}
	
	public void calculateScores()
	{
		this.scores = new int[this.uniqueWords.size()];
		
		for (String word : this.allWords)
		{
			if (!word.isEmpty())
			{
				int wordIndex = this.uniqueWords.indexOf(word);
				this.scores[wordIndex]++;
			}
		}
		
		for (int i = 0; i < this.scores.length; i++)
		{
			this.scores[i] /= this.allWords.length;
		}
	}
	
	
}
