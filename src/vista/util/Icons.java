package vista.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class Icons {

    public static Icon save(int size) { return new LucideVectorIcon("save", size); }
    public static Icon save() { return save(18); }

    public static Icon trash(int size) { return new LucideVectorIcon("trash", size); }
    public static Icon trash() { return trash(18); }

    public static Icon edit(int size) { return new LucideVectorIcon("edit", size); }
    public static Icon edit() { return edit(18); }

    public static Icon search(int size) { return new LucideVectorIcon("search", size); }
    public static Icon search() { return search(18); }

    public static Icon plus(int size) { return new LucideVectorIcon("plus", size); }
    public static Icon plus() { return plus(18); }

    public static Icon settings(int size) { return new LucideVectorIcon("settings", size); }
    public static Icon settings() { return settings(18); }

    public static Icon user(int size) { return new LucideVectorIcon("user", size); }
    public static Icon user() { return user(18); }

    public static Icon logOut(int size) { return new LucideVectorIcon("logout", size); }
    public static Icon logOut() { return logOut(18); }

    public static Icon x(int size) { return new LucideVectorIcon("x", size); }
    public static Icon x() { return x(18); }

    public static Icon check(int size) { return new LucideVectorIcon("check", size); }
    public static Icon check() { return check(18); }

    public static Icon arrowLeft(int size) { return new LucideVectorIcon("arrow-left", size); }
    public static Icon arrowLeft() { return arrowLeft(18); }

    public static Icon chevronLeft(int size) { return new LucideVectorIcon("chevron-left", size); }
    public static Icon chevronLeft() { return chevronLeft(18); }

    public static Icon chevronRight(int size) { return new LucideVectorIcon("chevron-right", size); }
    public static Icon chevronRight() { return chevronRight(18); }

    public static Icon eye(int size) { return new LucideVectorIcon("eye", size); }
    public static Icon eye() { return eye(18); }

    public static Icon eyeOff(int size) { return new LucideVectorIcon("eye-off", size); }
    public static Icon eyeOff() { return eyeOff(18); }

    public static Icon info(int size) { return new LucideVectorIcon("info", size); }
    public static Icon info() { return info(18); }

    public static Icon triangleAlert(int size) { return new LucideVectorIcon("triangle-alert", size); }
    public static Icon triangleAlert() { return triangleAlert(18); }

    public static Icon house(int size) { return new LucideVectorIcon("house", size); }
    public static Icon house() { return house(18); }

    public static Icon menu(int size) { return new LucideVectorIcon("menu", size); }
    public static Icon menu() { return menu(18); }

    public static Icon refreshCw(int size) { return new LucideVectorIcon("refresh-cw", size); }
    public static Icon refreshCw() { return refreshCw(18); }

    public static Icon gift(int size) { return new LucideVectorIcon("gift", size); }
    public static Icon gift() { return gift(18); }

    public static Icon shoppingBag(int size) { return new LucideVectorIcon("shopping-bag", size); }
    public static Icon shoppingBag() { return shoppingBag(18); }

    public static Icon creditCard(int size) { return new LucideVectorIcon("credit-card", size); }
    public static Icon creditCard() { return creditCard(18); }

    public static Icon receipt(int size) { return new LucideVectorIcon("receipt", size); }
    public static Icon receipt() { return receipt(18); }

    public static Icon rotateCcw(int size) { return new LucideVectorIcon("rotate-ccw", size); }
    public static Icon rotateCcw() { return rotateCcw(18); }

    public static Icon award(int size) { return new LucideVectorIcon("award", size); }
    public static Icon award() { return award(18); }

    public static Icon utensils(int size) { return new LucideVectorIcon("utensils", size); }
    public static Icon utensils() { return utensils(18); }

    public static Icon broom(int size) { return new LucideVectorIcon("broom", size); }
    public static Icon broom() { return broom(18); }

    public static Icon clean(int size) { return broom(size); }
    public static Icon clean() { return broom(18); }

    public static Icon helpCircle(int size) { return new LucideVectorIcon("help-circle", size); }
    public static Icon helpCircle() { return helpCircle(18); }

    public static Icon boxes(int size) { return new LucideVectorIcon("boxes", size); }
    public static Icon boxes() { return boxes(18); }

    public static Icon filter(int size) { return new LucideVectorIcon("filter", size); }
    public static Icon filter() { return filter(18); }

    public static Icon filterSlash(int size) { return new LucideVectorIcon("filter-slash", size); }
    public static Icon filterSlash() { return filterSlash(18); }

    public static Icon filterX(int size) { return filterSlash(size); }
    public static Icon filterX() { return filterSlash(18); }

    public static Icon download(int size) { return new LucideVectorIcon("download", size); }
    public static Icon download() { return download(18); }

    public static class LucideVectorIcon implements Icon {

        private final String nombre;
        private final int tamano;

        public LucideVectorIcon(String nombre, int tamano) {
            this.nombre = nombre;
            this.tamano = Math.max(12, tamano);
        }

        @Override
        public int getIconWidth() {
            return tamano;
        }

        @Override
        public int getIconHeight() {
            return tamano;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                Color color = null;
                if (c != null) {
                    if (!c.isEnabled()) {
                        color = UIManager.getColor("Label.disabledForeground");
                        if (color == null) {
                            color = new Color(156, 163, 175);
                        }
                    } else {
                        color = c.getForeground();
                    }
                }
                if (color == null) {
                    color = TemaGestor.esModoOscuro() ? new Color(248, 250, 252) : new Color(15, 23, 42);
                }

                g2.setColor(color);
                g2.translate(x, y);

                double escala = (double) tamano / 24.0;
                g2.scale(escala, escala);

                float grosor = 2.0f;
                g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                renderizar(g2, nombre);
            } finally {
                g2.dispose();
            }
        }

        private void renderizar(Graphics2D g2, String id) {
            switch (id) {
                case "save": {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(15.2f, 3f);
                    p.lineTo(4.8f, 3f);
                    p.quadTo(3f, 3f, 3f, 4.8f);
                    p.lineTo(3f, 19.2f);
                    p.quadTo(3f, 21f, 4.8f, 21f);
                    p.lineTo(19.2f, 21f);
                    p.quadTo(21f, 21f, 21f, 19.2f);
                    p.lineTo(21f, 8.8f);
                    p.lineTo(17.2f, 5f);
                    p.closePath();
                    g2.draw(p);

                    Path2D.Float flap = new Path2D.Float();
                    flap.moveTo(17f, 21f);
                    flap.lineTo(17f, 14f);
                    flap.lineTo(7f, 14f);
                    flap.lineTo(7f, 21f);
                    g2.draw(flap);

                    Path2D.Float notch = new Path2D.Float();
                    notch.moveTo(7f, 3f);
                    notch.lineTo(7f, 8f);
                    notch.lineTo(14f, 8f);
                    notch.lineTo(14f, 3f);
                    g2.draw(notch);
                    break;
                }
                case "trash": {
                    g2.draw(new Line2D.Float(3f, 6f, 21f, 6f));

                    Path2D.Float handle = new Path2D.Float();
                    handle.moveTo(8f, 6f);
                    handle.lineTo(8f, 3f);
                    handle.lineTo(16f, 3f);
                    handle.lineTo(16f, 6f);
                    g2.draw(handle);

                    Path2D.Float can = new Path2D.Float();
                    can.moveTo(5.5f, 6f);
                    can.lineTo(6.5f, 19.2f);
                    can.quadTo(6.7f, 21f, 8.2f, 21f);
                    can.lineTo(15.8f, 21f);
                    can.quadTo(17.3f, 21f, 17.5f, 19.2f);
                    can.lineTo(18.5f, 6f);
                    g2.draw(can);

                    g2.draw(new Line2D.Float(10f, 10f, 10f, 17f));
                    g2.draw(new Line2D.Float(14f, 10f, 14f, 17f));
                    break;
                }
                case "edit": {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(18f, 2.5f);
                    p.lineTo(21.5f, 6f);
                    p.lineTo(7.5f, 20f);
                    p.lineTo(2.5f, 21.5f);
                    p.lineTo(4f, 16.5f);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Float(14.5f, 6f, 18f, 9.5f));
                    break;
                }
                case "plus": {
                    g2.draw(new Line2D.Float(5f, 12f, 19f, 12f));
                    g2.draw(new Line2D.Float(12f, 5f, 12f, 19f));
                    break;
                }
                case "search": {
                    g2.draw(new Ellipse2D.Float(3f, 3f, 14f, 14f));
                    g2.draw(new Line2D.Float(16.5f, 16.5f, 21f, 21f));
                    break;
                }
                case "refresh-cw": {
                    g2.draw(new Arc2D.Float(3f, 3f, 18f, 18f, 40f, 150f, Arc2D.OPEN));
                    Path2D.Float a1 = new Path2D.Float();
                    a1.moveTo(21f, 3.5f);
                    a1.lineTo(21f, 8f);
                    a1.lineTo(16.5f, 8f);
                    g2.draw(a1);

                    g2.draw(new Arc2D.Float(3f, 3f, 18f, 18f, 220f, 150f, Arc2D.OPEN));
                    Path2D.Float a2 = new Path2D.Float();
                    a2.moveTo(3f, 20.5f);
                    a2.lineTo(3f, 16f);
                    a2.lineTo(7.5f, 16f);
                    g2.draw(a2);
                    break;
                }
                case "settings": {
                    g2.draw(new Ellipse2D.Float(8.5f, 8.5f, 7f, 7f));
                    g2.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
                    g2.draw(new Line2D.Float(12f, 1.5f, 12f, 4.5f));
                    g2.draw(new Line2D.Float(12f, 19.5f, 12f, 22.5f));
                    g2.draw(new Line2D.Float(1.5f, 12f, 4.5f, 12f));
                    g2.draw(new Line2D.Float(19.5f, 12f, 22.5f, 12f));
                    g2.draw(new Line2D.Float(4.5f, 4.5f, 6.8f, 6.8f));
                    g2.draw(new Line2D.Float(17.2f, 17.2f, 19.5f, 19.5f));
                    g2.draw(new Line2D.Float(19.5f, 4.5f, 17.2f, 6.8f));
                    g2.draw(new Line2D.Float(4.5f, 19.5f, 6.8f, 17.2f));
                    break;
                }
                case "user": {
                    g2.draw(new Ellipse2D.Float(8f, 3.5f, 8f, 8f));
                    Path2D.Float body = new Path2D.Float();
                    body.moveTo(4f, 20.5f);
                    body.quadTo(4f, 15f, 12f, 15f);
                    body.quadTo(20f, 15f, 20f, 20.5f);
                    g2.draw(body);
                    break;
                }
                case "logout": {
                    Path2D.Float door = new Path2D.Float();
                    door.moveTo(10f, 3.5f);
                    door.lineTo(4.8f, 3.5f);
                    door.quadTo(3.5f, 3.5f, 3.5f, 4.8f);
                    door.lineTo(3.5f, 19.2f);
                    door.quadTo(3.5f, 20.5f, 4.8f, 20.5f);
                    door.lineTo(10f, 20.5f);
                    g2.draw(door);

                    g2.draw(new Line2D.Float(9f, 12f, 20.5f, 12f));
                    Path2D.Float tip = new Path2D.Float();
                    tip.moveTo(16f, 7.5f);
                    tip.lineTo(20.5f, 12f);
                    tip.lineTo(16f, 16.5f);
                    g2.draw(tip);
                    break;
                }
                case "x": {
                    g2.draw(new Line2D.Float(6f, 6f, 18f, 18f));
                    g2.draw(new Line2D.Float(18f, 6f, 6f, 18f));
                    break;
                }
                case "check": {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(4f, 12.5f);
                    p.lineTo(9.5f, 17.5f);
                    p.lineTo(20f, 6.5f);
                    g2.draw(p);
                    break;
                }
                case "arrow-left": {
                    g2.draw(new Line2D.Float(19f, 12f, 5f, 12f));
                    Path2D.Float tip = new Path2D.Float();
                    tip.moveTo(12f, 19f);
                    tip.lineTo(5f, 12f);
                    tip.lineTo(12f, 5f);
                    g2.draw(tip);
                    break;
                }
                case "chevron-left": {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(15f, 18f);
                    p.lineTo(9f, 12f);
                    p.lineTo(15f, 6f);
                    g2.draw(p);
                    break;
                }
                case "chevron-right": {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(9f, 18f);
                    p.lineTo(15f, 12f);
                    p.lineTo(9f, 6f);
                    g2.draw(p);
                    break;
                }
                case "eye": {
                    Path2D.Float eye = new Path2D.Float();
                    eye.moveTo(2.5f, 12f);
                    eye.quadTo(12f, 3.5f, 21.5f, 12f);
                    eye.quadTo(12f, 20.5f, 2.5f, 12f);
                    g2.draw(eye);
                    g2.draw(new Ellipse2D.Float(9f, 9f, 6f, 6f));
                    break;
                }
                case "eye-off": {
                    Path2D.Float eye = new Path2D.Float();
                    eye.moveTo(2.5f, 12f);
                    eye.quadTo(12f, 3.5f, 21.5f, 12f);
                    eye.quadTo(12f, 20.5f, 2.5f, 12f);
                    g2.draw(eye);
                    g2.draw(new Line2D.Float(3f, 3f, 21f, 21f));
                    break;
                }
                case "info": {
                    g2.draw(new Ellipse2D.Float(2f, 2f, 20f, 20f));
                    g2.draw(new Line2D.Float(12f, 7.5f, 12f, 8f));
                    g2.draw(new Line2D.Float(12f, 11f, 12f, 16.5f));
                    break;
                }
                case "triangle-alert": {
                    Path2D.Float t = new Path2D.Float();
                    t.moveTo(12f, 2.5f);
                    t.lineTo(21.5f, 19.5f);
                    t.lineTo(2.5f, 19.5f);
                    t.closePath();
                    g2.draw(t);
                    g2.draw(new Line2D.Float(12f, 8.5f, 12f, 13.5f));
                    g2.draw(new Line2D.Float(12f, 16.5f, 12f, 17f));
                    break;
                }
                case "house": {
                    Path2D.Float r = new Path2D.Float();
                    r.moveTo(3f, 10f);
                    r.lineTo(12f, 2.5f);
                    r.lineTo(21f, 10f);
                    g2.draw(r);

                    Path2D.Float b = new Path2D.Float();
                    b.moveTo(5.5f, 9.5f);
                    b.lineTo(5.5f, 20.5f);
                    b.lineTo(18.5f, 20.5f);
                    b.lineTo(18.5f, 9.5f);
                    g2.draw(b);

                    Path2D.Float d = new Path2D.Float();
                    d.moveTo(9.5f, 20.5f);
                    d.lineTo(9.5f, 14.5f);
                    d.lineTo(14.5f, 14.5f);
                    d.lineTo(14.5f, 20.5f);
                    g2.draw(d);
                    break;
                }
                case "menu": {
                    g2.draw(new Line2D.Float(4f, 6f, 20f, 6f));
                    g2.draw(new Line2D.Float(4f, 12f, 20f, 12f));
                    g2.draw(new Line2D.Float(4f, 18f, 20f, 18f));
                    break;
                }
                case "shopping-bag": {
                    Path2D.Float bag = new Path2D.Float();
                    bag.moveTo(6f, 6f);
                    bag.lineTo(3.5f, 20.5f);
                    bag.lineTo(20.5f, 20.5f);
                    bag.lineTo(18f, 6f);
                    bag.closePath();
                    g2.draw(bag);
                    g2.draw(new Arc2D.Float(9f, 2f, 6f, 8f, 0f, 180f, Arc2D.OPEN));
                    break;
                }
                case "gift": {
                    g2.draw(new RoundRectangle2D.Float(3.5f, 11f, 17f, 9.5f, 2f, 2f));
                    g2.draw(new RoundRectangle2D.Float(2.5f, 7f, 19f, 4f, 2f, 2f));
                    g2.draw(new Line2D.Float(12f, 7f, 12f, 20.5f));
                    Path2D.Float bow = new Path2D.Float();
                    bow.moveTo(12f, 7f);
                    bow.quadTo(8f, 2.5f, 6.5f, 4.5f);
                    bow.quadTo(5.5f, 6.5f, 12f, 7f);
                    bow.moveTo(12f, 7f);
                    bow.quadTo(16f, 2.5f, 17.5f, 4.5f);
                    bow.quadTo(18.5f, 6.5f, 12f, 7f);
                    g2.draw(bow);
                    break;
                }
                case "credit-card": {
                    g2.draw(new RoundRectangle2D.Float(2.5f, 5f, 19f, 14f, 3f, 3f));
                    g2.draw(new Line2D.Float(2.5f, 10f, 21.5f, 10f));
                    g2.draw(new Line2D.Float(6f, 15f, 10f, 15f));
                    break;
                }
                case "receipt": {
                    Path2D.Float p = new Path2D.Float();
                    p.moveTo(4f, 2.5f);
                    p.lineTo(20f, 2.5f);
                    p.lineTo(20f, 20.5f);
                    p.lineTo(17.3f, 19f);
                    p.lineTo(14.6f, 20.5f);
                    p.lineTo(12f, 19f);
                    p.lineTo(9.3f, 20.5f);
                    p.lineTo(6.6f, 19f);
                    p.lineTo(4f, 20.5f);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new Line2D.Float(7.5f, 7f, 16.5f, 7f));
                    g2.draw(new Line2D.Float(7.5f, 11f, 16.5f, 11f));
                    g2.draw(new Line2D.Float(7.5f, 15f, 12f, 15f));
                    break;
                }
                case "rotate-ccw": {
                    g2.draw(new Arc2D.Float(4f, 4f, 16f, 16f, 45f, 270f, Arc2D.OPEN));
                    Path2D.Float tip = new Path2D.Float();
                    tip.moveTo(4f, 10.5f);
                    tip.lineTo(4f, 4.5f);
                    tip.lineTo(10f, 4.5f);
                    g2.draw(tip);
                    break;
                }
                case "award": {
                    g2.draw(new Ellipse2D.Float(6f, 2.5f, 12f, 12f));
                    Path2D.Float rib = new Path2D.Float();
                    rib.moveTo(8.5f, 13f);
                    rib.lineTo(6f, 21.5f);
                    rib.lineTo(12f, 18.5f);
                    rib.lineTo(18f, 21.5f);
                    rib.lineTo(15.5f, 13f);
                    g2.draw(rib);
                    break;
                }
                case "utensils": {
                    g2.draw(new Line2D.Float(6f, 2.5f, 6f, 21.5f));
                    g2.draw(new Line2D.Float(3.5f, 2.5f, 3.5f, 7f));
                    g2.draw(new Line2D.Float(8.5f, 2.5f, 8.5f, 7f));
                    g2.draw(new Line2D.Float(3.5f, 7f, 8.5f, 7f));

                    Path2D.Float knife = new Path2D.Float();
                    knife.moveTo(17.5f, 2.5f);
                    knife.quadTo(19.5f, 6.5f, 17.5f, 10.5f);
                    knife.lineTo(15.5f, 10.5f);
                    knife.lineTo(15.5f, 2.5f);
                    knife.closePath();
                    knife.moveTo(16.5f, 10.5f);
                    knife.lineTo(16.5f, 21.5f);
                    g2.draw(knife);
                    break;
                }
                case "broom": {
                    g2.draw(new Line2D.Float(15f, 9f, 21.5f, 2.5f));
                    Path2D.Float cap = new Path2D.Float();
                    cap.moveTo(13f, 11f);
                    cap.lineTo(16f, 8f);
                    cap.lineTo(14f, 6f);
                    cap.lineTo(11f, 9f);
                    cap.closePath();
                    g2.draw(cap);
                    Path2D.Float escoba = new Path2D.Float();
                    escoba.moveTo(11f, 9f);
                    escoba.lineTo(16f, 14f);
                    escoba.lineTo(12.5f, 21.5f);
                    escoba.quadTo(8f, 22f, 3.5f, 20.5f);
                    escoba.quadTo(3.5f, 16f, 7f, 13f);
                    escoba.closePath();
                    g2.draw(escoba);
                    g2.draw(new Line2D.Float(8f, 15f, 6.5f, 20f));
                    g2.draw(new Line2D.Float(10.5f, 14.5f, 9.5f, 21f));
                    break;
                }
                case "help-circle": {
                    g2.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                    Path2D.Float q = new Path2D.Float();
                    q.moveTo(9.5f, 9f);
                    q.curveTo(9.5f, 7.6f, 10.6f, 6.5f, 12f, 6.5f);
                    q.curveTo(13.4f, 6.5f, 14.5f, 7.6f, 14.5f, 9f);
                    q.curveTo(14.5f, 10.5f, 13f, 11.5f, 12f, 12.5f);
                    q.lineTo(12f, 14f);
                    g2.draw(q);
                    g2.fill(new Ellipse2D.Float(11.25f, 16.5f, 1.5f, 1.5f));
                    break;
                }
                case "boxes": {
                    Path2D.Float b1 = new Path2D.Float();
                    b1.moveTo(2.5f, 7.5f);
                    b1.lineTo(12f, 2.5f);
                    b1.lineTo(21.5f, 7.5f);
                    b1.lineTo(12f, 12.5f);
                    b1.closePath();
                    g2.draw(b1);
                    g2.draw(new Line2D.Float(12f, 12.5f, 12f, 21.5f));
                    g2.draw(new Line2D.Float(2.5f, 7.5f, 2.5f, 16.5f));
                    g2.draw(new Line2D.Float(2.5f, 16.5f, 12f, 21.5f));
                    g2.draw(new Line2D.Float(21.5f, 7.5f, 21.5f, 16.5f));
                    g2.draw(new Line2D.Float(21.5f, 16.5f, 12f, 21.5f));
                    break;
                }
                case "filter": {
                    Path2D.Float f = new Path2D.Float();
                    f.moveTo(3f, 4.5f);
                    f.lineTo(21f, 4.5f);
                    f.lineTo(14f, 12.5f);
                    f.lineTo(14f, 19.5f);
                    f.lineTo(10f, 21.5f);
                    f.lineTo(10f, 12.5f);
                    f.closePath();
                    g2.draw(f);
                    break;
                }
                case "filter-slash": {
                    Path2D.Float f = new Path2D.Float();
                    f.moveTo(3f, 4.5f);
                    f.lineTo(21f, 4.5f);
                    f.lineTo(14f, 12.5f);
                    f.lineTo(14f, 19.5f);
                    f.lineTo(10f, 21.5f);
                    f.lineTo(10f, 12.5f);
                    f.closePath();
                    g2.draw(f);
                    g2.draw(new Line2D.Float(2.5f, 21.5f, 21.5f, 2.5f));
                    break;
                }
                case "download": {
                    Path2D.Float tray = new Path2D.Float();
                    tray.moveTo(4f, 15f);
                    tray.lineTo(4f, 19.5f);
                    tray.lineTo(20f, 19.5f);
                    tray.lineTo(20f, 15f);
                    g2.draw(tray);

                    g2.draw(new Line2D.Float(12f, 3.5f, 12f, 14.5f));

                    Path2D.Float arrow = new Path2D.Float();
                    arrow.moveTo(7.5f, 10.5f);
                    arrow.lineTo(12f, 15f);
                    arrow.lineTo(16.5f, 10.5f);
                    g2.draw(arrow);
                    break;
                }
                default: {
                    g2.draw(new Rectangle2D.Float(4f, 4f, 16f, 16f));
                    break;
                }
            }
        }
    }
}
