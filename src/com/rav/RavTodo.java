package com.rav;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RavTodo {
	private String message;
	private Properties properties;
	
	public static void main(String args[]) {
		RavTodo todo = new RavTodo();
		try {
			todo.readConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RavTodo() {
		this.message = "I got it to work!";
		this.properties = new Properties();
		try {
			readConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getMessage() {
		return message;
	}

	public void readConfig() throws IOException{
		String fileName = "ravTodo.conf";

		InputStream inputStream = new FileInputStream(fileName);

        this.properties.load(inputStream);
	}

	public String getConfigPath(){
		return this.properties.getProperty("todo.path");
	}
}


