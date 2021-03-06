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

import static org.jboss.errai.idea.plugin.util.Util.getErasedCanonicalText;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;

/**
 * @author Mike Brock
 */
public class PropertyInfo {
  private String propertyName;
  private PsiClass propertyType;
  private PsiElement getterElement;
  private PsiElement setterElement;

  public PsiElement getAccessorElement() {
    if (getterElement != null) {
      return getterElement;
    }
    else {
      return setterElement;
    }
  }

  public PsiField getAssociatedField() {
    PsiClass type = PsiUtil.getTopLevelClass(getAccessorElement());
    if (type == null) {
      return null;
    }

    for (PsiField psiField : type.getAllFields()) {
      final PsiClass effectiveFieldType = DataBindUtil.getPsiClassFromType(psiField.getProject(), psiField.getType());
      if (psiField.getName().equals(propertyName)
          && getErasedCanonicalText(effectiveFieldType.getQualifiedName()).equals(propertyType.getQualifiedName())) {

        return psiField;
      }
    }
    return null;
  }

  public PsiClass getPropertyType() {
    return propertyType;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public void setPropertyType(PsiClass propertyType) {
    this.propertyType = propertyType;
  }

  public void setGetterElement(PsiElement getterElement) {
    this.getterElement = getterElement;
  }

  public void setSetterElement(PsiElement setterElement) {
    this.setterElement = setterElement;
  }
}
