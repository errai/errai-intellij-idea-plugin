package org.jboss.errai.idea.plugin.marshalling.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierList;
import org.jboss.errai.idea.plugin.marshalling.MarshallingUtil;
import org.jboss.errai.idea.plugin.util.ErraiVersion;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jboss.errai.idea.plugin.util.VersionSpec;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author Mike Brock
 */
public class PortableTypeValidExtension extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Ensure's Errai portable types are valid";
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
    return "PortableTypeIsValid";
  }


  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
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

  private static class MyJavaElementVisitor extends JavaElementVisitor {
    private ProblemsHolder holder;

    private MyJavaElementVisitor(ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitClass(PsiClass aClass) {
      if (ErraiVersion.get(aClass) != VersionSpec.V3_0) {
        /**
         * If this not version 3.0, this inspection is not applicable.
         */
        return;
      }

      final PsiAnnotation portableAnno = Util.getAnnotationFromElement(aClass, Types.PORTABLE);
      if (portableAnno != null) {
        final Set<String> allProprtableTypes = MarshallingUtil.getAllProprtableTypes(aClass.getProject());

        for (PsiField psiField : aClass.getFields()) {
          final PsiModifierList modifierList = psiField.getModifierList();
          if (modifierList == null || modifierList.hasModifierProperty("transient")) {
            continue;
          }

          if (!allProprtableTypes.contains(Util. boxedType(psiField.getType().getCanonicalText()))) {
            holder.registerProblem(psiField.getTypeElement(), "field of portable type is not portable");
          }
        }
      }
    }
  }
}
