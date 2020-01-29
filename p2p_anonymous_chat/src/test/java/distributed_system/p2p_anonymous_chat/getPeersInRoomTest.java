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

public class getPeersInRoomTest {
	
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
		 * Verifico che il masterPeer sia l'unico peer 
		 * presente nella Master_Room
		 */
		assertEquals(1, masterPeer.getPeersInRoom("Master_Room"));
		
		peer1.joinRoom("Master_Room");
		peer2.joinRoom("Master_Room");
		peer3.joinRoom("Master_Room");
		
		/*
		 * Verifico che nella MasterRoom adesso vi siano 4 peers
		 */
		assertEquals(4, peer1.getPeersInRoom("Master_Room"));
		
		/*
		 * Se uno o più peers escono dalla room il numero deve 
		 * decrescere
		 */
		peer1.leaveRoom("Master_Room");
		peer2.leaveRoom("Master_Room");
		assertEquals(2, peer3.getPeersInRoom("Master_Room"));
		
		/*
		 * Verico che non è possibile verificare il numero di peer
		 * di una room che non esiste oppure a cui non siamo iscritti
		 */
		assertEquals(-1, peer1.getPeersInRoom("Room_1"));
		peer2.createSecretRoom("Secret_Room", "password");
		assertEquals(-1, peer3.getPeersInRoom("Secret_Room"));
	}

	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}
}
