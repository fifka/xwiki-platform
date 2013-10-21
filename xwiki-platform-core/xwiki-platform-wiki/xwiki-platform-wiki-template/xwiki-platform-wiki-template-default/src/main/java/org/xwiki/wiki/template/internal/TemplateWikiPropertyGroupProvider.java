/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.wiki.template.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.internal.descriptor.document.WikiDescriptorDocumentHelper;
import org.xwiki.wiki.manager.WikiManagerException;
import org.xwiki.wiki.properties.WikiPropertyGroup;
import org.xwiki.wiki.properties.WikiPropertyGroupException;
import org.xwiki.wiki.properties.WikiPropertyGroupProvider;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Named(TemplateWikiPropertyGroupProvider.GROUP_NAME)
@Singleton
public class TemplateWikiPropertyGroupProvider implements WikiPropertyGroupProvider
{
    public static final String GROUP_NAME = "template";

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private WikiDescriptorDocumentHelper wikiDescriptorDocumentHelper;

    @Override
    public WikiPropertyGroup get(String wikiId) throws WikiPropertyGroupException
    {
        XWikiContext context = xcontextProvider.get();
        XWiki xwiki = context.getWiki();

        TemplateWikiPropertyGroup group = new TemplateWikiPropertyGroup(GROUP_NAME);

        try {
            XWikiDocument descriptorDocument = wikiDescriptorDocumentHelper.getDocumentFromWikiId(wikiId);
            BaseObject object = descriptorDocument.getXObject(XWikiServerClassDocumentInitializer.SERVER_CLASS);
            group.setTemplate(object.getIntValue(XWikiServerClassDocumentInitializer.FIELD_ISWIKITEMPLATE) != 0);
        } catch (WikiManagerException e) {
            throw new WikiPropertyGroupException(String.format("Unable to load descriptor document for wiki %s.",
                    wikiId), e);
        }

        return group;
    }

    @Override
    public void save(WikiPropertyGroup group, String wikiId) throws WikiPropertyGroupException
    {
        XWikiContext context = xcontextProvider.get();
        XWiki xwiki = context.getWiki();

        TemplateWikiPropertyGroup templateGroup = (TemplateWikiPropertyGroup) group;

        try {
            XWikiDocument descriptorDocument = wikiDescriptorDocumentHelper.getDocumentFromWikiId(wikiId);
            BaseObject object = descriptorDocument.getXObject(XWikiServerClassDocumentInitializer.SERVER_CLASS);
            object.setIntValue(XWikiServerClassDocumentInitializer.FIELD_ISWIKITEMPLATE,
                    templateGroup.isTemplate() ? 1 : 0);
            xwiki.saveDocument(descriptorDocument, String.format("Changed property group [%s].", GROUP_NAME), context);
        } catch (WikiManagerException e) {
            throw new WikiPropertyGroupException(String.format("Unable to load descriptor document for wiki %s.",
                    wikiId), e);
        } catch (XWikiException e) {
            throw new WikiPropertyGroupException("Unable to save descriptor document.", e);
        }
    }

}
