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

package org.jboss.errai.idea.plugin;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jboss.errai.idea.plugin.databinding.inspection.BoundFieldValidityInspection;
import org.jboss.errai.idea.plugin.databinding.inspection.BoundModelValidInspection;
import org.jboss.errai.idea.plugin.databinding.inspection.DataBinderCanBeModelInspection;
import org.jboss.errai.idea.plugin.databinding.inspection.ModelSetterProxyableInspection;
import org.jboss.errai.idea.plugin.databinding.inspection.ModelSetterValidityInspection;
import org.jboss.errai.idea.plugin.ui.inspection.UIDataFieldInitProblemsInspection;
import org.jboss.errai.idea.plugin.ui.inspection.UITemplateExistenceInspection;
import org.jboss.errai.idea.plugin.ui.inspection.UITemplateIsValidWidgetInspection;
import org.jboss.errai.idea.plugin.ui.inspection.UiDataFieldIsValidInspection;
import org.jboss.errai.idea.plugin.ui.inspection.UiEventHandlerInspection;

/**
 * @author Mike Brock
 */
public class ErraiUIInspectionToolProvider implements InspectionToolProvider {
  @Override
  public Class[] getInspectionClasses() {
    return new Class[]{
        UIDataFieldInitProblemsInspection.class,
        UITemplateExistenceInspection.class,
        UITemplateIsValidWidgetInspection.class,
        UiDataFieldIsValidInspection.class,
        UiEventHandlerInspection.class,
        BoundFieldValidityInspection.class,
        BoundModelValidInspection.class,
        DataBinderCanBeModelInspection.class,
        ModelSetterValidityInspection.class,
        ModelSetterProxyableInspection.class
    };
  }
}
