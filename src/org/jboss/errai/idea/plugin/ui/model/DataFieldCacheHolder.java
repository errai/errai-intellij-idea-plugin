package org.jboss.errai.idea.plugin.ui.model;

import org.jboss.errai.idea.plugin.ui.TemplateDataField;

import java.util.Map;

/**
* @author Mike Brock
*/
public class DataFieldCacheHolder {
  private final long time;
  private final Map<String, TemplateDataField> value;

  public DataFieldCacheHolder(long time, Map<String, TemplateDataField> value) {
    this.time = time;
    this.value = value;
  }

  public long getTime() {
    return time;
  }

  public Map<String, TemplateDataField> getValue() {
    return value;
  }
}
