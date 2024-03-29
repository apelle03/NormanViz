package edu.tufts.cs.gv.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import edu.tufts.cs.gv.controller.VizEventType;
import edu.tufts.cs.gv.controller.VizState;
import edu.tufts.cs.gv.model.Dataset;
import edu.tufts.cs.gv.model.TestCase;
import edu.tufts.cs.gv.util.Colors;
import edu.tufts.cs.gv.util.DrawingHelp;

//This class will be a bar chart of the witnesses.

public class ResultsView extends VizView {
	private static final long serialVersionUID = 1L;

	private static final int maxBars = 5; // The maximum number of bars for a
											// particular test case
	private static final float barSpacing = 2; // Number of pixels between bars
												// of a test case
	private static final int barWidth = 14;
	private static final float testCaseSpacing = 20; // Spacing between test
														// cases
	private static final int paddingX = 10;
	private static final int paddingY = 10;
	private static final List<String> helpString = Arrays.asList("This view shows the distribution of witnesses for each test case.\n",
																 "For each witness, the top " + maxBars + " witnesses are shown along with their\n",
																 "respective counts. Mouse over various bars to see their witness names.\n",
																 "The bar height is directly proportional to the frequency of that particular\n",
																 "witness.");
	private boolean shouldUpdatePreferredSize;
	private ArrayList<HashMap<String, Integer>> witnesses;
	private ArrayList<String> testcases;
	private int maxBarChartHeight;
	private LinkedHashMap<Bar, String> bars;
	
	class Bar extends Rectangle {
		private static final long serialVersionUID = 1L;
		
		public Color c;
		public Bar(Color c, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.c = c;
		}
	}

	public ResultsView() {
		VizState.getState().addVizUpdateListener(this);
		maxBarChartHeight = 0;
		this.setToolTipText("");
		shouldUpdatePreferredSize = false;		
	}
	
