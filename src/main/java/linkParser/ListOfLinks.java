package linkParser;

import java.util.TreeSet;

public class ListOfLinks {
    public static TreeSet<Link> listOfLinks = new TreeSet<>();

    public static TreeSet<Link> getListOfLinks() {
        return listOfLinks;
    }

    public static void setListOfLinks(TreeSet<Link> list) {
        listOfLinks.addAll(list);
    }
}
