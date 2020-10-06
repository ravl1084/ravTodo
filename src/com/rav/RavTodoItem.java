package com.rav;

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diogonunes.jcolor.Ansi.colorize;

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
        readCompleteMark();
        readPriority();
        readCreatedDate();
        readProjects();
        readContexts();
        readRecurrence();
        readThreshold();
        readDue();
        readOutline();
        updateRawLine();
    }

    public String getRawLine() {
        //updateRawLine();
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
        AnsiFormat priA = new AnsiFormat(Attribute.TEXT_COLOR(1));
        AnsiFormat priB = new AnsiFormat(Attribute.TEXT_COLOR(208));
        AnsiFormat priC = new AnsiFormat(Attribute.TEXT_COLOR(11));
        AnsiFormat projFormat = new AnsiFormat(Attribute.CYAN_TEXT());
        AnsiFormat contFormat = new AnsiFormat(Attribute.GREEN_TEXT());
        if (isComplete) {
            System.out.println(colorize(String.format("%3d %s", this.index, getRawLine()), Attribute.STRIKETHROUGH()));
        } else {
            switch (priority) {
                case "A":
                    System.out.print(priA.format(String.format("%3d %s", this.index, "(" + priority + ") ")));
                    break;

                case "B":
                    System.out.print(priB.format(String.format("%3d %s", this.index, "(" + priority + ") ")));
                    break;

                case "C":
                    System.out.print(priC.format(String.format("%3d %s", this.index, "(" + priority + ") ")));
                    break;

                default:
                    System.out.print(String.format("%3d %s", this.index, ""));
                    break;
            }

            System.out.print(description.trim() + " ");

            for (String p : projects) {
                System.out.print(projFormat.format(p + " "));
            }

            for (String c : contexts) {
                System.out.print(contFormat.format(c + " "));
            }

            System.out.println();
        }
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
        Matcher m = regex.matcher(description);

        if (m.find()){
            projects.add(m.group(2).trim());
            description = m.group(1) + m.group(3);
        }
    }

    public String getProject() {
        return this.projects.get(0);
    }

    public void readContexts(){
        String contextPattern = "(.*\\s)(\\@\\w+)(.*)";

        Pattern regex = Pattern.compile(contextPattern);
        Matcher m = regex.matcher(description);

        if (m.find()){
            contexts.add(m.group(2).trim());
            description = m.group(1) + m.group(3);
        }
    }

    public void setContext(String str) {
        contexts.add(str);
    }

    public void removeContext(String str) {

        Predicate<String> pr = a->a.equals(str);
        contexts.removeIf(pr);
    }

    public ArrayList<String> getContext() {
        return this.contexts;
    }

    public void readCreatedDate() {
        String searchPattern = "^\\s?(\\d{4}-\\d{2}-\\d{2})\\s+(.*)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher m = regex.matcher(description);

        if (m.find()) {
            createdDate = LocalDate.parse(m.group(1));
            description = m.group(2);
        } else {
            createdDate = LocalDate.now();
        }
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void readCompleteMark() {
        String searchPattern = "^x (\\d{4}-\\d{2}-\\d{2})\\s+(.*)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(rawLine);

        if(matcher.find()) {
            isComplete = true;
            description = matcher.group(2);
            completeDate = LocalDate.parse(matcher.group(1));
        } else {
            description = rawLine;
        }
    }

    public boolean isTodoComplete() {
        return isComplete;
    }

    public void readPriority() {
        String searchPattern = "^\\(([a-zA-Z])\\)(.*)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(description);

        if (matcher.find()){
            priority = matcher.group(1);
            description = matcher.group(2);
        } else {
            priority = "Z";
        }
    }

    public String getPriority(){
        return priority;
    }

    public void readThreshold() {
        String searchPattern = "(.*\\s+)t:(\\d{4}-\\d{2}-\\d{2})(.*)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(description);

        if (matcher.find()){
            thresholdDate = LocalDate.parse(matcher.group(2));
            hasThreshold = true;
            description = matcher.group(1) + matcher.group(3);
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
        String searchPattern = "(.*\\s+)due:(\\d{4}-\\d{2}-\\d{2})(.*)";

        Pattern regex = Pattern.compile(searchPattern);
        Matcher matcher = regex.matcher(description);

        if (matcher.find()){
            dueDate = LocalDate.parse(matcher.group(2));
            hasDue = true;
            description = matcher.group(1) + matcher.group(3);
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

    public void readRecurrence() {
        String searchString = "(.* )rec:(\\+)*(\\d+)([dwmy])(.*)";
        Pattern regex = Pattern.compile(searchString);
        Matcher matcher = regex.matcher(description);
        //logger.info(rawLine);

        if (matcher.find()){
            this.recurrence = true;
            this.period = Integer.valueOf(matcher.group(3));
            this.unit = matcher.group(4);
            if (matcher.group(2) != null) {
                this.relative = false;
            } else {
                this.relative = true;
            }
            description = matcher.group(1) + matcher.group(5);
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

        newLine += createdDate + " " + description.trim();

        for (String t : projects) {
            newLine += " " + t;
        }

        for (String s : contexts) {
            newLine += " " + s;
        }

        if (hasThreshold) {
            newLine += " t:" + getThresholdDate();
        }

        if (hasDue) {
            newLine += " due:" + getDueDate();
        }

        if (isRecurrence()) {
            newLine += " rec:";
            if (!isRelative()) {
                newLine += "+";
            }
            newLine += period + unit;
        }

        if (hasOutline) {
            newLine += " outline:" + outline;
        }

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
        String searchString = "(.*\\s+)outline:(\\w+)(.*)";
        Pattern regex = Pattern.compile(searchString);
        Matcher matcher = regex.matcher(description);
        if (matcher.find()){
            this.outline = matcher.group(2);
            this.hasOutline = true;
            description = matcher.group(1) + matcher.group(3);
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

    public boolean hasContext(String str) {
        boolean result = false;
        for (String s : contexts) {
            if (s.equals(str)){
                result = true;
                break;
            }
        }
        return result;
    }

    public String getDescription() {
        description = description.trim();
        return description;
    }
}
