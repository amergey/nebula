/****************************************************************************
 * Copyright (c) 2005-2006 Jeremy Dowdall
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeremy Dowdall <jeremyd@aspencloud.com> - initial API and implementation
 *****************************************************************************/

package org.eclipse.swt.nebula.widgets.ctabletree;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.CContainerCell;
import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.CContainerItem;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA 
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p> 
 */
public class CTableTreeCell extends CContainerCell {

	private Image[] images = new Image[0];
	private String text;
	
	private Rectangle[] iBounds = new Rectangle[0];
	private Rectangle tBounds = new Rectangle(0,0,0,0);
	
	private int[] childSpan = new int[] { -1, 1 };	// default setting keeps the child area
													// within the same column as the title area

	public CTableTreeCell(CContainerItem item, int style) {
		super(item, style);
	}

	
	public Point computeSize(int wHint, int hHint) {
		Point size = new Point(0,0);
		size.x = marginLeft + marginWidth + marginWidth + marginRight;
		if(toggleVisible || ghostToggle) size.x += toggleWidth;

		if(item.useFixedTitleHeight) {
			size.y = item.getFixedTitleHeight();
		} else {
			size.y = marginTop + marginHeight + marginHeight + marginBottom;
			if(titleArea != null) {
				size.y += titleArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			} else {
				Image[] images = getImages();
				Rectangle iBounds = null;
				if(images != null && images.length > 0) {
					iBounds = images[0].getBounds();
					for(int i = 1; i < images.length; i++) {
						Rectangle ib = images[i].getBounds();
						iBounds.width += horizontalSpacing + ib.width;
						iBounds.height = Math.max(iBounds.height, ib.height);
					}
				} else {
					iBounds = new Rectangle(0,0,1,1);
				}
				
				String text = getText();
				GC gc = CTableTree.staticGC;
				Point tSize = (text.length() > 0) ? gc.textExtent(text) : new Point(0,0);
	
				size.x += iBounds.width + ((iBounds.width > 0 && tSize.x > 0) ? horizontalSpacing : 0) + tSize.x;
				size.y += Math.max(iBounds.height, tSize.y);
			}
		}
		
		if(open && childArea != null) {
			Point childSize = childArea.computeSize(SWT.DEFAULT,SWT.DEFAULT);
			size.x = marginLeft+marginWidth+toggleWidth+Math.max(size.x, childSize.x)+marginWidth+marginRight;
			size.y += (childSpacing + childSize.y + childSpacing);
		}
		
		if(wHint != SWT.DEFAULT) {
			size.x = Math.min(size.x, wHint);
		}
		if(hHint != SWT.DEFAULT) {
			size.y = Math.min(size.y, hHint);
		}
		
		return size;
	}
	
	public int computeTitleHeight(int hHint) {
		titleHeight = marginTop + marginHeight + marginHeight + marginBottom;
		if(titleArea != null) {
			titleHeight += titleArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		} else {
			int height = 0;
			for(int i = 0; i < iBounds.length; i++) {
				height = Math.max(height, iBounds[i].height);
			}
			String text = getText();
			if(text != null) {
				GC gc = CTableTree.staticGC;
				height = Math.max(height, gc.textExtent(text).y);
			}
			titleHeight += height;
		}
		if(hHint != SWT.DEFAULT) {
			titleHeight = Math.min(titleHeight, hHint);
		}
		return titleHeight;
	}
	
	/**
	 * Get information on if and how the child area of this CTableTree will span
	 * the columns.
	 * 
	 * @return an int[] with a length of 2: int[0] represents the starting
	 *         column, and int[1] represents the number of columns, from the
	 *         start, to span.
	 * @see #setChildSpan(int, int)
	 */
	public int[] getChildSpan() {
		return childSpan;
	}

	public Image getImage() {
		if(images.length > 0) return images[0];
		return null;
	}
	
	public Image[] getImages() {
		return images;
	}
	
	public String getText() {
		return (text == null) ? "" : text;
	}
	
