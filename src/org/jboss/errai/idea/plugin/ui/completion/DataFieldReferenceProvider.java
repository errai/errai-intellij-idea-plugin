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

package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class DataFieldReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    final PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
    final String value = (String) literalExpression.getValue();

    final String text;
    if (value == null) {
      text = "";
    }
    else {
      text = value.replace(Util.INTELLIJ_MAGIC_STRING, "");
    }

    return new PsiReference[] {
       new DataFieldReference(false, literalExpression, TextRange.create(1, text.length() + 1))
    };
  }
}
