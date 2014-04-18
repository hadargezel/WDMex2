import java.util.TreeSet;


public class WebGraph 
{
	Page root;
	TreeSet<Page> allPages;
	
	public WebGraph(Page root) 
	{
		this.root = root;
		this.allPages = new TreeSet<Page>(new PageComparator());
	}

	// 18.04 aner
	public void addNewPage(Page p)
	{
		p.initLinks();
		this.allPages.add(p);
	}
	
	/* 18.04 aner
	public void addPage(Page referrer, String URL){
		Page p = new Page(URL);
		if (!this.allPages.contains(p))
		{
			p.initLinks();
			allPages.add(p);
		}
		
		referrer.links.add(p); //need to fix accroding to new logic (you don't necessarly put a link on both just on one.)
		p.links.add(referrer);//need to fix accroding to new logic (you don't necessarly put a link on both just on one.)
	}
	*/
	
}
