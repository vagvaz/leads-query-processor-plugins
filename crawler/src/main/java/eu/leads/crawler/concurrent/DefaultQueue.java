package eu.leads.crawler.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default queue implementation
 *
 * @author ameshkov
 */
public class DefaultQueue implements Queue {

  private ConcurrentLinkedQueue innerList = new ConcurrentLinkedQueue();

  /** {@inheritDoc} */
  public void add(Object obj) {
    innerList.add(obj);
  }

  /** {@inheritDoc} */
  public void defer(Object obj) {
    add(obj);
  }

  /** {@inheritDoc} */
  public Object poll() {
    return innerList.poll();
  }

  /** {@inheritDoc} */
  public void dispose() {
    // Do nothing
  }

  /** {@inheritDoc} */
  public int size() {
    return innerList.size();
  }
}
