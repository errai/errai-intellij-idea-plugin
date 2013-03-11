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

package org.jboss.errai.idea.plugin.ui.model;

import com.intellij.psi.PsiElement;

/**
 * @author Mike Brock
 */
public class ConsolidateDataFieldElementResult {
  private final String name;
  private final String sourceName;
  private final PsiElement element;
  private PsiElement linkingElement;
  private final boolean dataFieldInClass;

  public ConsolidateDataFieldElementResult(String name, String sourceName, PsiElement element, boolean dataFieldInClass) {
    this.name = name;
    this.sourceName = sourceName;
    this.element = element;
    this.dataFieldInClass = dataFieldInClass;
  }

  public String getName() {
    return name;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setLinkingElement(PsiElement linkingElement) {
    this.linkingElement = linkingElement;
  }

  public PsiElement getLinkingElement() {
    return linkingElement != null ? linkingElement : element;
  }

  public boolean isDataFieldInClass() {
    return dataFieldInClass;
  }
}
