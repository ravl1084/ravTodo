package com.rav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class RavTodoTest {

	private RavTodo tasks;

	@BeforeEach
	public void init(){
		this.tasks = new RavTodo();
	}
	/**
	 * Initial test of environment setup
	 *  
	 **/
	@Test
	@DisplayName("Initial test of environment setup")
	public void mainTest() {
		assertEquals("I got it to work!", tasks.getMessage());
	}

	@Test
	@DisplayName("Validate reading config file")
	public void configTest() {
		try {
			tasks.readConfig();
		} catch (FileNotFoundException e) {
			fail("Couldn't find file.");
			e.printStackTrace();
		} catch (IOException e) {
			fail("File couldn't be read.");
		}
	}

	@Test
	@DisplayName("Check location of todo file from config.")
	public void pathTest() {
		System.out.println(tasks.getConfigPath());
		assertEquals("/home/rav/Nextcloud/Todo/todo.txt", tasks.getConfigPath());
	}
}
