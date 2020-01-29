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

public class CreateSecretRoomTest {
	
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
		assertTrue(masterPeer.createSecretRoom("Master_Room", "password"));
		/*
		 * verifichiamo che il creatore sia già all'interno 
		 * della room da lui creata e che ci sia solo lui
		 */
		assertEquals("Master_Room_secret", masterPeer.listRooms().get(0));
		assertEquals(1, masterPeer.getPeersInRoom("Master_Room"));
		
		assertTrue(peer1.createSecretRoom("Room_1", "password"));
		assertTrue(peer2.createSecretRoom("Room_2", "password"));
		assertTrue(peer3.createSecretRoom("Room_3", "password"));
		
		assertFalse(masterPeer.createSecretRoom("Room_1", "password_1"));
		assertFalse(peer1.createSecretRoom("Master_Room", "password_2"));
		assertFalse(peer2.createSecretRoom("Room_3", "pippo"));
		assertFalse(peer3.createSecretRoom("Room_2", "password"));
		
		/*
		 * Dobbiamo verificare che non sia possibile creare una secret room con lo stesso 
		 * nome di una public room e viceversa.
		 */
				
		masterPeer.createRoom("Prova_Room");
		assertFalse(peer1.createSecretRoom("Prova_Room", "password"));
		
		peer2.createSecretRoom("Prova_Room_2", "password");
		assertFalse(peer3.createRoom("Prova_Room_2"));
	}
	
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}
}
