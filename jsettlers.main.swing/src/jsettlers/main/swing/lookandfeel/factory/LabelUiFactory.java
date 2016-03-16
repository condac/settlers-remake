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
package jsettlers.main.swing.lookandfeel.factory;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import jsettlers.main.swing.lookandfeel.LFStyle;
import jsettlers.main.swing.lookandfeel.ui.SettlerLabelDynamicUi;
import jsettlers.main.swing.lookandfeel.ui.SettlerLabelUi;
import jsettlers.main.swing.lookandfeel.ui.UIDefaults;

/**
 * Label UI factory
 * 
 * @author Andreas Butti
 */
public class LabelUiFactory {

	/**
	 * Forward calls
	 */
	public static final ForwardFactory FORWARD = new ForwardFactory();

	/**
	 * Header Label
	 */
	private static final SettlerLabelUi headerLabel = new SettlerLabelUi(UIDefaults.HEADER_TEXT_COLOR, 311, 30, 210, 27);

	/**
	 * Label short
	 */
	private static final SettlerLabelUi labelShort = new SettlerLabelUi(UIDefaults.LABEL_TEXT_COLOR, 19, 324, 122, 26);

	/**
	 * Label long
	 */
	private static final SettlerLabelUi labelLong = new SettlerLabelUi(UIDefaults.LABEL_TEXT_COLOR, 311, 30, 210, 27);

	/**
	 * Label long
	 */
	private static final SettlerLabelDynamicUi labelDynamic = new SettlerLabelDynamicUi(UIDefaults.LABEL_TEXT_COLOR, 311, 30, 210, 27);

	/**
	 * Create PLAF
	 * 
	 * @param c
	 *            Component which need the UI
	 * @return UI
	 */
	public static ComponentUI createUI(JComponent c) {
		Object style = c.getClientProperty(LFStyle.KEY);
		if (LFStyle.LABEL_HEADER == style) {
			return headerLabel;
		}
		if (LFStyle.LABEL_LONG == style) {
			return labelLong;
		}
		if (LFStyle.LABEL_SHORT == style) {
			return labelShort;
		}
		if (LFStyle.LABEL_DYNAMIC == style) {
			return labelDynamic;
		}

		return FORWARD.create(c);
	}
}