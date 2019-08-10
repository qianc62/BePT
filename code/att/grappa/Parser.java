/***********************************************************
 *      This software is part of the graphviz package      *
 *                http://www.graphviz.org/                 *
 *                                                         *
 *            Copyright (c) 1994-2004 AT&T Corp.           *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *                      by AT&T Corp.                      *
 *                                                         *
 *        Information and Software Systems Research        *
 *              AT&T Research, Florham Park NJ             *
 **********************************************************/

package att.grappa;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Vector;

import java_cup_grappa.runtime.*;

/** CUP v0.10j generated parser.
 * @version Wed May 08 21:20:09 EDT 2002
 */
/**
 * This class provides a parser for the <i>dot</i> graph representation format.
 * It is used in conjunction with JavaCup, a yacc-like parser generator
 * originally by:
 * <p>
 * <center> <a
 * href="http://www.cc.gatech.edu/gvu/people/Faculty/Scott.E.Hudson.html">Scott
 * E. Hudson</a><br>
 * Graphics Visualization and Usability Center<br>
 * Georgia Institute of Technology<br>
 * </center>
 * </p>
 * and more recently modified and maintained by <a
 * href="http://www.cs.princeton.edu/~appel/modern/java/CUP/"> a number of
 * people at Princeton University</a>.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */

public class Parser extends java_cup_grappa.runtime.lr_parser {

	/** Default constructor. */
	public Parser() {
		super();
	}

	/** Constructor which sets the default scanner. */
	public Parser(java_cup_grappa.runtime.Scanner s) {
		super(s);
	}

	/** Production table. */
	protected static final short _production_table[][] = unpackFromStrings(new String[] { "\000\077\000\002\002\004\000\002\042\002\000\002\013"
			+ "\005\000\002\013\003\000\002\013\002\000\002\014\005"
			+ "\000\002\014\004\000\002\014\004\000\002\003\003\000"
			+ "\002\003\002\000\002\004\003\000\002\004\003\000\002"
			+ "\010\003\000\002\010\002\000\002\015\005\000\002\016"
			+ "\003\000\002\016\002\000\002\017\004\000\002\017\003"
			+ "\000\002\020\004\000\002\020\004\000\002\024\005\000"
			+ "\002\025\003\000\002\025\003\000\002\041\003\000\002"
			+ "\041\003\000\002\043\002\000\002\005\006\000\002\005"
			+ "\002\000\002\027\003\000\002\027\005\000\002\031\004"
			+ "\000\002\012\004\000\002\012\002\000\002\021\005\000"
			+ "\002\021\003\000\002\006\003\000\002\006\003\000\002"
			+ "\006\003\000\002\011\004\000\002\011\002\000\002\026"
			+ "\003\000\002\026\002\000\002\032\006\000\002\034\003"
			+ "\000\002\034\002\000\002\035\003\000\002\035\005\000"
			+ "\002\036\003\000\002\036\003\000\002\037\005\000\002"
			+ "\040\004\000\002\033\003\000\002\044\002\000\002\030"
			+ "\005\000\002\007\004\000\002\007\003\000\002\007\002"
			+ "\000\002\022\003\000\002\022\002\000\002\023\003\000"
			+ "\002\023\003\000\002\023\002" });

	/** Access to production table. */
	public short[][] production_table() {
		return _production_table;
	}

