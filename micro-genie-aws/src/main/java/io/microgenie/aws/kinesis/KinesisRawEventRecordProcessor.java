package io.microgenie.aws.kinesis;



import io.microgenie.application.events.Event;
import io.microgenie.application.events.EventHandler;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.route53.model.ThrottlingException;

/**
* Kinesis Record Processor
*/
public class KinesisRawEventRecordProcessor  implements IRecordProcessor {
   
   private static final Logger LOGGER = LoggerFactory.getLogger(KinesisRawEventRecordProcessor.class);
   
   private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
   private static final int NUM_RETRIES = 10;
   private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
   
   
   private final String topic;
   private final EventHandler handler;
   
   private String kinesisShardId;
   private long nextCheckpointTimeInMillis;		
   
   
   /**
    * Constructor.
    */
   public KinesisRawEventRecordProcessor(final String topic, final EventHandler  handler) {
       this.topic = topic;
       this.handler = handler;
   }
   
   
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize(final String shardId) {
       LOGGER.info("Initializing record processor for shard: {}", shardId);
       this.kinesisShardId = shardId;
   }

   
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void processRecords(final List<Record> records, final IRecordProcessorCheckpointer checkpointer) {
	   LOGGER.info("Processing {} records from ", records.size(), kinesisShardId);
       this.processRecordsWithRetries(records);
       if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
           this.checkpoint(checkpointer);
           this.nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
       }
   }

   


   
   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
       LOGGER.info("Shutting down record processor for shard: " + kinesisShardId);
       /** Important to checkpoint after reaching end of shard, so we can start processing data from child shards. **/
       if (reason == ShutdownReason.TERMINATE) {
           checkpoint(checkpointer);
       }
   }
   
   

   
   /** Process records performing retries as needed. Skip "poison pill" records.
    * @param records
    */
   private void processRecordsWithRetries(List<Record> records) {
	   for(Record record : records) {
		   boolean processedSuccessfully = false;
           for (int i = 0; i < NUM_RETRIES; i++) {
        	   processedSuccessfully = this.processRecord(record); 
    		   if(processedSuccessfully){
    			   break;   
    		   }
    		   /** If here then a failure occurred, let's back off **/
        	   this.backOff();
           }
           if (!processedSuccessfully){
               LOGGER.error("Couldn't process record {} - skipping record", record);
               /** TODO add an optional failure handler here, such as a dead letter queue **/
           }
       }
   }



   /***
    * Process the record by submitting to the handler
    * <p> 
    * If the handler throws an exception then false is returned. 
    * If the handler successfully returns, the true is returned 
    * @param record
    * @return true - if successful, otherwise false
    */
	private boolean processRecord(Record record) {
		
		try{
			
			final ByteBuffer buffer = record.getData();
			final byte[] byteArray = buffer.array();
			
			LOGGER.trace("sequence number: {}, partitionKey: {}, data:{}", record.getSequenceNumber(), record.getPartitionKey(), new String(byteArray));
	         
			final Event event = new Event(this.topic, record.getPartitionKey(), byteArray);
			handler.handle(event);
	        return true;			
	        
		}catch(Exception ex){
			LOGGER.warn("Failed to process record with sequenceNumber: {} and partitionKey: {} - retries will be attempted", record.getSequenceNumber(), record.getPartitionKey());
		}
		
		return false;
	}

   

   /** Checkpoint with retries.
    * @param checkpointer
    */
   private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
       LOGGER.info("Checkpointing shard " + kinesisShardId);
       for (int i = 0; i < NUM_RETRIES; i++) {
           try {
               checkpointer.checkpoint();
               break;
           } catch (ShutdownException se) {
               /** Ignore checkpoint if the processor instance has been shutdown (fail over). **/
               LOGGER.info("Caught shutdown exception, skipping checkpoint.", se);
               break;
           } catch (ThrottlingException e) {
               /** Backoff and re-attempt checkpoint upon transient failures **/
               if (i >= (NUM_RETRIES - 1)) {
                   LOGGER.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                   break;
               } else {
                   LOGGER.info("Transient issue when checkpointing - attempt " + (i + 1) + " of " + NUM_RETRIES, e);
               }
           } catch (InvalidStateException e) {
               /** This indicates an issue with the DynamoDB table (check for table, provisioned IOPS). **/
               LOGGER.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
               break;
           }
           this.backOff();
       }
       
   }

   	/***
   	 * Called to back off when processing failed records
   	 */
   	private void backOff() {
   		try {
   	       Thread.sleep(BACKOFF_TIME_IN_MILLIS);
   		} catch (InterruptedException e) {
   	       LOGGER.debug("Interrupted sleep", e);
   		}
   	}
}