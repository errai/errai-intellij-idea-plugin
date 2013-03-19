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
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.util.Types;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class UITemplateIsValidWidgetInspection extends BaseJavaLocalInspectionTool {
  @NotNull
  @Override
  public String getDisplayName() {
    return "Checks that the template widget is a valid composible widget type (ie. Composite)";
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
    return "UITemplateBeanIsComposite";
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
          ensureTemplateClassIsComposite(holder, PsiUtil.getTopLevelClass(annotation));
        }
      }
    }
  }

  private static void ensureTemplateClassIsComposite(final ProblemsHolder holder, final PsiClass templateClass) {
    boolean isComposite = false;
    PsiClass cls = templateClass;
    while ((cls = cls.getSuperClass()) != null) {
      if (cls.getQualifiedName().equals(Types.GWT_COMPOSITE_REF)) {
        isComposite = true;
        break;
      }
    }

    if (!isComposite) {
      if (templateClass == null) {
        return;
      }

      holder.registerProblem(templateClass.getNameIdentifier(), "Errai UI @Templated bean must extend " + Types.GWT_COMPOSITE_REF,
          new LocalQuickFix() {
            @NotNull
            @Override
            public String getName() {
              return "Make bean extend " + Types.GWT_COMPOSITE_REF;
            }

            @NotNull
            @Override
            public String getFamilyName() {
              return GroupNames.BUGS_GROUP_NAME;
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
              final PsiReferenceList extendsList = templateClass.getExtendsList();
              final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
              final PsiElementFactory elementFactory = instance.getElementFactory();
              final PsiJavaCodeReferenceElement classRef
                  = elementFactory.createReferenceElementByFQClassName(Types.GWT_COMPOSITE_REF, ProjectScope.getAllScope(project));

              if (extendsList != null) {
                if (extendsList.getReferenceElements().length > 0) {
                  for (PsiJavaCodeReferenceElement psiJavaCodeReferenceElement : extendsList.getReferenceElements()) {
                    psiJavaCodeReferenceElement.delete();
                  }
                }
                extendsList.add(classRef);
              }
            }
          });
    }
  }

}
