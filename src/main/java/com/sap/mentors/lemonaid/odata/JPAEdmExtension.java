package com.sap.mentors.lemonaid.odata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

public class JPAEdmExtension implements org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension {

	public static final String MAPPING_MODEL = "odata-mapping.xml";
	public static final String SAP_NAMESPACE = "http://www.sap.com/Protocols/SAPData";
	public static final String SAP_PREFIX = "sap";
	public static final String LABEL = "label";
	
	@Override
	public void extendWithOperation(JPAEdmSchemaView view) {
	}

	@Override
	public void extendJPAEdmSchema(JPAEdmSchemaView view) {
		ResourceBundle i18n = ODataContextUtil.getResourceBundle("i18n");
		final Schema edmSchema = view.getEdmSchema();
		
		for (EntityType entityType : edmSchema.getEntityTypes()) {
			
			// Add language specific labels to the properties
			for (Property property : entityType.getProperties()) {
				String label = null;
				if (i18n != null) { try { label = i18n.getString(entityType.getName() + "." + property.getName()); } catch (Exception e) {} }
				List<AnnotationAttribute> annotationAttributeList = new ArrayList<AnnotationAttribute>();
				if (label != null) {
					annotationAttributeList.add(new AnnotationAttribute()
							.setNamespace(SAP_NAMESPACE)
							.setPrefix(SAP_PREFIX)
							.setName(LABEL).setText(label));
				}
				property.setAnnotationAttributes(annotationAttributeList); 
			}

			// Add transient properties
			if (entityType.getName().equals("Mentor")) {
				JPAEdmMappingImpl mapping = new JPAEdmMappingImpl();
				mapping.setJPAType(boolean.class);
				entityType.getProperties().add(new SimpleProperty().setName("MayEdit").setType(EdmSimpleTypeKind.Boolean).setMapping(mapping));
			}
			
			// Turn entity 'Attachments' into a media entity (with a stream) and hide the data property
			if (entityType.getName().equals("Attachment")) {
				entityType.setHasStream(true);
				for (int i = entityType.getProperties().size() - 1; i >= 0; i--) {
					Property property = entityType.getProperties().get(i);
					if (property.getName().equals("Data")) {
						entityType.getProperties().remove(property);
					}
				}
			}

		}
	}

	@Override
	public InputStream getJPAEdmMappingModelStream() {
		return JPAEdmExtension.class.getClassLoader().getResourceAsStream(MAPPING_MODEL);
	}

}