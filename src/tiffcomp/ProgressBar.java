package tiffcomp;

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
			
			System.out.println(String.format("| %5.1f%%   NUM:[ %d / %d ]", PER, pos, full));
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
		
		System.out.println(String.format("| %5.1f%%", PER));
		}
		
	}
}
