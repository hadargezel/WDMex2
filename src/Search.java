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


	public LinkedHashMap<String, Double> TA(ArrayList<LinkedHashMap<String, Double>> rankMaps, int k)
	{
		ArrayList<Iterator<Entry<String, Double>>> rankMapIteratorArr = new ArrayList<Iterator<Entry<String, Double>>>(rankMaps.size());
		for (LinkedHashMap<String, Double> rankMap : rankMaps)
			rankMapIteratorArr.add(rankMap.entrySet().iterator());

		HashMap<Integer, Entry<String, Double>> minPerListInSortedAccess = new HashMap<Integer, Entry<String, Double>>(rankMaps.size());
		
		double[] maxRankPerList = new double[rankMaps.size()];
		
		// init threshold 20:00
		double threshold = 0.0;
			for (int i = 0; i < rankMaps.size(); i++)
			{
				LinkedHashMap<String, Double> rankMap = rankMaps.get(i);
				
				double maxRankInList = 0.0;
				if (rankMap.size() > 0)
				{
					maxRankInList = rankMap.get(rankMap.keySet().iterator().next());
					maxRankPerList[i] = maxRankInList;
				}
				threshold += maxRankInList;
			}
		TreeMap<String, String> topK = new TreeMap<String, String>(new LetMeHaveDuplicateDoubleKeysComparator());
		HashMap<Double, Integer> currInstanceOfTotalRank = new HashMap<Double, Integer>();

		// Double[2] : [0] => score, [1] => is in topK
		HashMap<String, Boolean> seenIdsDecisions = new HashMap<String, Boolean>();

		boolean[] isRankListEnded = new boolean[rankMaps.size()];

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
						double total = calculateTotalRank(rankMaps, nextItemWithLowerScore.getKey());
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

							// update threshold if not first id from list HASHUD LOGIC
							if (currMinItem != null)
								threshold = updateThreshold(threshold, rankMaps.size(), i, currMinItem.getValue(), nextItemWithLowerScore.getValue());
						}
					}

				}
				/* we've reached the end of the rank list threshold, so this list can contribute to the total rank only 0 from now on */
				else
				{
					// first time iterator is empty. update potential threshold - this rank list can contribute only 0 to total
					if (!isRankListEnded[i])
					{
						double oldScoreContributedToThreshold = 0.0;
						if (currMinItem != null)
							oldScoreContributedToThreshold = currMinItem.getValue();
						
						threshold = updateThreshold(threshold, rankMaps.size(), i, oldScoreContributedToThreshold, 0.0);
						isRankListEnded[i] = true;
					}
				}
			}
		}
		// when we iterate over it is it insertion order or natural sort?
		LinkedHashMap<String, Double> idWithScoreOrderbyScore = new LinkedHashMap<String, Double>();
		
		for (Entry<String, String> scoreWithId : topK.descendingMap().entrySet())
			idWithScoreOrderbyScore.put(scoreWithId.getValue(), Double.valueOf(scoreWithId.getKey().split("#")[0]));
		
		return idWithScoreOrderbyScore;
	}

	// speceifeic to data ranks etc.
	// aggregation func: [pagerank + avg(each word rank)] / 2 
	private double calculateTotalRank(ArrayList<LinkedHashMap<String, Double>> rankMaps, String id) 
	{
		double totalWordsRank = 0.0;
		double pageRank = 0.0;
		double numOfRanks = rankMaps.size();
		
		for (int i = 0; i < rankMaps.size(); i++)
		{
			Double rankOfId = rankMaps.get(i).get(id);
			
			if (i == 0)
				pageRank = rankOfId;
			else if (rankOfId != null)
				totalWordsRank += rankOfId;
		}
		
		return ( ((totalWordsRank / (numOfRanks-1.0)) + pageRank) / 2.0 );
	}



	// aggregation func: [pagerank + avg(each word rank)] / 2 
	public double updateThreshold(double threshold, double numOfRankMaps, int rankListIndexThatChanged, double oldVal, double newVal)
	{
		// speciefic
		if (rankListIndexThatChanged == 0) //pagerank!
			return (threshold - oldVal/2.0 + newVal/2.0);
		else
			return (threshold - oldVal/(2.0*(numOfRankMaps-1.0)) + newVal/(2.0*(numOfRankMaps-1.0)));
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

		int count = 0;
		while (m.find())
		{
			String url = "http://simple.wikipedia.org" + m.group(2);
			if (url != null)
			{
				p.links.add(url);
				if (!alreadyInQueue.contains(url))
				{
					count++;
					this.crawlingQueue.add(url);
					//this.alreadyInQueue.add(url);
				}
			}
		}
		System.out.println(count);
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
		PageRank pr = new PageRank(s.linksGraph, 0.00005);
		pr.findPR();
		
		String[] queryWords = {"Einstein", "physics"};
		
		ArrayList<LinkedHashMap<String, Double>> rankMaps = new ArrayList<LinkedHashMap<String, Double>>(queryWords.length);
		rankMaps.add(pr.getUrlsWithRanksOrderbyRank());
		
		for (String word : queryWords)
		{
			Word wordInDB = s.words.words.get(word);
			LinkedHashMap<String, Double> rankListOfWord = (wordInDB != null) ? wordInDB.getPageToScoreMapOrderbyScore() : InvertedIndex.getEmptyPageToScoreMap();
			rankMaps.add(rankListOfWord);
		}
		
		LinkedHashMap<String, Double> topUrlsWithScores = s.TA(rankMaps, 5);
		System.out.println("blalalaal");
	}

}
