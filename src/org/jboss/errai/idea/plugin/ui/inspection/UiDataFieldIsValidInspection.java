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

import com.google.common.collect.Multimap;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jboss.errai.idea.plugin.util.AnnotationValueElement;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class UiDataFieldIsValidInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Perform validity checks on declared @DataField elements in template beans";
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
    return "ErraiUIDataFieldValidity";
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
        if (qualifiedName.equals(Types.DATAFIELD)) {
          ensureDataFieldIsValid(holder, annotation);
        }
      }
    }
  }

  public static void ensureDataFieldIsValid(ProblemsHolder holder,
                                            PsiAnnotation annotation) {

    final TemplateMetaData templateMetaData = TemplateUtil.getTemplateMetaData(annotation);
    if (templateMetaData == null) {
      return;
    }
    final Multimap<String, TemplateDataField> allDataFieldTags
        = templateMetaData.getAllDataFieldsInTemplate(false);

    final PsiElement ownerElement = Util.getImmediateOwnerElement(annotation);

    final AnnotationValueElement annotationValue
        = Util.getValueStringFromAnnotationWithDefault(annotation);

    if (annotationValue == null) {
      return;
    }

    final TemplateUtil.DataFieldExistence dataFieldExistence = TemplateUtil.dataFieldExistenceCheck(annotation, templateMetaData);
    final AnnotationValueElement annoValueEl = Util.getValueStringFromAnnotationWithDefault(annotation);
    final String annoValue = annoValueEl.getValue();
    if (dataFieldExistence != TemplateUtil.DataFieldExistence.EXISTS) {
      if (dataFieldExistence == TemplateUtil.DataFieldExistence.OUT_OF_SCOPE) {
        holder.registerProblem(annoValueEl.getLogicalElement(), "Data-field is out of scope (it is not an descendant of the template root node)");
      }
      else {
        holder.registerProblem(annoValueEl.getLogicalElement(), "Cannot resolve data-field: " + annoValueEl.getValue());
      }
      return;
    }

    if (!allDataFieldTags.containsKey(annotationValue.getValue())) {
      holder.registerProblem(annotationValue.getLogicalElement(),
          "No corresponding data-field element in template: " + annotationValue.getValue());
    }

    final PsiClass typeOfElement = Util.getTypeOfElement(ownerElement);
    if (!Util.typeIsAssignableFrom(typeOfElement, Types.GWT_ELEMENT_TYPE, Types.GWT_WIDGET_TYPE)) {
      holder.registerProblem(ownerElement, "Type is not a valid template part (must be Element or Widget)");
    }
  }


}
