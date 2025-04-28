package edu.ucar.unidata.viz.aqi.rsc;
import java.util.ArrayList;
import java.util.List;
import tech.units.indriya.AbstractUnit;

import org.eclipse.swt.graphics.RGB;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.colormap.prefs.DataMappingPreferences;
import com.raytheon.uf.common.colormap.prefs.DataMappingPreferences.DataMappingEntry;
import com.raytheon.uf.common.colormap.ColorMapException;

import com.raytheon.uf.common.colormap.ColorMapLoader;


import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.DrawableCircle;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;
import edu.ucar.unidata.viz.aqi.renderable.AQIGroupRenderable;
import edu.ucar.unidata.viz.aqi.renderable.AQIRenderable;

public class AQIResource extends AbstractVizResource<AQIResourceData, IMapDescriptor> {
	AQIGroupRenderable renderable = new AQIGroupRenderable();
	
	protected AQIResource(AQIResourceData resourceData, LoadProperties loadProperties) {
		super(resourceData, loadProperties);
	}

	@Override
	public String getName() {
		return "Air Quality Index";
	}

	@Override
	protected void disposeInternal() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void paintInternal(IGraphicsTarget target, PaintProperties paintProps) throws VizException {
		renderable.paint(target, paintProps);
	}

	@Override
	protected void initInternal(IGraphicsTarget target) throws VizException {
		renderable.setMapDescriptor(descriptor);
	    ColorMapParameters params = new ColorMapParameters();

	    try {
	         params.setColorMap(ColorMapLoader.loadColorMap("aqi"
	                   + IPathManager.SEPARATOR + "DefaultColorMap"));
	    } catch (ColorMapException e) {
	         throw new VizException(e);
	    }
	    DataMappingPreferences preferences = new DataMappingPreferences();

        DataMappingEntry entry = new DataMappingEntry();
        entry.setDisplayValue(0.0);
        entry.setPixelValue(0.0);
        entry.setSample("Good");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(50.0);
        entry.setPixelValue(1.0);
        entry.setSample("Moderate");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(100.0);
        entry.setPixelValue(2.0);
        entry.setSample("Unhealthy (SG)");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(150.0);
        entry.setPixelValue(3.0);
        entry.setSample("Unhealthy");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(200.0);
        entry.setPixelValue(4.0);
        entry.setSample("Very Unhealthy");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(300.0);
        entry.setPixelValue(5.0);
        entry.setSample("Hazardous");
        preferences.addEntry(entry);

        entry = new DataMappingEntry();
        entry.setDisplayValue(400.0);
        entry.setPixelValue(6.0);
        preferences.addEntry(entry);
        
        params.setDisplayUnit(AbstractUnit.ONE);
        params.setDataMapping(preferences);
        params.setColorMapMin(0);
        params.setColorMapMax(6);
        getCapability(ColorMapCapability.class).setColorMapParameters(params);
        
        renderable.setColorMapParameter(params);
	}

	public void addRecord(AQIRecord rec) {
		AQIRenderable renderable1 = new AQIRenderable();
		renderable1.setRecord(rec);
		renderable.addRenderable(renderable1);
		
	}

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        // TODO Auto-generated method stub
        super.project(crs);
        renderable.reproject();
    }
}