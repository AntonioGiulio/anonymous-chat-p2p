package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
		
		
		masterPeer = new AnonymousChatImpl(0, "127.0.0.1", new MessageListenerImpl(10));
		peer1 = new AnonymousChatImpl(1, "127.0.0.1", new MessageListenerImpl(11));
		peer2 = new AnonymousChatImpl(2, "127.0.0.1", new MessageListenerImpl(12));
		peer3 = new AnonymousChatImpl(3, "127.0.0.1", new MessageListenerImpl(13));
		System.out.println("Ho creato la network di create room");
		System.out.println(masterPeer.listRooms());
		System.out.println(peer1.listRooms());
		System.out.println(peer2.listRooms());
		System.out.println(peer3.listRooms());
		
	}
	
	
	
	@Test
	public void test() throws Exception {
		System.out.println("inizio a fare sti test di merda");
		assertTrue(masterPeer.createRoom("Master_Room"));
		/*
		 * verifichiamo che il creatore sia già all'interno 
		 * della room da lui creata e che ci sia solo lui
		 */
		assertEquals("Master_Room", masterPeer.listRooms().get(0));
		assertEquals(1, masterPeer.getPeersInRoom("Master_Room"));
		
		assertTrue(peer1.createRoom("Room_1"));
		System.out.println(peer1.listRooms());
		System.out.println(peer2.listRooms());
		assertTrue(peer2.createRoom("Room_2"));
		assertTrue(peer3.createRoom("Room_3"));		
		
		assertFalse(masterPeer.createRoom("Room_1"));
		assertFalse(peer1.createRoom("Master_Room"));
		assertFalse(peer2.createRoom("Room_3"));
		assertFalse(peer3.createRoom("Room_2"));
		
		System.out.println("ho finito di fare sti test di merda");
		
			
	}
	
	
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}
	

}
