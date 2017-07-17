// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

/*
 *	堀哲也 2017/06/19
 *
 *	tiffcomp(Java版)
 *
 *	TIFF形式の夜景・星景写真比較明合成バッチ処理における合成処理手続き
 *
 *	TIFFファイル名が複数与えられ、画像データ以外は全てそのまま、画像データはピクセル毎に比較明合成を行い、出力する
 *	
 *	TIFFのファイル構造については以下を参考にしています
 *	http://symfo.web.fc2.com/blog/tiff_hex.html
 */

package production;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import progressBar.ProgressBar;
import tiff.InputFile;
import tiff.OutputFile;

// 比較明合成処理のピクセル比較部分の手続きを実現するクラス
public class Composite implements ProgressBar, Assets {
	
	InputFile[] inputFiles;
	OutputFile outputFile = null;
	
	// 入力ファイル名を列挙した配列と、出力ファイル名として使いたい文字列
	public Composite(String[] files, String outputName) {
		
		// ファイルを用意
		if(files.length < 2) {
			System.out.println("too few files!");
			System.exit(1);
		}
		this.inputFiles = new InputFile[files.length];
		
		// ファイルオープン
		for(int i = 0; i < files.length; i++) {
			try {
				this.inputFiles[i] = new InputFile(files[i]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// 出力ファイルを作成
		try {
			this.outputFile = new OutputFile(outputName);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		//メタデータを渡すために、1つ目の入力ファイルから一旦全てのデータをコピーする
		System.out.println("now copying all data");
		try {
			Assets.copyAll(this.inputFiles[0], this.outputFile);
		} catch (EOFException eof) {
			// EOFに到達するが無視
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// IDFの読み込み
		for(InputFile inputFile : this.inputFiles) {
			try {
				inputFile.setParams();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		// 出力ファイルに必要なパラメータをセットする(1つ目の入力ファイルから)
		outputFile.setImgHeight(this.inputFiles[0].getImgHeight());
		outputFile.setImgWidth(this.inputFiles[0].getImgWidth());
		outputFile.setImgPos(this.inputFiles[0].getImgPos());

		// 全てのファイルの縦横の長さが同一であることを確認
		checkLength();
		
		//System.out.println("ready");
	}
	
	// 比較明合成を実行
	public void hikakumei() {
		int colorBit = inputFiles[0].getColorBit();
		System.out.println(colorBit + "bit");
		try {
			if(colorBit == 8) {
				dividePixels();
			} else if(colorBit == 16) {
				dividePixels16();
			} else {
				System.out.println("invalid color bit! " + colorBit);
				System.exit(1);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//System.out.println("done");
	}
	
	// ファイルの使用を終了する
	public void close() {
		for(InputFile inputFile : this.inputFiles) {
			try {
				inputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		//System.out.println("closed");
	}
	
	/* ---------------------------------------------------------以下は内部処理用の関数--------------------------------------------------------- */
	/*
	// 最初のファイルから全てのデータをコピー
	protected void copyAll() throws IOException {
		
		// カーソルの位置を最初に合わせる
		this.inputFiles[0].setCursor(0L);
		this.outputFile.setCursor(0L);
		
		// ループ制御用に書き込むファイルサイズを確認
		final long FILESIZE = this.inputFiles[0].getLength();
		
		final int SIZE = 65536;			// 1度にコピーする情報量(byte)
		byte[] buffer = new byte[SIZE];	// コピーするデータを一時的に持つ
		
		for(int i = 0; i < FILESIZE / SIZE; i++) {
			this.inputFiles[0].read(buffer, 0, SIZE);
			this.outputFile.write(buffer, 0, SIZE);
		}
		int loaded = this.inputFiles[0].read(buffer, 0, SIZE);	// 読み込んだ数
		this.outputFile.write(buffer, 0, loaded);			// だけ書き込む
	}*/
	
	// 全ての入力ファイルの縦横の長さが同一であることを確かめる
	protected void checkLength() {
		
		final int HEIGHT = this.inputFiles[0].getImgHeight();
		final int WIDTH = this.inputFiles[0].getImgWidth();
		
		for(InputFile inputFile : this.inputFiles) {
			if(HEIGHT != inputFile.getImgHeight() || WIDTH != inputFile.getImgWidth()) {
				System.out.println("invalid image length");
				System.exit(1);
			}
		}
	}
	
	// 一度に全ての画素を配列に入れるのは不可能
	// 画像を先頭からJOB画素ずつに分割して、それぞれについて処理し、逐次書き込む
	protected void dividePixels() throws IOException {
		
		// 総画素数を計算
		final int IMGSIZE = this.inputFiles[0].getImgHeight() * this.inputFiles[0].getImgWidth();
		
		final int JOB_PIXEL = 5000;							// 1回に処理する画素数
		final int JOB_BYTES = JOB_PIXEL * 3;				// その時の1回に処理するバイト数
		final int EXTRAJOB_BYTES = (IMGSIZE % JOB_PIXEL);	// 余ったバイト数だけ最後に追加処理
		final int NUMOFPHAZE = IMGSIZE / JOB_PIXEL;			// 処理の回数
		
		// 作業開始前に1度プログレスバーを見せる
		ProgressBar.printProgress(0, NUMOFPHAZE);
		
		// 全てのファイルのカーソルを画像データ開始位置へ
		for(InputFile inputFile : this.inputFiles) {
			inputFile.setCursor(inputFile.getImgPos());
		}
		this.outputFile.setCursor(this.outputFile.getImgPos());
		
		/*--------------- 処理 ---------------*/
		byte[] array = new byte[JOB_BYTES];			// ここに比較結果が格納される
		byte[] inputPixels = new byte[JOB_BYTES];	// ここに入力データが入る
		for(int i = 0; i < NUMOFPHAZE; i++) {
			clear(array);	// 配列をbyte型の最小値(-128)で埋める
			
			// 比較
			for(InputFile inputFile : this.inputFiles) {
				inputFile.read(inputPixels, 0, JOB_BYTES);
				compareLight(array, inputPixels, JOB_BYTES);
			}
			
			// 書き込み
			this.outputFile.write(array);
			
			// 途中経過を示す
			if(i % 10 == 0) {
				ProgressBar.printProgress(i, NUMOFPHAZE);
			}
		}
		/*---------- 残った端切れ部分の処理 ----------*/
		clear(array);
		
		// 比較
		try {
			for(InputFile inputFile : this.inputFiles) {
				inputFile.read(inputPixels, 0, EXTRAJOB_BYTES);
				compareLight(array, inputPixels, EXTRAJOB_BYTES);
			}
		} catch (EOFException e) {
			// 正常動作でEOFになることが想定されるので何もしない
		}
		
		// 書き込み
		this.outputFile.write(array, 0, EXTRAJOB_BYTES);
		
		/* ---------------------------------------- */
		
		// 実行完了を示す
		ProgressBar.printProgress(NUMOFPHAZE, NUMOFPHAZE);
		
	}
	
	
	// 今までの輝度のうち最も明るい数値を記録した配列arrayと、inputFileの数値を比較して、輝度の高い数値を配列に入れる
	// 初回の比較では必ず全てのデータが配列に書き込まれることになる
	// 必ずしも配列の全てを使う訳ではないので、処理するbyte数numを与える
	protected static void compareLight(byte[] base, byte[] pixels, int num) throws IOException {
		
		for(int i = 0; i < num / 3; i++) {
			
			if(culcBrightness(base[i*3], base[i*3 + 1], base[i*3 + 2]) < culcBrightness(pixels[i*3], pixels[i*3 + 1], pixels[i*3 + 2])) {
				// ピクセルを更新
				for(int j = 0; j < 3; j++) {
					base[i*3 + j] = pixels[i*3 + j];
				}
			}
		}
		
	}
	
	// rgbのそれぞれの輝度が1byteで表され渡される
	// バイナリでは0から255で表されるが、Javaの仕様ではbyte型は-128から127で渡される
	// 128を加えて符号なし数として扱い計算する
	protected static int culcBrightness(byte rb, byte gb, byte bb) {
		
		// byte型では最上位ビットは符号として処理されるが、実データは符号なし整数である
		// 本来の数をintで表す
		int r = 0xFF & rb;
		int g = 0xFF & gb;
		int b = 0xFF & bb;
		
		// pchansblog.exblog.jp/26051068/
		return 306 * r + 601 * g + 117 * b;
		// 正しい輝度としては最後に512を足すが、比較目的なので省略
	}
	
	/* ---------------------------------------------------------16bit用の処理関数--------------------------------------------------------- */
	//16bit対応版
	protected void dividePixels16() throws IOException {
		
		// 総画素数を計算
		final int IMGSIZE = this.inputFiles[0].getImgHeight() * this.inputFiles[0].getImgWidth();
		
		final int JOB_PIXEL = 5000;
		
		final int JOB_BYTES = JOB_PIXEL * 6;				// 1回に処理するバイト数
		final int EXTRAJOB_BYTES = (IMGSIZE % JOB_PIXEL);	// 余ったバイト数だけ最後に追加処理
		final int NUMOFPHAZE = IMGSIZE / JOB_PIXEL;			// 処理の回数
		
		// 作業開始前に1度プログレスバーを見せる
		ProgressBar.printProgress(0, NUMOFPHAZE);
		
		// 全てのファイルのカーソルを画像データ開始位置へ
		for(InputFile inputFile : this.inputFiles) {
			inputFile.setCursor(inputFile.getImgPos());
		}
		this.outputFile.setCursor(this.outputFile.getImgPos());
		
		/*--------------- 処理 ---------------*/
		byte[] array = new byte[JOB_BYTES];			// ここに比較結果が格納される
		byte[] inputPixels = new byte[JOB_BYTES];	// ここに入力データが入る
		for(int i = 0; i < NUMOFPHAZE; i++) {
			clear(array);	// 配列をbyte型の最小値(-128)で埋める

			// 比較
			for(InputFile inputFile : this.inputFiles) {
				inputFile.read(inputPixels, 0, JOB_BYTES);
				compareLight16(array, inputPixels, JOB_BYTES);
			}
			
			// 書き込み
			this.outputFile.write(array);
			
			// 途中経過を示す
			if(i % 10 == 0) {
				ProgressBar.printProgress(i, NUMOFPHAZE);
			}
		}
		/*---------- 残った端切れ部分の処理 ----------*/
		clear(array);
		
		// 比較
		try {
			for(InputFile inputFile : this.inputFiles) {
				inputFile.read(inputPixels, 0, EXTRAJOB_BYTES);
				compareLight16(array, inputPixels, EXTRAJOB_BYTES);
			}
		} catch (EOFException e) {
			// 正常動作でEOFになることが想定されるので何もしない
		}
		
		// 書き込み
		this.outputFile.write(array, 0, EXTRAJOB_BYTES);
		/* ---------------------------------------- */
		
		// 実行完了を示す
		ProgressBar.printProgress(NUMOFPHAZE, NUMOFPHAZE);
		
	}
	
	// 今までの輝度のうち最も明るい数値を記録した配列arrayと、inputFileの数値を比較して、輝度の高い数値を配列に入れる
	// 初回の比較では必ず全てのデータが配列に書き込まれることになる
	// 必ずしも配列の全てを使う訳ではないので、処理するbyte数numを与える
	protected static void compareLight16(byte[] base, byte[] pixels, int num) throws IOException {
		
		// 関数に渡すための配列
		byte[] baseSet = new byte[6];
		byte[] pixelSet = new byte[6];
		
		// 1ピクセル=6byteずつ見ていく
		for(int i = 0; i < num / 6; i++) {
			// 対象のデータを関数に渡す準備
			for(int j = 0; j < 6; j++) {
				baseSet[j] = base[i*6 + j];
			}
			for(int j = 0; j < 6; j++) {
				pixelSet[j] = pixels[i*6 + j];
			}
			
			if(culcBrightness16(baseSet) < culcBrightness16(pixelSet)) {
				// ピクセルを更新
				for(int j = 0; j < 6; j++) {
					base[i*6 + j] = pixels[i*6 + j];
				}
			}
		}
	}
	
	// rgbのそれぞれの輝度が2byteで表され渡される
	protected static int culcBrightness16(byte[] set) {
		// 各2byteごとにバイトオーダーを直して通常の比較に持ち込む
		
		// byte型では最上位ビットは符号として処理されるが、実データは符号なし整数である
		// 本来の数をintで表す
		int r = (0xFFFF & set[1] << 8) | (0xFF & set[0]);
		int g = (0xFFFF & set[3] << 8) | (0xFF & set[2]);
		int b = (0xFFFF & set[5] << 8) | (0xFF & set[4]);
		
		// pchansblog.exblog.jp/26051068/
		return 306 * r + 601 * g + 117 * b;
		// 正しい輝度としては最後に512を足すが、比較目的なので省略
	}

	//------------------------------------------------------------------------------------------------------------------

	protected static void clear(byte[] array) {
		// byte型の最小値で初期化
		// 全てのビットを0にする
		for(int i = 0; i < array.length; i++) {
			array[i] = 0;	// 0b00000000 にする
		}
	}
	
}