	protected void layout() {
		if(item.useFixedTitleHeight) titleHeight = item.getFixedTitleHeight();
		
		toggleBounds.width = Math.min(bounds.width, toggleWidth);
		toggleBounds.height = Math.min(bounds.height, titleHeight);
		
		iBounds = new Rectangle[images.length];
		for(int i = 0; i < iBounds.length; i++) {
			iBounds[i] = (images[i] != null && !images[i].isDisposed()) ? images[i].getBounds() : new Rectangle(0,0,1,1);
		}
		String text = getText();
		Point tSize = (text.length() > 0) ? 
				CTableTree.staticGC.textExtent(text, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT) : new Point(0,0);
		tBounds.width = tSize.x;
		tBounds.height = tSize.y;
		
		int x = bounds.x, y = bounds.y, width = 0, height = 0;
		
		// indent
		width = indent;
		
		// toggle
		if(ghostToggle || toggleVisible) {
			width += toggleBounds.width;
		}
		
		// images
		for(int i = 0; i < iBounds.length; i++) {
			width += iBounds[i].width; 
			if((i != iBounds.length-1) || (text.length() > 0)) width += horizontalSpacing;
			height = Math.max(height, iBounds[i].height);
		}
		
		// text
		width += tSize.x;
		height += tSize.y;

	// calculate positions
		if((style & SWT.CENTER) != 0) {
			x += ((bounds.width-(indent+marginLeft+marginWidth+width+marginWidth+marginRight))/2);
		} else if((style & SWT.RIGHT) != 0) {
			x += (bounds.width-(width+marginWidth+marginRight));
		} else { // defaults to LEFT
			x += (indent+marginLeft+marginWidth);
		}

		if(ghostToggle || toggleVisible) {
			toggleBounds.x = x - 1;
			x += toggleBounds.width;
		}
		for(int i = 0; i < iBounds.length; i++) {
			iBounds[i].x = x;
			x += iBounds[i].width;
			if((i != iBounds.length-1) || (text.length() > 0)) x += horizontalSpacing;
		}
		tBounds.x = x;
		
		if((open && childArea != null) || (style & SWT.TOP) != 0) {
			toggleBounds.y = y; // toggle centers itself, so don't add margins to it
			y += marginTop+marginHeight;
			for(int i = 0; i < iBounds.length; i++) {
				iBounds[i].y = y;
			}
			tBounds.y = y;
		} else if((style & SWT.BOTTOM) != 0) {
			toggleBounds.y = y + bounds.height-(toggleBounds.y+marginHeight+marginBottom);
			for(int i = 0; i < iBounds.length; i++) {
				iBounds[i].y = y + bounds.height-(iBounds[i].y+marginHeight+marginBottom);
			}
			tBounds.y = y + bounds.height-(tBounds.y+marginHeight+marginBottom);
		} else { // defaults to CENTER
			toggleBounds.y = y + (bounds.height-toggleBounds.height)/2;
			for(int i = 0; i < iBounds.length; i++) {
				iBounds[i].y = y + (bounds.height-iBounds[i].height)/2;
			}
			tBounds.y = y + (bounds.height-tBounds.height)/2;
		}

	// set positions of self drawing components
		if(titleArea != null) {
			Rectangle ca = getTitleClientArea();
			titleArea.setBounds(bounds.x + ca.x, bounds.y + ca.y, ca.width, ca.height);
			titleArea.layout(true, true);
		}
		
		if(childArea != null) {
			if(open && childSpan[0] < container.getColumnCount()) {
				int s0 = childSpan[0] < 0 ?
						Arrays.asList(item.getCells()).indexOf(this) :
							childSpan[0];
				int s1 = childSpan[1] < 0 ?
						container.getColumnCount() - 1 :
							s0 + childSpan[1] - 1;
				if(s1 >= container.getColumnCount()) s1 = container.getColumnCount() - 1;

				int cx = childSpan[0] < 0 ?
						bounds.x+marginWidth+toggleBounds.width :
							container.getColumn(s0).getLeft();
				int cw = container.getColumn(s1).getRight() - cx - rightChildIndent;

				childArea.setBounds(
						cx,
						bounds.y+titleHeight+childSpacing,
						cw,
						bounds.height-(titleHeight+childSpacing+childSpacing)
						);
			} else {
				childArea.setBounds(0, 0, 0, 0);
			}
		}
		
		needsLayout = false;
	}
	
	public void paint(GC gc, Rectangle eventBounds) {
//		if((getStyle() & SWT.SIMPLE) != 0) return;
		if((bounds.width <= 0) || bounds.height <= 0) return;
		
		if(needsLayout) {
			layout();
		}
		
		if(background != null) gc.setBackground(background);
		if(foreground != null) gc.setForeground(foreground);

		// background
		gc.fillRectangle(
				bounds.x-eventBounds.x,
				bounds.y-eventBounds.y,
				bounds.width,
				open ? titleHeight : bounds.height
		);

		// images
		for(int i = 0; i < iBounds.length; i++) {
			if(!images[i].isDisposed()) {
				gc.drawImage(images[i], iBounds[i].x-eventBounds.x, iBounds[i].y-eventBounds.y);
			}
		}

		// text
		if(getText().length() > 0) {
			gc.drawText(getText(), tBounds.x-eventBounds.x, tBounds.y-eventBounds.y);
		}

		// toggle (it changes the colors again so paint it last...)
		if(toggleVisible) {
			paintToggle(gc, eventBounds);
		}
	}

	public void setImage(Image image) {
		if(image == null) images = new Image[0];
		else images = new Image[] { image };
	}
	
	public void setImages(Image[] images) {
		if(images == null) {
			this.images = new Image[0];
		}
		else {
			boolean doit = true;
			for(int i = 0; i < images.length; i++) {
				if(images[i] == null || images[i].isDisposed()) doit = false;
			}
			if(doit) this.images = images;
		}
		update = true;
		container.redraw(getItem());
	}

	/**
	 * Set which columns the Child Area of this CTableTreeCell will span.<br />
	 * The default setting is: start == -1 and len == 1. To span the entire
	 * CTableTree (all columns), then use <code>setChildSpan(0, -1)</code>.
	 * 
	 * @param start
	 *            the column in which the child area will begin. A value of -1
	 *            indicates that the child area should begin in the same column
	 *            as its title area (same cell).
	 * @param len
	 *            how many columns, starting with the one specified by 'start',
	 *            the child area will span. A value of '-1' indicates that the
	 *            child area should span all the way to the end of the last
	 *            column.
	 * @see #getChildSpan()
	 */
	public void setChildSpan(int start, int len) {
		childSpan[0] = start;
		childSpan[1] = len;
	}

	public void setText(String string) {
		if(string != null && !string.equals(getText())) {
			text = string;
			update = true;
			container.redraw(getItem());
		}
	}

}
