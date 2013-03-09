package org.jboss.errai.idea.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Mike Brock
 */
public class DataBindUtil {
  private static final int CASE_OFFSET = ('z' - 'Z');

  public static class BindabilityValidation {
    private boolean valid;
    private String expectedWidgetType;

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
  }

  public static class PropertyValidation {
    private BindabilityValidation bindabilityValidation;
    private String unresolvedPropertyElement;
    private PsiClass unresolvedParent;
    private PsiClass boundType;
    private boolean parentBindable;

    public PropertyValidation() {
    }

    public PropertyValidation(boolean parentBindable) {
      this.parentBindable = parentBindable;
    }

    public boolean hasBindabilityProblem() {
      return bindabilityValidation != null && !bindabilityValidation.isValid();
    }

    public BindabilityValidation getBindabilityValidation() {
      return bindabilityValidation;
    }

    public boolean isValid() {
      return unresolvedPropertyElement == null && unresolvedParent == null && parentBindable && (bindabilityValidation != null && bindabilityValidation.isValid());
    }

    public String getUnresolvedPropertyElement() {
      return unresolvedPropertyElement;
    }

    public PsiClass getUnresolvedParent() {
      return unresolvedParent;
    }

    public String getParentName() {
      if (unresolvedParent != null) {
        return unresolvedParent.getQualifiedName();
      }
      else {
        return "<unknown>";
      }
    }

    public PsiClass getBoundType() {
      return boundType;
    }

    public boolean isParentBindable() {
      return parentBindable;
    }
  }

  public static class PropertyInfo {
    private String propertyName;
    private PsiClass propertyType;
    private PsiElement getterElement;
    private PsiElement setterElement;

    public PsiElement getAccessorElement() {
      if (getterElement != null) {
        return getterElement;
      }
      else {
        return setterElement;
      }
    }

    public boolean isHasGetter() {
      return getterElement != null;
    }

    public boolean isHasSetter() {
      return setterElement != null;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public PsiClass getPropertyType() {
      return propertyType;
    }
  }

  public static class TemplateBindingMetaData {
    private final PsiClass templateClass;
    private final PsiClass boundClass;

    /**
     * This is so, if the user has specified more than one, we can detect and reference all of them for
     * error highlighting;
     */
    private final Collection<AnnotationSearchResult> autoBoundAnnotations;

    public TemplateBindingMetaData(PsiClass templateClass) {
      this.templateClass = templateClass;

      autoBoundAnnotations = Util.findAllAnnotatedElements(templateClass, Types.AUTO_BOUND);

      if (autoBoundAnnotations.size() == 1) {
        AnnotationSearchResult result = autoBoundAnnotations.iterator().next();
        boundClass = getErasedTypeParam(templateClass.getProject(), ((PsiVariable) result.getOwningElement()).getType().getCanonicalText());
      }
      else {
        boundClass = null;
      }
    }


    public PsiClass getTemplateClass() {
      return templateClass;
    }

    public PsiAnnotation getAutoboundAnnotation() {
      if (autoBoundAnnotations.size() == 1) {
        return autoBoundAnnotations.iterator().next().getAnnotation();
      }
      else {
        return null;
      }
    }

    public PsiElement getDataBinderElement() {
      if (autoBoundAnnotations.size() == 1) {
        return autoBoundAnnotations.iterator().next().getOwningElement();
      }
      else {
        return null;
      }
    }

    public PsiClass getBoundClass() {
      return boundClass;
    }
  }

  public static class BoundMetaData {
    private final TemplateBindingMetaData templateBindingMetaData;
    private final PsiElement owner;
    private final PsiAnnotation psiAnnotation;
    private final String property;
    private final String bindableConverter;

    public BoundMetaData(PsiElement owner) {
      this.templateBindingMetaData = getTemplateBindingMetaData(owner);
      this.owner = owner;
      this.psiAnnotation = Util.getAnnotationFromElement(owner, Types.BOUND);

      if (psiAnnotation != null) {
        property = Util.getAttributeValue(psiAnnotation, "property", DefaultPolicy.OWNER_IDENTIFIER_NAME);
        bindableConverter = Util.getAttributeValue(psiAnnotation, "converter", DefaultPolicy.NULL);
      }
      else {
        property = null;
        bindableConverter = null;
      }
    }

    public TemplateBindingMetaData getBindingMetaData() {
      return templateBindingMetaData;
    }

    public PsiAnnotation getPsiAnnotation() {
      return psiAnnotation;
    }

    public String getProperty() {
      return property;
    }

    public PsiElement getOwner() {
      return owner;
    }

    public String getBindableConverter() {
      return bindableConverter;
    }