	/** Parse-action table. */
	protected static final short[][] _action_table = unpackFromStrings(new String[] { "\000\121\000\020\002\ufffd\003\010\004\ufff8\012\007\013"
			+ "\ufff8\014\004\015\005\001\002\000\006\020\ufff4\027\120"
			+ "\001\002\000\006\020\ufff4\027\120\001\002\000\006\004"
			+ "\117\013\116\001\002\000\006\004\ufff9\013\ufff9\001\002"
			+ "\000\004\002\ufffe\001\002\000\004\002\114\001\002\000"
			+ "\004\020\000\001\002\000\004\020\015\001\002\000\004"
			+ "\002\uffff\001\002\000\020\004\026\005\037\006\025\007"
			+ "\016\020\uffc8\021\ufff1\027\022\001\002\000\006\020\uffc9"
			+ "\027\113\001\002\000\022\004\uffcd\005\uffcd\006\uffcd\007"
			+ "\uffcd\016\uffcd\020\uffcd\021\uffcd\027\uffcd\001\002\000\022"
			+ "\004\uffde\005\uffde\006\uffde\007\uffde\016\uffde\020\uffde\021"
			+ "\uffde\027\uffde\001\002\000\004\021\112\001\002\000\036"
			+ "\004\uffe0\005\uffe0\006\uffe0\007\uffe0\010\uffe0\011\uffe0\016"
			+ "\uffe0\017\uffe0\020\uffe0\021\uffe0\022\uffe0\024\063\025\071"
			+ "\027\uffe0\001\002\000\030\004\uffea\005\uffea\006\uffea\007"
			+ "\uffea\010\uffea\011\uffea\016\uffea\020\uffea\021\uffea\022\uffea"
			+ "\027\uffea\001\002\000\004\020\uffcc\001\002\000\006\022"
			+ "\uffdb\027\uffdb\001\002\000\006\022\uffdd\027\uffdd\001\002"
			+ "\000\032\004\uffe4\005\uffe4\006\uffe4\007\uffe4\010\uffe4\011"
			+ "\uffe4\016\uffe4\017\uffe4\020\uffe4\021\uffe4\022\uffe4\027\uffe4"
			+ "\001\002\000\022\004\uffc6\005\uffc6\006\uffc6\007\uffc6\016"
			+ "\074\020\uffc6\021\uffc6\027\uffc6\001\002\000\030\004\uffe5"
			+ "\005\uffe5\006\uffe5\007\uffe5\010\101\011\077\016\uffe5\020"
			+ "\uffe5\021\uffe5\022\uffe5\027\uffe5\001\002\000\022\004\uffc6"
			+ "\005\uffc6\006\uffc6\007\uffc6\016\074\020\uffc6\021\uffc6\027"
			+ "\uffc6\001\002\000\032\004\uffeb\005\uffeb\006\uffeb\007\uffeb"
			+ "\010\uffeb\011\uffeb\016\uffeb\017\066\020\uffeb\021\uffeb\022"
			+ "\uffeb\027\uffeb\001\002\000\020\004\uffef\005\uffef\006\uffef"
			+ "\007\uffef\020\uffef\021\uffef\027\uffef\001\002\000\006\022"
			+ "\uffd9\027\041\001\002\000\020\004\026\005\037\006\025"
			+ "\007\016\020\uffc8\021\ufff2\027\022\001\002\000\006\022"
			+ "\uffdc\027\uffdc\001\002\000\020\004\ufff0\005\ufff0\006\ufff0"
			+ "\007\ufff0\020\ufff0\021\ufff0\027\ufff0\001\002\000\004\024"
			+ "\065\001\002\000\004\022\uffd7\001\002\000\004\022\045"
			+ "\001\002\000\024\004\uffdf\005\uffdf\006\uffdf\007\uffdf\016"
			+ "\uffdf\020\uffdf\021\uffdf\022\uffd8\027\uffdf\001\002\000\010"
			+ "\023\uffd4\026\051\027\046\001\002\000\004\024\063\001"
			+ "\002\000\014\016\060\017\057\023\uffd5\026\uffc3\027\uffc3"
			+ "\001\002\000\014\016\uffd1\017\uffd1\023\uffd1\026\uffd1\027"
			+ "\uffd1\001\002\000\004\027\056\001\002\000\014\016\uffd3"
			+ "\017\uffd3\023\uffd3\026\uffd3\027\uffd3\001\002\000\014\016"
			+ "\uffd0\017\uffd0\023\uffd0\026\uffd0\027\uffd0\001\002\000\004"
			+ "\023\055\001\002\000\024\004\uffd6\005\uffd6\006\uffd6\007"
			+ "\uffd6\016\uffd6\020\uffd6\021\uffd6\022\uffd6\027\uffd6\001\002"
			+ "\000\014\016\uffce\017\uffce\023\uffce\026\uffce\027\uffce\001"
			+ "\002\000\006\026\uffc4\027\uffc4\001\002\000\006\026\uffc5"
			+ "\027\uffc5\001\002\000\006\026\051\027\046\001\002\000"
			+ "\014\016\uffd2\017\uffd2\023\uffd2\026\uffd2\027\uffd2\001\002"
			+ "\000\004\027\064\001\002\000\030\004\uffcf\005\uffcf\006"
			+ "\uffcf\007\uffcf\016\uffcf\017\uffcf\020\uffcf\021\uffcf\023\uffcf"
			+ "\026\uffcf\027\uffcf\001\002\000\004\022\uffda\001\002\000"
			+ "\004\027\067\001\002\000\034\004\uffe0\005\uffe0\006\uffe0"
			+ "\007\uffe0\010\uffe0\011\uffe0\016\uffe0\017\uffe0\020\uffe0\021"
			+ "\uffe0\022\uffe0\025\071\027\uffe0\001\002\000\032\004\uffe3"
			+ "\005\uffe3\006\uffe3\007\uffe3\010\uffe3\011\uffe3\016\uffe3\017"
			+ "\uffe3\020\uffe3\021\uffe3\022\uffe3\027\uffe3\001\002\000\004"
			+ "\027\073\001\002\000\032\004\uffe2\005\uffe2\006\uffe2\007"
			+ "\uffe2\010\uffe2\011\uffe2\016\uffe2\017\uffe2\020\uffe2\021\uffe2"
			+ "\022\uffe2\027\uffe2\001\002\000\032\004\uffe1\005\uffe1\006"
			+ "\uffe1\007\uffe1\010\uffe1\011\uffe1\016\uffe1\017\uffe1\020\uffe1"
			+ "\021\uffe1\022\uffe1\027\uffe1\001\002\000\020\004\uffc7\005"
			+ "\uffc7\006\uffc7\007\uffc7\020\uffc7\021\uffc7\027\uffc7\001\002"
			+ "\000\020\004\uffed\005\uffed\006\uffed\007\uffed\020\uffed\021"
			+ "\uffed\027\uffed\001\002\000\010\007\uffe7\020\uffe7\027\uffe7"
			+ "\001\002\000\010\007\uffe8\020\uffe8\027\uffe8\001\002\000"
			+ "\024\004\uffd7\005\uffd7\006\uffd7\007\uffd7\016\uffd7\020\uffd7"
			+ "\021\uffd7\022\uffd7\027\uffd7\001\002\000\010\007\uffe9\020"
			+ "\uffe9\027\uffe9\001\002\000\024\004\uffec\005\uffec\006\uffec"
			+ "\007\uffec\016\uffec\020\uffec\021\uffec\022\045\027\uffec\001"
			+ "\002\000\024\004\uffd8\005\uffd8\006\uffd8\007\uffd8\016\uffd8"
			+ "\020\uffd8\021\uffd8\022\uffd8\027\uffd8\001\002\000\010\007"
			+ "\016\020\uffc8\027\067\001\002\000\030\004\uffe5\005\uffe5"
			+ "\006\uffe5\007\uffe5\010\101\011\077\016\uffe5\020\uffe5\021"
			+ "\uffe5\022\uffe5\027\uffe5\001\002\000\024\004\uffe6\005\uffe6"
			+ "\006\uffe6\007\uffe6\016\uffe6\020\uffe6\021\uffe6\022\uffe6\027"
			+ "\uffe6\001\002\000\020\004\uffee\005\uffee\006\uffee\007\uffee"
			+ "\020\uffee\021\uffee\027\uffee\001\002\000\004\020\015\001"
			+ "\002\000\030\004\uffcb\005\uffcb\006\uffcb\007\uffcb\010\uffcb"
			+ "\011\uffcb\016\uffcb\020\uffcb\021\uffcb\022\uffcb\027\uffcb\001"
			+ "\002\000\032\002\ufff3\004\ufff3\005\ufff3\006\ufff3\007\ufff3"
			+ "\010\ufff3\011\ufff3\016\ufff3\020\ufff3\021\ufff3\022\ufff3\027"
			+ "\ufff3\001\002\000\004\020\uffca\001\002\000\004\002\001"
			+ "\001\002\000\006\020\ufff4\027\120\001\002\000\006\020"
			+ "\ufff6\027\ufff6\001\002\000\006\020\ufff7\027\ufff7\001\002"
			+ "\000\004\020\ufff5\001\002\000\004\020\ufffc\001\002\000"
			+ "\004\020\ufffa\001\002\000\004\020\ufffb\001\002" });

