package edu.ucar.unidata.uf.viz.streamgauge.ui;

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

import edu.ucar.unidata.common.dataplugin.usgs.StreamflowStation;


public class StreamgaugeMapResource extends
		AbstractVizResource<StreamgaugeMapResourceData, MapDescriptor> implements
		RemoveListener {
	private static StreamgaugeMapResource mapRsc = null;
			
	private static StreamgaugeMapResourceData mapRscData = null;
	
	private static AbstractEditor mapEditor = null;
	
	private static StreamgaugeMapMouseHandler mouseHandler;

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

    public static StreamgaugeMapResource getMapRsc() {
        return mapRsc;
    }
    
    private StreamgaugeMapResourceData streamgaugeMapResourceData;
    
    /** The set of symbols */
    List<DrawableCircle> circles = null;
        
    private static List<StreamflowStation> points = new ArrayList<StreamflowStation>();
    
    private StreamflowStation pickedPoint = new StreamflowStation();
    
    public void setPickedPoint(StreamflowStation point) {
    	this.pickedPoint = null;
        this.pickedPoint = point;
    }

    public List<StreamflowStation> getPoints() {
        return points;
    }
    
    public void setPoints(List<StreamflowStation> points) {
        if (points == null) {
            this.pickedPoint = null;
            this.points.clear();
        } else {
            this.points = points;
        }
    }

    public void addPoint(StreamflowStation point) {
        points.add(point);
    }

    protected StreamgaugeMapResource(StreamgaugeMapResourceData streamgaugeMapResourceData,
    		LoadProperties loadProperties) {
    	super(streamgaugeMapResourceData, loadProperties);
    	
        getCapability(EditableCapability.class).setEditable(true);

        this.streamgaugeMapResourceData = streamgaugeMapResourceData;
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
    	deleteStreamgaugeMapResource();
        try {
        	mapEditor = (AbstractEditor) EditorUtil.getActiveEditor();
        } catch (Exception ve) {
            System.out
                    .println("StreamgaugeMapResource Could not load initial editor: "
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
   public static StreamgaugeMapResource getOrCreateStreamgaugeMapResource() {
	   
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
                       mapRscData = new StreamgaugeMapResourceData();
                   mapRsc = mapRscData.construct(new LoadProperties(), desc);
                   
                   createMapMarkers();
                   
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
   
   private static void createMapMarkers() {
	   StaticPlotInfoPV spipv = StaticPlotInfoPV
               .readStaticPlotInfoPV("basemaps/streamgauge.spi");
	   
	   List<StreamflowStation> locs = new ArrayList<StreamflowStation>();
       for (Entry<String, SPIEntry> entry : spipv.getSpiList().entrySet()) {
    	   	   StreamflowStation loc = new StreamflowStation();
           Integer blockNumber = entry.getValue().blockNumber;
           loc.setStationId(blockNumber.toString());
           loc.setLat((float) entry.getValue().latlon.y);
           loc.setLon((float) entry.getValue().latlon.x);
           loc.setStationName(entry.getValue().accessId);
           locs.add(loc);
       }
       
       mapRsc.setPoints(locs);
   }
   
   public static void deleteStreamgaugeMapResource() {
	   System.out.println("StreamgaugeMapResource:deleteStreamgaugeMapResource ");
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
       return "Streamgauge Display";
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
           RGB color = new RGB (200,200,200);
           for (StreamflowStation p : points) {
               double lon, lat;
               lon = p.getLon();
               lat = p.getLat();
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
	   
	   getOrCreateStreamgaugeMapResource();
       
	   generateSymbolForDrawing();
       target.drawCircle(circles.toArray(new DrawableCircle[0]));
       
   }
   
   public boolean isProjectable(CoordinateReferenceSystem mapData) {
       return true;
   }
   
   @Override
   public void project(CoordinateReferenceSystem mapData) throws VizException {
       // System.out.println("StreamgaugeMapResource: project ");
   }
   
   private static StreamgaugeMapMouseHandler getMouseHandler() {
       if (mouseHandler == null) {
           mouseHandler = new StreamgaugeMapMouseHandler();
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
