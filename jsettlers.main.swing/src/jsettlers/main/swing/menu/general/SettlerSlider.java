/*******************************************************************************
 * Copyright (c) 2016
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.swing.menu.general;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Slider based on a progressbar, looks more like the original in the settler game
 * 
 * @author Andreas Butti
 */
public class SettlerSlider extends JProgressBar {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public SettlerSlider() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleMouseEvent(e);
			}
		});

		addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				handleMouseEvent(e);
			}
		});

	}

	/**
	 * Handle mouse events to set value
	 * 
	 * @param e
	 *            Event
	 */
	protected void handleMouseEvent(MouseEvent e) {
		if (!isEnabled()) {
			return;
		}

		// Retrieves the mouse position relative to the component origin.
		int mouseX = e.getX();

		// Computes how far along the mouse is relative to the component width then multiply it by the progress bar's maximum value.
		int progressBarVal = (int) Math.round((mouseX / (double) getWidth()) * (getMaximum() - getMinimum()) + getMinimum());

		if (progressBarVal < getMinimum()) {
			progressBarVal = getMinimum();
		}

		if (progressBarVal > getMaximum()) {
			progressBarVal = getMaximum();
		}

		setValue(progressBarVal);
	}

}
