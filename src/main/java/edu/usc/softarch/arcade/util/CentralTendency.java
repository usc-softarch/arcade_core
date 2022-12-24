package edu.usc.softarch.arcade.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CentralTendency {
	//region ATTRIBUTES
	private List<Double> values;
	private double mean;
	private double stDev;
	private boolean isSorted;
	//endregion

	//region CONSTRUCTORS
	public CentralTendency(Collection<Double> values) {
		this.values = new ArrayList<>(values);
		this.isSorted = false;
		this.mean = Double.NaN;
		this.stDev = Double.NaN;
	}

	public CentralTendency(double[] values) {
		this.values = Arrays.stream(values).boxed().collect(Collectors.toList());
		this.isSorted = false;
		this.mean = Double.NaN;
		this.stDev = Double.NaN;
	}

	public CentralTendency(Double[] values) {
		this.values = List.of(values);
		this.isSorted = false;
		this.mean = Double.NaN;
		this.stDev = Double.NaN;
	}

	public CentralTendency() {
		this.values = new ArrayList<>();
		this.isSorted = true;
		this.mean = Double.NaN;
		this.stDev = Double.NaN;
	}
	//endregion

	//region ACCESSORS
	public double[] getValues() {
		return this.values.stream().mapToDouble(Double::doubleValue).toArray(); }

	public void addValue(double value) {
		this.values.add(value);
		this.isSorted = false;
		this.mean = Double.NaN;
		this.stDev = Double.NaN;
	}

	public int getN() { return this.values.size(); }

	public double getMax() {
		if (this.values.isEmpty()) return Double.NaN;

		this.sort();
		return this.values.get(this.values.size() - 1);
	}

	public double getMin() {
		if (this.values.isEmpty()) return Double.NaN;

		this.sort();
		return this.values.get(0);
	}

	public double getMedian() {
		if (this.values.isEmpty()) return Double.NaN;

		this.sort();
		return this.values.get(this.values.size() / 2);
	}

	public double getMean() {
		if (this.values.isEmpty()) return Double.NaN;

		if (Double.isNaN(this.mean)) {
			this.mean = 0.0;
			for (Double value : this.values)
				this.mean += value;
			this.mean /= this.values.size();
		}
		return this.mean;
	}

	public double getStDev() {
		if (this.values.isEmpty()) return Double.NaN;

		if (Double.isNaN(this.stDev) && !this.values.isEmpty()) {
			double numerator = 0.0;
			for (Double value : values)
				numerator += Math.pow(value - this.getMean(), 2);
			this.stDev = Math.sqrt(numerator / (this.values.size() - 1));
		}
		return this.stDev;
	}
	//endregion

	//region PROCESSING
	private void sort() {
		if (!this.isSorted)
			this.values = this.values.stream().sorted().collect(Collectors.toList());
		this.isSorted = true;
	}
	//endregion
}
