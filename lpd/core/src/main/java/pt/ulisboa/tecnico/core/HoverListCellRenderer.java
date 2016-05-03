package pt.ulisboa.tecnico.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

@SuppressWarnings("serial")
public class HoverListCellRenderer extends DefaultListCellRenderer{

	private final Color HOVER_COLOR = Color.CYAN;
	private int hoverIndex = -1;
	private MouseAdapter handler;

	@Override
	@SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (!isSelected) {
			setBackground(index == hoverIndex
					? HOVER_COLOR : list.getBackground());
		}
		return this;
	}

	public MouseAdapter getHandler(JList<String> list) {
		if (handler == null) {
			handler = new HoverMouseHandler(list);
		}
		return handler;
	}

	class HoverMouseHandler extends MouseAdapter {

		private final JList<String> list;

		public HoverMouseHandler(JList<String> list) {
			this.list = list;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setHoverIndex(-1);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int index = list.locationToIndex(e.getPoint());
			setHoverIndex(list.getCellBounds(index, index).contains(e.getPoint())
					? index : -1);
		}

		private void setHoverIndex(int index) {
			if (hoverIndex == index) return;
			hoverIndex = index;
			list.repaint();
		}
	}
}
