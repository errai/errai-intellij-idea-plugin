package org.jboss.errai.idea.plugin.databinding.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jboss.errai.idea.plugin.databinding.model.BindabilityValidation;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.BoundMetaData;
import org.jboss.errai.idea.plugin.databinding.model.PropertyValidation;
import org.jboss.errai.idea.plugin.util.ExpressionErrorReference;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class DataBindingErrorInspections extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Perform validity checks for Errai databinding semantics";
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
    return "ErraiDataBindingChecks";
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

  public static void ensureBoundFieldIsValid(ProblemsHolder holder,
                                                     PsiAnnotation psiAnnotation) {


      final BoundMetaData boundMetaData = DataBindUtil.getBoundMetaData(psiAnnotation);

      if (boundMetaData.getBindingMetaData().getBoundClass() == null) {
        holder.registerProblem(psiAnnotation, "@Bound property is not associated with any model.");
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
          holder.registerProblem(psiAnnotation, "The widget type cannot be bound to: " + validation.getBoundType().getQualifiedName()
              + "; widget accepts type: " + bindabilityValidation.getExpectedWidgetType());

          //TODO: use this for more fine-grained errors.
          //holder.getManager().createProblemDescriptor()
        }
        else {
          final String errorText = "The property '" + validation.getUnresolvedPropertyElement() + "' was not found in parent bean: "
              + validation.getParentName();

          final PsiAnnotationMemberValue value = psiAnnotation.getParameterList().getAttributes()[0].getValue();
          for (PsiReference ref : value.getReferences()) {
            if (ref instanceof ExpressionErrorReference) {
              holder.registerProblemForReference(ref, ProblemHighlightType.ERROR, errorText);
              holder.registerProblem(psiAnnotation, "The binding is invalid.");
              return;
            }
          }

          holder.registerProblem(psiAnnotation, errorText);
        }
      }
    }
}
