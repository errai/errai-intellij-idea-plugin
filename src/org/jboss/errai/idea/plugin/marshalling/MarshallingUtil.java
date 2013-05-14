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

package org.jboss.errai.idea.plugin.marshalling;

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static com.intellij.psi.search.searches.AnnotatedElementsSearch.searchPsiClasses;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Query;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class MarshallingUtil {
  public static Set<String> getAllProprtableTypes(Project project) {
    final Set<String> bindableTypes = new HashSet<String>();
    bindableTypes.addAll(getConfiguredPortableTypes(project));
    bindableTypes.addAll(getAllClasspathMarshallers(project));
    return bindableTypes;
  }

  public static Set<String> getConfiguredPortableTypes(Project project) {
    final Set<String> bindableTypes = new HashSet<String>();

    for (PsiFile file : Util.getAllErraiAppProperties(project)) {
      for (IProperty property
          : PropertiesUtil.findAllProperties(project,
          PropertiesUtil.getResourceBundle(file), "errai.marshalling.serializableTypes")) {
        final String value = property.getValue();
        if (value != null) {
          for (String s : value.split("\\s+")) {
            bindableTypes.add(s.trim());
          }
        }
      }
    }

    return bindableTypes;
  }

  public static Set<String> getAllClasspathMarshallers(Project project) {
    Set<String> exposed = new HashSet<String>();

    final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope allScope = allScope(project);

    final PsiClass clientMarAnno = instance.findClass(Types.CLIENT_MARSHALLER, allScope);

    if (clientMarAnno == null) {
      return Collections.emptySet();
    }

    final PsiClass serverMarAnno = instance.findClass(Types.SERVER_MARSHALLER, allScope);

    final Query<PsiClass> psiClasses = searchPsiClasses(clientMarAnno, allScope(project));
    for (PsiClass psiClass : psiClasses) {
      final PsiAnnotation element = Util.getAnnotationFromElement(psiClass, Types.CLIENT_MARSHALLER);
      final PsiAnnotationMemberValue declaredAttributeValue = element.findDeclaredAttributeValue("value");

      if (declaredAttributeValue == null) {
        continue;
      }

      String fqcn = ((PsiClassObjectAccessExpression) declaredAttributeValue).getOperand()
          .getType().getCanonicalText();
      exposed.add(fqcn);

      final PsiAnnotation implAliases = Util.getAnnotationFromElement(psiClass, Types.IMPLEMENTATION_ALIASES);

      if (implAliases != null) {
        final PsiAnnotationMemberValue value = implAliases.findAttributeValue("value");

        if (value == null) {
          continue;
        }

        final PsiAnnotationMemberValue[] values
            = ((PsiArrayInitializerMemberValue) value).getInitializers();

        for (PsiAnnotationMemberValue v : values) {
          final String text = ((PsiClassObjectAccessExpression) v).getOperand().getType().getCanonicalText();
          exposed.add(text);
        }
      }
    }

    final PsiClass portableAnno = instance.findClass(Types.PORTABLE, allScope);
    final Query<PsiClass> psiClasses2 = searchPsiClasses(portableAnno, allScope);

    for (PsiClass psiClass : psiClasses2) {
      exposed.add(psiClass.getQualifiedName());
    }

    final PsiClass customMappingAnno = instance.findClass(Types.CUSTOM_MAPPING, allScope);
    final Query<PsiClass> psiClasses3 = searchPsiClasses(customMappingAnno, allScope);

    for (PsiClass psiClass : psiClasses3) {
      final PsiAnnotation customMapping = Util.getAnnotationFromElement(psiClass, Types.CUSTOM_MAPPING);
      final PsiAnnotationMemberValue declaredAttributeValue = customMapping.findDeclaredAttributeValue("value");

      if (declaredAttributeValue == null) {
        continue;
      }

      final PsiTypeElement operand = ((PsiClassObjectAccessExpression) declaredAttributeValue).getOperand();
      final String value = operand .getType().getCanonicalText();

      exposed.add(value);

      final PsiAnnotation inheretedMappings = Util.getAnnotationFromElement(psiClass, Types.INHERITED_MAPPINGS);

      if (inheretedMappings != null) {
        final PsiAnnotationMemberValue attributeValue = inheretedMappings.findAttributeValue("value");

        if (attributeValue == null) {
          continue;
        }

        final PsiAnnotationMemberValue[] values
            = ((PsiArrayInitializerMemberValue) attributeValue).getInitializers();

        for (PsiAnnotationMemberValue v : values) {
          final String text = ((PsiClassObjectAccessExpression) v).getOperand().getType().getCanonicalText();
          exposed.add(text);
        }
      }

    }

    return exposed;
  }
}
