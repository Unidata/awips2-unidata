package edu.ucar.unidata.uf.viz.streamflow.ui;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

public class StreamflowMapResourceData extends AbstractResourceData {

    private Float markerSize = 1.5f;

    private Integer markerWidth = 2;

    private String mapName = "Profiler";

	public StreamflowMapResourceData() {
		super();
	}
	
	@Override
	public StreamflowMapResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        // TODO Auto-generated method stub
        return new StreamflowMapResource(this, loadProperties);
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
        if (obj == null || !(obj instanceof StreamflowMapResourceData))
            return false;
        StreamflowMapResourceData rdata = (StreamflowMapResourceData) obj;
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
