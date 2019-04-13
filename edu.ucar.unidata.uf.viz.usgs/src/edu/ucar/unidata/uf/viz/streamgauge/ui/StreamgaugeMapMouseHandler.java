package edu.ucar.unidata.uf.viz.streamgauge.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.geotools.referencing.GeodeticCalculator;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.actions.LoadBundleHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.InputAdapter;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;
import com.vividsolutions.jts.geom.Coordinate;

import edu.ucar.unidata.common.dataplugin.usgs.StreamflowStation;


public class StreamgaugeMapMouseHandler extends InputAdapter {

	private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(StreamgaugeMapMouseHandler.class);
	
	public StreamgaugeMapMouseHandler() {
		instance = this;
	}

	private static final double MinDistance = 45000;
	
	private static StreamgaugeMapMouseHandler instance;
	
	private double lat, lon;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
    
    public static StreamgaugeMapMouseHandler getAccess() {
        return instance;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int, int,
     * int)
     */
    @Override
    public boolean handleMouseDown(int x, int y, int button) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDownMove(int,
     * int, int) handle left button, so user be able to shift map while it is
     * down
     */
    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        return false;

    }

    @Override
    public boolean handleMouseMove(Event e) {
    	int x = e.x;
    	int y = e.y;
    	AbstractEditor mapEditor = StreamgaugeMapResource.getMapEditor();
        
    	if (mapEditor != null) {
            
        	Coordinate loc = mapEditor.translateClick(x, y);
            if (loc == null)
                return false;
            
            List<StreamflowStation> pts = StreamgaugeMapResource
        			.getOrCreateStreamgaugeMapResource().getPoints();
            
            StreamflowStation pt = getPtWithinMinDist(pts, loc);
            
            Shell shell = ((Control) e.widget).getShell();
            Cursor cursor = null;
            
            if (pt != null) {
            	cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
            } else {
            	cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW);
            }
            
        	shell.setCursor(cursor);
        	
        }
    	
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseUp(int, int, int)
     * handle right button, so user be able to pick stn and print text report
     */
    @Override
    public boolean handleMouseUp(int x, int y, int button) {
    	
    	boolean returnStatus = false;
    	
        if (!StreamgaugeMapResource.getMapRsc().isEditable())
            return false;
        
        // left mouse button
        if (button == 1) {
            AbstractEditor mapEditor = StreamgaugeMapResource.getMapEditor();
            if (mapEditor != null) {
            	
                Coordinate loc = mapEditor.translateClick(x, y);
                if (loc == null)
                    return false;
                
                List<StreamflowStation> pts = StreamgaugeMapResource
            			.getOrCreateStreamgaugeMapResource().getPoints();
                StreamflowStation pt = getPtWithinMinDist(pts, loc);
                
                if (pt != null) {
                	
	                try {
	                	
		                	IWorkbenchWindow window = VizWorkbenchManager.getInstance()
		                            .getCurrentWindow();
		                	AbstractVizPerspectiveManager mgr = VizPerspectiveListener.getInstance(
		                            window).getActivePerspectiveManager();
		
	                    if (mgr != null) {
	                    	
	                    		Map<String, String> variableSubstitutions = new HashMap<>();
	                    		variableSubstitutions.put("stationid", pt.getStationId());
	                    		variableSubstitutions.put("lat", pt.getLatitude().toString());
	                    		variableSubstitutions.put("lon", pt.getLongitude().toString());
	                        new LoadBundleHandler("bundles/streamgauge/timeseries.xml", 
	                        			variableSubstitutions, 
	                        			null, 
	                        			true).execute(null);
	                        
	                        returnStatus = true;
	                        
	                    }
						
					} catch (ExecutionException e) {
						e.printStackTrace();
						return false;
					}
	                
                }
            }
        }
     
        return returnStatus;
    }
    
    
    /**
     * Gets the nearest point of an selected element to the input point
     * 
     * @param el
     *            element
     * @param pt
     *            input point
     * @return 
     */
    private StreamflowStation getPtWithinMinDist(
    		List<StreamflowStation> points, Coordinate pt) {
    	
        StreamflowStation thePoint = null;
        double minDistance = MinDistance;
        
        GeodeticCalculator gc;
        // can't assume this is a map Editor/MapDescriptor
        AbstractEditor mapEditor = StreamgaugeMapResource.getMapEditor();
        if (mapEditor != null && ! Double.isNaN(pt.x) && ! Double.isNaN(pt.y) ) {
            IMapDescriptor desc = (IMapDescriptor) mapEditor
                    .getActiveDisplayPane().getRenderableDisplay()
                    .getDescriptor();
            
            for (StreamflowStation selectPoint : points) {
            	
                double dist;
                try {
                	gc = new GeodeticCalculator(desc.getCRS());
                    gc.setStartingGeographicPoint(pt.x, pt.y);
                    gc.setDestinationGeographicPoint(selectPoint.getLongitude(),
                    		selectPoint.getLatitude());
                    dist = gc.getOrthodromicDistance();
                    if (dist < minDistance) {
                        minDistance = dist;
                        thePoint = selectPoint;
                    }
                } catch (Exception e) {
                    statusHandler.handle(
                    		Priority.WARN,"getOrthodromicDistance exception.",e);
                }
            }
           
            StreamgaugeMapResource.getOrCreateStreamgaugeMapResource()
            .setPickedPoint(thePoint);
        }
        return thePoint;

    }
    
    
}
