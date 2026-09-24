package validation;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.launch.platform.container.*;

/** Uses real class bytes and real Mixin, but never starts Minecraft or initializes entities. */
public final class HeadlessMixinService extends MixinServiceAbstract
        implements IClassProvider, IClassBytecodeProvider, IClassTracker {
    public static final Map<String, ClassNode> overrides = new HashMap<>();
    private final org.spongepowered.asm.launch.MixinLaunchPluginLegacy bytecodeReader;
    public HeadlessMixinService() {
        bytecodeReader = new org.spongepowered.asm.launch.MixinLaunchPluginLegacy();
        try {
            var loader = bytecodeReader.getClass().getDeclaredField("transformerLoader");
            loader.setAccessible(true);
            loader.set(bytecodeReader, (cpw.mods.modlauncher.serviceapi.ILaunchPluginService.ITransformerLoader)
                    this::readClassBytes);
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }
    public String getName() { return "StarFantasy bytecode regression"; }
    public boolean isValid() { return true; }
    public MixinEnvironment.Phase getInitialPhase() { return MixinEnvironment.Phase.DEFAULT; }
    public IClassProvider getClassProvider() { return this; }
    public IClassBytecodeProvider getBytecodeProvider() { return bytecodeReader; }
    public ITransformerProvider getTransformerProvider() { return null; }
    public IClassTracker getClassTracker() { return this; }
    public IMixinAuditTrail getAuditTrail() { return null; }
    public Collection<String> getPlatformAgents() { return List.of(); }
    public IContainerHandle getPrimaryContainer() { return new ContainerHandleVirtual("validation"); }
    public InputStream getResourceAsStream(String name) { return getClass().getClassLoader().getResourceAsStream(name); }
    public URL[] getClassPath() { return new URL[0]; }
    public Class<?> findClass(String name) throws ClassNotFoundException { return findClass(name, false); }
    public Class<?> findClass(String name, boolean init) throws ClassNotFoundException {
        return Class.forName(name, init, getClass().getClassLoader());
    }
    public Class<?> findAgentClass(String name, boolean init) throws ClassNotFoundException { return findClass(name, init); }
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException { return bytecodeReader.getClassNode(name); }
    public ClassNode getClassNode(String name, boolean transform) throws ClassNotFoundException, IOException {
        return bytecodeReader.getClassNode(name, transform);
    }
    private byte[] readClassBytes(String name) throws ClassNotFoundException {
        name = name.replace('/', '.');
        ClassNode override = overrides.get(name);
        if (override != null) { ClassWriter writer = new ClassWriter(0); override.accept(writer); return writer.toByteArray(); }
        try (InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (stream == null) throw new ClassNotFoundException(name);
            return stream.readAllBytes();
        } catch (IOException exception) { throw new ClassNotFoundException(name, exception); }
    }
    public void registerInvalidClass(String name) {}
    public boolean isClassLoaded(String name) { return false; }
    public String getClassRestrictions(String name) { return ""; }
}
