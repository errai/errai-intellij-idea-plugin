package org.jboss.errai.idea.plugin.util;

/**
 * @author Mike Brock
 */
public interface CacheProvider<T> {
  public T provide();

  public boolean isCacheValid(T t);
}
