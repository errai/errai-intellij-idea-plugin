package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
* @author Mike Brock
*/
class TemplateDatafieldReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final Project project;

  public TemplateDatafieldReference(Project project, PsiLiteralExpression element, boolean soft) {
    super(element, soft);
    this.project = project;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final Util.TemplateMetaData templateFile = Util.getTemplateMetaData(getElement(), project);
    if (templateFile != null) {
      final ArrayList<Object> list = new ArrayList<Object>();
      final String rootNode = templateFile.getTemplateReference().getRootNode();
      for (final String value : Util.findAllDataFieldTags(templateFile, project, false).keySet()) {
        if (rootNode.equals(value)) continue;

        list.add(LookupElementBuilder.create(value));
      }
      return list.toArray();
    }
    else {
      return new Object[0];
    }
  }
}
