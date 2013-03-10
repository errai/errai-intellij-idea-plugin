package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.util.Util;

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

    for (PsiField psiField : type.getAllFields()) {
      if (psiField.getName().equals(propertyName)
          && Util.getErasedCanonicalText(psiField.getType().getCanonicalText()).equals(propertyType.getQualifiedName())) {
        return psiField;
      }
    }
    return null;
  }

  public boolean isHasGetter() {
    return getterElement != null;
  }

  public boolean isHasSetter() {
    return setterElement != null;
  }

  public String getPropertyName() {
    return propertyName;
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
