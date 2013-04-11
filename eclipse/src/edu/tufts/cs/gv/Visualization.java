package edu.tufts.cs.gv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import edu.tufts.cs.gv.view.GraphView;
import edu.tufts.cs.gv.view.ResultsView;
import edu.tufts.cs.gv.view.StudentView;
import edu.tufts.cs.gv.view.VizView;

public class Visualization extends JFrame{
	private static final long serialVersionUID = 1L;

	private Timer renderTimer;
	
	private JSplitPane studentSplit, testSplit;
	private VizView graphView, studentView, resultsView;
	
	public Visualization(int fps) {
		graphView = new GraphView();
		resultsView = new ResultsView();
		studentView = new StudentView();
		
		testSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, graphView, resultsView);
		testSplit.setResizeWeight(.7);
		testSplit.setDividerLocation(.7);
		testSplit.setOneTouchExpandable(true);
		studentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, studentView, testSplit);
		studentSplit.setResizeWeight(.3);
		studentSplit.setDividerLocation(.3);
		studentSplit.setOneTouchExpandable(true);
		
		this.setSize(600, 600);
		this.add(studentSplit);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		renderTimer = new Timer((int)(1000.0 / fps), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graphView.update();
				resultsView.update();
				studentView.update();
				repaint();
			}
		});
		renderTimer.start();
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new Visualization(1);
	}
}