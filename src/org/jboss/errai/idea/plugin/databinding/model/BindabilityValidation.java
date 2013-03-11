package org.jboss.errai.idea.plugin.databinding.model;

/**
* @author Mike Brock
*/
public class BindabilityValidation {
  boolean valid;
  String expectedWidgetType;

  public BindabilityValidation() {
  }

  public BindabilityValidation(boolean valid) {
    this.valid = valid;
  }

  public boolean isValid() {
    return valid;
  }

  public String getExpectedWidgetType() {
    return expectedWidgetType;
  }

  public String getSimpleExpectedWidgetName() {
    if (expectedWidgetType == null) return null;
    return expectedWidgetType.substring(expectedWidgetType.lastIndexOf('.') + 1);
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public void setExpectedWidgetType(String expectedWidgetType) {
    this.expectedWidgetType = expectedWidgetType;
  }
}