	/** Access to parse-action table. */
	public short[][] action_table() {
		return _action_table;
	}

	/** <code>reduce_goto</code> table. */
	protected static final short[][] _reduce_table = unpackFromStrings(new String[] { "\000\121\000\010\003\005\013\010\014\011\001\001\000"
			+ "\004\010\122\001\001\000\004\010\121\001\001\000\004"
			+ "\004\114\001\001\000\002\001\001\000\002\001\001\000"
			+ "\002\001\001\000\004\042\012\001\001\000\004\015\013"
			+ "\001\001\000\002\001\001\000\034\006\034\007\023\016"
			+ "\020\017\035\020\033\021\027\024\031\025\030\027\032"
			+ "\030\022\031\026\033\017\037\016\001\001\000\002\001"
			+ "\001\000\002\001\001\000\002\001\001\000\002\001\001"
			+ "\000\004\012\071\001\001\000\002\001\001\000\004\044"
			+ "\107\001\001\000\002\001\001\000\002\001\001\000\002"
			+ "\001\001\000\004\022\106\001\001\000\006\005\077\041"
			+ "\075\001\001\000\004\022\074\001\001\000\002\001\001"
			+ "\000\002\001\001\000\004\011\041\001\001\000\030\006"
			+ "\034\007\023\020\037\021\027\024\031\025\030\027\032"
			+ "\030\022\031\026\033\017\037\016\001\001\000\002\001"
			+ "\001\000\002\001\001\000\002\001\001\000\006\026\042"
			+ "\032\043\001\001\000\002\001\001\000\002\001\001\000"
			+ "\014\034\053\035\046\036\051\037\047\040\052\001\001"
			+ "\000\002\001\001\000\004\023\060\001\001\000\002\001"
			+ "\001\000\002\001\001\000\002\001\001\000\002\001\001"
			+ "\000\002\001\001\000\002\001\001\000\002\001\001\000"
			+ "\002\001\001\000\002\001\001\000\010\036\061\037\047"
			+ "\040\052\001\001\000\002\001\001\000\002\001\001\000"
			+ "\002\001\001\000\002\001\001\000\004\031\067\001\001"
			+ "\000\004\012\071\001\001\000\002\001\001\000\002\001"
			+ "\001\000\002\001\001\000\002\001\001\000\002\001\001"
			+ "\000\002\001\001\000\004\043\103\001\001\000\002\001"
			+ "\001\000\006\026\101\032\102\001\001\000\002\001\001"
			+ "\000\002\001\001\000\002\001\001\000\014\007\023\025"
			+ "\104\027\032\030\022\031\026\001\001\000\006\005\105"
			+ "\041\075\001\001\000\002\001\001\000\002\001\001\000"
			+ "\004\015\110\001\001\000\002\001\001\000\002\001\001"
			+ "\000\002\001\001\000\002\001\001\000\004\010\120\001"
			+ "\001\000\002\001\001\000\002\001\001\000\002\001\001"
			+ "\000\002\001\001\000\002\001\001\000\002\001\001" });

	/** Access to <code>reduce_goto</code> table. */
	public short[][] reduce_table() {
		return _reduce_table;
	}

	/** Instance of action encapsulation class. */
	protected CUP$Parser$actions action_obj;

	/** Action encapsulation object initializer. */
	protected void init_actions() {
		action_obj = new CUP$Parser$actions(this);
	}

	/** Invoke a user supplied parse action. */
	public java_cup_grappa.runtime.Symbol do_action(int act_num,
			java_cup_grappa.runtime.lr_parser parser, java.util.Stack stack,
			int top) throws java.lang.Exception {
		/* call code in generated class */
		return action_obj.CUP$Parser$do_action(act_num, parser, stack, top);
	}

	/** Indicates start state. */
	public int start_state() {
		return 0;
	}

	/** Indicates start production. */
	public int start_production() {
		return 0;
	}

	/** <code>EOF</code> Symbol index. */
	public int EOF_sym() {
		return 0;
	}

	/** <code>error</code> Symbol index. */
	public int error_sym() {
		return 1;
	}

	/** User initialization code. */
	public void user_init() throws java.lang.Exception {

		lexer.init();
		action_obj.graph = theGraph;
		// action_obj.parser = this;

	}

	/** Scan to get the next Symbol. */
	public java_cup_grappa.runtime.Symbol scan() throws java.lang.Exception {
		return lexer.next_token(debugLevel);
	}

	private Graph theGraph = null;
	private Reader inReader;
	private PrintWriter errWriter;
	private Lexer lexer;
	private int debugLevel = 0;

	/**
	 * Create an instance of <code>Parser</code> with input, error output and a
	 * supplied <code>Graph</code> object. The graph object is cleared (reset)
	 * before new graph components are added to it by this parsing operation.
	 * 
	 * @param inputReader
	 *            input <code>Reader</code> object
	 * @param errorWriter
	 *            error output <code>Writer</code> object (or null to suppress
	 *            error output)
	 * @param graph
	 *            <code>Graph</code> object for storing parsed graph information
	 *            (or null to create a new object)
	 */
	public Parser(Reader inputReader, PrintWriter errorWriter, Graph graph) {
		super();
		inReader = inputReader;
		errWriter = errorWriter;
		theGraph = graph;
		lexer = new Lexer(inputReader, errorWriter);
	}

