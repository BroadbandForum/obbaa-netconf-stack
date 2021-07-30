package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Container(name = "albumimage", namespace = Jukebox.NAMESPACE)
public class AlbumImage extends AnnotationModelNode{
    public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "albumimage");
    public static final SchemaPath ALBUMIMAGE_SCHEMA_PATH = new SchemaPathBuilder().withParent(Album.ALBUM_SCHEMA_PATH)
            .appendQName(QNAME).build();

    public AlbumImage(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry, SubSystemRegistry subSystemRegistry, SchemaRegistry schemaRegistry) {
		super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
	}
	private static final long serialVersionUID = 1L;
    private String m_imageType="";
    private String m_imagePath="";
    private String m_imageId="";
    
    @ConfigAttribute(name = "imageid", namespace = Jukebox.NAMESPACE, isKey=true)
    public String getImageId() {
        return m_imageId;
    }
    public void setImageId(String imageId) {
        m_imageId = imageId;
    }
    @ConfigAttribute(name = "imagetype", namespace = Jukebox.NAMESPACE)
    public String getImageType() {
        return m_imageType;
    }
    public void setImageType(String imageType) {
        m_imageType = imageType;
    }
    @ConfigAttribute(name = "imagepath", namespace = Jukebox.NAMESPACE)
    public String getImagePath() {
        return m_imagePath;
    }
    public void setImagePath(String imagePath) {
        m_imagePath = imagePath;
    }
    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return ALBUMIMAGE_SCHEMA_PATH;
    }
    @Override
    public Set<ConfigLeafAttribute> getLeafList(QName qName) {
        return null;
    }

    @Override
    public Map<QName, LinkedHashSet<ConfigLeafAttribute>> getLeafLists() {
        return null;
    }
}
