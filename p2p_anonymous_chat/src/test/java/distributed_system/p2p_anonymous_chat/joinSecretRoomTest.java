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

public class joinSecretRoomTest {
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
		
		masterPeer.createSecretRoom("Secret_Room", "password");
		
		/*
		 * Gli altri peer entrano nella secret room
		 */
		assertTrue(peer1.joinSecretRoom("Secret_Room", "password"));
		assertTrue(peer2.joinSecretRoom("Secret_Room", "password"));
		
		/*
		 * Verifichiamo che nella lista delle room a cui 
		 * sono iscritti i peer ci sia la Secret_Room con il tag _ecret
		 */
		assertEquals("Secret_Room_secret", peer1.listRooms().get(0));
		assertEquals("Secret_Room_secret", peer2.listRooms().get(0));
		
		
		/*
		 * non si può accedere ad una room segreta sbagliando la 
		 * password
		 */
		assertFalse(peer3.joinSecretRoom("Secret_Room", "passwor"));
		
		/*
		 * Non si può accedere ad una room segreta che non esiste
		 */
		assertFalse(masterPeer.joinSecretRoom("Room_1", "psw"));
		assertFalse(peer2.joinSecretRoom("Room_x", "psw"));
		
		/*
		 * Non si può accedere ad una room segreta a cui si 
		 * è già iscritti
		 */
		assertFalse(peer1.joinSecretRoom("Secret_room", "password"));
		assertFalse(peer2.joinSecretRoom("Secret_Room", "password"));
	}
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}

}
