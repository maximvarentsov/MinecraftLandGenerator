/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package corrodias.minecraft.landgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
//import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.LongTag;
import org.jnbt.NBTInputStream;
import org.jnbt.NBTOutputStream;
import org.jnbt.Tag;
//import java.io.*;					//if we want to import everything...
//import java.util.*;

/**
 *
 * @author Corrodias, Morlok8k, pr0f1x
 */
public class Main {

	//Version Number!
	private static final String VERSION = "1.5.1";
	private static final String AUTHORS = "Corrodias, Morlok8k, pr0f1x";
	
	private static final String separator = System.getProperty("file.separator");
	//private static final String classpath = System.getProperty("java.class.path");
	//private static final String javaPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

	private int increment = 380;
	private ProcessBuilder minecraft = null;
	private String javaLine = null;
	private static final String defaultJavaLine = "java -Djline.terminal=jline.UnsupportedTerminal -Duser.language=en -Xms1024m -Xmx1024m -Xincgc -jar minecraft_server.jar nogui";
	private String serverPath = null;
	private String worldPath = null;
	private static String worldName = null;
	private static String doneText = null;
	private static String preparingText = null;
	private static String preparingLevel = null;
	private static String level_0 = null;			//the world
	private static String level_1 = null;			//the nether
	private static String level_2 = null;			//future worlds
	private static String level_3 = null;
	private static String level_4 = null;
	private static String level_5 = null;
	private static String level_6 = null;
	private static String level_7 = null;
	private static String level_8 = null;
	private static String level_9 = null;
	private int xRange = 0;
	private int yRange = 0;
	private Integer xOffset = null;
	private Integer yOffset = null;
	private boolean verbose = false;
	private boolean alternate = false;
	private static boolean waitSave = false;
	private static boolean ignoreWarnings = false;
	private static LongTag randomSeed = null;
	
	private static String MLG = "[MLG] ";
	
	private static DateFormat dateFormat = null;
	private static DateFormat dateFormatBuildID = null;
	private static Date date = null;
	private static Date MLG_Last_Modified_Date = null;
	
	
	private static final boolean testing = false;	// a constant to display more output when debugging
	
