import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
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


	public TreeMap<String, String> TA(ArrayList<LinkedHashMap<String, Double>> rankMaps, int k)
	{
		/*
		// can implement comparator inside the class (?) we did it in soft1...(?)
		class IdDataStruct implements Comparator<IdDataStruct>
		{
			private Double scoreAfterAggregation;
			private boolean inTopK;

			public int compare(IdDataStruct id1, IdDataStruct id2) 
			{
				return id1.scoreAfterAggregation.compareTo(id2.scoreAfterAggregation);
			}
		}
		 */

		ArrayList<Iterator<Entry<String, Double>>> rankMapIteratorArr = new ArrayList<Iterator<Entry<String, Double>>>(rankMaps.size());
		for (LinkedHashMap<String, Double> rankMap : rankMaps)
			rankMapIteratorArr.add(rankMap.entrySet().iterator());

		HashMap<Integer, Entry<String, Double>> minPerListInSortedAccess = new HashMap<Integer, Entry<String, Double>>(rankMaps.size());

		double threshold = Double.POSITIVE_INFINITY;

		TreeMap<String, String> topK = new TreeMap<String, String>(new LetMeHaveDuplicateDoubleKeysComparator());
		HashMap<Double, Integer> currInstanceOfTotalRank = new HashMap<Double, Integer>();

		// Double[2] : [0] => score, [1] => is in topK
		HashMap<String, Boolean> seenIdsDecisions = new HashMap<String, Boolean>();

		while ((topK.size() < k) || (Double.valueOf(topK.firstKey().split("#")[0]) < threshold))
		{
			// round of "parallel" sorted access to all rank lists
			for (int i = 0; i < rankMaps.size(); i++)
			{
				Entry<String, Double> currMinItem = minPerListInSortedAccess.get(i);
				Entry<String, Double> nextItemWithLowerScore = null;

				Iterator<Entry<String, Double>> rankListIterator = rankMapIteratorArr.get(i);
				if (rankListIterator.hasNext())
				{
					// walk down on the i-th ranks list
					nextItemWithLowerScore = rankListIterator.next();
					// update the lists minimum items hash table
					minPerListInSortedAccess.put(i, nextItemWithLowerScore);

					// if item not already seen
					if (!seenIdsDecisions.containsKey(nextItemWithLowerScore.getKey()))
					{
						// now its seen
						seenIdsDecisions.put(nextItemWithLowerScore.getKey(), false);

						// calculate total rank
						double total = 0;
						// add to topK if there aren't K items yet OR if its score is better than the lowest score

						boolean lessthanK = (topK.size() < k);
						if (lessthanK || (Double.valueOf(topK.firstKey().split("#")[0]) < total) )
						{
							// remove lowest item (we're replacing it with the better-total item)
							if (!lessthanK)
								topK.pollFirstEntry();

							if (!currInstanceOfTotalRank.containsKey(total))
								currInstanceOfTotalRank.put(total, 1);
							else
								currInstanceOfTotalRank.put(total, currInstanceOfTotalRank.get(total + 1));

							String keyWithInstanceCounting = String.valueOf(total) + '#' + String.valueOf(currInstanceOfTotalRank.get(total));
							topK.put(keyWithInstanceCounting, nextItemWithLowerScore.getKey());

							// mark that it's taken
							seenIdsDecisions.put(nextItemWithLowerScore.getKey(), true);

							// update threshold
							threshold = updateThreshold(threshold, rankMaps.size(), i, currMinItem.getValue(), nextItemWithLowerScore.getValue());
						}
					}

				}

				/* we've reached the end of the rank list     threshold ? and what else */
				else
				{

				}


			}
			/* there's some item that is the "lowest" in its rank list and it haven't been taken to the topK */
			/*else
				{

				}*/
		
		}
		return topK;
}

// aggregation func: [pagerank + avg(each word rank)] / 2 
public double updateThreshold(double threshold, int numOfRankMaps, int rankListIndexThatChanged, double oldVal, double newVal)
{
	// speciefic
	if (rankListIndexThatChanged == 0) //pagerank!
		return (threshold - oldVal/2 + newVal/2);
	else
		return (threshold - oldVal/(2*(numOfRankMaps-1)) + newVal/(2*(numOfRankMaps-1)));
}

public void crawl(int crawlPagesLimit)
{
	int counter = 0;
	String nextCrawlURL = this.startingURL;
	this.crawlingQueue.add(nextCrawlURL);

	while ((nextCrawlURL = crawlingQueue.poll()) != null && counter <= crawlPagesLimit )
	{
		System.out.println(nextCrawlURL);
		if (!alreadyInQueue.contains(nextCrawlURL))
		{
			String content = fetchPage(nextCrawlURL);

			if (!content.isEmpty())
			{
				Page p = new Page(nextCrawlURL);
				this.linksGraph.addNewPage(p);
				addLinksToQueueAndToPage(content, p);
				// 18.004
				//this.linksGraph.allPages.add(p);

				PageWordsParser parser = new PageWordsParser(nextCrawlURL, content);
				parser.extractWords();
				parser.calculateScores();
				parser.addWordsAndScoresToInvertedIndex(this.words);
			}
			this.alreadyInQueue.add(nextCrawlURL);
			counter++;
		}
	}
}

public void addLinksToQueueAndToPage(String content, Page p)
{
	String pattern = "(href=\"(\\/wiki\\/.*?)\")";
	Pattern pat = Pattern.compile(pattern);
	Matcher m = pat.matcher(content);

	while (m.find())
	{
		String url = "http://simple.wikipedia.org/" + m.group(2);
		if (url != null)
		{
			p.links.add(url);
			if (!alreadyInQueue.contains(url))
			{
				this.crawlingQueue.add(url);
				//this.alreadyInQueue.add(url);
			}
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

public static void main(String[] args)
{
	Search s = new Search("http://simple.wikipedia.org/wiki/Albert_einstein");
	s.crawl(20);
	//s.linksGraph.allPages
	PageRank pr = new PageRank(s.linksGraph, 1);
	pr.findPR();
	System.out.println("blalalaal");
}

}