    public PropertyValidation validateProperty() {
      final PsiClass boundClass = getBindingMetaData().getBoundClass();

      if (property != null && boundClass != null) {
        PsiClass cls = boundClass;
        for (String token : property.split("\\.")) {
          if (!Util.elementIsAnnotated(cls, Types.BINDABLE)) {
            PropertyValidation validation = new PropertyValidation();
            validation.parentBindable = false;
            validation.unresolvedParent = cls;
            validation.unresolvedPropertyElement = token;
            return validation;
          }
          PsiClass result = getBeanPropertyType(boundClass.getProject(), cls, token.trim());
          if (result == null) {
            PropertyValidation validation = new PropertyValidation();
            validation.parentBindable = true;
            validation.unresolvedParent = cls;
            validation.unresolvedPropertyElement = token;
            return validation;
          }
          cls = result;
        }

        final PropertyValidation validation = new PropertyValidation(true);

        PsiVariable variable = (PsiVariable) owner;
        final PsiClass widgetType = getPsiClassFromType(owner.getProject(), variable.getType());
        if (widgetType != null) {
          validation.bindabilityValidation = typeIsBindableToWidget(cls, widgetType);
        }

        validation.boundType = cls;

        return validation;
      }
      else {
        return new PropertyValidation(false);
      }
    }
  }

  public static Map<String, PropertyInfo> getAllProperties(PsiClass boundClass, String propertySearchRoot) {
    int idx = propertySearchRoot.lastIndexOf('.');
    if (idx == -1) {
      propertySearchRoot = null;
    }
    else {
      propertySearchRoot = propertySearchRoot.substring(0, idx);
    }


    PsiClass cls = boundClass;
    if (propertySearchRoot != null) {
      for (String token : propertySearchRoot.split("\\.")) {
        if (!Util.elementIsAnnotated(cls, Types.BINDABLE)) {
          cls = null;
          break;
        }
        PsiClass result = getBeanPropertyType(boundClass.getProject(), cls, token.trim());
        if (result == null) {
          cls = null;
          break;
        }
        cls = result;
      }
    }

    Map<String, PropertyInfo> properties = new LinkedHashMap<String, PropertyInfo>();
    final Map<String, PropertyInfo> allBeanProperties = getAllBeanProperties(boundClass.getProject(), cls);
    final String prefix = propertySearchRoot != null ? propertySearchRoot + "." : "";
    for (Map.Entry<String, PropertyInfo> entry : allBeanProperties.entrySet()) {
      properties.put(prefix + entry.getKey(), entry.getValue());
    }
    return properties;
  }


  public static BoundMetaData getBoundMetaData(PsiElement element) {
    return new BoundMetaData(Util.getImmediateOwnerElement(element));
  }

  public static TemplateBindingMetaData getTemplateBindingMetaData(PsiElement element) {
    return new TemplateBindingMetaData(PsiUtil.getTopLevelClass(element));
  }

  public static PsiClass getBeanPropertyType(Project project, PsiClass type, String property) {
    if (type == null) return null;

    final String getMethod = "get" + property;

    for (PsiMethod method : type.getAllMethods()) {
      if (method.getModifierList().hasModifierProperty("public")) {
        if (method.getName().equalsIgnoreCase(getMethod) && method.getParameterList().getParameters().length == 0) {
          return getPsiClassFromType(project, method.getReturnType());
        }
      }
    }
    return null;
  }

  private static PsiClass getPsiClassFromType(Project project, PsiType type) {
    String typeName = type.getCanonicalText();
    int paramStart = typeName.indexOf('<');
    if (paramStart != -1) {
      typeName = typeName.substring(0, paramStart);
    }


    if ("int".equals(typeName)) {
      typeName = Integer.class.getName();
    }
    else if ("boolean".equals(typeName)) {
      typeName = Boolean.class.getName();
    }
    else if ("long".equals(typeName)) {
      typeName = Long.class.getName();
    }
    else if ("double".equals(typeName)) {
      typeName = Double.class.getName();
    }
    else if ("float".equals(typeName)) {
      typeName = Float.class.getName();
    }
    else if ("short".equals(typeName)) {
      typeName = Short.class.getName();
    }
    else if ("char".equals(typeName)) {
      typeName = Character.class.getName();
    }
    else if ("byte".equals(typeName)) {
      typeName = Byte.class.getName();
    }

    return JavaPsiFacade.getInstance(project).findClass(typeName, GlobalSearchScope.allScope(project));
  }

