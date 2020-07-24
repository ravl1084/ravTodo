package com.rav;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RavTodoItem {
    private String rawLine;
    private int index;
    private ArrayList<String> projects = new ArrayList<>();
    private ArrayList<String> contexts = new ArrayList<>();
    private Date createdDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat();

    public RavTodoItem(int index, String str){
        this.rawLine = str;
        this.index = index;
        readProjects();
        readContexts();
        try {
            readCreatedDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

    public void readCreatedDate() throws ParseException {
        String searchPattern = "^(\\d{4}-\\d{2}-\\d{2})\\s+";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()) {
            createdDate = dateFormat.parse(m.group(1));
        } else {
            createdDate = null;
        }
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    //TODO: implement Complete

    //TODO: implement Priority

    //TODO: implement Threshold Date

    //TODO: implement Due Date
}
