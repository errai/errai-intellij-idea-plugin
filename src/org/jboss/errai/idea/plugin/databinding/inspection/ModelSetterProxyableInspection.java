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
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ModelSetterProxyableInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Bean with @MethodSetter is a member of a proxyable bean";
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
    return "MethodSetterOnProxyableBean";
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
        if (qualifiedName.equals(Types.MODEL_SETTER)) {
          ensureBeanisProxyable(holder, annotation);
        }
      }
    }
  }

  private static void ensureBeanisProxyable(ProblemsHolder holder, PsiAnnotation annotation) {
    final PsiClass topLevelClass = PsiUtil.getTopLevelClass(annotation);
    if (topLevelClass == null) {
      return;
    }

    final PsiModifierList modifierList = topLevelClass.getModifierList();
    if (modifierList == null) {
      return;
    }

    final PsiIdentifier nameIdentifier = topLevelClass.getNameIdentifier();
    if (nameIdentifier == null) {
      return;
    }

    if (!modifierList.hasModifierProperty("public")) {
      holder.registerProblem(nameIdentifier,
          "Bean declares a @ModelSetter, but the bean is not public and cannot be proxied");
    }

    if (!Util.isDefaultInstantiable(topLevelClass)) {
      holder.registerProblem(nameIdentifier, "Bean declares a @ModelSetter, but the bean is not proxyable as it does " +
          "not have a default no-arg constructor. ", new LocalQuickFix() {
        @NotNull
        @Override
        public String getName() {
          return "Add a default no-arg constructor to make bean proxyable";
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

          PsiElement offset = null;
          for (PsiField field : topLevelClass.getFields()) {
            final PsiModifierList fieldModifierList = field.getModifierList();
            if (fieldModifierList == null) {
              continue;
            }

            if (fieldModifierList.hasModifierProperty("final") && !field.hasInitializer()) {
              fieldModifierList.setModifierProperty("final", false);
            }

            offset = field;
          }

          final PsiMethod[] constructors = topLevelClass.getConstructors();
          //noinspection LoopStatementThatDoesntLoop
          for (final PsiMethod constructor : constructors) {
            topLevelClass.addBefore(elementFactory.createConstructor(), constructor);
            return;
          }
          if (offset == null) {
            topLevelClass.add(elementFactory.createConstructor());
          }
          else {
            topLevelClass.addAfter(offset, elementFactory.createConstructor());
          }
        }
      });
    }
  }
}
