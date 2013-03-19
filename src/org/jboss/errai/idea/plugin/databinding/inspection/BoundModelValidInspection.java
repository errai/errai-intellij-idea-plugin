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

package org.jboss.errai.idea.plugin.databinding.inspection;

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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.BoundMetaData;
import org.jboss.errai.idea.plugin.util.AnnotationSearchResult;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class BoundModelValidInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Verify that the injected model is valid";
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
    return "BoundModelIsValid";
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
        if (qualifiedName.equals(Types.AUTO_BOUND) || qualifiedName.equals(Types.MODEL)) {
          ensureBoundModelIsValid(holder, annotation);
        }
      }
    }

    private static void ensureBoundModelIsValid(ProblemsHolder holder, PsiAnnotation annotation) {
      final BoundMetaData boundMetaData = DataBindUtil.getBoundMetaData(annotation);

      if (!boundMetaData.getBindingMetaData().isValidBindableModel()) {
        final PsiClass boundClass = boundMetaData.getBindingMetaData().getBoundClass();
        if (boundClass != null) {
          final PsiVariable var = (PsiVariable) Util.getImmediateOwnerElement(annotation);
          if (var == null) {
            return;
          }
          final PsiTypeElement typeElement = var.getTypeElement();
          if (typeElement == null) {
            return;
          }
          holder.registerProblem(typeElement, "The model type (" + boundClass.getQualifiedName() + ") is not bindable.");
        }
        else {
          final Collection<AnnotationSearchResult> autoBoundAnnotations
              = boundMetaData.getBindingMetaData().getModelAnnotations();

          if (autoBoundAnnotations.size() > 1) {
            holder.registerProblem(annotation, "Multiple model binding annotations found");
          }
        }
      }
      else {
        final PsiElement element = Util.getMethodOrField(annotation);

        if (!Util.elementIsAnnotated(element, Types.JAVAX_INJECT)) {
          holder.registerProblem(annotation, "Model specifier is not injected", new LocalQuickFix() {
            @NotNull
            @Override
            public String getName() {
              return "Add @Inject to class member";
            }

            @NotNull
            @Override
            public String getFamilyName() {
              return GroupNames.BUGS_GROUP_NAME;
            }

            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
              final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
              final PsiElementFactory elementFactory = instance.getElementFactory();

              final PsiImportStatement importJavaxInject = instance.getElementFactory()
                  .createImportStatement(
                      instance.findClass(Types.JAVAX_INJECT,
                          ProjectScope.getAllScope(project))
                  );

              final PsiImportList importList = ((PsiJavaFile) PsiUtil.getTopLevelClass(element).getParent()).getImportList();
              importList.add(importJavaxInject);

              ((PsiModifierListOwner) element).getModifierList().addAnnotation("Inject");
            }
          });
        }
      }
    }
  }
}