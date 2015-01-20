package io.microgenie.application.queue;

import java.util.List;

/**
 * Producer Interface
 * @author shawn
 */
public interface Producer {
	public void submit(Message message);
	public void submitBatch(List<Message> message);
}
