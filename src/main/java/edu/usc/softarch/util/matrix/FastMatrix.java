package edu.usc.softarch.util.matrix;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FastMatrix<T extends Comparable<T>, G extends Comparable<G>> {
	//region ATTRIBUTES
	private static final int threadCount = 8;

	private final Map<T, Map<T, Cell<T, G>>> matrix;
	private final ConcurrentSkipListSet<Cell<T, G>> fastMatrix;
	private final CellValueCalculator<T, G> calculator;
	private final CellValueValidator<T, G> validator;
	//endregion

	//region CONSTRUCTORS
	public FastMatrix(CellValueCalculator<T, G> calculator,
			CellValueValidator<T, G> validator) {
		this.matrix = new ConcurrentHashMap<>();
		this.fastMatrix = new ConcurrentSkipListSet<>();
		this.calculator = calculator;
		this.validator = validator;
	}
	//endregion

	//region ACCESSORS
	public int size() { return matrix.size(); }

	public Collection<Map<T, Cell<T, G>>> getColumns() {
		return this.matrix.values(); }

	public void addColumn(T value)
			throws ExecutionException, InterruptedException {
		Runnable[] tasks = new Runnable[threadCount];
		CompletableFuture<?>[] futures = new CompletableFuture<?>[threadCount];
		ArrayBlockingQueue<Map.Entry<T, Map<T, Cell<T, G>>>> rows
			= new ArrayBlockingQueue<>(matrix.size());
		rows.addAll(matrix.entrySet());
		for (int i = 0; i < threadCount; i++) {
			tasks[i] = () -> {
				try {
					while (true) {
						Map.Entry<T, Map<T, Cell<T, G>>> row =
							rows.poll(1L, TimeUnit.MICROSECONDS);
						if (row == null) return;
						Cell<T, G> cellData = computeCellData(row.getKey(), value);
						if (this.validator.validateCellValue(cellData)) {
							row.getValue().put(value, cellData);
							this.fastMatrix.add(cellData);
						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e); //TODO handle it
				}
			};
			futures[i] = CompletableFuture.runAsync(tasks[i]);
		}

		// Wait until done
		CompletableFuture.allOf(futures).get();
	}

	public void addRow(T c) throws ExecutionException, InterruptedException {
		// Create new row
		Map<T, Cell<T, G>> newRow = new ConcurrentHashMap<>();
		this.matrix.put(c, newRow);
		Map<T, Cell<T, G>> row = this.matrix.get(c);

		// Set up threads
		Runnable[] tasks = new Runnable[threadCount];
		CompletableFuture<?>[] futures = new CompletableFuture<?>[threadCount];
		ArrayBlockingQueue<T> cols = new ArrayBlockingQueue<>(this.matrix.size());
		cols.addAll(this.matrix.values().stream().findFirst().get().keySet());
		for (int i = 0; i < threadCount; i++) {
			tasks[i] = () -> {
				try {
					while (true) {
						T col = cols.poll(1L, TimeUnit.MICROSECONDS);
						if (col == null) return;
						Cell<T, G> cellData = computeCellData(c, col);
						if (!c.equals(col) && this.validator.validateCellValue(cellData)) {
							row.put(col, cellData);
							this.fastMatrix.add(cellData);
						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e); //TODO handle it
				}
			};
			futures[i] = CompletableFuture.runAsync(tasks[i]);
		}

		// Wait until done
		CompletableFuture.allOf(futures).get();
	}
	
	public void removeRow(T row) {
		try {
			for (Cell<T, G> cell : this.matrix.get(row).values())
				this.fastMatrix.remove(cell);
		} catch (NullPointerException e) {
			e.printStackTrace(); //TODO handle it
		}
		this.matrix.remove(row);
	}
	
	public void removeColumn(T column) {
		try {
			for (Map<T, Cell<T, G>> row : this.matrix.values()) {
				Cell<T, G> cell = row.get(column);
				if (cell != null)
					this.fastMatrix.remove(cell);
				row.remove(column);
			}
		} catch (NullPointerException e) {
			e.printStackTrace(); //TODO handle it
		}
	}

	public Cell<T, G> getMinCell() { return this.fastMatrix.first(); }

	public Cell<T, G> pollMinCell() { return this.fastMatrix.pollFirst(); }
	//endregion

	//region PROCESSING
	protected Cell<T, G> computeCellData(T row, T col, int... values) {
		return new Cell<>(row, col,
			this.calculator.computeCellValue(row, col, values));
	}
	//endregion
}
