package com.rav;

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diogonunes.jcolor.Ansi.colorize;

/**
 * This class represents a todo item per the Todo.txt standard
 */
public class RavTodoItem {
    //private Logger logger = LoggerFactory.getLogger(RavTodoItem.class);
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
    private boolean isOverdue = false;
    private int index;
    private String outline;
    private boolean hasOutline;

    /**
     * This constructor creates a RavTodoItem object from the string in the todo.txt file
     *
     * @param index the index of this todo item in the list
     * @param str   the todo item string as recoded in the todo.txt file
     */
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

    /**
     * @return the raw string per the todo.txt file
     */
    public String getRawLine() {
        //updateRawLine();
        return rawLine;
    }

    /**
     * This method sets the todo item's raw string to the provided string
     *
     * @param rawLine the new raw string for the todo item
     */
    public void setRawLine(String rawLine) {
        this.rawLine = rawLine;
    }

    /**
     * This comparator sorts the todo items first by priority, then created date and completion
     */
    public static Comparator<RavTodoItem> PriDueComparator = new Comparator<RavTodoItem>() {
        @Override
        public int compare(RavTodoItem ravTodoItem, RavTodoItem t1) {
            String pri1 = ravTodoItem.getPriority();
            String pri2 = t1.getPriority();
            LocalDate due1 = ravTodoItem.getDueDate();
            LocalDate due2 = t1.getDueDate();
            LocalDate created1 = ravTodoItem.getCreatedDate();
            LocalDate created2 = t1.getCreatedDate();
            int comp1 = ravTodoItem.isTodoComplete()?-5000:50;
            int comp2 = t1.isTodoComplete()?-5000:50;
            int priCompare = pri1.compareTo(pri2);
            int dueCompare = due1.compareTo(due2);
            int createCompare = created1.compareTo(created2);
            int compCompare = comp2 - comp1;

            return priCompare*10 + dueCompare + createCompare + compCompare;
        }
    };

    /**
     * This method shows an ANSI-colorized version of the todo item, coloring priorities A through C,
     * highlighting contexts and projects, and showing complete items as strikethrough
     */
    public void displayItem() {
        AnsiFormat priA = new AnsiFormat(Attribute.TEXT_COLOR(1));
        AnsiFormat priB = new AnsiFormat(Attribute.TEXT_COLOR(208));
        AnsiFormat priC = new AnsiFormat(Attribute.TEXT_COLOR(11));
        AnsiFormat projFormat = new AnsiFormat(Attribute.CYAN_TEXT());
        AnsiFormat contFormat = new AnsiFormat(Attribute.GREEN_TEXT());
        AnsiFormat outlineFormat = new AnsiFormat(Attribute.BLUE_TEXT());
        AnsiFormat overdueFormat = new AnsiFormat(Attribute.RED_BACK());
        String display = "";
        if (!isComplete) {
            switch (priority) {
                case "A":
                    display += priA.format(String.format("%3d %s", this.index, "(" + priority + ") "));
                    break;

                case "B":
                    display += priB.format(String.format("%3d %s", this.index, "(" + priority + ") "));
                    break;

                case "C":
                    display += priC.format(String.format("%3d %s", this.index, "(" + priority + ") "));
                    break;

                default:
                    display += String.format("%3d %s", this.index, "");
                    break;
            }

        }

        if (!isComplete && isOverdue){
            display += overdueFormat.format(description.trim() + "");
        } else {
            display += description.trim() + " ";
        }

        for (String p : projects) {
            display += projFormat.format(p + " ");
        }

        for (String c : contexts) {
            display += contFormat.format(c + " ");
        }

        if (isPartOfOutline()) {
            display += outlineFormat.format("outline:" + getOutline() + " ");
        }

        if (isComplete) {
            System.out.println(colorize(String.format("%3d %s", this.index, display), Attribute.STRIKETHROUGH()));
        } else {
            System.out.println(display);
        }

    }

    /**
     * @param terms the array of Strings that holds the search terms
     * @return true if the todo item matches any of the terms, false otherwise
     */
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