	/**
	 * A convenience constructor equivalent to
	 * <code>Parser(inputReader,errorWriter,null)</code>.
	 * 
	 * @param inputReader
	 *            input <code>Reader</code> object
	 * @param errorWriter
	 *            error output <code>Writer</code> object (or null to suppress
	 *            error output)
	 */
	public Parser(Reader inputReader, PrintWriter errorWriter) {
		this(inputReader, errorWriter, null);
	}

	/**
	 * A convenience constructor equivalent to
	 * <code>Parser(inputReader,null,null)</code>.
	 * 
	 * @param inputReader
	 *            input <code>Reader</code> object
	 */
	public Parser(Reader inputReader) {
		this(inputReader, (PrintWriter) null, null);
	}

	/**
	 * Create an instance of <code>Parser</code> with input, error output and a
	 * supplied <code>Graph</code> object. The input stream is converted to a
	 * <code>Reader</code> and the error stream is converted to a
	 * <code>Writer</code>.
	 * 
	 * @param inputStream
	 *            input <code>InputStream</code> object
	 * @param errorStream
	 *            error output <code>OutputStream</code> object (or null to
	 *            suppress error output)
	 * @param graph
	 *            <code>Graph</code> object for storing parsed graph information
	 *            (or null to create a new object)
	 */
	public Parser(InputStream inputStream, OutputStream errorStream, Graph graph) {
		this(new InputStreamReader(inputStream), new PrintWriter(errorStream,
				true), graph);
	}

	/**
	 * A convenience constructor equivalent to
	 * <code>Parser(inputStream,errorStream,null)</code>.
	 * 
	 * @param inputStream
	 *            input <code>InputStream</code> object
	 * @param errorStream
	 *            error output <code>OutputStream</code> object
	 */
	public Parser(InputStream inputStream, OutputStream errorStream) {
		this(new InputStreamReader(inputStream), new PrintWriter(errorStream,
				true), null);
	}

	/**
	 * A convenience constructor equivalent to
	 * <code>Parser(inputStream,null,null)</code>.
	 * 
	 * @param inputStream
	 *            input <code>InputStream</code> object
	 */
	public Parser(InputStream inputStream) {
		this(new InputStreamReader(inputStream), (PrintWriter) null, null);
	}

	/**
	 * Get the <code>Lexer</code> object associated with this parser.
	 * 
	 * @return the associated lexical analyzer.
	 */
	public Lexer getLexer() {
		return lexer;
	}

	/**
	 * Get the error writer, if any, for this parser.
	 * 
	 * @return the error writer for this parser.
	 */
	public PrintWriter getErrorWriter() {
		return (errWriter);
	}

	/**
	 * Get the debug level for this parser. The debug level is set to a non-zero
	 * value by calling <code>debug_parse</code>.
	 * 
	 * @return the debug level of this parser.
	 * @see Parser#debug_parse(int)
	 */
	public int getDebugLevel() {
		return (debugLevel);
	}

	/**
	 * Report a fatal error. Calling this method will throw a
	 * <code>GraphParserException</code>.
	 * 
	 * @param message
	 *            the error message to send to the error stream and include in
	 *            the thrown exception
	 * @param info
	 *            not used
	 * 
	 * @exception GraphParserException
	 *                whenver this method is called
	 */
	public void report_error(String message, Object info)
			throws GraphParserException {
		String loc = getLexer().getLocation();
		if (errWriter != null) {
			errWriter.println("ERROR: Parser" + loc + ": " + message);
		}
		throw new GraphParserException("at " + loc + ": " + message);
	}

	/**
	 * Report a non-fatal error.
	 * 
	 * @param message
	 *            the warning message to send to the error stream, if the stream
	 *            non-null.
	 * @param info
	 *            not used
	 */
	public void report_warning(String message, Object info) {
		String loc = getLexer().getLocation();
		if (errWriter != null) {
			errWriter.println("WARNING: Parser" + loc + ": " + message);
		}
	}

	/**
	 * Write a debugging message to the error stream. The debug level of the
	 * message is 5.
	 * 
	 * @param message
	 *            the debug message to send to the error stream, if the stream
	 *            non-null.
	 * @see Parser#debug_message(int,String)
	 */
	public void debug_message(String message) {
		debug_message(5, message);
	}

	/**
	 * Write a debugging message to the error stream. A message is written only
	 * if the error stream is not null and the debug level of the message is
	 * greater than or equal to the debugging level of the parser.
	 * 
	 * @param level
	 *            the level of the message
	 * @param message
	 *            the debug message to send to the error stream, if the stream
	 *            non-null.
	 * @see Parser#getDebugLevel()
	 */
	public void debug_message(int level, String message) {
		if (debugLevel < level) {
			return;
		}
		String loc = getLexer().getLocation();
		if (errWriter != null) {
			errWriter.println("DEBUG: Parser" + loc + ": " + message);
		}
	}

