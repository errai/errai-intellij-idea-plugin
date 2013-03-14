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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jboss.errai.idea.plugin.ui.model.ConsolidateDataFieldElementResult;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TemplateEventHandlerReference extends PsiReferenceBase<PsiLiteralExpression> {
  public TemplateEventHandlerReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final Project project = getElement().getProject();
    final ConsolidateDataFieldElementResult consolidateElementResult
        = TemplateUtil.getConsolidatedDataFields(getElement(), project).get(getValue());

    if (consolidateElementResult != null) {
      return consolidateElementResult.getLinkingElement();
    }
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final boolean hasSinkEvent
        = Util.fieldOrMethodIsAnnotated(getElement(), Types.SINKNATIVE_ANNOTATION_NAME);

    final Project project = getElement().getProject();

    final ArrayList<Object> list = new ArrayList<Object>();
    for (Map.Entry<String, ConsolidateDataFieldElementResult> entry
        : TemplateUtil.getConsolidatedDataFields(getElement(), project).entrySet()) {

      LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(entry.getKey());

      if (entry.getValue().isDataFieldInClass()) {
        lookupElementBuilder = lookupElementBuilder.bold();
      }

      if (!hasSinkEvent && !entry.getValue().isDataFieldInClass()) {
        lookupElementBuilder = lookupElementBuilder.strikeout();
      }

      list.add(lookupElementBuilder.withTypeText(entry.getValue().getSourceName(), true).withIcon(AllIcons.Xml.Html_id));
    }

    return list.toArray();
  }
}
