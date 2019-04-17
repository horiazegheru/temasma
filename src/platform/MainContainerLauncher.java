package platform;

import agents.EnvironmentAgent;
import agents.utils.GridPosition;
import agents.utils.Hole;
import agents.utils.Tile;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import agents.MyAgent;

public class MainContainerLauncher
{
	AgentContainer						mainContainer;

	int agentsNr;
	int operationTime;
	int totalTime;
	int width;
	int height;
	Map<String, GridPosition> agentColPos = new HashMap<>();
	ArrayList<GridPosition> obstacles = new ArrayList<>();
	ArrayList<Tile> tiles = new ArrayList<>();
	ArrayList<Hole> holes = new ArrayList<>();

	/**
	 * Configures and launches the main container.
	 */
	void setupPlatform()
	{
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, "Intro-Main"); // you can rename it
		
		mainProps.setProperty(Profile.LOCAL_HOST, "localhost");
		mainProps.setProperty(Profile.LOCAL_PORT, "1099");
		mainProps.setProperty(Profile.PLATFORM_ID, "intro");
		
		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		mainContainer = Runtime.instance().createMainContainer(mainProfile);
	}
	
	@SuppressWarnings("serial")

	void readTest() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("data/tests/system__default.txt"))) {
			String[] lineElems;
			lineElems = br.readLine().split(" ");
			agentsNr = Integer.parseInt(lineElems[0]);
			operationTime = Integer.parseInt(lineElems[1]);
			totalTime = Integer.parseInt(lineElems[2]);
			width = Integer.parseInt(lineElems[3]);
			height = Integer.parseInt(lineElems[4]);

			br.readLine();

			String[] colors = br.readLine().split(" ");
			String[] positions = br.readLine().split(" ");

			for (int i = 0; i < agentsNr; ++i) {
				agentColPos.put(colors[i], new GridPosition(
						Integer.parseInt(positions[2 * i]),
						Integer.parseInt(positions[2 * i + 1])));
			}

			br.readLine();
			br.readLine();
			String[] obstaclesLine = br.readLine().split(" ");

			for (int i = 0; i < obstaclesLine.length / 2; ++i) {
				obstacles.add(new GridPosition(
						Integer.parseInt(obstaclesLine[2 * i]),
						Integer.parseInt(obstaclesLine[2 * i + 1])));
			}

			br.readLine();
			br.readLine();

			String line;
			while((line = br.readLine()) != null) {
				String[] tilesElem = line.split(" ");
				if(tilesElem.length == 1)
					break;

				tiles.add(new Tile(
						Integer.parseInt(tilesElem[0]),
						tilesElem[1],
						new GridPosition(
								Integer.parseInt(tilesElem[2]),
								Integer.parseInt(tilesElem[3]))
				));
			}

			br.readLine();

			while((line = br.readLine()) != null) {
				String[] holesElem = line.split(" ");
				if(holesElem.length == 1)
					break;

				holes.add(new Hole(
						Integer.parseInt(holesElem[0]),
						holesElem[1],
						new GridPosition(
								Integer.parseInt(holesElem[2]),
								Integer.parseInt(holesElem[3]))
				));
			}
		}
	}

	/**
	 * Starts the agents assigned to the main container.
	 */
	void launchAgents() throws StaleProxyException {
		for (Map.Entry<String, GridPosition> entry: agentColPos.entrySet()) {
			AgentController agentCtrl = mainContainer.createNewAgent(entry.getKey(), MyAgent.class.getName(),
					new Object[] {entry.getValue()});
			agentCtrl.start();
		}

		AgentController envCtrl = mainContainer.createNewAgent("env", EnvironmentAgent.class.getName(),
				new Object[] {agentsNr, operationTime, totalTime,
						width, height, obstacles, tiles, holes});
		envCtrl.start();
	}

	/**
	 * Launches the main container.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args)
	{
		MainContainerLauncher launcher = new MainContainerLauncher();
		
		try
		{
			launcher.readTest();
			launcher.setupPlatform();
			launcher.launchAgents();
		} catch(IOException e)
		{
			e.printStackTrace();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
}
