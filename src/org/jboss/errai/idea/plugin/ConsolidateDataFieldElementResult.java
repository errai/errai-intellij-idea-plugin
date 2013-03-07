package org.jboss.errai.idea.plugin;

import com.intellij.psi.PsiElement;

/**
 * @author Mike Brock
 */
public class ConsolidateDataFieldElementResult {
  private final String name;
  private final String sourceName;
  private final PsiElement element;
  private final boolean dataFieldInClass;

  public ConsolidateDataFieldElementResult(String name, String sourceName, PsiElement element, boolean dataFieldInClass) {
    this.name = name;
    this.sourceName = sourceName;
    this.element = element;
    this.dataFieldInClass = dataFieldInClass;
  }

  public String getName() {
    return name;
  }

  public String getSourceName() {
    return sourceName;
  }

  public PsiElement getElement() {
    return element;
  }

  public boolean isDataFieldInClass() {
    return dataFieldInClass;
  }
}
