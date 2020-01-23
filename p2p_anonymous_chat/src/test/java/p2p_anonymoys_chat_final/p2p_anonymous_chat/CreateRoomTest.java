package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.Test;

public class CreateRoomTest {
	class MessageListenerImpl implements MessageListener{
		int peerId;
		
		public MessageListenerImpl(int peerId) {
			this.peerId = peerId;
		}

		@Override
		public Object parseMessage(Object obj) {
			TextIO textIO = TextIoFactory.getTextIO();
			@SuppressWarnings("rawtypes")
			TextTerminal terminal = textIO.getTextTerminal();
			terminal.printf("\n[" + peerId +"] (DM Received) " + obj + "\n\n");
			return "success";
		}
	}
	
	
	
	@Test
	public void test() throws Exception {
		AnonymousChatImpl masterPeer = new AnonymousChatImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		
		assertTrue(masterPeer.createRoom("Storia"));
			
	}
	

}
