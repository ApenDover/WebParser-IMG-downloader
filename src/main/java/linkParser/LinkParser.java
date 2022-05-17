package linkParser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkParser {
    String linkPath; //переданная ссылка в Parser
    static String linkGlobal; //Глобальная начальная ссылка
    int step;

//    String linkBP; // переданная ссылка без "/" в конце
    TreeSet<Link> linkSet = new TreeSet<>();

    public LinkParser(Link link)
    {

        linkPath = link.getLinkValue();
        linkGlobal = link.getLinkValue().split("/")[0] + "//" + link.getLinkValue().split("/")[2]; //https://envylab.ru
        step = link.getLinkValue().split("/").length;

    }

    public TreeSet<Link> call() throws Exception {

        System.out.println("СКАНИРУЮ ССЫЛКУ " + linkPath + "  поток: " + Thread.currentThread().getName());

//          TIMEOUT чтобы нас сервер не заблокировал за частые подключения
        TimeUnit.MILLISECONDS.sleep(150);

        Pattern pattern1 = Pattern.compile("^/"); // выдернем все ссылки локальные
        Pattern pattern2 = Pattern.compile("^" + linkGlobal + "/"); // выдернем все ссылки глобальные

        try {
            Document doc = Jsoup.connect(linkPath).get(); //подключаемся к ссылке
            Elements linkFromPage = doc.select("a"); //выделяем тег <a>

            //заполним linksSet уникальными ссылками с этой страницы
            //пройдем по каждому элементу

            for (Element link : linkFromPage) {
                //далее сверка с шаблоном

                Matcher matcher1 = pattern1.matcher(link.attr("href")); // в каждом теге <a> находим параметр "href"
                if (matcher1.find()) {

                    if (link.attr("href").equals("/")) {
//                          если ссылка пустая (на саму себя) - пропускаем
                        continue;
                    } else {
                        String a = linkPath.split("/")[linkPath.split("/").length - 1];
                        String b = link.attr("href").split("/")[1];
                        if (Objects.equals(a, b)) {
                            StringBuilder buffer = new StringBuilder(); // в буффер запишем ссылку без совпадающего первого куска
                            String k = link.attr("href");
                            int count = a.length() + 1;
                            for (int l = count; l < k.length(); l++) {
                                buffer.append(k.charAt(l));
                            } //убираем первый кусок. Пояснение:
                            /*
                            * в ссылке "http://сайт.com/privet/popa" будет ссылка "/popa/podstava"
                            * мы просто убираем один кусок "popa". Это странно, но видимо попался
                            * подобный кривой сайт. Исключаем этот момент
                            * */

                            Jsoup.connect(linkGlobal + buffer).get(); //проверяем рабочая ли ссылка
                            linkSet.add(new Link(linkGlobal + buffer));
                            continue;
                        } else {
                            try {
                                String thisLink = linkGlobal + link.attr("href");
                                if (thisLink.split("/").length > step) {
                                    TimeUnit.MILLISECONDS.sleep(150);
                                    Jsoup.connect(thisLink).get(); //проверяем рабочая ли ссылка
                                    linkSet.add(new Link(linkGlobal + link.attr("href")));
                                } else {
                                    continue;
                                }
                            } catch (Exception e) {
                                System.out.println("99link: " + e.getMessage() + " в " + link.attr("href"));
                            }
                        }
                    }
                }

                Matcher matcher2 = pattern2.matcher(link.attr("href"));
                if (matcher2.find()) {
                    try {
                        String thisLink = link.attr("href");
                        if (thisLink.split("/").length > step) {
                            TimeUnit.MILLISECONDS.sleep(150);
                            Jsoup.connect(thisLink).get();
                            linkSet.add(new Link(link.attr("href")));
                        }

                    } catch (Exception e) {
                        System.out.println("112e: " + e.getMessage() + " в " + link.attr("href"));
                    }
                }
            }
        } catch (HttpStatusException o) {
            System.out.println("121o: " + o.getMessage());
        }
        return linkSet;
    }
}
