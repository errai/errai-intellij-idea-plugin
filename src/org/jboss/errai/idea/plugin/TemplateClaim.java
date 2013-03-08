package org.jboss.errai.idea.plugin;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Mike Brock
 */
public class TemplateClaim extends Key<List<PsiClass>> {
  public TemplateClaim(@NotNull @NonNls String name) {
    super(name);
  }
}
