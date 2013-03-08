package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Mike Brock
 */
class BeanDataFieldReference extends PsiReferenceBase<PsiLiteralExpression> {

  public BeanDataFieldReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final Project project = getElement().getProject();
    final ConsolidateDataFieldElementResult consolidateElementResult
        = Util.getConsolidatedDataFields(getElement(), project).get(getValue());

    if (consolidateElementResult != null) {
      return consolidateElementResult.getLinkingElement();
    }
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final boolean hasSinkEvent
        = Util.fieldOrMethodIsAnnotated(getElement(), ErraiFrameworkSupport.SINKNATIVE_ANNOTATION_NAME);

    final Project project = getElement().getProject();
    final Map<String, ConsolidateDataFieldElementResult> dataFields
        = Util.getConsolidatedDataFields(getElement(), project);

    final ArrayList<Object> list = new ArrayList<Object>();
    for (Map.Entry<String, ConsolidateDataFieldElementResult> entry : dataFields.entrySet()) {
      LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(entry.getKey());

      if (entry.getValue().isDataFieldInClass()) {
        lookupElementBuilder = lookupElementBuilder.bold();
      }

      if (!hasSinkEvent && !entry.getValue().isDataFieldInClass()) {
        lookupElementBuilder = lookupElementBuilder.strikeout();
      }

      list.add(lookupElementBuilder.withTypeText(entry.getValue().getSourceName(), true));
    }

    return list.toArray();
  }
}
