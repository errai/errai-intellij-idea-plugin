package org.jboss.errai.idea.plugin.ui.model;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameValuePair;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;

/**
 * @author Mike Brock
 */
public class ClassDataField {
  private final PsiElement psiElement;
  private final String dataFieldName;

  private final PsiAnnotation boundAnnotation;

  public ClassDataField(String dataFieldName, PsiElement psiElement) {
    this.dataFieldName = dataFieldName;
    this.psiElement = psiElement;

    if (psiElement != null) {
      boundAnnotation = Util.getAnnotationFromElement(psiElement, Types.BOUND);
    }
    else {
      boundAnnotation = null;
    }
  }

  public PsiElement getPsiElement() {
    return psiElement;
  }

  public String getDataFieldName() {
    return dataFieldName;
  }

  public boolean isBoundElement() {
    return boundAnnotation != null;
  }

  public String getBindableProperty() {
    if (isBoundElement()) {
      final PsiNameValuePair[] attributes = boundAnnotation.getParameterList().getAttributes();
      for (PsiNameValuePair attribute : attributes) {
        if ("property".equals(attribute.getName())) {
          final PsiAnnotationMemberValue value = attribute.getValue();
          if (value != null) {
            final String text = value.getText();
            return text.substring(1, text.length() - 1);
          }
          return null;
        }
      }
    }
    return null;
  }

  public String getBindableConverter() {
    if (isBoundElement()) {
      final PsiNameValuePair[] attributes = boundAnnotation.getParameterList().getAttributes();
      for (PsiNameValuePair attribute : attributes) {
        if ("converter".equals(attribute.getName())) {
          final PsiAnnotationMemberValue value = attribute.getValue();
          if (value != null) {
            final String text = value.getText();
            return text.substring(1, text.length() - 1);
          }
          return null;
        }
      }
    }
    return null;
  }

}
