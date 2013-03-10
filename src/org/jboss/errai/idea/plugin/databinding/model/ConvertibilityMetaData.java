package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiClass;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class ConvertibilityMetaData {
  private final Map<PsiClass, PsiClass> conversionRules = new HashMap<PsiClass, PsiClass>();

  public ConvertibilityMetaData() {
  }

  public ConvertibilityMetaData(PsiClass from, PsiClass to) {
    addConversionRule(from, to);
  }

  public void addConversionRule(PsiClass from, PsiClass to) {
    conversionRules.put(from, to);
  }

  public boolean canConvert(PsiClass from, PsiClass to) {
    return conversionRules.containsKey(from) && conversionRules.get(from).equals(to);
  }
}
