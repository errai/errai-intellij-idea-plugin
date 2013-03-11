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

package org.jboss.errai.idea.plugin.ui.model;

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
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
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
public class TemplatedReference extends PsiReferenceBase<PsiLiteralExpression> {
  public TemplatedReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);
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
    final TemplateMetaData templateMetaData = TemplateUtil.getTemplateMetaData(getElement());
    if (templateMetaData == null) {
      return Collections.emptyMap();
    }

    final PsiDirectory baseDir = getBaseDir();

    final Collection<TemplateDataField> allDataFieldTags = templateMetaData.getAllDataFieldsInTemplate(true).values();
    final VirtualFile templateFile = templateMetaData.getTemplateFile();

    if (allDataFieldTags.isEmpty()) {
      for (final PsiElement element : baseDir.getChildren()) {
        if (element.getContainingFile().getName().endsWith(".html")) {
          completions.put(element.getContainingFile().getName(), element);
        }
      }
    }
    else if (templateFile != null) {
      completions.put(templateMetaData.getTemplateExpression().getFileName(),
          PsiManager.getInstance(project).findFile(templateFile));

      for (TemplateDataField dataFieldReference : allDataFieldTags) {
        completions.put(templateMetaData.getTemplateExpression().getFileName() + "#" + dataFieldReference.getDataFieldName(),
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
