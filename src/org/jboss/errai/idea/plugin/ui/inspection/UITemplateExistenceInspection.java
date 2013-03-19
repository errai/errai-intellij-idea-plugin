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

package org.jboss.errai.idea.plugin.ui.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameValuePair;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jboss.errai.idea.plugin.util.Types;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Mike Brock
 */
public class UITemplateExistenceInspection extends BaseJavaLocalInspectionTool {
  @NotNull
  @Override
  public String getDisplayName() {
    return "Checks that the template specified in the template bean exists and is valid";
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return GroupNames.BUGS_GROUP_NAME;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "UITemplateExists";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new MyJavaElementVisitor(holder);
  }

  private static class MyJavaElementVisitor extends JavaElementVisitor {
    private final ProblemsHolder holder;

    public MyJavaElementVisitor(ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitAnnotation(PsiAnnotation annotation) {
      final String qualifiedName = annotation.getQualifiedName();
      if (qualifiedName != null) {
        if (qualifiedName.equals(Types.TEMPLATED_ANNOTATION_NAME)) {
          ensureTemplateExists(holder, annotation);
        }
      }
    }

    private static void ensureTemplateExists(ProblemsHolder holder,
                                             PsiAnnotation annotation) {

      final TemplateMetaData metaData = TemplateUtil.getTemplateMetaData(annotation);

      if (metaData == null) {
        return;
      }

      final VirtualFile templateVF = metaData.getTemplateFile();
      final PsiNameValuePair attribute = metaData.getAttribute();

      if (templateVF == null) {
        if (annotation != null && metaData.isDefaultReference()) {
          holder.registerProblem(annotation, "Could not find companion Errai UI template: " + metaData.getTemplateExpression().getFileName());
        }
        else if (attribute != null) {
          holder.registerProblem(attribute, "Errai UI template file cannot be resolved: " + metaData.getTemplateExpression().getFileName());
        }
      }
      else if (attribute != null && !metaData.getTemplateExpression().getRootNode().equals("")) {
        final Map<String, TemplateDataField> allDataFieldTags = metaData.getAllDataFieldsInTemplate(true);

        if (!allDataFieldTags.containsKey(metaData.getTemplateExpression().getRootNode())) {
          holder.registerProblem(attribute, "The data-field element specified for the root " +
              "note does not exist: " + metaData.getTemplateExpression().getRootNode());
        }
      }
    }

  }
}
