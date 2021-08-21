package activeSegmentation.gui;

import ij.plugin.Colors;
import ij.util.Java2;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Panel;

class ColorViewer extends Panel {
    private int width = 150;
    private int height = 50;
    private Font font;
    private Color c;

    ColorViewer(Color c, double scale) {
        this.c = c;
        this.width = (int)((double)this.width * scale);
        this.height = (int)((double)this.height * scale);
        this.font = new Font("Monospaced", 0, (int)(18.0D * scale));
    }

    public Dimension getPreferredSize() {
        return new Dimension(this.width, this.height);
    }

    void setColor(Color c) {
        this.c = c;
    }

    public Dimension getMinimumSize() {
        return new Dimension(this.width, this.height);
    }

    public void paint(Graphics g) {
        g.setColor(this.c);
        g.fillRect(0, 0, this.width, this.height);
        int intensity = (this.c.getRed() + this.c.getGreen() + this.c.getBlue()) / 3;
        Color c2 = intensity < 128 ? Color.white : Color.black;
        g.setColor(c2);
        g.setFont(this.font);
        Java2.setAntialiasedText(g, true);
        String s = Colors.colorToString(this.c);
        g.drawString(s, 5, this.height - 5);
        g.setColor(Color.black);
        g.drawRect(0, 0, this.width - 1, this.height - 1);
    }
}
