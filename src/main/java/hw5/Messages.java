package hw5;

import java.io.File;

/**
 * Messages that are passed around the actors are usually immutable classes.
 * Think how you go about creating immutable classes:) Make them all static
 * classes inside the Messages class.
 * 
 * This class should have all the immutable messages that you need to pass
 * around actors. You are free to add more classes(Messages) that you think is
 * necessary
 * 
 * @author akashnagesh
 *
 */
public class Messages {
	private final File text;
	private final Double c;
	
	public Messages(File text, Double c) {
		this.text = text;
		this.c = c;
	}

	public File getText() {
		return text;
	}

	public Double getC() {
		return c;
	}
	
}