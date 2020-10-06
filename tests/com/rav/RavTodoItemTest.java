package com.rav;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class RavTodoItemTest {
    private RavTodoItem todoItem;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final static PrintStream originalOut = System.out;
    private final static PrintStream originalErr = System.err;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    public void init() {
        this.todoItem = new RavTodoItem(1,"(A) 2020-07-24 do something +good @now t:2020-08-08 due:2020-10-10");
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    public static void shutdown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("Validate creation of todo item")
    public void createItemTest(){
        assertEquals("(A) 2020-07-24 do something +good @now t:2020-08-08 due:2020-10-10", this.todoItem.getRawLine());
    }

    @Test
    @DisplayName("Validate projects can be read")
    public void readProjectTest(){
        assertEquals("+good", todoItem.getProject());
    }

    @Test
    @DisplayName("Validate context can be read")
    public void readContextTest(){
        assertEquals("@now", todoItem.getContext().get(0));
    }

    @Test
    @DisplayName("Validate display of item")
    public void displayItemTest() {
        this.todoItem.displayItem();
        assertEquals("  1 (A) 2020-07-24 do something +good @now t:2020-08-08 due:2020-10-10\n", outContent.toString());
    }

    @Test
    @DisplayName("Validate term match")
    public void positiveTermMatch() {
        String[] terms1 = {"+good", "@later"};
        String[] terms2 = {"+bad", "@now"};
        String[] terms3 = {"do", "+bad"};
        String[] terms4 = {"+bad", "@later"};
        assertTrue(this.todoItem.matchesTerms(terms1));
        assertTrue(this.todoItem.matchesTerms(terms2));
        assertTrue(this.todoItem.matchesTerms(terms3));
        assertFalse(this.todoItem.matchesTerms(terms4));
    }

    @Test
    @DisplayName("Validate reading of Created Date")
    public void readCreatedDateTest() {
        assertEquals(LocalDate.of(2020, 7, 24), todoItem.getCreatedDate());
    }

    @Test
    @DisplayName("Validate reading of complete indicator")
    public void isTodoCompleteTest() {
        assertFalse(todoItem.isTodoComplete());
    }

    @Test
    @DisplayName("Validate reading of priority")
    public void readPriorityTest() {
        assertEquals("A", this.todoItem.getPriority());
    }

    @Test
    @DisplayName("Validate threshold date")
    public void readThresholdDateTest() {
        assertEquals(LocalDate.of(2020, 8, 8), todoItem.getThresholdDate());
    }

    @Test
    @DisplayName("Validate due date")
    public void readDueDateTest(){
        assertEquals(LocalDate.of(2020, 10, 10), todoItem.getDueDate());
    }

    @Test
    @DisplayName("Validate description processing")
    public void readDescriptionTest() {
        assertEquals("do something", this.todoItem.getDescription());
    }
}
