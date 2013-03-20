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

package org.jboss.errai.idea.plugin;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.completion.TemplateEventHandlerReference;
import org.jboss.errai.idea.plugin.ui.completion.XmlDatafieldReference;
import org.jboss.errai.idea.plugin.util.AnnotationMatchingPattern;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.XmlAttributeMatchingPattern;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ErraiFrameworkSupport implements ApplicationComponent {

  private final ReferenceProvidersRegistry registry;

  public ErraiFrameworkSupport(ReferenceProvidersRegistry registry) {
    this.registry = registry;
  }

  public void initComponent() {
    final PsiReferenceRegistrar javaRegistrar = registry.getRegistrar(Language.findInstance(JavaLanguage.class));
    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.EVENTHANDLER),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new TemplateEventHandlerReference[]{new TemplateEventHandlerReference((PsiLiteralExpression) element, false)};
          }
        });

    final PsiReferenceRegistrar xmlRegistrar = registry.getRegistrar(Language.findInstance(HTMLLanguage.class));

    xmlRegistrar.registerReferenceProvider(new XmlAttributeMatchingPattern(TemplateUtil.DATA_FIELD_TAG_ATTRIBUTE),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new XmlDatafieldReference[]{new XmlDatafieldReference((XmlAttribute) element, false)};
          }
        });
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "ErraiFrameworkTools";
  }
}
