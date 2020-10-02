package com.rav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RavTodoItem {
    private Logger logger = LoggerFactory.getLogger(RavTodoItem.class);
    private ArrayList<String> contexts = new ArrayList<>();
    private ArrayList<String> projects = new ArrayList<>();
    private Integer period;
    private LocalDate completeDate;
    private LocalDate createdDate;
    private LocalDate dueDate;
    private LocalDate thresholdDate;
    private String priority;
    private String rawLine;
    private String description;
    private String unit;
    private boolean isComplete = false;
    private boolean recurrence;
    private boolean relative;
    private boolean hasDue;
    private boolean hasThreshold;
    private int index;
    private String outline;
    private boolean hasOutline;

    public RavTodoItem(int index, String str){
        this.rawLine = str;
        this.index = index;
        readProjects();
        readContexts();
        readCompleteMark();
        readPriority();
        readRecurrence();
        readOutline();
        readCreatedDate();
        readThreshold();
        readDue();
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
            LocalDate due1 = ravTodoItem.getCreatedDate();
            LocalDate due2 = t1.getCreatedDate();
            int comp1 = ravTodoItem.isTodoComplete()?-5000:50;
            int comp2 = t1.isTodoComplete()?-5000:50;
            int priCompare = pri1.compareTo(pri2);
            int dueCompare = due1.compareTo(due2);
            int compCompare = comp2 - comp1;

            return priCompare*10 + dueCompare + compCompare;
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

    public void readCreatedDate() {
        String searchPattern = "^(x )*(\\([A-Z]\\) )*(\\d{4}-\\d{2}-\\d{2})\\s+(.*)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher m = regex.matcher(rawLine);

        if (m.find()) {
            createdDate = LocalDate.parse(m.group(3));
            description = m.group(4);
        } else {
            createdDate = LocalDate.now();
            description = rawLine;
            updateRawLine();
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

    public void readThreshold() {
        String searchPattern = ".*\\s+t:(\\d{4}-\\d{2}-\\d{2}).*";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if (matcher.find()){
            thresholdDate = LocalDate.parse(matcher.group(1));
            hasThreshold = true;
        } else {
            thresholdDate = LocalDate.parse("1900-01-01");
            hasThreshold = false;
        }
    }

    public LocalDate getThresholdDate() {
        return thresholdDate;
    }

    public void setThresholdDate(LocalDate date) {
        this.thresholdDate = date;
    }

    public void readDue() {
        String searchPattern = ".*\\s+due:(\\d{4}-\\d{2}-\\d{2}).*";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if (matcher.find()){
            dueDate = LocalDate.parse(matcher.group(1));
            hasDue = true;
        } else {
            dueDate = LocalDate.parse("2999-12-31");
            hasDue = false;
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate date) {
        this.dueDate = date;
    }

    public int getIndex() {
        return this.index;
    }

    public void markComplete() {
        System.out.print("Done: ");
        displayItem();
        if (!isTodoComplete()) {
            this.isComplete = true;
            this.completeDate = LocalDate.now();
            this.priority = "Z";
            updateRawLine();
        } else {
            System.out.println("Todo already complete");
        }
    }


    //TODO: implement recurrence

    public void readRecurrence() {
        String searchString = ".* rec:(\\+)*(\\d+)([dwmy]).*";
        Pattern regex = Pattern.compile(searchString);
        Matcher matcher = regex.matcher(rawLine);
        //logger.info(rawLine);

        if (matcher.find()){
            this.recurrence = true;
            this.period = Integer.valueOf(matcher.group(2));
            this.unit = matcher.group(3);
            if (matcher.group(1) != null) {
                this.relative = false;
            } else {
                this.relative = true;
            }
        } else {
            this.recurrence = false;
        }
    }

    public boolean isRecurrence() {
        return recurrence;
    }

    public RavTodoItem createNext() {
        RavTodoItem t = new RavTodoItem(999, rawLine);
        LocalDate today = LocalDate.now();
        if (t.isRelative()){
            switch (unit) {
                case "d":
                    t.setThresholdDate(today.plusDays(period));
                    break;

                case "w":
                    t.setThresholdDate(today.plusWeeks(period));
                    break;

                case "m":
                    t.setThresholdDate(today.plusMonths(period));
                    break;

                case "y":
                    t.setThresholdDate(today.plusYears(period));
            }
        } else {
            if (t.usesThreshold()) {
                LocalDate date = t.getThresholdDate();
                switch (unit) {
                    case "d":
                        t.setThresholdDate(date.plusDays(period));
                        break;

                    case "w" :
                        t.setThresholdDate(date.plusWeeks(period));
                        break;

                    case "m":
                        t.setThresholdDate(date.plusMonths(period));
                        break;

                    case "y":
                        t.setThresholdDate(date.plusYears(period));
                        break;
                }
            }
            // if it has due, add period to due
            if (t.usesDue()){
                LocalDate date = t.getDueDate();
                switch (unit) {
                    case "d":
                        t.setDueDate(date.plusDays(period));
                        break;

                    case "w":
                        t.setDueDate(date.plusWeeks(period));
                        break;

                    case "m":
                        t.setDueDate(date.plusMonths(period));
                        break;

                    case "y":
                        t.setDueDate(date.plusYears(period));
                }
            }

        }
        // regenerate rawLine properly

        t.updateRawLine();

        return t;
    }

    private void updateRawLine() {
        String newLine = "";
        if (isTodoComplete()){
            newLine += "x " + completeDate + " ";
        } else {
            if (!priority.equals("Z")) {
                newLine += "(" + priority + ") ";
            }
        }

        newLine += createdDate + " ";

        if (hasThreshold) {
            String oldThreshold = "(.*)\\s+t:\\d{4}-\\d{2}-\\d{2}(.*)";
            Pattern regexT = Pattern.compile(oldThreshold);
            Matcher mt = regexT.matcher(description);
            if (mt.find()) {
                description = mt.group(1) + mt.group(2);
            }
            description += " t:" + getThresholdDate();
        }

        if (hasDue) {
            String oldDue = "(.*)\\s+due:\\d{4}-\\d{2}-\\d{2}(.*)";
            Pattern regexD = Pattern.compile(oldDue);
            Matcher md = regexD.matcher(description);
            if (md.find()) {
                description = md.group(1) + md.group(2);
            }
            description += " due:" + getDueDate();
        }

        newLine += description;

        setRawLine(newLine);
    }

    private boolean usesDue() {
        return hasDue;
    }

    private boolean usesThreshold() {
        return hasThreshold;
    }

    public boolean isRelative() {
        return relative;
    }

    public boolean hasMetThreshold() {
        LocalDate today = LocalDate.now();
        return !thresholdDate.isAfter(today);
    }

    public void readOutline() {
        String searchString = ".*\\s+outline:(\\w+).*";
        Pattern regex = Pattern.compile(searchString);
        Matcher matcher = regex.matcher(rawLine);
        if (matcher.find()){
            this.outline = matcher.group(1);
            this.hasOutline = true;
        } else {
            this.outline = "";
            this.hasOutline = false;
        }
    }

    public String getOutline() {
        return outline;
    }

    public boolean isPartOfOutline() {
        return hasOutline;
    }
}
