// Copyright (c) 2018 11/16 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package progressBar;

//プログレスバーを管理・表示する
public class ProgressBar {
	int pos=0, full, step = 20;
	
	public ProgressBar(int full) {
	    if(full < 1) {
	        throw new IllegalArgumentException();
        }
	    this.full = full;
	}
	
	public ProgressBar(int pos, int full, int step) {
		this(full);

		if(pos < 0 || full < pos) {
			throw new IllegalArgumentException();
		}
        this.pos = pos;

		if(step < 0) {
		    throw new IllegalArgumentException();
        }
		this.step = step;
	}

	public boolean click() {
        /* @param return click(posの増加)に成功したかどうか */
	    if(pos == full) return false;
	    pos++;
	    return true;
    }
	
	public void print() {
		final double PER = (double)pos / full * 100;	// %で進行状況を保持
        final int BAR = pos * step / full;

        System.out.print("\r|");

        for(int i = 0; i < BAR; i++) {
            System.out.print("*");
        }

        for(int i = BAR; i < step; i++) {
            System.out.print("-");
        }

        System.out.print(String.format("| %5.1f%%   NUM:[ %d / %d ]", PER, pos, full));
        if(pos == full) {
            System.out.println();
        }
	}
	
	public static void main(String[] args) {
		ProgressBar pb = new ProgressBar(10);
		pb.click();
		pb.print();
	}
}