	/**
	 * Invokes the parser in debug mode. The lowering the debug level reduces
	 * the amount of debugging output. A level of 0 inhibits all debugging
	 * messages, generally a level of 10 will let all messages get through.
	 * 
	 * @param debug
	 *            the debug level to use for filtering debug messages based on
	 *            priority.
	 * @exception Exception
	 *                if <code>parse()</code> does
	 */
	public Symbol debug_parse(int debug) throws java.lang.Exception {
		if (debug == 0) {
			return parse();
		}

		debugLevel = debug;

		/* the current action code */
		int act;

		/* the Symbol/stack element returned by a reduce */
		Symbol lhs_sym = null;

		/* information about production being reduced with */
		short handle_size, lhs_sym_num;

		/* set up direct reference to tables to drive the parser */
		production_tab = production_table();
		action_tab = action_table();
		reduce_tab = reduce_table();

		debug_message(5, "# Initializing parser");

		/* initialize the action encapsulation object */
		init_actions();

		/* do user initialization */
		user_init();

		/* the current Symbol */
		cur_token = scan();

		debug_message(5, "# Current Symbol is #" + cur_token.sym);

		/* push dummy Symbol with start state to get us underway */
		stack.push(new Symbol(0, start_state()));
		tos = 0;

		/* continue until we are told to stop */
		for (_done_parsing = false; !_done_parsing;) {
			/* current state is always on the top of the stack */

			/* look up action out of the current state with the current input */
			act = get_action(((Symbol) stack.peek()).parse_state, cur_token.sym);

			/* decode the action -- > 0 encodes shift */
			if (act > 0) {
				/* shift to the encoded state by pushing it on the stack */
				cur_token.parse_state = act - 1;
				debug_shift(cur_token);
				stack.push(cur_token);
				tos++;

				/* advance to the next Symbol */
				cur_token = scan();
				debug_message(5, "# Current token is #" + cur_token.sym);
			}
			/* if its less than zero, then it encodes a reduce action */
			else if (act < 0) {
				/* perform the action for the reduce */
				lhs_sym = do_action((-act) - 1, this, stack, tos);

				/* look up information about the production */
				lhs_sym_num = production_tab[(-act) - 1][0];
				handle_size = production_tab[(-act) - 1][1];

				debug_reduce((-act) - 1, lhs_sym_num, handle_size);

				/* pop the handle off the stack */
				for (int i = 0; i < handle_size; i++) {
					stack.pop();
					tos--;
				}

				/* look up the state to go to from the one popped back to */
				act = get_reduce(((Symbol) stack.peek()).parse_state,
						lhs_sym_num);

				/* shift to that state */
				lhs_sym.parse_state = act;
				stack.push(lhs_sym);
				tos++;

				debug_message(5, "# Goto state #" + act);
			}
			/* finally if the entry is zero, we have an error */
			else if (act == 0) {
				/* call user syntax error reporting routine */
				syntax_error(cur_token);

				/* try to error recover */
				if (!error_recovery(true)) {
					/* if that fails give up with a fatal syntax error */
					unrecovered_syntax_error(cur_token);

					/* just in case that wasn't fatal enough, end parse */
					done_parsing();
				} else {
					lhs_sym = (Symbol) stack.peek();
				}
			}
		}
		return lhs_sym;
	}

	CUP$Parser$actions getActionObject() {
		return action_obj;
	}

	/**
	 * Get the graph resulting from the parsing operations.
	 * 
	 * @return the graph generated from the input.
	 */
	public Graph getGraph() {
		return action_obj.graph;
	}

}

/** Cup generated class to encapsulate user supplied action code. */
class CUP$Parser$actions {

	// a list of variables used in action code during grammar translation
	// Parser parser = null;
	Subgraph rootSubgraph;
	Subgraph lastSubgraph;
	Graph graph;
	Subgraph thisGraph;
	Node thisNode;
	Edge thisEdge;
	Node fromNode;
	Node toNode;
	String portName = null;
	String toPortName;
	String fromPortName;
	int thisAttrType;
	int thisElemType;
	boolean directed = true;
	String graphType;
	private int anon_id = 0;
	Vector attrs = new Vector(8, 4);
	Vector nodes = new Vector(8, 4);
	Vector edges = new Vector(8, 4);

	void appendAttr(String name, String value) {
		attrs.addElement(new Attribute(thisElemType, name, value));
	}

	void noMacros() {
		parser.report_error("attribute macros are not supported yet", null);
	}

	void attrStmt(int kind, String macroName) {
		if (macroName != null) {
			noMacros();
			return;
		}
		if (attrs.size() == 0) {
			return;
		}
		Attribute attr = null;
		for (int i = 0; i < attrs.size(); i++) {
			if ((attr = (Attribute) (attrs.elementAt(i))).getValue() == null) {
				// null means to not attach the attribute to an element
				continue;
			} else {
				switch (kind) {
				case Grappa.NODE:
					parser.debug_message(1, "adding node default attr ("
							+ attr.getName() + ") to thisGraph("
							+ thisGraph.getName() + ")");
					thisGraph.setNodeAttribute(attr);
					break;
				case Grappa.EDGE:
					parser.debug_message(1, "adding edge default attr ("
							+ attr.getName() + ") to thisGraph("
							+ thisGraph.getName() + ")");
					thisGraph.setEdgeAttribute(attr);
					break;
				case Grappa.SUBGRAPH:
					parser.debug_message(1, "adding subg default attr ("
							+ attr.getName() + ") to thisGraph("
							+ thisGraph.getName() + ")");
					thisGraph.setAttribute(attr);
					break;
				}
			}
		}
		attrs.removeAllElements();
	}

	void startGraph(String name, boolean type, boolean strict) {
		if (graph == null) {
			graph = new Graph(name, type, strict);
		} else {
			graph.reset(name, type, strict);
		}
		directed = type;
		rootSubgraph = (Subgraph) graph;
		parser.debug_message(1, "Creating top level graph (" + name + ")");
		anon_id = 0;
	}

	void openGraph() {
		thisGraph = rootSubgraph;
		thisElemType = Grappa.SUBGRAPH;
		parser.debug_message(1, "thisGraph(" + thisGraph.getName() + ")");
	}

	void closeGraph() {
		int level = 1;

		if (parser.getErrorWriter() != null && parser.getDebugLevel() >= level) {

			parser.debug_message(level, "parsed graph follows:");
			rootSubgraph.printSubgraph(parser.getErrorWriter());
		}
	}

	void openSubg(String name) {
		thisGraph = new Subgraph(thisGraph, name);
		parser.debug_message(1, "thisGraph(" + thisGraph.getName() + ")");
		thisElemType = Grappa.SUBGRAPH;
	}

