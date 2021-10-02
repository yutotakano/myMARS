package mars.venus;
import mars.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FileReloadAction extends GuiAction {

	public FileReloadAction(String name, Icon icon, String descrip,
	                        Integer mnemonic, KeyStroke accel, VenusUI gui) {
		super(name, icon, descrip, mnemonic, accel, gui);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
		EditPane editPane = mainUI.getMainPane().getEditPane();
		if (editPane != null) {
			editPane.reloadFile();
		}
	}
}
