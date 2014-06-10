package eu.leads.processor.common.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by vagvaz on 6/1/14.
 */
public class PrintUtilities {

  public static void printMap(Map<?, ?> map) {
    System.out.println("Map{\n");
    for ( Map.Entry<?, ?> e : map.entrySet() ) {
      System.out.println("\t " + e.getKey().toString() + "--->" + e.getValue() + "\n");
    }
    System.out.println("end of map }");
  }

  public static void printList(List<?> list) {
    System.out.println("List{");
    Iterator<?> it = list.iterator();
    while ( it.hasNext() ) {
      System.out.println("\t" + it.next().toString());
    }

    System.out.println("end of list}");
  }

  public static void printIterable(Iterator<Object> testCache) {
    System.out.println("Iterable{");
    Iterator<?> it = testCache;
    while ( it.hasNext() ) {
      System.out.println("\t" + it.next().toString());
    }
    System.out.println("end of iterable");
  }
}
