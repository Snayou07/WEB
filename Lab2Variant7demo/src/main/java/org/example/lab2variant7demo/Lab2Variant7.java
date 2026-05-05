package org.example.lab2variant7demo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Lab2Variant7 extends JPanel implements ActionListener {
    private Timer timer;

    // Параметри анімації
    private double angle = 0; // Обертання
    private double tx = -100, ty = -100; // Рух по квадрату
    private int movementState = 0; // Стан руху (0-3)
    private final int moveSpeed = 3;
    private final int pathSize = 150;

    private static int maxWidth, maxHeight;

    public Lab2Variant7() {
        timer = new Timer(15, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Якість малювання
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        g2d.setColor(new Color(0, 121, 121));
        g2d.fillRect(0, 0, getWidth(), getHeight());


        // Використовуємо JOIN_BEVEL за варіантом
        float dash[] = {10.0f};
        BasicStroke frameStroke = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        g2d.setStroke(frameStroke);
        g2d.setColor(Color.WHITE);
        // Малюємо рамку, всередині якої буде рух (на достатній відстані від країв)
        g2d.drawRect(50, 50, maxWidth - 100, maxHeight - 100);

        // --- 2. ПІДГОТОВКА ДО АНІМАЦІЇ МАЛЮНКУ ---
        // Зберігаємо початковий стан координат
        AffineTransform oldTransform = g2d.getTransform();

        // Центруємо малюнок і додаємо рух по квадрату (Анімація №3)
        g2d.translate(maxWidth / 2 + tx, maxHeight / 2 + ty);

        // Обертання за годинниковою стрілкою (Анімація №5)
        g2d.rotate(angle);



        // Вусики (Лінії)
        g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.BLACK);
        g2d.drawLine(-100, -30, -180, -110); // Верхній
        g2d.drawLine(-100, 40, -190, 80);   // Нижній

        // Тіло - Верхня частина
        GeneralPath topBody = new GeneralPath();
        topBody.moveTo(-130, 0);
        topBody.lineTo(0, -120);
        topBody.lineTo(170, -30);
        topBody.lineTo(50, 20);
        topBody.closePath();
        g2d.setColor(Color.GREEN);
        g2d.fill(topBody);

        // Тіло - Нижня частина
        GeneralPath bottomBody = new GeneralPath();
        bottomBody.moveTo(-130, 0);
        bottomBody.lineTo(50, 20);
        bottomBody.lineTo(70, 100);
        bottomBody.lineTo(-70, 110);
        bottomBody.closePath();
        g2d.fill(bottomBody);

        // Центральна лінія
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        g2d.drawLine(-130, 0, 50, 20);

        // Хвіст із ГРАДІЄНТОМ
        GradientPaint tailGradient = new GradientPaint(65, 30, Color.YELLOW, 130, 90, Color.ORANGE, true);
        g2d.setPaint(tailGradient);
        GeneralPath tail = new GeneralPath();
        tail.moveTo(65, 30);
        tail.lineTo(130, 15);
        tail.lineTo(90, 90);
        tail.closePath();
        g2d.fill(tail);

        // Очі
        g2d.setColor(new Color(0, 100, 0)); // DARKGREEN
        g2d.fillRect(-60, -55, 12, 12); // Верхнє око
        g2d.fillRect(-70, 30, 12, 12);  // Нижнє око

        // Повертаємо координати назад
        g2d.setTransform(oldTransform);
    }

    // Логіка анімації
    @Override
    public void actionPerformed(ActionEvent e) {
        // Обертання за годинниковою стрілкою
        angle += 0.03;

        // Рух по квадрату ПРОТИ годинникової стрілки
        switch (movementState) {
            case 0: // Вниз
                ty += moveSpeed;
                if (ty >= pathSize) movementState = 1;
                break;
            case 1: // Вправо
                tx += moveSpeed;
                if (tx >= pathSize) movementState = 2;
                break;
            case 2: // Вгору
                ty -= moveSpeed;
                if (ty <= -pathSize) movementState = 3;
                break;
            case 3: // Вліво
                tx -= moveSpeed;
                if (tx <= -pathSize) movementState = 0;
                break;
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Лабораторна 2: Java2D Комаха");
        Lab2Variant7 panel = new Lab2Variant7();
        frame.add(panel);
        frame.setSize(800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Отримання розмірів для малювання
        Insets insets = frame.getInsets();
        maxWidth = frame.getWidth() - insets.left - insets.right;
        maxHeight = frame.getHeight() - insets.top - insets.bottom;
    }
}