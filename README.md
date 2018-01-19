# AndroidTetris
Android实现俄罗斯方块（Tetris）游戏

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}Copy
Step 2. Add the dependency

	dependencies {
	        compile 'com.github.luweibin3118:AndroidTetris:v1.0.0'
	}
	
Step 3. Add TetrisView in your layout xml


    <com.lwb.tetrislib.TetrisView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />


![image](https://github.com/luweibin3118/AndroidTetris/blob/master/app/20180120002730.jpg)