	public ComponentListener getListener() {
		return new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {	
			}
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				bars = null;
				shouldUpdatePreferredSize = true;
			}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
		};
	}

	@Override
	public void vizUpdated(VizEventType eventType) {
		if (eventType == VizEventType.NEW_DATA_SOURCE) {
			Dataset dataset = VizState.getState().getDataset();
			Set<String> testNames = dataset.getAllTestNames();
			witnesses = new ArrayList<>(testNames.size());
			testcases = new ArrayList<>(testNames.size());
			for (String testName : testNames) {
				Set<TestCase> testCases = dataset.getTestCasesForTest(testName);
				testcases.add(testName);
				HashMap<String, Integer> witnessMap = new HashMap<>();
				witnesses.add(witnessMap);
				for (TestCase t : testCases) {
					if (!t.didPass()) {
						String witness = t.getWitness();
						int count = 1;
						if (witnessMap.containsKey(witness)) {
							count += witnessMap.get(witness);
						}
						maxBarChartHeight = Math.max(maxBarChartHeight, count);
						witnessMap.put(witness, count);
					}
				}
				if(witnessMap.size() > maxBars) {
					ArrayList<String> keys = new ArrayList<>(witnessMap.keySet());
					final HashMap<String, Integer> clojures = witnessMap;
					Collections.sort(keys, new Comparator<String>() {
						public int compare(String s1, String s2) {
							return clojures.get(s2).intValue() - clojures.get(s1).intValue();
						  }
					});
					HashMap<String, Integer> smallerWitnessMap = new HashMap<>();
					for(int i=0; i<maxBars && i<witnessMap.size(); i++) {
						String key = keys.get(i);
						smallerWitnessMap.put(key, witnessMap.get(key));
					}
					witnessMap = smallerWitnessMap;
				}
			}
			shouldUpdatePreferredSize = true;
			bars = null;
		}
	}
	
	private int totalTextHeight(int maxTextHeight) {
		return Math.min((this.getParent().getHeight() - paddingY)/3, maxTextHeight);
	}

	private void updateRectangles(Graphics g) {
		if (witnesses == null)
			return;
		FontMetrics metrics = g.getFontMetrics();
		bars = new LinkedHashMap<>();
		int height = this.getParent().getHeight() - paddingY * 2;
		// Find the height that the text will take up
		int maxTextHeight = 0;
		for (String testname : testcases) {
			maxTextHeight = Math.max(maxTextHeight,	getTextHeight(metrics, testname));
		}
		int heightDiff = totalTextHeight(maxTextHeight);
		
		float x = paddingX;
		int y = height - 1 - heightDiff + paddingY;
		for (int i = 0; i < witnesses.size(); i++) {
			HashMap<String, Integer> testcase = witnesses.get(i);
			int maxBar = 0;
			for(String witness: testcase.keySet()) {
				maxBar = Math.max(maxBar, ((Integer)(testcase.get(witness)).intValue()));
			}
			float heightFactor = (height - heightDiff) / (float) maxBar;
			int q = 0;
			for (String witness : testcase.keySet()) {
				int count = ((Integer) testcase.get(witness)).intValue();
				int barHeight = (int) (count * heightFactor);
				bars.put(new Bar(Colors.resultsBars[q % Colors.resultsBars.length],
						(int) x, y - barHeight,
						barWidth, barHeight), witness);
				x += barWidth + barSpacing;
				q++;
			}
			x += testCaseSpacing;
		}
	}

	private int getTextHeight(FontMetrics metrics, String text) {
		return (int) (Math.sqrt(3) / 2 * metrics.stringWidth(text));
	}

	private int getTestcaseWidth(FontMetrics metrics, int testIndex) {
		int numBars = Math.min(maxBars, witnesses.get(testIndex).size());
		return (int)(numBars * (barSpacing + barWidth));
	}
	
	private void paintBarGraph(Graphics g) {
		if (bars == null) {
			updateRectangles(g);
		}
		if (bars == null) {
			return;
		}
		for (Bar bar : bars.keySet()) {
			g.setColor(bar.c);
			g.fillRect(bar.x, bar.y, bar.width, bar.height);
		}
		Bar aBar = bars.keySet().iterator().next();
		int bottomOfBars = aBar.y + aBar.height;
		int x = 0;
		Graphics2D g2 = (Graphics2D) g;
		int maxTextHeight = 0;
		for(String testname : testcases) {
			maxTextHeight = Math.max(maxTextHeight, getTextHeight(g.getFontMetrics(), testname));
		}
		
		g.setColor(Colors.foreground);
		int y = bottomOfBars;
		for (int i = 0; i < testcases.size(); i++) {
			String text = testcases.get(i);
			AffineTransform orig = g2.getTransform();
			AffineTransform rotation = new AffineTransform(orig);
			rotation.translate(x + getTestcaseWidth(g.getFontMetrics(), i) / 2, y + 5);
			rotation.rotate(Math.PI / 3);
			g2.setTransform(rotation);
			g2.drawString(text, 0, 0);
			x += getTestcaseWidth(g.getFontMetrics(), i) + testCaseSpacing;
			g2.setTransform(orig);
		}
		if(shouldUpdatePreferredSize) {
			int height = bottomOfBars + maxTextHeight;
			int width = (int)(x + paddingX * 2 + maxTextHeight/Math.sqrt(3));
			
			this.setPreferredSize(new Dimension(width, height));
			this.getParent().revalidate();
			shouldUpdatePreferredSize = false;
		}
	}
	
	private void paintHelp(Graphics g) {
		DrawingHelp.renderHelpText(this.getParent(), helpString, g);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Colors.canvasBackground);
		g.fillRect(0, 0, getWidth(), getHeight());
		paintBarGraph(g);
		if(VizState.getState().isShowingHelp()) {
			paintHelp(g);
		}
	}

	public String getToolTipText(MouseEvent e) {
		if (bars != null) {
			for (Bar bar : bars.keySet()) {
				if (bar.contains(e.getX(), e.getY())) {
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
		// System.out.println("Results update");
	}
}
