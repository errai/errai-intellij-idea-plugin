package org.jboss.errai.idea.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.FakePsiElement;

/**
 * @author Mike Brock
 */
public class FakeNamedPsi extends FakePsiElement implements PsiNamedElement {
  private PsiElement parent;
  public FakeNamedPsi(PsiElement parent) {
    this.parent =parent;
  }

  @Override
  public PsiElement getParent() {
    return parent;
  }
}
