package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import entity.zombie.Armor;

// Cone / ember / koran / helm yang jatuh saat armor zombie hancur
public class ArmorDropEffect extends Effect {
    private final Armor.Kind kind;
    private final double startX, startY, groundY;

    public ArmorDropEffect(Armor.Kind kind, double x, double y, double groundY) {
        super(seconds(1.0));
        this.kind = kind;
        this.startX = x;
        this.startY = y;
        this.groundY = groundY;
    }

    @Override
    public void draw(Graphics2D g) {
        double t = age;
        // lempar sedikit ke belakang (kanan) lalu jatuh dan memantul
        double x = startX + t * 1.2;
        double y = startY - 2.5 * t + 0.25 * t * t;
        double angle = Math.min(t * 0.12, 1.6);
        if (y > groundY) {
            y = groundY;
            angle = 1.6;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.4)));
        g2.translate(x, y);
        g2.rotate(angle);
        switch (kind) {
            case CONE:
                g2.setColor(new Color(245, 130, 30));
                g2.fillPolygon(new Polygon(new int[] { -10, 10, 0 }, new int[] { 10, 10, -18 }, 3));
                g2.setColor(new Color(255, 240, 230));
                g2.fillRect(-6, -2, 12, 3);
                g2.setColor(new Color(120, 60, 10));
                g2.drawPolygon(new Polygon(new int[] { -10, 10, 0 }, new int[] { 10, 10, -18 }, 3));
                break;
            case BUCKET:
                g2.setColor(new Color(170, 175, 185));
                g2.fillPolygon(new Polygon(new int[] { -12, 12, 9, -9 }, new int[] { -12, -12, 12, 12 }, 4));
                g2.setColor(new Color(90, 95, 105));
                g2.drawPolygon(new Polygon(new int[] { -12, 12, 9, -9 }, new int[] { -12, -12, 12, 12 }, 4));
                g2.drawLine(-10, -4, 10, -4);
                break;
            case HELMET:
                g2.setColor(new Color(200, 30, 30));
                g2.fillArc(-13, -12, 26, 24, 0, 180);
                g2.setColor(Color.WHITE);
                g2.drawLine(-12, 0, 12, 0);
                break;
            case PAPER:
            default:
                g2.setColor(new Color(235, 235, 225));
                g2.fillRect(-12, -9, 24, 18);
                g2.setColor(new Color(120, 120, 120));
                for (int i = -6; i <= 6; i += 4) {
                    g2.drawLine(-9, i, 9, i);
                }
                break;
        }
        g2.dispose();
    }
}
