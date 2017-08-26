package org.kurco3.klink.sponge;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.kucro3.klink.Klink;
import org.kucro3.klink.PackLoader;
import org.kucro3.klink.SequenceUtil;
import org.kucro3.klink.Util;
import org.kucro3.klink.expression.ExpressionLibrary;
import org.kucro3.klink.expression.ExpressionLoader;
import org.kucro3.klink.expression.ExpressionPackLoader;
import org.kucro3.klink.syntax.Sequence;

public class KlinkSpongePackLoader implements PackLoader {
	private KlinkSpongePackLoader()
	{
	}
	
	public static KlinkSpongePackLoader getInstance()
	{
		return INSTANCE;
	}
	
	public Optional<Closeable> load(Klink sys, ExpressionLibrary lib, String filename)
	{
		return load(sys, lib, new File(filename));
	}
	
	public Optional<Closeable> load(Klink sys, ExpressionLibrary lib, File file)
	{
		if(ADD_URL == null)
			throw new IllegalStateException("Failed to initialize");
		try {
			URLClassLoader classLoader = (URLClassLoader) KlinkSpongePackLoader.class.getClassLoader();
			ADD_URL.invoke(classLoader, file.toURI().toURL());
			JarFile jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry(ExpressionPackLoader.RESOURCE_CATALOG);
			try {
				if(entry == null)
					throw ExpressionPackLoader.CatalogNotFound(file);
				InputStream is = jar.getInputStream(entry);
				Sequence seq = SequenceUtil.readFrom(is);
				while(seq.hasNext())
					ExpressionLoader.load(sys, lib, classLoader.loadClass(seq.next()));
			} finally {
				jar.close();
			}
		} catch (IOException e) {
			throw Util.IOException(e);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// unused
		} catch (ClassNotFoundException e) {
			throw ExpressionPackLoader.ClassNotFound(e.getMessage());
		}
		
		return Optional.empty();
	}
	
	private static final KlinkSpongePackLoader INSTANCE = new KlinkSpongePackLoader();
	
	private static final Method ADD_URL;
	
	static {
		Method mthd;
		try {
			mthd = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			mthd.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			// ignored
			mthd = null;
		}
		ADD_URL = mthd;
	}
}