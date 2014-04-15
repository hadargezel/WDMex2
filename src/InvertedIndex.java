import java.util.TreeSet;


public class InvertedIndex 
{
	TreeSet<Word> words;
	
	public InvertedIndex()
	{
		this.words = new TreeSet<Word>(new WordComparator());
	}
	
	public void addWord(Word word)
	{
		word.initPageUrlToScore();
		this.words.add(word);
	}
}
