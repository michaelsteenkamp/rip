package main;

import fileParser.FileParser;
import gui.MainGUI;
import routingDaemon.RoutingDaemon;

public class Main {


	public static void main(String[] args) {
		MainGUI gui = new MainGUI();
	}

	public static void StartRoutingDaemon(FileParser fp) {
		RoutingDaemon daemon = new RoutingDaemon(fp.RouterId, fp.InputPorts,
				fp.OutputPorts);
	}


}
