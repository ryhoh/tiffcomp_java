// Copyright (c) 2017 6/22 Tetsuya Hori
// Released under the MIT license
// https://opensource.org/licenses/mit-license.php

package main

/**
 * @author tetuya
 *
 */;



import production.Composite;
import production.MovieFlameMaker;

public class Main {
	
	public static void main(String[] args) {
		
		if(args.length < 1) {
			System.out.println("Give me args!");
			System.exit(1);
		}
		
		Composite composite = new Composite(args, "output.tif");
		composite.hikakumei();
		composite.close();
		
		MovieFlameMaker maker = new MovieFlameMaker(args);
		maker.run();
		
		System.out.println("Done.");
	}

}
