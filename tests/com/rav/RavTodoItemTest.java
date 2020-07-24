package com.rav;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RavTodoItemTest {
    private RavTodoItem todoItem;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void init() {
        this.todoItem = new RavTodoItem(1,"do something +good @now");
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    public void shutdown() {
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
    @DisplayName("Validate display of item")
    public void displayItemTest() {
        this.todoItem.displayItem();
        assertEquals("1 do something +good @now", outContent.toString());
    }
}
