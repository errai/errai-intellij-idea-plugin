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

package org.jboss.errai.idea.plugin.databinding.completion;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.BeanBindingMetaData;
import org.jboss.errai.idea.plugin.databinding.model.BoundMetaData;
import org.jboss.errai.idea.plugin.databinding.model.PropertyInfo;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jboss.errai.idea.plugin.util.DefaultPolicy;
import org.jboss.errai.idea.plugin.util.FakeNamedPsi;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Mike Brock
 */
public class BeanPropertyRenameProcessor extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    if (element instanceof PsiField) {
      final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
      if (topLevelClass != null) {
        if (Util.elementIsAnnotated(topLevelClass, Types.BINDABLE)) {
          return true;
        }
        if (DataBindUtil.getConfiguredBindableTypes(topLevelClass.getProject())
            .contains(topLevelClass.getQualifiedName())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void prepareRenaming(final PsiElement element,
                              final String replacementStr,
                              final Map<PsiElement, String> psiElementStringMap) {

    final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
    if (topLevelClass == null) {
      return;
    }

    for (TemplateMetaData metaData: TemplateUtil.getTemplateOwners(topLevelClass.getContainingFile())) {
      PsiClass psiClass = metaData.getTemplateClass();

      final BeanBindingMetaData dataBindMetaData = DataBindUtil.getTemplateBindingMetaData(psiClass);

      for (BoundMetaData md : DataBindUtil.getAllBoundMetaDataFromClass(psiClass)) {
        final PsiAnnotation boundAnnotation = md.getPsiAnnotation();
        final String property = Util.getAttributeValue(boundAnnotation, "property", DefaultPolicy.NULL);

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        PsiClass cls = dataBindMetaData.getBoundClass();
        for (String prop : property.split("\\.")) {
          if (!first) {
            sb.append('.');
          }
          first = false;

          final PropertyInfo beanPropertyInfo = DataBindUtil.getBeanPropertyInfo(cls, prop.trim());

          if (beanPropertyInfo == null) {
            return;
          }

          if (element.equals(beanPropertyInfo.getAssociatedField())) {
            sb.append(replacementStr);
          }
          else {
            sb.append(prop);
          }

          cls = beanPropertyInfo.getPropertyType();
        }

        final PsiAnnotationMemberValue property1 = Util.getAnnotationMemberValue(boundAnnotation, "property");

        FakeNamedPsi dummy = new FakeNamedPsi(property1) {
          @Override
          public PsiElement setName(@NotNull final String name) throws IncorrectOperationException {
            final PsiExpression expressionFromText = JavaPsiFacade.getInstance(element.getProject()).getElementFactory()
                .createExpressionFromText("\"" + sb.toString() + "\"", property1);
            boundAnnotation.setDeclaredAttributeValue("property", expressionFromText);

            return expressionFromText;
          }

          @NotNull
          @Override
          public String getName() {
            final String text = Util.getAnnotationMemberValue(boundAnnotation, "property").getText();
            return text.substring(1, text.length() - 1);
          }

          @Override
          public boolean isPhysical() {
            return false;
          }
        };

        psiElementStringMap.put(dummy, sb.toString());
      }
    }
  }
}
