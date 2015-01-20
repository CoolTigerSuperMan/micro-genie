package io.microgenie.aws.kinesis;

import io.microgenie.application.events.EventHandler;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;



/**
 * Record Processor Factory
 * @author shawn
 */
public class KinesisRecordProcessorFactory implements IRecordProcessorFactory{

	private final String topic;
	private final EventHandler handler;
	
	
	
	public KinesisRecordProcessorFactory(final String topic, EventHandler  handler){
		this.topic = topic;
		this.handler = handler;
	}
	
	
	@Override
	public IRecordProcessor createProcessor() {
		return new KinesisRawEventRecordProcessor(this.topic, handler);
	}
}
