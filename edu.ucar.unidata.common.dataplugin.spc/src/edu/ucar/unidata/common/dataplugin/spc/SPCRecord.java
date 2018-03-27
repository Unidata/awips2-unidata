package edu.ucar.unidata.common.dataplugin.spc;

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
@SequenceGenerator(initialValue = 1, name = PluginDataObject.ID_GEN, sequenceName = "spc_seq")
@Table(name = "spc", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "name", "refTime" }) })
@org.hibernate.annotations.Table(appliesTo = "spc", indexes = { @Index(name = "spc_refTimeIndex", columnNames = {
        "refTime", "forecastTime" }) })
@DynamicSerialize

public class SPCRecord extends PluginDataObject {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_NAME = "spc";

    @Column
    @DynamicSerializeElement
    @DataURI(position = 1)
    String name;

    @Column(name = "location", columnDefinition = "geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    @XmlJavaTypeAdapter(value = GeometryAdapter.class)
    @DynamicSerializeElement
    private Geometry geometry;

    public SPCRecord() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
