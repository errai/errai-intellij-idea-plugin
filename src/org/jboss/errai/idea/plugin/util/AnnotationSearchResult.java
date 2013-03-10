package org.jboss.errai.idea.plugin.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;

/**
* @author Mike Brock
*/
public class AnnotationSearchResult {
  private final PsiAnnotation annotation;
  private final PsiElement owningElement;

  public AnnotationSearchResult(PsiAnnotation annotation, PsiElement owningElement) {
    this.annotation = annotation;
    this.owningElement = owningElement;
  }

  public PsiAnnotation getAnnotation() {
    return annotation;
  }

  public PsiElement getOwningElement() {
    return owningElement;
  }
}
