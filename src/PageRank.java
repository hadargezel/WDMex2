import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

public class PageRank
{
	double[][] A;
	double[] v;
	WebGraph linksGraph;
	double epsilon;
	HashMap<String, Integer> urlToMatrixIndex;
	HashMap<Integer, String> matrixIndexToUrl;
	
	public PageRank(WebGraph linksGraph, double epsilon) 
	{
		this.linksGraph = linksGraph;
		this.epsilon = epsilon;
		this.initMappings();
		this.initA();
		
		this.v = new double[linksGraph.allPages.size()];
		Arrays.fill(this.v, 1.0);
	}
	
	private void initMappings()
	{
		this.urlToMatrixIndex = new HashMap<String, Integer>();
		this.matrixIndexToUrl = new HashMap<Integer, String>();

		//TODO do we have pages in the graph that we didnt crawl on them?
		int i = 0;
		
		for (Page p : this.linksGraph.allPages)
		{
			this.urlToMatrixIndex.put(p.URL, i);
			this.matrixIndexToUrl.put(i, p.URL);
			i++;
		}

	}
	private void initA()
	{		
		this.A = new double[this.linksGraph.allPages.size()][this.linksGraph.allPages.size()];
		
		//walk on webgraph, fill A
		for (Page p : this.linksGraph.allPages)
		{
			double eachLinkProb = 1 / (p.links.size());
			
			for (String link : p.links)
			{
				Page temp = new Page(link);
				if (this.linksGraph.allPages.contains(temp))
				{
					this.A[this.urlToMatrixIndex.get(link)][this.urlToMatrixIndex.get(p.URL)] = eachLinkProb;
				}
				
			}
				
		}
	}
	
	private double calculateRoundAv()
	{
		double[] oldV = this.v.clone();//check about deep clone
		
		// V[i]
		for (int i = 0; i < v.length; i++)
		{
			double sum = 0;
			
			for (int j = 0; j < this.v.length; j++)
				sum += this.A[i][j]*this.v[j];
			this.v[i] = sum;
		}
		// calculate distance(PAAR) if it will be smaller than epsilon we'll stop in the calling func that using this func
		return calculateDistance(oldV, this.v);
	}
	
	private double calculateDistance(double[] v1, double[] v2)
	{
		if (v1.length != v2.length)
			return -1;
		
		int sumOfPows = 0;
		
		for (int i = 0; i < v1.length; i++)
			sumOfPows += (v1[i]-v2[i])*(v1[i]-v2[i]);
		
		return Math.sqrt(sumOfPows);
	}
	
	public void findPR()
	{
		double distance = Double.POSITIVE_INFINITY;
		
		while (distance >= this.epsilon)
		{
			distance = calculateRoundAv();
		}
	}
	
	public LinkedHashMap<String, Double> getUrlsWithRanksOrderbyRank()
	{
		TreeMap<String, String> rankToUrlMapping = new TreeMap<String, String>(new LetMeHaveDuplicateDoubleKeysComparator());
		LinkedHashMap<String, Double> urlToRankMappingOrderbyRank = new LinkedHashMap<String, Double>();
		HashMap<Double, Integer> currInstanceOfRank = new HashMap<Double, Integer>();
		
		for (int i = 0; i < this.v.length; i++)
		{
			if (!currInstanceOfRank.containsKey(this.v[i]))
				currInstanceOfRank.put(this.v[i], 1);
			else
				currInstanceOfRank.put(this.v[i], currInstanceOfRank.get(this.v[i]) + 1);
			
			String keyWithInstanceCounting = String.valueOf(this.v[i]) + '#' + String.valueOf(currInstanceOfRank.get(this.v[i]));
			rankToUrlMapping.put(keyWithInstanceCounting, this.matrixIndexToUrl.get(i));
		}
		
		// reverse the key-value while keeping order (DESC sorted by the original key == rank)
		for (Entry<String, String> rankToUrl : rankToUrlMapping.descendingMap().entrySet())
			urlToRankMappingOrderbyRank.put(rankToUrl.getValue(), Double.valueOf(rankToUrl.getKey().split("#")[0]));
			
		return urlToRankMappingOrderbyRank;
	}
}