	String anonStr() {
		return Grappa.ANONYMOUS_PREFIX + anon_id++;
	}

	void closeSubg() {
		lastSubgraph = thisGraph;
		// getSubgraph() gets the parent subgraph
		thisGraph = thisGraph.getSubgraph();
		if (thisGraph == null) {
			parser.report_error("parser attempted to go above root Subgraph",
					null);
			thisGraph = rootSubgraph;
		}
		parser.debug_message(1, "Created subgraph (" + lastSubgraph.getName()
				+ ") in subgraph (" + thisGraph.getName() + ")...");
		parser.debug_message(1, "thisGraph(" + thisGraph.getName() + ")");
	}

	void appendNode(String name, String port) {
		if ((thisNode = rootSubgraph.findNodeByName(name)) == null) {
			parser.debug_message(1, "Creating node in subgraph ("
					+ thisGraph.getName() + ")...");
			thisNode = new Node(thisGraph, name);
		} else {
			parser.debug_message(1, "Node already in subgraph ("
					+ thisNode.getSubgraph().getName() + ")...");
		}
		Object[] pair = new Object[2];
		pair[0] = thisNode;
		pair[1] = port;
		nodes.addElement(pair);
		parser.debug_message(1, "thisNode(" + thisNode.getName() + ")");
		thisElemType = Grappa.NODE;
	}

	void nodeWrap() {
		Object[] pair = null;
		if (nodes.size() > 0 && attrs.size() > 0) {
			for (int i = 0; i < nodes.size(); i++) {
				pair = (Object[]) (nodes.elementAt(i));
				applyAttrs((Element) pair[0], null, null);
			}
		}
		attrs.removeAllElements();
		nodes.removeAllElements();
	}

	void bufferEdges() {
		Object[] pair = new Object[2];
		if (nodes.size() > 0) {
			pair[0] = nodes;
			nodes = new Vector(8, 4);
			pair[1] = new Boolean(true);
		} else if (lastSubgraph != null) {
			pair[0] = lastSubgraph;
			lastSubgraph = null;
			pair[1] = new Boolean(false);
		} else {
			parser.report_error(
					"EDGE_OP without clear antecedent nodelist or subgraph",
					null);
			return;
		}
		edges.addElement(pair);
	}

	void edgeWrap() {
		bufferEdges();
		Attribute key = null;
		Attribute name = null;
		Attribute attr = null;
		int skip = -1;
		for (int i = 0; i < attrs.size(); i++) {
			attr = (Attribute) (attrs.elementAt(i));
			if (attr.getName().equals("key")) {
				key = attr;
				if (name != null) {
					break;
				}
			} else if (attr.getName().equals("__nAmE__")) {
				name = attr;
				if (key != null) {
					break;
				}
			}
		}
		Object[] tailPair = (Object[]) (edges.elementAt(0));
		Object[] headPair = null;
		// note: when node list is used, a non-null name will cause errors
		// due to lack of uniqueness
		for (int i = 1; i < edges.size(); i++) {
			headPair = (Object[]) (edges.elementAt(i));
			if (((Boolean) (tailPair[1])).booleanValue()) { // true if node list
				Vector list = (Vector) (tailPair[0]);
				Object[] nodePair = null;
				for (int j = 0; j < list.size(); j++) {
					nodePair = (Object[]) (list.elementAt(j));
					edgeRHS((Node) (nodePair[0]), (String) (nodePair[1]),
							headPair, key, name);
				}
				list.removeAllElements();
			} else {
				Subgraph subg = (Subgraph) (tailPair[0]);
				Enumeration enu = subg.elements(Grappa.NODE);
				while (enu.hasMoreElements()) {
					edgeRHS((Node) (enu.nextElement()), null, headPair, key,
							name);
				}
			}
			tailPair = headPair;
		}
		edges.removeAllElements();
		attrs.removeAllElements();
	}

	void edgeRHS(Node tail, String tailPort, Object[] headPair,
			Attribute keyAttr, Attribute nameAttr) {
		String key = (keyAttr == null) ? null : keyAttr.getStringValue();
		String name = (nameAttr == null) ? null : nameAttr.getStringValue();
		if (((Boolean) (headPair[1])).booleanValue()) { // true if node list
			Vector list = (Vector) (headPair[0]);
			Object[] nodePair = null;
			for (int j = 0; j < list.size(); j++) {
				nodePair = (Object[]) (list.elementAt(j));
				thisEdge = new Edge(thisGraph, tail, tailPort,
						(Node) (nodePair[0]), (String) (nodePair[1]), key, name);
				parser.debug_message(1, "Creating edge in subgraph ("
						+ thisGraph.getName() + ")...");
				parser.debug_message(1, "thisEdge(" + thisEdge.getName() + ")");
				thisElemType = Grappa.EDGE;
				applyAttrs((Element) thisEdge, keyAttr, nameAttr);
			}
		} else {
			Subgraph subg = (Subgraph) (headPair[0]);
			Enumeration enu = subg.elements(Grappa.NODE);
			while (enu.hasMoreElements()) {
				thisEdge = new Edge(thisGraph, tail, tailPort, (Node) (enu
						.nextElement()), null, key, name);
				parser.debug_message(1, "Creating edge in subgraph ("
						+ thisGraph.getName() + ")...");
				parser.debug_message(1, "thisEdge(" + thisEdge.getName() + ")");
				thisElemType = Grappa.EDGE;
				applyAttrs((Element) thisEdge, keyAttr, nameAttr);
			}
		}
	}

