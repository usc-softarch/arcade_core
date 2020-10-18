package acdc;

/**
 * @author V. Tzerpos
 *
 */
public class IO {
  static int debug_level;
  static void put(String message, int level) {
  	if (level <= debug_level) System.out.println(message);
  }
  static void set_debug_level (int level) {
  	debug_level = level;
  }
}