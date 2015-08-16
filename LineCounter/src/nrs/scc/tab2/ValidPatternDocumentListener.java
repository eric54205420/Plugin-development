/*
 *  SourceCodeCounter
 *  Copyright (C) 2009 Nick Sydenham <nsydenham@yahoo.co.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nrs.scc.tab2;

import java.awt.Color;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import nrs.scc.SourceCodeCounterView;

/**
 * Used to check that a regular expression is valid
 * 
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class ValidPatternDocumentListener extends InputVerifier
{
	private SourceCodeCounterView sccView;
	private Color fgColor = UIManager.getColor("TextArea.foreground");

	public ValidPatternDocumentListener(SourceCodeCounterView sccView)
	{
		this.sccView = sccView;
	}

	@Override
	public boolean verify(JComponent input)
	{
		boolean valid = false;
		JTextComponent textC = (JTextComponent)input;
		try
		{
			Pattern.compile(textC.getText());
			textC.setForeground(fgColor);
			valid = true;
		}
		catch (PatternSyntaxException psex)
		{
			textC.setForeground(Color.RED);
			sccView.setStatusMessage(psex.getDescription());
		}
		return valid;
	}
}