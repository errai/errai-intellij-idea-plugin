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
      final PsiFile containingFile = element.getContainingFile();
      if (containingFile == null) {
        continue;
      }

      if (containingFile.getName().endsWith(".html")) {
        templateList.put(containingFile.getName(), containingFile.getOriginalFile());
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

    return list.toArray();
  }

  @Override
  public TextRange getRangeInElement() {
    return range;
  }
}