	void applyAttrs(Element elem, Attribute skip1, Attribute skip2) {
		Attribute attr = null;
		for (int i = 0; i < attrs.size(); i++) {
			attr = (Attribute) attrs.elementAt(i);
			if (attr == skip1) {
				continue;
			} else if (attr == skip2) {
				continue;
			}
			if (elem instanceof Edge
					&& attr.getName().equals(GrappaConstants.POS_ATTR)) {
				// added by little@2007.11.27
				// modify the pos attribute to append arrow tail and arrow head
				String arrowhead = (String) elem
						.getAttributeValue(GrappaConstants.ARROWHEAD_ATTR);
				int ihead = findArrowTypeIdx(arrowhead);
				if (ihead == -1) {
					/*
					 * No arrowType found. Use default for head (normal).
					 */
					ihead = 1;
				}
				String arrowtail = (String) elem
						.getAttributeValue(GrappaConstants.ARROWTAIL_ATTR);
				int itail = findArrowTypeIdx(arrowtail);
				if (itail == -1) {
					/*
					 * No arrowType found. Use default for tail (none).
					 */
					itail = 0;
				}
				String newValue = attr.getStringValue() + " " + itail + ","
						+ ihead;
				attr.setValue(newValue);
			}
			elem.setAttribute(attr);
		}
	}

	private static int findArrowTypeIdx(String arrowType) {
		if (arrowType == null) {
			return -1;
		}
		for (int idx = 0; idx < GrappaConstants.ARROWTYPES.length; idx++) {
			if (GrappaConstants.ARROWTYPES[idx].equals(arrowType)) {
				return idx;
			}
		}
		return -1;
	}

	private final Parser parser;

	/** Constructor */
	CUP$Parser$actions(Parser parser) {
		this.parser = parser;
	}

