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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.model.ConsolidateDataFieldElementResult;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jboss.errai.idea.plugin.util.AnnotationValueElement;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Mike Brock
 */
@SuppressWarnings("ALL")
public class UITemplateErrorInspections extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Perform validity checks on Errai UI @Templated classes";
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
    return "ErraiUITemplateChecks";
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
          final PsiClass templateClass = (PsiClass) PsiUtil.getTopLevelClass(annotation);
          ensureTemplateExists(holder, annotation);
          ensureTemplateClassIsComposite(holder, templateClass);
        }
        else if (qualifiedName.equals(Types.DATAFIELD_ANNOTATION_NAME)) {
          ensureDataFieldIsValid(holder, annotation);
        }
        else if (qualifiedName.equals(Types.EVENTHANDLER_ANNOTATION_NAME)) {
          ensureEventHandlerIsValid(holder, annotation);
        }
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

  public static void ensureTemplateClassIsComposite(final ProblemsHolder holder, final PsiClass templateClass) {
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

  public static void ensureDataFieldIsValid(ProblemsHolder holder,
                                            PsiAnnotation annotation) {

    final TemplateMetaData templateMetaData = TemplateUtil.getTemplateMetaData(annotation);
    if (templateMetaData == null) {
      return;
    }

    final Project project = holder.getProject();
    final Map<String, TemplateDataField> allDataFieldTags
        = templateMetaData.getAllDataFieldsInTemplate(false);

    final PsiElement ownerElement = Util.getImmediateOwnerElement(annotation);

    final AnnotationValueElement annotationValue
        = Util.getValueStringFromAnnotationWithDefault(annotation);

    if (annotationValue == null) {
      return;
    }

    final DataFieldExistence dataFieldExistence = dataFieldExistenceCheck(annotation, templateMetaData);
    final AnnotationValueElement annoValueEl = Util.getValueStringFromAnnotationWithDefault(annotation);
    final String annoValue = annoValueEl.getValue();
    if (dataFieldExistence != DataFieldExistence.EXISTS) {
      if (dataFieldExistence == DataFieldExistence.OUT_OF_SCOPE) {
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

    final PsiClass typeOfElement = Util.getTypeOfElement(ownerElement, project);
    if (!Util.typeIsAssignableFrom(typeOfElement, Types.GWT_ELEMENT_TYPE, Types.GWT_WIDGET_TYPE)) {
      holder.registerProblem(ownerElement, "Type is not a valid template part (must be Element or Widget)");
    }
  }

  public static void ensureEventHandlerIsValid(ProblemsHolder holder,
                                               PsiAnnotation annotation) {
    final TemplateMetaData metaData = TemplateUtil.getTemplateMetaData(annotation);
    final Project project = holder.getProject();

    final PsiClass bean = PsiUtil.getTopLevelClass(annotation);
    final PsiElement owner = Util.getImmediateOwnerElement(annotation);
    final boolean hasSinkEvent = Util.fieldOrMethodIsAnnotated(owner, Types.SINKNATIVE_ANNOTATION_NAME);

    if (!(owner instanceof PsiMethod)) return;

    final PsiMethod psiMethod = (PsiMethod) owner;
    final PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
    if (psiParameters.length == 0) {
      holder.registerProblem(psiMethod.getParameterList(), "Event handler method must accept one parameter");
      return;
    }
    else if (psiParameters.length > 1) {
      holder.registerProblem(psiMethod.getParameterList(), "Event handler method must only accept one parameter");
      return;
    }

    final PsiParameter psiParameter = psiParameters[0];
    final String parameterTypeFQN = psiParameter.getType().getCanonicalText();

    final AnnotationValueElement annoValueEl = Util.getValueStringFromAnnotationWithDefault(annotation);
    String annoValue = annoValueEl.getValue();

    if (annoValue == null) {
      /** default 'this' case **/
      return;
    }

    final PsiClass psiClassParameterType = JavaPsiFacade.getInstance(project)
        .findClass(parameterTypeFQN, ProjectScope.getAllScope(project));
    final boolean isGWTeventType = Util.typeIsAssignableFrom(psiClassParameterType, Types.GWT_EVENT_TYPE);

    final DataFieldExistence dataFieldExistence = dataFieldExistenceCheck(annotation, metaData);
    final Map<String, ConsolidateDataFieldElementResult> dataFields = metaData.getConsolidatedDataFields();

    if (dataFieldExistence != DataFieldExistence.EXISTS) {
      if (dataFieldExistence == DataFieldExistence.OUT_OF_SCOPE) {
        holder.registerProblem(annoValueEl.getLogicalElement(), "Data-field is out of scope (it is not an descendant of the template root node)");
      }
      else {
        holder.registerProblem(annoValueEl.getLogicalElement(), "Cannot resolve data-field: " + annoValueEl.getValue());
      }
    }
    else if (!hasSinkEvent && !dataFields.get(annoValue).isDataFieldInClass()) {
      holder.registerProblem(annotation, "Non-injected data-field element is missing @SinkNative", new LocalQuickFix() {
        @NotNull
        @Override
        public String getName() {
          return "Add @SinkNative";
        }

        @NotNull
        @Override
        public String getFamilyName() {
          return GroupNames.BUGS_GROUP_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {

          final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
          final PsiImportStatement importSinkNative = instance.getElementFactory()
              .createImportStatement(
                  instance.findClass(Types.SINKNATIVE_ANNOTATION_NAME,
                      ProjectScope.getAllScope(project))
              );

          final PsiImportStatement importDomEvent = instance.getElementFactory()
              .createImportStatement(
                  instance.findClass(Types.GWT_DOM_EVENT_TYPE,
                      ProjectScope.getAllScope(project))
              );

          final PsiImportList importList = ((PsiJavaFile) bean.getParent()).getImportList();

          importList.add(importSinkNative);
          importList.add(importDomEvent);

          psiMethod.getModifierList().addAnnotation("SinkNative(Event.ONCLICK)");
        }
      });
    }


    if (isGWTeventType) {
      if (hasSinkEvent && dataFields.containsKey(annoValue) && dataFields.get(annoValue).isDataFieldInClass()) {
        final PsiAnnotation sinkNativeAnnotation = Util.getAnnotationFromElement(psiMethod, Types.SINKNATIVE_ANNOTATION_NAME);

        holder.registerProblem(sinkNativeAnnotation, "Handler that extends GwtEvent is incompatible with @SinkNative",
            new LocalQuickFix() {
              @NotNull
              @Override
              public String getName() {
                return "Remove @SinkNative";
              }

              @NotNull
              @Override
              public String getFamilyName() {
                return GroupNames.BUGS_GROUP_NAME;
              }

              @Override
              public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                final PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
                for (PsiAnnotation a : annotations) {
                  if (a.getQualifiedName().equals(Types.SINKNATIVE_ANNOTATION_NAME)) {
                    a.delete();
                    return;
                  }
                }
              }
            });
      }

      if (dataFields.containsKey(annoValue) && !dataFields.get(annoValue).isDataFieldInClass()) {
        holder.registerProblem(psiParameter, "DOM based event binding cannot use a GwtEvent",
            new LocalQuickFix() {
              @NotNull
              @Override
              public String getName() {
                return "Change handled event type to: " + Types.GWT_DOM_EVENT_TYPE;
              }

              @NotNull
              @Override
              public String getFamilyName() {
                return GroupNames.BUGS_GROUP_NAME;
              }

              @Override
              public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
                final PsiClass psiClass = instance.findClass(Types.GWT_DOM_EVENT_TYPE,
                    ProjectScope.getAllScope(project));

                final PsiParameter[] parameters = psiParameters;
                final PsiElementFactory elementFactory = instance.getElementFactory();
                final PsiParameter parameter = parameters[0];
                parameter.replace(elementFactory.createParameter(parameter.getName(), elementFactory.createType(psiClass)));
              }
            });
      }
    }
    else if (!Util.typeIsAssignableFrom(psiClassParameterType, Types.GWT_DOM_EVENT_TYPE)) {
      holder.registerProblem(psiParameter, "Not a valid event type.");
    }
  }

  enum DataFieldExistence {
    EXISTS, DOES_NOT_EXIST, OUT_OF_SCOPE;

  }

  public static DataFieldExistence dataFieldExistenceCheck(PsiAnnotation annotation, TemplateMetaData metaData) {
    final Map<String, TemplateDataField> inScopeDataFields = metaData.getAllDataFieldsInTemplate(false);
    final Map<String, ConsolidateDataFieldElementResult> dataFields = metaData.getConsolidatedDataFields();

    final AnnotationValueElement annoValueEl = Util.getValueStringFromAnnotationWithDefault(annotation);
    final String annoValue = annoValueEl.getValue();

    final TemplateDataField result = inScopeDataFields.get(annoValue);
    if (result == null) {
      if (dataFields.containsKey(annoValue)) {
        return DataFieldExistence.OUT_OF_SCOPE;
      }
      else {
        return DataFieldExistence.DOES_NOT_EXIST;
      }
    }
    else {
      return DataFieldExistence.EXISTS;
    }
  }
}
