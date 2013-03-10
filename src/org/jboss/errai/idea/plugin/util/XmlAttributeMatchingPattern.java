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
