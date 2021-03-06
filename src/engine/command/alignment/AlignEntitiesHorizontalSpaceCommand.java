/*
 * Copyright (c) 2014 SSI Schaefer Noell GmbH
 *
 * $Header: $
 */

package engine.command.alignment;

import java.awt.Rectangle;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import sesion.Session;
import bridge.transferable.interfaces.CommandInterface;
import engine.views.interfaces.ViewDragableInterface;

public class AlignEntitiesHorizontalSpaceCommand implements CommandInterface {
	public static int HORIZONTAL_ELEMENT_GAP = 30;
	private ArrayList<ViewDragableInterface> alignedEntities;
	private ArrayList<Rectangle> boundsBeforeAlign;

	public AlignEntitiesHorizontalSpaceCommand(Set<ViewDragableInterface> selectedEntities) {
		alignedEntities = new ArrayList<ViewDragableInterface>();
		boundsBeforeAlign = new ArrayList<Rectangle>();

		alignedEntities.addAll(selectedEntities);
		for (ViewDragableInterface view : alignedEntities) {
			boundsBeforeAlign.add(view.getBounds());
		}
	}

	@Override
	public boolean execute() throws InvalidParameterException {
		if (!isValid())
			return false;
		// order by heigth
		Comparator<ViewDragableInterface> widthComparator = new Comparator<ViewDragableInterface>() {

			@Override
			public int compare(ViewDragableInterface o1, ViewDragableInterface o2) {
				if (o1.getBounds().x == o2.getBounds().x) {
					return 0;
				}
				if (o1.getBounds().x < o2.getBounds().x)
					return -1;

				return 1;
			}
		};
		List<ViewDragableInterface> heigthOrderEntities = new ArrayList<ViewDragableInterface>(alignedEntities);
		Collections.sort(heigthOrderEntities, widthComparator);

		// arange acording to it
		for (int i = 0; i < heigthOrderEntities.size(); i++) {
			ViewDragableInterface view = heigthOrderEntities.get(i);
			Rectangle prevBounds = view.getBounds();
			int xPos = prevBounds.x;

			if (i != 0) {
				xPos = heigthOrderEntities.get(i - 1).getX() + heigthOrderEntities.get(i - 1).getWidth() + HORIZONTAL_ELEMENT_GAP;
			}
			view.setBounds(xPos, prevBounds.y, prevBounds.width, prevBounds.height);
		}

		Session.getSelectedSheet().update();
		Session.getSelectedSheet().repaint(Session.getSelectedSheet().getVisibleRect());
		return true;
	}

	@Override
	public void undo() {
		if (alignedEntities.size() != boundsBeforeAlign.size()) {
			throw new RuntimeException("Trying to undo align left , previous bounds array and view array are distinct sizes");
		}
		for (int i = 0; i < alignedEntities.size(); i++) {
			Rectangle bounds = boundsBeforeAlign.get(i);
			alignedEntities.get(i).setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		Session.getSelectedSheet().update();
		Session.getSelectedSheet().repaint(Session.getSelectedSheet().getVisibleRect());

	}

	@Override
	public boolean isValid() {
		if (alignedEntities.size() == 0 || alignedEntities.size() < 2) {
			JOptionPane.showMessageDialog(Session.getSelectedSheet(), "There must be at lease 2 objects selected");
			return false;
		}
		return true;
	}

}
