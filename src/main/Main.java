package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TimerTask;

import fileParser.FileParser;
import routingDaemon.RoutingDaemon;
import timer.CustomTimer;

public class Main {

	public static void main(String[] args){
		System.out.print("Enter configuration file name: ");
		BufferedReader fileNameReader = new BufferedReader(new InputStreamReader(System.in));
		String fileName = null;
		
		try{
			fileName = fileNameReader.readLine();
		} catch (Exception e){
			System.out.println("Error: Could not read in file name");
			System.exit(1);
		}
		
		FileParser fp = new FileParser(fileName);
		System.out.println(fp.toString());
		StartRoutingDaemon(fp);
	}
	
	public static void StartRoutingDaemon(FileParser fp){
		RoutingDaemon daemon = new RoutingDaemon(fp.RouterId, fp.InputPorts, fp.OutputPorts);
	}

}
