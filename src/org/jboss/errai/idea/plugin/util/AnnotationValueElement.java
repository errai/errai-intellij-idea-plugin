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

import com.intellij.psi.PsiElement;

/**
* @author Mike Brock
*/
public class AnnotationValueElement {
  private final String value;
  private final PsiElement logicalElement;

  public AnnotationValueElement(String value, PsiElement logicalElement) {
    this.value = value;
    this.logicalElement = logicalElement;
  }


  public String getValue() {
    return value;
  }

  public PsiElement getLogicalElement() {
    return logicalElement;
  }
}