	/** Method with the actual generated action code. */
	public final java_cup_grappa.runtime.Symbol CUP$Parser$do_action(
			int CUP$Parser$act_num,
			java_cup_grappa.runtime.lr_parser CUP$Parser$parser,
			java.util.Stack CUP$Parser$stack, int CUP$Parser$top)
			throws java.lang.Exception {
		/* Symbol object for return from actions */
		java_cup_grappa.runtime.Symbol CUP$Parser$result;

		/* select the action based on the action number */
		switch (CUP$Parser$act_num) {
		/* . . . . . . . . . . . . . . . . . . . . */
		case 62: { // optSeparator ::=
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					17 /* optSeparator */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 61: { // optSeparator ::= COMMA
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					17 /* optSeparator */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 60: { // optSeparator ::= SEMI
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					17 /* optSeparator */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 59: { // optSemi ::=
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					16 /* optSemi */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 58: { // optSemi ::= SEMI
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					16 /* optSemi */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 57: { // optSubgHdr ::=
			String RESULT = null;

			RESULT = anonStr();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					5 /* optSubgHdr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 56: { // optSubgHdr ::= SUBGRAPH
			String RESULT = null;

			RESULT = anonStr();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					5 /* optSubgHdr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 55: { // optSubgHdr ::= SUBGRAPH ATOM
			String RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String val = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			RESULT = val;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					5 /* optSubgHdr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 54: { // subgraph ::= optSubgHdr NT$2 body
			Object RESULT = null;
			// propagate RESULT from NT$2
			if (((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value != null) {
				RESULT = (Object) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
						.elementAt(CUP$Parser$top - 1)).value;
			}
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).right;
			String val = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).value;

			closeSubg();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					22 /* subgraph */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 53: { // NT$2 ::=
			Object RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String val = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			openSubg(val);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(34 /* NT$2 */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 52: { // graphAttrDefs ::= attrAssignment
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					25 /* graphAttrDefs */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 51: { // attrMacro ::= ATSIGN ATOM
			Object RESULT = null;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			appendAttr(name, null);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					30 /* attrMacro */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 50: { // attrAssignment ::= ATOM EQUAL ATOM
			Object RESULT = null;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).value;
			int valueleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valueright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String value = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			appendAttr(name, value);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					29 /* attrAssignment */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 49: { // attrItem ::= attrMacro
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					28 /* attrItem */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 48: { // attrItem ::= attrAssignment
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					28 /* attrItem */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 47: { // attrDefs ::= attrDefs optSeparator attrItem
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					27 /* attrDefs */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 46: { // attrDefs ::= attrItem
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					27 /* attrDefs */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 45: { // optAttrDefs ::=
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					26 /* optAttrDefs */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 44: { // optAttrDefs ::= attrDefs
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					26 /* optAttrDefs */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 43: { // attrList ::= optAttr LBR optAttrDefs RBR
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					24 /* attrList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 3)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 42: { // optAttr ::=
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					20 /* optAttr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 41: { // optAttr ::= attrList
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					20 /* optAttr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 40: { // optMacroName ::=
			String RESULT = null;

			RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					7 /* optMacroName */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 39: { // optMacroName ::= ATOM EQUAL
			String RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).right;
			String val = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value;

			RESULT = val;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					7 /* optMacroName */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 38: { // attrType ::= EDGE
			Integer RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			Integer val = (Integer) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			RESULT = new Integer(Grappa.EDGE);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					4 /* attrType */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 37: { // attrType ::= NODE
			Integer RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			Integer val = (Integer) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			RESULT = new Integer(Grappa.NODE);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					4 /* attrType */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 36: { // attrType ::= GRAPH
			Integer RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			Integer val = (Integer) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			RESULT = new Integer(Grappa.SUBGRAPH);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					4 /* attrType */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 35: { // attrStmt ::= graphAttrDefs
			Object RESULT = null;

			attrStmt(Grappa.SUBGRAPH, null);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					15 /* attrStmt */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 34: { // attrStmt ::= attrType optMacroName attrList
			Object RESULT = null;
			int typeleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).left;
			int typeright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).right;
			Integer type = (Integer) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).value;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value;

			attrStmt(type.intValue(), name);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					15 /* attrStmt */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 33: { // optPort ::=
			String RESULT = null;

			RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					8 /* optPort */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 32: { // optPort ::= COLON ATOM
			String RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String val = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			RESULT = val;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					8 /* optPort */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 31: { // node ::= ATOM optPort
			Object RESULT = null;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value;
			int portleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int portright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String port = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			appendNode(name, port);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(23 /* node */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 30: { // nodeList ::= nodeList COMMA node
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					21 /* nodeList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 29: { // nodeList ::= node
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					21 /* nodeList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 28: { // rCompound ::=
			Boolean RESULT = null;

			thisElemType = Grappa.NODE;
			RESULT = new Boolean(false);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					3 /* rCompound */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 27: { // rCompound ::= edge_op NT$1 simple rCompound
			Boolean RESULT = null;
			// propagate RESULT from NT$1
			if (((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).value != null) {
				RESULT = (Boolean) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
						.elementAt(CUP$Parser$top - 2)).value;
			}
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			Boolean val = (Boolean) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			thisElemType = Grappa.EDGE;
			RESULT = new Boolean(true);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					3 /* rCompound */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 3)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 26: { // NT$1 ::=
			Object RESULT = null;

			thisElemType = Grappa.EDGE;
			bufferEdges();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(33 /* NT$1 */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 25: { // edge_op ::= ND_EDGE_OP
			Object RESULT = null;

			if (directed) {
				// CUP$parser.report_error
				// ("attempt to create a non-directed edge in a directed graph",null);
				parser
						.report_error(
								"attempt to create a non-directed edge in a directed graph",
								null);
			}

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					31 /* edge_op */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 24: { // edge_op ::= D_EDGE_OP
			Object RESULT = null;

			if (!directed) {
				// CUP$parser.report_error
				// ("attempt to create a directed edge in a non-directed graph",null);
				parser
						.report_error(
								"attempt to create a directed edge in a non-directed graph",
								null);
			}

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					31 /* edge_op */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 23: { // simple ::= subgraph
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					19 /* simple */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 22: { // simple ::= nodeList
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					19 /* simple */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 21: { // compound ::= simple rCompound optAttr
			Object RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).right;
			Boolean val = (Boolean) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value;

			if (val.booleanValue()) {
				edgeWrap();
			} else {
				nodeWrap();

			}
			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					18 /* compound */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 20: { // stmt ::= compound optSemi
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(14 /* stmt */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 19: { // stmt ::= attrStmt optSemi
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(14 /* stmt */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 18: { // stmtList ::= stmt
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					13 /* stmtList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 17: { // stmtList ::= stmtList stmt
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					13 /* stmtList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 16: { // optStmtList ::=
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					12 /* optStmtList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 15: { // optStmtList ::= stmtList
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					12 /* optStmtList */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 14: { // body ::= LCUR optStmtList RCUR
			Object RESULT = null;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(11 /* body */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 13: { // optGraphName ::=
			String RESULT = null;

			RESULT = anonStr();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					6 /* optGraphName */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 12: { // optGraphName ::= ATOM
			String RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String val = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			RESULT = val;

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					6 /* optGraphName */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 11: { // graphType ::= DIGRAPH
			Boolean RESULT = null;

			RESULT = new Boolean(true);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					2 /* graphType */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 10: { // graphType ::= GRAPH
			Boolean RESULT = null;

			RESULT = new Boolean(false);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					2 /* graphType */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 9: { // optStrict ::=
			Boolean RESULT = null;

			RESULT = new Boolean(false);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					1 /* optStrict */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 8: { // optStrict ::= STRICT
			Boolean RESULT = null;

			RESULT = new Boolean(true);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					1 /* optStrict */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 7: { // hdr ::= STRICTDIGRAPH optGraphName
			Object RESULT = null;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			startGraph(name, true, true);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(10 /* hdr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 6: { // hdr ::= STRICTGRAPH optGraphName
			Object RESULT = null;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			startGraph(name, true, false);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(10 /* hdr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 5: { // hdr ::= optStrict graphType optGraphName
			Object RESULT = null;
			int strictleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).left;
			int strictright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).right;
			Boolean strict = (Boolean) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 2)).value;
			int typeleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).left;
			int typeright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).right;
			Boolean type = (Boolean) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value;
			int nameleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int nameright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			String name = (String) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			startGraph(name, type.booleanValue(), strict.booleanValue());

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(10 /* hdr */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 4: { // graph ::=
			Object RESULT = null;

			graph = new Graph("empty");
			// ((Parser)(CUP$parser)).report_warning
			// ("The graph to parse is empty.", null);
			((Parser) (parser)).report_warning("The graph to parse is empty.",
					null);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					9 /* graph */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 3: { // graph ::= error
			Object RESULT = null;
			int valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).left;
			int valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).right;
			Object val = (Object) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 0)).value;

			// CUP$parser.report_error
			// ("An error was encountered while graph parsing (" +
			// val.toString() + ").", null);
			parser.report_error(
					"An error was encountered while graph parsing ("
							+ val.toString() + ").", null);

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					9 /* graph */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 2: { // graph ::= hdr NT$0 body
			Object RESULT = null;
			// propagate RESULT from NT$0
			if (((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value != null) {
				RESULT = (Object) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
						.elementAt(CUP$Parser$top - 1)).value;

			}
			closeGraph();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					9 /* graph */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 2)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 1: { // NT$0 ::=
			Object RESULT = null;

			openGraph();

			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(32 /* NT$0 */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			return CUP$Parser$result;

			/* . . . . . . . . . . . . . . . . . . . . */
		case 0: { // $START ::= graph EOF
			Object RESULT = null;
			int start_valleft = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).left;
			int start_valright = ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).right;
			Object start_val = (Object) ((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
					.elementAt(CUP$Parser$top - 1)).value;
			RESULT = start_val;
			CUP$Parser$result = new java_cup_grappa.runtime.Symbol(
					0 /* $START */,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 1)).left,
					((java_cup_grappa.runtime.Symbol) CUP$Parser$stack
							.elementAt(CUP$Parser$top - 0)).right, RESULT);
		}
			/* ACCEPT */
			CUP$Parser$parser.done_parsing();
			return CUP$Parser$result;

			/* . . . . . . */
		default:
			throw new Exception(
					"Invalid action number found in internal parse table");

		}
	}
}
