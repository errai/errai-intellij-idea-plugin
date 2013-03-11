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

import com.intellij.psi.PsiClass;

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
