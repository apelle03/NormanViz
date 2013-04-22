package edu.tufts.cs.gv.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.tufts.cs.gv.controller.VizEventType;
import edu.tufts.cs.gv.controller.VizState;
import edu.tufts.cs.gv.model.Dataset;
import edu.tufts.cs.gv.model.TestCase;
import edu.tufts.cs.gv.model.graph.Vertex;

//This class will be a bar chart of the witnesses.

public class ResultsView extends VizView {
	private static final long serialVersionUID = 1L;
	
	private static final int maxBars = 5; //The maximum number of bars for a particular test case
	private static final float barSpacing = 2; //Number of pixels between bars of a test case
	private static final int barWidth   = 70;
	private static final float testCaseSpacing = 20; //Spacing between test cases
	
	private ArrayList<HashMap<String, Integer>> witnesses;
	private ArrayList<String> testcases;
	private int maxBarChartHeight;
	private HashMap<Rectangle, String> bars;

	public ResultsView() {
		VizState.getState().addVizUpdateListener(this);
		maxBarChartHeight = 0;
		this.setToolTipText("");
	}
	
	@Override
	public void vizUpdated(VizEventType eventType) {
		if(eventType == VizEventType.NEW_DATA_SOURCE) {
			int screenWidth = 0;
			Dataset dataset = VizState.getState().getDataset();
			Set<String> testNames = dataset.getAllTestNames();
			witnesses = new ArrayList<>(testNames.size());
			testcases = new ArrayList<>(testNames.size());
			for(String testName : testNames) {
				Set<TestCase> testCases = dataset.getTestCasesForTest(testName);
				testcases.add(testName);
				HashMap<String, Integer> witnessMap = new HashMap<>();
				witnesses.add(witnessMap);
				int numUniqueWitnesses = 0;
				for(TestCase t : testCases) {
					String witness = t.getWitness();
					int count = 1;
					if(witnessMap.containsKey(witness)) {
						count += witnessMap.get(witness);
					} else {
						numUniqueWitnesses++;
						if(numUniqueWitnesses <= maxBars) {
							screenWidth += barSpacing + barWidth;
						}
					}
					maxBarChartHeight = Math.max(maxBarChartHeight, count);
					witnessMap.put(witness, count);
				}
				//TODO: if there are more than 5, limit to 5
				screenWidth += testCaseSpacing;
			}
			this.setPreferredSize(new Dimension(screenWidth, this.getHeight()));
			updateRectangles();
		}
	}
	
	private void updateRectangles() {
		if(witnesses == null)
			return;
		bars = new HashMap<>();
		int height = this.getHeight();
		float heightFactor = height/(float)maxBarChartHeight;
		float x = 0;
		int y = height - 1;
		for(HashMap<String, Integer> testcase : witnesses) {
			for(String witness : testcase.keySet()) {
				int count = ((Integer)testcase.get(witness)).intValue();
				int barHeight = (int)(count * heightFactor);
				bars.put(new Rectangle((int)x, y - barHeight, barWidth, barHeight), witness);
				x += barWidth + barSpacing;
			}
			x += testCaseSpacing;
		}
	}

	public void paint(Graphics g) {
		if(bars == null) {
			updateRectangles();
		}
		if(bars == null) {
			return;
		}
		Color[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE};
		int colorIndex = 0;
		for(Rectangle bar : bars.keySet()) {
			g.setColor(colors[colorIndex]);
			colorIndex = (colorIndex + 1) % colors.length;
			g.fillRect(bar.x, bar.y, bar.width, bar.height);
		}
	}
	
	public String getToolTipText(MouseEvent e) {
		if (bars != null) {
			for (Rectangle bar : bars.keySet()) {
				if(bar.contains(e.getX(), e.getY())) {
					return bars.get(bar);
				}
			}
			return null;
		}
		return null;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		//System.out.println("Results update");
	}
}
