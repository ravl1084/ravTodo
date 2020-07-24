package com.rav;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class RavTodo {
    private Properties properties;

    private static ArrayList<RavTodoItem> todoList = new ArrayList<>();

    public static void main(String args[]) {
        if (args.length > 0){
            switch (args[0]) {
                case "ls": {
                    if (args.length > 1) {
                        String[] terms = Arrays.copyOfRange(args, 1,args.length);
                        listTodoItems(terms);
                    } else {
                        listAllTodoItems();
                    }
                };

                default: {
                    System.out.println("Unknown command.");
                }

            }

        } else {
            printUsage();
        }
        RavTodo todo = new RavTodo();
        try {
            todo.readConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listAllTodoItems() {
        //TODO: implement display of all valid items
        for (int i = 0; i < todoList.size(); i++){
            todoList.get(i).displayItem();
        }
    }

    private static void listTodoItems(String[] terms) {
        //TODO: implement term search
    }

    public RavTodo() {
        this.properties = new Properties();
        try {
            readConfig();
        } catch (IOException e) {
            e.printStackTrace();
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

    //TODO: implement 'do'

    //TODO: implement 'archive'

    //TODO: implement 'add'
}


