/*
* Парсер изображений и ссылок с сайта
* @author ApenDover
* telegram @ApenDover
* */


import linkParser.Link;
import linkParser.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main_imageParser {

    /*  Общий подсчет скаченных изображений, включая дубликаты */
    static int globalCount = 0;

    /* Меню, которым прервем бесконеный цикл,
       если пользователь не захочет продолжить указав "yes" */
    static void menu()
    {
        Scanner scanner = new Scanner(System.in);
        boolean k = false;
        while (!k){

            System.out.println("Продолжить? [y/n]");
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("y"))
            {
                k = true;
            }
            else System.exit(1);
        }
    }

    /* Проверка на корректность введенного URL */
    static boolean isURLcorrect(String url)
    {
        try {
            URL u = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("GET");  //OR  huc.setRequestMethod ("HEAD");
            urlConnection.connect();
        } catch (Exception e) {
            System.out.println("Ошибка в загрузке сайта: " + e.getMessage());
            return false;
        }
        return true;
    }

    /* Сохранение изображений */
    static void images(String url, String path) throws IOException {
//          проверим наличие "/" в пути к папке
        if (!(path.charAt(path.length() - 1) == '/')) {
            path = path + "/";
        }

//          создаем каталог
        File file = new File(path);
        if (!file.exists()) {
            boolean rezult = file.mkdirs();
            if (rezult) {
                System.out.println("Создан каталог " + path);
            }
        }

//            глобализируем ссылки, для этого возьмем корень сайта
        StringBuffer buffer = new StringBuffer();
        String[] main = url.split("/", 4);
        buffer.append(main[0]).append("//").append(main[2]);
        String mainurl = buffer.toString();

//            парсим всю страницу по тегу "img", параметрам "src" и "data-src"
        Document doc = Jsoup.connect(url).get();
        AtomicInteger count = new AtomicInteger();
        String finalPath = path;
        ArrayList<Elements> allLinkImagesElements = new ArrayList<>();
        allLinkImagesElements.add(doc.select("img"));
        allLinkImagesElements.add(doc.select("div"));
        ArrayList<String> downloadThis = new ArrayList<>();
        allLinkImagesElements.forEach(elements -> {
            for (Element link : elements) {

//                  Проверим, что выдернутая ссылка начинается с "/", если нет, то добавим. Для всех вариантов тегов.
                String actualLink = null;
                if (!(link.attr("src").isEmpty()) && !(link.attr("src").charAt(0) == '/')) {
                    actualLink = "/" + link.attr("src");
                } else if (!(link.attr("src")).isEmpty()) {
                    actualLink = link.attr("src");
                }

                if (!(link.attr("data-src").isEmpty()) && !(link.attr("data-src").charAt(0) == '/')) {
                    actualLink = "/" + link.attr("data-src");
                } else if (!(link.attr("data-src")).isEmpty()) {
                    actualLink = link.attr("data-src");
                }

                if (!(link.attr("data-original").isEmpty()) && !(link.attr("data-original").charAt(0) == '/')) {
                    actualLink = "/" + link.attr("data-original");
                } else if (!(link.attr("data-original").isEmpty())) {
                    actualLink = link.attr("data-original");
                }

//              исключаем пустую ссылку или метрику Яндекс
                if ((actualLink == null) || (actualLink.isEmpty()) || (actualLink.contains("mc.yandex.ru/"))) {
                    continue;
                }

//              Набор ссылок на изображения для загрузки
                downloadThis.add(mainurl + actualLink);
            }
        });

//      сохраняем изображение по каждой ссылке в папку
        downloadThis.forEach(s -> {
            String[] name = s.split("/");
            String finalName = name[name.length - 1];
            try {
                if (!s.equals(mainurl + "/")) {
                    InputStream inputStream = new URL(s).openStream();
                    Files.copy(inputStream, Paths.get(finalPath + finalName), StandardCopyOption.REPLACE_EXISTING);
                    count.incrementAndGet();
                }
            } catch (IOException e) {
                System.out.println("error: " + e.getMessage() + " в ссылке: " + s);
            }
        });

        globalCount += count.get();

//      отчет в консоль
        System.out.println("Со страницы \"" + url + "\" сохранено " + count + " изображений: " + path + " " + Thread.currentThread().getName());

//      запишем log file
        LogWritter logWritter = new LogWritter(path, downloadThis);
        logWritter.writeList();

    }

    public static void main(String[] args) throws Exception {

        Instant start = null;

        for (;;) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите полный URL адрес, откуда будем парсить картинки: ");
            String url = scanner.nextLine();

//          проверяем корректность ссылки
            if(!isURLcorrect(url)){
                menu();
                continue;
            }

            System.out.println("Введите путь, куда будем сохранять: ");
            String localPath = scanner.nextLine();

//          Модуль парсинга ссылок со всего сайта
            System.out.println("Парсим весь сайт со всеми внутренними ссылками? (исключая внешние) [y/n]");
            String parseall = scanner.nextLine();
            start = Instant.now();

            if (parseall.equalsIgnoreCase("y")) {
                System.out.println("Разложить по папкам?[y/n]");
                String sorted = scanner.nextLine();

//              Запускаем forkJoinPool для рекурсивного парсинга
                Link rootLink = new Link(url);
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                TreeSet<Link> result = forkJoinPool.invoke(new Task(rootLink));
                System.out.println("Всего найдено: " + result.size() + " ссылок");

//              пишем LOG в папку
                LogWritter linkWritter = new LogWritter(localPath, result);
                linkWritter.writeSet();

//              для отслеживания, когда все нити сохранения изобржений отработают
                CountDownLatch latch = new CountDownLatch(result.size());

                ExecutorService executor = Executors.newFixedThreadPool(8);
                ArrayList<Thread> pool = new ArrayList<>();
                for (Link link : result) {
                    pool.add(new Thread(() -> {
                        try {
                            if (sorted.equals("y")){
                                String name = link.getLinkValue().split("/")[link.getLinkValue().split("/").length-1];
                                if (!((localPath.charAt(localPath.length()-1)) == '/')){
                                    images(link.getLinkValue(), localPath + "/" + name);
                                }
                                else images(link.getLinkValue(), localPath + name);
                            }
                            else images(link.getLinkValue(), localPath);

//                          считаем запущенные нити
                            latch.countDown();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }

                for (Thread thread : pool) {
                    executor.submit(thread);
                }
                executor.shutdown();

//              Ждем завершения всех нитей
                latch.await();
            }
            else
                images(url, localPath);

            System.out.println("");
            Instant end = Instant.now();
            System.out.println(Duration.between(start, end));
            System.out.println("Готово. Всего скачено " + globalCount + " изображений (включая дубликаты)");
            menu();

        }
    }
}