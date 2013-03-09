package org.jboss.errai.idea.plugin;

import com.intellij.psi.PsiElement;

/**
* @author Mike Brock
*/
public class AnnotationValueElement {
  private final boolean isDefault;
  private final String value;
  private final PsiElement logicalElement;

  public AnnotationValueElement(boolean aDefault, String value, PsiElement logicalElement) {
    isDefault = aDefault;
    this.value = value;
    this.logicalElement = logicalElement;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public String getValue() {
    return value;
  }

  public PsiElement getLogicalElement() {
    return logicalElement;
  }
}
