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
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.BindabilityValidation;
import org.jboss.errai.idea.plugin.databinding.model.BoundMetaData;
import org.jboss.errai.idea.plugin.databinding.model.PropertyValidation;
import org.jboss.errai.idea.plugin.util.AnnotationSearchResult;
import org.jboss.errai.idea.plugin.util.ExpressionErrorReference;
import org.jboss.errai.idea.plugin.util.FileTemplateUtil;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Mike Brock
 */
public class  BoundFieldValidityInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Perform validity checks for Errai @Bound elements.";
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
    return "BoundElementIsValid";
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
        if (qualifiedName.equals(Types.BOUND)) {
          ensureBoundFieldIsValid(holder, annotation);
        }
      }
    }
  }

  private static void ensureBoundFieldIsValid(ProblemsHolder holder,
                                              final PsiAnnotation psiAnnotation) {

    final BoundMetaData boundMetaData = DataBindUtil.getBoundMetaData(psiAnnotation);

    if (boundMetaData.getBindingMetaData().getBoundClass() == null) {
      final Collection<AnnotationSearchResult> autoBoundAnnotations
          = boundMetaData.getBindingMetaData().getModelAnnotations();
      if (autoBoundAnnotations.size() > 1) {
        holder.registerProblem(psiAnnotation, "@Bound property cannot be associated with model because" +
            " multiple models are injected.");
      }
      else {
        holder.registerProblem(psiAnnotation, "@Bound property is not associated with any model.");
      }
      return;
    }

    final PropertyValidation validation = boundMetaData.validateProperty();
    if (!validation.isValid()) {
      if (!validation.isParentBindable()) {
        final PsiClass unresolvedParent = validation.getUnresolvedParent();
        if (unresolvedParent == null) {
          return;
        }

        holder.registerProblem(Util.getAnnotationMemberValue(psiAnnotation, "property"),
            "The property '" + validation.getUnresolvedPropertyElement() + "' is invalid because its parent bean "
                + "(" + unresolvedParent.getQualifiedName() + ") is not bindable.");
      }
      else if (validation.hasBindabilityProblem()) {
        final BindabilityValidation bindabilityValidation = validation.getBindabilityValidation();
        holder.registerProblem(psiAnnotation,
            "The widget type cannot be bound to: " + validation.getBoundType().getQualifiedName()
                + "; widget accepts type: " + bindabilityValidation.getExpectedWidgetType(),
            new LocalQuickFix() {
              @NotNull
              @Override
              public String getName() {
                return "Create a data binding Converter";
              }

              @NotNull
              @Override
              public String getFamilyName() {
                return GroupNames.BUGS_GROUP_NAME;
              }

              @Override
              public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                final String name = validation.getBoundType().getName()
                    + "To" + bindabilityValidation.getSimpleExpectedWidgetName() + "Converter";
                final PsiClass topLevelClass = PsiUtil.getTopLevelClass(psiAnnotation);
                final PsiDirectory directory = topLevelClass.getOriginalElement().getContainingFile().getParent();

                FileTemplateUtil.createFileFromTemplate("Converter.java", name, directory
                    , new HashMap<String, String>() {
                  {
                    put("CONVERTER_INTERFACE_TYPE", Types.CONVERTER);
                    put("MODEL_TYPE", validation.getBoundType().getQualifiedName());
                    put("WIDGET_TYPE", bindabilityValidation.getExpectedWidgetType());
                  }
                });

                psiAnnotation.setDeclaredAttributeValue("converter",
                    JavaPsiFacade.getInstance(psiAnnotation.getProject()).getElementFactory()
                        .createAnnotationFromText("@A(converter = " + name + ".class)", null)
                        .findDeclaredAttributeValue("converter"));
              }
            });
      }
      else {
        final String errorText = "The property '" + validation.getUnresolvedPropertyElement()
            + "' was not found in parent bean: " + validation.getParentName();

        final PsiNameValuePair[] attributes = psiAnnotation.getParameterList().getAttributes();

        if (attributes.length > 0) {
          final PsiAnnotationMemberValue value = attributes[0].getValue();
          if (value != null) {

            for (PsiReference ref : value.getReferences()) {
              if (ref instanceof ExpressionErrorReference) {
                holder.registerProblemForReference(ref, ProblemHighlightType.ERROR, errorText);
                holder.registerProblem(psiAnnotation, "The binding is invalid.");
                return;
              }
            }
          }
        }

        holder.registerProblem(psiAnnotation, errorText);
      }
    }
  }
}
