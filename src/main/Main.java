// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package main

/**
 * @author tetuya
 *
 */;



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

import tiff.Composite;

public class Main {
	
	public static void main(String[] args) {
		
		if(args.length < 1) {
			System.out.println("Give me args!");
			System.exit(1);
		}
		
		Composite composite = new Composite(args);
		composite.hikakumei();
		composite.close();
	}

}
