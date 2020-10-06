package com.rav;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RavTodo {
    private Properties properties;

    private static ArrayList<RavTodoItem> todoList = new ArrayList<>();

    public static void main(String args[]) {
        RavTodo todo = new RavTodo();
        String numString = "\\d+";
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

                default: {
                    System.out.println("Unknown command.");
                    todo.printUsage();
                }

            }

        } else {
            todo.printUsage();
        }
    }

    public RavTodo() {
        this.properties = new Properties();
        try {
            readConfig();
            //System.out.println("Config found and loaded!");
            readTodoFile();
            //System.out.println("Todo file found and loaded!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listAllTodoItems() {
        Collections.sort(todoList, RavTodoItem.PriDueComparator);
        LocalDate today = LocalDate.now();
        for (RavTodoItem t : todoList) {
            if (!t.getThresholdDate().isAfter(today) && !t.isTodoComplete()) {
                t.displayItem();
            }
        }
    }

    private void listTodoItems(String[] terms) {
        Collections.sort(todoList, RavTodoItem.PriDueComparator);
        for (RavTodoItem t : todoList){
            if (t.matchesTerms(terms) && t.hasMetThreshold()){
                t.displayItem();
            }
        }
    }

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

    public String getConfigPath(){
        String path = properties.getProperty("todo.path");
        if (path.matches(":")) {
            path += "\\";
        } else {
            path += "/";
        }
        return path;
    }

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

    public int getTodoCount() {
        return todoList.size();
    }

    public void printUsage(){
        System.out.println("Usage: ravtodo [command] <arguments>");
        System.out.println("Available commands:");
        System.out.println("   ls");
    }


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

    public void addTask(RavTodoItem t) throws IOException {
        todoList.add(t);
        writeTodoFile();
    }

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

    private void getNextTask(String fName) throws IOException {
        File outline = new File(getConfigPath() + fName + ".ol.txt");
        Scanner scnr = new Scanner(outline);
        ArrayList<String> items = new ArrayList<>();
        while (scnr.hasNextLine()) {
            items.add(scnr.nextLine());
        }
        scnr.close();
        if (items.size() > 0) {
            Path outlinePath = outline.toPath();
            Files.move(outlinePath, outlinePath.resolveSibling(fName + "ol.bak"), REPLACE_EXISTING);
            File newOutline = new File(getConfigPath() + fName + ".ol.txt");
            PrintWriter out = new PrintWriter(newOutline);
            String prev = "";
            String current = "";
            int pTabs = -1;
            int cTabs = 0;
            boolean flag = false;
            for (int i = 0; i < items.size(); i++) {
                if (!flag) {
                    if (i + 1 < items.size()) {
                        pTabs = countTabs(items.get(i));
                        cTabs = countTabs(items.get(i + 1));
                        if (pTabs >= cTabs) {
                            addTask(new RavTodoItem(999, items.get(i).trim() + " outline:" + fName));
                            flag = true;
                        } else {
                            out.println(items.get(i));
                        }
                    } else {
                        addTask(new RavTodoItem(999, items.get(i).trim() + " outline:" + fName));
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

}


