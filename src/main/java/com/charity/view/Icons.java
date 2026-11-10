package com.charity.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * Small set of vector icons drawn with Java2D (no image files or icon fonts needed).
 * All icons are designed on a 24x24 grid and scaled to the requested size.
 */
public final class Icons {
    public enum Kind {
        DASHBOARD, HEART, COIN, USERS, USER, FILE, SEND, CHART, USER_PLUS, LOGOUT,
        CLOCK, ARROW_UP, ARROW_DOWN, WALLET, EYE, EYE_OFF, CHECK, PLUS, TRASH, REFRESH, DOWNLOAD
    }

    private Icons() {
    }

    public static Icon of(Kind kind, int size, Color color) {
        return new VectorIcon(kind, size, color);
    }

    private record VectorIcon(Kind kind, int size, Color color) implements Icon {
        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            double s = size / 24.0;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g2);
            g2.dispose();
        }

        private void draw(Graphics2D g) {
            switch (kind) {
                case DASHBOARD -> {
                    g.draw(new RoundRectangle2D.Double(3, 3, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(14, 3, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(3, 14, 7, 7, 2, 2));
                    g.draw(new RoundRectangle2D.Double(14, 14, 7, 7, 2, 2));
                }
                case HEART -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 20.5);
                    p.curveTo(4, 14.5, 2.5, 10.5, 4.5, 7.5);
                    p.curveTo(6.5, 4.5, 10.5, 5, 12, 8.2);
                    p.curveTo(13.5, 5, 17.5, 4.5, 19.5, 7.5);
                    p.curveTo(21.5, 10.5, 20, 14.5, 12, 20.5);
                    g.draw(p);
                }
                case COIN -> {
                    g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    g.draw(new Line2D.Double(12, 7, 12, 17));
                    Path2D p = new Path2D.Double();
                    p.moveTo(14.5, 9.5);
                    p.curveTo(14, 8.5, 10, 8.3, 10, 10.3);
                    p.curveTo(10, 12.3, 14.5, 11.7, 14.5, 13.7);
                    p.curveTo(14.5, 15.7, 10.5, 15.5, 9.5, 14.5);
                    g.draw(p);
                }
                case USERS -> {
                    g.draw(new Ellipse2D.Double(8, 4, 7, 7));
                    g.draw(new Arc2D.Double(4, 13, 15, 14, 0, 180, Arc2D.OPEN));
                    g.draw(new Arc2D.Double(15.5, 5, 5, 6, 270, 180, Arc2D.OPEN));
                }
                case USER -> {
                    g.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
                    g.draw(new Arc2D.Double(4.5, 13.5, 15, 14, 0, 180, Arc2D.OPEN));
                }
                case USER_PLUS -> {
                    g.draw(new Ellipse2D.Double(5, 4, 7.5, 7.5));
                    g.draw(new Arc2D.Double(1.5, 14, 14, 12, 0, 180, Arc2D.OPEN));
                    g.draw(new Line2D.Double(19, 8, 19, 14));
                    g.draw(new Line2D.Double(16, 11, 22, 11));
                }
                case FILE -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(6, 3);
                    p.lineTo(14, 3);
                    p.lineTo(19, 8);
                    p.lineTo(19, 21);
                    p.lineTo(6, 21);
                    p.closePath();
                    g.draw(p);
                    g.draw(new Line2D.Double(14, 3, 14, 8));
                    g.draw(new Line2D.Double(14, 8, 19, 8));
                    g.draw(new Line2D.Double(9, 13, 16, 13));
                    g.draw(new Line2D.Double(9, 17, 14, 17));
                }
                case SEND -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(21, 3);
                    p.lineTo(3, 10.5);
                    p.lineTo(10.5, 13.5);
                    p.lineTo(13.5, 21);
                    p.closePath();
                    g.draw(p);
                    g.draw(new Line2D.Double(21, 3, 10.5, 13.5));
                }
                case CHART -> {
                    g.draw(new Line2D.Double(4, 20, 20, 20));
                    g.draw(new RoundRectangle2D.Double(5, 11, 3.5, 9, 1, 1));
                    g.draw(new RoundRectangle2D.Double(10.5, 5, 3.5, 15, 1, 1));
                    g.draw(new RoundRectangle2D.Double(16, 14, 3.5, 6, 1, 1));
                }
                case LOGOUT -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(10, 4);
                    p.lineTo(5, 4);
                    p.lineTo(5, 20);
                    p.lineTo(10, 20);
                    g.draw(p);
                    g.draw(new Line2D.Double(10, 12, 20, 12));
                    g.draw(new Line2D.Double(16, 8, 20, 12));
                    g.draw(new Line2D.Double(16, 16, 20, 12));
                }
                case CLOCK -> {
                    g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 7);
                    p.lineTo(12, 12);
                    p.lineTo(15.5, 14);
                    g.draw(p);
                }
                case ARROW_UP -> {
                    g.draw(new Line2D.Double(12, 20, 12, 5));
                    g.draw(new Line2D.Double(6, 11, 12, 5));
                    g.draw(new Line2D.Double(18, 11, 12, 5));
                }
                case ARROW_DOWN -> {
                    g.draw(new Line2D.Double(12, 4, 12, 19));
                    g.draw(new Line2D.Double(6, 13, 12, 19));
                    g.draw(new Line2D.Double(18, 13, 12, 19));
                }
                case WALLET -> {
                    g.draw(new RoundRectangle2D.Double(3, 6, 18, 14, 3, 3));
                    g.draw(new Line2D.Double(5, 6, 16, 3.5));
                    g.fill(new Ellipse2D.Double(15.5, 11.5, 3, 3));
                }
                case EYE -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(2, 12);
                    p.curveTo(6, 5, 18, 5, 22, 12);
                    p.curveTo(18, 19, 6, 19, 2, 12);
                    g.draw(p);
                    g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                }
                case EYE_OFF -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(2, 12);
                    p.curveTo(6, 5, 18, 5, 22, 12);
                    p.curveTo(18, 19, 6, 19, 2, 12);
                    g.draw(p);
                    g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                    g.draw(new Line2D.Double(4, 20, 20, 4));
                }
                case CHECK -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(5, 12.5);
                    p.lineTo(10, 17.5);
                    p.lineTo(19, 7);
                    g.draw(p);
                }
                case PLUS -> {
                    g.draw(new Line2D.Double(12, 5, 12, 19));
                    g.draw(new Line2D.Double(5, 12, 19, 12));
                }
                case TRASH -> {
                    g.draw(new Line2D.Double(4, 7, 20, 7));
                    g.draw(new Line2D.Double(9, 7, 9, 4));
                    g.draw(new Line2D.Double(9, 4, 15, 4));
                    g.draw(new Line2D.Double(15, 4, 15, 7));
                    Path2D p = new Path2D.Double();
                    p.moveTo(6, 7);
                    p.lineTo(7, 20);
                    p.lineTo(17, 20);
                    p.lineTo(18, 7);
                    g.draw(p);
                }
                case REFRESH -> {
                    g.draw(new Arc2D.Double(4, 4, 16, 16, 40, 270, Arc2D.OPEN));
                    g.draw(new Line2D.Double(18.5, 3, 18.5, 8));
                    g.draw(new Line2D.Double(18.5, 8, 13.5, 8));
                }
                case DOWNLOAD -> {
                    g.draw(new Line2D.Double(12, 4, 12, 15));
                    g.draw(new Line2D.Double(7, 10, 12, 15));
                    g.draw(new Line2D.Double(17, 10, 12, 15));
                    g.draw(new Line2D.Double(5, 20, 19, 20));
                }
            }
        }
    }
}
