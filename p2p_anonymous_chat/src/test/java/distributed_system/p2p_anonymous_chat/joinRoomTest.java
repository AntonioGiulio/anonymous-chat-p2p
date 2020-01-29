package distributed_system.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import distibuted_system.p2p_anonymous_chat.AnonymousChatImpl;
import distibuted_system.p2p_anonymous_chat.MessageListener;

public class joinRoomTest {
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
		
		/*
		 * Gli altri peer entrano nella room creata
		 */
		assertTrue(peer1.joinRoom("Master_Room"));
		assertTrue(peer2.joinRoom("Master_Room"));
		assertTrue(peer3.joinRoom("Master_Room"));
		
		/*
		 * Verifichiamo che nella lista delle room a cui 
		 * sono iscritti i peer ci sia la MasterRoom
		 */
		assertEquals("Master_Room", masterPeer.listRooms().get(0));
		assertEquals("Master_Room", peer1.listRooms().get(0));
		assertEquals("Master_Room", peer2.listRooms().get(0));
		assertEquals("Master_Room", peer3.listRooms().get(0));
		
		/*
		 * Verifichiamo che adesso i peer nella room sono 4
		 */
		assertEquals(4, masterPeer.getPeersInRoom("Master_Room"));
		
		/*
		 * verifichiamo che non si può accedere ad una room
		 * che non esiste
		 */
		assertFalse(masterPeer.joinRoom("notExist"));
		assertFalse(peer1.joinRoom("notExist"));
		assertFalse(peer2.joinRoom("notExist"));
		assertFalse(peer3.joinRoom("notExist"));
		
		/*
		 * Verifichiamo che non si può accedere in una 
		 * room in cui si è già iscritti
		 */
		assertFalse(masterPeer.joinRoom("Master_Room"));
		assertFalse(peer1.joinRoom("Master_Room"));
		assertFalse(peer2.joinRoom("Master_Room"));
		assertFalse(peer3.joinRoom("Master_Room"));
		
		/*
		 * Verifichiamo che con il metodo joinRoom
		 * non è possibile accedere ad una room segreta
		 */
		masterPeer.createSecretRoom("Secret_Room", "password");
		assertFalse(peer1.joinRoom("Secret_Room"));
		assertFalse(peer2.joinRoom("Secret_Room"));
		assertFalse(peer3.joinRoom("Secret_Room"));		
		
	}
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}

}
