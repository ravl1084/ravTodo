package com.rav;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RavTodoItem {
    private String rawLine;
    private int index;
    private ArrayList<String> projects = new ArrayList<>();
    private ArrayList<String> contexts = new ArrayList<>();

    public RavTodoItem(int index, String str){
        this.rawLine = str;
        this.index = index;
        readProjects();
        readContexts();
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }


    public void displayItem() {
        System.out.println(this.index + " " + getRawLine());
    }

    public boolean matchesTerms(String[] terms){
        boolean matchFound = false;
        for (int i = 0; i < terms.length; i++) {
            String searchString = ".*" + Pattern.quote(terms[i]) + ".*";
            Pattern regex = Pattern.compile(searchString);
            Matcher m = regex.matcher(rawLine);
            if (m.find()) {
                matchFound = true;
                break;
            }
        }
        return matchFound;
    }

    public void readProjects(){
        String projectPattern = "(.*\\s)(\\+\\w+)(.*)";

        Pattern regex = Pattern.compile(projectPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()){
            projects.add(m.group(2));
        }
    }

    public String getProject() {
        return this.projects.get(0);
    }

    //TODO: implement Contexts
    public void readContexts(){
        String contextPattern = "(.*\\s)(\\@\\w+)(.*)";

        Pattern regex = Pattern.compile(contextPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()){
            contexts.add(m.group(2));
        }
    }

    public String getContext() {
        return this.contexts.get(0);
    }

    //TODO: implement CreatedDate

    //TODO: implement Complete

    //TODO: implement Priority

    //TODO: implement Threshold Date

    //TODO: implement Due Date
}
