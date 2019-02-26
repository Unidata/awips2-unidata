package edu.ucar.unidata.uf.viz.streamgauge.ui;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

public class StreamgaugeMapResourceData extends AbstractResourceData {

    private Float markerSize = 1.5f;

    private Integer markerWidth = 2;

    private String mapName = "Profiler";

	public StreamgaugeMapResourceData() {
		super();
	}
	
	@Override
	public StreamgaugeMapResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        // TODO Auto-generated method stub
        return new StreamgaugeMapResource(this, loadProperties);
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.core.rsc.AbstractResourceData#update(java.lang.Object
     * )
     */
    @Override
    public void update(Object updateData) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StreamgaugeMapResourceData))
            return false;
        StreamgaugeMapResourceData rdata = (StreamgaugeMapResourceData) obj;
        if (this.markerSize.equals(rdata.getMarkerSize())
                && this.markerWidth.equals(rdata.getMarkerWidth()))
            return true;

        return false;
    }

    public Float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(Float markerSize) {
        this.markerSize = markerSize;
    }

    public Integer getMarkerWidth() {
        return markerWidth;
    }

    public void setMarkerWidth(Integer markerWidth) {
        this.markerWidth = markerWidth;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
}
