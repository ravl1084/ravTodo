package com.rav;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RavTodoItem {
    private String rawLine;
    private int index;
    private ArrayList<String> projects = new ArrayList<>();
    private ArrayList<String> contexts = new ArrayList<>();
    private LocalDate createdDate;
    private LocalDate thresholdDate;
    private LocalDate dueDate;
    private LocalDate completeDate;
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

    public static Comparator<RavTodoItem> PriDueComparator = new Comparator<RavTodoItem>() {
        @Override
        public int compare(RavTodoItem ravTodoItem, RavTodoItem t1) {
            String pri1 = ravTodoItem.getPriority();
            String pri2 = t1.getPriority();
            LocalDate due1 = ravTodoItem.getDueDate();
            LocalDate due2 = t1.getDueDate();
            int comp1 = ravTodoItem.isTodoComplete()?-5000:50;
            int comp2 = t1.isTodoComplete()?-5000:50;
            int priCompare = pri1.compareTo(pri2);
            int dueCompare = due1.compareTo(due2);
            int compCompare = comp2 - comp1;

            return priCompare+dueCompare + compCompare;
        }
    };

    public void displayItem() {
        System.out.format("%3d %s\n",this.index, getRawLine());
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
            createdDate = LocalDate.parse(m.group(3));
        } else {
            createdDate = LocalDate.now();
        }
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void readCompleteMark() {
        String searchPattern = "^x .*";

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
            priority = "Z";
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
            thresholdDate = LocalDate.parse(matcher.group(1));
        } else {
            thresholdDate = LocalDate.parse("1900-01-01");
        }
    }

    public LocalDate getThresholdDate() {
        return thresholdDate;
    }

    public void readDue() throws ParseException {
        String searchPattern = ".*\\s+due:(\\d{4}-\\d{2}-\\d{2}).*";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if (matcher.find()){
            dueDate = LocalDate.parse(matcher.group(1));
        } else {
            dueDate = LocalDate.parse("2999-12-31");
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getIndex() {
        return this.index;
    }

    public void markComplete() {
        displayItem();
        if (!isTodoComplete()) {
            this.isComplete = true;
            this.completeDate = LocalDate.now();
            this.priority = "Z";
            setRawLine("x " + completeDate + " " + rawLine);
        } else {
            System.out.println("Todo already complete");
        }
    }


    //TODO: implement recurrence

    //TODO: implement outline

}
