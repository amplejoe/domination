package net.yura.domination.ui.guicommon;

import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JTextField;

public class HistoryListener extends KeyAdapter
{
	private JTextField field;
	private int pointer;
	private Vector history;
	private String tempText;
	
	public HistoryListener(JTextField field)
	{
		this.field = field;
		history = new Vector();
		pointer = -1;
		field.addKeyListener(this);
	}
	
	public void addHistoryElement(String input)
	{
		history.addElement(input);
	}
	
	public int getHistorySize()
	{
		return history.size();
	}
	
	public void clearHistory()
	{
		history.clear();
	}
	
	public void setPointer(int value)
	{
		pointer = value;
	}
	
	public void keyPressed(KeyEvent key)
	{

		//System.out.println("Key pressed:"+key.getKeyCode());
		if (key.getKeyCode() == 38)
		{
			// Testing.append("up key (history)\n");

			if (pointer < 0)
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				if (pointer == history.size() - 1)
				{
					tempText = field.getText();
				}
				field.setText((String) history.elementAt(pointer));
				pointer--;
			}
		}
		else if (key.getKeyCode() == 40)
		{
			// Testing.append("down key (history)\n");

			if (pointer > history.size() - 2)
			{
				Toolkit.getDefaultToolkit().beep();
			}
			else if (pointer == history.size() - 2)
			{
				field.setText(tempText);
				pointer++;
			}
			else
			{
				pointer = pointer + 2;
				field.setText((String) history.elementAt(pointer));
				pointer--;
			}

		}
		else
		{
			pointer = history.size() - 1;
		}

	}
	
	public void attachHistoryListener(JTextField field, int pointer, Vector history, String tempText)
	{
		field.addKeyListener(new KeyAdapter()
		{

			
		});
	}
}
