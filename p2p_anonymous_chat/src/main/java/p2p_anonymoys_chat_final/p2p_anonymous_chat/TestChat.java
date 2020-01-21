package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class TestChat {
	
	@Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;
	
	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;
	

	public static void main(String[] args) throws Exception {
		
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
		
		TestChat testChat = new TestChat();
		final CmdLineParser parser = new CmdLineParser(testChat);
		
		try {
			parser.parseArgument(args);
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			
			AnonymousChatImpl peer = new AnonymousChatImpl(id, master, new MessageListenerImpl(id));
			String room_name;
			while(true) {
				printMenu(terminal);
				
				int option = textIO.newIntInputReader()
						.withMaxVal(9)
						.withMinVal(0)
						.read("Option: ");
				
				switch (option) {
				case 0:
					terminal.printf("\nARE YOU SURE TO LEAVE NETWORK?\n");
					boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
					if(exit) {
						peer.leaveNetwork();
						System.exit(0);
					}
					break;
				case 1:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					if(peer.createRoom(room_name))
						terminal.printf("\n SUCCESSFULLY CREATED %s ROOM \n", room_name);
					else
						terminal.printf("\n ERROR WHILE CREATING ROOM \n");
					break;
					
				case 2:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-secret-room")
							.read("Name: ");
					String password = textIO.newStringInputReader()
							.withDefaultValue("password")
							.withInputMasking(true)
							.read("Password: ");
					terminal.printf("\nENTER PASSWORD\n");
					String password__1 = textIO.newStringInputReader()
							.withDefaultValue("password")
							.withInputMasking(true)
							.read("Rewrite password: ");
					if(password.equals(password__1)) {
						if(peer.createSecretRoom(room_name, password)) 
							terminal.printf("\nSUCCESSFULLY CREATED %s SECRET ROOM\n", room_name);
						else
							terminal.printf("\nERROR WHILE CREATING SECRET ROOM\n");								
					}else
						terminal.printf("\nERROR IN PASSWORD\n");
					break;
				case 3:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					if(peer.joinRoom(room_name))
						terminal.printf("\nSUCCESSFULLY ENTERED IN %s ROOM\n", room_name);
					else 
						terminal.printf("\nERROR WHILE JOINING ROOM\n");
					break;
				case 4:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					terminal.printf("\nENTER PASSWORD\n");
					String psw = textIO.newStringInputReader()
							.withDefaultValue("password")
							.withInputMasking(true)
							.read("Password: ");
					if(peer.joinSecretRoom(room_name, psw))
						terminal.printf("\nSUCCESSFULLY ENTERED IN %s SECRET ROOM\n", room_name);
					else
						terminal.printf("\nERROR WHILE JOINING SECRET ROOM\n");
					break;
				case 5:
					terminal.printf("\nENTER ROOM NAME\n");
					room_name = textIO.newStringInputReader()
							.withDefaultValue("default-room")
							.read("Name: ");
					if(peer.leaveRoom(room_name))
						terminal.printf("\nSUCCESSFULLY LEAVED %s ROOM\n", room_name);
					else 
						terminal.printf("\nERROR WHILE LEAVING ROOM\n");
				default:
					break;
				}
			}
			
		}catch (CmdLineException clEx) {
			System.out.println("ERROR: Unable to parse command-line optins: " + clEx);
			clEx.printStackTrace();
		}

	}
	
	@SuppressWarnings("rawtypes")
	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE CHAT ROOM\n");
		terminal.printf("\n2 - CREATE SECRET CHAT ROOM\n");
		terminal.printf("\n3 - JOIN TO CHAT ROOM\n");
		terminal.printf("\n4 - JOIN TO SECRET CHAT ROOM\n");
		terminal.printf("\n5 - EXIT FROM CHAT ROOM\n");
		
		terminal.printf("\n0 - EXIT\n");
	}

}
