package linkParser;

import java.util.TreeSet;

public class Link implements Comparable<Link>{
    private final String linkValue;
    private TreeSet<Link> underLinks;

    public Link(String link) throws Exception {
        this.linkValue = link;
    }

    public String getLinkValue() {
        return linkValue;
    }

    public TreeSet<Link> getUnderLinks() {
        return underLinks;
    }
    public void setUnderLinks(TreeSet<Link> underLinks) {
        this.underLinks = underLinks;
    }

    @Override
    public int compareTo(Link link) {
        return getLinkValue().compareTo(link.getLinkValue());
    }
}
