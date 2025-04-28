package edu.ucar.unidata.viz.aqi.renderable;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IRenderable;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

public class AQIGroupRenderable implements IRenderable {
    List<DrawableString> strings = new ArrayList<DrawableString>();
    List<AQIRenderable> renderables = new ArrayList<AQIRenderable>();
    
    boolean ready = false;
    ColorMapParameters colorParams;
    
    public void addRenderable(AQIRenderable renderable) {
        renderables.add(renderable);
    }
    
    public void setMapDescriptor(IMapDescriptor descriptor)  {
        for (AQIRenderable renderable: renderables) {
            renderable.setDescriptor(descriptor);
            strings.add(renderable.getDrawableString());
        }
        ready = true;
    }
    
    @Override
    public void paint(IGraphicsTarget target, PaintProperties paintProps) throws VizException {
        if (ready == true) {
            target.drawStrings(strings);
        }

    }

    public void reproject() {
        ready = false;
        strings.clear();
        for (AQIRenderable renderable : renderables) {
            renderable.reproject();
            strings.add(renderable.getDrawableString());
        }
        ready = true;
            
    }
    
    public void setColorMapParameter(ColorMapParameters params) {
        this.colorParams = params;
        for (AQIRenderable renderable : renderables) {
            renderable.setColorMapParameters(params);
        }
            
    }
}