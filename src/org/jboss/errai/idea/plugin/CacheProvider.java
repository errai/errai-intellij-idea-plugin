package org.jboss.errai.idea.plugin;

/**
 * @author Mike Brock
 */
public interface CacheProvider<T> {
  public T provide();

  public boolean isCacheValid(T t);
}
