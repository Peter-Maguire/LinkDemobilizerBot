package com.unacceptableuse.linkdemobilizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.Credentials;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.LoggedInAccount;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.listener.ComponentListener;


public class LinkDemobilizerBot 
{
	public static final String CONFIG_PATH = "bot.cfg";
	public static final String DEFAULT_CONFIG = 
				"#Reddit Bot Config# \n"
			+ 	"username=CHANGEME #Bot username\n"
			+ 	"password=CHANGEME #Bot password\n"
			+ 	"userAgent=CHANGEME #User Agent, typically BOTNAME VERSION by CREATOR\n"
			+ 	"submissionLimit=100 #How many submissions to do per time interval ";
		
	Properties props = new Properties();
	RedditClient reddit;
	AccountManager manager;
	GUIScreen gui;
	MainWindow window = new MainWindow("LinkDemoblizerBot");
	private int subredditCount = 0;
	
	/**
	 * Loads the properties, starts the reddit connection and runs the loop
	 * @throws FileNotFoundException If the properties file isn't found
	 * @throws IOException If it fails to connect to reddit
	 * @throws ApiException If reddit's API fails
	 * @throws NetworkException If it fails to connect to reddit
	 */
	public LinkDemobilizerBot() throws FileNotFoundException, IOException, NetworkException, ApiException
	{
		gui = TerminalFacade.createGUIScreen();
		
		if(gui == null)
			throw new NullPointerException("Couldn't allocate a terminal!");
	    	
		
		props.load(new FileInputStream(CONFIG_PATH));
		reddit = new RedditClient(props.getProperty("userAgent"));
		reddit.setEnforceRatelimit(30);
		reddit.setRequestLoggingEnabled(false);

		LoggedInAccount account = reddit.login(Credentials.standard(props.getProperty("username"), props.getProperty("password")));
		
		manager = new AccountManager(reddit);
		
		
		ThreadReddit bot = new ThreadReddit(this, reddit, manager, props);
		Thread thread = new Thread(bot);
		
		thread.start();
		
		gui.getScreen().startScreen();
		gui.showWindow(window, GUIScreen.Position.FULL_SCREEN);
		
		
		gui.getScreen().stopScreen();
	}
	
	public void log(String log){
		window.logArea.appendLine(log);
	}
	
	public void addSubreddit(String subreddit){
		if(subredditCount > 0)if(window.subArea.getLine(subredditCount-1).equals(subreddit))return;
		window.subArea.insertLine(subredditCount, subreddit);
		window.subArea.appendLine("");
		subredditCount++;
	}

	
    public static void main( String[] args )
    {
        try{
			new LinkDemobilizerBot();
			
		} catch (FileNotFoundException e){
			System.err.println("Properties file not found... creating one.");
			File newFile = new File(CONFIG_PATH);
			if(newFile.exists())
			{
				System.err.println("Properties file exists already? Please check this and try again.");
				System.exit(-1);
			}else
			{
				try{
					newFile.createNewFile();
					BufferedWriter br = new BufferedWriter(new FileWriter(newFile));
					br.write(DEFAULT_CONFIG);
					br.close();
				}catch(IOException e1)
				{
					System.err.println("Unable to create new file: "+e1.getMessage());
					System.exit(-1);
				}
			}
		} catch (IOException e){
			
			System.err.println("Error reading properties file: "+e.getMessage());
			e.printStackTrace();
		} catch (NetworkException e) {
			System.err.println("Error connecting to reddit");
			e.printStackTrace();
		} catch (ApiException e) {
			System.err.println("Error connecting to reddit");
			e.printStackTrace();
		}
    }
    
    

}
