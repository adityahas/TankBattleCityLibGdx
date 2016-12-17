package com.tankbattle.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tankbattle.game.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		MyGdxGame theGame = new MyGdxGame();
		config.width = 650;
		config.height = 480;
		new LwjglApplication(theGame, config);
	}
}
