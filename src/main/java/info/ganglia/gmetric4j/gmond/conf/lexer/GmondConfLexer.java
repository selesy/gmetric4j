package info.ganglia.gmetric4j.gmond.conf.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class GmondConfLexer {

	static final Charset DEFAULT_CHARSET = Charset.forName("US-ASCII");

	// static final char[] COMMENT_START = {'#'};
	// static final char[] LINE_TERMINATORS = {'\n', '\r'};
	// static final char[] OPERATORS = {'{', '}', '='};
	// static final char[] WHITESPACE = {' ', '\t', '\n', '\r'};

	static final Set<Character> COMMENT_START = new HashSet<Character>(
			Arrays.asList('#'));
	static final Set<Character> LINE_TERMINATORS = new HashSet<Character>(
			Arrays.asList('\n', '\r'));
	static final Set<Character> OPERATORS = new HashSet<Character>(
			Arrays.asList('{', '}', '='));
	static final Set<Character> WHITESPACE = new HashSet<Character>(
			Arrays.asList(' ', '\t', '\n', '\r'));

	enum State {

		IN_COMMENT, IN_OPERATOR, IN_TOKEN, IN_WHITESPACE, PAUSED;

	}

	BufferedReader characterStream;
	State currentState;

	Deque<String> tokenStack = new LinkedList<String>();

	public GmondConfLexer(String first, String... more) throws IOException {
		this(Paths.get(first, more));
	}

	public GmondConfLexer(Path path) throws IOException {
		this(path, DEFAULT_CHARSET);
	}

	public GmondConfLexer(Path path, Charset charset) throws IOException {
		this(Files.newBufferedReader(path, charset));
	}

	public GmondConfLexer(BufferedReader characterStream) {
		this.characterStream = characterStream;
	}

	public String getToken() throws IOException {
		String output = null;
		if (tokenStack.size() > 0) {
			output = tokenStack.removeFirst();
		} else {
			StringBuilder token = new StringBuilder();

			State state = State.PAUSED;
			lex(state, token);

			output = token.toString();
		}

		if (output.length() == 0) {
			output = null;
		}

		return output;
	}

	void lex(State state, StringBuilder token) throws IOException {
		int character = characterStream.read();
		if (character > -1) {
			System.out.println("State: " + state.name() + ", Character: \""
					+ ((char) character) + "\"");
			switch (state) {
			case IN_COMMENT:
				processComment(character, token);
				break;
			case IN_OPERATOR:
				break;
			case IN_TOKEN:
				processToken(character, token);
				break;
			case IN_WHITESPACE:
				processWhiteSpace(character, token);
				break;
			case PAUSED:
				processPaused(character, token);
				break;
			default:
				System.out.println("lex() - We should never get here");
				break;
			}
		}
	}

	State processComment(int character, StringBuilder token) throws IOException {
		State state = State.IN_COMMENT;
		if (LINE_TERMINATORS.contains((char) character)) {
			System.out.println("Ending comment with terminating character");
			state = State.IN_WHITESPACE;
			lex(state, token);
		} else {
			System.out
					.println("Waste comment character: \"" + character + "\"");
			lex(state, token);
		}
		return state;
	}

	State processPaused(int character, StringBuilder token) throws IOException {
		State state = State.PAUSED;
		if (COMMENT_START.contains((char) character)) {
			lex(State.IN_COMMENT, token);
		} else if (OPERATORS.contains((char) character)) {
			token.append((char) character);
		} else if (WHITESPACE.contains((char) character)) {
			lex(State.IN_WHITESPACE, token);
		} else {
			token.append((char) character);
			lex(State.IN_TOKEN, token);
		}
		return state;
	}

	State processToken(int character, StringBuilder token) throws IOException {
		State state = State.IN_TOKEN;
		if (COMMENT_START.contains((char) character)) {
			state = State.PAUSED;
		} else if (OPERATORS.contains((char) character)) {
			tokenStack.addLast(String.valueOf((char) character));
			state = State.PAUSED;
		} else if (WHITESPACE.contains((char) character)) {
			if(token.indexOf("\"") == 0 && token.lastIndexOf("\"") == 0) {
				token.append((char) character);
				lex(state, token);
			} else {
				state = State.PAUSED;
			}
		} else {
			token.append((char) character);
			lex(state, token);
		}
		return state;
	}

	State processWhiteSpace(int character, StringBuilder token)
			throws IOException {
		State state = State.IN_WHITESPACE;
		if (COMMENT_START.contains((char) character)) {
			state = State.IN_COMMENT;
			lex(state, token);
		} else if (OPERATORS.contains((char) character)) {
			token.append((char) character);
			state = State.PAUSED;
		} else if (WHITESPACE.contains((char) character)) {
			lex(state, token);
		} else {
			token.append((char) character);
			state = State.IN_TOKEN;
			lex(state, token);
		}
		return state;
	}

}
