package linkParser;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Task extends RecursiveTask<TreeSet<Link>> {
    Link mainLink;

    public Task(Link link){
        mainLink = link;
    }

    @Override
    protected TreeSet<Link> compute() {

        System.out.print("###### ");

        TreeSet<Link> taskRezult = new TreeSet<>();

        AtomicBoolean flag = new AtomicBoolean(true);

        if (ListOfLinks.listOfLinks.contains(mainLink)){
            flag.set(false);
            System.out.println(mainLink.getLinkValue() + " УЖЕ СКАНИРОВАЛАСЬ");
        }
        if (flag.get()) {
            ListOfLinks.listOfLinks.add(mainLink);
            TreeSet<Link> underLinkParser;
            LinkParser linkParser = new LinkParser(mainLink);
            try { underLinkParser = linkParser.call();
            } catch (Exception e) {throw new RuntimeException(e);}
            mainLink.setUnderLinks(underLinkParser);
            taskRezult.addAll(mainLink.getUnderLinks());

            ArrayList<Task> tasks = new ArrayList<>();

            for(Link link : mainLink.getUnderLinks())
            {
                Task task = new Task(link);
                task.fork();
                tasks.add(task);
            }

            for (Task task : tasks) {
                try {
                    taskRezult.addAll(task.join());
                }
                catch (Exception e)
                {
                    if (!Objects.equals(e.getMessage(), "null")){
                    System.out.println("53_task: ОШИБКА = ERROR  " + e.getMessage());
                    }
                }
            }

        }
            return taskRezult;
    }
}
