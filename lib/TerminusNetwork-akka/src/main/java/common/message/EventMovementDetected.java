package edu.buffalo.cse.terminus.common.message;

import java.awt.image.BufferedImage;

public class EventMovementDetected extends EventMessage {

	private BufferedImage snapshot;

	public EventMovementDetected(String id) {
		super(id);
	}

	public EventMovementDetected(BufferedImage image, String id) {
		super(id);
		this.snapshot = image;
	}

	public void setSnapshot(BufferedImage image) {
		this.snapshot = image;
	}

	public BufferedImage getSnapshot() {
		return this.snapshot;
	}

}
