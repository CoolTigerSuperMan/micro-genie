package io.microgenie.commands.mocks;

import io.microgenie.commands.core.FunctionCommands.Func1;
import io.microgenie.commands.core.FunctionCommands.Func2;
import io.microgenie.commands.core.Functions.ReduceFunction;
import io.microgenie.commands.core.Functions.TransformFunction;
import io.microgenie.commands.core.Inputs.Input;
import io.microgenie.commands.core.Inputs.Input1;
import io.microgenie.commands.core.Inputs.Input2;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;


/**
 * Example Functions
 * 
 * @author shawn
 *
 */
public class Functions{

	
	
	/***
	 * A function to print a submitted String
	 */
	public final static Func1<String, String> PRINT_STRING_FUNCTION = new Func1<String, String>(){
		@SuppressWarnings("unchecked")
		@Override
		public String run(Input input) {
			return this.run((Input1<String>)input);
		}
		@Override
		public String run(Input1<String> input) {
			System.out.println("printing page");
			System.out.println("===============");
			System.out.println(input.a);
			return input.a;
		}
	};
	
	/***
	 * Add Two input values and return the result as a string
	 */
	public final static Func2<Integer,Integer,Integer> ADDITION_FUNCTION = new Func2<Integer,Integer,Integer>(){
		@Override
		public Integer run(Input2<Integer, Integer> input) {
			Integer result = input.a + input.b;
			System.out.println("ADDITION_FUNCTION result: " + result);
			return result;
		}
		@SuppressWarnings("unchecked")
		@Override
		public Integer run(Input input) {
			return this.run((Input2<Integer,Integer>)input);
		}};

		
		
		
	/**
	 * An example function that converts an integer to a String
	 */
	 public final static Func1<Integer, String> INTEGER_TO_STRING_FUNCTION = new Func1<Integer,String>(){
		@Override
		public String run(Input1<Integer> input) {
			return input.a.toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public String run(Input input) {
			return this.run((Input1<Integer>)input);
		}
	};
	
		

	public static final ReduceFunction<Object,Integer> COUNT_RESULTS_FUNCTION = new ReduceFunction<Object, Integer>(){
		@Override
		public Integer run(Input input) {
			return 0;
		}
		@Override
		protected Integer reduce(List<Object> from) {
			return from.size();
		}};
	
	
	/**
	 * Reduce pages into one string, truncating each page
	 */
	public static final ReduceFunction<String,String> REDUCE_TO_SINGLE_TRUNCATED_RESULT_FUNCTION = new ReduceFunction<String, String>(){
		@Override
		protected String reduce(List<String> from) {
			final StringBuilder sb = new StringBuilder();
			for(String text : from){
				sb.append(TRUNCATE_STRING.apply(text));	
			}
			return sb.toString();
		}
	};
	
	
	
	/***
	 * Example Function used to collect items and transform output by reducing to one value
	 * @author shawn
	 */
	public static class TRANSFORM_FUNCTION extends TransformFunction<String, String>{
	
		private final List<Object> results = new ArrayList<Object>();
		@Override
		protected void collect(Object result) {
			results.add(result);
		}
		@Override
		protected String transform() {
			StringBuilder sb = new StringBuilder();
			for(Object o : results){
				sb.append(TRUNCATE_STRING.apply(o.toString()));
			}
			return sb.toString();
		}
	}

			
			

			
	/** 
	 * Example Function, used to truncate Strings
	 * 
	 * Truncate string content
	 */
	public static Function<String, String> TRUNCATE_STRING = new Function<String, String>() {
		private static final int MAX_LENGTH = 1024;
		@Override
		public String apply(String text) {
			String pageContent = (text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH): text);
			return pageContent.concat(" ...");
		}
	};
}



