package edu.buffalo.cse.terminus.common.message;

import java.io.*;

public abstract class TerminusMessage implements Serializable {

	private final String id;

	public TerminusMessage(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	} 
  
}
