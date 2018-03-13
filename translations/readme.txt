Remove headers from tsv files, and open corde.db in sqlite:

.separator "\t"

delete FROM cardData;
.import cards/data.tsv cardData

delete FROM cardSet where language = 'en' or language = 'de';
.import expansions/translation-en.tsv cardSet
.import expansions/translation-de.tsv cardSet

delete FROM cardTrans where language = 'en' or language = 'de';
.import cards/translation-en.tsv cardTrans
.import cards/translation-de.tsv cardTrans
