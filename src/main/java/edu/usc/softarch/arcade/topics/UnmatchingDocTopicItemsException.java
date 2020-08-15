package edu.usc.softarch.arcade.topics;

/**
 * Thrown when an attempt is made to merge two DocTopicItems with different
 * DocTopic numbers.
 * 
 * @author Marcelo Schmitt Laser
 */
public class UnmatchingDocTopicItemsException extends Exception {
  static final long serialVersionUID = 1L;

  public UnmatchingDocTopicItemsException() { super(); }
  public UnmatchingDocTopicItemsException(String message) { super(message); }
}