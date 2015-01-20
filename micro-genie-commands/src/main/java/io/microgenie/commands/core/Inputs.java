package io.microgenie.commands.core;


/**
 * Typed input parameters for variable function signatures
 * @author shawn
 *
 */
public class Inputs {

	/**
	 * Marker interface for all Input types
	 */
	public static abstract class Input{
		public static  <A> Input1<A> with(A a){
			return new Input1<A>(a);
		}
		public static  <A, B> Input2<A, B> with(A a, B b){
			return new Input2<A,B>(a, b);
		}
		public static  <A, B, C> Input3<A,B,C> with(A a, B b, C c){
			return new Input3<A,B,C>(a, b, c);
		}
		public static  <A, B, C, D> Input4<A,B,C,D> with(A a, B b, C c, D d){
			return new Input4<A,B,C, D>(a, b, c, d);
		}
		public static  <A, B, C, D, E> Input5<A,B,C,D,E> with(A a, B b, C c, D d, E e){
			return new Input5<A,B,C, D,E>(a, b, c, d,e);
		}
	}
	
	
	
	/**
	 * Command Input with one argument
	 * @param <A>
	 */
	public static class Input1<A> extends Input{
		public A a;
		public Input1(A a){
			this.a=a;
		}
	}
	/**
	 * Command Input with two arguments
	 * @param <A>
	 * @param <B>
	 */
	public static class Input2<A, B> extends Input{
		public A a;
		public B b;
		public Input2(A a, B b){
			this.a=a;
			this.b=b;
		}
	}
	/**
	 * Command Input with three arguments
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 */
	public static class Input3<A, B, C> extends Input{
		public A a;
		public B b;
		public C c;
		public Input3(A a, B b, C c){
			this.a=a;
			this.b=b;
			this.c=c;
		}
	}
	/**
	 * Command Input with four arguments
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param <D>
	 */
	public static class Input4<A, B, C, D> extends Input{
		public A a;
		public B b;
		public C c;
		public D d;
		public Input4(A a, B b, C c, D d){
			this.a=a;
			this.b=b;
			this.c=c;
			this.d=d;
		}
	}
	/**
	 * Command Input with five arguments
	 * @param <A>
	 * @param <B>
	 * @param <C>
	 * @param <D>
	 * @param <E>
	 */
	public static class Input5<A, B, C, D, E> extends Input{
		public A a;
		public B b;
		public C c;
		public D d;
		public E e;
		public Input5(A a, B b, C c, D d, E e){
			this.a=a;
			this.b=b;
			this.c=c;
			this.d=d;
			this.e=e;
		}
	}
	
}