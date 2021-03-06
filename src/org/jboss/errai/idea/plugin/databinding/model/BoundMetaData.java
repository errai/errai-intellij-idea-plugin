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

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static org.jboss.errai.idea.plugin.databinding.DataBindUtil.getConvertibilityMetaData;
import static org.jboss.errai.idea.plugin.databinding.DataBindUtil.typeIsBindableToWidget;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
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
  private final BeanBindingMetaData beanBindingMetaData;
  private final PsiElement owner;
  private final PsiAnnotation psiAnnotation;
  private final String property;
  private PsiClass propertyType;


  public BoundMetaData(PsiElement owner) {
    this.beanBindingMetaData = DataBindUtil.getDataBindingMetaData(owner);
    this.owner = owner;
    this.psiAnnotation = Util.getAnnotationFromElement(owner, Types.BOUND);

    if (psiAnnotation != null) {
      property = Util.getAttributeValue(psiAnnotation, "property", DefaultPolicy.OWNER_IDENTIFIER_NAME);
    }
    else {
      property = null;
    }
  }

  public PsiClass getPropertyType() {
    return propertyType;
  }

  public void setPropertyType(PsiClass propertyType) {
    this.propertyType = propertyType;
  }


  public BeanBindingMetaData getBindingMetaData() {
    return beanBindingMetaData;
  }

  public PsiAnnotation getPsiAnnotation() {
    return psiAnnotation;
  }

  public PropertyValidation validateProperty() {
    final PsiClass boundClass = getBindingMetaData().getBoundClass();

    if (property != null && boundClass != null && boundClass.getParent() != null) {
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
        final ConvertibilityMetaData convertibilityMetaData = getConvertibilityMetaData(cls, psiAnnotation);
        if (convertibilityMetaData.isConverterInputInvalid()) {
          validation.setConverterInputInvalid(true);
          propertyType = cls;
          return validation;
        }

        validation.setBindabilityValidation(typeIsBindableToWidget(cls, widgetType, convertibilityMetaData));
      }

      validation.setBoundType(cls);

      return validation;
    }
    else {
      return new PropertyValidation(false);
    }
  }

  public boolean isModelApplicable(PsiClass modelClass) {
    final PsiClass boundClass = getBindingMetaData().getBoundClass();
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(modelClass.getProject());

    return boundClass != null && _isModelApplicable(boundClass, modelClass, facade);
  }

  private static boolean _isModelApplicable(PsiClass toCheck, PsiClass modelClass, JavaPsiFacade facade) {
    if (toCheck == null) {
      return false;
    }

    final String qualifiedName = toCheck.getQualifiedName();

    if (qualifiedName == null) {
      return false;
    }

    if (qualifiedName.equals(modelClass.getQualifiedName())) {
      return true;
    }

    for (PsiField field : toCheck.getAllFields()) {
      if (field.getType().getCanonicalText().equals(modelClass.getQualifiedName())) {
        return true;
      }

      final PsiClass psiClass = facade.findClass(field.getType().getCanonicalText(), allScope(facade.getProject()));
      if (_isModelApplicable(psiClass, modelClass, facade)) {
        return true;
      }
    }
    return false;
  }
}
