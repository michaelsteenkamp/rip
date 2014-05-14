package gui;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
/*
 * This class allows a text area to be bound to the system.out
 */
public class TextAreaOutputStream extends OutputStream {
	private JTextArea textArea;
	private int carretCount = 0;

	public TextAreaOutputStream(JTextArea control) {
		textArea = control;
	}

	public void write(int b) throws IOException {
		textArea.append(String.valueOf((char) b));
		carretCount++;
		textArea.setCaretPosition(carretCount);
	}
}
