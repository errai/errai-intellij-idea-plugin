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
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiUtil;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.TemplateBindingMetaData;
import org.jboss.errai.idea.plugin.util.ElementFilter;
import org.jboss.errai.idea.plugin.util.ErraiVersion;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jboss.errai.idea.plugin.util.VersionSpec;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class DataBinderCanBeModelInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Check if an injected DataBinder can be represented as a basic @Model injection (Errai 3.0 Only)";
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return GroupNames.VERBOSE_GROUP_NAME;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "ModelCanBeDirectlyInjected";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  public boolean runForWholeFile() {
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
        if (qualifiedName.equals(Types.AUTO_BOUND)) {
          checkModelCanBeDirectlyInjected(holder, annotation);
        }
      }
    }

    private static void checkModelCanBeDirectlyInjected(final ProblemsHolder holder,
                                                        final PsiAnnotation annotation) {
      if (ErraiVersion.get(annotation) != VersionSpec.V3_0) {
        /**
         * If this not version 3.0, this inspection is not applicable.
         */
        return;
      }

      final PsiClass declaringClass = PsiUtil.getTopLevelClass(annotation);
      final PsiVariable var = Util.getEnclosingVariable(annotation);

      if (var == null) {
        return;
      }

      final String name = var.getName();
      final PsiElement owningElement = Util.getMethodOrField(annotation);
      if (owningElement instanceof PsiMethod) {
        PsiMethod method = (PsiMethod) owningElement;

        Collection<PsiElement> childrenElements
            = Util.findChildrenElements(method.getBody(), new ElementFilter() {
          @Override
          public boolean filter(PsiElement element) {
            if (element instanceof PsiAssignmentExpression) {
              return true;
            }
            else if (element.getChildren().length != 0) {
              return false;
            }

            return element instanceof PsiExpressionStatement;
          }
        });

        final Set<String> escapeSet = new HashSet<String>();
        class ReplaceSafety {
          boolean safe = true;
        }

        final ReplaceSafety safety = new ReplaceSafety();

        for (PsiElement element : childrenElements) {
          if (element instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression) element;
            final PsiExpression rExpression = assignmentExpression.getRExpression();
            if (rExpression != null && name != null && name.equals(rExpression.getText())) {
              String lhs = assignmentExpression.getLExpression().getText();
              if (lhs.startsWith("this.")) {
                lhs = lhs.substring(5);
              }
              escapeSet.add(lhs);
            }
          }
          else if (element instanceof PsiExpressionStatement) {
            if (element.getText().indexOf('.') != -1) {
              safety.safe = false;
              break;
            }
          }
        }

        final List<PsiElement> getModelRefExpressions = new ArrayList<PsiElement>();
        final List<PsiElement> setModelRefExpressions = new ArrayList<PsiElement>();

        if (safety.safe && !escapeSet.isEmpty()) {
          childrenElements
              = Util.findChildrenElements(declaringClass, new ElementFilter() {
            @Override
            public boolean filter(PsiElement element) {
              if (element instanceof PsiReferenceExpression) {
                return true;
              }
              else if (element.getChildren().length != 0) {
                return false;
              }

              return element instanceof PsiExpressionStatement;
            }
          });

          DeepCheck:
          for (PsiElement element : childrenElements) {
            // PsiExpressionStatement psiExpressionStatement = (PsiExpressionStatement) element;

            boolean refDisqualifies = false;

            // if the parent is a PisExpressionList, then this is a method or constructor call. in which case
            // an expression containing the reference means this can't be safe.
            PsiElement el = element;
            while ((el = el.getParent()) != null) {
              if (el instanceof PsiExpressionList) {
                refDisqualifies = true;
                break;
              }
              else if (el instanceof PsiMethod) {
                break;
              }
            }

            String text = element.getText().replaceAll(" ", "");
            for (String escaped : escapeSet) {
              if (text.startsWith("this.")) {
                text = text.substring(5);
              }
              if (text.startsWith(escaped)) {
                if (!refDisqualifies) {
                  // there's a method call. disqualify!
                  if (text.indexOf('.') != -1) {
                    if (text.equals(escaped + ".getModel")) {
                      getModelRefExpressions.add(element);
                    }
//                    else if (text.equals(escaped + ".setModel")) {
//                      setModelRefExpressions.add(element);
//                    }
                    else {
                      safety.safe = false;
                      break DeepCheck;
                    }
                  }
                }
                else {
                  safety.safe = false;
                  break DeepCheck;
                }
              }
            }
          }
        }

        if (safety.safe) {
          holder.registerProblem(annotation.getParent().getParent(),
              "Injected @AutoBound DataBinder can safely be replaced with @Model.",
              new LocalQuickFix() {
                @NotNull
                @Override
                public String getName() {
                  return "Replace @AutoBound with injected @Model";
                }

                @NotNull
                @Override
                public String getFamilyName() {
                  return GroupNames.VERBOSE_GROUP_NAME;
                }

                @Override
                public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                  final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
                  final PsiElementFactory elementFactory = instance.getElementFactory();
                  final TemplateBindingMetaData metaData = DataBindUtil.getTemplateBindingMetaData(annotation);

                  final PsiType typeFromText
                      = elementFactory.createTypeFromText(
                      metaData.getBoundClass().getQualifiedName(),
                      var.getTypeElement()
                  );

                  var.getTypeElement().replace(elementFactory.createTypeElement(typeFromText));
                  for (PsiAnnotation psiAnnotation : var.getModifierList().getAnnotations()) {
                    if (psiAnnotation.getQualifiedName().equals(Types.AUTO_BOUND)) {
                      psiAnnotation.delete();
                    }
                  }

                  final PsiImportStatement importModelAnnot = instance.getElementFactory()
                      .createImportStatement(
                          instance.findClass(Types.MODEL,
                              ProjectScope.getAllScope(project))
                      );

                  final PsiImportList importList = ((PsiJavaFile) declaringClass.getParent()).getImportList();
                  importList.add(importModelAnnot);

                  var.getModifierList().addAnnotation("Model");

                  if (!escapeSet.isEmpty()) {
                    final String name = escapeSet.iterator().next();

                    for (PsiField field : declaringClass.getAllFields()) {
                      if (field.getName().equals(name)) {
                        field.getTypeElement().replace(elementFactory.createTypeElement(typeFromText));
                      }
                    }
                    for (PsiElement element : getModelRefExpressions) {
                      PsiReferenceExpression stmt = (PsiReferenceExpression) element;
                      final PsiExpression expression = elementFactory.createExpressionFromText("this." + name, element);
                      stmt.getParent().replace(expression);
                    }
                  }
                }
              });
        }
      }
    }
  }
}
