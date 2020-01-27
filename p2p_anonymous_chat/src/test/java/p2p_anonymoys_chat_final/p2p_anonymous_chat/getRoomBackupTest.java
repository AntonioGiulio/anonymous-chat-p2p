package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import static org.junit.Assert.*;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class getRoomBackupTest {
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
		peer1.joinRoom("Master_Room");
		peer2.joinRoom("Master_Room");
		peer3.joinRoom("Master_Room");
		peer1.sendMessage("Master_Room", "Ciao a tutti!");
		peer2.sendMessage("Master_Room", "Come va?");
		peer3.sendMessage("Master_Room", "Tutto bene!");
		masterPeer.sendMessage("Master_Room", "Tutto bene anche a me!");
		
		/*
		 * Verifico che sia possibile accedere al backup di una chat
		 * a cui si è iscritti.
		 */
		assertNotNull(peer1.getRoomBackup("Master_Room"));
		
		/*
		 * Verifico che non si possa accedere al backup di una 
		 * chat che abbiamo abbandonato.
		 */
		peer2.leaveRoom("Master_Room");
		assertNull(peer2.getRoomBackup("Master_Room"));
		
		/*
		 * Verifico che si possa accedere al backup di una chat che 
		 * abbiamo abbandonato e in cui siamo rientrati.
		 */
		peer2.joinRoom("Master_Room");
		assertNotNull(peer2.getRoomBackup("Master_Room"));
		
		/*
		 * Verifico che non si possa accedere al backup di una 
		 * chat che non esiste.
		 */
		assertNull(masterPeer.getRoomBackup("Room_1"));
	}
	
	@AfterClass
	public static void shutDown() {
		masterPeer.leaveNetwork();
		peer1.leaveNetwork();
		peer2.leaveNetwork();
		peer3.leaveNetwork();
	}

}
