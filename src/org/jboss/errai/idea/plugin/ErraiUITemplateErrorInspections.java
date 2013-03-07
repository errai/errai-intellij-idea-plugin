package org.jboss.errai.idea.plugin;

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
import com.intellij.psi.PsiAnnotationParameterList;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Mike Brock
 */
public class ErraiUITemplateErrorInspections extends BaseJavaLocalInspectionTool {
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
        if (qualifiedName.equals(ErraiUISupport.TEMPLATED_ANNOTATION_NAME)) {
          final PsiClass templateClass = ((PsiClass) annotation.getParent().getParent());
          ensureTemplateExists(holder, annotation);
          ensureTemplateClassIsComposite(holder, templateClass);
        }
        else if (qualifiedName.equals(ErraiUISupport.DATAFIELD_ANNOTATION_NAME)) {
          ensureDataFieldIsValid(holder, annotation);
        }
      }
    }
  }


  private static void ensureTemplateExists(ProblemsHolder holder,
                                           PsiAnnotation annotation) {

    final Util.TemplateMetaData metaData = Util.getTemplateMetaData(annotation, holder.getProject());

    final VirtualFile templateVF = metaData.getTemplateFile();
    final PsiNameValuePair attribute = metaData.getAttribute();

    if (templateVF == null) {
      if (annotation != null && metaData.isDefaultReference()) {
        holder.registerProblem(annotation, "Could not find companion Errai UI template: " + metaData.getTemplateReference().getFileName());
      }
      else if (attribute != null) {
        holder.registerProblem(attribute, "Errai UI template file cannot be resolved.");
      }
    }
    else if (attribute != null && !metaData.getTemplateReference().getRootNode().equals("")) {
      final Map<String, Util.DataFieldReference> allDataFieldTags
          = Util.findAllDataFieldTags(metaData, holder.getProject(), true);

      if (!allDataFieldTags.containsKey(metaData.getTemplateReference().getRootNode())) {
        holder.registerProblem(attribute, "The data-field element specified for the root " +
            "note does not exist: " + metaData.getTemplateReference().getRootNode());
      }
    }
  }

  public static void ensureTemplateClassIsComposite(final ProblemsHolder holder, final PsiClass templateClass) {
    boolean isComposite = false;
    PsiClass cls = templateClass;
    while ((cls = cls.getSuperClass()) != null) {
      if (cls.getQualifiedName().equals(ErraiUISupport.GWT_COMPOSITE_REF)) {
        isComposite = true;
        break;
      }
    }

    if (!isComposite) {
      if (templateClass == null) {
        return;
      }

      holder.registerProblem(templateClass, "Errai UI @Templated bean must extend " + ErraiUISupport.GWT_COMPOSITE_REF,
          new LocalQuickFix() {
            @NotNull
            @Override
            public String getName() {
              return "Make bean extend " + ErraiUISupport.GWT_COMPOSITE_REF;
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
                  = elementFactory.createReferenceElementByFQClassName(ErraiUISupport.GWT_COMPOSITE_REF, GlobalSearchScope.allScope(project));

              if (extendsList != null) {
                if (extendsList.getReferenceElements().length > 0) {
                  for (PsiJavaCodeReferenceElement psiJavaCodeReferenceElement : extendsList.getReferenceElements()) {
                    psiJavaCodeReferenceElement.delete();
                  }
                }
                extendsList.add(classRef);
              }
              else {
                System.out.println();
              }
            }
          });
    }
  }

  public static void ensureDataFieldIsValid(ProblemsHolder holder,
                                            PsiAnnotation annotation) {

    final Util.TemplateMetaData templateMetaData = Util.getTemplateMetaData(annotation, holder.getProject());
    final Project project = holder.getProject();
    final Map<String, Util.DataFieldReference> allDataFieldTags
        = Util.findAllDataFieldTags(templateMetaData, project, false);

    final PsiAnnotationParameterList parameterList = annotation.getParameterList();
    final PsiNameValuePair[] attributes = parameterList.getAttributes();
    final PsiElement ownerElement = Util.getDataFieldOwnerElement(annotation);

    final String dataFieldName;
    final PsiElement errorElement;

    if (attributes.length == 0) {
      dataFieldName = Util.getNameOfElement(ownerElement);
      errorElement = annotation;
    }
    else {
      final String text = attributes[0].getText();
      dataFieldName = text.substring(1, text.length() - 1);
      errorElement = attributes[0];
    }

    if (errorElement == null) {
      return;
    }

    if (!allDataFieldTags.containsKey(dataFieldName)) {
      holder.registerProblem(errorElement, "No corresponding data-field element in template: " + dataFieldName);
    }

    final PsiClass typeOfElement = Util.getTypeOfElement(ownerElement, project);
    if (!Util.typeIsAssignableFrom(typeOfElement, ErraiUISupport.ELEMENT_TYPE, ErraiUISupport.WIDGET_TYPE)) {
      holder.registerProblem(ownerElement, "Type is not a valid template part (must be Element or Widget)");
    }
  }
}