	// REMINDER: because I always forget/mix up languages:
	// "static" in java means "global"
	// "final" means "constant"

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		(new Main()).run(args);  //Why?  idk, but merging this with run() creates errors, and i'm lazy!
	}

	private void run(String[] args) {
		
		// Lets get a nice Date format for display, and a compact one for telling apart builds.
		dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:m a zzzz", Locale.ENGLISH);
		dateFormatBuildID = new SimpleDateFormat("'BuildID:' (yyMMdd.HHmmssZ)", Locale.ENGLISH);
        date = new Date();
        //dateFormat.format(date);
		
        //Here we attempt to get the last modified date of this compiled .class file from the filesystem!
		try {
			MLG_Last_Modified_Date = new Date(Main.class.getResource("Main.class").openConnection().getLastModified());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//The following displays no matter what happens, so we needed this date stuff to happen first.
		
		System.out.println("Minecraft Land Generator version " + VERSION);
		System.out.println(dateFormatBuildID.format(MLG_Last_Modified_Date));
		System.out.println("This version was last modified on " + dateFormat.format(MLG_Last_Modified_Date));
		System.out.println("");
		System.out.println("Uses a Minecraft server to generate square land of a specified size.");
		System.out.println("");
		System.out.println("");

		
		
		
		// =====================================================================
		//                           INSTRUCTIONS
		// =====================================================================

		if (args.length == 0 || args[0].equals("-version") || args[0].equals("-help") || args[0].equals("/?")) {
			System.out.println("Usage: java -jar MinecraftLandGenerator.jar x y [serverpath] [switches]");
			System.out.println("");
			System.out.println("Arguments:");
			System.out.println("              x : X range to generate");
			System.out.println("              y : Y range to generate");
			System.out.println("     serverpath : the path to the directory in which the server runs (takes precedence over the config file setting)");
			System.out.println("");
			System.out.println("Switches:");
			System.out.println("       -verbose : causes the application to output the server's messages to the console");
			System.out.println("             -v : same as -verbose");
			System.out.println("             -w : Ignore [WARNING] and [SEVERE] messages.");
			System.out.println("           -alt : alternate server launch sequence");
			System.out.println("             -a : same as -alt");
			System.out.println("            -i# : override the iteration spawn offset increment (default 300) (example: -i100)");
			System.out.println("            -x# : set the X offset to generate land around (example: -x0)");
			System.out.println("            -y# : set the X offset to generate land around (example: -y0)");
			System.out.println("");
			System.out.println("Other options:");
			System.out.println("  java -jar MinecraftLandGenerator.jar -printspawn");
			System.out.println("  java -jar MinecraftLandGenerator.jar -ps");
			System.out.println("        Outputs the current world's spawn point coordinates.");
			System.out.println("");
			System.out.println("  java -jar MinecraftLandGenerator.jar -conf");
			System.out.println("        Generates a MinecraftLandGenerator.conf file.");
			System.out.println("");
			System.out.println("  java -jar MinecraftLandGenerator.jar -version");
			System.out.println("  java -jar MinecraftLandGenerator.jar -help");
			System.out.println("  java -jar MinecraftLandGenerator.jar /?");
			System.out.println("        Prints this message.");
			System.out.println("");
			System.out.println("When launched with the -conf switch, this application creates a MinecraftLandGenerator.conf file that contains configuration options.");
			System.out.println("If this file does not exist or does not contain all required properties, the application will not run.");
			System.out.println("");
			System.out.println("MinecraftLandGenerator.conf properties:");
			System.out.println("           Java : The command line to use to launch the server");
			System.out.println("     ServerPath : The path to the directory in which the server runs (can be overridden by the serverpath argument)");
			System.out.println("      Done_Text : The output from the server that tells us that we are done");
			System.out.println(" Preparing_Text : The output from the server that tells us the percentage");
			System.out.println("Preparing_Level : The output from the server that tells us the level it is working on");
			System.out.println("        Level-0 : Name of Level 0: The Overworld");
			System.out.println("        Level-1 : Name of Level 1: The Nether");
			System.out.println("        Level-2 : Name of Level 2: The End");
			System.out.println("        Level-3 : Name of Level 3: (Future Level)");
			System.out.println("        Level-4 : Name of Level 4: (Future Level)");
			System.out.println("        Level-5 : Name of Level 5: (Future Level)");
			System.out.println("        Level-6 : Name of Level 6: (Future Level)");
			System.out.println("        Level-7 : Name of Level 7: (Future Level)");
			System.out.println("        Level-8 : Name of Level 8: (Future Level)");
			System.out.println("        Level-9 : Name of Level 9: (Future Level)");
			System.out.println("       WaitSave : Optional: Wait before saving.");

			return;
		}

		// =====================================================================
		//                         STARTUP AND CONFIG
		// =====================================================================

				
		// the arguments are apparently okay so far. parse the conf file.
		if (args[0].equalsIgnoreCase("-conf")) {
			try {
				File config = new File("MinecraftLandGenerator.conf");
				BufferedWriter out = new BufferedWriter(new FileWriter(config));
				out.write("#Minecraft Land Generator Configuration File:  Version: " + VERSION);
				out.newLine();
				out.write("#Authors: " + AUTHORS);
				out.newLine();
				out.write("#Auto-Generated: " + dateFormat.format(date));
				out.newLine();
				out.newLine();
				out.write("#Line to run server:");
				out.newLine();
				out.write("Java=" + defaultJavaLine);   // reads the default from a constant, makes it easier!
				out.newLine();
				out.newLine();
				out.write("#Location of server.  use \".\" for the same folder as MLG");
				out.newLine();
				out.write("ServerPath=.");
				out.newLine();
				out.newLine();
				out.write("#Strings read from the server");
				out.newLine();
				out.write("Done_Text=[INFO] Done");
				out.newLine();
				out.write("Preparing_Text=[INFO] Preparing spawn area:");
				out.newLine();
				out.write("Preparing_Level=[INFO] Preparing start region for");
				out.newLine();
				out.write("Level-0=The Overworld");
				out.newLine();
				out.write("Level-1=The Nether");
				out.newLine();
				out.write("Level-2=The End");
				out.newLine();
				out.write("Level-3=Level 3 (Future Level)");
				out.newLine();
				out.write("Level-4=Level 4 (Future Level)");
				out.newLine();
				out.write("Level-5=Level 5 (Future Level)");
				out.newLine();
				out.write("Level-6=Level 6 (Future Level)");
				out.newLine();
				out.write("Level-7=Level 7 (Future Level)");
				out.newLine();
				out.write("Level-8=Level 8 (Future Level)");
				out.newLine();
				out.write("Level-9=Level 9 (Future Level)");
				out.newLine();
				out.newLine();
				out.write("#Optional: Wait a few seconds after saving.");
				out.newLine();
				out.write("WaitSave=false");
				out.newLine();
				out.close();
				System.out.println( MLG + "MinecraftLandGenerator.conf file created.");
				return;
			} catch (IOException ex) {
				System.err.println( MLG + "Could not create MinecraftLandGenerator.conf.");
				return;
			}
		} else if (args[0].equalsIgnoreCase("-ps") || args[0].equalsIgnoreCase("-printspawn")) {
			// okay, sorry, this is an ugly hack, but it's just a last-minute feature.
			printSpawn();
			return;
		} else if (args.length == 1) {
			System.out.println( MLG + "For help, use java -jar MinecraftLandGenerator.jar -help");
			return;
		}

		try {
			File config = new File("MinecraftLandGenerator.conf");
			BufferedReader in = new BufferedReader(new FileReader(config));
			String line;
			while ((line = in.readLine()) != null) {
				int pos = line.indexOf('=');
				int end = line.lastIndexOf('#');	//comments, ignored lines
								
				if (end == -1){		// If we have no hash sign, then we read till the end of the line
					end = line.length();
				}
				if (end <= pos){	// If hash is before the '=', we may have a issue... it should be fine, cause we check for issues next, but lets make sure.
					end = line.length();
				}

				if (pos != -1) {
					if (line.substring(0, pos).toLowerCase().equals("serverpath")) {
						serverPath = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("java")) {
						javaLine = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("done_text")) {
						doneText = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("preparing_text")) {
						preparingText = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("preparing_level")) {
						preparingLevel = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-1")) {		//old, but still works
						level_1 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-2")) {		//to be removed later!
						level_2 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-3")) {
						level_3 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-4")) {
						level_4 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-5")) {
						level_5 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-6")) {
						level_6 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-7")) {
						level_7	 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-8")) {
						level_8 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("dim-9")) {		//end of deprecated names
						level_9 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-0")) {
						level_0 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-1")) {
						level_1 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-2")) {
						level_2 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-3")) {
						level_3 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-4")) {
						level_4 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-5")) {
						level_5 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-6")) {
						level_6 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-7")) {
						level_7	 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-8")) {
						level_8 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("level-9")) {
						level_9 = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("waitsave")) {
						String wstmp = line.toLowerCase().substring(pos + 1, end);
			            if (wstmp.equals( "true" ) ){
			              waitSave = true;
			            } else {
			              waitSave = false;
			            }
					}
				}
			}
			in.close();

			if (testing) {
				System.out.println( MLG + "[TEST] Test Output: Reading of Config File ");
				System.out.println( MLG + "[TEST]     serverPath: " + serverPath);
				System.out.println( MLG + "[TEST]       javaLine: " + javaLine);
				System.out.println( MLG + "[TEST]       doneText: " + doneText);
				System.out.println( MLG + "[TEST]  preparingText: " + preparingText);
				System.out.println( MLG + "[TEST] preparingLevel: " + preparingLevel);
				System.out.println( MLG + "[TEST]        level_0: " + level_0);
				System.out.println( MLG + "[TEST]        level_1: " + level_1);
				System.out.println( MLG + "[TEST]        level_2: " + level_2);
				System.out.println( MLG + "[TEST]        level_3: " + level_3);
				System.out.println( MLG + "[TEST]        level_4: " + level_4);
				System.out.println( MLG + "[TEST]        level_5: " + level_5);
				System.out.println( MLG + "[TEST]        level_6: " + level_6);
				System.out.println( MLG + "[TEST]        level_7: " + level_7);
				System.out.println( MLG + "[TEST]        level_8: " + level_8);
				System.out.println( MLG + "[TEST]        level_9: " + level_9);
				System.out.println( MLG + "[TEST]       waitSave: " + waitSave);
			}
			
			boolean oldConf = false;				//This next section checks to see if we have a old configuration file (or none!)
						
			if (serverPath == null || javaLine == null) {			//MLG 1.2 Check for a valid .conf file.  
				System.err.println( MLG + "MinecraftLandGenerator.conf does not contain all required properties.  Making New File!");		// Please recreate it by running this application with -conf.
				//return;							//We no longer quit.  We generate a new one with defaults.
				javaLine = defaultJavaLine;
				serverPath = ".";
				oldConf = true;
			}
			
			if (doneText == null) {					//MLG 1.3
				oldConf = true;
			} else if ( preparingText == null) { 	//MLG 1.4?
				oldConf = true;
			} else if (preparingLevel == null){		//MLG 1.4?
				oldConf = true;
			} else if (level_1 == null) {			//MLG 1.5.0?
				oldConf = true;
			} else if (level_0 == null) {			//MLG 1.5.1
				oldConf = true;
			}
			
			
			if (oldConf) {
				System.err.println( MLG + "Old Version of MinecraftLandGenerator.conf found.  Updating...");
				try {
					File configUpdate = new File("MinecraftLandGenerator.conf");
					BufferedWriter out = new BufferedWriter(new FileWriter(configUpdate));
					out.write("#Minecraft Land Generator Configuration File:  Version: " + VERSION);
					out.newLine();
					out.write("#Authors: " + AUTHORS);
					out.newLine();
					out.write("#Auto-Updated: " + dateFormat.format(date));
					out.newLine();
					out.newLine();
					out.write("#Line to run server:");
					out.newLine();
					out.write("Java=" + javaLine);
					out.newLine();
					out.newLine();
					out.write("#Location of server.  use \".\" for the same folder as MLG");
					out.newLine();
					out.write("ServerPath=" + serverPath);
					out.newLine();
					out.newLine();
					out.write("#Strings read from the server");
					out.newLine();
					out.write("Done_Text=[INFO] Done");
					out.newLine();
					out.write("Preparing_Text=[INFO] Preparing spawn area:");
					out.newLine();
					out.write("Preparing_Level=[INFO] Preparing start region for");
					out.newLine();
					out.write("Level-0=The Overworld");
					out.newLine();
					out.write("Level-1=The Nether");
					out.newLine();
					out.write("Level-2=The End");
					out.newLine();
					out.write("Level-3=Level 3 (Future Level)");
					out.newLine();
					out.write("Level-4=Level 4 (Future Level)");
					out.newLine();
					out.write("Level-5=Level 5 (Future Level)");
					out.newLine();
					out.write("Level-6=Level 6 (Future Level)");
					out.newLine();
					out.write("Level-7=Level 7 (Future Level)");
					out.newLine();
					out.write("Level-8=Level 8 (Future Level)");
					out.newLine();
					out.write("Level-9=Level 9 (Future Level)");
					out.newLine();
					out.newLine();
					out.write("#Optional: Wait a few seconds after saving.");
					out.newLine();
					out.write("WaitSave=false");
					out.newLine();
					out.close();
					System.out.println( MLG + "MinecraftLandGenerator.conf file created.");
					
					System.out.println("");
					int count = 0;
					while (count <= 100) {
						System.out.print(count + "% ");
						
						try {
				              Thread.sleep( 1000 );
				        } catch ( InterruptedException e ) {
				              e.printStackTrace();
				        }
						count += 10;
					}
					System.out.println("");
					return;
					
				} catch (IOException ex) {
					System.err.println( MLG + "Could not create MinecraftLandGenerator.conf.");
					return;
				}
			}
			
		} catch (FileNotFoundException ex) {
			System.out.println( MLG + "Could not find MinecraftLandGenerator.conf. It is recommended that you run the application with the -conf option to create it.");
			return;
		} catch (IOException ex) {
			System.err.println( MLG + "Could not read MinecraftLandGenerator.conf");
			return;
		}

		// ARGUMENTS
		try {
			xRange = Integer.parseInt(args[0]);
			yRange = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			System.err.println( MLG + "Invalid X or Y argument.");
			return;
		}
		
		verbose = false;		// Verifing that these vars are false
		alternate = false;		// before changing them...
		
		// This is embarrassing. Don't look.
		try {
			for (int i = 0; i < args.length - 2; i++) {
				String nextSwitch = args[i + 2].toLowerCase();
				if (nextSwitch.equals("-verbose") || nextSwitch.equals("-v")) {
					verbose = true;
				} else if (nextSwitch.startsWith("-i")) {
					increment = Integer.parseInt(args[i + 2].substring(2));
				} else if (nextSwitch.startsWith("-w")) {
					ignoreWarnings = true;
				} else if (nextSwitch.equals("-alt") || nextSwitch.equals("-a")) {
					System.out.println( MLG + "Using Alternate Launching...");
					alternate = true;
				} else if (nextSwitch.startsWith("-x")) {
					xOffset = Integer.valueOf(args[i + 2].substring(2));
				} else if (nextSwitch.startsWith("-y")) {
					yOffset = Integer.valueOf(args[i + 2].substring(2));
				} else {
					serverPath = args[i + 2];
				}
			}
		} catch (NumberFormatException ex) {
			System.err.println( MLG + "Invalid -i switch value.");
			return;
		}

		{
			// verify that we ended up with a good server path, either from the file or from an argument.
			File file = new File(serverPath);
			if (!file.exists() || !file.isDirectory()) {
				System.err.println( MLG + "The server directory is invalid: " + serverPath);
				return;
			}
		}

		try {
			// read the name of the current world from the server.properties file
			BufferedReader props = new BufferedReader(new FileReader(new File(serverPath + separator + "server.properties")));
			String line;
			while ((line = props.readLine()) != null) {
				int pos = line.indexOf('=');
				if (pos != -1) {
					if (line.substring(0, pos).toLowerCase().equals("level-name")) {
						worldPath = serverPath + separator + line.substring(pos + 1);
						worldName = line.substring(pos + 1);
					}
					
				}
			}

		} catch (FileNotFoundException ex) {
			System.err.println( MLG + "Could not open " + serverPath + separator + "server.properties");
			return;
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}

		{
			File backupLevel = new File(worldPath + separator + "level_backup.dat");
			if (backupLevel.exists()) {
				System.err.println( MLG + "There is a level_backup.dat file left over from a previous attempt that failed. You should go determine whether to keep the current level.dat"
						+ " or restore the backup.");
				return;
			}
		}

		// =====================================================================
		//                              PROCESSING
		// =====================================================================

		System.out.println( MLG + "Processing world \"" + worldPath + "\", in " + increment + " block increments, with: " + javaLine);
		//System.out.println( MLG + "Processing \"" + worldName + "\"...");
		
		System.out.println("");

		// prepare our two ProcessBuilders
		//minecraft = new ProcessBuilder(javaLine, "-Xms1024m", "-Xmx1024m", "-jar", jarFile, "nogui");
		minecraft = new ProcessBuilder(javaLine.split("\\s")); // is this always going to work? i don't know.
		minecraft.directory(new File(serverPath));
		minecraft.redirectErrorStream(true);

		try {
			System.out.println( MLG + "Launching server once to make sure there is a world.");
			runMinecraft(minecraft, verbose, alternate, javaLine);
			System.out.println("");

			File serverLevel = new File(worldPath + separator + "level.dat");
			File backupLevel = new File(worldPath + separator + "level_backup.dat");

			System.out.println( MLG + "Backing up level.dat to level_backup.dat.");
			copyFile(serverLevel, backupLevel);
			System.out.println("");

			Integer[] spawn = getSpawn(serverLevel);
			System.out.println( MLG + "Spawn point detected: [" + spawn[0] + ", " + spawn[2] + "]");
			{
				boolean overridden = false;
				if (xOffset == null) {
					xOffset = spawn[0];
				} else {
					overridden = true;
				}
				if (yOffset == null) {
					yOffset = spawn[2];
				} else {
					overridden = true;
				}
				if (overridden) {
					System.out.println( MLG + "Centering land generation on [" + xOffset + ", " + yOffset + "] due to switches.");
				}
			}
			System.out.println("");

			int totalIterations = (xRange / increment + 1) * (yRange / increment + 1);
			int currentIteration = 0;

			long differenceTime = System.currentTimeMillis();
			Long[] timeTracking = new Long[]{differenceTime, differenceTime, differenceTime, differenceTime};
			for (int currentX = 0 - xRange / 2; currentX <= xRange / 2; currentX += increment) {
				for (int currentY = 0 - yRange / 2; currentY <= yRange / 2; currentY += increment) {
					currentIteration++;
					
					
					
					System.out.println( MLG + "Setting spawn to [" + Integer.toString(currentX + xOffset) + ", " + Integer.toString(currentY + yOffset) + "] (" + currentIteration + "/" + totalIterations + ") " + Float.toString((Float.parseFloat(Integer.toString(currentIteration)) / Float.parseFloat(Integer.toString(totalIterations))) * 100) + "% Done" );			// Time Remaining estimate
					timeTracking[0] = timeTracking[1];
					timeTracking[1] = timeTracking[2];
					timeTracking[2] = timeTracking[3];
					timeTracking[3] = System.currentTimeMillis();
					if (currentIteration >= 4) {
						differenceTime = (timeTracking[3] - timeTracking[0]) / 3; // well, this is what it boils down to
						differenceTime *= 1 + (totalIterations - currentIteration);
						System.out.println( MLG + String.format("Estimated time remaining: %dh%dm%ds",
								differenceTime / (1000 * 60 * 60), (differenceTime % (1000 * 60 * 60)) / (1000 * 60), ((differenceTime % (1000 * 60 * 60)) % (1000 * 60)) / 1000));
					}

					// Set the spawn point
					setSpawn(serverLevel, currentX + xOffset, 128, currentY + yOffset);

					// Launch the server
					runMinecraft(minecraft, verbose, alternate, javaLine);
					System.out.println("");
				}
			}

			System.out.println( MLG + "Finished generating chunks.");
			copyFile(backupLevel, serverLevel);
			backupLevel.delete();
			System.out.println( MLG + "Restored original level.dat.");
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	protected static Integer[] getSpawn(File level) throws IOException {
		try {
			NBTInputStream input = new NBTInputStream(new FileInputStream(level));
			CompoundTag originalTopLevelTag = (CompoundTag) input.readTag();
			input.close();

			Map<String, Tag> originalData = ((CompoundTag) originalTopLevelTag.getValue().get("Data")).getValue();
			// This is our map of data. It is an unmodifiable map, for some reason, so we have to make a copy.
			Map<String, Tag> newData = new LinkedHashMap<String, Tag>(originalData);
			// .get() a couple of values, just to make sure we're dealing with a valid level file, here. Good for debugging, too.
			IntTag spawnX = (IntTag) newData.get("SpawnX");
			IntTag spawnY = (IntTag) newData.get("SpawnY");
			IntTag spawnZ = (IntTag) newData.get("SpawnZ");
			
			randomSeed = (LongTag) newData.get("RandomSeed");
			System.out.println( MLG + "Seed: " + randomSeed.getValue());	//lets output the seed, cause why not?
			
			
			Integer[] ret = new Integer[]{spawnX.getValue(), spawnY.getValue(), spawnZ.getValue()};
			return ret;
		} catch (ClassCastException ex) {
			throw new IOException("Invalid level format.");
		} catch (NullPointerException ex) {
			throw new IOException("Invalid level format.");
		}
	}

	/**
	 * Changes the spawn point in the given Alpha/Beta level to the given coordinates.
	 * Note that, in Minecraft levels, the Y coordinate is height, while Z is
	 * what may normally be thought of as Y.
	 * @param level the level file to change the spawn point in
	 * @param x the new X value
	 * @param z the new Y value
	 * @param y the new Z value
	 * @throws IOException if there are any problems reading/writing the file
	 */
	protected static void setSpawn(File level, Integer x, Integer y, Integer z) throws IOException {
		try {
			NBTInputStream input = new NBTInputStream(new FileInputStream(level));
			CompoundTag originalTopLevelTag = (CompoundTag) input.readTag();
			input.close();

// <editor-fold defaultstate="collapsed" desc="structure">
// Structure:
//
//TAG_Compound("Data"): World data.
//    * TAG_Long("Time"): Stores the current "time of day" in ticks. There are 20 ticks per real-life second, and 24000 ticks per Minecraft day, making the day length 20 minutes. 0 appears to be sunrise, 12000 sunset and 24000 sunrise again.
//    * TAG_Long("LastPlayed"): Stores the Unix time stamp (in milliseconds) when the player saved the game.
//    * TAG_Compound("Player"): Player entity information. See Entity Format and Mob Entity Format for details. Has additional elements:
//          o TAG_List("Inventory"): Each TAG_Compound in this list defines an item the player is carrying, holding, or wearing as armor.
//                + TAG_Compound: Inventory item data
//                      # TAG_Short("id"): Item or Block ID.
//                      # TAG_Short("Damage"): The amount of wear each item has suffered. 0 means undamaged. When the Damage exceeds the item's durability, it breaks and disappears. Only tools and armor accumulate damage normally.
//                      # TAG_Byte("Count"): Number of items stacked in this inventory slot. Any item can be stacked, including tools, armor, and vehicles. Range is 1-255. Values above 127 are not displayed in-game.
//                      # TAG_Byte("Slot"): Indicates which inventory slot this item is in.
//          o TAG_Int("Score"): Current score, doesn't appear to be implemented yet. Always 0.
//    * TAG_Int("SpawnX"): X coordinate of the player's spawn position. Default is 0.
//    * TAG_Int("SpawnY"): Y coordinate of the player's spawn position. Default is 64.
//    * TAG_Int("SpawnZ"): Z coordinate of the player's spawn position. Default is 0.
//    * TAG_Byte("SnowCovered"): 1 enables, 0 disables, see Winter Mode							//Old!
//    * TAG_Long("SizeOnDisk"): Estimated size of the entire world in bytes.
//    * TAG_Long("RandomSeed"): Random number providing the Random Seed for the terrain.
// </editor-fold>

			Map<String, Tag> originalData = ((CompoundTag) originalTopLevelTag.getValue().get("Data")).getValue();
			// This is our map of data. It is an unmodifiable map, for some reason, so we have to make a copy.
			Map<String, Tag> newData = new LinkedHashMap<String, Tag>(originalData);
			// .get() a couple of values, just to make sure we're dealing with a valid level file, here. Good for debugging, too.

			@SuppressWarnings("unused")
			IntTag spawnX = (IntTag) newData.get("SpawnX");		// we never use these...   Its only here for potential debugging.
			@SuppressWarnings("unused")
			IntTag spawnY = (IntTag) newData.get("SpawnY");		// but whatever...  so I (Morlok8k) suppressed these warnings.
			@SuppressWarnings("unused")
			IntTag spawnZ = (IntTag) newData.get("SpawnZ");		// I don't want to remove existing code, either by myself (Morlok8k) or Corrodias
			
			newData.put("SpawnX", new IntTag("SpawnX", x));
			newData.put("SpawnY", new IntTag("SpawnY", y));
			newData.put("SpawnZ", new IntTag("SpawnZ", z));

			// Again, we can't modify the data map in the old Tag, so we have to make a new one.
			CompoundTag newDataTag = new CompoundTag("Data", newData);
			Map<String, Tag> newTopLevelMap = new HashMap<String, Tag>(1);
			newTopLevelMap.put("Data", newDataTag);
			CompoundTag newTopLevelTag = new CompoundTag("", newTopLevelMap);

			NBTOutputStream output = new NBTOutputStream(new FileOutputStream(level));
			output.writeTag(newTopLevelTag);
			output.close();
		} catch (ClassCastException ex) {
			throw new IOException("Invalid level format.");
		} catch (NullPointerException ex) {
			throw new IOException("Invalid level format.");
		}
	}

	/**
	 * Starts the process in the given ProcessBuilder, monitors its output for a
	 * "[INFO] Done!" message, and sends it a "stop\r\n" message. One message is printed
	 * to the console before launching and one is printed to the console when the
	 * Done! message is detected. If "verbose" is true, the process's output will
	 * also be printed to the console.
	 * @param minecraft
	 * @param verbose
	 * @throws IOException
	 */
	protected static void runMinecraft(ProcessBuilder minecraft, boolean verbose, boolean alternate, String javaLine) throws IOException {
		System.out.println( MLG + "Starting server.");
		
		boolean warning = false;
		
		// monitor output and print to console where required.
		// STOP the server when it's done.
		
		// Damn it Java!  I hate you so much!
		// I can't reuse code the way I want to, like in other langauges.
		// So, here is a bunch of duplicate code...
		// Stupid compile errors...
		
		if (alternate) {   //Alternate - a replication (slightly stripped down) of MLG 1.3.0's code.  simplest code possible.
			System.out.println( MLG + "Alternate Launch");
			Process process = minecraft.start();

			// monitor output and print to console where required.
			// STOP the server when it's done.
			BufferedReader pOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = pOut.readLine()) != null) {
				System.out.println(line);
				if (line.contains(doneText)) {     //EDITED By Morlok8k for Minecraft 1.3+ Beta
					OutputStream outputStream = process.getOutputStream();
					if (waitSave) {
			            System.out.println( MLG + "Waiting 30 seconds to save.");
			            
			            int count = 0;
						while (count <= 30) {
							System.out.print(count + "sec ");
							
							try {
					              Thread.sleep( 1000 );
					        } catch ( InterruptedException e ) {
					              e.printStackTrace();
					        }
							count += 1;
						}
						System.out.println("");
					}
					
					System.out.println( MLG + "Saving server data...");
					byte[] saveall = {'s', 'a', 'v', 'e', '-', 'a', 'l', 'l', '\r', '\n'};
					outputStream.write(saveall);
					outputStream.flush();
					byte[] stop = {'s', 't', 'o', 'p', '\r', '\n'};
					
					System.out.println( MLG +  "Stopping server..." );
					
					outputStream.write(stop);
					outputStream.flush();
					if (waitSave) {
			            System.out.println( MLG + "Waiting 10 seconds for save.");
			            int count = 0;
						while (count <= 10) {
							System.out.print(count + "sec ");
							
							try {
					              Thread.sleep( 1000 );
					        } catch ( InterruptedException e ) {
					              e.printStackTrace();
					        }
							count += 1;
						}
						System.out.println("");
					}
				}
			}
			// readLine() returns null when the process exits
			
		} else {  //start minecraft server normally!
			Process process = minecraft.start();
			if (verbose) {
				System.out.println( MLG + "Started Server.");
			}
			BufferedReader pOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			if (verbose) {
				System.out.println( MLG + "Accessing Server Output...");
			}
			
			String line = null;
			
			byte[] stop = {'s', 't', 'o', 'p', '\r', '\n'};		//Moved here, so this code wont run every loop, thus Faster!
					//and no, i can't use a string here!
			
			byte[] saveAll = {'s', 'a', 'v', 'e', '-', 'a', 'l', 'l', '\r', '\n'};
			
			OutputStream outputStream = process.getOutputStream();		//moved here to remove some redundancy
			

			while ((line = pOut.readLine()) != null) {
				if (verbose) {
					if (line.contains("[INFO]")){
						System.out.println(line.substring(line.lastIndexOf("]") + 2));
					} else {
						System.out.println(line);
					}
				} else if (line.contains(preparingText)){
					System.out.print(line.substring(line.length() - 3, line.length()) + "... ");
				} else if (line.contains(preparingLevel)){
					if (line.contains("level 0")) {				//"Preparing start region for level 0"
						System.out.println("\r\n" + worldName + "-" + level_0 + ":");
					} else if (line.contains("level 1")) {		//"Preparing start region for level 1"
						System.out.println("\r\n" + worldName + "-" + level_1 + ":");
					} else if (line.contains("level 2")) {		//"Preparing start region for level 2"
						System.out.println("\r\n" + worldName + "-" + level_2 + ":");
					} else if (line.contains("level 3")) {		//"Preparing start region for level 3"
						System.out.println("\r\n" + worldName + "-" + level_3 + ":");
					} else if (line.contains("level 4")) {		//"Preparing start region for level 4"
						System.out.println("\r\n" + worldName + "-" + level_4 + ":");
					} else if (line.contains("level 5")) {		//"Preparing start region for level 5"
						System.out.println("\r\n" + worldName + "-" + level_5 + ":");
					} else if (line.contains("level 6")) {		//"Preparing start region for level 6"
						System.out.println("\r\n" + worldName + "-" + level_6 + ":");
					} else if (line.contains("level 7")) {		//"Preparing start region for level 7"
						System.out.println("\r\n" + worldName + "-" + level_7 + ":");
					} else if (line.contains("level 8")) {		//"Preparing start region for level 8"
						System.out.println("\r\n" + worldName + "-" + level_8 + ":");
					} else if (line.contains("level 9")) {		//"Preparing start region for level 9"
						System.out.println("\r\n" + worldName + "-" + level_9 + ":");
					} else {
						System.out.println(line.substring(line.lastIndexOf("]") + 2));
					}
				}
				
				if (line.contains(doneText)) {     // now this is configurable!
					System.out.println("");
					if ( waitSave ) {
						System.out.println( MLG +  "Waiting 30 seconds for save." );

						int count = 0;
						while (count <= 30) {
							System.out.print(count + "sec ");
							
							try {
					              Thread.sleep( 1000 );
					        } catch ( InterruptedException e ) {
					              e.printStackTrace();
					        }
							count += 1;
						}
						System.out.println("");
					}
					System.out.println( MLG + "Saving server data.");
					outputStream.write(saveAll);
					outputStream.flush();
					
					System.out.println( MLG + "Stopping server.");
					//OutputStream outputStream = process.getOutputStream();
					outputStream.write(stop);
					outputStream.flush();
					//outputStream.close();

					if ( waitSave ) {
						System.out.println( MLG +  "Waiting 10 seconds for save." );
						int count = 0;
						while (count <= 10) {
							System.out.print(count + "sec ");
							
							try {
					              Thread.sleep( 1000 );
					        } catch ( InterruptedException e ) {
					              e.printStackTrace();
					        }
							count += 1;
						}
						System.out.println("");
					}
				}
				if (ignoreWarnings == false) {
					if (line.contains("[WARNING]")) {		//If we have a warning, stop...
						System.out.println("");
						System.out.println( MLG + "Warning found: Stopping Minecraft Land Generator");
						if (verbose == false) {   //If verbose is true, we already displayed it.
							System.out.println(line);
						}
						System.out.println("");
						System.out.println( MLG + "Forcing Save...");
							outputStream.write(saveAll);
							outputStream.flush();
						//OutputStream outputStream = process.getOutputStream();
						outputStream.write(stop);	//if the warning was a fail to bind to port, we may need to write stop twice!
						outputStream.flush();
						outputStream.write(stop);
						outputStream.flush();
						//outputStream.close();
						warning = true;
						//System.exit(1);  
					}
					if (line.contains("[SEVERE]")) {		//If we have a severe error, stop...
						System.out.println("");
						System.out.println( MLG + "Severe error found: Stopping server.");
						if (verbose == false) {   //If verbose is true, we already displayed it.
							System.out.println(line);
						}
						System.out.println("");
						System.out.println( MLG + "Forcing Save...");
            outputStream.write(saveAll);
            outputStream.flush();
						//OutputStream outputStream = process.getOutputStream();
						outputStream.write(stop);
						outputStream.flush();
						outputStream.write(stop);	//sometimes we need to do stop twice...
						outputStream.flush();
						//outputStream.close();
						warning = true;
						//System.exit(1);
						//Quit!
					}
				}
			}
			
			if (warning == true){			//in 1.4.4 we had a issue.  tried to write stop twice, but we had closed the stream already.  this, and other lines should fix this.
				outputStream.flush();
				outputStream.close();
				System.exit(1);
			}
			
			outputStream.close();
		}
		
		// readLine() returns null when the process exits
	}
	
	// Corrodias:
	// I'd love to use nio, but it requires Java 7.
	// I could use Apache Commons, but i don't want to include a library for one little thing.
	// Copies src file to dst file.
	// If the dst file does not exist, it is created
	public static void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) >= 0) {
			if (len > 0) {
				out.write(buf, 0, len);
			}
		}
		in.close();
		out.flush();
		out.close();
	}

	private boolean printSpawn() {
		// ugh, sorry, this is an ugly hack, but it's a last-minute feature.
		// this is a lot of duplicated code.

		try {
			File config = new File("MinecraftLandGenerator.conf");
			BufferedReader in = new BufferedReader(new FileReader(config));
			String line;
			while ((line = in.readLine()) != null) {
				int pos = line.indexOf('=');
				int end = line.lastIndexOf('#');	//comments, ignored lines
				
				
				if (end == -1){		// If we have no hash sign, then we read till the end of the line
					end = line.length();
				}
				
				if (end <= pos){	// If hash is before the '=', we may have a issue... it should be fine, cause we check for issues next, but lets make sure.
					end = line.length();
				}
				
				if (pos != -1) {
					if (line.substring(0, pos).toLowerCase().equals("serverpath")) {
						serverPath = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("java")) {
						javaLine = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("done_text")) {
						doneText = line.substring(pos + 1, end);
					} else if (line.substring(0, pos).toLowerCase().equals("preparing_text")) {
						preparingText = line.substring(pos + 1, end);
					}
				}
			}
			in.close();

			if (serverPath == null || javaLine == null) {
				System.err.println( MLG + "MinecraftLandGenerator.conf does not contain all requird properties. Please recreate it by running this application with no arguments.");
				return false;
			}
		} catch (FileNotFoundException ex) {
			System.out.println( MLG + "Could not find MinecraftLandGenerator.conf. It is recommended that you run the application with the -conf option to create it.");
			return false;
		} catch (IOException ex) {
			System.err.println( MLG + "Could not read MinecraftLandGenerator.conf");
			return false;
		}

		{
			// verify that we ended up with a good server path, either from the file or from an argument.
			File file = new File(serverPath);
			if (!file.exists() || !file.isDirectory()) {
				System.err.println( MLG + "The server directory is invalid: " + serverPath);
				return false;
			}
		}

		try {
			// read the name of the current world from the server.properties file
			BufferedReader props = new BufferedReader(new FileReader(new File(serverPath + separator + "server.properties")));
			String line;
			while ((line = props.readLine()) != null) {
				int pos = line.indexOf('=');
				if (pos != -1) {
					if (line.substring(0, pos).toLowerCase().equals("level-name")) {
						worldPath = serverPath + separator + line.substring(pos + 1);
					}
				}
			}

		} catch (FileNotFoundException ex) {
			System.err.println( MLG + "Could not open " + serverPath + separator + "server.properties");
			return false;
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}

		File level = new File(worldPath + separator + "level.dat");
		if (!level.exists() || !level.isFile()) {
			System.err.println( MLG + "The currently-configured world does not exist. Please launch the server once, first.");
			return false;
		}

		try {
			Integer[] spawn = getSpawn(level);
			System.out.println( MLG + "The current spawn point is: [" + spawn[0] + ", " + spawn[2] + "]");
			return true;
		} catch (IOException ex) {
			System.err.println( MLG + "Error while reading " + level.getPath());
			return false;
		}
	}
}