package com.rav;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RavTodoItem {
    private String rawLine;
    private ArrayList<String> projects = new ArrayList<>();

    public RavTodoItem(String str){
        this.rawLine = str;
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
}
