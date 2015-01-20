package io.microgenie.application.queue;

import java.util.List;

/**
 * MessageHandler Interface
 * @author shawn
 */
public interface MessageHandler {
	public void handle(Message message);
	public void handleBatch(List<Message> messages);
}
