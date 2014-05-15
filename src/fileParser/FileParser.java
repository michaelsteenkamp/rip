 package fileParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileParser {
	
	private int RouterId;
	private ArrayList<String> Lines = new ArrayList<String>();
	private ArrayList<Integer> InputPorts = new ArrayList<Integer>();
	private ArrayList<OutputPortInformation> OutputPorts = new ArrayList<OutputPortInformation>();
	
	public FileParser(String fileName) {
		readLines(fileName);
		extractRoutingDaemonData();
	}
	
	/**
	 * @return the routerId
	 */
	public int getRouterId() {
		return RouterId;
	}

	/**
	 * @param routerId the routerId to set
	 */
	public void setRouterId(int routerId) {
		RouterId = routerId;
	}

	/**
	 * @return the inputPorts
	 */
	public ArrayList<Integer> getInputPorts() {
		return InputPorts;
	}

	/**
	 * @param inputPorts the inputPorts to set
	 */
	public void setInputPorts(ArrayList<Integer> inputPorts) {
		InputPorts = inputPorts;
	}

	/**
	 * @return the outputPorts
	 */
	public ArrayList<OutputPortInformation> getOutputPorts() {
		return OutputPorts;
	}

	/**
	 * @param outputPorts the outputPorts to set
	 */
	public void setOutputPorts(ArrayList<OutputPortInformation> outputPorts) {
		OutputPorts = outputPorts;
	}
	
	/**
	 * Reads data in the specified input file into the array 'Lines' so that it
	 * can be extracted
	 */
	private void readLines(String fileName) {
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (IOException e) {
			System.out.println(String.format(
					"Error: Could not open file '%s' ", fileName));
			System.exit(1);
		}

		BufferedReader br = new BufferedReader(fr);
		String line;

		try {
			while ((line = br.readLine()) != null) {
				Lines.add(line);
			}

			br.close();
			fr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Extracts the data in the Lines array into proper types and variables so
	 * that it can be easily accessed
	 */
	private void extractRoutingDaemonData() {
		extractRouterId();
		extractInputPorts();
		extractOutputPorts();
	}

	/**
	 * Extracts the router id, then converts it to an integer
	 */
	private void extractRouterId() {
		for (String line : Lines) {
			if (line.contains("router-id")) {
				String[] split = line.split(" ", -1);
				for (int i = 1; i < split.length; i++) {
					try {
						RouterId = Integer.parseInt(split[i]);
					} catch (NumberFormatException e) {
						System.out
								.println("Error: Incorrect router-id in configuration file");
						System.exit(0);
					}
				}
			}
		}
	}

	/**
	 * Extracts the input ports into a list of integers
	 */
	private void extractInputPorts() {
		for (String line : Lines) {
			if (line.contains("input-ports")) {
				String[] split = line.split(" ", -1);
				for (int i = 1; i < split.length; i++) {
					try {
						split[i] = split[i].replace(",", "");
						int inputPort = Integer.parseInt(split[i]);
						if (inputPort < 1024 || inputPort > 64000) {
							throw new Exception();
						}
						InputPorts.add(inputPort);
					} catch (Exception e) {
						System.out
								.println("Error: Incorrect input port in configuration file");
						System.exit(0);
					}
				}
			}
		}
	}

	/**
	 * Extracts the output port information into the array list OutputPorts The
	 * output port information is represented like this
	 * 
	 * PortNumber-LinkCost-RouterId 5000-3-1
	 * 
	 */
	private void extractOutputPorts() {
		for (String line : Lines) {
			if (line.contains("outputs")) {
				String[] split = line.split(" ", -1);
				try {
					for (int i = 1; i < split.length; i++) {
						split[i] = split[i].replace(",", "");
					}
					for (int i = 1; i < split.length; i++) {
						try {
							String[] split2 = split[i].split("-", -1);
							int portNumber = Integer.parseInt(split2[0]);
							if (portNumber < 1024 || portNumber > 64000) {
								throw new Exception();
							}
							int linkCost = Integer.parseInt(split2[1]);
							int routerId = Integer.parseInt(split2[2]);
							OutputPortInformation info = new OutputPortInformation(
									portNumber, linkCost, routerId);
							OutputPorts.add(info);

						} catch (Exception e) {
							System.out
									.println("Error: Incorrect output in configuration file");
							System.exit(0);
						}
					}

				} catch (Exception e) {

				}
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Router id: " + RouterId + "\n");
		sb.append("Input ports: " + InputPorts + "\n");
		sb.append("Outputs(port-cost-id): " + OutputPorts + "\n");
		return sb.toString();
	}
}
