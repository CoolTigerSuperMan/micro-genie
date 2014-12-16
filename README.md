Micro Genie
=======

A library built on top of Guava's ListenableFuture for composing a pipeline of asynchronous function calls, with both dependent and independent calls in parallel. 

Genie includes the ability to execute asynchronous pipelines with the following benefits:

__Note - Work in progress__

* Typed input parameters (Variable Lengths)
* Chained Commands - Forcing an async call to return and submitting it's result to dependent commands
* Parallel Execution of many commands
* Running batches of commands async. Where batch one must complete before batch2 begins
* Command Timeouts
* Default Fallback Values
* Result Callbacks - onSuccess and onFailure
* Custom functions applied to results
* Thread Pool Groups - Isolation for Group Commands (Think Hystrix) - Useful for isolation against backend service failure
* A Fluent interface


# Use Case

The primary use case is for composing multiple service calls in a Micro Service Architecture, asynchronously while offering the ability to transform / reduce results, provide fallback values, command chaining, short circuiting and bounded thread pool groups modeled after HystrixThreadPools.   

#Examples

The call below executes 5 asynchronous calls but blocks when dependent / chained calls are specified. 

The first call will add 10 + 10 asynchronously. The results will be applied as input to the function ```INTEGER_TO_STRING_FUNCTION```. 

3 Http requests are executed in parallel, each http command provides a fallback value in case the http endpoint is not available. 

The reduce function will accumulate all results and output the number of results obtained   


```java
		int resultCount = commands()
					.withFunction(Functions.ADDITION_FUNCTION, Input.with(10, 10))
					.asInputTo(Functions.INTEGER_TO_STRING_FUNCTION)
					.inParallel(commands().http().get(CNN_URL, "CNN Not Available"))
					.inParallel(commands().http().get(LINKED_IN_URL, "LinkedIn Not Available"))
					.inParallel(commands().http().get(GOOGLE_URL, "Google Not Available"))	
				.queue()
				.reduce(Functions.COUNT_RESULTS_FUNCTION);
		
		
```


```java
	// Print out the number of results obtained
	System.out.println("Results Found: " + resultCount);
```


The Function ```ADDITION_FUNCTION``` is defined as:


```java
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

```


The reduce function implements the abstract ```ReduceFunction<I,O>``` that is part of micro-genie-commands and is defined as: 



```java
	public static final ReduceFunction<Object,Integer> COUNT_RESULTS_FUNCTION = new ReduceFunction<Object, Integer>(){
		@Override
		protected Integer reduce(List<Object> from) {
			return from.size();
		}};
```


Execute 4 Http requests in parallel and for each result print


```java
	CommandResult<String> results = http.get(GOOGLE_URL)
					.inParallel(http.get(CNN_URL))
					.inParallel(http.get(LINKED_IN_URL))
					.inParallel(http.get(GIT_HUB_URL))
				.queue();
				
	
```

For each result -> Print the contents

```java 
	results.forEach(Functions.PRINT_STRING_FUNCTION);
```


# Comparison
Micro Genie commands are somewhat of a hybrid between Hystrix and Trickle, from Netflix and Spotify respectively. Hystrix and Trickle are mature libraries and used in real production environments. If you're looking for an async command framework for use now, it's highly encouraged to check those out.   


# Under Development

Micro Genie is currently under development in a non released stage

The APIs are not completed and will change. Sufficient test coverage does not exist and some methods are currently only stubbed out. 

