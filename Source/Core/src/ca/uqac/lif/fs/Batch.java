package ca.uqac.lif.fs;

import java.util.ArrayList;
import java.util.List;

public class Batch
{
	/**
	 * The list of commands to execute.
	 */
	/*@ non_null @*/ protected List<BatchOperation> m_commands;
	
	public Batch()
	{
		super();
		m_commands = new ArrayList<BatchOperation>();
	}
	
	public static interface BatchOperation
	{
		public void execute() throws FileSystemException;
	}
}
