package edu.usc.softarch.arcade.topics.exceptions;

/**
 * Thrown when an error occurs during the comparison of two probability
 * distributions
 * 
 * @author Marcelo Schmitt Laser
 */
public class DistributionSizeMismatchException extends Exception {
  static final long serialVersionUID = 1L;

  public DistributionSizeMismatchException() { super(); }
  public DistributionSizeMismatchException(String message) { super(message); }
}