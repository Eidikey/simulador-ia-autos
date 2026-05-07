import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.io.InputStream;

public class InspectImage {
    public static void main(String[] args) {
        try (InputStream is = InspectImage.class.getResourceAsStream("/pista.png")) {
            if (is == null) {
                System.out.println("ERROR: pista.png not found in resources");
                return;
            }
            Image img = new Image(is, 800, 600, false, true);
            PixelReader pr = img.getPixelReader();
            System.out.println("Image dimensions: " + img.getWidth() + "x" + img.getHeight());
            
            // Check default spawn point (400,500)
            Color defaultSpawn = pr.getColor(400, 500);
            System.out.println("Default spawn (400,500) - R:" + defaultSpawn.getRed() + " G:" + defaultSpawn.getGreen() + " B:" + defaultSpawn.getBlue());
            
            // Search for blue pixels (spawn line)
            int blueCount = 0;
            double bestBlue = 0;
            int bestX = 0, bestY = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    Color c = pr.getColor(x, y);
                    if (c.getBlue() > 0.5 && c.getRed() < 0.5 && c.getGreen() < 0.5) {
                        blueCount++;
                        if (c.getBlue() > bestBlue) {
                            bestBlue = c.getBlue();
                            bestX = x;
                            bestY = y;
                        }
                    }
                }
            }
            System.out.println("Blue spawn pixels found: " + blueCount);
            if (blueCount > 0) {
                System.out.println("Best blue pixel at: (" + bestX + "," + bestY + ") B:" + bestBlue);
                Color bestPixel = pr.getColor(bestX, bestY);
                System.out.println("Best pixel color - R:" + bestPixel.getRed() + " G:" + bestPixel.getGreen() + " B:" + bestPixel.getBlue());
            }
            
            // Check if best spawn point is on track (green) or wall (black)
            if (blueCount > 0) {
                Color spawnColor = pr.getColor(bestX, bestY);
                System.out.println("Spawn point color - R:" + spawnColor.getRed() + " G:" + spawnColor.getGreen() + " B:" + spawnColor.getBlue());
                boolean isWall = spawnColor.getRed() < 0.1 && spawnColor.getGreen() < 0.1 && spawnColor.getBlue() < 0.1;
                System.out.println("Is spawn on wall? " + isWall);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
