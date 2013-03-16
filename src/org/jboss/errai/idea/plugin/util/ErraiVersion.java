package org.jboss.errai.idea.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author Mike Brock
 */
public abstract class ErraiVersion {
  private ErraiVersion() {}

  public static VersionSpec get(final PsiElement element) {
    final Project project = element.getProject();
    if (hasErrai30Types(project)) {
      return VersionSpec.V3_0;
    }
    else {
      return VersionSpec.V2_2;
    }
  }

  private static boolean hasErrai30Types(final Project project) {
    return JavaPsiFacade.getInstance(project).findClass(Types.MODEL, GlobalSearchScope.allScope(project)) != null;
  }
}
