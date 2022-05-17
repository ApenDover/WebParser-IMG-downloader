import linkParser.Link;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TreeSet;

public class LogWritter {

    private String path;
    private TreeSet<Link> setToWrite;
    private ArrayList<String> listToWrite;

//    Этот конструктор, если на запись в файл передаем treeSet.
//    Он используется при парсинге общих ссылок Link
    public LogWritter(String path, TreeSet<Link> setToWrite) {
        if (!(path.charAt(path.length()-1) == '/')){
        this.path = path + '/';
        }
        else this.path = path;
        this.setToWrite = setToWrite;
    }

//    Этот конструктор, если на запись в файл передаем ArrayList.
//    Он используется при парсинге ссылок на изображения
    public LogWritter(String path, ArrayList<String> listToWrite) {
        if (!(path.charAt(path.length()-1) == '/')){
            this.path = path + '/';
        }
        else this.path = path;
        this.listToWrite = listToWrite;
    }


    public void writeSet() throws FileNotFoundException {
        String fileName = "Links LOG " + Instant.now().toString() + ".txt";
        File dirFile = new File(path);
        if (dirFile.mkdir())
        {
            System.out.println("Создана дирректория " + path);
        }
        File file = new File(path + fileName);
        PrintWriter writer = new PrintWriter(file);
        System.out.println("Создан файл со всеми ссылками: " + path + fileName);
        setToWrite.forEach(s -> writer.write("\t".repeat(s.getLinkValue().split("/").length - 3) + s.getLinkValue() + "\n"));
        writer.flush();
    }

    public void writeList() throws FileNotFoundException {
        File log = new File(path + "Image LOG.txt");
        try {
            log.createNewFile();
            java.io.FileWriter writer = new java.io.FileWriter(log, true);
            listToWrite.forEach(s -> {
                try {
                    writer.write(s);
                    writer.append('\n');
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
