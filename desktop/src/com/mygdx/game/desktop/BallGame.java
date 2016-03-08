package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.*;

public class BallGame {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Ball Game Test";
		config.useGL30 = true;
		config.width = 1024;
		config.height = 768;
		new LwjglApplication(new Game(), config);
	}
}
