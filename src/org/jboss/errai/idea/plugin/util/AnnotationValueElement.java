package org.jboss.errai.idea.plugin.util;

import com.intellij.psi.PsiElement;

/**
* @author Mike Brock
*/
public class AnnotationValueElement {
  private final String value;
  private final PsiElement logicalElement;

  public AnnotationValueElement(String value, PsiElement logicalElement) {
    this.value = value;
    this.logicalElement = logicalElement;
  }


  public String getValue() {
    return value;
  }

  public PsiElement getLogicalElement() {
    return logicalElement;
  }
}
