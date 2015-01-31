package io.microgenie.application.queue;

/***
 * Consumer Interface
 * @author shawn
 */
public interface Consumer {
	public void start();
	public void stop();
	public boolean isRunning();
	
}
