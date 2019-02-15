package edu.ucar.unidata.common.dataplugin.usgs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURI;
import com.raytheon.uf.common.geospatial.adapter.GeometryAdapter;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

@Entity
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "usgs_seq")
@Table(name = "usgs", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "name", "refTime" }) })
@org.hibernate.annotations.Table(appliesTo = "usgs", indexes = { @Index(name = "usgs_refTimeIndex", columnNames = {
        "refTime" }) })
@DynamicSerialize
public class USGSRecord extends PluginDataObject {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "usgs";

    @Column
    @DynamicSerializeElement
    @DataURI(position = 1)
    String name;

    @Column
    @DynamicSerializeElement
    Float cfs;

    @Column
    @DynamicSerializeElement
    Float height;

    @Column(name = "location", columnDefinition = "geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Geometry geometry;

    public USGSRecord() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getCfs() {
        return cfs;
    }

    public void setCfs(Float cfs) {
        this.cfs = cfs;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
