package org.jboss.errai.idea.plugin;

import com.intellij.psi.xml.XmlTag;

import java.util.Map;

/**
* @author Mike Brock
*/
public class DataFieldCacheHolder {
  private final long time;
  private final XmlTag tag;
  private final Map<String, TemplateDataField> value;

  public DataFieldCacheHolder(long time, XmlTag tag, Map<String, TemplateDataField> value) {
    this.time = time;
    this.tag = tag;
    this.value = value;
  }

  public long getTime() {
    return time;
  }

  public XmlTag getTag() {
    return tag;
  }

  public Map<String, TemplateDataField> getValue() {
    return value;
  }
}
