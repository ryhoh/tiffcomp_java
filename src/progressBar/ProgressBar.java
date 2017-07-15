// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package progressBar;

public interface ProgressBar {
	
	// プログレスバーを表示するインタフェース
	public static void printProgress(int pos, int full) {
		
		final double PER = (double)pos / full * 100;	// %で進行状況を保持
		
		// 不正なposを入れていないかチェック
		if(pos < 0 || full < pos) {
			System.out.println("invalid progress");
		} else {
			final int BAR = pos * 20 / full;	// 0から20までの21段階化
			
			// 表示
			System.out.print("|");
			
			for(int i = 0; i < BAR; i++) {
				System.out.print("*");
			}
			
			for(int i = BAR; i < 20; i++) {
				System.out.print("-");
			}
			
			System.out.print(String.format("| %5.1f%%   NUM:[ %d / %d ]", PER, pos, full));
			if(pos == full) {
				System.out.println("");	// 改行
			} else {
				System.out.print("\r");	// プログレスバーの更新に備えてカーソルを頭に戻す
			}
		}
	}
	
	public static void simpleBar(int pos, int full) {
		
		final double PER = (double)pos / full * 100;	// %で進行状況を保持
		
		// 不正なposを入れていないかチェック
		if(pos < 0 || full < pos) {
			System.out.println("invalid progress");
		} else {
			final int BAR = pos * 20 / full;	// 0から20までの21段階化
			
			// 表示
			System.out.print("|");
			
			for(int i = 0; i < BAR; i++) {
				System.out.print("*");
			}
			
			for(int i = BAR; i < 20; i++) {
				System.out.print("-");
			}
			
			System.out.print(String.format("| %5.1f%%", PER));
			if(pos == full) {
				System.out.println("");	// 改行
			} else {
				System.out.print("\r");	// プログレスバーの更新に備えてカーソルを頭に戻す
			}
		}
		
	}
}
