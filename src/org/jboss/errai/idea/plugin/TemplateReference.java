package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* @author Mike Brock
*/
class TemplateReference extends PsiReferenceBase<PsiLiteralExpression> {

  public TemplateReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);

   //todo: replace with: getElement().getProject()
  }

  private PsiDirectory getBaseDir() {
    final PsiClass psiClass = PsiUtil.getTopLevelClass(getElement());

    if (psiClass != null) {
      final PsiFile containingFile = psiClass.getContainingFile();

      if (containingFile != null) {
        return containingFile.getOriginalFile().getParent();
      }
    }
    return null;
  }

  public Map<String, PsiElement> getTemplateCompletions() {
    Map<String, PsiElement> completions = new LinkedHashMap<String, PsiElement>();

    final Project project = getElement().getProject();
    final Util.TemplateMetaData templateMetaData = Util.getTemplateMetaData(getElement(), project);
    final PsiDirectory baseDir = getBaseDir();

    final Collection<Util.DataFieldReference> allDataFieldTags;
    final VirtualFile templateFile = templateMetaData.getTemplateFile();
    if (templateFile != null) {
      allDataFieldTags = Util.findAllDataFieldTags(templateMetaData, project, true).values();
    }
    else {
      allDataFieldTags = Collections.emptyList();
    }

    if (allDataFieldTags.isEmpty()) {
      for (final PsiElement element : baseDir.getChildren()) {
        if (element.getContainingFile().getName().endsWith(".html")) {
          completions.put(element.getContainingFile().getName(), element);
        }
      }
    }
    else if (templateFile != null) {
      completions.put(templateMetaData.getTemplateReference().getFileName(),
          PsiManager.getInstance(project).findFile(templateFile));

      for (Util.DataFieldReference dataFieldReference : allDataFieldTags) {
        completions.put(templateMetaData.getTemplateReference().getFileName() + "#" + dataFieldReference.getDataFieldName(),
            dataFieldReference.getTag());
      }
    }

    return completions;
  }


  @Nullable
  public PsiElement resolve() {
    return getTemplateCompletions().get(getValue());
  }

  @NotNull
  public Object[] getVariants() {
    Map<String, PsiElement> completions = getTemplateCompletions();

    List<Object> list = new ArrayList<Object>();

    for (Map.Entry<String, PsiElement> entry : completions.entrySet()) {
      list.add(LookupElementBuilder.create(entry.getKey()));
    }

    return list.toArray();
  }
}
