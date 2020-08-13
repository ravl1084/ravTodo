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
    private Date thresholdDate;
    private Date dueDate;
    private Date completeDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private boolean isComplete = false;
    private String priority;

    public RavTodoItem(int index, String str){
        this.rawLine = str;
        this.index = index;
        readProjects();
        readContexts();
        readCompleteMark();
        readPriority();
        try {
            readCreatedDate();
            readThreshold();
            readDue();
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

    public void readContexts(){
        String contextPattern = "(.*\\s)(\\@\\w+)(.*)";

        Pattern regex = Pattern.compile(contextPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()){
            contexts.add(m.group(2));
        }
    }

    public ArrayList<String> getContext() {
        return this.contexts;
    }

    public void readCreatedDate() throws ParseException {
        String searchPattern = "^(x )*(\\([A-Z]\\) )*(\\d{4}-\\d{2}-\\d{2})\\s+.*";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()) {
            createdDate = dateFormat.parse(m.group(3));
        } else {
            createdDate = null;
        }
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void readCompleteMark() {
        String searchPattern = "^x ";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        isComplete = matcher.find();
    }

    public boolean isTodoComplete() {
        return isComplete;
    }

    public void readPriority() {
        String searchPattern = "^\\(([a-zA-Z])\\)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if (matcher.find()){
            priority = matcher.group(1);
        } else {
            System.out.println("No Match!");
        }
    }

    public String getPriority(){
        return priority;
    }

    public void readThreshold() throws ParseException {
        String searchPattern = ".*\\s+t:(\\d{4}-\\d{2}-\\d{2}).*";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if (matcher.find()){
            thresholdDate = dateFormat.parse(matcher.group(1));
        }
    }

    public Date getThresholdDate() {
        return thresholdDate;
    }

    public void readDue() throws ParseException {
        String searchPattern = ".*\\s+due:(\\d{4}-\\d{2}-\\d{2}).*";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if (matcher.find()){
            dueDate = dateFormat.parse(matcher.group(1));
        }
    }

    public Date getDueDate() {
        return dueDate;
    }

}
