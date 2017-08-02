# tiffcomp_java

## 概要

もともとc言語で書かれていたtiffcompをJavaで書き直したものです。  
Javaで書き直したことによって、**windowsを含む様々なプラットフォーム上で動作するように**なりました。  
また、このプログラムの一部（クラス）を再利用して別のプログラムを作ることも容易になりました。  

## 使い方

	>cd bin
	>java tiffcomp.Main (引数)
引数に、処理する.tiffファイルへのパスを半角スペース区切りで列挙すると、それらを処理します。  
（ファイルが2つ未満だとエラーを吐きます）