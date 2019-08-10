package org.processmining.importing.erlangnet.arcinscriptionparser;

import java_cup.runtime.*;

%%

%class Scanner
%cup
%unicode
%line
%column
%public

%{

	private Symbol symbol(int type) {
		return new Symbol(type, yyline, yycolumn);
	}
	
	private Symbol symbol(int type, Object value) {
		return new Symbol(type, yyline, yycolumn, value);
	}
%}

LineTerminator	= \r|\n|\r\n
WhiteSpace		= {LineTerminator} | [ \t\f]
Identifier        = [a-zA-Z] [0-9a-zA-Z]*
Anything		  = [:jletterdigit:]+
Environment		  = "env" [0-9]*
StringIdentifier  = "\"" {Identifier}+ "\""
String			  = "\"" [0-9a-zA-Z]* "\""
Integer			  = [0-9]+
Boolean			  = (true|false)

%%


<YYINITIAL> {
	"("				{ return symbol(sym.LPAREN); }
	")"				{ return symbol(sym.RPAREN); }
	","				{ return symbol(sym.COMMA); }
	
	/* keywords */
	"pid"			{ return symbol(sym.PID); }
	{Environment}	{ return symbol(sym.ENV, yytext()); }
	"set"			{ return symbol(sym.SET); }
	"sm"			{ return symbol(sym.SM); }
	"sender"			{ return symbol(sym.SENDER); }
	"receiver"			{ return symbol(sym.RECEIVER); }
	"value"				{ return symbol(sym.VALUE); }
	"get"				{ return symbol(sym.GET); }
	"getPid"			{ return symbol(sym.GETPID); }
	"INT"				{ return symbol(sym.INTTYPE); }
	"STRING"			{ return symbol(sym.STRINGTYPE); }
	"BOOL"				{ return symbol(sym.BOOLTYPE); }
	"PID"				{ return symbol(sym.PIDTYPE); }
		
	{StringIdentifier}	{ return symbol(sym.STRINGID, yytext()); }
	{String}			{ return symbol(sym.STRING, yytext()); }
	{Boolean}			{ return symbol(sym.BOOL, yytext()); }
	{Identifier}		{ return symbol(sym.ID, yytext()); }
	{Integer}			{ return symbol(sym.INT, yytext()); }
	
	/* whitespace */
	{WhiteSpace}		{ /* ignore */ }
}

/* error fallback */
.|\n					{ throw new java.io.IOException("Illegal character <" + yytext() + ">"); }
