// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package production;

import java.io.IOException;

import tiff.InputFile;
import tiff.OutputFile;

// Compositeクラスを利用して、だんだん光跡が伸びていくような動画を作ることに応用する
// 動画ファイルを直接吐くのは難しいので、まずは動画に使うフレーム（画像）の束を作る
public class MovieFlameMaker implements Assets {
	
	protected Composite composite;	// これを使い回しながらフレームを作る
	protected String[] fileNames;
	
	// ファイル名は"flameXX.tif"の形式とする（XXは画像の番号）
	protected String[] FILENAME = {"flame", ".tif"};
	protected int fileNumber = 0;
	
	
	public MovieFlameMaker(String[] file) {
		this.fileNames = file;
		System.out.println(file.length + " pictures loaded.");
	}
	
	public void run() {
		// 0枚目はそのまま何もしていない画像になる
		// 1枚目から最後までをそれぞれ、それまでに出力された一時ファイルと合成
		// [n-1までの合成]とn枚目を合成して[nまでの合成]とする
		// [nまでの合成]とn+1枚目を合成して[n+1までの合成]とする
		// 以下繰り返し...
		
		// ----------------------0枚目はそのままコピー---------------------
		try {
			InputFile firstInput = new InputFile(fileNames[0]);
			OutputFile firstOutput = new OutputFile(FILENAME[0] + fileNumber + FILENAME[1]);
			Assets.copyAll(firstInput, firstOutput);
			fileNumber++;
			System.out.println(1 + "/" + (this.fileNames.length) + " completed.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// ------------------------------------------------------------
		
		// 1枚目以降をその時の[それまでの合成]と合成
		for(; fileNumber < this.fileNames.length; fileNumber++) {
			// compositeに渡す配列に処理対象の名前をセット
			String[] args = {FILENAME[0] + (fileNumber-1) + FILENAME[1], fileNames[fileNumber]};
			
			composite = new Composite(args, FILENAME[0] + fileNumber + FILENAME[1]);
			composite.hikakumei();
			composite.close();
			
			System.out.println((fileNumber+1) + "/" + (this.fileNames.length) + " completed.");
		}
	}
}
