package com.rav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RavTodoItemTest {
    private RavTodoItem todoItem;

    @BeforeEach
    public void init(){
        this.todoItem = new RavTodoItem("do something +good @now");
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
}
