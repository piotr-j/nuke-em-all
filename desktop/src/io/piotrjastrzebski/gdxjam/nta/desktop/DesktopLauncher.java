package io.piotrjastrzebski.gdxjam.nta.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.piotrjastrzebski.gdxjam.nta.NukeGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(720, 1280);
		config.setPreferencesConfig(".nukethemall", Files.FileType.External);
		new Lwjgl3Application(new NukeGame(), config);
	}
}
