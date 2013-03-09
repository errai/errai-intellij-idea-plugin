package org.jboss.errai.idea.plugin;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ErraiUITemplateCodeSmellInspections extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Show potential problems with Errai UI @Templated classes";
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
    return "HasHTMLTemplate";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
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
        if (qualifiedName.equals(Types.DATAFIELD_ANNOTATION_NAME)) {
          ensureDataFieldIsValid(holder, annotation);
        }
      }
    }
  }


  public static void ensureDataFieldIsValid(final ProblemsHolder holder,
                                            final PsiAnnotation annotation) {

    final PsiElement ownerElement = Util.getImmediateOwnerElement(annotation);

    final boolean fieldIsInject = Util.fieldOrMethodIsAnnotated(ownerElement, Types.JAVAX_INJECT);
    final boolean fieldInitialized = Util.fieldElementIsInitialized(ownerElement);
    if (!fieldIsInject && !fieldInitialized) {
      holder.registerProblem(ownerElement, "Un-injected @DataField element is not initialized and may fail at runtime.");
    }
    else if (fieldIsInject && fieldInitialized) {
      final LocalQuickFix localQuickFix = new LocalQuickFix() {
        @NotNull
        @Override
        public String getName() {
          return "Remove @Inject annotation";
        }

        @NotNull
        @Override
        public String getFamilyName() {
          return GroupNames.BUGS_GROUP_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
          final PsiElement psiElement = Util.getImmediateOwnerElement(annotation);
          final PsiField psiField = (PsiField) psiElement;
          final PsiModifierList modifierList = psiField.getModifierList();
          if (modifierList != null) {
            for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
              final String qualifiedName = psiAnnotation.getQualifiedName();
              if (qualifiedName != null && qualifiedName.equals(Types.JAVAX_INJECT)) {
                psiAnnotation.delete();
              }
            }
          }
        }
      };

      if (ownerElement != null) {
        holder.registerProblem(ownerElement, "Injected @DataField element has a default value which will be overwritten at runtime.",
            localQuickFix);
      }
    }
  }
}
