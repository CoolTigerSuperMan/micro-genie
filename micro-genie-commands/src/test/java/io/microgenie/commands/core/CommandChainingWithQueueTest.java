//package io.microgenie.commands.core;
//
//import static org.junit.Assert.assertTrue;
//import io.microgenie.commands.core.CommandResult;
//import io.microgenie.commands.mocks.MockCommands.TestCommand;
//import io.microgenie.commands.mocks.MockCommands.TestInputCommand;
//
//import java.util.Date;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeoutException;
//
//import org.junit.Test;
//
//
//
///***
// * Tests the Command Chaining Features, such as 
// * command.before(new OtherCommand()).execute();
// * 
// * Where command should complete before other command is executed
// * @author shawn
// *
// */
//public class CommandChainingWithQueueTest {
//	
//
////	
////	/**
////	 * An asynchronous command should complete before the next command is executed when .before is called
////	 * @throws ExecutionException 
////	 * @throws TimeoutException 
////	 */
////	@Test
////	public void asyncCommandShouldWaitForCompletionWhenBeforeIsCalled() throws TimeoutException, ExecutionException{
////		/** First Command **/
////		final TestCommand<Long, Long> firstCommand = new TestCommand<Long, Long>(0L,"firstCommand"){
////		
////			@Override
////			public Long run(Long input){
////				try {
////					/** Sleep just to ensure that the second command is run after **/
////					Thread.sleep(1000);
////					return new Date().getTime();
////				} catch (InterruptedException e) {
////					throw new RuntimeException(e.getMessage(),e);
////				}	
////			}
////		};
////		
////		/** Second Command **/
////		final TestCommand<Long, Long> secondCommand = new TestCommand<Long, Long> (0L, "secondCommand"){
////			@Override
////			public Long run(Long input){
////				return  new Date().getTime();
////			}
////		};
////		CommandResult<Long> firstResult = firstCommand
////				.before(secondCommand)
////				.queue();
////		
////		long firstCommandEndTime = ((long)firstResult.get());
////		long secondCommandStartTime = ((long)firstResult.getChildren().get(0).get());
////		assertTrue(firstCommandEndTime<secondCommandStartTime);	
////	}
////	
////	
//	
//
//	
//	/**
//	 * An asynchronous command should complete before the next command is executed when asInputTo
//	 * is called, and the first command should submit it's result to the second command. 
//	 * 
//	 * @throws ExecutionException 
//	 * @throws TimeoutException 
//	 */
//	@Test
//	public void asyncCommandShouldWaitForCompletionWhenAsInputToIsCalledAndSubmitInput() throws TimeoutException, ExecutionException{
//		/** First Command **/
//		final TestCommand<Long,Long> firstCommand = new TestCommand<Long, Long>(0L,"firstCommand"){
//			@Override
//			public Long run(Long input){
//				try {
//					/** Sleep just to ensure that the second command is run after **/
//					Thread.sleep(1000);
//					return new Date().getTime();
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e.getMessage(),e);
//				}	
//			}
//		};
//		
//		
//		/** Second Command **/
//		final TestInputCommand<Long, Long> secondCommand = new TestInputCommand<Long, Long> (0L, "secondCommand");
//
//		
//		/** Queue of the commands, chaining the result from command 1 to the result from command 2 **/
//		final CommandResult<Long> firstResult = firstCommand
//				.into(secondCommand)
//				.queue();
//		
//		long firstCommandEndTime = ((long)firstResult.get());
//		long secondCommandStartTime = ((long)firstResult.getChildren().get(0).get());
//		assertTrue(firstCommandEndTime==secondCommandStartTime);	
//	}
//}
