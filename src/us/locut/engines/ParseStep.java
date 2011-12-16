package us.locut.engines;

import java.util.List;

import us.locut.parsers.*;
import us.locut.parsers.Parser.ParseResult;

public class ParseStep {
	public final List<Object> input;

	public final ParseResult result;

	public final Parser parser;

	public ParseStep(final List<Object> input, final Parser parser, final ParseResult result) {
		this.input = input;
		this.parser = parser;
		this.result = result;
	}

	@Override
	public String toString() {
		return alParse(input) + " -> " + parser.getClass().getSimpleName() + " : " + parser + " -> "
				+ alParse(result.output);
	}

	private static String alParse(final List<Object> input) {
		final StringBuilder sb = new StringBuilder();
		for (final Object o : input) {
			sb.append(o.getClass().getSimpleName() + "[" + o.toString() + "] ");
		}
		return sb.toString();
	}
}
