
public class PageRank
{
	double[][] A;
	double[] v;
	WebGraph wg;
	double epsilon;
	
	public PageRank(WebGraph wg, double epsilon) 
	{
		this.wg = wg;
		this.epsilon = epsilon;
		this.A = this.initA();
	}
	
	public double[][] initA()
	{
		//walk on webgraph, fill A
	}
	
	public double calculateRoundAv()
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
	
	public double calculateDistance(double[] v1, double[] v2)
	{
	}
	
	public void findPR()
	{
		double distance = INF;
		while distance >= epsilon
		{
			distance = calculateRoundAv();
		}
	}
	
}

