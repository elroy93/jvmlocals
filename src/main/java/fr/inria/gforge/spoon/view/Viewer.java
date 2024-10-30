package fr.inria.gforge.spoon.view;

import spoon.Launcher;

import java.io.File;
import java.io.IOException;

public class Viewer {

	public static void main(String[] args1) throws IOException {
		final String[] args = {
				"-i", "src/main/java/fr/inria/gforge/spoon/view/A.java",
				"--gui"
		};
		// 打印运行的路径
		File file = new File(".");
		System.out.println(file.getCanonicalFile());
		Launcher launcher = new Launcher();
		launcher.run(args);
	}
}