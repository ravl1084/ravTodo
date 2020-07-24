package com.rav;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RavTodoItem {
    private String rawLine;
    private int index;
    private ArrayList<String> projects = new ArrayList<>();

    public RavTodoItem(int index, String str){
        this.rawLine = str;
        this.index = index;
        readProjects();
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    public void readProjects(){
        String projectPattern = "(.*)(\\+\\w+)(.*)";

        Pattern regex = Pattern.compile(projectPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()){
            projects.add(m.group(2));
        }
    }

    public String getProject() {
        return this.projects.get(0);
    }

    public void displayItem() {
        System.out.println(this.index + " " + getRawLine());
    }

    //TODO: implement Contexts

    //TODO: implement CreatedDate

    //TODO: implement Complete

    //TODO: implement Priority

    //TODO: implement Threshold Date

    //TODO: implement Due Date
}
