package edu.ucar.unidata.viz.aqi.renderable;

import org.eclipse.swt.graphics.RGB;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.drawables.IRenderable;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;

public class AQIRenderable  {

    private  IMapDescriptor  descriptor = null;
    private AQIRecord record =null;
    DrawableString string  =null;
    ColorMapParameters params;
    
    private double[] recordLocation;
    private RGB color = new RGB(126,126,126);
    private HorizontalAlignment horizontalTextAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalTextAlignment = VerticalAlignment.MIDDLE;

    public AQIRenderable() {
        super();
    }
    
    public void setRecord(AQIRecord record) {
        this.record = record;
        string  = new DrawableString(String.valueOf(record.getAqi()));
        string.basics.color = color;
        calculateLocation();
    }
    
    public void setDescriptor(IMapDescriptor descriptor) {
        this.descriptor = descriptor;
        calculateLocation();
    }
    
    private void calculateLocation() {
        if (record != null && descriptor != null) {
            Coordinate coord = record.getLocation().getCoordinate();
            recordLocation = descriptor.worldToPixel(new double[] {coord.x,coord.y});
            string.setCoordinates(recordLocation[0], recordLocation[1]);
        }
    }

    public DrawableString getDrawableString(){
        return string;
    }

    public void reproject() {
        this.calculateLocation();
    }

    public void setColorMapParameters(ColorMapParameters params) {
        this.params = params;
        Color colorValue = params.getColorByValue((float)record.getAqi());
            
        color = new RGB((int)(colorValue.getRed()*255.),(int)(colorValue.getGreen()*255.),(int)(colorValue.getBlue()*255.));
        string.setText(String.valueOf(record.getAqi()), color);
    }
    
}