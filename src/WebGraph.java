import java.util.TreeSet;


public class WebGraph 
{
	Page root;
	TreeSet<Page> allPages;
	
	public WebGraph(Page root) 
	{
		this.root = root;
		this.allPages = new TreeSet<Page>(new pageComperator());
	}

	public void addPage(Page referrer, String URL){
		Page p = new Page(URL);
		if (!this.allPages.contains(p))
		{
			p.initLinks();
			allPages.add(p);
		}
		referrer.links.add(p);
		p.links.add(referrer);
	}
	
	
}
