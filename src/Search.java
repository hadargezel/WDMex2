import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Search
{
	private String startingURL;
	private WebGraph linksGraph;
	private InvertedIndex words;
	private Queue<String> crawlingQueue = new LinkedList<String>();
	private Set<String> alreadyInQueue = new TreeSet<String>();

	
	public Search(String startingURL)
	{
		this.startingURL = startingURL;
		this.linksGraph = new WebGraph(new Page(this.startingURL));
		this.words = new InvertedIndex();
		this.crawlingQueue = new LinkedList<String>();
		this.alreadyInQueue = new TreeSet<String>();
	}
	
	public void crawl(int crawlPagesLimit)
	{
		int counter = 0;
		String nextCrawlURL = this.startingURL;
		alreadyInQueue.add(nextCrawlURL);
		
		while (nextCrawlURL != null && counter <= crawlPagesLimit )
		{
			String content = fetchPage(nextCrawlURL);
			
			if (!content.isEmpty())
			{
				Page p = new Page(nextCrawlURL);
				addLinksToQueueAndToPage(content, p);
				// 18.004
				//this.linksGraph.allPages.add(p);
				this.linksGraph.addNewPage(p);
				/* 
				 * words shit patrsing
				 */
			}
			
			counter++;
			nextCrawlURL = crawlingQueue.poll();
		}
	}
	public static void main(String[] args)
	{
		Search s = new Search("http://simple.wikipedia.org/wiki/Albert_einstein");
	}


	public void addLinksToQueueAndToPage(String content, Page p)
	{
		String pattern = "(href=\"(\\/wiki\\/.*?)\")";
		Pattern pat = Pattern.compile(pattern);
		Matcher m = pat.matcher(content);
		
		while (m.find())
		{
			String url = "http://simple.wikipedia.org/" + m.group(2);
			p.links.add(url);
			if (!alreadyInQueue.contains(url))
			{
				this.crawlingQueue.add(url);
				this.alreadyInQueue.add(url);
			}
		}
	}

	public String fetchPage(String urlAddress)
	{
		URL url;
		InputStream inputStream = null;
		BufferedReader bufferedReader;
		String content="";
		String line;
		try {
			url = new URL(urlAddress);
			inputStream = url.openStream();  // throws an IOException
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = bufferedReader.readLine()) != null) {
				content+=line;
			}
		}
		catch (MalformedURLException mue) 
		{
			mue.printStackTrace();
		}
		catch (IOException ioe) 
		{
			ioe.printStackTrace();
		} 
		finally 
		{
			try 
			{
				if (inputStream != null) inputStream.close();
			}
			catch (IOException ioe) 
			{
				ioe.printStackTrace();
			}

		}
		return content;
	}
}
