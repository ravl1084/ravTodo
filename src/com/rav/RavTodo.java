package com.rav;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Main class implementing the different actions needed to use and manage a Todo.txt file.
 */
public class RavTodo {
    private Properties properties;

    private static ArrayList<RavTodoItem> todoList = new ArrayList<>();

    public static void main(String args[]) {
        RavTodo todo = new RavTodo();
        String numString = "\\d+";
        String schedString = "(\\d+) (\\d{4}-\\d{2}-\\d{2})";
        Pattern schedRegex = Pattern.compile(schedString);
        Pattern numRegex = Pattern.compile(numString);
        Matcher matcher;
        if (args.length > 0){
            switch (args[0]) {
                case "ls":
                    if (args.length > 1) {
                        String[] terms = Arrays.copyOfRange(args, 1,args.length);
                        todo.listTodoItems(terms);
                    } else {
                        todo.listAllTodoItems();
                    }
                    break;

                case "do":
                    if (args.length > 1) {
                        matcher = numRegex.matcher(args[1]);
                        if (matcher.find()){
                            try {
                                todo.doTask(Integer.valueOf(args[1]));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        todo.printUsage();
                    }
                    break;

                case "archive":
                    try {
                        todo.archiveTasks();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "add":
                    if (args.length > 1) {
                        String[] terms = Arrays.copyOfRange(args, 1, args.length);
                        String taskString = "";
                        for (int i = 0; i < terms.length; i++) {
                            taskString += terms[i] + " ";
                        }
                        try {
                            todo.addTask(new RavTodoItem(999, taskString));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        todo.printUsage();
                    }
                    break;

                case "next":
                    try {
                        todo.findNextActions();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "process":
                    try {
                        todo.processInbox();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "j":
                    if (args.length > 1) {
                        String[] terms = Arrays.copyOfRange(args, 1, args.length);
                        String journalString = "";
                        for (int i = 0; i < terms.length; i++) {
                            journalString += terms[i] + " ";
                        }
                        try {
                            todo.addJournalEntry(journalString);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        todo.printUsage();
                    }
                    break;

                case "vj":
                    if (args.length > 1) {
                        matcher = numRegex.matcher(args[1]);
                        if (matcher.find()){
                            try {
                                todo.showJournalEntries(Integer.valueOf(args[1]));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //TODO: implement text search of journal
                    } else {
                        todo.printUsage();
                    }
                    break;

                case "schedule":
                    if (args.length > 1) {
                        String[] terms = Arrays.copyOfRange(args, 1, args.length);
                        String checkString = "";
                        for (int i = 0; i < terms.length; i++) {
                            checkString += terms[i] + " ";
                        }
                        matcher = schedRegex.matcher(checkString);
                        if (matcher.find()) {
                            try {
                                todo.scheduleTask(Integer.valueOf(matcher.group(1)), matcher.group(2));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            todo.printUsage();
                        }
                    } else {
                        todo.printUsage();
                    }
                    break;
                //TODO: implement pri command

                default: {
                    System.out.println("Unknown command.");
                    todo.printUsage();
                }

            }

        } else {
            todo.printUsage();
        }
    }

    private void scheduleTask(Integer idx, String dt) throws IOException {
        RavTodoItem task = null;
        try {
            task = getTask(idx);
        } catch (RavTodoNotFoundException e) {
            System.out.println("Task not found.");
            e.printStackTrace();
        }
        LocalDate newDate = LocalDate.parse(dt);
        task.setThresholdDate(newDate);
        writeTodoFile();

        System.out.println(task.getRawLine());
        System.out.println("Task scheduled for " + newDate);

    }

    public RavTodo() {
        this.properties = new Properties();
        try {
            readConfig();
            readTodoFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method lists all the todo items in the file that have met their threshold date and are not complete.
     */
    private void listAllTodoItems() {
        Collections.sort(todoList, RavTodoItem.PriDueComparator);
        LocalDate today = LocalDate.now();
        for (RavTodoItem t : todoList) {
            if (!t.getThresholdDate().isAfter(today) && !t.isTodoComplete()) {
                t.displayItem();
            }
        }
    }

    /**
     * This method filters the todo list by the search terms provided and lists the matching todos.
     *
     * @param terms A String array of all the search terms to filter the todo list
     */
    private void listTodoItems(String[] terms) {
        Collections.sort(todoList, RavTodoItem.PriDueComparator);
        for (RavTodoItem t : todoList){
            if (t.matchesTerms(terms) && t.hasMetThreshold()){
                t.displayItem();
            }
        }
    }

    /**
     * This method looks for the RavTodo config file in the HOME directory and loads it if found.
     *
     * @throws IOException
     */
    public void readConfig() throws IOException{
        String pathDirs = System.getenv("HOME");
        if (pathDirs.matches(":")) {
            pathDirs += "\\";
        } else {
            pathDirs += "/";
        }
        String fileName = "ravTodo.conf";

        InputStream inputStream = new FileInputStream(pathDirs + fileName);

        this.properties.load(inputStream);
    }

    /**
     * This method returns the path to the todo file set in the config file.
     *
     * @return the path defined in the config file for the todo.txt file
     */
    public String getConfigPath(){
        String path = properties.getProperty("todo.path");
        if (path.matches(":")) {
            path += "\\";
        } else {
            path += "/";
        }
        return path;
    }

    /**
     * This method looks for the todo file in the config and loads each item into a RavTodoItem object
     *
     * @throws IOException
     */
    public void readTodoFile() throws IOException{
        File todoFile = new File(getConfigPath() + "todo.txt");
        Scanner todoReader = new Scanner(todoFile);
        int lineNum = 1;

        while (todoReader.hasNext()){
            String taskLine = todoReader.nextLine();
            todoList.add(new RavTodoItem(lineNum, taskLine));
            lineNum++;
        }

        todoReader.close();
    }

    /**
     * @return the amount of todos in the list
     */
    public int getTodoCount() {
        return todoList.size();
    }

    /**
     * This method prints the available commands
     */
    public void printUsage(){
        System.out.println("Usage: ravtodo [command] <arguments>");
        System.out.println("Available commands:");
        System.out.format("%10s %s%n", "ls", "List todos");
        System.out.format("%10s %s%n", "add <text>", "Add a todo");
        System.out.format("%10s %s%n", "do <id>", "Mark a todo as complete");
        System.out.format("%10s %s%n", "archive", "Archive completed todos into done.txt");
        System.out.format("%10s %s%n", "schedule <id> <YYYY-MM-DD>", "Set threshold date for todo");
        System.out.format("%10s %s%n", "next", "Scan outlines for next actions");
        System.out.format("%10s %s%n", "process", "Process GTD inbox");
        System.out.format("%10s %s%n", "j <text>", "Add journal entry");
        System.out.format("%10s %s%n", "vj <text>", "View journal entries, if a number will show the latest n");
    }


    /**
     * This method marks the provided task as complete.
     *
     * @param n the index of the task to mark as complete.
     * @throws IOException
     */
    public void doTask(int n) throws IOException {
        try {
            //System.out.println("Searching for todo " + n);
            RavTodoItem t = getTask(n);
            if (t.isRecurrence()){
                addTask(t.createNext());
            }
            t.markComplete();
        } catch (RavTodoNotFoundException e) {
            System.out.println("Index not found in Todo file.");
        }
        writeTodoFile();
    }

    /**
     * This method adds a RavTodoItem object to the existing list.
     *
     * @param t the RavTodoItem object to add to the list
     * @throws IOException
     */
    public void addTask(RavTodoItem t) throws IOException {
        todoList.add(t);
        writeTodoFile();
    }

    /**
     * This method writes all the RavTodoItem objects into the todo.txt file in the standard format
     *
     * @throws IOException
     */
    public void writeTodoFile() throws IOException {
        File todoFile = new File(getConfigPath() + "todo.txt");
        Path todopath = todoFile.toPath();
        Files.move(todopath, todopath.resolveSibling("todo.bak"), REPLACE_EXISTING);
        File newFile = new File(getConfigPath() + "todo.txt");
        PrintWriter out = new PrintWriter(newFile);
        Iterator<RavTodoItem> iter = todoList.iterator();
        while (iter.hasNext()){
            out.println(iter.next().getRawLine());
        }
        out.close();
    }

    /**
     * This method returns the RavTodoItem that matches the index provided.
     *
     * @param i the index of the todo item been requested
     * @return the RavTodoItem with the provided index
     * @throws RavTodoNotFoundException
     */
    public RavTodoItem getTask(int i) throws RavTodoNotFoundException {
        Iterator<RavTodoItem> iter = todoList.iterator();
        RavTodoItem result = null;
        while (iter.hasNext()) {
            result = iter.next();
            if (result.getIndex() == i){
                break;
            }
        }
        if (result != null) {
            return result;
        } else {
            throw new RavTodoNotFoundException();
        }
    }

    /**
     * This method backs up both the todo.txt file and the done.txt file and then moves the completed
     * todo items to done.txt.
     *
     * @throws IOException
     */
    public void archiveTasks() throws IOException {
        File todoFile = new File(getConfigPath() + "todo.txt");
        Path todopath = todoFile.toPath();
        Files.move(todopath, todopath.resolveSibling("todo.bak"), REPLACE_EXISTING);
        File file = new File(getConfigPath() + "done.txt");
        todopath = file.toPath();
        Files.copy(todopath, todopath.resolveSibling("done.bak"), REPLACE_EXISTING);
        FileWriter newFile = new FileWriter(getConfigPath() + "todo.txt");
        FileWriter doneFile = new FileWriter(getConfigPath() + "done.txt", true);
        PrintWriter outDone = new PrintWriter(doneFile);
        PrintWriter outTodo = new PrintWriter(newFile);

        Iterator<RavTodoItem> iter = todoList.iterator();
        RavTodoItem t = null;
        int n = 0;
        while (iter.hasNext()){
           t = iter.next();
           if (t.isTodoComplete()) {
               outDone.println(t.getRawLine());
               n++;
           } else {
               outTodo.println(t.getRawLine());
           }
        }

        System.out.println("Archived " + n + " tasks.");
        outDone.close();
        outTodo.close();
    }

    /**
     * This method looks for any outline in the todo folder and if there are no active tasks in the todo.txt
     * file, proceeds to add the next item per the outline.
     *
     * @throws IOException
     */
    public void findNextActions() throws IOException {
        File path = new File(properties.getProperty("todo.path"));
        String[] outlines = path.list((lamFolder, lamName) -> lamName.matches(".*\\.ol\\.txt"));
        String extension = "(.*)\\.ol\\.txt";
        Pattern regex = Pattern.compile(extension);
        Matcher matcher;
        String oName;
        boolean findFlag = false;

        for (String fName : outlines) {
            matcher = regex.matcher(fName);
            if (matcher.find()) {
                oName = matcher.group(1);
                findFlag = false;
                for (RavTodoItem t : todoList) {
                    if (!t.isTodoComplete() && t.isPartOfOutline() && t.getOutline().equals(oName)) {
                        findFlag = true;
                        break;
                    }
                }
                if (!findFlag) {
                    System.out.println(oName + " has next actions!");
                    getNextTask(oName);
                }
            }
        }
    }

    /**
     * This method parses the given outline and finds the next item per the outline structure and adds it to the
     * todo list
     *
     * @param fName the name of the outline
     * @throws IOException
     */
    private void getNextTask(String fName) throws IOException {
        File outline = new File(getConfigPath() + fName + ".ol.txt");
        Scanner scnr = new Scanner(outline);
        String contextString = "(.*)(\\+\\+\\w+)(.*)";
        String projectString = "(.*)(\\@\\@\\w+)(.*)";
        Pattern contextRegex = Pattern.compile(contextString);
        Pattern projectRegex = Pattern.compile(projectString);
        Matcher matcher;
        ArrayList<String> items = new ArrayList<>();
        while (scnr.hasNextLine()) {
            items.add(scnr.nextLine());
        }
        scnr.close();
        String parentProject = "";
        String parentContext = "";
        if (items.size() > 0) {
            Path outlinePath = outline.toPath();
            Files.move(outlinePath, outlinePath.resolveSibling(fName + ".ol.bak"), REPLACE_EXISTING);
            File newOutline = new File(getConfigPath() + fName + ".ol.txt");
            PrintWriter out = new PrintWriter(newOutline);
            int pTabs = -1;
            int cTabs = 0;
            boolean flag = false;
            for (int i = 0; i < items.size(); i++) {
                matcher = projectRegex.matcher(items.get(i));
                if (matcher.find()) {
                    parentProject += matcher.group(2).substring(1);
                }
                matcher = contextRegex.matcher(items.get(i));
                if (matcher.find()){
                    parentContext += matcher.group(2).substring(1);
                }
                if (!flag) {
                    if (i + 1 < items.size()) {
                        pTabs = countTabs(items.get(i));
                        cTabs = countTabs(items.get(i + 1));
                        if (pTabs >= cTabs) {
                            addTask(new RavTodoItem(999, items.get(i).trim() + " " + parentProject + " " + parentContext + " outline:" + fName));
                            flag = true;
                        } else {
                            out.println(items.get(i));
                        }
                    } else {
                        addTask(new RavTodoItem(999, items.get(i).trim() + " " + parentProject + " " + parentContext  + " outline:" + fName));
                        flag = true;
                    }
                } else {
                    out.println(items.get(i));
                }
            }
            out.close();
        } else {
            System.out.println(fName + " is empty, removing file.");
            Files.delete(outline.toPath());
        }
    }

    /**
     * This is a helper method to assist in parsing the outline file
     *
     * @param str String to process
     * @return
     */
    private int countTabs(String str) {
        String searchString = "^\\t(.*)";
        Pattern regex = Pattern.compile(searchString);
        Matcher matcher = regex.matcher(str);
        if (matcher.find()){
            return 1 + countTabs(matcher.group(1));
        } else {
            return 0;
        }
    }

    /**
     * This method implements the processing of any items in the todo file with the context @inbox. This is
     * an implementation of David Allen's Getting Things Done methodology of capturing items in the @inbox context
     * first to then regularly review and decide where in the system it belongs. This method will allow to add a
     * different context and any appropriate projects to the item.
     *
     * @throws IOException
     */
    public void processInbox() throws IOException {
        Console con = System.console();
        ArrayList<String> newTasks = new ArrayList<>();

        if (con == null) {
            System.out.println("No console available!");
        } else {
            con.printf("Add @context and +tags: %n");
            for (RavTodoItem t : todoList) {
                if (t.hasContext("@inbox")) {
                    String str = con.readLine(t.getDescription() + ": ");
                    newTasks.add(t.getDescription() + " " + str);
                    t.markComplete();
                }
            }

            for (String s : newTasks) {
                addTask(new RavTodoItem(999, s));
            }
            writeTodoFile();
        }
    }

    public void addJournalEntry(String str) throws IOException {
        File journalFile = new File(getConfigPath() + "journal.txt");
        Path journalPath = journalFile.toPath();
        Files.copy(journalPath, journalPath.resolveSibling("journal.bak"), REPLACE_EXISTING);
        FileWriter journal = new FileWriter(getConfigPath() + "journal.txt", true);
        PrintWriter out = new PrintWriter(journal);

        LocalDateTime today = LocalDateTime.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        out.println(formattedDate + ": " + str);
        out.close();
    }

    public void showJournalEntries(int n) throws FileNotFoundException {
        File journalFile = new File(getConfigPath() + "journal.txt");
        Scanner scnr = new Scanner(journalFile);
        ArrayList<String> entries = new ArrayList<>();
        while (scnr.hasNextLine()){
            entries.add(scnr.nextLine());
        }
        int length;
        if (n >= entries.size()){
            length = 0;
        } else {
            length = entries.size() - n;
        }
        for (int i = length; i < entries.size(); i++) {
            System.out.println(entries.get(i));
        }
    }

}


