package io.microgenie.application.queue;

/***
 * Consumer Interface
 * @author shawn
 */
public interface Consumer {
	public void start(MessageHandler handler);
	public void stop();
	public boolean isRunning();
}
