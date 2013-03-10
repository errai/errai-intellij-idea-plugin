package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiClass;
import org.jboss.errai.idea.plugin.databinding.BindabilityValidation;

/**
* @author Mike Brock
*/
public class PropertyValidation {
  private BindabilityValidation bindabilityValidation;
  private String unresolvedPropertyElement;
  private PsiClass unresolvedParent;
  private PsiClass boundType;
  private boolean parentBindable;

  public PropertyValidation() {
  }

  public PropertyValidation(boolean parentBindable) {
    this.parentBindable = parentBindable;
  }

  public boolean hasBindabilityProblem() {
    return bindabilityValidation != null && !bindabilityValidation.isValid();
  }

  public BindabilityValidation getBindabilityValidation() {
    return bindabilityValidation;
  }

  public boolean isValid() {
    return unresolvedPropertyElement == null && unresolvedParent == null && parentBindable && (bindabilityValidation != null && bindabilityValidation.isValid());
  }

  public String getUnresolvedPropertyElement() {
    return unresolvedPropertyElement;
  }

  public PsiClass getUnresolvedParent() {
    return unresolvedParent;
  }

  public String getParentName() {
    if (unresolvedParent != null) {
      return unresolvedParent.getQualifiedName();
    }
    else {
      return "<unknown>";
    }
  }

  public PsiClass getBoundType() {
    return boundType;
  }

  public boolean isParentBindable() {
    return parentBindable;
  }

  public void setBindabilityValidation(BindabilityValidation bindabilityValidation) {
    this.bindabilityValidation = bindabilityValidation;
  }

  public void setUnresolvedPropertyElement(String unresolvedPropertyElement) {
    this.unresolvedPropertyElement = unresolvedPropertyElement;
  }

  public void setUnresolvedParent(PsiClass unresolvedParent) {
    this.unresolvedParent = unresolvedParent;
  }

  public void setBoundType(PsiClass boundType) {
    this.boundType = boundType;
  }

  public void setParentBindable(boolean parentBindable) {
    this.parentBindable = parentBindable;
  }
}
