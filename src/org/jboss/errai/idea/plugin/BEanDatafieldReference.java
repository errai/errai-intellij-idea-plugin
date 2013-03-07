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
class BeanDatafieldReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final Project project;

  public BeanDatafieldReference(Project project, PsiLiteralExpression element, boolean soft) {
    super(element, soft);
    this.project = project;
  }
//
//  private Map<String, ConsolidateDataFieldElementResult> getDataFields() {
//
//    final Util.TemplateMetaData metaData = Util.getTemplateMetaData(getElement(), project);
//    final String beanClass = PsiUtil.getTopLevelClass(getElement()).getQualifiedName();
//
//    final Map<String, ConsolidateDataFieldElementResult> results = new LinkedHashMap<String, ConsolidateDataFieldElementResult>();
//
//    final Collection<Util.AnnotationSearchResult> allInjectionPoints
//        = Util.findAllAnnotatedElements(getElement(), ErraiUISupport.DATAFIELD_ANNOTATION_NAME);
//
//    for (Util.AnnotationSearchResult r : allInjectionPoints) {
//      final String value = Util.getValueStringFromAnnotationWithDefault(r.getAnnotation()).getValue();
//      results.put(value, new ConsolidateDataFieldElementResult(value, beanClass, r.getOwningElement(), true));
//    }
//
//    final Map<String, Util.DataFieldReference> allDataFieldTags = Util.findAllDataFieldTags(metaData, project, false);
//    for (Util.DataFieldReference ref : allDataFieldTags.values()) {
//      if (results.containsKey(ref.getDataFieldName())) continue;
//
//      results.put(ref.getDataFieldName(), new ConsolidateDataFieldElementResult(ref.getDataFieldName(),
//          metaData.getTemplateReference().getFileName(), ref.getTag(), false));
//    }
//
//    return results;
//  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final ConsolidateDataFieldElementResult consolidateElementResult
        = Util.getConsolidatedDataFields(getElement(), project).get(getValue());

    if (consolidateElementResult != null) {
      return consolidateElementResult.getElement();
    }
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final boolean hasSinkEvent = Util.fieldOrMethodIsAnnotated(getElement(), ErraiUISupport.SINKNATIVE_ANNOTATION_NAME);
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
