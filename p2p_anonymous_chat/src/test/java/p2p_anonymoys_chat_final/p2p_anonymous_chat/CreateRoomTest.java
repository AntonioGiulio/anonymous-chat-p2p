package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;




public class CreateRoomTest {
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
	public void test() throws Exception {
		
		assertTrue(masterPeer.createRoom("Master_Room"));
		/*
		 * verifichiamo che il creatore sia già all'interno 
		 * della room da lui creata e che ci sia solo lui
		 */
		assertEquals("Master_Room", masterPeer.listRooms().get(0));
		assertEquals(1, masterPeer.getPeersInRoom("Master_Room"));
		
		assertTrue(peer1.createRoom("Room_1"));
		assertTrue(peer2.createRoom("Room_2"));
		assertTrue(peer3.createRoom("Room_3"));		
		
		assertFalse(masterPeer.createRoom("Room_1"));
		assertFalse(peer1.createRoom("Master_Room"));
		assertFalse(peer2.createRoom("Room_3"));
		assertFalse(peer3.createRoom("Room_2"));			
	}
	
	
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}
	

}
