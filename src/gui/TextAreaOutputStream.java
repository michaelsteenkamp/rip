package gui;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class TextAreaOutputStream extends OutputStream {
	private JTextArea textControl;
	private int carretCount = 0;

	public TextAreaOutputStream(JTextArea control) {
		textControl = control;
	}

	public void write(int b) throws IOException {
		textControl.append(String.valueOf((char) b));
		carretCount++;
		textControl.setCaretPosition(carretCount);
	}
}
