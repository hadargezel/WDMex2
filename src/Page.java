import java.util.TreeSet;


public class Page{
	String URL;
	TreeSet<String> links;
	
	
	public Page(String URL)
	{
		this.URL = URL;
		this.links = null;
	}
	
	public void initLinks()
	{
		this.links = new TreeSet<String>();
	}
	
	
}

