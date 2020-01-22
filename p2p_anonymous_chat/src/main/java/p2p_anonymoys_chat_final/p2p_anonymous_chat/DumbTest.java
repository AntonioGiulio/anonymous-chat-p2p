package p2p_anonymoys_chat_final.p2p_anonymous_chat;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.beryx.textio.TerminalProperties;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;


public class DumbTest {

	public static void main(String[] args) {
		TextIO textIO = TextIoFactory.getTextIO();

		

		TextTerminal terminal = textIO.getTextTerminal();
		terminal.getProperties().setPromptColor("cyan");
		terminal.println("1. Choose the desired hard drive.");
		terminal.executeWithPropertiesConfigurator(
		        props -> ((TerminalProperties) props).setPromptColor("red"),
		        t -> ((TextTerminal) t).println("2. Backup all your data."));
		terminal.println("3. Start the formatting process.");
		
	}

}
