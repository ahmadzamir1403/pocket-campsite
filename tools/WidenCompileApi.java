import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
public class WidenCompileApi {
 static final Set<String> INNER=Set.of("net/minecraft/client/gui/screens/MenuScreens$ScreenConstructor","net/minecraft/world/inventory/MenuType$MenuSupplier");
 static int publicFlags(int flags){ return (flags & ~(ClassFile.ACC_PRIVATE|ClassFile.ACC_PROTECTED)) | ClassFile.ACC_PUBLIC; }
 public static void main(String[] args) throws Exception {
  var cf=ClassFile.of(); int changed=0;
  try(var input=new JarFile(args[0]); var output=new JarOutputStream(Files.newOutputStream(Path.of(args[1])))){
   var entries=input.entries();
   while(entries.hasMoreElements()){
    var entry=entries.nextElement(); byte[] bytes;
    try(var in=input.getInputStream(entry)){bytes=in.readAllBytes();}
    String name=entry.getName();
    if(name.equals("net/minecraft/client/gui/screens/MenuScreens.class")||name.equals("net/minecraft/world/inventory/MenuType.class")||INNER.stream().anyMatch(n->name.equals(n+".class"))){
     final String owner=name.substring(0,name.length()-6);
     bytes=cf.transformClass(cf.parse(bytes),(b,e)->{
      if(e instanceof AccessFlags a && INNER.contains(owner)){b.withFlags(publicFlags(a.flagsMask()));}
      else if(e instanceof MethodModel m && ((owner.endsWith("MenuScreens")&&m.methodName().equalsString("register"))||(owner.endsWith("MenuType")&&m.methodName().equalsString("<init>")))){
       b.transformMethod(m,(mb,me)->{if(me instanceof AccessFlags a)mb.withFlags(publicFlags(a.flagsMask()));else mb.with(me);});
      }else if(e instanceof InnerClassesAttribute a){
       b.with(InnerClassesAttribute.of(a.classes().stream().map(i->INNER.contains(i.innerClass().asInternalName())?InnerClassInfo.of(i.innerClass(),i.outerClass(),i.innerName(),publicFlags(i.flagsMask())):i).toList()));
      }else b.with(e);
     });changed++;
    }
    var fresh=new JarEntry(name);fresh.setTime(entry.getTime());output.putNextEntry(fresh);output.write(bytes);output.closeEntry();
   }
  }
  if(changed!=4)throw new IllegalStateException("Expected4 compile API classes; changed "+changed);
  System.out.println("Widened4 compile-only menu API classes. Runtime original game unchanged.");
 }
}