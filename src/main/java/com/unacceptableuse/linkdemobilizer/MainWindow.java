package com.unacceptableuse.linkdemobilizer;

import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.ProgressBar;
import com.googlecode.lanterna.gui.component.TextArea;
import com.googlecode.lanterna.terminal.TerminalSize;

public class MainWindow extends Window{

	
	
	public TextArea logArea = new TextArea(), subArea = new TextArea();
	public ProgressBar submissionProgress = new ProgressBar(250);
	public ProgressBar overallProgress = new ProgressBar(250);
	public MainWindow(String title) {
		super(title);
		Panel horisontalPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
        Panel leftPanel = new Panel(new Border.Bevel(true), Panel.Orientation.VERTICAL);
        Panel middlePanel = new Panel(new Border.Bevel(true), Panel.Orientation.VERTICAL);
        Panel rightPanel = new Panel(new Border.Bevel(true), Panel.Orientation.VERTICAL);
        
        logArea.setMinimumSize(new TerminalSize(250, 100));
        logArea.setMaximumSize(new TerminalSize(250, 100));
        
        middlePanel.setTitle("Log");
        rightPanel.setTitle("Subreddits");
        
        middlePanel.addComponent(logArea);
        rightPanel.addComponent(subArea);
        
        horisontalPanel.addComponent(middlePanel);
        horisontalPanel.addComponent(rightPanel);
        

        addComponent(horisontalPanel);
        addComponent(submissionProgress);
        addComponent(overallProgress);
        
       
	}
	
	
	
	

}
