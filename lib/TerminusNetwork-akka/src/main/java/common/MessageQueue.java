package edu.buffalo.cse.terminus.common;

import edu.buffalo.cse.terminus.common.message.*;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue<TerminusMessage> extends LinkedList<TerminusMessage> {
 
  private int maxSize;
 
  public MessageQueue (int i) {
	this.maxSize = i;
  }
 
  @Override 
  public boolean add(TerminusMessage o) {
	super.add(o);
	while(this.size() > maxSize) {
		this.remove();
	}
	return true;
  }

}
