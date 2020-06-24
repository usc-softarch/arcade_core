package edu.usc.softarch.arcade.topics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;

public class AlphabeticOnlyPipe extends Pipe implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 311790690585484614L;
	
	
	public Instance pipe(Instance carrier) {
		
		if (carrier.getData() instanceof String) {
			String data = (String) carrier.getData();
			List<String> splitTokens = new ArrayList<String>();
			for (String w : data.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
		        splitTokens.add(w);
		    }
			carrier.setData(Joiner.on(" ").join(splitTokens));
		}
		else {
			throw new IllegalArgumentException("CamelCaseSeparatorPipe expects a String, found a " + carrier.getData().getClass());
		}

		return carrier;
	}

}
