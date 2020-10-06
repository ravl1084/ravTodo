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
		assertEquals("/home/rav/Nextcloud/Todo/", tasks.getConfigPath());
	}

	@Test
	@DisplayName("Check if todo file was read.")
	public void readTodoFileTest(){
		try {
			tasks.readTodoFile();
			if (tasks.getTodoCount() == 0) {
				fail("File contents were not loaded");
			}
		} catch (IOException e) {
			fail("Couldn't read file");
			e.printStackTrace();
		}
	}
}
