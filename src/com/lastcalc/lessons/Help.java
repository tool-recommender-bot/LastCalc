package com.lastcalc.lessons;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;

import com.google.common.collect.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import com.lastcalc.*;
import com.lastcalc.parsers.*;
import com.lastcalc.parsers.UserDefinedParserParser.UserDefinedParser;

public class Help {
	private static final Logger log = Logger.getLogger(Help.class.getName());

	public static Map<String, Lesson> lessons;

	public static String helpDocAsString;

	static {
		lessons = Maps.newHashMap();
		try {
			final Document helpDoc = createHelpDoc();

			helpDocAsString = helpDoc.toString();

			for (final Element lessonElement : helpDoc.body().select("div.lesson")) {
				final Lesson lesson = new Lesson(lessonElement);
				lessons.put(lesson.id, lesson);
			}
		} catch (final Exception e) {
			log.warning("Exception while processing tutorial.html " + e);
			e.printStackTrace();
		}
	}

	public static Document createHelpDoc() throws IOException {
		final Document helpDoc = Jsoup.parse(Help.class.getResourceAsStream("help.html"), "UTF-8", "/");

		helpDoc.outputSettings().charset(Charset.forName("UTF-8"));
		helpDoc.head().appendElement("meta").attr("charset", "UTF-8");
		helpDoc.head().appendElement("meta").attr("http-equiv", "Content-Type")
		.attr("content", "text/html; charset=UTF-8");

		// Process calculations

		final SequentialParser sp = SequentialParser.create();

		for (final Element oldQuestion : helpDoc.select("div.question")) {
			final String questionText = oldQuestion.text();
			oldQuestion.text("");
			oldQuestion.removeClass("question");
			oldQuestion.addClass("line").addClass("firstLine");
			final Element line = oldQuestion;
			final TokenList tokenizedQuestion = Tokenizer.tokenize(questionText);
			final TokenList answer = sp.parseNext(tokenizedQuestion);
			// Important to render the question after we've calculated the
			// answer so that variables are lighlighted in the line they are
			// created in
			final String questionAsHtml = Renderers.toHtml("/", tokenizedQuestion, sp.getUserDefinedKeywordMap())
					.toString();
			final TokenList strippedAnswer = sp.stripUDF(answer);
			final boolean isFunction = strippedAnswer.size() == 1 && strippedAnswer.get(0) instanceof UserDefinedParser;
			line.appendElement("div").addClass("question").html(questionAsHtml);
			if (!isFunction) {
				line.appendElement("div").attr("class", "equals").text("=");
				line.appendElement("div").attr("class", "answer")
						.html(Renderers.toHtml("/", PreParser.flatten(strippedAnswer)).toString());
			} else {
				line.appendElement("div").attr("class", "equals")
				.html("<span style=\"font-size:10pt;\">&#10003</span>");
				line.appendElement("div").attr("class", "answer");
			}
		}

		// Create menu

		final Element menuUl = helpDoc.select("ul#menu").first();
		for (final Element header : helpDoc.select("div.section h1")) {
			final String headerText = header.text();
			final String anchor = headerText.toLowerCase().replace(' ', '-');
			header.html("<a name=\"" + anchor + "\">" + headerText + "</a>");
			menuUl.appendElement("li").appendElement("a").attr("href", "/help#" + anchor).text(headerText);
		}
		return helpDoc;
	}

	public static class Lesson {
		public Lesson(final Element lesson) {
			id = lesson.id();
			explanation = lesson.select("div.explanation").first();
			requires = Sets.newHashSet();
			for (final Element li : lesson.select("div.requires li")) {
				requires.add(li.text());
			}
		}

		public String id;

		public Element explanation;

		public Set<String> requires;
	}
}
