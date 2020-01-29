package distributed_system.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import distributed_system.p2p_anonymous_chat.AnonymousChatImpl;
import distributed_system.p2p_anonymous_chat.MessageListener;

public class sendMessageTest {
	private static AnonymousChatImpl masterPeer;
	private static AnonymousChatImpl peer1;
	private static AnonymousChatImpl peer2;
	private static AnonymousChatImpl peer3;
	
	@BeforeClass 
	public static void init() throws Exception {
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
		
		
		masterPeer = new AnonymousChatImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		peer1 = new AnonymousChatImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		peer2 = new AnonymousChatImpl(2, "127.0.0.1", new MessageListenerImpl(2));
		peer3 = new AnonymousChatImpl(3, "127.0.0.1", new MessageListenerImpl(3));
	}

	@Test
	public void test() {
		
		masterPeer.createRoom("Master_Room");
		peer1.createSecretRoom("Secret_Room", "password");
		
		peer1.joinRoom("Master_Room");
		peer2.joinRoom("Master_Room");
		
		masterPeer.joinSecretRoom("Secret_Room", "password");
		peer3.joinSecretRoom("Secret_Room", "password");
		
		/*
		 * Verifichiamo che si può mandare un messaggio in una 
		 * room a cui si è iscritti
		 */
		assertTrue(peer1.sendMessage("Master_Room", "Hello There!!"));
		assertTrue(peer2.sendMessage("Master_Room", "Hello There!!"));
		
		/*
		 * Verifichiamo che si può mandare un messaggio in una 
		 * room segreta a cui si è iscritti
		 */
		assertTrue(masterPeer.sendMessage("Secret_Room", "Hello there!!"));
		assertTrue(peer3.sendMessage("Secret_Room", "Hello there!!"));
		
		/*
		 * Verifichiamo che non si può mandare un messaggio in una room
		 * a cui non si è iscritti
		 */
		assertFalse(peer3.sendMessage("Master_room", "Hello_there!!"));
		
		/*
		 * Verifichiamo che non si può mandare un messaggio in una room
		 * segreta a cui non si è iscritti.
		 */
		assertFalse(peer2.sendMessage("Secret_Room", "Hello there!!"));
	}
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}

}
