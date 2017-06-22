// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package tiffcomp

/**
 * @author tetuya
 *
 */;

import java.io.EOFException;

/*
 *	堀哲也 2017/06/19
 *
 *	tiffcomp.c
 *
 *	TIFF形式の夜景・星景写真比較明合成バッチ処理における合成処理手続き
 *
 *	TIFFファイルが2つ与えられ、画像データ以外は全てそのまま、画像データはピクセル毎に比較明合成を行い、出力する
 *	入力ファイル1をベースとし、入力ファイル2により明るいピクセルがあれば、その部分をファイル2の内容で置き換える
 *	データのコピーは1つ目のファイルから全てコピー
 *
 *	TIFFのファイル構造については以下を参照
 *	http://symfo.web.fc2.com/blog/tiff_hex.html
 */

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main implements Composite {

	public static void main(String[] args) {
		
		// テスト用パラメータ
		//String[] names = {"ex1.tif", "ex2.tif", "ex3.tif"};
		
		// ファイルを用意
		//final int numOfImages = 3;
		final int numOfImages = args.length;
		if(numOfImages < 2) {
			System.out.println("too few images!");
			System.exit(1);
		}
		InputFile[] inputFiles = new InputFile[numOfImages];
		OutputFile outputFile = null;
		
		// ファイルオープン
		for(int i = 0; i < numOfImages; i++) {
			try {
				//inputFiles[i] = new InputFile(names[i]);
				inputFiles[i] = new InputFile(args[i]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// 出力ファイルを作成
		try {
			outputFile = new OutputFile("output.tif");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		// メタデータを渡すために、1つ目の入力ファイルから一旦全てのデータをコピーする
		System.out.println("now copying all data");
		try {
			Composite.copyAll(inputFiles[0], outputFile);
		} catch (EOFException eof) {
			// EOFに到達するが無視
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// IDFの読み込み
		for(InputFile inputFile : inputFiles) {
			try {
				inputFile.setParams();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		// 出力ファイルに必要なパラメータをセットする(1つ目の入力ファイルから)
		outputFile.setImgHeight(inputFiles[0].getImgHeight());
		outputFile.setImgWidth(inputFiles[0].getImgWidth());
		outputFile.setImgPos(inputFiles[0].getImgPos());

		// 全てのファイルの縦横の長さが同一であることを確認
		Composite.checkLength(inputFiles);
		
		// 比較明合成処理と出力処理
		
		// ここでファイルサイズが異常に増えている（上書きじゃなくて挿入？）
		try {
			Composite.dividePixels(inputFiles, outputFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// ファイルの使用を終了する
		for(InputFile inputFile : inputFiles) {
			try {
				inputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		System.out.println("done");
	}

}
