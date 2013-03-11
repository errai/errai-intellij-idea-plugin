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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

/**
 * This class implements the matching pattern used to find XML nodes of interest (also in HTML files) that should be
 * processed by various parts of the plugin.
 *
 * @author Mike Brock
 */
public class XmlAttributeMatchingPattern implements ElementPattern<XmlAttribute> {
  private final String attributeName;
  private final ElementPatternCondition<XmlAttribute> patternCondition =
      new ElementPatternCondition<XmlAttribute>(new InitialPatternCondition<XmlAttribute>(XmlAttribute.class) {
        @Override
        public boolean accepts(@Nullable Object o, ProcessingContext context) {
          if (o instanceof XmlAttribute) {
            XmlAttribute attribute = (XmlAttribute) o;
            return attributeName.equals(attribute.getName());
          }
          else {
            return false;
          }
        }
      });

  public XmlAttributeMatchingPattern(String attribute) {
    this.attributeName = attribute;
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
  public ElementPatternCondition<XmlAttribute> getCondition() {
    return patternCondition;
  }
}
