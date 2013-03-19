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

package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class ConvertibilityMetaData {
  private final Map<PsiClass, Set<PsiClass>> conversionRules = new HashMap<PsiClass, Set<PsiClass>>();

  public ConvertibilityMetaData(JavaPsiFacade facade) {
    conversionRules.putAll(getDefaultConversions(facade));
  }

  public void addConversionRule(PsiClass from, PsiClass to) {
    Set<PsiClass> set = conversionRules.get(from);
    if (set == null) {
      conversionRules.put(from, set = new HashSet<PsiClass>());
    }
    set.add(to);
  }

  public boolean canConvert(PsiClass from, PsiClass to) {
    return conversionRules.containsKey(from) && conversionRules.get(from).contains(to);
  }

  private static Map<PsiClass, Set<PsiClass>> _defaultConversionsCache;

  public static Map<PsiClass, Set<PsiClass>> getDefaultConversions(JavaPsiFacade facade) {
    if (_defaultConversionsCache == null) {
      _defaultConversionsCache = createDefaults(facade);
    }
    return _defaultConversionsCache;
  }

  private static Map<PsiClass, Set<PsiClass>> createDefaults(JavaPsiFacade facade) {
    final TypeMapBuilder resolver = new TypeMapBuilder(facade);

    resolver.addSymmetric(Integer.class, String.class);
    resolver.addSymmetric(Long.class, String.class);
    resolver.addSymmetric(Double.class, String.class);
    resolver.addSymmetric(Boolean.class, String.class);
    resolver.addSymmetric(Date.class, String.class);
    resolver.addSymmetric(BigDecimal.class, String.class);
    resolver.addSymmetric(BigInteger.class, String.class);

    return resolver.getDefaultsMap();
  }

  private static class TypeMapBuilder {
    private JavaPsiFacade facade;
    private GlobalSearchScope scope;
    private final Map<PsiClass, Set<PsiClass>> defaultsMap = new HashMap<PsiClass, Set<PsiClass>>();

    private TypeMapBuilder(JavaPsiFacade facade) {
      this.facade = facade;
      this.scope = GlobalSearchScope.allScope(facade.getProject());
    }

    public void addSymmetric(Class a, Class b) {
      add(a, b);
      add(b, a);
    }

    public void add(Class from, Class to) {
      add(from.getName(), to.getName());
    }

    public void add(String from, String to) {
      final PsiClass psiClass = get(from);

      Set<PsiClass> list = defaultsMap.get(psiClass);
      if (list == null) {
        defaultsMap.put(psiClass, list = new HashSet<PsiClass>());
      }

      list.add(get(to));
    }

    public PsiClass get(Class clazz) {
      return get(clazz.getName());
    }

    public PsiClass get(String fqcn) {
      return facade.findClass(fqcn, scope);
    }

    public Map<PsiClass, Set<PsiClass>> getDefaultsMap() {
      return defaultsMap;
    }
  }

}