  public static Map<String, PropertyInfo> getAllBeanProperties(Project project, PsiClass psiClass) {
    final Map<String, PropertyInfo> propertyInfoMap = new LinkedHashMap<String, PropertyInfo>();
    for (final PsiMethod method : psiClass.getAllMethods()) {
      if (method.getModifierList().hasModifierProperty("public")) {

        if (PsiUtil.getTopLevelClass(method).getQualifiedName().equals("java.lang.Object")) {
          continue;
        }

        final String property = getPropertyFromAccessor(method.getName());

        final PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length == 0
            && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {


          PsiClass type = getPsiClassFromType(project, method.getReturnType());

          final PropertyInfo info = getOrCreatePropertyInfo(propertyInfoMap, property);
          info.getterElement = method;
          if (info.propertyType == null) {
            info.propertyType = type;
          }
        }
        else if (parameters.length == 1 && method.getName().startsWith("set")) {

          PsiClass type = getPsiClassFromType(project, parameters[0].getType());
          final PropertyInfo info = getOrCreatePropertyInfo(propertyInfoMap, property);
          info.setterElement = method;
          if (info.propertyType == null) {
            info.propertyType = type;
          }
        }
      }
    }
    return propertyInfoMap;
  }

  private static PropertyInfo getOrCreatePropertyInfo(Map<String, PropertyInfo> map, String property) {
    PropertyInfo info = map.get(property);
    if (info == null) {
      map.put(property, info = new PropertyInfo());
      info.propertyName = property;
    }
    return info;
  }

  public DataBindUtil() {
  }

  @SuppressWarnings("ConstantConditions")
  public static BindabilityValidation typeIsBindableToWidget(PsiClass bindingType, PsiClass widgetType) {
    if (bindingType == null) return new BindabilityValidation(false);
    //todo: this needs to be aware of converters.

    if (bindingType.getQualifiedName().equals(String.class.getName())) {
      BindabilityValidation validation = new BindabilityValidation();

      if (!Util.typeIsAssignableFrom(widgetType, Types.GWT_HAS_TEXT)) {
        validation.valid = false;
        validation.expectedWidgetType = Types.GWT_ELEMENT_TYPE;
      }
      else {
        validation.valid = true;
      }
      return validation;
    }
    else {
      BindabilityValidation validation = new BindabilityValidation();
      final PsiClassType[] superTypes = widgetType.getSuperTypes();
      validation.valid = true;

      Stack<PsiClassType> toSearch = new Stack<PsiClassType>();
      for (PsiClassType type : superTypes) {
        toSearch.push(type);
      }

      while (!toSearch.isEmpty()) {
        PsiClassType type = toSearch.pop();
        for (PsiType psiType : type.getSuperTypes()) {
          if (psiType instanceof PsiClassType) {
            PsiClassType t = (PsiClassType) psiType;
            if (!t.getCanonicalText().equals("java.lang.Object")) {
              toSearch.push(t);
            }
          }
        }

        if (type.getCanonicalText().startsWith(Types.GWT_TAKES_VALUE)) {
          PsiClass typeParm = getErasedTypeParam(bindingType.getProject(), type.getCanonicalText());
          if (typeParm != null) {
            if (!Util.typeIsAssignableFrom(typeParm, bindingType.getQualifiedName())) {
              validation.valid = false;
              validation.expectedWidgetType = typeParm.getQualifiedName();
            }
          }
          else {
            validation.valid = false;
            validation.expectedWidgetType = "<invalid class>";
          }
          break;
        }
      }
      return validation;
    }
  }


  public static String getPropertyFromAccessor(String s) {
    char[] c = s.toCharArray();
    char[] chars;

    if (c.length > 3 && c[1] == 'e' && c[2] == 't') {
      chars = new char[c.length - 3];

      if (c[0] == 'g' || c[0] == 's') {
        if (c[3] < 'a') {
          chars[0] = (char) (c[3] + CASE_OFFSET);
        }
        else {
          chars[0] = c[3];
        }

        for (int i = 1; i < chars.length; i++) {
          chars[i] = c[i + 3];
        }

        return new String(chars);
      }
      else {
        return s;
      }
    }
    else if (c.length > 2 && c[0] == 'i' && c[1] == 's') {
      chars = new char[c.length - 2];

      if (c[2] < 'a') {
        chars[0] = (char) (c[2] + CASE_OFFSET);
      }
      else {
        chars[0] = c[2];
      }

      for (int i = 1; i < chars.length; i++) {
        chars[i] = c[i + 2];
      }

      return new String(chars);
    }
    return s;
  }

  public static PsiClass getErasedTypeParam(Project project, String signature) {
    final String typeParam;
    int typeParamBegin = signature.indexOf('<');
    if (typeParamBegin == -1) {
      typeParam = null;
    }
    else {
      String s = signature.substring(typeParamBegin + 1, signature.indexOf('>'));
      int trim = s.indexOf('<');
      if (trim != -1) {
        s = s.substring(0, trim);
      }
      typeParam = s;
    }

    if (typeParam != null) {
      return JavaPsiFacade.getInstance(project)
          .findClass(typeParam, GlobalSearchScope.allScope(project));
    }
    return null;
  }
}
