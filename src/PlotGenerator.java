import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Generates publication-quality PNG charts using standard Java2D (BufferedImage)
 * so that plots are reproducible without external third-party libraries.
 */
public class PlotGenerator {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 580;
    private static final int PAD_LEFT = 95;
    private static final int PAD_RIGHT = 50;
    private static final int PAD_TOP = 75;
    private static final int PAD_BOTTOM = 85;

    private static final Color[] PALETTE = {
        new Color(37, 99, 235),   // Royal Blue
        new Color(220, 38, 38),   // Crimson Red
        new Color(5, 150, 105),   // Emerald Green
        new Color(217, 119, 6),   // Amber Orange
        new Color(124, 58, 237),  // Purple
        new Color(14, 116, 144)   // Teal
    };

    public static void generateLinePlot(
            String outputPath,
            String title,
            String xAxisLabel,
            String yAxisLabel,
            int[] xValues,
            String[] seriesNames,
            double[][] seriesYValues) throws IOException {

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int plotW = WIDTH - PAD_LEFT - PAD_RIGHT;
        int plotH = HEIGHT - PAD_TOP - PAD_BOTTOM;

        // Compute max Y
        double maxY = 0.0;
        for (double[] series : seriesYValues) {
            for (double v : series) {
                if (v > maxY) {
                    maxY = v;
                }
            }
        }
        maxY = (maxY <= 0.0) ? 1.0 : maxY * 1.15;

        // Grid and Y-axis ticks
        int yTicks = 6;
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        for (int i = 0; i <= yTicks; i++) {
            double val = maxY * i / yTicks;
            int y = PAD_TOP + plotH - (int) Math.round((val / maxY) * plotH);

            g.setColor(new Color(229, 231, 235));
            g.setStroke(new BasicStroke(1.0f));
            g.drawLine(PAD_LEFT, y, PAD_LEFT + plotW, y);

            g.setColor(new Color(55, 65, 81));
            String label = val >= 100 ? String.format("%.0f", val) : String.format("%.3f", val);
            g.drawString(label, PAD_LEFT - fm.stringWidth(label) - 10, y + 4);
        }

        // X-axis positions (equally spaced categorical/log-scaled steps for clarity)
        int nPoints = xValues.length;
        int[] xCoords = new int[nPoints];
        for (int i = 0; i < nPoints; i++) {
            xCoords[i] = PAD_LEFT + (int) Math.round((i + 0.5) * plotW / nPoints);
            g.setColor(new Color(243, 244, 246));
            g.drawLine(xCoords[i], PAD_TOP, xCoords[i], PAD_TOP + plotH);

            g.setColor(new Color(31, 41, 55));
            String xLbl = String.format("%,d", xValues[i]);
            g.drawString(xLbl, xCoords[i] - fm.stringWidth(xLbl) / 2, PAD_TOP + plotH + 24);
        }

        // Axes border
        g.setColor(new Color(75, 85, 99));
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(PAD_LEFT, PAD_TOP, PAD_LEFT, PAD_TOP + plotH);
        g.drawLine(PAD_LEFT, PAD_TOP + plotH, PAD_LEFT + plotW, PAD_TOP + plotH);

        // Plot series lines and markers
        for (int s = 0; s < seriesNames.length; s++) {
            Color color = PALETTE[s % PALETTE.length];
            g.setColor(color);
            g.setStroke(new BasicStroke(2.8f));

            int prevX = -1;
            int prevY = -1;
            for (int i = 0; i < nPoints; i++) {
                double yVal = seriesYValues[s][i];
                int cx = xCoords[i];
                int cy = PAD_TOP + plotH - (int) Math.round((yVal / maxY) * plotH);
                if (prevX != -1) {
                    g.drawLine(prevX, prevY, cx, cy);
                }
                prevX = cx;
                prevY = cy;
            }

            for (int i = 0; i < nPoints; i++) {
                double yVal = seriesYValues[s][i];
                int cx = xCoords[i];
                int cy = PAD_TOP + plotH - (int) Math.round((yVal / maxY) * plotH);
                g.setColor(Color.WHITE);
                g.fillOval(cx - 6, cy - 6, 12, 12);
                g.setColor(color);
                g.fillOval(cx - 4, cy - 4, 8, 8);
            }
        }

        // Title
        g.setColor(new Color(17, 24, 39));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics titleFm = g.getFontMetrics();
        g.drawString(title, (WIDTH - titleFm.stringWidth(title)) / 2, 36);

        // X-axis label
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics axisFm = g.getFontMetrics();
        g.drawString(xAxisLabel, PAD_LEFT + (plotW - axisFm.stringWidth(xAxisLabel)) / 2, HEIGHT - 25);

        // Y-axis label (rotated)
        Graphics2D gRot = (Graphics2D) g.create();
        gRot.setFont(new Font("SansSerif", Font.BOLD, 14));
        gRot.translate(26, PAD_TOP + (plotH + axisFm.stringWidth(yAxisLabel)) / 2);
        gRot.rotate(-Math.PI / 2.0);
        gRot.drawString(yAxisLabel, 0, 0);
        gRot.dispose();

        // Legend box
        int legendX = PAD_LEFT + 18;
        int legendY = PAD_TOP + 14;
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics legFm = g.getFontMetrics();
        int maxLegWidth = 0;
        for (String name : seriesNames) {
            maxLegWidth = Math.max(maxLegWidth, legFm.stringWidth(name));
        }
        int boxW = maxLegWidth + 46;
        int boxH = seriesNames.length * 22 + 12;

        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(legendX, legendY, boxW, boxH, 8, 8);
        g.setColor(new Color(209, 213, 219));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(legendX, legendY, boxW, boxH, 8, 8);

        for (int s = 0; s < seriesNames.length; s++) {
            int rowY = legendY + 18 + s * 22;
            g.setColor(PALETTE[s % PALETTE.length]);
            g.setStroke(new BasicStroke(2.8f));
            g.drawLine(legendX + 10, rowY - 4, legendX + 28, rowY - 4);
            g.fillOval(legendX + 16, rowY - 7, 6, 6);

            g.setColor(new Color(31, 41, 55));
            g.drawString(seriesNames[s], legendX + 36, rowY);
        }

        g.dispose();

        File outFile = new File(outputPath);
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }
        ImageIO.write(image, "png", outFile);
    }
}
