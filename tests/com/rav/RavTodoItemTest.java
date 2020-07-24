package com.rav;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class RavTodoItemTest {
    private RavTodoItem todoItem;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final static PrintStream originalOut = System.out;
    private final static PrintStream originalErr = System.err;

    @BeforeEach
    public void init() {
        this.todoItem = new RavTodoItem(1,"do something +good @now");
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
        assertEquals("do something +good @now", this.todoItem.getRawLine());
    }

    @Test
    @DisplayName("Validate projects can be read")
    public void readProjectTest(){
        assertEquals("+good", todoItem.getProject());
    }

    @Test
    @DisplayName("Validate context can be read")
    public void readContextTest(){
        assertEquals("@now", todoItem.getContext());
    }

    @Test
    @DisplayName("Validate display of item")
    public void displayItemTest() {
        this.todoItem.displayItem();
        assertEquals("1 do something +good @now\n", outContent.toString());
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
}
