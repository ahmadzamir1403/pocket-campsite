import java.util.*;
import java.util.jar.*;
import java.lang.classfile.*;
import javax.imageio.ImageIO;
import com.google.gson.*;
public class VerifyMod {
 public static void main(String[] args)throws Exception {
  var cf=ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(ClassHierarchyResolver.ofResourceParsing(VerifyMod.class.getClassLoader()).cached()));
  int classes=0,pngs=0,jsons=0;var names=new HashSet<String>();
  try(var jar=new JarFile(args[0])){
   var entries=jar.entries();
   while(entries.hasMoreElements()){
    var entry=entries.nextElement();String name=entry.getName();names.add(name);
    if(name.endsWith(".class")){
     byte[] bytes;try(var in=jar.getInputStream(entry)){bytes=in.readAllBytes();}
     var errors=cf.verify(bytes);if(!errors.isEmpty())throw new IllegalStateException(name+": "+errors);classes++;
    }
    if(name.endsWith(".png")){
     try(var in=jar.getInputStream(entry)){
      var image=ImageIO.read(in);int expected=name.contains("textures/item/")?64:256;
      if(image==null||image.getWidth()!=expected||image.getHeight()!=expected)throw new IllegalStateException("Invalid image "+name);pngs++;
     }
    }
    if(name.endsWith(".json")){
     try(var in=jar.getInputStream(entry)){JsonParser.parseString(new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));}jsons++;
    }
    if(name.startsWith("net/minecraft/"))throw new IllegalStateException("Compile-only Minecraft class leaked into mod");
   }
   for(String name:List.of("fabric.mod.json","assets/pocketcampsite/items/campsite_kit.json","assets/pocketcampsite/models/item/campsite_kit.json","assets/pocketcampsite/textures/item/campsite_kit.png","assets/pocketcampsite/textures/entity/pocket_campsite.png","assets/pocketcampsite/blockstates/camp_bed.json","assets/pocketcampsite/models/block/camp_bed.json","data/pocketcampsite/recipe/campsite_kit.json","com/pocketcampsite/client/PocketCampsiteClient.class","com/pocketcampsite/PocketCampsite.class"))if(!names.contains(name))throw new IllegalStateException("Missing "+name);
  }
  if(classes<10||pngs!=3)throw new IllegalStateException("Incomplete package");
  System.out.println("PASS: "+classes+" Java classes verified, "+pngs+" PNGs validated, "+jsons+" JSON resources parsed; required assets and recipe present; compile dependencies excluded.");
 }
}