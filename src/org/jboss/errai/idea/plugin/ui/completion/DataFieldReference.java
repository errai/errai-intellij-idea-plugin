/*
 * Copyright 2013 Red Hat, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class DataFieldReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final TextRange range;
  private final boolean considerRoot;

  public DataFieldReference(boolean considerRoot, PsiLiteralExpression element, TextRange range) {
    super(element, false);
    this.range = range;
    this.considerRoot = considerRoot;
  }


  private Map<String, PsiElement> getAvailableDataFields() {
    Map<String, PsiElement> map = new HashMap<String, PsiElement>();
    final TemplateMetaData metaData = TemplateUtil.getTemplateMetaData(getElement());
    if (metaData != null) {
      final String rootNode = metaData.getTemplateExpression().getRootNode();

      for (Map.Entry<String, TemplateDataField> entry : metaData.getAllDataFieldsInTemplate(considerRoot).entrySet()) {
        if (!considerRoot && rootNode.equals(entry.getKey())) continue;

        final XmlAttribute attribute = entry.getValue().getTag().getAttribute(TemplateUtil.DATA_FIELD_TAG_ATTRIBUTE);

        if (attribute != null) {
          map.put(entry.getKey(), attribute.getValueElement());
        }
      }
    }
    return map;
  }


  @Nullable
  @Override
  public PsiElement resolve() {
    return getAvailableDataFields().get(getValue());
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return element.equals(resolve());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final ArrayList<Object> list = new ArrayList<Object>();
    for (final String value : getAvailableDataFields().keySet()) {

      list.add(LookupElementBuilder.create(value));
    }
    return list.toArray();
  }

  @Override
  public TextRange getRangeInElement() {
    return range;
  }
}
