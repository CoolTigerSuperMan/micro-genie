package io.microgenie.application.blob;

import java.util.ArrayList;
import java.util.List;

import io.microgenie.commands.core.Functions.ReduceFunction;


/**
 * 
 * @author shawn
 */
public class FileFunctions {
	
	
	/***
	 * A ReduceFunction capable of collecting file paths
	 * @return reduceFunction - {@link ReduceFunction}
	 */
	public static ReduceFunction<List<Object>, List<FilePath>> collectFilePaths(){
		return COLLECT_FILE_PATHS;
	}
	
	
	
	
	/**
	 * Filter out command results and return a {@link List} of {@link FilePath}
	 */
	public static final ReduceFunction<List<Object>, List<FilePath>> COLLECT_FILE_PATHS = new ReduceFunction<List<Object>, List<FilePath>>(){
		@Override
		protected List<FilePath> reduce(List<List<Object>> from) {
			List<FilePath> paths = new ArrayList<FilePath>();
			for(Object input : from){
				if(FilePath.class.isInstance(input)){
					paths.add((FilePath)input);
				}
			}
			return paths;
		}
	};
}
