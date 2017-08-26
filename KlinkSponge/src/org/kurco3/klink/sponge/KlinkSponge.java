package org.kurco3.klink.sponge;

import java.io.File;
import java.io.IOException;

import org.kucro3.klink.Executor;
import org.kucro3.klink.Klink;
import org.kucro3.klink.Messenger;
import org.kucro3.klink.Executable;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

@Plugin(id = "klinksponge",
		name = "KlinkSponge",
		version = "1.0",
		description = "Klink bridge for McSponge",
		authors = {"Kumonda221"})
public class KlinkSponge {
	public KlinkSponge()
	{
	}
	
	@Listener
	public void onLoad(GamePreInitializationEvent event) 
	{
		ENGINE.setPackLoader(KlinkSpongePackLoader.getInstance());
		try {
			INSTANCE = this;
			ENGINE.setMessenger(new Messenger() {
				@Override
				public void warn(String msg) 
				{
					logger.warn(msg);
				}
				
				@Override
				public void info(String msg) 
				{
					logger.info(msg);
				}
			});
			ensureRootDirectory();
			ensureLibDirectory();
			ensureBootDocument();
			loadLib();
			loadBoot();
			this.initialized = true;
		} catch (Exception e) {
			logger.error("Failed to initialize", e);
		}
		checkInitialized();
		new BootExecutor().execute(boot, ENGINE); // boot
	}
	
	public void ensureRootDirectory()
	{
		ensureDirectory(ROOT);
	}
	
	public void ensureLibDirectory()
	{
		ensureDirectory(LIB);
	}
	
	void ensureDirectory(File d)
	{
		if(!d.exists() || !d.isDirectory())
			d.mkdir();
	}
	
	public void ensureBootDocument() throws IOException
	{
		ensureFile(BOOT);
	}
	
	void ensureFile(File f) throws IOException
	{
		if(!f.exists() || !f.isFile())
			f.createNewFile();
	}
	
	public Logger getLogger()
	{
		return logger;
	}
	
	public void loadLib()
	{
		File[] fs = LIB.listFiles((file) -> file.getName().endsWith(".jar"));
		for(File f : fs)
		{
			logger.info("Loading library: " + f.getName());
			ENGINE.getPackLoader().load(ENGINE, ENGINE.getExpressions(), f);
		}
	}
	
	public void loadBoot()
	{
		boot = ENGINE.compile(BOOT);
	}
	
	public void checkInitialized()
	{
		if(!initialized)
			throw new IllegalStateException("Not initialized");
	}
	
	public final File ROOT = new File(".\\Klink");
	
	public final File LIB = new File(".\\Klink\\lib");
	
	public final File BOOT = new File(".\\Klink\\boot.klnk");
	
	public final Klink ENGINE = Klink.getDefault();
	
	public static KlinkSponge INSTANCE;
	
	@Inject
	private Logger logger;
	
	private Executable boot;
	
	private boolean initialized;
	
	public class BootExecutor extends Executor
	{
		public BootExecutor()
		{
			this.setInterruptionHandler((e) -> logger.info("Boot progress interrupted by program, code: " + e.code()));
			this.setScriptExceptionHandler((e) -> logger.warn("Script exception", e));
		}
	}
}