package edu.usc.softarch.arcade.topics;

import java.io.Serializable;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

// code from Miroslav Batchkarov and posted http://comments.gmane.org/gmane.comp.ai.mallet.devel/1724
public class StemmerPipe extends Pipe implements Serializable {
	public Instance pipe(Instance carrier) {
		SnowballStemmer stemmer = new englishStemmer();
		TokenSequence in = (TokenSequence) carrier.getData();

		for (Token token : in) {
			stemmer.setCurrent(token.getText());
			stemmer.stem();
			token.setText(stemmer.getCurrent());
		}

		return carrier;
	}
}
