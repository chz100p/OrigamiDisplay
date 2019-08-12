// This file is part of Origami Editor 3D.
// Copyright (C) 2013, 2014, 2015 Bágyoni Attila <ba-sz-at@users.sourceforge.net>
// Origami Editor 3D is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http:// www.gnu.org/licenses/>.
package origamidisplay;

import origamieditor3d.origami.Camera;
import origamieditor3d.origami.Origami;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class DisplayPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public DisplayPanel() {

        super();
        ready_to_paint = false;
        PanelCamera = new Camera(0, 0, 1);
        paper_front_color = 0x0033CC;
    }

    protected Origami PanelOrigami;
    protected Camera PanelCamera;
    private boolean ready_to_paint;
    private int paper_front_color;

    public void setFrontColor(int rgb) {
        paper_front_color = rgb;
    }

    public int getFrontColor() {
        return paper_front_color;
    }

    public void update(Origami origami) {

        PanelOrigami = origami;
        ready_to_paint = true;
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (ready_to_paint) {

            PanelCamera.drawGradient(g, paper_front_color, PanelOrigami);
            PanelCamera.drawEdges(g, Color.black, PanelOrigami);
        }
    }
}
