package edu.ucar.unidata.uf.viz.streamflow.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.viz.core.DrawableCircle;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery.QueryLanguage;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList.RemoveListener;
import com.raytheon.uf.viz.core.rsc.capabilities.EditableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.MagnificationCapability;
import com.raytheon.viz.pointdata.StaticPlotInfoPV;
import com.raytheon.viz.pointdata.StaticPlotInfoPV.SPIEntry;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.EditableManager;

import edu.ucar.unidata.common.dataplugin.streamflow.StreamflowStation;


public class StreamflowMapResource extends
		AbstractVizResource<StreamflowMapResourceData, MapDescriptor> implements
		RemoveListener {
	private static StreamflowMapResource mapRsc = null;
			
	private static StreamflowMapResourceData mapRscData = null;
	
	private static AbstractEditor mapEditor = null;
	
	private static StreamflowMapMouseHandler mouseHandler;

	private static Cursor waitCursor = null;

    private static Control cursorControl;

    private static boolean mouseHandlerRegistered = false;
    
    public static void bringMapEditorToTop() {
        try {
            if (mapEditor != null
                    && PlatformUI.getWorkbench() != null
                    && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
                    && PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage() != null) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().bringToTop((IWorkbenchPart) mapEditor);
                mapEditor.refresh();
            }
        } catch (Exception e) {
        }
    }
    
    public static AbstractEditor getMapEditor() {
        return mapEditor;
    }

    public static StreamflowMapResource getMapRsc() {
        return mapRsc;
    }
    
    private StreamflowMapResourceData streamflowMapResourceData;
    
    /** The set of symbols */
    List<DrawableCircle> circles = null;
        
    private static List<StreamflowStation> points = new ArrayList<StreamflowStation>();
    
    private StreamflowStation pickedPoint = new StreamflowStation();
    
    public void setPickedPoint(StreamflowStation point) {
        this.pickedPoint = point;
    }

    public List<StreamflowStation> getPoints() {
        return points;
    }
    
    public void setPoints(List<StreamflowStation> points) {
        if (points == null) {
            this.points.clear();
        } else {
            this.points = points;
        }
    }

    public void addPoint(StreamflowStation point) {
        points.add(point);
    }

    protected StreamflowMapResource(StreamflowMapResourceData streamflowMapResourceData,
    		LoadProperties loadProperties) {
    	super(streamflowMapResourceData, loadProperties);
    	
        getCapability(EditableCapability.class).setEditable(true);

        this.streamflowMapResourceData = streamflowMapResourceData;
    }

    public static void startWaitCursor() {
        waitCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
        cursorControl = Display.getCurrent().getCursorControl();
        if (cursorControl != null && waitCursor != null)
            cursorControl.setCursor(waitCursor);
    }

    public static void stopWaitCursor() {
        if (cursorControl != null && waitCursor != null) {
            cursorControl.setCursor(null);
        }
        if (waitCursor != null) {
            waitCursor.dispose();
            waitCursor = null;
        }
    }
    
    private static void createMapEditor() {
    	deleteStreamflowMapResource();
        try {
        	mapEditor = (AbstractEditor) EditorUtil.getActiveEditor();
        } catch (Exception ve) {
            System.out
                    .println("StreamflowMapResource Could not load initial editor: "
                            + ve.getMessage());
            ve.printStackTrace();
        }
    }
    
    public static void registerMouseHandler() {
        if (mouseHandlerRegistered)
            return;

        mouseHandler = getMouseHandler();
        if (mapEditor != null && mouseHandler != null) {
            mapEditor.registerMouseHandler((IInputHandler) mouseHandler);
            mouseHandlerRegistered = true;
        }
    }

    public static void unregisterMouseHandler() {
        if (!mouseHandlerRegistered)
            return;
        mouseHandler = getMouseHandler();
        if (mapEditor != null && mouseHandler != null) {
            mapEditor.unregisterMouseHandler((IInputHandler) mouseHandler);
            mouseHandlerRegistered = false;
        }
    }
    
    /**
    * Create a new MapResource and add it to the current editor.
    * 
    * @return the MapResource
    */
   public static StreamflowMapResource getOrCreateStreamflowMapResource() {
	   
       if (mapRsc == null) {
           if (mapEditor == null) {
        	   createMapEditor();
           }
           if (mapEditor != null) {
               IMapDescriptor desc = (IMapDescriptor) mapEditor
                       .getActiveDisplayPane().getRenderableDisplay()
                       .getDescriptor();
               try {
                   if (mapRscData == null)
                       mapRscData = new StreamflowMapResourceData();
                   mapRsc = mapRscData.construct(new LoadProperties(), desc);
                   
                   createDBMapMarkers();
                   
                   desc.getResourceList().add(mapRsc);
                   mapRsc.init(mapEditor.getActiveDisplayPane().getTarget());
                   mouseHandler = getMouseHandler();
                   mapEditor
                           .registerMouseHandler((IInputHandler) mouseHandler);

               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       }
       return mapRsc;
   }
   
   private static void createDBMapMarkers() {
	  
	   String query = "SELECT distinct(stationid), latitude, longitude from streamflow;";
       List<Object[]> rows = null;
       try {
    	   rows = DirectDbQuery.executeQuery(query, "metadata", QueryLanguage.SQL);
       } catch (VizException e) {
           e.printStackTrace();
           rows = new ArrayList<Object[]>();
       }
       
       for (int i = 0; i < rows.size(); i++) {
    	   StreamflowStation pnt = new StreamflowStation();
	    	   Object[] pntObject = rows.get(i);
	    	   pnt.setStationId((String) pntObject[0]);
	    	   pnt.setLatitude((Float) pntObject[1]);
	    	   pnt.setLongitude((Float) pntObject[2]);
	    	   mapRsc.addPoint(pnt);
       }
       
   }
   
   private static void createSPIMapMarkers() {

	   StaticPlotInfoPV spipv = StaticPlotInfoPV
               .readStaticPlotInfoPV("basemaps/streamflow.spi");
	   
	   List<StreamflowStation> locs = new ArrayList<StreamflowStation>();
       for (Entry<String, SPIEntry> entry : spipv.getSpiList().entrySet()) {
    	   	   StreamflowStation loc = new StreamflowStation();
           Integer blockNumber = entry.getValue().blockNumber;
           loc.setStationId(blockNumber.toString());
           loc.setLatitude((float) entry.getValue().latlon.y);
           loc.setLongitude((float) entry.getValue().latlon.x);
           loc.setStationName(entry.getValue().accessId);
           locs.add(loc);
       }
       
       mapRsc.setPoints(locs);
   }
   
   public static void deleteStreamflowMapResource() {
	   System.out.println("StreamflowMapResource:deleteStreamflowMapResource ");
       if (mapRsc != null) {
           mapRsc.dispose();
           mapRsc = null;
       }
   }
   
   /**
    * Called when resource is disposed
    * 
    * @see com.raytheon.viz.core.rsc.IVizResource#dispose()
    */
   @Override
   public void disposeInternal() {
       if (mapEditor != null) {
           mapEditor.unregisterMouseHandler(mouseHandler);
           mouseHandler = null;
           mapEditor = null;
       }
       pickedPoint = null;
       mapRsc = null;
       mapRscData = null;
       if (waitCursor != null)
           waitCursor.dispose();
       waitCursor = null;
       mouseHandlerRegistered = false;
   }

   public CoordinateReferenceSystem getCoordinateReferenceSystem() {
       if (descriptor == null)
           return null;
       return descriptor.getCRS();
   }
   
   @Override
   public String getName() {
       return "Streamflow Display";
   }

   @Override
   public void initInternal(IGraphicsTarget target) throws VizException {
       // make the map resource editable
       EditableManager.makeEditable(this,
               getCapability(EditableCapability.class).isEditable());
   }
   
   public boolean isApplicable(PixelExtent extent) {
       return true;
   }
   
   private void generateSymbolForDrawing() {
	   	   
	   circles = new ArrayList<DrawableCircle>(mapRsc.getPoints().size());

       if (points.isEmpty() == true) {
           circles = null;
       } else {
           
           for (StreamflowStation p : points) {
        	   RGB color = new RGB (200,200,200);
        	   if (p.equals(pickedPoint)) {
        		   color = new RGB (100,100,100);
        	   }
               double lon, lat;
               lon = p.getLongitude();
               lat = p.getLatitude();
               double[] pixel = descriptor.worldToPixel(new double[] { lon, lat });
               DrawableCircle circle = new DrawableCircle();
               circle.setCoordinates(pixel[0], pixel[1]);
               circle.lineWidth = 1;
               circle.screenRadius = getRadius()*1.4;
               circle.numberOfPoints = (int) (circle.screenRadius * 4);
               circle.basics.color = color;
               circle.filled = false;
               circles.add(circle);
           }
           
       }

   }
   
   protected double getRadius() {
       return 5 * getCapability(MagnificationCapability.class)
               .getMagnification();
   }
   
   @Override
   public void paintInternal(IGraphicsTarget target, PaintProperties paintProps)
           throws VizException {
	   
	   getOrCreateStreamflowMapResource();
       
	   generateSymbolForDrawing();
       target.drawCircle(circles.toArray(new DrawableCircle[0]));
       
   }
   
   public boolean isProjectable(CoordinateReferenceSystem mapData) {
       return true;
   }
   
   @Override
   public void project(CoordinateReferenceSystem mapData) throws VizException {
       // System.out.println("StreamflowMapResource: project ");
   }
   
   private static StreamflowMapMouseHandler getMouseHandler() {
       if (mouseHandler == null) {
           mouseHandler = new StreamflowMapMouseHandler();
       }
       return mouseHandler;
   }
   
   @Override
   public void notifyRemove(ResourcePair rp) throws VizException {
       // TODO Auto-generated method stub
   }
   
   public boolean isEditable() {
       return getCapability(EditableCapability.class).isEditable();
   }

   public void setEditable(boolean enable) {
       getCapability(EditableCapability.class).setEditable(enable);
       EditableManager.makeEditable(this,
               getCapability(EditableCapability.class).isEditable());
   }

}
