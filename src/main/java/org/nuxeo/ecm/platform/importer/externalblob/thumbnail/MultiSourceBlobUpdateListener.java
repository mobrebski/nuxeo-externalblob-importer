package org.nuxeo.ecm.platform.importer.externalblob.thumbnail;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.BEFORE_DOC_UPDATE;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.thumbnail.ThumbnailConstants;
import org.nuxeo.ecm.platform.thumbnail.listener.CheckBlobUpdateListener;
import org.nuxeo.runtime.api.Framework;

public class MultiSourceBlobUpdateListener extends CheckBlobUpdateListener {

    @Override
    public void handleEvent(Event event) throws ClientException {
        EventContext ec = event.getContext();
        if (!(ec instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext context = (DocumentEventContext) ec;
        DocumentModel doc = context.getSourceDocument();
        if (!doc.hasSchema("file") && !doc.hasFacet("externalfile")) {
            return;
        }

        //Check ExternalFile
        Property content;
        if (doc.hasFacet("externalfile")){
            content = doc.getProperty("externalfile:content");
        } else {
            content = doc.getProperty("file:content");
        }

        if (DOCUMENT_CREATED.equals(event.getName()) || content.isDirty()) {

            if (BEFORE_DOC_UPDATE.equals(event.getName()) && doc.hasFacet(ThumbnailConstants.THUMBNAIL_FACET)) {
                doc.setPropertyValue(ThumbnailConstants.THUMBNAIL_PROPERTY_NAME, null);
            }

            if (content.getValue() != null) {
                doc.addFacet(ThumbnailConstants.THUMBNAIL_FACET);
                Framework.getLocalService(EventService.class).fireEvent(
                        ThumbnailConstants.EventNames.scheduleThumbnailUpdate.name(), context);
            }
        }
    }

}
