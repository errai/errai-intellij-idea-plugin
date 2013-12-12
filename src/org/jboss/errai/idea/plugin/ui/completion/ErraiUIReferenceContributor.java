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

import static org.jboss.errai.idea.plugin.util.Types.DATAFIELD;
import static org.jboss.errai.idea.plugin.util.Types.TEMPLATED;

import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.util.AnnotationMatchingPattern;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.XmlAttributeMatchingPattern;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ErraiUIReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(TEMPLATED), new TemplatedReferenceProvider());
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(DATAFIELD), new DataFieldReferenceProvider());

    registrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.EVENTHANDLER),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new TemplateEventHandlerReference[]{new TemplateEventHandlerReference((PsiLiteralExpression) element, false)};
          }
        });

    registrar.registerReferenceProvider(new XmlAttributeMatchingPattern(TemplateUtil.DATA_FIELD_TAG_ATTRIBUTE),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new XmlDatafieldReference[]{new XmlDatafieldReference((XmlAttribute) element, false)};
          }
        });
    registrar.registerReferenceProvider(new XmlAttributeMatchingPattern(TemplateUtil.ID_ATTRIBUTE),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new XmlDatafieldReference[]{(new XmlDatafieldReference((XmlAttribute) element, false))};
          }
        });
  }
}
