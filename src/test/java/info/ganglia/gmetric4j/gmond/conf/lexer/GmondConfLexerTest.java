package info.ganglia.gmetric4j.gmond.conf.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

public class GmondConfLexerTest {
	
	static final String[] COLLECTION_GROUP_TOKENS = {
		"collection_group",
		"{",
		"collect_every",
		"=",
		"40",
		"time_threshold",
		"=",
		"180",
		"metric",
		"{",
		"name",
		"=",
		"\"disk_free\"",
		"value_threshold",
		"=",
		"1.0",
		"title",
		"=",
		"\"Disk Space Available\"",
		"}",
		"metric",
		"{",
		"name",
		"=",
		"\"part_max_used\"",
		"value_threshold",
		"=",
		"1.0",
		"title",
		"=",
		"\"Maximum Disk Space Used\"",
		"}",
		"}"
	};

	static final String COMMENT_TEST_1 = "# This is a full line comment\n\r";
	static final String COMMENT_TEST_2 = "udp_send_channel { # This is an EOL comment\n\r";

	static final String TOKEN_BLOCK_START_WITH_SPACE = "udp_send_channel {";
	static final String TOKEN_BLOCK_START_WITHOUT_SPACE = "udp_send_channel{";

	static final String UDP_SEND_PACKET_SECTION = "# jon1\n"
			+ "udp_send_channel {\n" + "  bind_hostname = yes\n"
			+ "  host = 146.186.15.103\n" + "  port = 8649\n" + "  ttl = 1\n"
			+ "}";
	static final String[] UDP_SEND_PACKET_TOKENS = {
		"udp_send_channel",
		"{",
		"bind_hostname",
		"=",
		"yes",
		"host",
		"=",
		"146.186.15.103",
		"port",
		"=",
		"8649",
		"ttl",
		"=",
		"1"
	};

	GmondConfLexer lexer;

	@Before
	public void setUp() {
		// lexer = new GmondConfLexer();
	}
	
	BufferedReader getBufferedReaderFromStream(InputStream inputStream) {
		BufferedReader buffered = new BufferedReader(new InputStreamReader(inputStream));
		return buffered;
	}

	BufferedReader getBufferedReaderFromString(String input) {
		StringReader reader = new StringReader(input);
		BufferedReader buffered = new BufferedReader(reader);
		return buffered;
	}
	
	GmondConfLexer getLexerFromStream(InputStream inputStream) {
		return new GmondConfLexer(getBufferedReaderFromStream(inputStream));
	}
	
	GmondConfLexer getLexerFromStream(String resource) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(resource);
		return getLexerFromStream(inputStream);
	}

	GmondConfLexer getLexerFromString(String string) {
		return new GmondConfLexer(getBufferedReaderFromString(string));
	}
	
	@Test
	public void testCollectionGroupWithNestedSections() throws IOException {
		GmondConfLexer lexer = getLexerFromStream("collection-group.conf");
		for(String token: COLLECTION_GROUP_TOKENS) {
			assertEquals(token, lexer.getToken());
		}
//		String token = null;
//		while((token = lexer.getToken()) != null) {
//			System.out.println("Token: " + token);
//		}
	}

	@Test
	public void testFullLineCommentsAreIgnored() throws IOException {
		GmondConfLexer lexer = getLexerFromString(COMMENT_TEST_1);
		String token = lexer.getToken();
		assertNull(token);
	}

	void testOneTokenAndBlockStart(String input) throws IOException {
		GmondConfLexer lexer = getLexerFromString(input);

		String token1 = lexer.getToken();
		assertNotNull(token1);
		assertEquals("udp_send_channel", token1);

		String token2 = lexer.getToken();
		assertNotNull(token2);
		assertEquals("{", token2);

		String token3 = lexer.getToken();
		assertNull(token3);
	}

	@Test
	public void testEndOfLineCommentsAreIgnored() throws IOException {
		testOneTokenAndBlockStart(COMMENT_TEST_2);
	}

	@Test
	public void testOneTokenAndBlockStartWithoutSpace() throws IOException {
		testOneTokenAndBlockStart(TOKEN_BLOCK_START_WITHOUT_SPACE);
	}

	@Test
	public void testOneTokenAndBlockStartWithSpace() throws IOException {
		testOneTokenAndBlockStart(TOKEN_BLOCK_START_WITH_SPACE);
	}
	
	@Test
	public void testUdpSendChannelSection() throws IOException {
		GmondConfLexer lexer = getLexerFromString(UDP_SEND_PACKET_SECTION);
		for(String token: UDP_SEND_PACKET_TOKENS) {
			assertEquals(token, lexer.getToken());
		}
	}

}
