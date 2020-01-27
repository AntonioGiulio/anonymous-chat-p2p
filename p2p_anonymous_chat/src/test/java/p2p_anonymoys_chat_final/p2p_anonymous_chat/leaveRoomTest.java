package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class leaveRoomTest {
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
		masterPeer.createSecretRoom("Secret_Room", "password");
		
		
		/*
		 * Verifichiamo che quando un peer lascia una room in cui è da solo
		 * la room viene distrutta e può esserne creata un'altra con lo 
		 * stesso nome;
		 */
		assertTrue(masterPeer.leaveRoom("Master_Room"));
		assertEquals("Secret_Room_secret", masterPeer.listRooms().get(0));
		
		assertTrue(masterPeer.leaveRoom("Secret_Room"));
		assertNull(masterPeer.listRooms());
		
		assertTrue(masterPeer.createRoom("Master_Room"));
		assertTrue(masterPeer.createSecretRoom("Secret_Room", "password"));
		
		/*
		 * Verifichiamo che è impossibile lasciare una room a cui non 
		 * si è iscritti.
		 */
		assertFalse(masterPeer.leaveRoom("Room_1"));
		
		/*
		 * Verifichiamo che è possibile lasciare una room che non ha creato 
		 * lo stesso peer
		 */
		peer1.joinRoom("Master_Room");
		assertTrue(peer1.leaveRoom("Master_Room"));
		
	}
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}

}
