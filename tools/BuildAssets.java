import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
public class BuildAssets {
 static void scale(Path source, Path dest, int size) throws Exception {
  var input=ImageIO.read(source.toFile()); var output=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
  var g=output.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  g.drawImage(input,0,0,size,size,null);g.dispose();ImageIO.write(output,"png",dest.toFile());
 }
 public static void main(String[] args) throws Exception {
  var root=Path.of(args[0]);var assets=root.resolve("src/main/resources/assets/pocketcampsite");
  scale(root.resolve("art/pocket-campsite-concept.png"),assets.resolve("textures/item/campsite_kit.png"),64);
  scale(root.resolve("art/pocket-campsite-concept.png"),assets.resolve("icon.png"),256);
  scale(root.resolve("art/pocket-campsite-materials-generated.png"),assets.resolve("textures/entity/pocket_campsite.png"),256);
  System.out.println("Prepared campsite item64x64, icon256x256 and material atlas256x256.");
 }
}