// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package production;

import java.io.IOException;

import tiff.InputFile;
import tiff.OutputFile;

// ファイル操作にまつわる便利な手続きをまとめる
public interface Assets {
	
	// あるInputFileからあるOutputFileへ全てのデータをコピー
	public static void copyAll(InputFile inputFile, OutputFile outputFile) throws IOException {
		
		// カーソルの位置を最初に合わせる
		inputFile.setCursor(0L);
		outputFile.setCursor(0L);
		
		// ループ制御用に書き込むファイルサイズを確認
		final long FILESIZE = inputFile.getLength();
		
		final int SIZE = 65536;			// 1度にコピーする情報量(byte)
		byte[] buffer = new byte[SIZE];	// コピーするデータを一時的に持つ
		
		for(int i = 0; i < FILESIZE / SIZE; i++) {
			inputFile.read(buffer, 0, SIZE);
			outputFile.write(buffer, 0, SIZE);
		}
		int loaded = inputFile.read(buffer, 0, SIZE);	// 読み込んだ数
		outputFile.write(buffer, 0, loaded);			// だけ書き込む
	}
}
