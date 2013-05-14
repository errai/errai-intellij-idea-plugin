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

/**
 * @author Mike Brock
 */
public class Types {
  public static final String JAVAX_INJECT = "javax.inject.Inject";

  public static final String ENTRY_POINT = "org.jboss.errai.ioc.client.api.EntryPoint";

  public static final String TEMPLATED = "org.jboss.errai.ui.shared.api.annotations.Templated";
  public static final String DATAFIELD = "org.jboss.errai.ui.shared.api.annotations.DataField";
  public static final String EVENTHANDLER = "org.jboss.errai.ui.shared.api.annotations.EventHandler";
  public static final String SINKNATIVE_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.SinkNative";

  public static final String AUTO_BOUND = "org.jboss.errai.ui.shared.api.annotations.AutoBound";
  public static final String BINDABLE = "org.jboss.errai.databinding.client.api.Bindable";
  public static final String BOUND = "org.jboss.errai.ui.shared.api.annotations.Bound";
  public static final String CONVERTER = "org.jboss.errai.databinding.client.api.Converter";

  public static final String GWT_COMPOSITE = "com.google.gwt.user.client.ui.Composite";
  public static final String GWT_WIDGET_TYPE = "com.google.gwt.user.client.ui.Widget";
  public static final String GWT_ELEMENT_TYPE = "com.google.gwt.dom.client.Element";
  public static final String GWT_DOM_EVENT_TYPE = "com.google.gwt.user.client.Event";
  public static final String GWT_EVENT_TYPE = "com.google.gwt.event.shared.GwtEvent";
  public static final String GWT_HAS_TEXT = "com.google.gwt.user.client.ui.HasText";
  public static final String GWT_TAKES_VALUE = "com.google.gwt.user.client.TakesValue";

  public static final String GWT_BUTTON = "com.google.gwt.user.client.ui.Button";
  public static final String GWT_TEXTBOX = "com.google.gwt.user.client.ui.TextBox";

  public static final String GWT_CLICKEVENT = "com.google.gwt.event.dom.client.ClickEvent";

  public static final String CALLER = "org.jboss.errai.common.client.api.Caller";
  public static final String REMOTE_CALLBACK = "org.jboss.errai.common.client.api.RemoteCallback";

  public static final String PORTABLE = "org.jboss.errai.common.client.api.annotations.Portable";
  public static final String MAPS_TO = "org.jboss.errai.marshalling.client.api.annotations.MapsTo";
  public static final String CLIENT_MARSHALLER = "org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller";
  public static final String SERVER_MARSHALLER = "org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller";
  public static final String MARSHALLER = "org.jboss.errai.marshalling.client.api.Marshaller";
  public static final String IMPLEMENTATION_ALIASES = "org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases";
  public static final String CUSTOM_MAPPING = "org.jboss.errai.marshalling.rebind.api.CustomMapping";
  public static final String INHERITED_MAPPINGS = "org.jboss.errai.marshalling.rebind.api.InheritedMappings";

  /**
   * 3.0 Types *
   */
  public static final String MODEL = "org.jboss.errai.ui.shared.api.annotations.Model";
  public static final String MODEL_SETTER = "org.jboss.errai.ui.shared.api.annotations.ModelSetter";
}
