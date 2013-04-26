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

package org.jboss.errai.idea.plugin.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.ElementPatternCondition;
import com.intellij.patterns.InitialPatternCondition;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

/**
 * @author Mike Brock
 */
public class AnnotationMatchingPattern implements ElementPattern<PsiLiteralExpression> {
  private final String type;
  private final String attributeName;

  private final ElementPatternCondition<PsiLiteralExpression> patternCondition =
      new ElementPatternCondition<PsiLiteralExpression>(new InitialPatternCondition<PsiLiteralExpression>(PsiLiteralExpression.class) {
        @Override
        public boolean accepts(@Nullable Object o, ProcessingContext context) {
          if (o instanceof PsiLiteralExpression) {
            final PsiAnnotation parentOfType = PsiTreeUtil.getParentOfType((PsiLiteralExpression) o, PsiAnnotation.class);

            if (parentOfType != null && type.equals(parentOfType.getQualifiedName())) {
              if (attributeName != null) {
                final PsiNameValuePair nvp = PsiTreeUtil.getParentOfType((PsiLiteralExpression) o, PsiNameValuePair.class);
                return nvp != null && attributeName.equals(nvp.getName());
              }
              else {
                return true;
              }
            }
          }
          return false;
        }
      });

  public AnnotationMatchingPattern(String type, String attributeName) {
    this.type = type;
    this.attributeName = attributeName;
  }

  public AnnotationMatchingPattern(String type) {
    this(type, null);
  }

  @Override
  public boolean accepts(@Nullable Object o) {
    return false;
  }

  @Override
  public boolean accepts(@Nullable Object o, ProcessingContext processingContext) {
    return getCondition().accepts(o, processingContext);
  }

  @Override
  public ElementPatternCondition<PsiLiteralExpression> getCondition() {
    return patternCondition;
  }
}
