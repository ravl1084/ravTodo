package com.rav;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RavTodo implements Iterable{
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
        for (RavTodoItem t : todoList){
            if (t.matchesTerms(terms)){
                t.displayItem();
            }
        }
    }

    public void readConfig() throws IOException{
        String fileName = "ravTodo.conf";

        InputStream inputStream = new FileInputStream(fileName);

        this.properties.load(inputStream);
    }

    public String getConfigPath(){
        return this.properties.getProperty("todo.path");
    }

    public void readTodoFile() throws IOException{
        File todoFile = new File(getConfigPath() + "/todo.txt");
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

    @Override
    public Iterator iterator() {
        return todoList.iterator();
    }


    //TODO: implement 'do'
    public void doTask(int n) throws IOException {
        try {
            System.out.println("Searching for todo " + n);
            getTask(n).markComplete();
        } catch (RavTodoNotFoundException e) {
            System.out.println("Index not found in Todo file.");
        }
        writeTodoFile();
    }

    public void writeTodoFile() throws IOException {
        File todoFile = new File(properties.getProperty("todo.path") + "/todo.txt");
        Path path = todoFile.toPath();
        Files.move(path, path.resolveSibling("todo.bak"), REPLACE_EXISTING);
        File newFile = new File(properties.getProperty("todo.path") + "/todo.txt");
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

    //TODO: implement 'archive'

    //TODO: implement 'add'

    //TODO: implement threshold date filtering
}


