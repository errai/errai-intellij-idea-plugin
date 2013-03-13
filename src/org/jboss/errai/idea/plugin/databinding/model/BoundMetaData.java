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

package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiVariable;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.util.DefaultPolicy;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;

import java.util.Set;

/**
* @author Mike Brock
*/
public class BoundMetaData {
  private final TemplateBindingMetaData templateBindingMetaData;
  private final PsiElement owner;
  private final PsiAnnotation psiAnnotation;
  private final String property;

  public BoundMetaData(PsiElement owner) {
    this.templateBindingMetaData = DataBindUtil.getTemplateBindingMetaData(owner);
    this.owner = owner;
    this.psiAnnotation = Util.getAnnotationFromElement(owner, Types.BOUND);

    if (psiAnnotation != null) {
      property = Util.getAttributeValue(psiAnnotation, "property", DefaultPolicy.OWNER_IDENTIFIER_NAME);
   //   bindableConverter = Util.getAttributeValue(psiAnnotation, "converter", DefaultPolicy.NULL);
    }
    else {
      property = null;
     // bindableConverter = null;
    }
  }

  public TemplateBindingMetaData getBindingMetaData() {
    return templateBindingMetaData;
  }

  public PsiAnnotation getPsiAnnotation() {
    return psiAnnotation;
  }

  public PropertyValidation validateProperty() {
    final PsiClass boundClass = getBindingMetaData().getBoundClass();

    if (property != null && boundClass != null) {
      final Set<String> bindableTypes = DataBindUtil.getConfiguredBindableTypes(boundClass.getProject());

      PsiClass cls = boundClass;
      for (String token : property.split("\\.")) {
        if (!bindableTypes.contains(cls.getQualifiedName()) && !Util.elementIsAnnotated(cls, Types.BINDABLE)) {
          PropertyValidation validation = new PropertyValidation();
          validation.setParentBindable(false);
          validation.setUnresolvedParent(cls);
          validation.setUnresolvedPropertyElement(token);
          return validation;
        }
        PsiClass result = DataBindUtil.getBeanPropertyType(cls, token.trim());
        if (result == null) {
          PropertyValidation validation = new PropertyValidation();
          validation.setParentBindable(true);
          validation.setUnresolvedParent(cls);
          validation.setUnresolvedPropertyElement(token);
          return validation;
        }
        cls = result;
      }

      final PropertyValidation validation = new PropertyValidation(true);

      PsiVariable variable = (PsiVariable) owner;
      final PsiClass widgetType = DataBindUtil.getPsiClassFromType(owner.getProject(), variable.getType());
      if (widgetType != null) {
        final ConvertibilityMetaData convertibilityMetaData = DataBindUtil.getConvertibilityMetaData(psiAnnotation);

        validation.setBindabilityValidation(DataBindUtil.typeIsBindableToWidget(cls, widgetType, convertibilityMetaData));
      }

      validation.setBoundType(cls);

      return validation;
    }
    else {
      return new PropertyValidation(false);
    }
  }
}
