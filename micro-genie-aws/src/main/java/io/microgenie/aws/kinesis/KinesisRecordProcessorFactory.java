package io.microgenie.aws.kinesis;

import io.microgenie.application.events.EventHandler;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * Record Processor Factory
 * @author shawn
 */
public class KinesisRecordProcessorFactory implements IRecordProcessorFactory{

	private final String topic;
	private final EventHandler handler;
	private final ObjectMapper mapper;
	
	
	public KinesisRecordProcessorFactory(final String topic, EventHandler  handler, final ObjectMapper mapper){
		this.topic = topic;
		this.handler = handler;
		this.mapper = mapper;
	}
	
	
	@Override
	public IRecordProcessor createProcessor() {
		return new KinesisRawEventRecordProcessor(this.topic, this.handler, this.mapper);
	}
}
