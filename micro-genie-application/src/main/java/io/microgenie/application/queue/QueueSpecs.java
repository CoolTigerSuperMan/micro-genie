package io.microgenie.application.queue;

import io.microgenie.application.commands.QueueCommandFactory.ToMessageFunction;


/***
 * Queue Specifications used to Queue Commands
 * @author shawn
 *
 */
public class QueueSpecs {

	
	/***
	 * A default Queue Input specification contains the queue name and a function that can parse input into 
	 * a Queue message
	 * @author shawn
	 *
	 * @param <I> - The type passed as input into a message producer when using command chaining
	 */
	public static class DefaultQueueInputSpec<I>{
		private final String queue;
		private final ToMessageFunction<I> toMessageFunction;
		
		public DefaultQueueInputSpec(final String queue, final ToMessageFunction<I> toMessageFunction){
			this.queue = queue;
			this.toMessageFunction = toMessageFunction;
		}
		public String getQueue() {
			return queue;
		}
		public ToMessageFunction<I> getToMessageFunction() {
			return toMessageFunction;
		}
	}
}
