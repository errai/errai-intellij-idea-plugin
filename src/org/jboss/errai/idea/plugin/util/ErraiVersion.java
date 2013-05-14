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
    return get(element.getProject());
  }

  public static VersionSpec get(final Project project) {
    if (hasErrai30Types(project)) {
      return VersionSpec.V3_0;
    }
    else if (hasErrai22Types(project)) {
      return VersionSpec.V2_2;
    }
    else {
      return VersionSpec.NONE;
    }
  }

  private static boolean hasErrai30Types(final Project project) {
    return JavaPsiFacade.getInstance(project).findClass(Types.MODEL, GlobalSearchScope.allScope(project)) != null;
  }

  private static boolean hasErrai22Types(final Project project) {
    return JavaPsiFacade.getInstance(project).findClass(Types.AUTO_BOUND, GlobalSearchScope.allScope(project)) != null;
  }
}
