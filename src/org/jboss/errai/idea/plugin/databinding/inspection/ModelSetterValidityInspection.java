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
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.BoundMetaData;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ModelSetterValidityInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Verify that the @ModelSetter method is valid";
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
    return "ModelSetterIsValid";
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
          ensureModelSetterIsValid(holder, annotation);
        }
      }
    }
  }

  private static void ensureModelSetterIsValid(ProblemsHolder holder, PsiAnnotation annotation) {
    final BoundMetaData boundMetaData = DataBindUtil.getBoundMetaData(annotation);
    final PsiElement element = Util.getImmediateOwnerElement(annotation);
    final PsiMethod method = (PsiMethod) element;

    final PsiParameter[] parameters = method.getParameterList().getParameters();
    if (parameters.length != 1) {
      holder.registerProblem(method.getParameterList(), "@MethodSetter method must have exactly one parameter");
    }
    else {
      final PsiParameter parameter = parameters[0];
      final PsiClass typeOfElement = Util.getTypeOfElement(parameter);

      if (!typeOfElement.equals(boundMetaData.getBindingMetaData().getBoundClass())) {
        final PsiClass boundClass = boundMetaData.getBindingMetaData().getBoundClass();
        if (boundClass == null) {
          return;
        }

        holder.registerProblem(parameter, "Wrong type found for @MethodSetter method. Expected: "
            + boundClass.getQualifiedName());
      }
    }
  }

}
