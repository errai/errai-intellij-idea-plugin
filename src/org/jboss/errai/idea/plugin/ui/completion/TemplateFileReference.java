package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TemplateFileReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final TextRange range;

  public TemplateFileReference(PsiLiteralExpression literalExpression, TextRange range) {
    super(literalExpression, false);
    this.range = range;
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

  private Map<String, PsiFile> getAllPossibleTemplateFiles() {
    final Map<String, PsiFile> templateList = new LinkedHashMap<String, PsiFile>();

    for (final PsiElement element : getBaseDir().getChildren()) {
      if (element.getContainingFile().getName().endsWith(".html")) {
        templateList.put(element.getContainingFile().getName(), element.getContainingFile().getOriginalFile());
      }
    }

    return templateList;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return getAllPossibleTemplateFiles().get(getValue());
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return element.equals(resolve());
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final Map<String, PsiFile> allPossibleTemplateFiles = getAllPossibleTemplateFiles();

    List<Object> list = new ArrayList<Object>();

    for (Map.Entry<String, PsiFile> entry : allPossibleTemplateFiles.entrySet()) {
      list.add(LookupElementBuilder.create(entry.getKey()).withTypeText("File"));
    }

    System.out.println("returned completions: " + list);

    return list.toArray();
  }

  @Override
  public TextRange getRangeInElement() {
    return range;
  }
}
