import java.util.TreeSet;


public class Page{
	String URL;
	TreeSet<Page> links;
	
	
	public Page(String URL) {
		this.URL = URL;
		this.links = null;
	}
	
	public void initLinks()
	{
		this.links = new TreeSet<Page>(new pageComperator());
	}
	
	
}

