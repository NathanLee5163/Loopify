import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class TerminalClientTest {

    private TerminalClient client;
    private MockBufferedReader mockReader;
    private MockPrintWriter mockWriter;
    private MockSocket mockSocket;

    @BeforeEach
    public void setUp() {
        mockReader = new MockBufferedReader();
        mockWriter = new MockPrintWriter();
        mockSocket = new MockSocket(mockReader, mockWriter);

        client = new TerminalClient("localhost", 54321);
        client.socket = mockSocket;
        client.serverReader = mockReader;
        client.serverWriter = mockWriter;
    }

    // Method: register
    @Test
    public void testRegisterSuccess() throws IOException {
        mockReader.setResponse("SUCCESS Registration complete");
        client.register("testUser", "password");
        assertTrue(mockWriter.getLastCommand().contains("REGISTER testUser password"));
    }

    // Method: register
    @Test
    public void testRegisterFailure() throws IOException {
        mockReader.setResponse("ERROR Registration failed");
        client.register("testUser", "password");
        assertTrue(mockWriter.getLastCommand().contains("REGISTER testUser password"));
    }

    // Method: addFriend
    @Test
    public void testAddFriendSuccess() throws IOException {
        mockReader.setResponse("SUCCESS Friend added");
        client.addFriend("friendUser");
        assertTrue(mockWriter.getLastCommand().contains("ADD_FRIEND testUser friendUser"));
    }

    // Method: addFriend
    @Test
    public void testAddFriendFailure() throws IOException {
        mockReader.setResponse("ERROR Friend already exists");
        client.addFriend("friendUser");
        assertTrue(mockWriter.getLastCommand().contains("ADD_FRIEND testUser friendUser"));
    }

    // Method: removeFriend
    @Test
    public void testRemoveFriendSuccess() throws IOException {
        mockReader.setResponse("SUCCESS Friend removed");
        client.removeFriend("friendUser");
        assertTrue(mockWriter.getLastCommand().contains("REMOVE_FRIEND testUser friendUser"));
    }

    // Method: removeFriend
    @Test
    public void testRemoveFriendFailure() throws IOException {
        mockReader.setResponse("ERROR Friend not found");
        client.removeFriend("friendUser");
        assertTrue(mockWriter.getLastCommand().contains("REMOVE_FRIEND testUser friendUser"));
    }

    static class MockSocket extends Socket {
        private final BufferedReader reader;
        private final PrintWriter writer;

        public MockSocket(BufferedReader reader, PrintWriter writer) {
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        public BufferedReader getReader() {
            return reader;
        }

        public PrintWriter getWriter() {
            return writer;
        }
    }

    static class MockBufferedReader extends BufferedReader {
        private String response;

        public MockBufferedReader() {
            super(new StringReader(""));
        }

        public void setResponse(String response) {
            this.response = response;
        }

        @Override
        public String readLine() {
            return response;
        }
    }

    static class MockPrintWriter extends PrintWriter {
        private String lastCommand = "";

        public MockPrintWriter() {
            super(new StringWriter());
        }

        @Override
        public void println(String x) {
            this.lastCommand = x;
        }

        public String getLastCommand() {
            return lastCommand;
        }
    }
}
