package com.rav;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RavTodo implements Iterable{
    private Properties properties;

    private static ArrayList<RavTodoItem> todoList = new ArrayList<>();

    public static void main(String args[]) {
        RavTodo todo = new RavTodo();
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

                default: {
                    System.out.println("Unknown command.");
                    printUsage();
                }

            }

        } else {
            printUsage();
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
        for (RavTodoItem t : todoList) {
            t.displayItem();
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
        File todoFile = new File(getConfigPath());
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

    public static void printUsage(){
        System.out.println("Usage: ravtodo [command] <arguments>");
        System.out.println("Available commands:");
        System.out.println("   ls");
    }

    @Override
    public Iterator iterator() {
        return todoList.iterator();
    }


    //TODO: implement 'do'

    //TODO: implement 'archive'

    //TODO: implement 'add'

    //TODO: implement threshold date filtering
}


