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

package org.jboss.errai.idea.plugin.rpc.inspection;

import static org.jboss.errai.idea.plugin.util.Util.typeIsAssignableFrom;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class RpcRemoteCallbackInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Checks RemoteCallback<T> accepts correct return type";
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
    return "RPCReturnTypeValid";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new MyJavaElementVisitor(holder);
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  private static class MyJavaElementVisitor extends JavaElementVisitor {
    private final ProblemsHolder holder;

    private MyJavaElementVisitor(ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
      PsiReferenceExpression e = findMethodCallRootExpression(expression);
      if (e == null) {
        return;
      }
      final Project project = expression.getProject();

      final PsiClass eType = getPsiClass(project, e.getType());
      if (eType == null || e.getType() == null) {
        return;
      }

      if (!Util.typeIsAssignableFrom(eType, Types.CALLER)) {
        return;
      }

      final PsiClass remoteType = Util.getErasedTypeParam(project, e.getType().getCanonicalText());

      if (remoteType == null) {
        return;
      }

      PsiMethod method = findPsiMethod(remoteType, expression);

      if (method == null) {
        return;
      }

      PsiClass callbackType = getRemoteCallbackReturnType(expression);

      if (callbackType == null) {
        return;
      }

      ensureRemoteCallbackValidReturnType(holder, method, callbackType, expression);
    }
  }




  private static void ensureRemoteCallbackValidReturnType(ProblemsHolder holder,
                                                          PsiMethod resolvedMethod,
                                                          PsiClass callbackType,
                                                          PsiMethodCallExpression expression) {

    if (!Util.typeIsAssignableFrom(callbackType,
        Util.getErasedCanonicalText(resolvedMethod.getReturnType().getCanonicalText()))) {
      holder.registerProblem(expression,
          "RemoteCallback accepts wrong return type (" + callbackType.getQualifiedName() + "); expected: "
              + resolvedMethod.getReturnType().getCanonicalText());
    }
  }

  private static PsiClass getRemoteCallbackReturnType(PsiMethodCallExpression expression) {
    final PsiElement parent = expression.getParent();
    PsiElement el = parent.getFirstChild();
    while ((el = el.getFirstChild()) != null) {
      if (el instanceof PsiMethodCallExpression) {
        break;
      }
    }

    if (el == null) {
      return null;
    }

    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) el;
    final PsiType[] expressionTypes = methodCallExpression.getArgumentList().getExpressionTypes();
    if (expressionTypes.length == 0) {
      return null;
    }

    return Util.getErasedTypeParam(expression.getProject(), expressionTypes[0].getCanonicalText());
  }

  private static PsiClass getPsiClass(Project project, PsiType element) {
    return JavaPsiFacade.getInstance(project).findClass(
        Util.getErasedCanonicalText(element.getCanonicalText())
        , GlobalSearchScope.allScope(project));
  }

  private static PsiMethod findPsiMethod(PsiClass remoteClass, PsiMethodCallExpression expression) {
    final PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
    final PsiElement referenceNameElement = expression.getMethodExpression().getReferenceNameElement();
    if (referenceNameElement == null) {
      return null;
    }

    final String methodName = referenceNameElement.getText();

    if (methodName == null) {
      return null;
    }

    final Project project = remoteClass.getProject();

    Search:
    for (PsiMethod psiMethod : remoteClass.getAllMethods()) {
      if (psiMethod.getName().equals(methodName)
          && expressionTypes.length == psiMethod.getParameterList().getParametersCount()) {

        final PsiParameter[] parameters = psiMethod.getParameterList().getParameters();

        for (int i = 0; i < parameters.length; i++) {
          final String s = Util.boxedType(expressionTypes[i].getCanonicalText());
          final PsiClass psiClass = getPsiClass(project, parameters[i].getType());
          if (!typeIsAssignableFrom(psiClass, s)) {
            continue Search;
          }
        }

        return psiMethod;
      }
    }

    return null;
  }

  private static PsiReferenceExpression findMethodCallRootExpression(PsiElement element) {
    PsiElement el = null;
    while ((element = element.getFirstChild()) != null) {
      if (element instanceof PsiReferenceExpression) {
        el = element;
      }
    }
    return (PsiReferenceExpression) el;
  }
}