    /**
     * This method parses the project for the todo
     */
    public void readProjects(){
        String projectPattern = "(.*\\s)(\\+\\w+)(.*)";

        Pattern regex = Pattern.compile(projectPattern);
        Matcher m = regex.matcher(description);

        if (m.find()){
            projects.add(m.group(2).trim());
            description = m.group(1) + m.group(3);
        }
    }

    /**
     * @return the todo item's project
     */
    public String getProject() {
        return this.projects.get(0);
    }

    /**
     * This method parses the context for the todo
     */
    public void readContexts(){
        String contextPattern = "(.*\\s)(\\@\\w+)(.*)";

        Pattern regex = Pattern.compile(contextPattern);
        Matcher m = regex.matcher(description);

        if (m.find()){
            contexts.add(m.group(2).trim());
            description = m.group(1) + m.group(3);
        }
    }

    /**
     * @return the list of contexts
     */
    public ArrayList<String> getContext() {
        return this.contexts;
    }

    /**
     * This method parses the created date for the todo
     */
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

    /**
     * @return the todo item's created date
     */
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    /**
     * This method parses the todo item and determines if it is complete
     */
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

    /**
     * @return true if the todo is complete, false otherwise
     */
    public boolean isTodoComplete() {
        return isComplete;
    }

    /**
     * This methos parses the todo item and extracts the priority. The priority is set to Z for items
     * with no explicit priority for sorting purposes
     */
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

    /**
     * @return the String with the priority
     */
    public String getPriority(){
        return priority;
    }

    /**
     * This method parses the todo item and extracts the threshold date. The threshold date is set to 1900-01-01
     * when no explicit threshold exists for sorting purposes
     */
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

    /**
     * @return the threshold date of the todo item
     */
    public LocalDate getThresholdDate() {
        return thresholdDate;
    }

    /**
     * This method sets the threshold date to the provided date
     * @param date the new threshold date
     */
    public void setThresholdDate(LocalDate date) {
        this.thresholdDate = date;
        this.hasThreshold = true;
        updateRawLine();
    }

    /**
     * This method parses the todo item and reads the due date. If the todo item has no explicit due date, the
     * date is set to 2999-12-31 for sorting purposes
     */
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

        if (dueDate.isBefore(LocalDate.now())) {
            isOverdue = true;
        } else {
            isOverdue = false;
        }
    }

    /**
     * @return the due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * This method sets the due date to the provided date
     *
     * @param date the new due date
     */
    public void setDueDate(LocalDate date) {
        this.dueDate = date;
    }

    /**
     * @return the index of the todo item
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * This method marks this todo item as complete.
     */
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

    /**
     * This method parses the todo item and extracts the recurrence
     */
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

    /**
     * @return true if the todo is recurring, false otherwise
     */
    public boolean isRecurrence() {
        return recurrence;
    }

    /**
     * This method creates the new iteration of a recurring item
     *
     * @return the new iteration of the todo item
     */
    public RavTodoItem createNext() {
        RavTodoItem t = new RavTodoItem(999, rawLine);
        LocalDate today = LocalDate.now();
        LocalDate future = LocalDate.parse("2999-12-31");
        if (t.isRelative()){
            switch (unit) {
                case "d":
                    t.setThresholdDate(today.plusDays(period));
                    if (t.usesDue()) t.setDueDate(today.plusDays(period));
                    break;

                case "w":
                    t.setThresholdDate(today.plusWeeks(period));
                    if (t.usesDue()) t.setDueDate(today.plusWeeks(period));
                    break;

                case "m":
                    t.setThresholdDate(today.plusMonths(period));
                    if (t.usesDue()) t.setDueDate(today.plusMonths(period));
                    break;

                case "y":
                    t.setThresholdDate(today.plusYears(period));
                    if (t.usesDue()) t.setDueDate(today.plusYears(period));
                    break;
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

    /**
     * This method re-generates the raw line, making any updates necessary
     */
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

    /**
     * @return true if threshold date is in the past, false otherwise
     */
    public boolean hasMetThreshold() {
        LocalDate today = LocalDate.now();
        return !thresholdDate.isAfter(today);
    }

    /**
     * This method parses the todo item and extracts the outline name
     */
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
