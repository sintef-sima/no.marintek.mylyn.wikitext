package no.marintek.mylyn.wikitext.elements;

import org.eclipse.core.runtime.IProgressMonitor;


public interface TableDescription {

	/**
	 * Loads the model of this table description.
	 * As this is potentially a long running task, a IProgressMonitor is provided.
	 * 
	 * @param monitor
	 */
	public void loadModel(IProgressMonitor monitor);

	/**
	 * @throws IllegalStateException
	 *             if the model has not been loaded.
	 */
	public int getNumRows() throws IllegalStateException;

	/**
	 * @throws IllegalStateException
	 *             if the model has not been loaded.
	 */
	public int getNumColumns() throws IllegalStateException;

	/**
	 * @throws IllegalStateException
	 *             if the model has not been loaded.
	 */
	public TableCellDescription getCell(int iRow, int iCol) throws IllegalStateException;

}